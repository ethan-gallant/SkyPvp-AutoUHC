package io.skypvp.uhc.scenario;

import io.skypvp.uhc.SkyPVPUHC;
import io.skypvp.uhc.UHCSystem;

import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class ScenarioDrops {
	
	final SkyPVPUHC instance;
	
	private final BlockBreakEvent breakEvt;
	
	// The block with the drops.
	private final Block block;
	
	// The original drops.
	private final Collection<ItemStack> originalDrops;
	
	// The drops that will be dropped.
	private Collection<ItemStack> drops;
	
	// Scenarios that have queued to update.
	private ArrayList<DropUpdaterScenario> queuedDropUpdates;
	
	public ScenarioDrops(SkyPVPUHC main, BlockBreakEvent breakEvt) {
		this.instance = main;
		this.breakEvt = breakEvt;
		this.block = breakEvt.getBlock();
		this.originalDrops = block.getDrops();
		this.drops = originalDrops;
		this.queuedDropUpdates = new ArrayList<DropUpdaterScenario>();
		
		// Let's clear the drops and schedule the task.
		breakEvt.setCancelled(true);
		block.getDrops().clear();
		
		new BukkitRunnable() {
			
			public void run() {
				dropItems();
			}
			
		}.runTaskLater(instance, 1L);
	}
	
	public void dropItems() {
		for(DropUpdaterScenario scenario : queuedDropUpdates) {
			drops = scenario.handleDrops(drops);
		}
		
		// Set the type back to AIR.
		block.setType(Material.AIR);
		
		for(ItemStack drop : drops) {
			block.getLocation().getWorld().dropItemNaturally(block.getLocation(), drop);
		}
		
		
		// We need to redrop the experience.
		int expDrop = breakEvt.getExpToDrop();
		
		ExperienceOrb exp = ((ExperienceOrb) block.getWorld().spawn(block.getLocation(), ExperienceOrb.class));
		exp.setExperience(expDrop);
		
		UHCSystem.getScenarioDrops().remove(block);
	}
	
	/**
	 * Queue a scenario type to access and change the drops.
	 * @param DropUpdaterScenario scenario
	 * @throws IllegalArgumentException if ScenarioType is not listed inside the DROP_UPDATE_ORDER ArrayList.
	 */
	
	public void queue(DropUpdaterScenario scenario) throws IllegalArgumentException {
		if(!DropUpdaterScenario.DROP_UPDATE_ORDER.contains(scenario.getType())) {
			String errorMsg = String.format("&cERROR: &4ScenarioType %s is not a valid drop type.", scenario.getType().name());
			throw new IllegalArgumentException(errorMsg);
		}
		
		// Queue the ScenarioType for drop
		if(!queuedDropUpdates.contains(scenario.getType())) {
			queuedDropUpdates.set(DropUpdaterScenario.DROP_UPDATE_ORDER.indexOf(scenario.getType()), scenario);
		}
	}
	
	public BlockBreakEvent getBlockBreakEvent() {
		return this.breakEvt;
	}
	
	public Block getBlock() {
		return this.block;
	}
	
	public Collection<ItemStack> getOriginalDrops() {
		return this.originalDrops;
	}
	
	public void setDrops(Collection<ItemStack> drops) {
		this.drops = drops;
	}
	
	public Collection<ItemStack> getDrops() {
		return this.drops;
	}
}
