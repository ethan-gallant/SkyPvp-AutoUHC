package io.skypvp.uhc.arena;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.wimbli.WorldBorder.BorderData;

import io.skypvp.uhc.Globals;
import io.skypvp.uhc.Messages;
import io.skypvp.uhc.Settings;
import io.skypvp.uhc.SkyPVPUHC;
import io.skypvp.uhc.UHCSystem;
import io.skypvp.uhc.arena.state.GameState;
import io.skypvp.uhc.arena.state.GameStateManager;
import io.skypvp.uhc.arena.state.TimedGameState;
import io.skypvp.uhc.menu.Menu;
import io.skypvp.uhc.menu.TeamSelectorMenu;
import io.skypvp.uhc.player.UHCPlayer;
import io.skypvp.uhc.player.event.UHCPlayerChangeTeamEvent;
import io.skypvp.uhc.player.event.UHCPlayerDeathEvent;
import io.skypvp.uhc.scenario.ScenarioType;
import io.skypvp.uhc.timer.event.UHCMatchTimerExpiredEvent;

public class ArenaEventsListener implements Listener {

    final SkyPVPUHC main;
    final Settings settings;
    final Messages msgs;
    final UHCGame game;

    public ArenaEventsListener(SkyPVPUHC instance, UHCGame game) {
        this.main = instance;
        this.settings = main.getSettings();
        this.msgs = main.getMessages();
        this.game = game;
    }

    //////////////////////////////////////////////////////////////

    /*
     * Let's just handle Menu events here because I'm
     * too lazy to create another listener class.
     */

