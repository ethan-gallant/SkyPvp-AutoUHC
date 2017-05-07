package io.skypvp.uhc.jedis;

import io.skypvp.uhc.SkyPVPUHC;
import io.skypvp.uhc.arena.Profile;
import io.skypvp.uhc.arena.UHCGame;
import io.skypvp.uhc.scenario.ScenarioType;

import java.util.ArrayList;
import java.util.Set;

import net.md_5.bungee.api.ChatColor;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

public class UHCJedis {
    
    UHCGame game;
    final SkyPVPUHC main;
    final String serverName;
    private JedisPool pool;
    private String password;
    
    // These strings represent the keys for each Jedis update.
    public static final String SERVER_DATA_MAP = "uhcServers";
    public static final String LOBBY_DATA_MAP = "uhcLobbies";
    public static final String GAME_STATE_KEY = "state";
    public static final String GAME_PRIVACY_KEY = "status";
    public static final String GAME_MODE_KEY = "mode";
    public static final String ONLINE_PLAYERS_KEY = "onlinePlayers";
    public static final String MAX_PLAYERS_KEY = "maxPlayers";
    public static final String ACTIVE_SCENARIOS_KEY = "scenarios";
    public static final String TEAM_SIZE_KEY = "teamSize";
    
    public UHCJedis(SkyPVPUHC instance) {
        this.main = instance;
        this.serverName = main.getSettings().getServerName();
        this.pool = null;
        this.password = null;
    }
    
    public void connect(String host, int port, String password) {
        ClassLoader prevClassLdr = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(UHCJedis.class.getClassLoader());
        pool = new JedisPool(host, port);
        this.password = password;
        Thread.currentThread().setContextClassLoader(prevClassLdr);
    }
    
    public void handshake() {
        String serverName = main.getSettings().getServerName();
        this.game = main.getGame();
        
        try(Jedis jedis = pool.getResource()) {
            if(!password.isEmpty()) jedis.auth(password);
            boolean exists = jedis.sismember(SERVER_DATA_MAP, serverName);
            
            if(!exists) {
                announceServerDetails(jedis);
                main.sendConsoleMessage(ChatColor.GREEN + "Successfully shook redis hand!");
            }else {
                main.sendConsoleMessage(ChatColor.RED + String.format("A UHC server with the name %s already exists according to Redis."
                   + " Change the server-name value inside of a config to a unique server name and restart the server.",
                serverName));
                main.disable();
            }
        }
    }
    
    public void farewell() {
        String serverName = main.getSettings().getServerName();
        
        try(Jedis jedis = pool.getResource()) {
            if(!password.isEmpty()) jedis.auth(password);
            Pipeline p = jedis.pipelined();
            Response<Boolean> srvExists = p.sismember(SERVER_DATA_MAP, serverName);
            p.sync();
            
            boolean exists = srvExists.get();
            
            if(exists) {
                p.del(serverName);
                p.srem(SERVER_DATA_MAP, serverName);
                main.sendConsoleMessage(ChatColor.GREEN + "Success!");
            }else {
                main.sendConsoleMessage(ChatColor.RED + String.format("Redis: This server is not a member of the '%s' set.", SERVER_DATA_MAP));
            }
        }
        
        close();
    }
    
    public ArrayList<UHCLobbyResponse> getAvailableLobbies() {
        ArrayList<UHCLobbyResponse> responses = new ArrayList<UHCLobbyResponse>();
        try(Jedis jedis = pool.getResource()) {
            if(!password.isEmpty()) jedis.auth(password);
            Pipeline p = jedis.pipelined();
            Response<Boolean> srvSetExists = p.exists(LOBBY_DATA_MAP);
            Response<Set<String>> respSrvNames = p.smembers(LOBBY_DATA_MAP);
            p.sync();

            boolean setExists = srvSetExists.get();

            if(setExists) {
                Set<String> serverNames = respSrvNames.get();
                for(String srvName : serverNames) {
                    //Response<Map<String, String>> data = p.hgetAll(srvName);
                    responses.add(new UHCLobbyResponse(srvName, null));
                }

                p.sync();
                
                /*
                for(UHCLobbyResponse response : responses) {
                    response.setData(response.getResponseData().get());
                }*/
            }else {
                main.sendConsoleMessage(ChatColor.RED + "Redis doesn't have a list of UHC servers.");
            }
        }

        return responses;
    }
    
