package io.skypvp.uhc.arena;

import io.skypvp.uhc.Globals;
import io.skypvp.uhc.SkyPVPUHC;
import io.skypvp.uhc.UHCSystem;
import io.skypvp.uhc.player.UHCPlayer;
import io.skypvp.uhc.scenario.Scenario;
import io.skypvp.uhc.timer.MatchTimer;
import io.skypvp.uhc.timer.TimerUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.wimbli.WorldBorder.BorderData;

import net.md_5.bungee.api.ChatColor;

public class UHCGame {
	
	public enum GameState {
		PREPARING, WAITING, STARTING, GRACE_PERIOD, PVP, DEATHMATCH, FINISHED
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
		this.state = GameState.PREPARING;
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
		main.getWorldHandler().getGameWorld().getCBWorld().setTime(500L);
		spawnPlayers();
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
				members[0].getBukkitPlayer().teleport(spawn);
				
				for(int i = 1; i < members.length; i++) {
					minX = spawn.getX() - 5;
					maxX = spawn.getX() + 5;
					minZ = spawn.getZ() - 5;
					maxZ = spawn.getZ() + 5;
					members[i].getBukkitPlayer().teleport(UHCSystem.getRandomSpawnPoint(minX, maxX, minZ, maxZ));
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
			pTeam.removeMember(player);
		}
		
		player.setInGame(false);
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
		setState(GameState.PREPARING);
		scenarios.clear();
		
		// Let's reset our timers.
		initialPlayers = 0;
		timer.reset();
		
		// Let's clear the players out if need-be.
		for(UHCPlayer p : getPlayers()) {
			handlePlayerExit(p);
		}
		
		// Let's delete the UHC world.
		main.getWorldHandler().deleteGameWorld();
	}
	
	public void setState(GameState newState) {
		this.state = newState;
		
		if(newState == GameState.STARTING) {
			UHCSystem.broadcastMessageAndSound(main.getMessages().getMessage("lobby-timer-begun"), main.getSettings().getStateUpdateSound());
			UHCSystem.getLobbyTimer().reset();
			UHCSystem.getLobbyTimer().runTaskTimer(main, 0L, 20L);
		}
	}
	
	public GameState getState() {
		return this.state;
	}
	
	public boolean isScenarioActive(Scenario scenario) {
		return scenarios.contains(scenario);
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
