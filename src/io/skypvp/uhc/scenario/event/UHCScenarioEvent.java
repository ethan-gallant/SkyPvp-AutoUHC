package io.skypvp.uhc.scenario.event;

import io.skypvp.uhc.scenario.Scenario;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public abstract class UHCScenarioEvent extends Event {

	protected final Scenario scenario;
	private static final HandlerList handlers = new HandlerList();
	
	public UHCScenarioEvent(Scenario scenario) {
		this.scenario = scenario;
	}
	
	public Scenario getScenario() {
		return this.scenario;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}

}
