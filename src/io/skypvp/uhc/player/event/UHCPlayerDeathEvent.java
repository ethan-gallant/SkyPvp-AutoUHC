package io.skypvp.uhc.player.event;

import org.bukkit.event.entity.PlayerDeathEvent;

import io.skypvp.uhc.player.UHCPlayer;

public class UHCPlayerDeathEvent extends UHCPlayerEvent {
	
	private final PlayerDeathEvent deathEvent;
	
	public UHCPlayerDeathEvent(UHCPlayer player, PlayerDeathEvent deathEvt) {
		super(player);
		this.deathEvent = deathEvt;
	}
	
	public PlayerDeathEvent getDeathEvent() {
		return this.deathEvent;
	}
}
