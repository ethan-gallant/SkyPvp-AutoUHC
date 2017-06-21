package io.skypvp.uhc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldType;
import org.bukkit.block.Block;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.wimbli.WorldBorder.BorderData;
import com.wimbli.WorldBorder.Config;
import com.wimbli.WorldBorder.WorldBorder;

import io.skypvp.uhc.arena.Profile;
import net.md_5.bungee.api.ChatColor;

public class WorldHandler {

	final SkyPVPUHC main;
	final WorldBorder worldBorder;
	final MultiverseCore multiverse;
	final MVWorldManager worldMgr;
	final List<String> seeds;
	private MultiverseWorld gameWorld;
	String lastSeed;

	public WorldHandler(SkyPVPUHC instance) { 
		this.main = instance;
		this.worldBorder = instance.getWorldBorder();
		this.multiverse = null; //instance.getMultiverse();
		this.worldMgr = multiverse.getMVWorldManager();
		this.seeds = instance.getSettings().getSeeds();
		this.gameWorld = null;

		// Let's create the lastSeed.txt file in-case it doesn't exist.
		File lastSeedFile = new File(main.getDataFolder() + "/lastSeed.txt");
		if(!lastSeedFile.exists()) {
			try {
				lastSeedFile.createNewFile();
			} catch (IOException e) {
				main.sendConsoleMessage(ChatColor.DARK_RED + "Encountered an error while creating lastSeed.txt file.");
				e.printStackTrace();
			}
		}

		this.lastSeed = getStoredSeed();
	}

	private String getStoredSeed() {
		try(BufferedReader br = new BufferedReader(new FileReader(main.getDataFolder() + "/lastSeed.txt"))) {
			String seed = br.readLine();
			if(seeds.contains(seed)) {
				return seed;
			}else if(seed == null || !seed.isEmpty()) {
				main.sendConsoleMessage(ChatColor.DARK_RED + "Obtained a last seed that is not listed in the config.");
				return "";
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return "";
	}

	public void storeLastSeed() {
		try {
			PrintWriter writer = new PrintWriter(main.getDataFolder() + "/lastSeed.txt", "UTF-8");
			writer.println(lastSeed);
			writer.close();
		} catch (IOException e) {
			main.sendConsoleMessage(ChatColor.DARK_RED + "Encountered an error while trying to save last seed.");
			e.printStackTrace();
		}
	}

	public void setPVP(boolean flag) {
		gameWorld.setPVPMode(flag);
	}

	public void createGameWorld() {
		String seed = getSeed();

		main.sendConsoleMessage(ChatColor.YELLOW + "Preparing new UHC game world...");
		main.sendConsoleMessage(String.format(ChatColor.YELLOW + "World will use seed '%s'", seed));
		boolean added = worldMgr.addWorld(Globals.GAME_WORLD_NAME, Environment.NORMAL, seed, WorldType.NORMAL, true, "", false);

		if(added) {
			lastSeed = seed;
			gameWorld = worldMgr.getMVWorld(Globals.GAME_WORLD_NAME);
			storeLastSeed();

			Profile profile = main.getProfile();
			int initMapSize = profile.getInitialMapSize();

			// Let's setup the world border.
			setBorder(initMapSize, true);

			// Let's set the respawn world.
			gameWorld.setRespawnToWorld(Globals.LOBBY_WORLD_NAME);

			main.sendConsoleMessage(ChatColor.DARK_GREEN + "Successfully created new UHC game world!");

			main.getGameStateManager().startRunning();
		}
	}

	public void setBorder(int mapSize, boolean makeBlockBorder) {
		World cbWorld = gameWorld.getCBWorld();
		Config.setBorder(Globals.GAME_WORLD_NAME, mapSize, mapSize, cbWorld.getSpawnLocation().getX(), cbWorld.getSpawnLocation().getZ(), false);

		if(makeBlockBorder) {
			BorderData border = worldBorder.getWorldBorder(Globals.GAME_WORLD_NAME);
			generateBlockBorder(border);
		}
	}

	public void deleteGameWorld() {
		worldMgr.removePlayersFromWorld(Globals.GAME_WORLD_NAME);
		worldMgr.deleteWorld(Globals.GAME_WORLD_NAME, true);
		main.sendConsoleMessage(ChatColor.DARK_GREEN + "Successfully deleted UHC game world.");
	}

	public void generateBlockBorder(BorderData border) {
		double minX = border.getX() - border.getRadiusX(), maxX = border.getX() + border.getRadiusX();
		double minZ = border.getZ() - border.getRadiusZ(), maxZ = border.getZ() + border.getRadiusZ();
		double startY = 75.0, endY = 1.0;

		main.sendConsoleMessage(ChatColor.YELLOW + String.format("Preparing %dx%d Block Border... this may take a while...", 
				(int) border.getRadiusX(), (int) border.getRadiusZ()));

		// Wall from minX -> maxX
		for(double y = startY; y >= endY; y--) {
			for(double cX = minX; cX <= maxX; cX++) {
				Block b = gameWorld.getCBWorld().getBlockAt((int) cX, (int) y, (int) minZ);
				b.setType(Material.BEDROCK);
			}
		}

		double mZ = Math.min(minZ, maxZ);
		double mxZ = Math.max(minZ, maxZ);
		// Wall from minZ -> maxZ using maxX for X
		for(double y = startY; y >= endY; y--) {
			for(double cZ = minZ; cZ <= maxZ; cZ++) {
				Block b = gameWorld.getCBWorld().getBlockAt((int) maxX, (int) y, (int) cZ);
				b.setType(Material.BEDROCK);
			}
		}
		// Wall from minZ -> maxZ using minX for X
		for(double y = startY; y >= endY; y--) {
			for(double cZ = mZ; cZ <= mxZ; cZ++) {
				Block b = gameWorld.getCBWorld().getBlockAt((int) minX, (int) y, (int) cZ);
				b.setType(Material.BEDROCK);
			}
		}

		// Wall from minX -> maxX
		for(double y = startY; y >= endY; y--) {
			for(double cX = minX; cX <= maxX; cX++) {
				Block b = gameWorld.getCBWorld().getBlockAt((int) cX, (int) y, (int) maxZ);
				b.setType(Material.BEDROCK);
			}
		}

		main.sendConsoleMessage(ChatColor.DARK_GREEN + "Successfully created Block Border!");
	}

	public String getSeed() {
		if(main.getSettings().wantRandomSeeds()) {
			return seeds.get(ThreadLocalRandom.current().nextInt(0, seeds.size()));
		}else {
			int lastIndex = seeds.indexOf(lastSeed);
			return ((lastIndex + 1) < seeds.size()) ? seeds.get(lastIndex + 1) : seeds.get(0);
		}
	}

	public String getLastSeed() {
		return this.lastSeed;
	}

	public MVWorldManager getWorldManager() {
		return this.worldMgr;
	}

	public MultiverseWorld getGameWorld() {
		return this.gameWorld;
	}
}
