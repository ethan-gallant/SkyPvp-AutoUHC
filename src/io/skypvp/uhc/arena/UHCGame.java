package io.skypvp.uhc.arena;

import io.skypvp.uhc.Globals;
import io.skypvp.uhc.SkyPVPUHC;
import io.skypvp.uhc.UHCScoreboard;
import io.skypvp.uhc.UHCSystem;
import io.skypvp.uhc.jedis.UHCJedis;
import io.skypvp.uhc.jedis.UHCLobbyResponse;
import io.skypvp.uhc.player.UHCPlayer;
import io.skypvp.uhc.player.UHCPlayer.PlayerState;
import io.skypvp.uhc.scenario.Scenario;
import io.skypvp.uhc.scenario.ScenarioType;
import io.skypvp.uhc.timer.MatchTimer;
import io.skypvp.uhc.timer.TimerUtils;
import io.skypvp.uhc.util.FireworkEffectBuilder;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.util.Vector;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.wimbli.WorldBorder.BorderData;

public class UHCGame {
	
	public enum GameState {
	    WAITING(0), STARTING(1), PREPARING(2), GRACE_PERIOD(3), PVP(4), DEATHMATCH(5), FINISHED(6);
		
		private final int stateIndex;
		
		private GameState(int index) {
		    this.stateIndex = index;
		}
		
		public int toIndex() {
		    return this.stateIndex;
		}
	}
	
	final SkyPVPUHC main;
	private GameState state;
	private HashSet<Scenario> scenarios;
	private MatchTimer timer;
	
	// Variables for team matches.
	private boolean isTeamMatch;
	
	// Variables for handling standings.
	private int initialPlayers;
	
	public UHCGame(SkyPVPUHC instance) {
		this.main = instance;
		this.state = GameState.WAITING;
		this.scenarios = new HashSet<Scenario>();
		this.isTeamMatch = true;
		this.initialPlayers = 0;
		
		// Let's setup the timer.
		this.timer = TimerUtils.createTimer(main, "Preparing", main.getSettings().getFreezeTime());
		
		// Let's register arena events.
		main.getServer().getPluginManager().registerEvents(new ArenaEventsListener(main, this), main);
	}
	
	/**
	 * After all our players are ready.
	 * Let's begin the match.
	 */
	
	public void startMatch() {
		setState(GameState.PREPARING);
		main.getWorldHandler().getGameWorld().getCBWorld().setTime(500L);
		
		// Let's handle the players.
		for(UHCPlayer player : main.getOnlinePlayers().values()) {
			player.setState(PlayerState.FROZEN);
			player.setInGame(true);
			player.getBukkitPlayer().getInventory().clear();
			
			for(ItemStack item : main.getProfile().getStartingItems()) {
			    player.getBukkitPlayer().getInventory().addItem(item.clone());
			    System.out.println(item.getType());
			}
			
			if(player.getTeam() != null) {
				player.getTeam().giveArmor(player);
				
				// Let's default to having team chat on.
				player.setInTeamChat(true);
			}
			
			UHCScoreboard scoreboard = new UHCScoreboard(main, "gameScoreboard", DisplaySlot.SIDEBAR);
			scoreboard.generate(player);
			player.setScoreboard(scoreboard);
		}
		
		// Let's load our scenarios.
		scenarios.clear();
		
		for(ScenarioType type : main.getProfile().getScenarios()) {
			Class<? extends Scenario> clazz = null;
			try {
				clazz = ScenarioType.getScenarioClassByType(type);
				Object[] constrNeeds = {main};
				Scenario scenario = clazz.getDeclaredConstructor(SkyPVPUHC.class).newInstance(constrNeeds);
				scenarios.add(scenario);
				main.sendConsoleMessage(ChatColor.DARK_GREEN + String.format("Successfully created new %s instance.", scenario.getType().name()));
			} catch (IllegalArgumentException e) {
				main.sendConsoleMessage(main.getMessages().color(
					String.format("&cERROR: &4Could not instantiate new Scenario from type. Error: %s", 
				e.getMessage())));
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			}
		}
		
		spawnPlayers();
		
		int[] timings = TimerUtils.convertToMinutesAndSeconds(main.getSettings().getFreezeTime());
		timer.set("Preparing", timings[0], timings[1]);
		
		new BukkitRunnable() {
			
			public void run() {
				timer.runTaskTimer(main, 0L, 20L);
			}
			
		}.runTaskLater(main, 40L);
	}
	
