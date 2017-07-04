package io.skypvp.uhc.player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;

import io.skypvp.uhc.Globals;
import io.skypvp.uhc.Messages;
import io.skypvp.uhc.SkyPVPUHC;
import io.skypvp.uhc.UHCSystem;
import io.skypvp.uhc.arena.Profile;
import io.skypvp.uhc.arena.Team;
import io.skypvp.uhc.arena.UHCGame;
import io.skypvp.uhc.arena.state.GameStateManager;
import io.skypvp.uhc.player.UHCPlayer.PlayerState;
import io.skypvp.uhc.player.event.UHCPlayerDamageEvent;
import io.skypvp.uhc.player.event.UHCPlayerDamagedByUHCPlayerEvent;
import io.skypvp.uhc.player.event.UHCPlayerDeathEvent;
import io.skypvp.uhc.player.event.UHCPlayerKillUHCPlayerEvent;
import net.md_5.bungee.api.ChatColor;

public class ArenaPlayerEventsListener implements Listener {

	final SkyPVPUHC instance;
	final GameStateManager gsm;
	final UHCGame game;
	final Profile profile;
	final PluginManager pMgr;

	public ArenaPlayerEventsListener(SkyPVPUHC main) {
		this.instance = main;
		this.gsm = instance.getGameStateManager();
		this.game = instance.getGame();
		this.profile = instance.getProfile();
		this.pMgr = instance.getServer().getPluginManager();
	}

	/**
	 * Points a {@link UHCPlayer}'s compass to the nearest {@link Team} member.
	 * @param {@link UHCPlayer} uhcPlayer
	 * PRECONDITION: Assumes that the current match is a team-match
	 * PRECONDITION: The {@link Team} data member of the {@link UHCPlayer} object isn't null.
	 */

	private void pointCompassToNearestTeamMember(UHCPlayer uhcPlayer) {
		if(uhcPlayer == null) return;
		
	    Team team = uhcPlayer.getTeam();

		double shortestDist = Double.MAX_VALUE;
		Location target = null;

		Iterator<UHCPlayer> teamMembers = team.getMembers().iterator();

		while(teamMembers.hasNext()) {
			Location start = uhcPlayer.getBukkitPlayer().getLocation();
			Location dest = teamMembers.next().getBukkitPlayer().getLocation();
			double distance = dest.distance(start);

			if(distance < shortestDist) {
				target = dest;
				shortestDist = distance;
			}
		}

		if(target != null) {
			uhcPlayer.getBukkitPlayer().setCompassTarget(target);
			uhcPlayer.resetCompassUpdateTime();
		}
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

		Messages msgs = instance.getMessages();
		String format = "";

		// Let's add the host prefix if needed.
		String matchOwner = instance.getProfile().getOwner();
		if(matchOwner != null && matchOwner.equals(uhcPlayer.uuid)) {
			format = format.concat(msgs.getRawMessage("host-prefix"));
		}else if(UHCSystem.isMatchAdmin(p)) {
		    format = format.concat(msgs.getRawMessage("admin-prefix"));
		}

		// Let's setup the spectate prefix.
		String spectatePrefix = msgs.getRawMessage("spectate-prefix");

		// Let's setup the wins suffix.
		String suffix = instance.getMessages().getRawMessage("name-suffix");
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

			if(profile.isTeamMatch()) {
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

		if(profile.isTeamMatch() && gsm.getActiveState().toIndex() > 3 && uhcPlayer.isInTeamChat()) {
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

		if(profile.isTeamMatch() && uhcPlayer.getTeam() != null) {
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

			if(uhcPlayer != null && isPlayerInGame(uhcPlayer) && gsm.getActiveState().toIndex() > 4) {
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
			if(gsm.getActiveState().getName().equalsIgnoreCase("gracePeriod")) {
				damager.getBukkitPlayer().sendMessage(instance.getMessages().getMessage("pvp-disabled"));
				dmgEvt.setCancelled(true);
				return;
			}else if(damaged.getTeam() != null && damager.getTeam() != null) {
				if(damaged.getTeam() == damager.getTeam() && !profile.allowsFriendlyFire()) {
					damager.getBukkitPlayer().sendMessage(instance.getMessages().getMessage("pvp-team"));
					dmgEvt.setCancelled(true);
					return;
				}
			}
			
			// The "damaged" player is in combat with "damager".
			damaged.setInCombatWith(damager);
		}
	}
	
	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent evt) {
	    UHCPlayer p = instance.getOnlinePlayers().get(((Player) evt.getEntity()).getUniqueId());
	    
	    if(p != null && Globals.INVULNERABILITY_PERIODS.contains(gsm.getActiveState().toIndex())
	            || p.getState() == PlayerState.SPECTATING) {
	        evt.setCancelled(true);
	    }
	}

	@EventHandler
	public void onPlayerHurtPlayer(EntityDamageByEntityEvent evt) {
		Entity ent = evt.getDamager();
		Entity damaged = evt.getEntity();

		if(damaged instanceof Player) {
			Player hit = (Player) damaged;
			UHCPlayer hitUHC = instance.getOnlinePlayers().get(hit.getUniqueId());
			if(hitUHC == null || gsm.getActiveState().toIndex() < 5) return;

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

		if(uhcPlayer != null) {
			Location from = evt.getFrom();
			Location to = evt.getTo();

			if(uhcPlayer.getState() == PlayerState.FROZEN) {
				if(to.getX() != from.getX() || to.getZ() != from.getZ()) {
					player.teleport(from);

					if(System.currentTimeMillis() >= uhcPlayer.getLastFreezeWarning() + Globals.FROZEN_WARNING_TIME) {
						player.sendMessage(instance.getMessages().getMessage("move-disabled"));
						uhcPlayer.resetFreezeCooldown();
					}
				}
			}else if(gsm.getActiveState().toIndex() > 3 && profile.isTeamMatch()) {
			    if(System.currentTimeMillis() >= uhcPlayer.getLastCompassUpdateTime() + Globals.COMPASS_UPDATE_TIME) {
		             pointCompassToNearestTeamMember(uhcPlayer);
			    }
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
		}else if(damaged instanceof Player) {
		    if(gsm.getActiveState().toIndex() < 5) {
		        evt.setCancelled(true);
		    }
		}
	}

	private boolean isPlayerInGame(UHCPlayer player) {
		List<Integer> outGameStates = new ArrayList<Integer>(Arrays.asList(0, 1));
		return player.isInGame() && !outGameStates.contains(gsm.getActiveState().toIndex());
	}
}
