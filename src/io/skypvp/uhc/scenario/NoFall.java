package io.skypvp.uhc.scenario;

import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import io.skypvp.uhc.SkyPVPUHC;
import io.skypvp.uhc.player.event.UHCPlayerDamageEvent;

public class NoFall extends Scenario {
	
	public NoFall(SkyPVPUHC main) {
		super(main, ScenarioType.NO_FALL);
	}
	
	@EventHandler
	public void onPlayerDamaged(UHCPlayerDamageEvent evt) {
		EntityDamageEvent dmgEvt = evt.getDamageEvent();
		if(dmgEvt.getCause() == DamageCause.FALL && isActive()) {
			dmgEvt.setDamage(0.0);
			dmgEvt.setCancelled(true);
		}
	}

	@Override
	public void unregisterEvents() {
		UHCPlayerDamageEvent.getHandlerList().unregister(this);
	}

}
