package io.skypvp.uhc.arena.state;

import io.skypvp.uhc.SkyPVPUHC;
import io.skypvp.uhc.arena.Team;
import io.skypvp.uhc.player.UHCPlayer;

public class MatchCompleteGameState extends TimedGameState {

    // The player who won free-for-all
    private final UHCPlayer ffaWinner;

    // The team that won the match.
    private final Team teamWinner;

    /**
     * The constructor for free-for-all matches when an individual player wins.
     * @param {@link SkyPVPUHC} main
     * @param {@link GameStateManager} stateMgr
     * @param {@link UHCPlayer} winner
     */

    public MatchCompleteGameState(SkyPVPUHC main, GameStateManager stateMgr,
            UHCPlayer winner) {
        super(main, "matchComplete", stateMgr);
        this.ffaWinner = winner;
        this.teamWinner = null;
    }

    /**
     * The constructor for team matches when a team wins.
     * @param {@link SkyPVPUHC} main
     * @param {@link GameStateManager} stateMgr
     * @param {@link Team} winner
     */

    public MatchCompleteGameState(SkyPVPUHC main, GameStateManager stateMgr,
            Team winner) {
        super(main, "matchComplete", stateMgr);
        this.ffaWinner = null;
        this.teamWinner = winner;
    }

    /**
     * Obtains the free-for-all winner (if there is one)
     * @return {@link UHCPlayer} if free-for-all match OR null.
     */

    public UHCPlayer getFreeForAllWinner() {
        return this.ffaWinner;
    }

    /**
     * Obtains the team who won (if there is one)
     * @return {@link Team} if team match OR null.
     */

    public Team getTeamWhoWon() {
        return this.teamWinner;
    }
}
