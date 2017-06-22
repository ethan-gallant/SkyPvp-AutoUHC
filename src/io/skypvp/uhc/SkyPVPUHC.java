package io.skypvp.uhc;

import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.wimbli.WorldBorder.WorldBorder;

import io.skypvp.uhc.arena.ArenaEventsListener;
import io.skypvp.uhc.arena.Profile;
import io.skypvp.uhc.arena.UHCGame;
import io.skypvp.uhc.arena.state.GameStateManager;
import io.skypvp.uhc.command.CommandPool;
import io.skypvp.uhc.database.HikariDatabase;
import io.skypvp.uhc.event.TrafficEventsListener;
import io.skypvp.uhc.jedis.UHCJedis;
import io.skypvp.uhc.player.ArenaPlayerEventsListener;
import io.skypvp.uhc.player.UHCPlayer;
import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;

public class SkyPVPUHC extends JavaPlugin {

    private static SkyPVPUHC instance;
	private Settings settings;
	private Profile profile;
	private Messages msgs;
	private WorldHandler worldHdl;
	private CommandPool cmdPool;
	private GameStateManager gsm;

	public UHCGame game;
	public HashMap<UUID, UHCPlayer> onlinePlayers;

	private boolean canContinue;

	// Dependencies
	private WorldBorder worldBorder;
	private Economy economy;
	
	/**
	 * NOTE: Because the plugin.yml has "LOAD" set to "STARTUP",
	 * the method is called BEFORE worlds are loaded.
	 * Instantiates a new instance of four game services:
	 * - {@link Settings},
	 * - {@link Profile},
	 * - {@link Messages},
	 * - {@link GameStateManager}.
	 * 
	 * Next, it calls the #load() method of the Settings class.
	 */

	public void onLoad() {
		SkyPVPUHC.instance = this;
		onlinePlayers = new HashMap<UUID, UHCPlayer>();
		canContinue = true;

		// Let's instantiate a Settings, Profile, and Messages object.
		settings = new Settings(this);
		profile = new Profile(this);
		msgs = new Messages(this);
		gsm = new GameStateManager(this);

		settings.load();
	}
	
	/**
	 * Called when the plugin is finally enabled.
	 * Runs verifications to make sure that we have a lobby world created,
	 * the required dependencies, and an economy plugin installed.
	 * Registers an outgoing plugin channel for server-to-server teleportation; and
	 * registers event listeners.
	 */

	public void onEnable() {
		if(!canContinue) {
			setEnabled(false);
			return;
		}

		// We're going to require the lobby world to be called a certain name.
		if(getServer().getWorld(Globals.LOBBY_WORLD_NAME) == null) {
			sendConsoleMessage(ChatColor.DARK_RED + 
					String.format("Your main UHC lobby world must be called '%s' in order for this plugin to work correctly.", 
							Globals.LOBBY_WORLD_NAME));
			disable();
			return;
		}

		cmdPool = new CommandPool(this);
		worldHdl = new WorldHandler(this);

		// We need to be able to send messages to BungeeCord.
		getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

		worldBorder = (WorldBorder) getRequiredDependency("WorldBorder", WorldBorder.class);

		// Let's make sure we have Multiverse-Core.
		getRequiredDependency("Multiverse-Core", MultiverseCore.class);

		// This makes sure we have Vault.
		JavaPlugin vault = (JavaPlugin) getServer().getPluginManager().getPlugin("Vault");

		// Let's setup the economy.
		if(vault != null) {
			RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);

			if(rsp != null) {
				economy = rsp.getProvider();
				sendConsoleMessage(ChatColor.GREEN + String.format("Hooked into %s!", economy.getName()));
			}else {
				sendConsoleMessage(ChatColor.RED + "You must have an economy plugin installed to use this plugin!");
				disable();
			}
		}else {       
		    sendConsoleMessage(ChatColor.DARK_RED + "Vault could not be found. You must install it to use this plugin.");
		    setEnabled(false);
		}

