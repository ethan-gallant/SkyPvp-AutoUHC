package io.skypvp.uhc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.wimbli.WorldBorder.WorldBorder;

public class WorldManager implements Listener {

    final SkyPVPUHC main;
    final WorldBorder worldBorder;
    final List<String> seeds;

    private String lastSeed;
    private boolean initialWorldLoadDenied;

    public WorldManager(SkyPVPUHC instance) {
        this.main = instance;
        this.worldBorder = instance.getWorldBorder();
        this.seeds = instance.getSettings().getSeeds();
        this.initialWorldLoadDenied = false;

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

    @EventHandler
    public void onWorldLoad(WorldLoadEvent e) {
        if(!initialWorldLoadDenied) {
            deleteWorld();
            initialWorldLoadDenied = true;

            // generateWorld();
        }
    }

    /**
     * Generates a world suitable for UHC.
     */

    public void generateWorld() {
        String seed = getSeed();

        main.sendConsoleMessage(ChatColor.YELLOW + "Preparing new UHC game world...");
        main.sendConsoleMessage(String.format(ChatColor.YELLOW + "World will use seed '%s'", seed));

        WorldCreator worldCreator = new WorldCreator(Globals.GAME_WORLD_NAME);
        worldCreator.generateStructures(true);
        worldCreator.environment(Environment.NORMAL);
        worldCreator.type(WorldType.NORMAL);
        worldCreator.seed(Long.valueOf(seed));

        World world = worldCreator.createWorld();

        new BukkitRunnable() {

            public void run() {
                if(Bukkit.getWorld(Globals.GAME_WORLD_NAME) != null) {
                    main.sendConsoleMessage("Is world loaded? Yes!");
                    cancel();
                }else {
                    main.sendConsoleMessage("Is world loaded? No.");
                }
            }
        }.runTaskTimer(main, 200L, 0L);
    }

    /**
     * Unloads the world with the game world name and deletes
     * its directory.
     * @return boolean - If the world was unloaded or not.
     */

    public boolean deleteWorld() {
        World world = Bukkit.getWorld(Globals.GAME_WORLD_NAME);
        if(world != null) {
            boolean unloaded = Bukkit.unloadWorld(world, false);

            new BukkitRunnable() {

                public void run() {
                    File sessionLock = new File(Bukkit.getWorldContainer() + String.format("/%s/session.lock", Globals.GAME_WORLD_NAME));
                    if(!sessionLock.exists()) {
                        main.sendConsoleMessage(ChatColor.RED + "Deleted world folder!");
                        File folder = new File(Bukkit.getWorldContainer() + "/" + Globals.GAME_WORLD_NAME);
                        folder.delete();
                        cancel();
                    }
                }

            }.runTaskAsynchronously(main);

            return unloaded;
        }

        return false;
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
}
