package io.skypvp.uhc.arena.state;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import io.skypvp.uhc.SkyPVPUHC;
import io.skypvp.uhc.UHCSystem;
import io.skypvp.uhc.arena.Profile;
import io.skypvp.uhc.arena.Team;
import io.skypvp.uhc.jedis.UHCJedis;
import io.skypvp.uhc.jedis.UHCLobbyResponse;
import io.skypvp.uhc.player.UHCPlayer;
import io.skypvp.uhc.timer.MatchTimer;
import net.md_5.bungee.api.ChatColor;

public class GameStateManager {

    final SkyPVPUHC main;
    private GameState activeState;
    private MatchTimer timer;
    private BukkitTask stateThread;
    
    // The amount of players online when a force-start was passed.
    private volatile int forcestartPlayers;
    
    // This is if an admin forced match start or not.
    private volatile boolean adminForcedStart;
    
    private final ArrayList<GameState> states;

    public GameStateManager(SkyPVPUHC instance) {
        this.main = instance;
        this.activeState = null;
        this.timer = null;
        this.stateThread = null;
        this.states = new ArrayList<GameState>();
        
        // Variables relating to force-start.
        this.forcestartPlayers = -1;
        this.adminForcedStart = false;

        // Sets up our states
        states.add(new SetupGameState(main, this));
        states.add(new LobbyWaitState(main, this));
        states.add(new StartingGameState(main, this));
        states.add(new PreparingGameState(main, this));
        states.add(new GracePeriodGameState(main, this));
        states.add(new MapShrinkGameState(main, this));
        states.add(new DeathmatchGameState(main, this));
    }

    /**
     * Handles the unexpected exit of a player while a match is in-progress.
     * @param {@link UHCPlayer} player
     */

    public void handleInProgressGamePlayerExit(UHCPlayer player) {
        Profile profile = main.getProfile();

        // Let's check if this is a team match or not.
        if(profile.isTeamMatch()) {
            Team pTeam = player.getTeam();
            pTeam.removeMember(player);

            if(pTeam.getMembers().size() == 0) {
                String teamEliminated = UHCSystem.getTeamNameWithPrefix(pTeam).concat(" has been eliminated!");
                UHCSystem.broadcastMessageAndSound(main.getMessages().constructMessage(teamEliminated), 
                        Sound.ENDERDRAGON_HIT, 2F);
            }
            
            main.getGame().setAliveTeams(main.getGame().getAliveTeams() - 1);
        }else {
            // This is a free-for-all match.
        }
    }

    public void think() {
        if(activeState == null) {
            setActiveState(getStates().get(0), false);
        }else if(!activeState.canContinue() && !activeState.canMoveOn()) {
            activeState.onFailure();

            if(activeState.getFailureLogic() == FailureLogic.RESET) {
                setActiveState(getStates().get(0), false);
            }else {
                GameState previousState = (activeState.toIndex() > 0) ? (getStates().get(activeState.toIndex() - 1)) : getStates().get(0);
                setActiveState(previousState, false);
            }

        }else if(!activeState.canContinue() && activeState.canMoveOn()) {
            GameState nextState = (activeState.toIndex() < states.size()) ? (getStates().get(activeState.toIndex() + 1)) : getStates().get(0);
            setActiveState(nextState, true);
        }
        
        activeState.run();
    }

    public void startRunning() {
        stateThread = new BukkitRunnable() {

            public void run() {
                think();
            }

        }.runTaskTimer(main, 1L, 1L);
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
        UHCLobbyResponse lobby = null;
        
        // ThreadLocalRandom will throw an error if lobbies.size() is 0.
        if(lobbies.size() > 0) {
            lobby = lobbies.get(ThreadLocalRandom.current().nextInt(0, lobbies.size())); 
        }

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
     * @param boolean callExit - If the {@link GameState.#onExit()} should be called or not.
     * @throws IllegalArgumentException - {@link GameState} must be inside of the "states" ArrayList.
     */

    public void setActiveState(GameState state, boolean callExit) throws IllegalArgumentException {
        if(!states.contains(state)) throw new IllegalArgumentException("State must be inside of \"states\" ArrayList.");

        if(activeState != null && callExit) {
            activeState.onExit();
        }

        this.activeState = state;
        this.activeState.onEnter();

        // Let's register the events of the newly active state.
        main.getServer().getPluginManager().registerEvents(activeState, main);

        // Let's alert jedis that our state has changed.
        UHCJedis jedis = main.getSettings().getJedis();
        if(jedis != null) jedis.updateStatus();
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
    
    ///////////////////////////////////////////////////////
    
    /**
     * Sets the amount of players online when a force-start was called.
     * @param int amount - The amount of players online or -1 to reset.
     */
    
    public void setForceStartPlayers(int amount) {
        this.forcestartPlayers = amount;
    }
    
    /**
     * Fetches the amount of players online when a force-start was called.
     * @return int amount
     */
    
    public int getForceStartPlayers() {
        return this.forcestartPlayers;
    }
    
    /**
     * Sets if an admin forced the start of the match or not.
     * @param boolean flag
     */
    
    public void setAdminForcedStart(boolean flag) {
        this.adminForcedStart = flag;
    }
    
    /**
     * Fetches if an admin forced the start of the match.
     * @return boolean true/false flag
     */
    
    public boolean didAdminForceStart() {
        return this.adminForcedStart;
    }
    
    ///////////////////////////////////////////////////////

    /**
     * Fetches the state thread.
     * @return {@link BukkitTask}
     */

    public BukkitTask getStateThread() {
        return this.stateThread;
    }
    
}
