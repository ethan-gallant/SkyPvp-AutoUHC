package io.skypvp.uhc.player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.skypvp.uhc.SkyPVPUHC;
import io.skypvp.uhc.arena.UHCGame;
import io.skypvp.uhc.arena.UHCGame.GameState;
import io.skypvp.uhc.player.event.UHCPlayerDamageEvent;
import io.skypvp.uhc.player.event.UHCPlayerDeathEvent;
import io.skypvp.uhc.player.event.UHCPlayerKillUHCPlayer;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.PluginManager;

public class ArenaPlayerEventsListener implements Listener {
	
	final SkyPVPUHC instance;
	final UHCGame game;
	final PluginManager pMgr;
	
	public ArenaPlayerEventsListener(SkyPVPUHC main) {
		this.instance = main;
		this.game = instance.getGame();
		this.pMgr = instance.getServer().getPluginManager();
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent evt) {
		Player p = evt.getEntity();
		UHCPlayer uhcPlayer = instance.getOnlinePlayers().get(p.getUniqueId());
		
		if(uhcPlayer != null && isPlayerInGame(uhcPlayer)) {
			pMgr.callEvent(new UHCPlayerDeathEvent(uhcPlayer));
		}
	}
	
	@EventHandler
	public void onPlayerDamaged(EntityDamageEvent evt) {
		Entity ent = evt.getEntity();
		if(ent instanceof Player) {
			Player p = (Player) ent;
			UHCPlayer uhcPlayer = instance.getOnlinePlayers().get(p.getUniqueId());
			
			if(uhcPlayer != null && isPlayerInGame(uhcPlayer)) {
				pMgr.callEvent(new UHCPlayerDamageEvent(uhcPlayer, evt));
			}
		}
	}
	
	@EventHandler
	public void onPlayerKillEntity(EntityDamageByEntityEvent evt) {
		Entity damager = evt.getDamager();
		Entity damaged = evt.getEntity();
		
		if(damager instanceof Player && damaged instanceof Player) {
			Player killer = (Player) damager;
			Player killed = (Player) damaged;
			UHCPlayer uhcKiller = instance.getOnlinePlayers().get(killer.getUniqueId());
			UHCPlayer uhcKilled = instance.getOnlinePlayers().get(killed.getUniqueId());
			
			if(uhcKiller != null && uhcKiller != null && isPlayerInGame(uhcKiller) && isPlayerInGame(uhcKilled)) {
				if(killed.getHealth() - evt.getDamage() <= 0 && !evt.isCancelled()) {
					pMgr.callEvent(new UHCPlayerKillUHCPlayer(uhcKiller, uhcKilled));
				}
			}
		}
	}
	
	private boolean isPlayerInGame(UHCPlayer player) {
		List<GameState> outGameStates = new ArrayList<GameState>(Arrays.asList(GameState.WAITING, GameState.PREPARING, GameState.FINISHED));
		return player.isInGame() && !outGameStates.contains(game.getState());
	}
}
