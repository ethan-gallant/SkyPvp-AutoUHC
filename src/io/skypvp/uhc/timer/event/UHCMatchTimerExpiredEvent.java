package io.skypvp.uhc.timer.event;

import io.skypvp.uhc.timer.MatchTimer;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class UHCMatchTimerExpiredEvent extends Event {

	private final MatchTimer timer;
	private static final HandlerList handlers = new HandlerList();
	
	public UHCMatchTimerExpiredEvent(MatchTimer timer) {
		this.timer = timer;
	}
	
	public MatchTimer getTimer() {
		return this.timer;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}

}
