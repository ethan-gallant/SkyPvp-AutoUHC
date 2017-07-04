package io.skypvp.uhc;

import java.util.Arrays;
import java.util.List;

import net.md_5.bungee.api.ChatColor;

public class Globals {

	// MySQL database constants
	public static final String TABLE_NAME = "stats";
	public static final String CREATE_TABLE = "CREATE TABLE IF NOT "
	        + "EXISTS %s(UUID char(36), "
            + "GAMES_PLAYED int, GAMES_WON int, "
            + "KILLS int, DEATHS int, TOKENS int, PRIMARY KEY (UUID));";
	public static final String NEW_TABLE_ENTRY = "INSERT INTO %s "
			+ "(UUID, GAMES_PLAYED, GAMES_WON, KILLS, DEATHS, TOKENS) VALUES "
			+ "(?, 0, 0, 0, 0, 0);";
	public static final String TABLE_ENTRY_UPDATE = "UPDATE %s "
			+ "SET GAMES_PLAYED=%d, GAMES_WON=%d, KILLS=%d, "
			+ "DEATHS=%d, TOKENS=%d WHERE UUID='%s';";
	public static final String QUERY_USER = "SELECT * FROM %s WHERE UUID='%s';";
	
	// Constants relating to worlds
	public static final String GAME_WORLD_NAME = "uhc-game";
	public static final String LOBBY_WORLD_NAME = "uhc-lobby";
	
	// Constants relating to redis/jedis
    public static final int JEDIS_STATUS_UPDATE = 0;
    public static final String JEDIS_SERVER_LIST = "uhcServers";
	
	public static final ChatColor CLIENT_COLOR = ChatColor.LIGHT_PURPLE;
	
	// The indexes of the invulnerability periods
	public static final List<Integer> INVULNERABILITY_PERIODS = Arrays.asList(0, 1, 2, 3, 4);
	
	// Constants related to cooldowns
	public static final long COMBAT_TAG_TIME = 120000;
	public static final long FROZEN_WARNING_TIME = 3000;
	public static final long COMPASS_UPDATE_TIME = 1000;
	public static final long WAITING_FOR_PLAYERS_WARNING_TIME = 15000;

}
