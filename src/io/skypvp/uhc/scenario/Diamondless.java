package io.skypvp.uhc.scenario;

import io.skypvp.uhc.SkyPVPUHC;
import io.skypvp.uhc.player.UHCPlayer;
import io.skypvp.uhc.player.event.UHCPlayerDeathEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;

public class Diamondless extends Scenario {
	
	private ArrayList<Material> inaccessibleTypes;
	
	public Diamondless(SkyPVPUHC main) {
		super(main, ScenarioType.DIAMONDLESS);
		this.inaccessibleTypes = new ArrayList<Material>(Arrays.asList(Material.DIAMOND_ORE, Material.DIAMOND_BLOCK));
	}
	
	@EventHandler
	public void onEntityExplosion(EntityExplodeEvent evt) {
		if(!isActive() || evt.isCancelled()) return;
		Iterator<Block> bIterator = evt.blockList().iterator();
		
		while(bIterator.hasNext()) {
			Block b = bIterator.next();
			
			if(inaccessibleTypes.contains(b.getType())) {
				bIterator.remove();
			}
		}
	}
	
	@EventHandler
	public void onPlayerDeath(UHCPlayerDeathEvent evt) {
		UHCPlayer uhcPlayer = evt.getPlayer();
		
		if(isActive()) {
			Player p = uhcPlayer.getBukkitPlayer();
			p.getWorld().dropItem(p.getLocation(), new ItemStack(Material.DIAMOND, 2));
		}
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent evt) {
		Player p = evt.getPlayer();
		Block b = evt.getBlock();
		UHCPlayer uhcPlayer = instance.getOnlinePlayers().get(p.getUniqueId());
		
		if(uhcPlayer != null && inaccessibleTypes.contains(b.getType()) && active) {
			evt.setExpToDrop(0);
			evt.setCancelled(true);
		}
	}

	@Override
	public void unregisterEvents() {
		BlockBreakEvent.getHandlerList().unregister(this);
		EntityExplodeEvent.getHandlerList().unregister(this);
		UHCPlayerDeathEvent.getHandlerList().unregister(this);
	}

}
