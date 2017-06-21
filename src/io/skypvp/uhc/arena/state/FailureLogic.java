package io.skypvp.uhc.arena.state;

public enum FailureLogic {
	// When "RETURN_TO_PREVIOUS" is used as FailureLogic, the StateManager
	// goes back to the last state in the manager when canContinue() and canMoveOn() fails.
	RETURN_TO_PREVIOUS,

	// When "RESET" is used as FailureLogic, the StateManager
	// returns to the very first state in the cycle when canContinue() and canMoveOn() fails.
	RESET;

}
