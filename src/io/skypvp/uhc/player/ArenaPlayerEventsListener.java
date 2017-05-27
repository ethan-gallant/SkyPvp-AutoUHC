package io.skypvp.uhc.player;

import io.skypvp.uhc.Globals;
import io.skypvp.uhc.Messages;
import io.skypvp.uhc.SkyPVPUHC;
import io.skypvp.uhc.arena.Team;
import io.skypvp.uhc.arena.UHCGame;
import io.skypvp.uhc.arena.UHCGame.GameState;
import io.skypvp.uhc.player.UHCPlayer.PlayerState;
import io.skypvp.uhc.player.event.UHCPlayerDamageEvent;
import io.skypvp.uhc.player.event.UHCPlayerDamagedByUHCPlayerEvent;
import io.skypvp.uhc.player.event.UHCPlayerDeathEvent;
import io.skypvp.uhc.player.event.UHCPlayerKillUHCPlayerEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
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
        if(uhcPlayer == null || !uhcPlayer.isInTeamChat()) return;
        
        Messages msgs = instance.getMessages();
        String format = "";
        
        // Let's add the host prefix if needed.
        String matchOwner = instance.getProfile().getOwner();
        if(matchOwner != null && matchOwner.equals(uhcPlayer.uuid)) {
            format = format.concat(msgs.getRawMessage("hostPrefix"));
        }
        
        // Let's setup the spectate prefix.
        String spectatePrefix = msgs.getRawMessage("spectatePrefix");
        
        // Let's setup the wins suffix.
        String suffix = instance.getMessages().getRawMessage("nameSuffix");
        suffix = suffix.replaceAll("\\{wins\\}", String.valueOf(uhcPlayer.getGamesWon()));
        
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
            
            // Let's setup the format.
            format = format.concat(spectatePrefix);
            
            if(game.isTeamMatch()) {
                format = format.concat(" " + uhcPlayer.getTeam().getName().substring(0, 2));
            }else {
                format = format.concat(" &a");
            }
            
            format = format.concat(ChatColor.stripColor(p.getDisplayName()));
            format = format.concat(suffix);
            format = format.concat(String.format(" &7%s", evt.getMessage()));
            evt.setFormat(ChatColor.translateAlternateColorCodes('&', format));
            
            // We don't care if this player is in a team game or whatever. Only spectators can see this message.
            // We're done.
            return;
        }
        
        if(game.isTeamMatch() && game.getState().toIndex() > GameState.PREPARING.toIndex()) {
            // Great, the player is a team game that has already started and is past the "Preparing" stage.
            Team team = uhcPlayer.getTeam();
            String teamColor = team.getName().substring(0, 2);
            
            evt.setFormat(ChatColor.translateAlternateColorCodes('&', String.format("&7[%sTEAM&7] %s%s%s &7%s", teamColor,
                    teamColor, ChatColor.stripColor(p.getDisplayName()), suffix, evt.getMessage())));
            
            if(team != null) {
                evt.getRecipients().clear();
                evt.getRecipients().add(p);
                for(UHCPlayer member : team.getMembers()) {
                    if(member.getBukkitPlayer() != p) {
                        evt.getRecipients().add(member.getBukkitPlayer());
                    }
                }
            }
            
            return;
        }
        
        if(game.isTeamMatch()) {
            format = format.concat(" " + uhcPlayer.getTeam().getName().substring(0, 2));
        }else {
            format = format.concat(" &a");
        }
        
        format = format.concat(ChatColor.stripColor(p.getDisplayName()));
        format = format.concat(suffix);
        format = format.concat(String.format(" &7%s", evt.getMessage()));
        evt.setFormat(ChatColor.translateAlternateColorCodes('&', format));
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
	public void onUHCPlayerDamagedByUHCPlayer(UHCPlayerDamagedByUHCPlayerEvent evt) {
	    UHCPlayer damaged = evt.getPlayer();
	    UHCPlayer damager = evt.getDamager();
	    EntityDamageEvent dmgEvt = evt.getDamageEvent();
	    
	    if(damaged.isInGame() && damager.isInGame()) {
	        if(game.getState() == GameState.GRACE_PERIOD) {
	            damager.getBukkitPlayer().sendMessage(instance.getMessages().getMessage("pvp-disabled"));
	            dmgEvt.setCancelled(true);
	            return;
	        }else if(damaged.getTeam() != null && damager.getTeam() != null) {
	            if(damaged.getTeam() == damager.getTeam()) {
	                damager.getBukkitPlayer().sendMessage(instance.getMessages().getMessage("pvp-team"));
	                dmgEvt.setCancelled(true);
	                return;
	            }
	        }else {
	            // The "damaged" player is in combat with "damager".
	            damaged.setInCombatWith(damager);
	        }
	    }
	}
	
	@EventHandler
	public void onPlayerHurtPlayer(EntityDamageByEntityEvent evt) {
		Entity ent = evt.getDamager();
		Entity damaged = evt.getEntity();
		
		if(damaged instanceof Player) {
            Player hit = (Player) damaged;
            UHCPlayer hitUHC = instance.getOnlinePlayers().get(hit.getUniqueId());
            if(hitUHC == null) return;
            
		    if(ent instanceof Player) {
	            Player hitter = (Player) ent;
	            UHCPlayer hitterUHC = instance.getOnlinePlayers().get(hitter.getUniqueId());
	            
	            if(hitterUHC != null) {
	                instance.getServer().getPluginManager().callEvent(new UHCPlayerDamagedByUHCPlayerEvent(hitUHC, hitterUHC, evt));
	            }
	            
		    }else if(ent instanceof Projectile) {
		        Projectile proj = (Projectile) ent;
		        
		        if(proj.getShooter() instanceof Player && evt.getDamage() > 0.0d) {
		            Player hitter = (Player) proj.getShooter();
	                UHCPlayer hitterUHC = instance.getOnlinePlayers().get(hitter.getUniqueId());
	                
		            if(hitter != hit && hitterUHC != null) {
		                instance.getServer().getPluginManager().callEvent(new UHCPlayerDamagedByUHCPlayerEvent(hitUHC, hitterUHC, evt));
		            }
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
