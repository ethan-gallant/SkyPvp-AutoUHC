package io.skypvp.uhc.scenario;

import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.projectiles.ProjectileSource;

import io.skypvp.uhc.SkyPVPUHC;
import io.skypvp.uhc.player.event.UHCPlayerDamageEvent;

public class Switcharoo extends Scenario {
	
	public Switcharoo(SkyPVPUHC main) {
		super(main, ScenarioType.SWITCHAROO);
	}
	
	@EventHandler
	public void onPlayerDamage(UHCPlayerDamageEvent evt) {
		EntityDamageEvent dmgEvt = evt.getDamageEvent();
		
		// Let's only do this mess when this scenario is active.
		if(isActive()) {
			if(dmgEvt instanceof EntityDamageByEntityEvent) {
				EntityDamageByEntityEvent entDmgEvt = (EntityDamageByEntityEvent) dmgEvt;
				Entity damager = entDmgEvt.getDamager();
				
				// Our player must be shot by an arrow for this scenario to work.
				if(damager instanceof Arrow) {
					Arrow arrow = (Arrow) damager;
					
					// Let's make sure our shooter is another player.
					ProjectileSource projSrc = arrow.getShooter();
					
					if(projSrc instanceof Player && !entDmgEvt.isCancelled()) {
						Player shooter = (Player) projSrc;
						
						// Great, we have our shooter. Let's switch player positions.
						Player shot = evt.getPlayer().getBukkitPlayer();
						Location shotLoc = shot.getLocation();
						
						shot.teleport(shooter.getLocation());
						shooter.teleport(shotLoc);
					}
				}
			}
		}
	}

	@Override
	public void unregisterEvents() {
		UHCPlayerDamageEvent.getHandlerList().unregister(this);
	}

}
