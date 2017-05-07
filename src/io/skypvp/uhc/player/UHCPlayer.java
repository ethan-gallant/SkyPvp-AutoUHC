package io.skypvp.uhc.player;

import io.skypvp.uhc.Globals;
import io.skypvp.uhc.UHCScoreboard;
import io.skypvp.uhc.arena.Team;
import io.skypvp.uhc.menu.Menu;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class UHCPlayer {
	
	public enum PlayerState {
		ACTIVE, FROZEN, SPECTATING
	}
	
	final UUID uuid;
	private PlayerState state;
	
	// These are database statistics.
	private int gamesPlayed;
	private int gamesWon;
	private int kills;
	private int deaths;
	
	// These are temporary game statistics.
	private int gameKills;
	
	// Here are variables that are for game management.
	private Menu activeMenu;
	private Team team;
	private UHCScoreboard scoreboard;
	private boolean inGame;
	private boolean inTeamChat;
	
	// Variables to handle combat-tagging.
	private UHCPlayer combatTagger;
	
	// The time in milliseconds when this player was tagged.
	private long tagTimeMs;
	
	public UHCPlayer(UUID id, int gamesPlayed, int gamesWon, int kills, int deaths) {
		this.uuid = id;
		this.state = PlayerState.ACTIVE;
		this.gamesPlayed = gamesPlayed;
		this.gamesWon = gamesWon;
		this.kills = kills;
		this.gameKills = 0;
		this.deaths = deaths;
		this.activeMenu = null;
		this.team = null;
		this.scoreboard = null;
		this.inGame = true;
		this.inTeamChat = false;
		this.combatTagger = null;
		this.tagTimeMs = 0;
	}
	
	public void prepareForGame() {
		getBukkitPlayer().getInventory().clear();
		getBukkitPlayer().getInventory().setHelmet(null);
		getBukkitPlayer().getInventory().setChestplate(null);
		getBukkitPlayer().getInventory().setLeggings(null);
		getBukkitPlayer().getInventory().setBoots(null);
		getBukkitPlayer().setHealth(getBukkitPlayer().getMaxHealth());
		getBukkitPlayer().setFoodLevel(20);
		inTeamChat = false;
		//UHCSystem.setGhost(getBukkitPlayer(), false);
	}
	
	public void setState(PlayerState state) {
		this.state = state;
	}
	
	public PlayerState getState() {
		return this.state;
	}
	
	public void setScoreboard(UHCScoreboard board) {
		this.scoreboard = board;
		
		if(board != null) {
			scoreboard.build(getBukkitPlayer());
			getBukkitPlayer().setScoreboard(scoreboard.getBoard());
		}
	}
	
	public UHCScoreboard getScoreboard() {
		return this.scoreboard;
	}
	
	public void setActiveMenu(Menu menu) {
		this.activeMenu = menu;
	}
	
	public Menu getActiveMenu() {
		return this.activeMenu;
	}
	
	public void setTeam(Team team) {
		this.team = team;
	}
	
	public Team getTeam() {
		return this.team;
	}
	
	public void setInGame(boolean flag) {
		this.inGame = flag;
	}
	
	public boolean isInGame() {
		return this.inGame;
	}
	
	public void setInTeamChat(boolean flag) {
	    this.inTeamChat = flag;
	}
	
	public boolean isInTeamChat() {
	    return this.inTeamChat;
	}
	
	public int getKillDeathRatio() {
		int d = (deaths > 0) ? deaths : 1;
		return kills / d;
	}
	
	/*
	 * Returns a String that can be used to execute a prepared statement.
	 */
	
	public String toMySQLUpdate() {
		return String.format("UPDATE %s SET GAMES_PLAYED=%d, GAMES_WON=%d, KILLS=%d, DEATHS=%d WHERE UUID='%s'",
			Globals.TABLE_NAME, gamesPlayed, gamesWon, kills, deaths, uuid.toString());
	}
	
	public void incrementGamesPlayed() {
		gamesPlayed++;
	}
	
	public int getGamesPlayed() {
		return this.gamesPlayed;
	}
	
	public void incrementGamesWon() {
		gamesWon++;
	}
	
	public int getGamesWon() {
		return this.gamesWon;
	}
	
	public void incrementKills() {
		kills++;
		if(inGame) gameKills++;
	}
	
	public int getKills() {
		return this.kills;
	}
	
	public int getGameKills() {
		return this.gameKills;
	}
	
	public void incrementDeaths() {
		deaths++;
	}
	
	public int getDeaths() {
		return this.deaths;
	}
	
	public UUID getUUID() {
		return this.uuid;
	}
	
	/**
	 * Sets the player that this UHCPlayer is in combat with.
	 * Expects a valid UHCPlayer instance or null.
	 * @param UHCPlayer p
	 */
	
	public void setInCombatWith(UHCPlayer p) {
	    this.combatTagger = p;
	    this.tagTimeMs = (p != null) ? System.currentTimeMillis() : 0;
	}
	
	/**
	 * Returns if this player is in combat or not.
	 * @return true/false flag
	 */
	
	public boolean isInCombat() {
	    if(combatTagger == null) return false;
	    
	    if(combatTagger != null) {
	        if(System.currentTimeMillis() >= tagTimeMs + Globals.COMBAT_TAG_TIME) {
	            setInCombatWith(null);
	            return false;
	        }
	        
	        return true;
	    }
	    
	    return false;
	}
	
	public Player getBukkitPlayer() {
		return Bukkit.getPlayer(uuid);
	}
}
