package io.skypvp.uhc.scenario;

import io.skypvp.uhc.SkyPVPUHC;
import io.skypvp.uhc.player.UHCPlayer;
import io.skypvp.uhc.scenario.event.UHCScenarioActivateEvent;
import io.skypvp.uhc.scenario.event.UHCScenarioDeactivateEvent;

import java.util.Date;

public abstract class Scenario implements IScenario {
	
	final SkyPVPUHC instance;
	protected final ScenarioType type;
	protected boolean active;
	protected Date startTime;
	
	// This is a boolean that says if this Scenario changes player stats or not.
	protected boolean editsPlayerStats;
	
	public Scenario(SkyPVPUHC main, ScenarioType type) {
		this.instance = main;
		this.type = type;
		this.active = false;
		this.editsPlayerStats = false;
	}
	
	/**
	 * Resets player stats if need-be.
	 * This is implemented in here so every Scenario doesn't
	 * need to implement it if it's not needed.
	 */
	
	public void resetStats(UHCPlayer player) {
		return;
	}
	
	/**
	 * Implement method from interface.
	 */
	
	public boolean doesEditPlayerStats() {
		return this.editsPlayerStats;
	}
	
	/**
	 * This method should always be called when the Scenario
	 * is becoming active in a UHC game.
	 * If overriding method, make sure to use super.activate()
	 * to keep functionality.
	 */
	
	@Override
	public void activate() {
		// Let's register our events.
		registerEvents();
		active = true;
		startTime = new Date();
		
		// Let's let the server know that this scenario is now active.
		UHCScenarioActivateEvent evt = new UHCScenarioActivateEvent(this);
		instance.getServer().getPluginManager().callEvent(evt);
	}
	
	/**
	 * This method should always be called when the Scenario
	 * is being deactivated in a UHC game.
	 * If overriding method, make sure to use super.deactivate()
	 * to keep functionality.
	 */
	
	public void deactivate() {
		// Let's let the server know that this scenario is deactivating.
		UHCScenarioDeactivateEvent evt = new UHCScenarioDeactivateEvent(this);
		instance.getServer().getPluginManager().callEvent(evt);
		
		// Let's unregister our events.
		unregisterEvents();
		active = false;
	}
	
	@Override
	public boolean isActive() {
		return this.active;
	}
	
	@Override
	public final void registerEvents() {
		instance.getServer().getPluginManager().registerEvents(this, instance);
	}

	@Override
	public abstract void unregisterEvents();
	
	public ScenarioType getType() {
		return this.type;
	}
	
	public Date getStartTime() {
		return this.startTime;
	}

}
