package io.skypvp.uhc;

import java.util.Arrays;
import java.util.List;

import net.md_5.bungee.api.ChatColor;

public class Globals {

	// MySQL database constants
	public static final String TABLE_NAME = "stats";
	public static final String NEW_TABLE_ENTRY = "INSERT INTO %s "
			+ "(UUID, GAMES_PLAYED, GAMES_WON, KILLS, DEATHS) VALUES (?, 0, 0, 0, 0) ";
	public static final String TABLE_ENTRY_UPDATE = "UPDATE %s "
			+ "SET GAMES_PLAYED=%d, GAMES_WON=%d, KILLS=%d, DEATHS=%d WHERE UUID='%s'";

	public static final long POLL_DATABASE_TIME = 10000;
	public static final long ANNOUNCE_POLL_TIME = 2000;
	public static final String GAME_WORLD_NAME = "uhc-game";
	public static final String LOBBY_WORLD_NAME = "uhc-lobby";
	public static final ChatColor CLIENT_COLOR = ChatColor.LIGHT_PURPLE;
	public static final List<Integer> INVULNERABILITY_PERIODS = Arrays.asList(4);
	public static final int JEDIS_STATUS_UPDATE = 0;
	public static final String JEDIS_SERVER_LIST = "uhcServers";
	public static final long COMBAT_TAG_TIME = 120000;
	public static final long FROZEN_WARNING_TIME = 3000;
}
