package io.skypvp.uhc.arena.state;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.entity.Player;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import io.skypvp.uhc.SkyPVPUHC;
import io.skypvp.uhc.jedis.UHCLobbyResponse;
import io.skypvp.uhc.timer.MatchTimer;

public class GameStateManager {
    
    final SkyPVPUHC main;
    private GameState activeState;
    private MatchTimer timer;
    private final ArrayList<GameState> states;
    
    public GameStateManager(SkyPVPUHC instance) {
        this.main = instance;
        this.activeState = null;
        this.timer = null;
        this.states = new ArrayList<GameState>();
        
        // Sets up our states
        states.add(new SetupGameState(main, this));
    }
    
    ///////////////////////////////////////////////////////
    
    /**
     * Attempts to send a player to a UHC lobby.
     * Precondition: Plugin has attempted to connect to redis.
     * If a lobby is not found, player is simply kicked.
     * @param Player p
     */
    
    public void sendPlayerToRandomLobby(Player p) {
        ArrayList<UHCLobbyResponse> lobbies = main.getSettings().getJedis().getAvailableLobbies();
        UHCLobbyResponse lobby = lobbies.get(ThreadLocalRandom.current().nextInt(0, lobbies.size()));
        
        if(lobby != null) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Connect");
            out.writeUTF(lobby.getName());
            p.sendPluginMessage(main, "BungeeCord", out.toByteArray());
        }else {
            p.kickPlayer(ChatColor.RED + "Could not identify a viable UHC lobby in time to connect to. "
                    + "Please reconnect to the network.");
        }
    }
    
    ///////////////////////////////////////////////////////
    
    /**
     * Sets the current timer for the manager.
     * @param {@link MatchTimer} timer
     */
    
    public void setTimer(MatchTimer timer) {
        this.timer = timer;
    }
    
    /**
     * Fetches the manager's timer.
     * @return {@link MatchTimer}
     */
    
    public MatchTimer getTimer() {
        return this.timer;
    }
    
    ///////////////////////////////////////////////////////
    
    /**
     * Sets the active state, enters it, and "exits" the old one if
     * a state was active previously.
     * @param {@link GameState} state - The state to become active.
     * @throws IllegalArgumentException - {@link GameState} must be inside of the "states" ArrayList.
     */
    
    public void setActiveState(GameState state) throws IllegalArgumentException {
        if(!states.contains(state)) throw new IllegalArgumentException("State must be inside of \"states\" ArrayList.");
        
        if(activeState != null) {
            activeState.onExit();
        }
        
        this.activeState = state;
        this.activeState.onEnter();
        
        // Let's register the events of the newly active state.
        main.getServer().getPluginManager().registerEvents(activeState, main);
    }
    
    /**
     * Fetches the current GameState.
     * @return {@link GameState}
     */
    
    public GameState getActiveState() {
        return this.activeState;
    }
    
    /**
     * Fetches the list of GameStates.
     * @return {@link ArrayList<GameState>}
     */
    
    public ArrayList<GameState> getStates() {
        return this.states;
    }
}
