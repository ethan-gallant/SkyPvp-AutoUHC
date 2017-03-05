package io.skypvp.uhc.scenario;

import io.skypvp.uhc.SkyPVPUHC;
import io.skypvp.uhc.player.UHCPlayer;
import io.skypvp.uhc.player.event.UHCPlayerDeathEvent;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;

public class EnchantedDeath extends Scenario {
	
	public EnchantedDeath(SkyPVPUHC main) {
		super(main, ScenarioType.ENCHANTED_DEATH);
	}
	
	@EventHandler
	public void onPlayerDeath(UHCPlayerDeathEvent evt) {
		UHCPlayer uhcPlayer = evt.getPlayer();
		
		if(isActive()) {
			Player p = uhcPlayer.getBukkitPlayer();
			Location placeLocation = ScenarioUtil.findSafePlaceLocation(p.getLocation(), true);
			placeLocation.getBlock().setType(Material.ENCHANTMENT_TABLE);
		}
	}
	
	@EventHandler
	public void onCraftItem(PrepareItemCraftEvent evt) {
		ItemStack result = evt.getRecipe().getResult();
		if(isActive() && result.getType() == Material.ENCHANTMENT_TABLE) {
			evt.getInventory().setResult(new ItemStack(Material.AIR));
			for(HumanEntity ent : evt.getViewers()) {
				((Player) ent).sendMessage(instance.getMessages().getMessage("recipe-disabled"));
			}
		}
	}

	@Override
	public void unregisterEvents() {
		UHCPlayerDeathEvent.getHandlerList().unregister(this);
		PrepareItemCraftEvent.getHandlerList().unregister(this);
	}

}
