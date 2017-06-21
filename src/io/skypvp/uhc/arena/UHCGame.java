package io.skypvp.uhc.arena;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import com.wimbli.WorldBorder.BorderData;

import io.skypvp.uhc.Globals;
import io.skypvp.uhc.SkyPVPUHC;
import io.skypvp.uhc.UHCSystem;
import io.skypvp.uhc.arena.state.GameStateManager;
import io.skypvp.uhc.jedis.UHCJedis;
import io.skypvp.uhc.player.UHCPlayer;
import io.skypvp.uhc.player.UHCPlayer.PlayerState;
import io.skypvp.uhc.scenario.Scenario;
import io.skypvp.uhc.scenario.ScenarioType;
import io.skypvp.uhc.timer.MatchTimer;
import io.skypvp.uhc.timer.TimerUtils;
import io.skypvp.uhc.util.FireworkEffectBuilder;
import net.md_5.bungee.api.ChatColor;

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
	final GameStateManager gsm;
	private HashSet<Scenario> scenarios;

	// Variables for handling standings.
	private int initialPlayers;

	public UHCGame(SkyPVPUHC instance) {
		this.main = instance;
		this.gsm = main.getGameStateManager();
		this.scenarios = new HashSet<Scenario>();
		this.initialPlayers = 0;
	}

	/**
	 * Sets up the scenarios inside of our "scenarios" {@link HashSet}.
	 * NOTE: Should only be called once per match.
	 * This method uses reflection to create new instances.
	 */

	public void setupScenarios() {
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
	}

	/**
	 * Activates all scenarios inside of the "scenarios" {@link HashSet}.
	 * NOTE: This is usually called at the beginning of the Grace Period state.
	 */

	public void activateScenarios() {
		for(Scenario scenario : scenarios) {
			scenario.activate();
		}
	}

	public void spawnPlayers() {
		if(!main.getProfile().isTeamMatch()) {
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
		if(main.getProfile().isTeamMatch()) {
			Team pTeam = player.getTeam();
			if(pTeam != null) {
				pTeam.removeMember(player);

				if(pTeam.getMembers().size() == 0) {
					String teamEliminated = UHCSystem.getTeamNameWithPrefix(pTeam).concat(" has been eliminated!");
					UHCSystem.broadcastMessageAndSound(main.getMessages().color(teamEliminated), Sound.ENTITY_ENDERDRAGON_HURT, 2F);
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
		// Let's clear up the scenarios.
		for(Scenario scenario : scenarios) {
			scenario.deactivate();
		}

		scenarios.clear();

		// Let's reset our timers.
		initialPlayers = 0;
	}

	/**
	 * Checks if a {@link ScenarioType} is active.
	 * @param {@link ScenarioType} type
	 * @return if the scenario is active or not (true/false)
	 */

	public boolean isScenarioActive(ScenarioType type) {
		for(Scenario scenario : scenarios) {
			if(scenario.getType() == type) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns a {@link HashSet} of {@link Scenario}s
	 * @return HashSet of Scenarios
	 */

	public HashSet<Scenario> getScenarios() {
		return this.scenarios;
	}

	/**
	 * Returns a {@link HashSet} of {@link UHCPlayer}s who are
	 * currently in-game.
	 * @return
	 */

	public HashSet<UHCPlayer> getPlayers() {
		HashSet<UHCPlayer> players = new HashSet<UHCPlayer>();
		for(UHCPlayer p : main.getOnlinePlayers().values()) {
			if(p.isInGame()) players.add(p);
		}

		return players;
	}
}
