package io.skypvp.uhc.arena.state;

import io.skypvp.uhc.SkyPVPUHC;
import io.skypvp.uhc.UHCSystem;
import io.skypvp.uhc.timer.TimerUtils;

public class DeathmatchGameState extends TimedGameState {

	public DeathmatchGameState(SkyPVPUHC main, GameStateManager gsm) {
		super(main, "deathmatch", gsm);
	}

	public void onEnter() {
		timer = TimerUtils.createTimer(main, "Deathmatch", main.getProfile().getGracePeriodLength());
		UHCSystem.broadcastMessageAndSound(main.getMessages().getMessage("deathmatch-begun"), 
				main.getSettings().getStateUpdateSound());
		super.onEnter();
	}
}
