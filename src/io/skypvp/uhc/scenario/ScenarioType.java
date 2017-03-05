package io.skypvp.uhc.scenario;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public enum ScenarioType {
	CUTCLEAN("cutclean"),
	TIMEBOMB("timebomb"),
	SWITCHAROO("switcharoo"),
	DIAMONDLESS("diamondless"),
	TRIPLE_ORES("triple-ores"),
	TIMBER("timber"),
	FIRELESS("fireless"),
	DOUBLE_HEALTH("double-health"),
	NO_FALL("no-fall"),
	INCREASING_SPEED("increasing-speed"),
	ENCHANTED_DEATH("enchanted-death"),
	ONE_HEAL("one-heal"),
	DOUBLE_OR_NOTHING("double-or-nothing");
	
	private final String configKey;
	private String name;
	private ItemStack icon;
	private ConfigurationSection settingsSection;
	
	private ScenarioType(String configKey) {
		this.configKey = configKey;
		this.name = null;
		this.icon = null;
		this.settingsSection = null;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setIcon(ItemStack icon) {
		this.icon = icon;
	}
	
	public ItemStack getIcon() {
		return this.icon.clone();
	}
	
	public void setSettingsSection(ConfigurationSection section) {
		this.settingsSection = section;
	}
	
	public ConfigurationSection getSettingsSection() {
		return this.settingsSection;
	}
	
	public String getConfigKey() {
		return this.configKey;
	}
}