    @EventHandler
    public void onInventoryClick(InventoryClickEvent evt) {
        Inventory inv = evt.getInventory();
        Player p = ((Player) evt.getWhoClicked());
        UHCPlayer player = main.getOnlinePlayers().get(p.getUniqueId());
        Menu activeMenu = player.getActiveMenu();

        if(activeMenu != null && activeMenu.getUI().equals(inv)) {
            activeMenu.clickPerformed(evt);
            evt.setCancelled(true);
        }else if(activeMenu == null) {
            ItemStack item = evt.getCurrentItem();
            if(item != null && item.isSimilar(settings.getTeamSelectorItem())) {
                UHCSystem.openMenu(player, new TeamSelectorMenu(main, player));
                evt.setCancelled(true);
            }
        }

        ItemStack clickedItem = evt.getCurrentItem();
        if(clickedItem != null && UHCSystem.isRestrictedItem(clickedItem)) evt.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent evt) {
        Inventory inv = evt.getInventory();
        Player p = ((Player) evt.getPlayer());
        UHCPlayer player = main.getOnlinePlayers().get(p.getUniqueId());
        Menu activeMenu = player.getActiveMenu();

        if(activeMenu != null && activeMenu.getUI() == inv) {
            activeMenu.closed();
            player.setActiveMenu(null);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent evt) {
        UHCPlayer player = main.getOnlinePlayers().get(evt.getPlayer().getUniqueId());
        ItemStack item = evt.getItem();
        Action action = evt.getAction();

        if(action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            if(item != null) {
                if(item.isSimilar(settings.getTeamSelectorItem())) {
                    UHCSystem.openMenu(player, new TeamSelectorMenu(main, player));
                }

                if(UHCSystem.isRestrictedItem(item)) evt.setCancelled(true);
            }
        }
    }

    //////////////////////////////////////////////////////////////

    @EventHandler
    public void onUHCPlayerChangeTeam(UHCPlayerChangeTeamEvent evt) {
        Player player = evt.getPlayer().getBukkitPlayer();
        for(UHCPlayer p : main.getOnlinePlayers().values()) {
            if(p.getActiveMenu() != null && p.getActiveMenu() instanceof TeamSelectorMenu) {
                p.getActiveMenu().show();
            }

            String message = msgs.getRawMessage("player-joined-team");
            int index = message.indexOf("{player}");
            message = message.replaceAll("\\{team\\}", UHCSystem.getTeamNameWithPrefix(evt.getTeam()));
            message = message.replaceAll("\\{player\\}", player.getDisplayName());

            if(p.getBukkitPlayer().getDisplayName().equalsIgnoreCase(player.getDisplayName())) {
                message = Globals.CLIENT_COLOR + message.substring(index, message.length());
            }

            p.getBukkitPlayer().sendMessage(msgs.constructMessage(message));
        }
    }

    @EventHandler
    public void onUHCPlayerDeath(UHCPlayerDeathEvent evt) {
        UHCPlayer player = evt.getPlayer();
        Player cbPlayer = player.getBukkitPlayer();

        // The player isn't in the match anymore.
        //game.handlePlayerExit(player);

        // Let's do the lightning effect if it's enabled.
        if(settings.wantLightningDeaths()) main.getWorldHandler().getGameWorld().getCBWorld().strikeLightningEffect(cbPlayer.getLocation());

        // Let's setup the message.
        String message = msgs.getRawMessage("player-died");
        message = message.replaceAll("\\{player\\}", cbPlayer.getDisplayName());

        ExperienceOrb exp = ((ExperienceOrb) cbPlayer.getWorld().spawn(cbPlayer.getLocation(), ExperienceOrb.class));
        exp.setExperience(evt.getDeathEvent().getDroppedExp());

        if(!game.isScenarioActive(ScenarioType.TIMEBOMB)) {
            for(ItemStack drop : evt.getDeathEvent().getDrops()) {
                cbPlayer.getWorld().dropItemNaturally(cbPlayer.getLocation(), drop);
            }
        }

        player.prepareForGame();
        UHCSystem.broadcastMessageAndSound(msgs.color(message), Sound.ENDERDRAGON_GROWL, 4F);
    }

    @EventHandler
    public void onUHCMatchTimerExpire(UHCMatchTimerExpiredEvent evt) {
        GameStateManager gsm = main.getGameStateManager();
        if(gsm.getActiveState().getName().equalsIgnoreCase("mapShrink")) {
            BorderData border = main.getWorldBorder().getWorldBorder(Globals.GAME_WORLD_NAME);
            int borderX = border.getRadiusX();
            if(borderX - 250 >= 250) {
                borderX -= 250;
            }else if(borderX > 50 && borderX <= 250) {
                borderX -= 50;
            }

            main.getWorldHandler().setBorder(borderX, false);
            border = main.getWorldBorder().getWorldBorder(Globals.GAME_WORLD_NAME);
            double minX = border.getX() - border.getRadiusX();
            double maxX = border.getX() + border.getRadiusX();
            double minZ = border.getZ() - border.getRadiusZ();
            double maxZ = border.getZ() + border.getRadiusZ();

            for(UHCPlayer player : main.getOnlinePlayers().values()) {
                Player p = player.getBukkitPlayer();
                Location l = p.getLocation();


                if(!(minX < l.getX() && l.getX() < maxX) || (!(minZ < l.getZ() && l.getZ() < maxZ))) {
                    p.teleport(UHCSystem.getRandomSpawnPoint(minX, maxX, minZ, maxZ));
                    p.sendMessage("You've been teleported so you're within the new border.");
                }
            }

            if(borderX == 50) gsm.setActiveState(gsm.getStates().get(6), true);
        }else if(gsm.getActiveState() instanceof TimedGameState) {
            GameState nextState = (gsm.getActiveState().toIndex() < gsm.getStates().size()) 
                    ? (gsm.getStates().get(gsm.getActiveState().toIndex() + 1)) : gsm.getStates().get(0);
                    gsm.setActiveState(nextState, true);
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onItemCraft(PrepareItemCraftEvent evt) {
        ItemStack result = evt.getRecipe().getResult();
        if(!settings.wantGodApples() && result.getType() == Material.GOLDEN_APPLE && result.getData().getData() == 1) {
            evt.getInventory().setResult(new ItemStack(Material.AIR));
            for(HumanEntity ent : evt.getViewers()) {
                ((Player) ent).sendMessage(msgs.getMessage("recipe-disabled"));
            }
        }
    }
}