    /**
     * Used for when we want to let redis know about our server while
     * we are doing other redis work in another method or area.
     * @param Jedis jedis - The current jedis instance we're using.
     */
    
    public void announceServerDetails(Jedis jedis) {
        boolean exists = jedis.sismember(SERVER_DATA_MAP, serverName);
        Profile profile = main.getProfile();
        
        // Let's use a pipeline to reduce network overhead.
        Pipeline p = jedis.pipelined();
        if(!exists) p.sadd(SERVER_DATA_MAP, serverName);
        
        // Let's create the hash for this server.
        // This is if this server is public or private.
        p.hset(serverName, GAME_PRIVACY_KEY, (profile.isPrivate()) ? "private" : "public");
     
        // If this is a FFA or Team Game
        p.hset(serverName, GAME_MODE_KEY, (game.isTeamMatch()) ? "team" : "ffa");
        
        // Let's announce our team size.
        p.hset(serverName, TEAM_SIZE_KEY, String.valueOf(profile.getTeamSize()));
        
        // The state of the game.
        p.hset(serverName, GAME_STATE_KEY, String.valueOf(game.getState().toIndex()));
        
        // Let's give a list of our active scenarios.
        p.hset(serverName, ACTIVE_SCENARIOS_KEY, scenarioListToString());
        
        // Let's tell redis what our traffic is looking like.
        p.hset(serverName, ONLINE_PLAYERS_KEY, String.valueOf(main.getServer().getOnlinePlayers().size()));
        p.hset(serverName, MAX_PLAYERS_KEY, String.valueOf(profile.getMaxPlayers()));
        
        // Let's let our UHC system know that we've updated some details.
        /**
        Jedis publisher = new Jedis();
        if(!password.isEmpty()) publisher.auth(password);
        publisher.publish("uhc", String.format("%s %d", serverName, Globals.JEDIS_STATUS_UPDATE));
        publisher.close();*/
    }
    
    /**
     * Generates a String using all the active scenarios like so:
     * "Switch-a-roo,DoubleHealth, etc"
     * @return String
     */
    
    private String scenarioListToString() {
        String scenarios = "";
        for(ScenarioType scenario : main.getProfile().getScenarios()) {
            scenarios = scenarios.concat(scenario.getName());
            
            if(main.getProfile().getScenarios().indexOf(scenario) != main.getProfile().getScenarios().size() - 1) {
                scenarios = scenarios.concat(",");
            }
        }
        
        return scenarios;
    }
    
    /**
     * We're letting redis know the latest on what's going on over here.
     */
    
    public void announceServerDetails() {
        String serverName = main.getSettings().getServerName();
        
        try(Jedis jedis = pool.getResource()) {
            if(!password.isEmpty()) jedis.auth(password);
            boolean exists = jedis.sismember(SERVER_DATA_MAP, serverName);
            
            if(exists) {
                announceServerDetails(jedis);
            }
        }
    }
    
    public void updateStatus() {
        String serverName = main.getSettings().getServerName();
        
        try(Jedis jedis = pool.getResource()) {
            if(!password.isEmpty()) jedis.auth(password);
            boolean exists = jedis.sismember(SERVER_DATA_MAP, serverName);
            
            if(exists) {
                jedis.hset(serverName, GAME_STATE_KEY, String.valueOf(main.getGame().getState().toIndex()));
                
                /*
                Jedis publisher = new Jedis();
                if(!password.isEmpty()) publisher.auth(password);
                publisher.publish("uhc", String.format("%s %d", serverName, Globals.JEDIS_STATUS_UPDATE));
                publisher.close();*/
            }else {
                main.sendConsoleMessage(ChatColor.RED + "Attempted to update status within redis hash while server is not a member of 'uhcServers' set within redis.");
                main.sendConsoleMessage(ChatColor.RED + "Maybe tried to update status after server closed?");
            }
        }
    }
    
    public void close() {
        if(pool != null) {
            pool.destroy();
        }
    }
}