	public void cancelStart() {
		// Let's stop the timer.
		UHCSystem.getLobbyTimer().requestCancel();
		setState(GameState.WAITING);
		
		String msg = main.getMessages().getRawMessage("not-enough-players");
		int numPlayers = (isTeamMatch) ? main.getSettings().getMinimumTeamGamePlayers() : main.getSettings().getMinimumSoloGamePlayers();
		msg = main.getMessages().constructMessage(msg.replaceAll("\\{numPlayers\\}", String.valueOf(numPlayers)));
		
		// We don't want to iterate again if we don't have to.
		// Play the sound in here.
		for(UHCPlayer p : main.getOnlinePlayers().values()) {
			p.getBukkitPlayer().playSound(p.getBukkitPlayer().getLocation(), main.getSettings().getErrorSound(), 1F, 1F);
			p.getBukkitPlayer().sendMessage(msg);
		}
	}
	
	public void spawnPlayers() {
		if(!isTeamMatch) {
			spawnPlayers(main.getOnlinePlayers().values());
		}else {
			for(Team team : UHCSystem.getTeams()) {
				BorderData border = main.getWorldBorder().getWorldBorder(Globals.GAME_WORLD_NAME);
				double minX = border.getX() - (border.getRadiusX() - 10);
				double maxX = border.getX() + (border.getRadiusX() - 10);
				double minZ = border.getZ() - (border.getRadiusZ() - 10);
				double maxZ = border.getZ() + (border.getRadiusZ() - 10);
				
				Location spawn = UHCSystem.getRandomSpawnPoint(minX, maxX, minZ, maxZ);
				UHCPlayer[] members = team.getMembers().toArray(new UHCPlayer[team.getMembers().size()]);
				if(members.length > 0) {
					members[0].getBukkitPlayer().teleport(spawn);
					
					for(int i = 1; i < members.length; i++) {
						minX = spawn.getX() - 5;
						maxX = spawn.getX() + 5;
						minZ = spawn.getZ() - 5;
						maxZ = spawn.getZ() + 5;
						members[i].getBukkitPlayer().teleport(UHCSystem.getRandomSpawnPoint(minX, maxX, minZ, maxZ));
					}
				}
			}
			
			// We have to spawn the solo players.
			ArrayList<UHCPlayer> players = new ArrayList<UHCPlayer>();
			for(UHCPlayer p : main.getOnlinePlayers().values()) {
				if(p.getTeam() == null) players.add(p);
			}
			
			spawnPlayers(players);
		}
	}
	
	public void spawnPlayers(Collection<UHCPlayer> players) {
		for(UHCPlayer p : players) {
			Player player = p.getBukkitPlayer();
			BorderData border = main.getWorldBorder().getWorldBorder(Globals.GAME_WORLD_NAME);
			double minX = border.getX() - (border.getRadiusX() - 10);
			double maxX = border.getX() + (border.getRadiusX() - 10);
			double minZ = border.getZ() - (border.getRadiusZ() - 10);
			double maxZ = border.getZ() + (border.getRadiusZ() - 10);
			player.teleport(UHCSystem.getRandomSpawnPoint(minX, maxX, minZ, maxZ));
		}
	}
	
	/**
	 * This should be called when a player has left the game permanently.
	 * @param UHCPlayer player
	 */
	
