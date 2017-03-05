package io.skypvp.uhc.scenario;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class ScenarioUtil {
	
	/**
	 * Fetches the first location with air.
	 * @param Location startLocation | Location to begin moving up; searching for air.
	 * @param boolean lookUp | If we should add to the iteration location instead of deplete.
	 * @return Location with air.
	 */
	
	public static Location findSafePlaceLocation(Location startLocation, boolean lookUp) {
		Location safeLocation = null;
		Location iterLocation = startLocation;
		
		// Begin finding a safe location.
		while(safeLocation == null) {
			Block block = iterLocation.getBlock();
			if(block.getType() != Material.AIR) {
				int yChange = (lookUp) ? 1 : -1;
				iterLocation = block.getLocation().add(0, yChange, 0);
			}else {
				safeLocation = block.getLocation();
			}
		}
		
		return safeLocation;
	}
}
