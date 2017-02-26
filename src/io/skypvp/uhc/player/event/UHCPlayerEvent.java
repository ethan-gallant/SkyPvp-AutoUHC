package io.skypvp.uhc.player.event;

import io.skypvp.uhc.player.UHCPlayer;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class UHCPlayerEvent extends Event {

	protected final UHCPlayer player;
	private static final HandlerList handlers = new HandlerList();
	
	public UHCPlayerEvent(UHCPlayer player) {
		this.player = player;
	}
	
	public UHCPlayer getPlayer() {
		return this.player;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}

}
