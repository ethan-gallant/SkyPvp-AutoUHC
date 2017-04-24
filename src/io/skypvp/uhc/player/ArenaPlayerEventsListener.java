package io.skypvp.uhc.player;

import io.skypvp.uhc.Globals;
import io.skypvp.uhc.SkyPVPUHC;
import io.skypvp.uhc.arena.Team;
import io.skypvp.uhc.arena.UHCGame;
import io.skypvp.uhc.arena.UHCGame.GameState;
import io.skypvp.uhc.player.UHCPlayer.PlayerState;
import io.skypvp.uhc.player.event.UHCPlayerDamageEvent;
import io.skypvp.uhc.player.event.UHCPlayerDeathEvent;
import io.skypvp.uhc.player.event.UHCPlayerKillUHCPlayerEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
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
			// Update the scoreboards.
			for(UHCPlayer player : instance.getOnlinePlayers().values()) {
				if(player.getScoreboard() != null) {
					player.getScoreboard().generate(player);
				}
			}
		}
	}
	
    
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent evt) {
        Player p = evt.getPlayer();
        UHCPlayer uhcPlayer = instance.getOnlinePlayers().get(p.getUniqueId());
        if(uhcPlayer == null) return;
        
        // If the player is spectating, we only want spectators to see the message.
        if(uhcPlayer.getState() == PlayerState.SPECTATING) {
            Iterator<Player> pIterator = evt.getRecipients().iterator();
            while(pIterator.hasNext()) {
                Player receiver = pIterator.next();
                UHCPlayer uPlayer = instance.getOnlinePlayers().get(receiver.getUniqueId());
                if(uPlayer != null && uPlayer.getState() != PlayerState.SPECTATING) {
                    evt.getRecipients().remove(receiver);
                }
            }
            
            // We don't care if this player is in a team game or whatever. Only spectators can see this message.
            // We're done.
            return;
        }
        
        if(game.isTeamMatch() && game.getState().toIndex() > GameState.PREPARING.toIndex()) {
            // Great, the player is a team game that has already started and is past the "Preparing" stage.
            Team team = uhcPlayer.getTeam();
            
            if(team != null) {
                evt.getRecipients().clear();
                for(UHCPlayer member : team.getMembers()) {
                    if(member.getBukkitPlayer() != p) {
                        evt.getRecipients().add(member.getBukkitPlayer());
                    }
                }
            }
        }
    }
	
	@EventHandler
	public void onPlayerDamaged(EntityDamageEvent evt) {
		Entity ent = evt.getEntity();
		if(ent instanceof Player) {
			Player p = (Player) ent;
			UHCPlayer uhcPlayer = instance.getOnlinePlayers().get(p.getUniqueId());
			
			if(uhcPlayer != null && isPlayerInGame(uhcPlayer) && !Globals.INVULNERABILITY_PERIODS.contains(game.getState())) {
				if(p.getHealth() - evt.getDamage() > 0.0) {
					pMgr.callEvent(new UHCPlayerDamageEvent(uhcPlayer, evt));
				}else {
					ArrayList<ItemStack> drops = new ArrayList<ItemStack>();
					
					for(ItemStack drop : p.getInventory().getContents()) {
						if(drop != null && drop.getType() != Material.AIR) {
							drops.add(drop);
						}
					}
					
					PlayerDeathEvent deathEvt = new PlayerDeathEvent(p, drops, (int) p.getExp(), "");
					pMgr.callEvent(new UHCPlayerDeathEvent(uhcPlayer, deathEvt));
					evt.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerPickupItems(PlayerPickupItemEvent evt) {
		Player p = evt.getPlayer();
		UHCPlayer uhcPlayer = instance.getOnlinePlayers().get(p.getUniqueId());
		
		if(uhcPlayer != null && uhcPlayer.getState() == PlayerState.SPECTATING) {
			evt.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerHurtPlayer(EntityDamageByEntityEvent evt) {
		Entity ent = evt.getDamager();
		Entity damaged = evt.getEntity();
		
		if(damaged instanceof Player && ent instanceof Player) {
			Player hitter = (Player) ent;
			Player hit = (Player) damaged;
			UHCPlayer hitterUHC = instance.getOnlinePlayers().get(hitter.getUniqueId());
			UHCPlayer hitUHC = instance.getOnlinePlayers().get(hit.getUniqueId());
			
			if(hitUHC != null && hitterUHC != null && hitUHC.isInGame() && hitterUHC.isInGame()) {
				if(hitUHC.getTeam() != null && hitterUHC.getTeam() != null) {
					if(hitUHC.getTeam() == hitterUHC.getTeam()) {
						hitterUHC.getBukkitPlayer().sendMessage(instance.getMessages().getMessage("pvp-team"));
						evt.setCancelled(true);
						return;
					}
				}else if(game.getState() == GameState.GRACE_PERIOD) {
					hitterUHC.getBukkitPlayer().sendMessage(instance.getMessages().getMessage("pvp-disabled"));
					evt.setCancelled(true);
					return;
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent evt) {
		Player player = evt.getPlayer();
		UHCPlayer uhcPlayer = instance.getOnlinePlayers().get(player.getUniqueId());
		
		if(uhcPlayer != null && Arrays.asList(PlayerState.FROZEN, PlayerState.SPECTATING).contains(uhcPlayer.getState())) {
			evt.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent evt) {
		Player player = evt.getPlayer();
		UHCPlayer uhcPlayer = instance.getOnlinePlayers().get(player.getUniqueId());
		
		if(uhcPlayer != null && uhcPlayer.getState() == PlayerState.FROZEN) {
			Location from = evt.getFrom();
			Location to = evt.getTo();
			if(to.getX() != from.getX() || to.getZ() != from.getZ()) {
				player.teleport(from);
				player.sendMessage(instance.getMessages().getMessage("move-disabled"));
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
					pMgr.callEvent(new UHCPlayerKillUHCPlayerEvent(uhcKiller, uhcKilled));
				}
			}
		}
	}
	
	private boolean isPlayerInGame(UHCPlayer player) {
		List<GameState> outGameStates = new ArrayList<GameState>(Arrays.asList(GameState.WAITING, GameState.PREPARING, GameState.FINISHED));
		return player.isInGame() && !outGameStates.contains(game.getState());
	}
}
