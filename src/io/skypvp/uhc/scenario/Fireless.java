package io.skypvp.uhc.scenario;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import io.skypvp.uhc.SkyPVPUHC;

public class Fireless extends Scenario {
	
	public Fireless(SkyPVPUHC main) {
		super(main, ScenarioType.FIRELESS);
	}
	
	public void onPlayerDamage(EntityDamageEvent evt) {
		Entity ent = evt.getEntity();
		DamageCause cause = evt.getCause();
		
		ArrayList<DamageCause> fireCauses = new ArrayList<DamageCause>(Arrays.asList(DamageCause.FIRE, DamageCause.FIRE_TICK, DamageCause.LAVA));
		
		if(ent instanceof Player && fireCauses.contains(cause) && active) {
			evt.setDamage(0.0);
			evt.setCancelled(true);
		}
	}

	@Override
	public void unregisterEvents() {
		EntityDamageEvent.getHandlerList().unregister(this);
	}

}
