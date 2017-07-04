package io.skypvp.uhc.arena.state;

import io.skypvp.uhc.SkyPVPUHC;
import io.skypvp.uhc.timer.MatchTimer;

public abstract class TimedGameState extends GameState {

    protected MatchTimer timer;

    public TimedGameState(SkyPVPUHC instance, String name, GameStateManager stateMgr) {
        super(instance, name, stateMgr, FailureLogic.RESET);
        this.timer = stateMgr.getTimer();
    }

    /**
     * Special modified constructor for the {@link StartingGameState} state.
     * @param {@link SkyPVPUHC} instance
     * @param {@link GameStateManager} stateMgr
     * Uses {@link FailureLogic}.RETURN_ON_PREVIOUS instead of the default FailureLogic.RESET.
     */

    public TimedGameState(SkyPVPUHC instance, GameStateManager stateMgr) {
        super(instance, "startingGame", stateMgr, FailureLogic.RETURN_TO_PREVIOUS);
        this.timer = stateMgr.getTimer();
    }

    public void run() {}

    /**
     * TimedGameStates have timers that automatically change
     * to the next game state when the timer expires.
     * @return This always will return false.
     */

    public boolean canMoveOn() {
        return false;
    }

    /**
     * Default logic is to check if there are enough players to continue
     * the match. Child states of this class should define their own logic.
     * @return if there are enough players to continue this state.
     */

    @Override
    public boolean canContinue() {
        boolean canContinue = getDefaultContinuationLogic();
        if(!canContinue) stateMgr.getTimer().requestCancel();

        return canContinue;
    }

    /**
     * Simply starts the {@link MatchTimer} associated with this state.
     */

    public void onEnter() {
        timer.runTaskTimer(main, 0L, 20L);
    }

    public void onExit() {}

}
