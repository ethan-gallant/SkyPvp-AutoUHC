package io.skypvp.uhc.scenario;

import io.skypvp.uhc.Globals;
import io.skypvp.uhc.SkyPVPUHC;
import io.skypvp.uhc.UHCSystem;
import io.skypvp.uhc.player.UHCPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

public class Cutclean extends DropUpdaterScenario {
	
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
	
	public Collection<ItemStack> handleDrops(Collection<ItemStack> drops) {
		ArrayList<ItemStack> newDrops = new ArrayList<ItemStack>();
		for(ItemStack item : drops) {
			Material smeltedType = Cutclean.SMELTED_VERSIONS.get(item.getType());
			
			// If we have a smelted version available, let's change the drops.
			if(smeltedType != null) {
				// Let's use the smelted version instead.
				ItemStack newItem = new ItemStack(smeltedType, item.getAmount());
				newDrops.add(newItem);
			}else {
				// No smelted version, just add it back.
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
		
		if(uhcPlayer != null && isActive()) {
			ScenarioDrops drops = UHCSystem.getScenarioDrops(b);
			if(drops == null && evt.isCancelled()) return;
			
			if(drops == null) {
				drops = new ScenarioDrops(instance, evt);
				UHCSystem.addScenarioDrop(b, drops);
			}
			
			drops.queue(this);
		}
	}
	
	@EventHandler
	public void onEntityDeath(EntityDeathEvent evt) {
		Entity dead = evt.getEntity();
		
		if(isActive() && dead.getWorld().getName().equalsIgnoreCase(Globals.GAME_WORLD_NAME)) {
			List<ItemStack> drops = evt.getDrops();
			evt.getDrops().clear();
			
			for(ItemStack drop : handleDrops(drops)) {
				dead.getLocation().getWorld().dropItemNaturally(dead.getLocation(), drop);
			}
		}
	}

	@Override
	public void unregisterEvents() {
		BlockBreakEvent.getHandlerList().unregister(this);
		EntityDeathEvent.getHandlerList().unregister(this);
	}
	
	public static HashMap<Material, Material> getSmeltedVersions() {
		return Cutclean.SMELTED_VERSIONS;
	}

}
