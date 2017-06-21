package io.skypvp.uhc.event;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import io.skypvp.uhc.Globals;
import io.skypvp.uhc.SkyPVPUHC;
import io.skypvp.uhc.UHCScoreboard;
import io.skypvp.uhc.UHCSystem;
import io.skypvp.uhc.arena.Profile;
import io.skypvp.uhc.database.DatabaseQuery;
import io.skypvp.uhc.database.HikariDatabase;
import io.skypvp.uhc.player.UHCPlayer;
import net.md_5.bungee.api.ChatColor;

public class TrafficEventsListener implements Listener {

    final SkyPVPUHC main;
    final HikariDatabase database;

    public TrafficEventsListener(SkyPVPUHC instance) {
        this.main = instance;
        this.database = main.getSettings().getDatabase();
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent evt) {
        ItemStack item = evt.getItemDrop().getItemStack();
        evt.setCancelled(UHCSystem.isRestrictedItem(item));
    }

    /**
     * Allow players to join if the profile max players is greater than the server max.
     * @param PlayerLoginEvent evt
     */

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent evt) {
        Profile profile = main.getProfile();
        if(evt.getResult() == Result.KICK_FULL && main.getServer().getOnlinePlayers().size() < profile.getMaxPlayers()) {
            evt.allow();
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent evt) {
        final Player p = evt.getPlayer();

        new BukkitRunnable() {

            public void run() {
                DatabaseQuery query = database.query(String.format("SELECT * FROM %s WHERE UUID='%s';", Globals.TABLE_NAME, p.getUniqueId())); 
                ResultSet rs = query.getResultSet();
                UHCPlayer player;

                // Let's close the connection that query used.
                database.closeConnection(query.getConnection());

                // We need to handle creating new profiles and obtaining them.
                if(rs == null) {
                    try {
                        String update = String.format(Globals.NEW_TABLE_ENTRY, Globals.TABLE_NAME);
                        Connection conn = database.obtainConnection();

                        if(conn != null) {
                            PreparedStatement statement = conn.prepareStatement(update);
                            statement.setString(1, p.getUniqueId().toString());
                            statement.executeUpdate();

                            player = new UHCPlayer(p.getUniqueId(), 0, 0, 0, 0);
                            main.getOnlinePlayers().put(p.getUniqueId(), player);
                            database.closeConnection(conn);
                        }
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

                // Let's let the current state know that a new player has arrived.
                main.getGameStateManager().getActiveState().onPlayerJoin(player);

                player.prepareForGame();
                UHCSystem.handleLobbyArrival(main, player);
            }

        }.runTaskLater(main, 1L);
    }

    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent evt) {
        UHCPlayer player = main.getOnlinePlayers().get(evt.getPlayer().getUniqueId());
        if(player != null) {
            main.getOnlinePlayers().remove(player);

            for(UHCPlayer uhcPlayer : main.getOnlinePlayers().values()) {
                UHCScoreboard board = uhcPlayer.getScoreboard();
                if(board != null) {
                    board.generate(uhcPlayer);
                    uhcPlayer.setScoreboard(board);
                }
            }

            World world = Bukkit.getWorld(Globals.LOBBY_WORLD_NAME);
            player.getBukkitPlayer().teleport(world.getSpawnLocation());
            main.getSettings().getJedis().announceServerDetails();

            new BukkitRunnable() {

                public void run() {
                    database.handlePlayerExit(evt.getPlayer().getUniqueId());
                }

            }.runTaskAsynchronously(main);
        }
    }

}
