package io.skypvp.uhc.arena;

import io.skypvp.uhc.SkyPVPUHC;
import io.skypvp.uhc.scenario.ScenarioType;
import io.skypvp.uhc.util.ConfigUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

public class Profile {

	final SkyPVPUHC main;
	private File profileFile;
	private YamlConfiguration profile;

	// Basic settings from the profile.
	private int initMapSize;
	private int maxPlayers;
	private boolean isPrivate;
	private String owner;

	// Timings from the profile. (in seconds)
	private int gracePeriodLength;
	private int healPlayersAfterTime;
	private int gameplayTime;
	private int beginBorderShrink;

	/////////////////////////////////
	// Settings related to teams.  //
	/////////////////////////////////

	// If this match is team mode or not.
	private boolean teamMode;

	// How many slots per team.
	private int teamSize;

	// If team members can attack each other.
	private boolean friendlyFire;

	// If true, players are randomly put on a team and can't change.
	private boolean randomTeams;

	/////////////////////////////////

	// The scenarios that are active
	private ArrayList<ScenarioType> scenarios;

	// The default starting items for a match.
	private ArrayList<ItemStack> startingItems;

	public Profile(SkyPVPUHC instance) {
		this.main = instance;
		this.profileFile = null;
		this.profile = null;

		// Initialize our profile variables.
		this.initMapSize = 0;
		this.maxPlayers = 0;
		this.isPrivate = false;
		this.owner = null;
		this.gracePeriodLength = 0;
		this.healPlayersAfterTime = 0;
		this.beginBorderShrink = 0;
		this.gameplayTime = 0;
		this.scenarios = new ArrayList<ScenarioType>();
		this.startingItems = new ArrayList<ItemStack>();

		// Settings relating to teams.
		this.teamMode = true;
		this.teamSize = 2;
		this.friendlyFire = false;
		this.randomTeams = false;

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

	///////////////////////////////////////////////////////////
	// 				SETTINGS RELATED TO TEAMS                //
	///////////////////////////////////////////////////////////

	/**
	 * Sets if this match uses teams or not.
	 * @param boolean flag
	 */

	public void setTeamMode(boolean flag) {
		this.teamMode = flag;
	}

	/**
	 * Fetches if this is a team match or not.
	 * @return true/false
	 */

	public boolean isTeamMatch() {
		return this.teamMode;
	}

	/**
	 * Sets the team size.
	 * @param int teamSize
	 */

	public void setTeamSize(int teamSize) {
		this.teamSize = teamSize;
	}

	/**
	 * Fetches the team size.
	 * @return int
	 */

	public int getTeamSize() {
		return this.teamSize;
	}

	/**
	 * Sets if friendly fire is enabled or not.
	 * @param boolean flag
	 */

	public void setFriendlyFire(boolean flag) {
		this.friendlyFire = flag;
	}

	/**
	 * Fetches if friendly fire is allowed or not.
	 * @return true/false
	 */

	public boolean allowsFriendlyFire() {
		return this.friendlyFire;
	}

	/**
	 * Sets if this match uses random teams or not.
	 * @param boolean flag
	 */

	public void setRandomTeams(boolean flag) {
		this.randomTeams = flag;
	}

	/**
	 * Fetches if this match uses random teams or not.
	 * @return true/false
	 */

	public boolean usesRandomTeams() {
		return this.randomTeams;
	}

	///////////////////////////////////////////////////////////

	/**
	 * Sets if this server is private or not.
	 * @param flag
	 */

	public void setPrivate(boolean flag) {
		this.isPrivate = flag;
	}

	/**
	 * Fetches if this server is private or not.
	 * @return
	 */

	public boolean isPrivate() {
		return this.isPrivate;
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

    ///////////////////////////////////////////////////////////
    //           MATCH TIMINGS (MOSTLY IN SECONDS)           //
    ///////////////////////////////////////////////////////////

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
	 * Sets how long gameplay lasts. (in seconds)
	 * @param int gameTime
	 */

	public void setGameplayTime(int gameplayTime) {
		this.gameplayTime = gameplayTime;
	}

	/**
	 * Fetches how long a game lasts. (in seconds)
	 * @return int
	 */

	public int getGameplayTime() {
		return this.gameplayTime;
	}
	
	//////////////////////////////////////////////////

	/**
	 * Fetches the Scenarios selected for this profile.
	 * @return ArrayList<ScenarioType>
	 */

	public ArrayList<ScenarioType> getScenarios() {
		return this.scenarios;
	}

	/**
	 * Fetches the starting items selected for this profile.
	 * @return ArrayList<ItemStack>
	 */

	public ArrayList<ItemStack> getStartingItems() {
		return this.startingItems;
	}
	
	/**
	 * Fetches the minimum needed players to start a match.
	 * @return int
	 */
	
	public int getMinimumNeededPlayers() {
	    return (isTeamMatch()) ? main.getSettings().getMinimumTeamGamePlayers() :
	        main.getSettings().getMinimumSoloGamePlayers();
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
		profile.set("private", isPrivate);
		profile.set("owner", (owner == null) ? "null" : owner);

		// Let's save our team settings.
		profile.set("teamMode", teamMode);
		profile.set("teamSize", teamSize);
		profile.set("friendlyFire", friendlyFire);
		profile.set("randomTeams", randomTeams);

		// Let's save the timings.
		ConfigurationSection timings = profile.getConfigurationSection("timings");
		timings.set("gracePeriodLength", gracePeriodLength);
		timings.set("healPlayersAfter", healPlayersAfterTime);
		timings.set("beginBorderShrink", beginBorderShrink);
		timings.set("gameplayTime", gameplayTime);
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
		isPrivate = profile.getBoolean("private");
		owner = profile.getString("owner");

		// Let's load our team settings.
		teamMode = profile.getBoolean("teamMode");
		teamSize = profile.getInt("teamSize");
		friendlyFire = profile.getBoolean("friendlyFire");
		randomTeams = profile.getBoolean("randomTeams");

		// Let's load our timings.
		ConfigurationSection timings = profile.getConfigurationSection("timings");
		gracePeriodLength = timings.getInt("gracePeriodLength");
		healPlayersAfterTime = timings.getInt("healPlayersAfter");
		beginBorderShrink = timings.getInt("beginBorderShrink");
		gameplayTime = timings.getInt("gameplayTime");

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

		// Let's load our starting items.
		for(String itemData : profile.getStringList("startingItems")) {
			ItemStack item = ConfigUtils.handleIconString(itemData);
			startingItems.add(item);
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
