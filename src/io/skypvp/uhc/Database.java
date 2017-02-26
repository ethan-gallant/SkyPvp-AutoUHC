package io.skypvp.uhc;

import io.skypvp.uhc.player.UHCPlayer;

import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;
import java.util.UUID;

import org.bukkit.scheduler.BukkitRunnable;

import net.md_5.bungee.api.ChatColor;

public class Database {
	
	final SkyPVPUHC main;
	private final String host;
	private final String port;
	private final String username;
	private final String password;
	private final String database;
	private Connection conn;
	
	public Database(SkyPVPUHC instance, String host, String port, String username, String password, String database) {
		this.main = instance;
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
		this.database = database;
		this.conn = null;
	}
	
	public void openConnection() throws SQLException {
		if(conn != null && !conn.isClosed()) return;
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(String.format("jdbc:mysql://%s:%s/%s",
					host, port, database), username, password);
			new BukkitRunnable() {
				public void run() {
					main.databaseConnected();
				}
			}.runTask(main);
			main.sendConsoleMessage(ChatColor.DARK_GREEN + "Successfully connected to MySQL database!");
		} catch (ClassNotFoundException e) {
			main.getLogger().severe("Could not find JDBC MySQL driver, please install it to use this plugin.");
			main.disable();
		}
	}
	
	public void handlePlayerExit(final UUID id) {
		final UHCPlayer player = main.getOnlinePlayers().get(id);
		
		if(player != null) {
			Statement statement;
			try {
				statement = main.getSettings().getDatabase().getConnection().createStatement();
				statement.execute(player.toMySQLUpdate());
				main.getOnlinePlayers().remove(id);
			} catch (SQLException e) {
				main.sendConsoleMessage(ChatColor.DARK_RED + "SEVERE!! Encountered an error while saving player stats...");
				e.printStackTrace();
			}
		}
	}
	
	public ResultSet query(String query) {
		ResultSet rs = null;
		
		try {
			if(conn != null && !conn.isClosed()) {
				Statement statement = conn.createStatement();
				rs = statement.executeQuery(query);
				if(!rs.next()) rs = null;
			}
		}catch (SQLException e) {
			main.sendConsoleMessage(ChatColor.DARK_RED + "Encountered an error while attempting to execute MySQL query.");
			e.printStackTrace();
		}
		
		return rs;
	}
	
	public boolean tableExists() {
		DatabaseMetaData md;
		try {
			md = conn.getMetaData();
			ResultSet rs = md.getTables(null, null, Globals.TABLE_NAME, null);
			boolean found = false;
			while (rs.next()) {
				found = rs.getString(3).equalsIgnoreCase(Globals.TABLE_NAME);
				break;
			}
			return found;
		} catch (SQLException e) {
			main.sendConsoleMessage(ChatColor.DARK_RED + "Encountered an error while checking status of table.");
			e.printStackTrace();
			main.disable();
		}
		
		return false;
	}
	
	public void createTable() {
		try {
			Statement statement = conn.createStatement();
			statement.executeUpdate(String.format("CREATE TABLE IF NOT EXISTS %s(UUID varchar(36), GAMES_PLAYED int,"
					+ "GAMES_WON int, KILLS int, DEATHS int, PRIMARY KEY (UUID));", Globals.TABLE_NAME));
			main.sendConsoleMessage(ChatColor.DARK_GREEN + "Successfully created database table.");
		} catch (SQLException | NullPointerException e) {
			main.getLogger().severe(String.format("Encountered an error while trying to create table. Exception: %s",
				e.getMessage()));
			main.sendConsoleMessage(ChatColor.DARK_RED + "Ignore the following stack trace if NullPointerException OR table already created. Restart plugin.");
			e.printStackTrace();
			main.disable();
		}
	}
	
	public String getHost() {
		return this.host;
	}
	
	public String getPort() {
		return this.port;
	}
	
	public String getUsername() {
		return this.username;
	}
	
	public String getPassword() {
		return this.password;
	}
	
	public String getDatabase() {
		return this.database;
	}
	
	public Connection getConnection() {
		return this.conn;
	}
}
