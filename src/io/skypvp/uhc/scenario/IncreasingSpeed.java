package io.skypvp.uhc.scenario;

import java.util.Collection;
import java.util.Iterator;

import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import io.skypvp.uhc.SkyPVPUHC;
import io.skypvp.uhc.player.UHCPlayer;
import io.skypvp.uhc.player.event.UHCPlayerKillUHCPlayerEvent;
import io.skypvp.uhc.scenario.event.UHCScenarioActivateEvent;

public class IncreasingSpeed extends Scenario {
	
	public IncreasingSpeed(SkyPVPUHC main) {
		super(main, ScenarioType.INCREASING_SPEED);
		this.editsPlayerStats = true;
	}
	
	@Override
	public void resetStats(UHCPlayer player) {
		player.getBukkitPlayer().getActivePotionEffects().clear();
	}
	
	@EventHandler
	public void onScenarioActivate(UHCScenarioActivateEvent evt) {
		Scenario scenario = evt.getScenario();
		if(scenario.getType() == type) {
			for(UHCPlayer player : instance.getOnlinePlayers().values()) {
				PotionEffect effect = new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1);
				player.getBukkitPlayer().getActivePotionEffects().add(effect);
			}
		}
	}
	
	@EventHandler
	public void onPlayerKillPlayer(UHCPlayerKillUHCPlayerEvent evt) {
		UHCPlayer killer = evt.getPlayer();
		Collection<PotionEffect> effects = killer.getBukkitPlayer().getActivePotionEffects();
		
		if(isActive()) {
			Iterator<PotionEffect> iterator = effects.iterator();
			while(iterator.hasNext()) {
				PotionEffect effect = iterator.next();
				if(effect.getType() == PotionEffectType.SPEED) {
					effects.remove(effect);
	
					// Let's add back the increased speed.
					int newAmp = effect.getAmplifier() + 1;
					effects.add(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, newAmp));
					break;
				}
			}
			
			killer.getBukkitPlayer().getActivePotionEffects().clear();
			killer.getBukkitPlayer().getActivePotionEffects().addAll(effects);
		}
	}

	@Override
	public void unregisterEvents() {
		UHCPlayerKillUHCPlayerEvent.getHandlerList().unregister(this);
		UHCScenarioActivateEvent.getHandlerList().unregister(this);
	}

}
