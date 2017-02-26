package io.skypvp.uhc.player.event;

import io.skypvp.uhc.player.UHCPlayer;

import org.bukkit.event.entity.EntityDamageEvent;

public class UHCPlayerDamageEvent extends UHCPlayerEvent {
	
	private EntityDamageEvent evt;
	
	public UHCPlayerDamageEvent(UHCPlayer player, EntityDamageEvent evt) {
		super(player);
		this.evt = evt;
	}
	
	public EntityDamageEvent getDamageEvent() {
		return this.evt;
	}
}
