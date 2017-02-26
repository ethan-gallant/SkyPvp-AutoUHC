package io.skypvp.uhc.scenario;

import io.skypvp.uhc.SkyPVPUHC;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class TripleOres extends Scenario {
	
	public static ArrayList<Material> ORES;
	
	static {
		ORES.add(Material.COAL_ORE);
		ORES.add(Material.REDSTONE_ORE);
		ORES.add(Material.EMERALD_ORE);
		ORES.add(Material.GOLD_ORE);
		ORES.add(Material.IRON_ORE);
		ORES.add(Material.LAPIS_ORE);
		ORES.add(Material.DIAMOND_ORE);
	}
	
	public TripleOres(SkyPVPUHC main) {
		super(main, ScenarioType.TRIPLE_ORES);
	}
	
	public void handleOre(Block b) {
		Iterator<ItemStack> drops = b.getDrops().iterator();
		while(drops.hasNext()) {
			ItemStack drop = drops.next();
			ItemStack update = new ItemStack(drop.getType(), (drop.getAmount() * 3) - drop.getAmount());
			b.getLocation().getWorld().dropItemNaturally(b.getLocation(), update);
			b.getDrops().add(update);
		}
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent evt) {
		Block b = evt.getBlock();
		
		if(isActive() && !evt.isCancelled() && ORES.contains(b.getType())) {
			handleOre(b);
		}
	}

	@Override
	public void unregisterEvents() {
		BlockBreakEvent.getHandlerList().unregister(this);
	}
	
	public static ArrayList<Material> getOres() {
		return TripleOres.ORES;
	}

}