	public void handlePlayerExit(UHCPlayer player) {
		if(isTeamMatch) {
			Team pTeam = player.getTeam();
			if(pTeam != null) {
				pTeam.removeMember(player);
				
				if(pTeam.getMembers().size() == 0) {
					String teamEliminated = UHCSystem.getTeamNameWithPrefix(pTeam).concat(" has been eliminated!");
					UHCSystem.broadcastMessageAndSound(main.getMessages().color(teamEliminated), Sound.ENDERDRAGON_HIT, 2F);
				}
			}
			
			final ArrayList<Team> teamsAlive = new ArrayList<Team>();
			for(Team team : UHCSystem.getTeams()) {
				if(team.getMembers().size() > 0) {
					teamsAlive.add(team);
				}
			}
			
			if(teamsAlive.size() == 1) {
				String teamWon = UHCSystem.getTeamNameWithPrefix(teamsAlive.get(0)).concat(" has won the game!");
				UHCSystem.broadcastMessage(main.getMessages().color(teamWon));
				timer = new MatchTimer(main, "Game Over", -1, -1);
				
				final BukkitTask fireworkTask = new BukkitRunnable() {
					
					public void run() {
						for(UHCPlayer player : teamsAlive.get(0).getMembers()) {
							// Let's shoot a firework at the player's location.
							final FireworkEffect effect = FireworkEffectBuilder.buildRandomEffect();
							final Location pLoc = player.getBukkitPlayer().getLocation();
							final Location fLoc = new Location(pLoc.getWorld(), pLoc.getX() + 0.5, pLoc.getY() + 1, pLoc.getZ() + 0.5);
							final Firework firework = (Firework) pLoc.getWorld().spawnEntity(fLoc, EntityType.FIREWORK);
							final FireworkMeta meta = firework.getFireworkMeta();
							meta.addEffect(effect);
							meta.setPower(1);
							firework.setFireworkMeta(meta);
						}
					}
					
				}.runTaskTimer(main, 0L, 10L);
				
				new BukkitRunnable() {
					
					public void run() {
						fireworkTask.cancel();
						reset();
					}
					
				}.runTaskLater(main, 100L);
			}
		}
		
		// We need to reset the player's stats if we changed them.
		if(state.toIndex() >= GameState.GRACE_PERIOD.toIndex()) {
    		for(Scenario scenario : scenarios) {
    		    if(scenario.doesEditPlayerStats()) {
    		        scenario.resetStats(player);
    		    }
    		}
    		
    	    enterSpectate(player);
		}
		
		player.setInGame(false);
	}
	
	public void enterSpectate(final UHCPlayer player) {
		for(UHCPlayer uhcPlayer : main.getOnlinePlayers().values()) {
			if(uhcPlayer.getState() == PlayerState.ACTIVE) {
				uhcPlayer.getBukkitPlayer().hidePlayer(player.getBukkitPlayer());
				player.getBukkitPlayer().showPlayer(uhcPlayer.getBukkitPlayer());
			}else if(uhcPlayer.getState() == PlayerState.SPECTATING) {
				uhcPlayer.getBukkitPlayer().showPlayer(player.getBukkitPlayer());
				player.getBukkitPlayer().showPlayer(uhcPlayer.getBukkitPlayer());
			}
		}
		
		player.setState(PlayerState.SPECTATING);
		player.getBukkitPlayer().setVelocity(new Vector(0, 1, 0));
		player.getBukkitPlayer().setAllowFlight(true);
		//UHCSystem.setGhost(player.getBukkitPlayer(), true);
		
		new BukkitRunnable() {
			
			public void run() {
			    if(player != null) {
    				player.getBukkitPlayer().setFlying(true);
    				player.getBukkitPlayer().setGameMode(GameMode.CREATIVE);
			    }
			}
			
		}.runTaskLater(main, 10L);
	}
	
	/**
	 * This method handles after a player respawns
	 * or if they've won the match.
	 * @param player
	 */
	
	public void handlePostRespawn(UHCPlayer player) {
		UHCSystem.handleLobbyArrival(main, player);
	}
	
	public void setInitialPlayers() {
		this.initialPlayers = getPlayers().size();
	}
	
	public int getInitialPlayers() {
		return this.initialPlayers;
	}
	
