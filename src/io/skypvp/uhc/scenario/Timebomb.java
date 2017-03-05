package io.skypvp.uhc.scenario;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import io.skypvp.uhc.SkyPVPUHC;
import io.skypvp.uhc.player.UHCPlayer;
import io.skypvp.uhc.player.event.UHCPlayerDeathEvent;
import io.skypvp.uhc.scenario.event.UHCScenarioActivateEvent;

public class Timebomb extends Scenario {
	
	private BukkitTask clearTask;
	private final HashMap<Location, Long> chests;
	
	public Timebomb(SkyPVPUHC main) {
		super(main, ScenarioType.TIMEBOMB);
		this.chests = new HashMap<Location, Long>();
		this.clearTask = null;
	}
	
	public void beginClearTask() {
		if(clearTask != null) return;
		clearTask = new BukkitRunnable() {
		
			public void run() {
				Iterator<Location> keyIterator = chests.keySet().iterator();
				while(keyIterator.hasNext()) {
					Location chestLoc = keyIterator.next();
					long explodeTime = chests.get(chestLoc);
					if(System.currentTimeMillis() >= explodeTime && chestLoc.getBlock().getType() == Material.CHEST) {
						// Clear the contents of the chest and remove
						// the chest from our HashMap.
						Chest chest = (Chest) chestLoc.getBlock().getState();
						chest.getBlockInventory().clear();
						chests.remove(chestLoc);
						
						// Explode!
						chestLoc.getWorld().createExplosion(chestLoc, 2.0F);
					}
				}
			}
			
		}.runTaskTimer(instance, 20L, 20L);
	}
	
	public void cancelClearTask() {
		if(clearTask != null) {
			clearTask.cancel();
			clearTask = null;
		}
	}
	
	@EventHandler
	public void onScenarioActivate(UHCScenarioActivateEvent evt) {
		Scenario scenario = evt.getScenario();
		if(scenario.getType() == type) {
			beginClearTask();
		}
	}
	
	@EventHandler
	public void onPlayerDeath(UHCPlayerDeathEvent evt) {
		UHCPlayer player = evt.getPlayer();
		Location deathLocation = player.getBukkitPlayer().getLocation();
		List<ItemStack> drops = evt.getDeathEvent().getDrops();
		
		// Okay, let's clear the drops from the event and put them in a chest.
		evt.getDeathEvent().getDrops().clear();
		
		// Let's find an empty place to put our chest.
		Location placeLocation = ScenarioUtil.findSafePlaceLocation(deathLocation, true);
		Block block = placeLocation.getBlock();
		
		// Let's setup the chest.
		block.setType(Material.CHEST);
		Chest chest = (Chest) block.getState();
		
		// Add our items.
		for(ItemStack item : drops) {
			chest.getBlockInventory().addItem(item);
		}
		
		// Let's add this chest to the HashMap.
		long explodeMs = instance.getSettings().getTimebombExplodeTime() * 1000;
		chests.put(block.getLocation(), System.currentTimeMillis() + explodeMs);
	}
	
	@EventHandler
	public void onEntityExplode(EntityExplodeEvent evt) {
		if(isActive() && !evt.isCancelled()) {
			for(Block b : evt.blockList()) {
				if(chests.containsKey(b.getLocation())) {
					chests.remove(b.getLocation());
					break;
				}
			}
		}
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent evt) {
		Block b = evt.getBlock();
		
		// Let's remove the chest from the list, if someone breaks
		// the actual chest.
		if(isActive() && chests.containsKey(b.getLocation()) && !evt.isCancelled()) {
			chests.remove(b.getLocation());
		}
	}
	
	@Override
	public void unregisterEvents() {
		UHCPlayerDeathEvent.getHandlerList().unregister(this);
		UHCScenarioActivateEvent.getHandlerList().unregister(this);
		BlockBreakEvent.getHandlerList().unregister(this);
		EntityExplodeEvent.getHandlerList().unregister(this);
		
		// Let's cancel the clear task.
		cancelClearTask();
	}
	
}
