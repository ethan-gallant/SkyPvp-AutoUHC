package io.skypvp.uhc.arena.state;

import io.skypvp.uhc.SkyPVPUHC;
import io.skypvp.uhc.UHCSystem;
import io.skypvp.uhc.timer.TimerUtils;

public class MapShrinkGameState extends TimedGameState {

	public MapShrinkGameState(SkyPVPUHC main, GameStateManager stateMgr) {
		super(main, "mapShrink", stateMgr);
	}

	/**
	 * Sets up the Map Shrink timer and announces the end of grace period.
	 */

	public void onEnter() {
		timer = TimerUtils.createTimer(main, "Map Shrink", main.getProfile().getBeginBorderShrinkTime());
		UHCSystem.broadcastMessageAndSound(main.getMessages().getMessage("gracePeriodEnded"), 
				main.getSettings().getStateUpdateSound());
		super.onEnter();
	}
}
