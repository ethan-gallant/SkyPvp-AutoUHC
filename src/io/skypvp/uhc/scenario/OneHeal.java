package io.skypvp.uhc.scenario;

import io.skypvp.uhc.Messages;
import io.skypvp.uhc.SkyPVPUHC;
import io.skypvp.uhc.UHCSystem;
import io.skypvp.uhc.player.UHCPlayer;
import io.skypvp.uhc.scenario.event.UHCScenarioActivateEvent;

import java.util.Arrays;
import java.util.List;

import org.bukkit.EntityEffect;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class OneHeal extends Scenario {

    private ItemStack healItem;

    public OneHeal(SkyPVPUHC main) {
        super(main, ScenarioType.ONE_HEAL);
        this.healItem = null;
    }

    @EventHandler
    public void onScenarioActivate(UHCScenarioActivateEvent evt) {
        ConfigurationSection settingsSection = type.getSettingsSection();
        Material itemMaterial = Material.GOLD_HOE;
        Messages msgs = instance.getMessages();
        String requestType = settingsSection.getString("heal-item");

        try {
            itemMaterial = Material.valueOf(requestType);
        }catch (IllegalArgumentException | NullPointerException e) {
            String errorMsg = String.format("OneHeal: &cERROR: &4Material type %s could not be found. Using default Gold Hoe instead...", requestType);
            instance.sendConsoleMessage(msgs.color(errorMsg));
        }

        List<String> description = settingsSection.getStringList("heal-item-desc");

        for(int i = 0; i < description.size(); i++) {
            String line = description.get(i);
            description.set(i, msgs.color(line));
        }

        healItem = new ItemStack(itemMaterial, 1);
        healItem = UHCSystem.nameAndLoreItem(healItem, msgs.color(settingsSection.getString("heal-item-name")), description);

        for(UHCPlayer player : instance.getOnlinePlayers().values()) {
            player.getBukkitPlayer().getInventory().addItem(healItem);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent evt) {
        Action action = evt.getAction();
        Player p = evt.getPlayer();
        UHCPlayer player = instance.getOnlinePlayers().get(p.getUniqueId());

        if(isActive() && player != null && Arrays.asList(Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK).contains(action)) {
            ItemStack item = p.getItemInHand();
            if(item.isSimilar(healItem) && p.getHealth() < p.getMaxHealth()) {
                p.getInventory().remove(item);
                p.setHealth(p.getMaxHealth());
                p.playEffect(EntityEffect.WOLF_HEARTS);
                p.sendMessage(SkyPVPUHC.get().getMessages().getMessage("health-refilled"));
            }
        }
    }

    @Override
    public void unregisterEvents() {
        UHCScenarioActivateEvent.getHandlerList().unregister(this);
        PlayerInteractEvent.getHandlerList().unregister(this);
    }

}
