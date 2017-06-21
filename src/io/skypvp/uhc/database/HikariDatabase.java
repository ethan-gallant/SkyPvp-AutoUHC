package io.skypvp.uhc.database;

import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;
import java.util.UUID;

import net.md_5.bungee.api.ChatColor;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.zaxxer.hikari.HikariDataSource;

import io.skypvp.uhc.Globals;
import io.skypvp.uhc.SkyPVPUHC;
import io.skypvp.uhc.player.UHCPlayer;

public class HikariDatabase {

	final SkyPVPUHC instance;
	final String username;
	final String password;
	final String hostName;
	final String port;
	final String database;

	private HikariDataSource hikari;

	public HikariDatabase(SkyPVPUHC main, String username, String password, String hostName, String port, String database) {
		this.instance = main;
		this.username = username;
		this.password = password;
		this.hostName = hostName;
		this.port = port;
		this.database = database;
		this.hikari = null;
	}

	/**
	 * Attempts to instantiate a connection pool.
	 * After an attempt is made, this method will verify that a connection has been made.
	 * Console is alerted if connection pool is established or not.
	 */

	public void connect() {
		instance.sendConsoleMessage(ChatColor.YELLOW + String.format("Attempting to create a connection pool to: %s:%s...",
				hostName, port));

		// Let's create our HikariDataSource
		hikari = new HikariDataSource();
		hikari.setMaximumPoolSize(5);
		hikari.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
		hikari.addDataSourceProperty("serverName", hostName);
		hikari.addDataSourceProperty("port", port);
		hikari.addDataSourceProperty("databaseName", database);
		hikari.addDataSourceProperty("user", username);
		hikari.addDataSourceProperty("password", password);
		hikari.setThreadFactory(new ThreadFactoryBuilder().setDaemon(true).setNameFormat("hikari-sql-pool-%d").build());

		// Let's verify that we're connected.
		try {
			Connection conn = hikari.getConnection();
			instance.sendConsoleMessage(ChatColor.GREEN + String.format("Successfully instantiated connection pool to %s:%s!",
					hostName, port));
			instance.databaseConnected();
			conn.close();
		} catch (SQLException e) {
			instance.sendConsoleMessage(ChatColor.RED + String.format("Failed to instantiate connection pool to: %s:%s."
					+ "SQLException: %s.", hostName, port, e.getMessage()));
		}
	}

	/**
	 * Verifies that the UHC database table exists.
	 * If the table does not exist, this method will attempt to create it.
	 */

	public void verifyTableExists() {
		Connection conn = obtainConnection();
		DatabaseMetaData md = null;

		if(conn != null) {
			try {
				md = conn.getMetaData();
				ResultSet rs = md.getTables(null, null, Globals.TABLE_NAME, null);
				boolean found = false;

				while(rs.next()) {
					found = rs.getString(3).equalsIgnoreCase(Globals.TABLE_NAME);
					break;
				}

				if(!found) {
					// Table was not found from query, let's create it.
					instance.sendConsoleMessage(ChatColor.YELLOW + "Attempting to generate UHC database table...");
					PreparedStatement ps = conn.prepareStatement(String.format("CREATE TABLE IF NOT EXISTS %s(UUID varchar(36), "
							+ "GAMES_PLAYED int, GAMES_WON int, KILLS int, DEATHS int, PRIMARY KEY (UUID));", 
							Globals.TABLE_NAME));
					ps.executeUpdate();
					instance.sendConsoleMessage(ChatColor.GREEN + "Generated UHC database table!");
				}

			} catch (SQLException e) {
				instance.sendConsoleMessage(ChatColor.RED + String.format("Encountered an error while verifying "
						+ "the integrity of the UHC database table. Error: %s.", e.getMessage()));
				instance.disable();
			} finally {
				closeConnection(conn);
			}
		}
	}

