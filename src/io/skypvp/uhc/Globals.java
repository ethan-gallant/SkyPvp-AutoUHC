package io.skypvp.uhc;

import java.util.Arrays;
import java.util.List;

import io.skypvp.uhc.arena.UHCGame.GameState;
import net.md_5.bungee.api.ChatColor;

public class Globals {
	
	public static final String TABLE_NAME = "stats";
	public static final long POLL_DATABASE_TIME = 10000;
	public static final long ANNOUNCE_POLL_TIME = 2000;
	public static final String GAME_WORLD_NAME = "uhc-lobby";
	public static final String LOBBY_WORLD_NAME = "uhc-lobby";
	public static final ChatColor CLIENT_COLOR = ChatColor.LIGHT_PURPLE;
	public static final List<GameState> INVULNERABILITY_PERIODS = Arrays.asList(GameState.FINISHED, GameState.GRACE_PERIOD);
    public static final int JEDIS_STATUS_UPDATE = 0;
    public static final String JEDIS_SERVER_LIST = "uhcServers";
	public static long COMBAT_TAG_TIME = 120000;
    public static int MAX_MEMBERS_PER_TEAM = 1;
}
