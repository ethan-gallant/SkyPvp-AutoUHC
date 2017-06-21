package io.skypvp.uhc.arena;

import org.bukkit.inventory.ItemStack;

public enum Scenario {
	CUTCLEAN("cutclean"),
	TIMEBOMB("timebomb"),
	SWITCHAROO("switcharoo"),
	DIAMONDLESS("diamondless"),
	BLOOD_DIAMONDS("blood-diamonds"),
	TRIPE_ORES("triple-ores"),
	VEIN_MINER("vein-miner"),
	TIMBER("timber"),
	BOWLESS("bowless"),
	FIRELESS("fireless"),
	STACKABLE_SPEED("stackable-speed"),
	RANDOM_SWITCH("random-switch");

	private final String configKey;
	private String name;
	private ItemStack icon;

	private Scenario(String configKey) {
		this.configKey = configKey;
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

	public String getConfigKey() {
		return this.configKey;
	}
}