	public void reset() {
		main.sendConsoleMessage(ChatColor.YELLOW + "Resetting game system...");
		setState(GameState.WAITING);
		
		// Let's clear up the scenarios.
		for(Scenario scenario : scenarios) {
		    scenario.deactivate();
		}

		scenarios.clear();
		
		// Let's reset our timers.
		initialPlayers = 0;
		timer = TimerUtils.createTimer(main, "Preparing", main.getSettings().getFreezeTime());
		
		// Let's send the players to a lobby, if need-be.
		for(UHCPlayer p : getPlayers()) {
		    ArrayList<UHCLobbyResponse> lobbies = main.getSettings().getJedis().getAvailableLobbies();
		    UHCLobbyResponse lobby = lobbies.get(ThreadLocalRandom.current().nextInt(0, lobbies.size()));
	        ByteArrayDataOutput out = ByteStreams.newDataOutput();
	        out.writeUTF("Connect");
	        out.writeUTF(lobby.getName());
	        p.getBukkitPlayer().sendPluginMessage(main, "BungeeCord", out.toByteArray());
		}
		
		// Let's delete the UHC world.
		main.getWorldHandler().deleteGameWorld();
		main.getWorldHandler().createGameWorld();
		
		// Let's reset the lobby timer.
		UHCSystem.setLobbyTimer(main);
		
		Iterator<UHCPlayer> players = main.getOnlinePlayers().values().iterator();
		while(players.hasNext()) {
			players.next().getBukkitPlayer().kickPlayer("RECONNECT for a new round.");
			players.remove();
		}
	}
	
	public void setState(GameState newState) {
		GameState prevState = state;
		this.state = newState;
		
		if(newState == GameState.STARTING) {
			UHCSystem.broadcastMessageAndSound(main.getMessages().getMessage("lobby-timer-begun"), main.getSettings().getStateUpdateSound());
			UHCSystem.getLobbyTimer().reset();
			UHCSystem.getLobbyTimer().runTaskTimer(main, 0L, 20L);
		}else if(newState == GameState.PREPARING) {
			UHCSystem.broadcastMessageAndSound(main.getMessages().getMessage("welcome"), main.getSettings().getStateUpdateSound());
		}else if(newState == GameState.GRACE_PERIOD) {
			// Activate all the players.
			for(UHCPlayer player : main.getOnlinePlayers().values()) {
				player.setState(PlayerState.ACTIVE);
			}
			
			for(Scenario scenario : scenarios) {
				scenario.activate();
			}

			timer = TimerUtils.createTimer(main, "Grace Period", main.getProfile().getGracePeriodLength());
			timer.runTaskTimer(main, 0L, 20L);
			UHCSystem.broadcastMessageAndSound(main.getMessages().getMessage("gracePeriodBegin"), main.getSettings().getStateUpdateSound());
		}else if(newState == GameState.PVP) {
			main.getWorldHandler().setPVP(true);
			int shrinkTime = main.getProfile().getBeginBorderShrinkTime();
			if(prevState == GameState.PVP) {
				shrinkTime = main.getSettings().getBorderShrinkEveryTime();
			}
			
			timer = TimerUtils.createTimer(main, "Map Shrink", shrinkTime);
			timer.runTaskTimer(main, 0L, 20L);
			if(prevState != GameState.PVP) UHCSystem.broadcastMessageAndSound(main.getMessages().getMessage("gracePeriodEnded"), main.getSettings().getStateUpdateSound());
		}else if(newState == GameState.DEATHMATCH) {
			timer = TimerUtils.createTimer(main, "Deathmatch", main.getProfile().getGracePeriodLength());
			timer.runTaskTimer(main, 0L, 20L);
		}
		
		UHCJedis jedis = main.getSettings().getJedis();
		if(jedis != null) {
		    jedis.updateStatus();
		}
	}
	
	public GameState getState() {
		return this.state;
	}
	
	public boolean isScenarioActive(ScenarioType type) {
		for(Scenario scenario : scenarios) {
			if(scenario.getType() == type) {
				return true;
			}
		}
		
		return false;
	}
	
	public HashSet<Scenario> getScenarios() {
		return this.scenarios;
	}
	
	public HashSet<UHCPlayer> getPlayers() {
		HashSet<UHCPlayer> players = new HashSet<UHCPlayer>();
		for(UHCPlayer p : main.getOnlinePlayers().values()) {
			if(p.isInGame()) players.add(p);
		}

		return players;
	}
	
	public boolean isTeamMatch() {
		return this.isTeamMatch;
	}
	
	public MatchTimer getTimer() {
		return this.timer;
	}
}
