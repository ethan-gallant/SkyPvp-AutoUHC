package io.skypvp.uhc.scenario;

import java.util.HashMap;

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

    public static final HashMap<ScenarioType, Class<? extends Scenario>> SCENARIO_TYPE_2_SCENARIO = new HashMap<ScenarioType, Class<? extends Scenario>>();

    static {
        SCENARIO_TYPE_2_SCENARIO.put(CUTCLEAN, Cutclean.class);
        SCENARIO_TYPE_2_SCENARIO.put(TIMEBOMB, Timebomb.class);
        SCENARIO_TYPE_2_SCENARIO.put(SWITCHAROO, Switcharoo.class);
        SCENARIO_TYPE_2_SCENARIO.put(DIAMONDLESS, Diamondless.class);
        SCENARIO_TYPE_2_SCENARIO.put(TRIPLE_ORES, TripleOres.class);
        SCENARIO_TYPE_2_SCENARIO.put(TIMBER, Timber.class);
        SCENARIO_TYPE_2_SCENARIO.put(FIRELESS, Fireless.class);
        SCENARIO_TYPE_2_SCENARIO.put(DOUBLE_HEALTH, DoubleHealth.class);
        SCENARIO_TYPE_2_SCENARIO.put(NO_FALL, NoFall.class);
        SCENARIO_TYPE_2_SCENARIO.put(INCREASING_SPEED, IncreasingSpeed.class);
        SCENARIO_TYPE_2_SCENARIO.put(ENCHANTED_DEATH, EnchantedDeath.class);
        SCENARIO_TYPE_2_SCENARIO.put(ONE_HEAL, OneHeal.class);
        SCENARIO_TYPE_2_SCENARIO.put(DOUBLE_OR_NOTHING, DoubleOrNothing.class);
    }

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

    /**
     * Searches for the ScenarioType by its config key.
     * @param String key
     * @return ScenarioType or null if not found.
     */

    public static ScenarioType getScenarioTypeByKey(String key) {
        for(ScenarioType type : ScenarioType.values()) {
            if(type.getConfigKey().equalsIgnoreCase(key)) {
                return type;
            }
        }

        return null;
    }

    /**
     * Fetches the class of the Scenario from the ScenarioType.
     * @param ScenarioType type
     * @return Class<? extends Scenario>
     * @throws IllegalArgumentException if ScenarioType does not have an implementation.
     */

    public static Class<? extends Scenario> getScenarioClassByType(ScenarioType type) throws IllegalArgumentException {
        Class<? extends Scenario> clazz = SCENARIO_TYPE_2_SCENARIO.get(type);
        if(clazz == null) throw new IllegalArgumentException(String.format("ScenarioType type is not implemented or not valid.", type.name()));
        return clazz;
    }
}
