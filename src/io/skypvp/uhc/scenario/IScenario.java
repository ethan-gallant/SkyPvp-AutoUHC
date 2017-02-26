package io.skypvp.uhc.scenario;

import io.skypvp.uhc.player.UHCPlayer;

import org.bukkit.event.Listener;

public interface IScenario extends Listener {
	
	/**
	 * Registers all events within the Scenario.
	 */
	
	public void registerEvents();
	
	/**
	 * Should be called when cleaning up the Scenario.
	 * Unregisters the Scenario from the Event's handlers list.
	 */
	
	public void unregisterEvents();
	
	/**
	 * Should be called when a Scenario should activate.
	 */
	
	public void activate();
	
	/**
	 * Should be called when a Scenario should deactivate.
	 */
	
	public void deactivate();
	
	/**
	 * Returns if the Scenario is active or not.
	 * @return active (true/false)
	 */
	
	public boolean isActive();
	
	/**
	 * Resets player stats if necessary.
	 */
	
	public void resetStats(UHCPlayer player);
	
	/**
	 * Returns if the Scenario edits player stats or not.
	 * @return edits (true/false)
	 */
	
	public boolean doesEditPlayerStats();
}
