package io.skypvp.uhc.arena;

import io.skypvp.uhc.Globals;
import io.skypvp.uhc.Messages;
import io.skypvp.uhc.Settings;
import io.skypvp.uhc.SkyPVPUHC;
import io.skypvp.uhc.UHCSystem;
import io.skypvp.uhc.arena.UHCGame.GameState;
import io.skypvp.uhc.menu.Menu;
import io.skypvp.uhc.menu.TeamSelectorMenu;
import io.skypvp.uhc.player.UHCPlayer;
import io.skypvp.uhc.player.event.UHCPlayerChangeTeamEvent;
import io.skypvp.uhc.player.event.UHCPlayerDeathEvent;
import io.skypvp.uhc.timer.MatchTimer;
import io.skypvp.uhc.timer.TimerUtils;
import io.skypvp.uhc.timer.event.UHCMatchTimerExpiredEvent;

import org.bukkit.Bukkit;
import org.bukkit.Material;
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
		Player cbPlayer = Bukkit.getPlayer(player.getUUID());
		
		// The player isn't in the match anymore.
		game.handlePlayerExit(player);
		
		// Let's do the lightning effect if it's enabled.
		if(settings.wantLightningDeaths()) main.getWorldHandler().getGameWorld().getCBWorld().strikeLightningEffect(cbPlayer.getLocation());
		
		// Let's setup the message.
		String message = msgs.getRawMessage("player-died");
		message = message.replaceAll("\\{player\\}", cbPlayer.getDisplayName());
		
		// Let's send the message to all the players in the match still.
		for(UHCPlayer p : game.getPlayers()) {
			p.getBukkitPlayer().sendMessage(msgs.constructMessage(message));
		}
	}
	
	@EventHandler
	public void onUHCMatchTimerExpire(UHCMatchTimerExpiredEvent evt) {
		MatchTimer timer = evt.getTimer();
		
		if(game.getState() == GameState.STARTING && timer == game.getTimer()) {
			int[] array = TimerUtils.convertToMinutesAndSeconds(settings.getGracePeriodTime());
			timer.set("Grace Period", array[0], array[1]);
		}
		
		if(timer == game.getTimer()) {
			timer.runTaskTimer(main, 0L, 20L);
		}else {
			
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onItemCraft(PrepareItemCraftEvent evt) {
		ItemStack result = evt.getRecipe().getResult();
		if(!settings.wantGodApples() && result.getType() == Material.GOLDEN_APPLE && result.getData().getData() == 1) {
			for(HumanEntity ent : evt.getViewers()) {
				((Player) ent).sendMessage(msgs.getMessage("recipe-disabled"));
			}
		}
	}
}