		if(isEnabled()) {
			// We're register our events.
			getServer().getPluginManager().registerEvents(new ArenaEventsListener(this, game), this);
			getServer().getPluginManager().registerEvents(new TrafficEventsListener(this), this);
			getServer().getPluginManager().registerEvents(new ArenaPlayerEventsListener(this), this);
			getServer().getPluginManager().registerEvents(cmdPool, this);
		}
	}
	
	/**
	 * Fetches a {@link JavaPlugin} dependency by name and if it matches the
	 * provided class that represents the main class of the needed plugin dependency.
	 * If the dependency cannot be found, the plugin is disabled.
	 * 
	 * @param String name - The name of the dependency.
	 * @param Class<? extends JavaPlugin> returnType - The main class of the needed plugin dependency.
	 * @return {@link JavaPlugin} or null if the plugin cannot be found.
	 */

	public JavaPlugin getRequiredDependency(final String name, final Class<? extends JavaPlugin> returnType) {
		final Plugin plugin = getServer().getPluginManager().getPlugin(name);

		if(plugin != null && returnType.isInstance(plugin)) {
			sendConsoleMessage(ChatColor.DARK_GREEN + String.format("Successfully hooked into %s [%s]!", 
					plugin.getDescription().getName(), 
					plugin.getDescription().getVersion()));

			return returnType.cast(plugin);
		}

		sendConsoleMessage(ChatColor.DARK_RED + String.format("%s could not be found. You must install it to use this plugin.", name));
		setEnabled(false);
		return null;
	}
	
	/**
	 * Called when {@link HikariDatabase}'s {@link HikariDatabase.#connect()} is successful.
	 * Creates a new instance of the {@link UHCGame} class and acknowledges redis.
	 * Calls #handshake() method inside of {@link UHCJedis}
	 */

	public void databaseConnected() {
		if(canContinue) {
			game = new UHCGame(this);

			// Let's shake hands with Jedis.
			settings.getJedis().handshake();
		}
	}
	
	/**
	 * Called when the plugin is disabled.
	 * Saves player stats, closes Hikari connection pool,
	 * and calls #farewell() inside of {@link UHCJedis}
	 */

	public void onDisable() {
	    // If an instance of the Settings class has been created,
	    // let's save player stats, close the connection pool,
	    // and let redis know that this server is going offline.
	    if(settings != null) {
	        HikariDatabase db = settings.getDatabase();
	        
	        if(db != null) {
	            // If there are players online, let's save their stats.
        		if(onlinePlayers.size() > 0) {
        			Iterator<UUID> iterator = onlinePlayers.keySet().iterator();
        			while(iterator.hasNext()) {
        				UUID id = iterator.next();
        				db.handlePlayerExit(id);
        			}
        		}
        		
        		// Let's close the connection pool.
                try {
                    db.close();
                    sendConsoleMessage(ChatColor.DARK_GREEN + "Successfully closed connection to MySQL database.");
                } catch (NullPointerException e) {
                    sendConsoleMessage(ChatColor.DARK_RED + "Encountered an error while closing connection to MySQL database.");
                    e.printStackTrace();
                }
	        }
	        
	        // Let's let redis know that this server is going offline.
	        if(settings.getJedis() != null) {
	            settings.getJedis().farewell();
	        }
		}
	}

	/**
	 * Stops the plugin from being enabled or disables the plugin
	 * if it's enabled.
	 */

	public void disable() {
		sendConsoleMessage(ChatColor.DARK_RED + "Disabling...");

		if(isEnabled()) {
			setEnabled(false);
		}

		canContinue = false;
	}
	
    /////////////////////////////////////////////////////////
    //            Methods for obtaining services           //
    /////////////////////////////////////////////////////////
	
	/**
	 * Fetches our instance of the {@link Settings} class.
	 * @return {@link Settings} or null.
	 */
	
	public Settings getSettings() {
		return this.settings;
	}
	
    /**
     * Fetches our instance of the {@link Messages} class.
     * @return {@link Messages} or null.
     */

    public Messages getMessages() {
        return this.msgs;
    }
    
    /**
     * Fetches our instance of the {@link UHCGame} class.
     * @return {@link UHCGame} or null.
     */
    
    public UHCGame getGame() {
        return this.game;
    }
	
	/**
	 * Fetches our instance of the {@link Profile} class.
	 * @return {@link Profile} or null.
	 */

	public Profile getProfile() {
		return this.profile;
	}
	
	/**
	 * Fetches our instance of the {@link GameStateManager} class.
	 * This class is responsible for keeping track of game states.
	 * @return {@link GameStateManager} or null.
	 */
	
    public GameStateManager getGameStateManager() {
        return this.gsm;
    }
	
	/**
	 * Fetches our instance of the {@link WorldHandler} class.
	 * This class is responsible for creating our UHC world.
	 * @return {@link WorldHandler} or null.
	 */
	
    public WorldHandler getWorldHandler() {
        return this.worldHdl;
    }
	
	/**
	 * Fetches our instance of the {@link CommandPool} class.
	 * This class keeps track of our UHC commands.
	 * @return {@link CommandPool} or null.
	 */

	public CommandPool getCommandPool() {
		return this.cmdPool;
	}
	
	/**
	 * Fetches our hook into the {@link WorldBorder} plugin.
	 * NOTE: If this returns null, the plugin most likely will be disabled
	 * shortly thereafter as the {@link WorldHandler} requires WorldBorder.
	 * @return {@link WorldBorder} hook or null.
	 */

	public WorldBorder getWorldBorder() {
		return this.worldBorder;
	}
	
	/**
	 * Fetches our hook into the server's {@link Economy} plugin.
	 * This is obtained from the Vault plugin.
	 * NOTE: If this returns null, the plugin will most likely be
	 * disabled shortly thereafter as the points system requires both
	 * Vault and an economy plugin to function.
	 * @return {@link Economy} hook or null.
	 */

	public Economy getEconomy() {
		return this.economy;
	}
	
    /////////////////////////////////////////////////////////
	
	/**
	 * Fetches a {@link HashMap} of {@link UUID}-{@link UHCPlayer} pairs.
	 * @return HashMap<UUID, UHCPlayer>
	 */

	public HashMap<UUID, UHCPlayer> getOnlinePlayers() {
		return this.onlinePlayers;
	}
	
	/**
	 * Sends a colored and prefixed message to console.
	 * @param String msg - The message to send to console.
	 * Accepts {@link ChatColor}
	 */

	public void sendConsoleMessage(String msg) {
		String pluginName = getDescription().getName();
		String prefix = ChatColor.DARK_GRAY +  "[" + ChatColor.GOLD + pluginName + ChatColor.DARK_GRAY + "]";
		getServer().getConsoleSender().sendMessage(prefix + " " + msg);
	}
	
	/**
	 * Fetches the "working" instance of the main class of the UHC plugin.
	 * @return {@link SkyPVPUHC} or null if the plugin hasn't been loaded yet.
	 */
	
	public static SkyPVPUHC get() {
	    return SkyPVPUHC.instance;
	}

}