	public void handlePlayerExit(UUID id) {
		UHCPlayer player = instance.getOnlinePlayers().get(id);
		Connection conn = obtainConnection();

		if(player != null && conn != null) {
			try {
				PreparedStatement ps = conn.prepareStatement(player.toMySQLUpdate());
				ps.executeUpdate();
				instance.getOnlinePlayers().remove(id);
			} catch (SQLException e) {
				instance.sendConsoleMessage(ChatColor.RED + String.format("SEVERE!! Encountered an error while saving player stats!"
						+ "Error: %s.", e.getMessage()));
			} finally {
				closeConnection(conn);
			}
		}
	}

	/**
	 * Executes a query using a {@link java.sql.PreparedStatement}
	 * @param String query - The query to execute.
	 * @return {@link DatabaseQuery} containing the connection and the ResultSet.
	 */

	public DatabaseQuery query(String query) {
		Connection conn = obtainConnection();

		if(conn != null && query != null) {
			PreparedStatement statement = null;
			ResultSet rs = null;
			try {
				statement = conn.prepareStatement(query);
				rs = statement.executeQuery();
				return new DatabaseQuery(conn, rs);
			} catch (SQLException e) {
				instance.sendConsoleMessage(ChatColor.RED + String.format("Failed to execute MySQL query. Error: %s.", 
						e.getMessage()));
			}
		}

		return null;
	}

	/**
	 * Executes a specified {@link java.sql.PreparedStatement}.
	 * @param PreparedStatement to execute.
	 */

	public void executeUpdate(PreparedStatement st) {
		Connection conn = obtainConnection();

		if(conn != null) {
			try {
				st.execute();
			} catch (SQLException e) {
				instance.sendConsoleMessage(ChatColor.RED + String.format("Failed to execute PreparedStatement. Error: %s.",
						e.getMessage()));
			} finally {
				closeConnection(conn);
			}
		}
	}

	/**
	 * Obtains a connection from the connection pool.
	 * Alerts console if anything goes wrong while obtaining said {@link java.sql.Connection}
	 * @return Connection or null if an error occurs.
	 */

	public Connection obtainConnection() {
		Connection conn = null;
		try {
			conn = hikari.getConnection();
			return conn;
		} catch (SQLException e) {
			instance.sendConsoleMessage(ChatColor.RED + "Failed to obtain a connection from the Hikari connection pool.");
		}

		return conn;
	}

	/**
	 * Takes a {@link java.sql.Connection} passed to it and closes it
	 * while alerting the console if anything goes wrong.
	 * This should be used inside of a finally block of a try-catch.
	 * @param Connection conn - The connection to close
	 */

	public void closeConnection(Connection conn) {
		try {
			conn.close();
		} catch (SQLException | NullPointerException e) {
			instance.sendConsoleMessage(ChatColor.RED + String.format("Failed to close connection from connection pool. Error: %s",
					e.getMessage()));
		}
	}

	/**
	 * Closes the Hikari Connection Pool if it's open.
	 */

	public void close() {
		if(hikari != null) {
			hikari.close();
			instance.sendConsoleMessage(ChatColor.GREEN + "Successfully closed Hikari Connection Pool!");
		}else {
			instance.sendConsoleMessage(ChatColor.RED + "The Hikari Connection Pool is not open, so it cannot be closed. :(");
		}
	}

	/**
	 * Fetches the host name.
	 * @return String hostName
	 */

	public String getHostName() {
		return this.hostName;
	}

	/**
	 * Fetches the port.
	 * @return String port
	 */

	public String getPort() {
		return this.port;
	}

	/**
	 * Fetches the username.
	 * @return String username
	 */

	public String getUsername() {
		return this.username;
	}

	/**
	 * Fetches the password.
	 * @return String password
	 */

	public String getPassword() {
		return this.password;
	}

	/**
	 * Fetches the database.
	 * @return String database
	 */

	public String getDatabase() {
		return this.database;
	}

}
