package io.skypvp.uhc.arena.state;

import org.bukkit.event.Listener;

import io.skypvp.uhc.SkyPVPUHC;
import io.skypvp.uhc.arena.UHCGame;
import io.skypvp.uhc.timer.MatchTimer;

public abstract class GameState implements IGameState, Listener {

	protected final SkyPVPUHC main;
	protected final GameStateManager stateMgr;
	protected final UHCGame game;
	protected final String name;
	protected final FailureLogic failureLogic;
	protected MatchTimer timer;

	public GameState(SkyPVPUHC instance, String stateName, GameStateManager stateMgr) {
		this.main = instance;
		this.name = stateName;
		this.stateMgr = stateMgr;
		this.game = main.getGame();
		this.timer = stateMgr.getTimer();
		this.failureLogic = FailureLogic.RETURN_TO_PREVIOUS;
	}

	public GameState(SkyPVPUHC instance, String stateName, GameStateManager stateMgr, 
			FailureLogic exitLogic) {
		this.main = instance;
		this.name = stateName;
		this.stateMgr = stateMgr;
		this.game = main.getGame();
		this.timer = stateMgr.getTimer();
		this.failureLogic = exitLogic;
	}

	/**
	 * This method is almost never used, so let's leave it empty in here.
	 */

	 public void onFailure() {}

	/**
	 * Returns the name of this state
	 * @return String representing name
	 */

	 public String getName() {
		 return this.name;
	 }

	 /**
	  * Returns the index of this state.
	  * Gets index within the "states" {@link java.util.ArrayList} 
	  * inside of the {@link GameStateManager}
	  * @return -1 or index
	  */

	 public int toIndex() {
		 return this.stateMgr.getStates().indexOf(this);
	 }

	 /**
	  * Returns the {@link FailureLogic} associated with this state.
	  * @return {@link FailureLogic}
	  */

	 public FailureLogic getFailureLogic() {
		 return this.failureLogic;
	 }

}
