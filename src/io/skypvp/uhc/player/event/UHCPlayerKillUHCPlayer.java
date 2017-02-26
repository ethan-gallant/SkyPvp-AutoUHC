package io.skypvp.uhc.player.event;

import io.skypvp.uhc.player.UHCPlayer;

public class UHCPlayerKillUHCPlayer extends UHCPlayerEvent {
	
	private final UHCPlayer killed;
	
	public UHCPlayerKillUHCPlayer(UHCPlayer killer, UHCPlayer killed) {
		super(killer);
		this.killed = killed;
	}
	
	public UHCPlayer getKilled() {
		return this.killed;
	}

}
