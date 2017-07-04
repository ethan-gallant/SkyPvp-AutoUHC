package io.skypvp.uhc.arena.state;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import io.skypvp.uhc.Globals;
import io.skypvp.uhc.SkyPVPUHC;
import io.skypvp.uhc.UHCSystem;
import io.skypvp.uhc.player.UHCPlayer;

public class LobbyWaitState extends GameState {

    private ArrayList<UUID> forcestartVotes;

    // The last time (in milliseconds) that we broadcasted that we were
    // waiting for more players.
    private long lastWaitNoticeMs;

    public LobbyWaitState(SkyPVPUHC instance, GameStateManager stateMgr) {
        super(instance, "lobbyWait", stateMgr);
        this.forcestartVotes = new ArrayList<UUID>();
        this.lastWaitNoticeMs = System.currentTimeMillis();
    }

    public void onPlayerJoin(final UHCPlayer uhcPlayer) {
        if(main.getProfile().usesRandomTeams() && main.getProfile().isTeamMatch()) {
            UHCSystem.assignPlayerToRandomTeam(uhcPlayer);
        }
    }

    public void run() {
        // Every 3 seconds, let's broadcast that we're waiting for more players.
        if(System.currentTimeMillis() > lastWaitNoticeMs + Globals.WAITING_FOR_PLAYERS_WARNING_TIME) {
            UHCSystem.broadcastMessage(main.getMessages().getMessage("waiting-for-players"));
            lastWaitNoticeMs = System.currentTimeMillis();
        }
    }

    /**
     * We can move onto the {@link StartingGameState} if at least 75%
     * have voted for a force-start, an admin has force started the match,
     * or if the server is 60% full.
     */

    @Override
    public boolean canMoveOn() {
        int onlinePlayers = main.getOnlinePlayers().keySet().size();
        double perct = ((double) forcestartVotes.size()) / onlinePlayers;

        return (perct >= 0.75 || getDefaultContinuationLogic());
    }

    /**
     * If {@link #canMoveOn()} fails, let's continue calling {@link #run()}
     * @return The opposite of {@link #canMoveOn()}.
     */

    public boolean canContinue() {
        return !canMoveOn();
    }

    public void onEnter() {}

    public void onExit() {
        forcestartVotes.clear();
    }

    ////////////////////////////////////////////////////////////
    // 			CODE RELATING TO FORCE-START VOTING           //
    ////////////////////////////////////////////////////////////

    /**
     * Attempts to send a vote for force-start.
     * If an admin of the match votes, this automatically starts the match.
     * @param {@link UUID} - The uuid of the {@link UHCPlayer} who's voting.
     * @return true/false, if the vote was successful or not.
     */

    public boolean voteForForceStart(UUID uuid) {
        boolean voted = hasVotedForForceStart(uuid);
        Player p = Bukkit.getPlayer(uuid);
        boolean matchAdmin = UHCSystem.isMatchAdmin(p);
        if(p == null) return false;

        if(!voted) {
            int onlinePlayers = main.getOnlinePlayers().keySet().size();
            if(onlinePlayers >= main.getProfile().getMinimumNeededPlayers() || matchAdmin) {
                // Let's verify that we're still in the LobbyWaitState...
                if(stateMgr.getActiveState() == this) {
                    forcestartVotes.add(uuid);
                    double perct = ((double) forcestartVotes.size()) / onlinePlayers;
                    p.sendMessage(main.getMessages().getMessage("vote-success"));
                    if(perct >= 0.75 || matchAdmin) {
                        // Great, we can now start the match.
                        String msgKey = (matchAdmin) ? "force-start-success-admin" : "force-start-success";
                        String forceStartMsg = main.getMessages().getMessage(msgKey);
                        Iterator<UUID> iterator = main.getOnlinePlayers().keySet().iterator();
                        while(iterator.hasNext()) {
                            UUID uid = iterator.next();
                            Player bP = Bukkit.getPlayer(uid);
                            if(bP != null && bP != p) {
                                bP.sendMessage(forceStartMsg);
                            }
                        }

                        // Let's give the state manager some information.
                        stateMgr.setAdminForcedStart(matchAdmin);
                        stateMgr.setForceStartPlayers(main.getServer().getOnlinePlayers().size());
                    }
                }
            }else {
                p.sendMessage(main.getMessages().getMessage("not-enough-players"));
                return false;
            }

            return true;
        }

        return false;
    }

    /**
     * Checks if a {@link UUID} is inside of the force start votes {@link ArrayList}
     * @param {@link UUID}
     * @return true/false
     */

    public boolean hasVotedForForceStart(UUID uuid) {
        return this.forcestartVotes.contains(uuid);
    }

    /**
     * Fetches the full {@link ArrayList} of the {@link UUID}s who have voted
     * for force-start.
     * @return {@link ArrayList}<{@link UUID}>
     */

    public ArrayList<UUID> getForceStartVotes() {
        return this.forcestartVotes;
    }

    ////////////////////////////////////////////////////////////

}
