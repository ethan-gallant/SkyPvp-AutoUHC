package io.skypvp.uhc.scenario;

import io.skypvp.uhc.SkyPVPUHC;

import java.util.ArrayList;

import org.bukkit.inventory.ItemStack;

public abstract class DropUpdaterScenario extends Scenario {
	
	public static ArrayList<ScenarioType> DROP_UPDATE_ORDER;
	
	static {
		DROP_UPDATE_ORDER = new ArrayList<ScenarioType>();
		DROP_UPDATE_ORDER.add(ScenarioType.TRIPLE_ORES);
		DROP_UPDATE_ORDER.add(ScenarioType.CUTCLEAN);
		DROP_UPDATE_ORDER.add(ScenarioType.DOUBLE_OR_NOTHING);
	}
	
	public DropUpdaterScenario(SkyPVPUHC main, ScenarioType type) {
		super(main, type);
	}
	
	/**
	 * Method that should be used to update item drops if the scenario calls for it.
	 * @param ArrayList<ItemStack> | The actual drops
	 * @return ArrayList<ItemStack> | Updated drops
	 */
	
	public abstract ArrayList<ItemStack> handleDrops(ArrayList<ItemStack> drops);

	public abstract void unregisterEvents();
	
	public static ArrayList<ScenarioType> getDROP_UPDATE_ORDER() {
		return DropUpdaterScenario.DROP_UPDATE_ORDER;
	}

}
