package io.skypvp.uhc.scenario;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;

import io.skypvp.uhc.SkyPVPUHC;

public class Timber extends Scenario {
	
	public static ArrayList<Material> TREE_BLOCKS;
	
	static {
		TREE_BLOCKS.add(Material.LOG);
		TREE_BLOCKS.add(Material.LOG_2);
		TREE_BLOCKS.add(Material.LEAVES);
		TREE_BLOCKS.add(Material.LEAVES_2);
	}
	
	public Timber(SkyPVPUHC main) {
		super(main, ScenarioType.TIMBER);
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent evt) {
		Block b = evt.getBlock();
		if(isActive() && !evt.isCancelled() && TREE_BLOCKS.contains(b.getType())) {
			handleTree(b);
		}
	}
	
	public void handleTree(Block treeBlock) {
		if(!TREE_BLOCKS.contains(treeBlock.getType())) return;
		treeBlock.breakNaturally();
		
		for(BlockFace face : BlockFace.values()) {
			handleTree(treeBlock.getRelative(face));
		}
	}

	@Override
	public void unregisterEvents() {
		BlockBreakEvent.getHandlerList().unregister(this);
	}
	
	public static ArrayList<Material> getTreeBlocks() {
		return Timber.TREE_BLOCKS;
	}

}
