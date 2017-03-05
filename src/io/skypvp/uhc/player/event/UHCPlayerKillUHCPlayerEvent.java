package io.skypvp.uhc.player.event;

import io.skypvp.uhc.player.UHCPlayer;

public class UHCPlayerKillUHCPlayerEvent extends UHCPlayerEvent {
	
	private final UHCPlayer killed;
	
	public UHCPlayerKillUHCPlayerEvent(UHCPlayer killer, UHCPlayer killed) {
		super(killer);
		this.killed = killed;
	}
	
	public UHCPlayer getKilled() {
		return this.killed;
	}

}
