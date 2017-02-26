package io.skypvp.uhc.scenario;

import io.skypvp.uhc.SkyPVPUHC;
import io.skypvp.uhc.player.UHCPlayer;
import io.skypvp.uhc.scenario.event.UHCScenarioActivateEvent;
import io.skypvp.uhc.scenario.event.UHCScenarioDeactivateEvent;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class DoubleHealth extends Scenario {
	
	public DoubleHealth(SkyPVPUHC main) {
		super(main, ScenarioType.DOUBLE_HEALTH);
		this.editsPlayerStats = true;
	}
	
	public void resetStats(UHCPlayer uhcPlayer) {
		Player p = uhcPlayer.getBukkitPlayer();
		p.setMaxHealth(20.0d);
		p.setHealth(20.0d);
	}
	
	@EventHandler
	public void onScenarioActivate(UHCScenarioActivateEvent evt) {
		Scenario scenario = evt.getScenario();
		if(scenario == this) {
			for(UHCPlayer p : instance.getOnlinePlayers().values()) {
				Player bP = p.getBukkitPlayer();
				bP.setMaxHealth(bP.getMaxHealth() * 2.0);
				bP.setHealth(bP.getMaxHealth());
			}
		}
	}
	
	@EventHandler
	public void onScenarioDeactivate(UHCScenarioDeactivateEvent evt) {
		Scenario scenario = evt.getScenario();
		if(scenario == this) {
			for(UHCPlayer p : instance.getOnlinePlayers().values()) {
				resetStats(p);
			}
		}
	}

	@Override
	public void unregisterEvents() {
		UHCScenarioActivateEvent.getHandlerList().unregister(this);
		UHCScenarioDeactivateEvent.getHandlerList().unregister(this);
	}

}
