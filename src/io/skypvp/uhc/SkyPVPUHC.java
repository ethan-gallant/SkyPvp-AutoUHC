package io.skypvp.uhc;

import io.skypvp.uhc.arena.ArenaEventsListener;
import io.skypvp.uhc.arena.Profile;
import io.skypvp.uhc.arena.UHCGame;
import io.skypvp.uhc.arena.state.GameStateManager;
import io.skypvp.uhc.command.CommandPool;
import io.skypvp.uhc.event.TrafficEventsListener;
import io.skypvp.uhc.player.ArenaPlayerEventsListener;
import io.skypvp.uhc.player.UHCPlayer;
import io.skypvp.uhc.util.ConfigUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.Vault;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.wimbli.WorldBorder.WorldBorder;

public class SkyPVPUHC extends JavaPlugin {
	
	private Settings settings;
	private Profile profile;
	private Messages msgs;
	private WorldHandler worldHdl;
	private CommandPool cmdPool;
	private GameStateManager gsm;
	
	public static UHCGame game;
	public static HashMap<UUID, UHCPlayer> onlinePlayers;
	
	private boolean canContinue;
	
	// Dependencies
    private WorldBorder worldBorder;
    private Economy economy;
	
	static {
	    onlinePlayers = new HashMap<UUID, UHCPlayer>();
	}
	
	public void onLoad() {
	    ConfigUtils.main = this;
	    canContinue = true;
	    
	    // Let's instantiate a Settings, Profile, and Messages object.
	    settings = new Settings(this);
	    profile = new Profile(this);
	    msgs = new Messages(this);
        gsm = new GameStateManager(this);
	    
        settings.load();
	}
	
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
		JavaPlugin vault = getRequiredDependency("Vault", Vault.class);
		
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
		}
		
		if(isEnabled()) {
            // We're register our events.
            getServer().getPluginManager().registerEvents(new ArenaEventsListener(this, game), this);
            getServer().getPluginManager().registerEvents(new TrafficEventsListener(this), this);
            getServer().getPluginManager().registerEvents(new ArenaPlayerEventsListener(this), this);
            getServer().getPluginManager().registerEvents(cmdPool, this);
		}
	}
	
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
	
	public void databaseConnected() {
		if(canContinue) {
	        game = new UHCGame(this);
	        
            // Let's shake hands with Jedis.
            settings.getJedis().handshake();
		}
	}
	
	public void onDisable() {
		// In the case of a plugin unload, let's save player stats.
		if(onlinePlayers.size() > 0) {
			Iterator<UUID> iterator = onlinePlayers.keySet().iterator();
			while(iterator.hasNext()) {
				UUID id = iterator.next();
				settings.getDatabase().handlePlayerExit(id);
			}
		}
		
		if(settings != null && settings.getDatabase() != null) {
			try {
				settings.getDatabase().close();
				sendConsoleMessage(ChatColor.DARK_GREEN + "Successfully closed connection to MySQL database.");
			} catch (NullPointerException e) {
				sendConsoleMessage(ChatColor.DARK_RED + "Encountered an error while closing connection to MySQL database.");
				e.printStackTrace();
			}
		}
		
		if(settings != null && settings.getJedis() != null) {
		    settings.getJedis().farewell();
		}
	}
	
	/**
	 * Disables the plugin from being enabled or disables the plugin
	 * if it's enabled.
	 */
	
	public void disable() {
		sendConsoleMessage(ChatColor.DARK_RED + "Disabling...");
		
		if(isEnabled()) {
		    setEnabled(false);
		}
		
		canContinue = false;
	}
	
	public Settings getSettings() {
		return this.settings;
	}
	
	public Profile getProfile() {
		return this.profile;
	}
	
	public Messages getMessages() {
		return this.msgs;
	}
	
	public CommandPool getCommandPool() {
	    return this.cmdPool;
	}
	
	public WorldBorder getWorldBorder() {
		return this.worldBorder;
	}
	
	public WorldHandler getWorldHandler() {
	    return this.worldHdl;
	}
	
	public GameStateManager getGameStateManager() {
	    return this.gsm;
	}
	
	public Economy getEconomy() {
	    return this.economy;
	}
	
	public UHCGame getGame() {
		return SkyPVPUHC.game;
	}
	
	public HashMap<UUID, UHCPlayer> getOnlinePlayers() {
		return SkyPVPUHC.onlinePlayers;
	}
	
	public void sendConsoleMessage(String msg) {
		String pluginName = getDescription().getName();
		String prefix = ChatColor.DARK_GRAY +  "[" + ChatColor.GOLD + pluginName + ChatColor.DARK_GRAY + "]";
		getServer().getConsoleSender().sendMessage(prefix + " " + msg);
	}
	
}
