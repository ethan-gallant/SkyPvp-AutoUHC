package io.skypvp.uhc.arena.state;

import io.skypvp.uhc.SkyPVPUHC;
import io.skypvp.uhc.UHCSystem;
import io.skypvp.uhc.timer.TimerUtils;

public class PreparingGameState extends TimedGameState {

	public PreparingGameState(SkyPVPUHC instance, GameStateManager stateMgr) {
		super(instance, "preparing", stateMgr);
	}

	public void onEnter() {
		timer = TimerUtils.createTimer(main, "Preparing", main.getSettings().getFreezeTime());
		super.onEnter();

		UHCSystem.broadcastMessageAndSound(main.getMessages().getMessage("welcome"), main.getSettings().getStateUpdateSound());
	}

}
