package io.skypvp.uhc;

import io.skypvp.uhc.arena.Team;
import io.skypvp.uhc.jedis.UHCJedis;
import io.skypvp.uhc.scenario.ScenarioType;
import io.skypvp.uhc.util.ConfigUtils;

import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class Settings {
	
	final SkyPVPUHC main;
	private Database database;
	private UHCJedis jedis;
	private final File configFile;
	private final YamlConfiguration config;
	private String serverName;
	private boolean wantRandomSeeds;
	private boolean wantLightningDeaths;
	private boolean wantGodApples;
	private boolean wantPrefixMsgs;
	private ItemStack kitSelector;
	private ItemStack teamSelector;
	private int borderShrinkEveryTime;
	private int freezeTime;
	private int startTime;
	private int timebombExplodeTime;
	private int minSoloGamePlayers;
	private int minTeamGamePlayers;
	private Sound countdown;
	private Sound error;
	private Sound stateUpdate;
	private List<String> seeds;
	
	// Stuff for scoreboards
	private String scoreboardHeader;
	
	// Server information for Jedis.
	private String jedisHost;
	private int jedisPort;
	private String jedisPassword;
	
	public Settings(SkyPVPUHC instance) {
		this.main = instance;
		this.configFile = new File(main.getDataFolder() + "/config.yml");
		this.serverName = "";
		this.seeds = null;
		this.wantRandomSeeds = false;
		this.wantLightningDeaths = true;
		this.wantGodApples = false;
		this.wantPrefixMsgs = true;
		this.kitSelector = null;
		this.teamSelector = null;
		this.borderShrinkEveryTime = 0;
		this.freezeTime = 0;
		this.startTime = 0;
		this.timebombExplodeTime = 0;
		this.minSoloGamePlayers = 0;
		this.minTeamGamePlayers = 0;
		this.scoreboardHeader = "";
		this.countdown = null;
		this.error = null;
		this.stateUpdate = null;
		this.jedis = null;
		this.jedisHost = "";
		this.jedisPort = 0;
		this.jedisPassword = "";
		
		boolean configAvailable = configFile.exists();
		if(!configAvailable) {
			main.saveDefaultConfig();
		}

		this.config = YamlConfiguration.loadConfiguration(configFile);
	}
	
	@SuppressWarnings("deprecation")
	private boolean configUpToDate() {
		String version = config.getString("config-version");
		YamlConfiguration jarConfig = YamlConfiguration.loadConfiguration(main.getResource("config.yml"));
		String jarCfgVersion = jarConfig.getString("config-version");
		
		if(!jarCfgVersion.equalsIgnoreCase(version)) {
			configFile.renameTo(new File(main.getDataFolder() + "/config-old.yml"));
			
			main.saveDefaultConfig();
			main.sendConsoleMessage(ChatColor.YELLOW + "Your config is out-of-date, "
					+ "a new one has been saved and your old one has been renamed. Please consider your old settings and restart the plugin.");
			main.disable();
			
			return false;
		}
		
		main.sendConsoleMessage(ChatColor.DARK_GREEN + String.format("Config version (%s) is up-to-date.", version));
		return true;
	}
	
	public void load() {
		if(configUpToDate()) {
			serverName = config.getString("server-name");
			seeds = config.getStringList("seeds");
			wantRandomSeeds = config.getBoolean("want-random-seeds");
			wantLightningDeaths = config.getBoolean("want-lightning-deaths");
			wantGodApples = config.getBoolean("want-god-apples");
			wantPrefixMsgs = config.getBoolean("want-prefix-messages");
			scoreboardHeader = config.getConfigurationSection("scoreboards").getString("header");
			minSoloGamePlayers = config.getInt("minimum-solo-game-players");
			minTeamGamePlayers = config.getInt("minimum-team-game-players");
			
			// Let's load up the materials.
			kitSelector = ConfigUtils.handleIconString(config.getString("kit-selector-item"));
			kitSelector = UHCSystem.nameItem(kitSelector, main.getMessages().color(main.getMessages().getRawMessage("kits")));
			teamSelector = ConfigUtils.handleIconString(config.getString("team-selector-item"));
			teamSelector = UHCSystem.nameItem(teamSelector, main.getMessages().color(main.getMessages().getRawMessage("teams")));
			UHCSystem.addRestrictedItem(kitSelector);
			UHCSystem.addRestrictedItem(teamSelector);
			
			// Let's load up the timings.
			ConfigurationSection timings = config.getConfigurationSection("timings");
			freezeTime = timings.getInt("freezeTime");
			borderShrinkEveryTime = timings.getInt("borderShrinkEveryTime");
			startTime = timings.getInt("startTime");
			timebombExplodeTime = timings.getInt("timebombExplodeTime");
			
			// Let's load up the sounds.
			ConfigurationSection sounds = config.getConfigurationSection("sounds");
			error = ConfigUtils.handleSoundString(sounds.getString("error"));
			countdown = ConfigUtils.handleSoundString(sounds.getString("countdown"));
			stateUpdate = ConfigUtils.handleSoundString(sounds.getString("state-update"));
			
			// Let's load up the teams.
			ConfigurationSection teams = config.getConfigurationSection("teams");
			for(String key : teams.getKeys(false)) {
				ConfigurationSection teamSection = teams.getConfigurationSection(key);
				String name = teamSection.getString("name");
				ItemStack icon = ConfigUtils.handleIconString(teamSection.getString("icon"));
				UHCSystem.addTeam(new Team(name, icon));
			}
			
			// Let's load up the scenario data.
			ConfigurationSection scenarioSection = config.getConfigurationSection("scenarios");
			HashMap<String, ScenarioType> scenarios = new HashMap<String, ScenarioType>();
			for(ScenarioType s : ScenarioType.values()) scenarios.put(s.getConfigKey(), s);
			
			for(String key : scenarioSection.getKeys(false)) {
				ConfigurationSection scenSection = scenarioSection.getConfigurationSection(key);
				String name = scenSection.getString("name");
				ItemStack icon = ConfigUtils.handleIconString(scenSection.getString("icon"));
				scenarios.get(key).setName(name);
				scenarios.get(key).setIcon(icon);
				scenarios.get(key).setSettingsSection(scenSection);
			}
			
			Globals.MAX_MEMBERS_PER_TEAM = (int) Math.ceil(main.getProfile().getMaxPlayers() / UHCSystem.getTeams().size());
			
			// Let's load up the Jedis settings.
			ConfigurationSection jedisSect = config.getConfigurationSection("jedis");
			jedisHost = jedisSect.getString("host");
			jedisPort = jedisSect.getInt("port");
			jedisPassword = jedisSect.getString("password");
			
		    jedis = new UHCJedis(main);
		    jedis.connect(jedisHost, jedisPort, jedisPassword);

			// Let's load the database settings.
			ConfigurationSection db = config.getConfigurationSection("database");
			String dbHost = db.getString("host");
			String dbPort = db.getString("port");
			String dbUsername = db.getString("username");
			String dbPassword = db.getString("password");
			String dbDatabase = db.getString("database");
			database = new Database(main, dbHost, dbPort, dbUsername, dbPassword, dbDatabase);
			
			new BukkitRunnable() {
				
				public void run() {
					try {
						database.openConnection();
						if(!database.tableExists()) database.createTable();
					} catch (SQLException e) {
						main.sendConsoleMessage(ChatColor.DARK_RED + "Failed to connect to MySQL database. Are your settings correct in the config?");
						e.printStackTrace();
						main.disable();
					}
				}
				
			}.runTaskAsynchronously(main);
		}
	}
	
	public Database getDatabase() {
		return this.database;
	}
	
	public UHCJedis getJedis() {
	    return this.jedis;
	}
	
	public String getServerName() {
		return this.serverName;
	}
	
	public boolean wantRandomSeeds() {
		return this.wantRandomSeeds;
	}
	
	public boolean wantLightningDeaths() {
		return this.wantLightningDeaths;
	}
	
	public boolean wantGodApples() {
		return this.wantGodApples;
	}
	
	public boolean wantPrefixMessages() {
		return this.wantPrefixMsgs;
	}
	
	public ItemStack getKitSelectorItem() {
		return this.kitSelector.clone();
	}
	
	public ItemStack getTeamSelectorItem() {
		return this.teamSelector.clone();
	}
	
	public int getFreezeTime() {
		return this.freezeTime;
	}
	
	public int getBorderShrinkEveryTime() {
		return this.borderShrinkEveryTime;
	}
	
	public int getStartTime() {
		return this.startTime;
	}
	
	public int getTimebombExplodeTime() {
		return this.timebombExplodeTime;
	}
	
	public Sound getCountdownSound() {
		return this.countdown;
	}
	
	public Sound getErrorSound() {
		return this.error;
	}
	
	public Sound getStateUpdateSound() {
		return this.stateUpdate;
	}
	
	public List<String> getSeeds() {
		return this.seeds;
	}
	
	public String getScoreboardHeader() {
		return this.scoreboardHeader;
	}
	
	public int getMinimumSoloGamePlayers() {
		return this.minSoloGamePlayers;
	}
	
	public int getMinimumTeamGamePlayers() {
		return this.minTeamGamePlayers;
	}
	
	public ConfigurationSection getMessagesSection() {
		return this.config.getConfigurationSection("messages");
	}
	
	public ConfigurationSection getScoreboardSection() {
		return config.getConfigurationSection("scoreboards");
	}
}
