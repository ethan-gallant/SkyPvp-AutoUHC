package io.skypvp.uhc.scenario;

import io.skypvp.uhc.Globals;
import io.skypvp.uhc.SkyPVPUHC;
import io.skypvp.uhc.player.UHCPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

public class Cutclean extends Scenario {
	
	public static HashMap<Material, Material> SMELTED_VERSIONS;
	
	static {
		SMELTED_VERSIONS = new HashMap<Material, Material>();
		SMELTED_VERSIONS.put(Material.IRON_ORE, Material.IRON_INGOT);
		SMELTED_VERSIONS.put(Material.GOLD_ORE, Material.GOLD_INGOT);
		SMELTED_VERSIONS.put(Material.RAW_BEEF, Material.COOKED_BEEF);
		SMELTED_VERSIONS.put(Material.RAW_CHICKEN, Material.COOKED_CHICKEN);
		SMELTED_VERSIONS.put(Material.RAW_FISH, Material.COOKED_FISH);
		SMELTED_VERSIONS.put(Material.POTATO_ITEM, Material.BAKED_POTATO);
		SMELTED_VERSIONS.put(Material.PORK, Material.GRILLED_PORK);
	}
	
	public Cutclean(SkyPVPUHC main) {
		super(main, ScenarioType.CUTCLEAN);
	}
	
	private Collection<ItemStack> handleDrops(Location dropLocation, Collection<ItemStack> drops) {
		ArrayList<ItemStack> newDrops = new ArrayList<ItemStack>();
		for(ItemStack item : drops) {
			Material smeltedType = Cutclean.SMELTED_VERSIONS.get(item.getType());
			
			// If we have a smelted version available, let's change the drops.
			if(smeltedType != null) {
				// Let's drop the smelted version instead.
				ItemStack newItem = new ItemStack(smeltedType, item.getAmount());
				dropLocation.getWorld().dropItemNaturally(dropLocation, newItem);
				newDrops.add(newItem);
			}else {
				// No smelted version, just drop it.
				dropLocation.getWorld().dropItemNaturally(dropLocation, item);
				newDrops.add(item);
			}
		}
		
		return newDrops;
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent evt) {
		Player p = evt.getPlayer();
		UHCPlayer uhcPlayer = instance.getOnlinePlayers().get(p.getUniqueId());
		Block b = evt.getBlock();
		Collection<ItemStack> originalDrops = b.getDrops();
		
		if(uhcPlayer != null && isActive() && !evt.isCancelled()) {
			evt.setCancelled(true);
			b.getDrops().clear();
			b.getDrops().addAll(handleDrops(b.getLocation(), originalDrops));
			
			// We need to redrop the experience.
			int expDrop = evt.getExpToDrop();
			b.setType(Material.AIR);
			
			ExperienceOrb exp = ((ExperienceOrb) b.getWorld().spawn(b.getLocation(), ExperienceOrb.class));
			exp.setExperience(expDrop);
		}
	}
	
	@EventHandler
	public void onEntityDeath(EntityDeathEvent evt) {
		Entity dead = evt.getEntity();
		
		if(isActive() && dead.getWorld().getName().equalsIgnoreCase(Globals.GAME_WORLD_NAME)) {
			List<ItemStack> drops = evt.getDrops();
			evt.getDrops().clear();
			
			evt.getDrops().addAll(handleDrops(dead.getLocation(), drops));
		}
	}

	@Override
	public void unregisterEvents() {
		BlockBreakEvent.getHandlerList().unregister(this);
		EntityDeathEvent.getHandlerList().unregister(this);
	}
	
	public static HashMap<Material, Material> getSMELTED_VERSIONS() {
		return Cutclean.SMELTED_VERSIONS;
	}

}
