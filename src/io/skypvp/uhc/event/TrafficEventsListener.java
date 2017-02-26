package io.skypvp.uhc.event;

import io.skypvp.uhc.Database;
import io.skypvp.uhc.Globals;
import io.skypvp.uhc.SkyPVPUHC;
import io.skypvp.uhc.UHCSystem;
import io.skypvp.uhc.arena.Team;
import io.skypvp.uhc.arena.UHCGame;
import io.skypvp.uhc.arena.UHCGame.GameState;
import io.skypvp.uhc.player.UHCPlayer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class TrafficEventsListener implements Listener {
	
	final SkyPVPUHC main;
	final Database database;
	
	public TrafficEventsListener(SkyPVPUHC instance) {
		this.main = instance;
		this.database = main.getSettings().getDatabase();
	}
	
	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent evt) {
		ItemStack item = evt.getItemDrop().getItemStack();
		evt.setCancelled(UHCSystem.isRestrictedItem(item));
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent evt) {
		final Player p = evt.getPlayer();
		
		new BukkitRunnable() {
			
			public void run() {
				ResultSet rs = database.query(String.format("SELECT * FROM %s WHERE UUID='%s';", Globals.TABLE_NAME, p.getUniqueId()));
				UHCPlayer player;
				
				// We need to handle creating new profiles and obtaining them.
				if(rs == null) {
					try {
						String update = String.format("INSERT INTO %s (UUID, GAMES_PLAYED, GAMES_WON, KILLS, DEATHS) VALUES (?, 0, 0, 0, 0)",
								Globals.TABLE_NAME);
						PreparedStatement statement = database.getConnection().prepareStatement(update);
						statement.setString(1, p.getUniqueId().toString());
						statement.execute();
						
						player = new UHCPlayer(p.getUniqueId(), 0, 0, 0, 0);
						main.getOnlinePlayers().put(p.getUniqueId(), player);
					} catch (SQLException e) {
						main.sendConsoleMessage(ChatColor.DARK_RED + "Encountered an error while creating row for new player.");
						e.printStackTrace();
					}
				}else {
					try {
						player = new UHCPlayer(p.getUniqueId(), rs.getInt("GAMES_PLAYED"), rs.getInt("GAMES_WON"), rs.getInt("KILLS"),
							rs.getInt("DEATHS"));
						main.getOnlinePlayers().put(p.getUniqueId(), player);
					} catch (SQLException e) {
						main.sendConsoleMessage(ChatColor.DARK_RED + "Encountered an error while obtaining row for player.");
						e.printStackTrace();
					}
				}
			}
			
		}.runTaskAsynchronously(main);
		
		new BukkitRunnable() {
			
			public void run() {
				UHCPlayer player = main.getOnlinePlayers().get(p.getUniqueId());
				
				// Let's assign them a team if the game hasn't started yet.
				GameState state = main.getGame().getState();
				if(state == GameState.WAITING || state == GameState.STARTING) {
					if(main.getGame().isTeamMatch()) {
						ArrayList<Team> availableTeams = new ArrayList<Team>();
						for(Team t : UHCSystem.getTeams()) {
							if(t.getMembers().size() < Globals.MAX_MEMBERS_PER_TEAM) {
								availableTeams.add(t);
							}
						}
						
						Team t = availableTeams.get(ThreadLocalRandom.current().nextInt(0, availableTeams.size()));
						player.setTeam(t);
					}
					
					if(state == GameState.WAITING && UHCSystem.canStartGame()) {
						main.getGame().setState(GameState.STARTING);
					}
				}
				
				UHCSystem.handleLobbyArrival(main, player);
			}
			
		}.runTaskLater(main, 30L);
	}
	
	@EventHandler
	public void onPlayerQuit(final PlayerQuitEvent evt) {
		UHCPlayer player = main.getOnlinePlayers().get(evt.getPlayer().getUniqueId());
		main.getOnlinePlayers().remove(player);
		
		UHCGame game = main.getGame();
		if(!UHCSystem.canStartGame() && game.getState() == GameState.STARTING) {
			game.cancelStart();
		}

		new BukkitRunnable() {
			
			public void run() {
				database.handlePlayerExit(evt.getPlayer().getUniqueId());
			}
			
		}.runTaskAsynchronously(main);
	}
}
