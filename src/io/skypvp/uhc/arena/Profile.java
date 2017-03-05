package io.skypvp.uhc.arena;

import io.skypvp.uhc.SkyPVPUHC;
import io.skypvp.uhc.scenario.ScenarioType;
import io.skypvp.uhc.util.ConfigUtils;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class Profile {
	
	final SkyPVPUHC main;
	private File profileFile;
	private YamlConfiguration profile;
	
	// Basic settings from the profile.
	private int initMapSize;
	private int maxPlayers;
	private String owner;
	private Date expiration;
	
	// Timings from the profile. (in seconds)
	private int gracePeriodLength;
	private int healPlayersAfterTime;
	private int beginBorderShrink;
	
	// The scenarios that are active
	private ArrayList<ScenarioType> scenarios;
	
	public Profile(SkyPVPUHC instance) {
		this.main = instance;
		this.profileFile = null;
		this.profile = null;
		
		// Initialize our profile variables.
		this.initMapSize = 0;
		this.maxPlayers = 0;
		this.owner = null;
		this.expiration = null;
		this.gracePeriodLength = 0;
		this.healPlayersAfterTime = 0;
		this.beginBorderShrink = 0;
		this.scenarios = new ArrayList<ScenarioType>();
		
		read("profile");
	}
	
	/**
	 * Sets the initial border size of the map.
	 * Expected params (250, 500, 750, 1000)
	 * @param int mapSize
	 */
	
	public void setInitialMapSize(int mapSize) {
		this.initMapSize = mapSize;
	}
	
	/**
	 * Fetches the initial map size.
	 * @return int
	 */
	
	public int getInitialMapSize() {
		return this.initMapSize;
	}
	
	/**
	 * Sets the max amount of players.
	 * @param int maxPlayers
	 */
	
	public void setMaxPlayers(int maxPlayers) {
		this.maxPlayers = maxPlayers;
	}
	
	/**
	 * Fetches the maximum amount of players.
	 * @return int
	 */
	
	public int getMaxPlayers() {
		return this.maxPlayers;
	}
	
	/**
	 * Sets the owner of this profile.
	 * @param String owner
	 */
	
	public void setOwner(String owner) {
		this.owner = owner;
	}
	
	/**
	 * Fetches the owner of this profile or null if default.
	 * @return String or null
	 */
	
	public String getOwner() {
		return this.owner;
	}
	
	/**
	 * Sets when the profile will expire.
	 * @param Date date
	 */
	
	public void setExpiration(Date date) {
		this.expiration = date;
	}
	
	/**
	 * Fetches when this profile will expire.
	 * @return Date or null
	 */
	
	public Date getExpiration() {
		return this.expiration;
	}
	
	/**
	 * All timings below are in minutes.
	 */
	
	/**
	 * Sets the length of the Grace Period (in seconds)
	 * @param int periodLength
	 */
	
	public void setGracePeriodLength(int periodLength) {
		this.gracePeriodLength = periodLength;
	}
	
	/**
	 * Fetches the length of the Grace Period (in seconds)
	 * @return int
	 */
	
	public int getGracePeriodLength() {
		return this.gracePeriodLength;
	}
	
	/**
	 * Sets how long until all players are healed (in seconds)
	 * Set this to -1 for no heal.
	 * @param int healTime
	 */
	
	public void setHealPlayersTime(int healTime) {
		this.healPlayersAfterTime = healTime;
	}
	
	/**
	 * Fetches how long until all players are healed. (in seconds)
	 * @return int
	 */
	
	public int getHealPlayersTime() {
		return this.healPlayersAfterTime;
	}
	
	/**
	 * Sets how long until the border begins to shrink. (in seconds)
	 * @param int shrinkTime
	 */
	
	public void setBeginBorderShrinkTime(int shrinkTime) {
		this.beginBorderShrink = shrinkTime;
	}
	
	/**
	 * Fetches how long until the border will begin to shrink. (in seconds)
	 * @return int
	 */
	
	public int getBeginBorderShrinkTime() {
		return this.beginBorderShrink;
	}
	
	/**
	 * Fetches the Scenarios selected for this profile.
	 * @return ArrayList<ScenarioType>
	 */
	
	public ArrayList<ScenarioType> getScenarios() {
		return this.scenarios;
	}
	
	/**
	 * Loads a profile file into the reader.
	 * Input raw file name of profile file within plugin directory.
	 * Omits file extensions if accidentally added in.
	 * @param String profileFilename
	 * @throws IllegalArgumentException if profile does not exist within plugin directory.
	 */
	
	public void read(String profileFilename) throws IllegalArgumentException {
		if(profileFilename.contains(".yml")) {
			profileFilename = profileFilename.substring(0, profileFilename.indexOf("."));
		}
		
		profileFile = new File(main.getDataFolder() + String.format("/%s.yml", profileFilename));
		
		if(!profileFile.exists()) {
			if(profileFilename.equalsIgnoreCase("profile")) {
				reset();
			}else {
				main.sendConsoleMessage(main.getMessages().color(
					String.format("&cERROR: &4Could not load Profile %s.yml", 
				profileFilename)));
				
				// Let's disable the plugin.
				main.disable();
				
				throw new IllegalArgumentException(String.format("Profile name %s does not exist.", profileFilename));
			}
		}
		
		profile = YamlConfiguration.loadConfiguration(profileFile);
		load();
	}
	
	public void save() throws NullPointerException, IOException {
		if(profileFile == null || profile == null) {
			throw new NullPointerException("A profile has not been loaded yet!");
		}
		
		// Let's make a list of the scenario keys.
		ArrayList<String> scenarioKeys = new ArrayList<String>();
		
		for(ScenarioType type : scenarios) {
			scenarioKeys.add(type.getConfigKey());
		}
		
		// Save the scenarios.
		profile.set("scenarios", scenarioKeys);
		
		// Save profile settings.
		profile.set("initMapSize", initMapSize);
		profile.set("maxPlayers", maxPlayers);
		profile.set("profileOwner", (owner == null) ? "null" : owner);
		
		// Save the date expiration.
		SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM d HH:mm:ss zzz yyyy");
		if(expiration != null) {
			profile.set("profileExpiration", sdf.format(expiration));
		}else {
			profile.set("profileExpiration", "null");
		}
		
		// Let's save the timings.
		ConfigurationSection timings = profile.getConfigurationSection("timings");
		timings.set("gracePeriodLength", gracePeriodLength);
		timings.set("healPlayersAfter", healPlayersAfterTime);
		timings.set("beginBorderShrink", beginBorderShrink);
		profile.set("timings", timings);
		
		// Let's save the profile.
		try {
			ConfigUtils.save(profileFile, profile);
		} catch (IOException e) {
			main.sendConsoleMessage(main.getMessages().color(
				String.format("&cERROR: &4Failed to save profile! Error: %s", 
			e.getMessage())));
		}
	}
	
	/**
	 * Loads the profile currently in the reader.
	 * @throws NullPointerException if a profile is not in the reader.
	 * The "reader" is when both the `profileFile` and `profile` variables are set.
	 * Those variables are only set after the #read(String profile) method has been successfully called.
	 */
	
	public void load() throws NullPointerException {
		if(profileFile == null || profile == null) {
			throw new NullPointerException("A profile has not been read yet!");
		}
		
		scenarios.clear();
		initMapSize = profile.getInt("initMapSize");
		maxPlayers = profile.getInt("maxPlayers");
		owner = profile.getString("profileOwner");
		expiration = null;
		
		// Let's load our expiration date.
		String dateStr = profile.getString("profileExpiration");
		SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM d HH:mm:ss zzz yyyy");
		
		if(dateStr != null) {
			try {
				expiration = sdf.parse(dateStr);
			} catch (ParseException e) {
				main.sendConsoleMessage(main.getMessages().color(
					String.format("&cERROR: &4Failed to load expiration date from profile. Error: %s", 
				e.getMessage())));
			}
			
			// Let's disable the plugin.
			main.disable();
		}
		
		// Let's load our timings.
		ConfigurationSection timings = profile.getConfigurationSection("timings");
		gracePeriodLength = timings.getInt("gracePeriodLength");
		healPlayersAfterTime = timings.getInt("healPlayersAfter");
		beginBorderShrink = timings.getInt("beginBorderShrink");
		
		// Let's load our scenarios.
		for(String scenario : profile.getStringList("scenarios")) {
			ScenarioType type = ScenarioType.getScenarioTypeByKey(scenario);
			if(type != null) {
				scenarios.add(type);
			}else {
				main.sendConsoleMessage(main.getMessages().color(
					String.format("&cERROR: &4Scenario Type config key %s does not exist.", 
				scenario)));
			}
		}
	}
	
	/**
	 * Completely resets the loaded settings to the factory-default.
	 * Saves a brand new profile.yml from the jar file.
	 */
	
	public void reset() {
		main.saveResource("profile.yml", true);
		main.sendConsoleMessage(ChatColor.GREEN + "Saved default profile!");
		read("profile");
		load();
	}
	
}
