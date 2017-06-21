package io.skypvp.uhc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;

import io.skypvp.uhc.arena.Team;
import io.skypvp.uhc.menu.Menu;
import io.skypvp.uhc.player.UHCPlayer;
import io.skypvp.uhc.scenario.ScenarioDrops;
import net.minecraft.server.v1_8_R3.PacketPlayOutScoreboardTeam;
import net.minecraft.server.v1_8_R3.Scoreboard;
import net.minecraft.server.v1_8_R3.ScoreboardTeam;

public class UHCSystem {

    private static SkyPVPUHC main;
    private static HashSet<Team> teams = new HashSet<Team>();
    private static HashSet<ItemStack> restrictedItems = new HashSet<ItemStack>();
    private static HashMap<Block, ScenarioDrops> scenarioDrops = new HashMap<Block, ScenarioDrops>();
    public static ScoreboardTeam GHOST_TEAM;

    static {
        teams = new HashSet<Team>();
        restrictedItems = new HashSet<ItemStack>();
        scenarioDrops = new HashMap<Block, ScenarioDrops>();
        GHOST_TEAM = new ScoreboardTeam(new Scoreboard(), "spectators");
        GHOST_TEAM.setCanSeeFriendlyInvisibles(true);
    }

    /**
     * Sets the specified {@link Player}'s ghost effect.
     * NOTE: Because this method sometimes doesn't work as
     * expected, it is now deprecated. Uses packets.
     * Most likely will be removed.
     * @param {@link Player} player who is effected.
     * @param boolean enable - if the ghost effect is enabled or disabled.
     */

    @Deprecated
    public static void setGhost(Player player, boolean enable) {
        PacketPlayOutScoreboardTeam packet = new PacketPlayOutScoreboardTeam();
        if(enable && player.hasPotionEffect(PotionEffectType.INVISIBILITY)){
            packet = new PacketPlayOutScoreboardTeam(GHOST_TEAM, Arrays.asList(player.getName()), 4);
            player.removePotionEffect(PotionEffectType.INVISIBILITY);
        } else if(!enable) {
            packet = new PacketPlayOutScoreboardTeam(GHOST_TEAM, Arrays.asList(player.getName()), 3);
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 15));
        }

        for(Player players : Bukkit.getOnlinePlayers()) {
            ((CraftPlayer) players).getHandle().playerConnection.sendPacket(packet);
        }
    }

    /**
     * Obtains a "safe" {@link Location} between the specified x and z values.
     * Method avoids the following {@link Material}s.
     * - {@link Material.LAVA}
     * - {@link Material.STATIONARY_LAVA}
     * - {@link Material.WATER}
     * - {@link Material.STATIONARY_WATER}
     * - {@link Material.CACTUS}
     * - {@link Material.WEB}
     * @param double minX - the minimum x
     * @param double maxX - the maximum x
     * @param double minZ - the minimum z
     * @param double maxZ - the maximum z
     * @return A "safe" {@link Location}
     */

    public static Location getRandomSpawnPoint(double minX, double maxX, double minZ, double maxZ) {
        World world = main.getWorldHandler().getGameWorld().getCBWorld();
        double x, z;
        Block highestBlock;

        do {
            x = ThreadLocalRandom.current().nextDouble(minX, maxX);
            z = ThreadLocalRandom.current().nextDouble(minZ, maxZ);
            highestBlock = world.getHighestBlockAt((int) x, (int) z);
        } while(Arrays.asList(Material.LAVA, Material.STATIONARY_LAVA, Material.WATER, 
                Material.STATIONARY_WATER, Material.CACTUS, 
                Material.WEB).contains(highestBlock.getType()));

        return new Location(world, x, highestBlock.getLocation().getY() + 1.0, z);
    }

    /////////////////////////////////////////////////////////
    //               Methods relating to teams             //
    /////////////////////////////////////////////////////////

    /**
     * Registers a {@link Team} to the system.
     * NOTE: All teams that should be available in-game must be registered here.
     * @param {@link Team} team - The team to register.
     */

    public static void registerTeam(Team team) {
        teams.add(team);
    }

    /**
     * Fetches a {@link HashSet} of registered {@link Team}s.
     * @return {@link HashSet} of {@link Team}.
     */

    public static HashSet<Team> getTeams() {
        return teams;
    }

    /**
     * Returns the name of a team.
     * Example: Team Orange
     * @param {@link Team} team - The team name that should be returned.
     * @return String - Colored name of team.
     */

    public static String getTeamNameWithPrefix(Team team) {
        String teamColor = team.getName().substring(0, 2);
        return teamColor.concat("Team ").concat(team.getName().substring(2, team.getName().length()));
    }

    /**
     * Assigns a {@link UHCPlayer} to a random {@link Team}.
     * @param {@link UHCPlayer} uhcPlayer - The player to assign to a random team.
     */

    public static void assignPlayerToRandomTeam(UHCPlayer uhcPlayer) {
        ArrayList<Team> availableTeams = new ArrayList<Team>();
        for(Team t : teams) {
            if(t.getMembers().size() < main.getProfile().getTeamSize()) {
                availableTeams.add(t);
            }
        }

        Team t = availableTeams.get(ThreadLocalRandom.current().nextInt(0, 
                availableTeams.size()));
        t.addMember(uhcPlayer);
    }

    /////////////////////////////////////////////////////////
    //         Methods relating to restricted items        //
    //   Restricted items are items that can't be dropped  //
    /////////////////////////////////////////////////////////

    /**
     * Starts keeping track of the {@link ItemStack} specified
     * as a "system" item. System items cannot be dropped or moved
     * around.
     * @param {@link ItemStack} item - Should not be null.
     */
    public static void addRestrictedItem(ItemStack item) {
        restrictedItems.add(item);
    }

    /**
     * Checks if an {@link ItemStack} is a restricted/system item.
     * @param {@link ItemStack} - The item to be checked.
     * PRECONDITION: Specified {@link ItemStack} is NOT null.
     * @return boolean flag (true/false)
     */

    public static boolean isRestrictedItem(ItemStack item) {
        for(ItemStack restricted : restrictedItems) {
            if(item.isSimilar(restricted)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Fetches the {@link HashSet} of restricted items.
     * @return {@link HashSet} of {@link ItemStack}.
     */

    public static HashSet<ItemStack> getRestrictedItems() {
        return restrictedItems;
    }

    /////////////////////////////////////////////////////////

    public static void addScenarioDrop(Block block, ScenarioDrops drop) {
        scenarioDrops.put(block, drop);
    }

    public static ScenarioDrops getScenarioDrops(Block block) {
        return scenarioDrops.get(block);
    }

    public static HashMap<Block, ScenarioDrops> getScenarioDrops() {
        return scenarioDrops;
    }

    /////////////////////////////////////////////////////////

    /**
     * Returns a String like 1st, 2nd, 3rd, 5th, etc.
     * @param int place
     * @return String
     */

    public static String getOrdinal(int place) {
        String suffix = "th";
        switch (place) {
        case 1:
            suffix = "st";
            break;
        case 2:
            suffix = "nd";
            break;
        case 3:
            suffix = "rd";
            break;
        default: break;
        }

        return String.format("%d%s", place, suffix);
    }

    /**
     * Opens a {@link Menu} and assigns it to the specified {@link UHCPlayer}.
     * @param {@link UHCPlayer} player who is looking at the {@link Menu}.
     * @param {@link Menu} menu that is being displayed.
     */

    public static void openMenu(UHCPlayer player, Menu menu) {
        player.setActiveMenu(menu);
        menu.show();
        player.getBukkitPlayer().openInventory(menu.getUI());
    }

    /////////////////////////////////////////////////////////

    /**
     * Names the specified {@link ItemStack} with the specified name.
     * @param {@link ItemStack} item to be named.
     * @param {@link String} name - what the item should be called.
     * @return The specified {@link ItemStack} with the specified name attached.
     */

    public static ItemStack nameItem(ItemStack item, String name) {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Names the specified {@link ItemStack} with the specified name
     * and attaches a lore to it.
     * @param {@link ItemStack} item to be renamed and given lore.
     * @param {@link String} name - what the item should be called.
     * @param {@link List} of {@link String} lore to be attached to the item.
     * @return The specified {@link ItemStack} with the specified name and lore attached.
     */

    public static ItemStack nameAndLoreItem(ItemStack item, String name, List<String> lore) {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /////////////////////////////////////////////////////////

    /**
     * Gives the specified {@link UHCPlayer} the 
     * lobby {@link UHCScoreboard} and the team selector, if
     * the current match is a team one.
     * @param {@link SkyPVPUHC} main - The main class of this plugin.
     * @param {@link UHCPlayer} player - The player to be setup.
     */

    public static void handleLobbyArrival(SkyPVPUHC main, UHCPlayer player) {
        Player p = player.getBukkitPlayer();
        UHCScoreboard scoreboard = new UHCScoreboard(main, "lobbyScoreboard", DisplaySlot.SIDEBAR);
        scoreboard.generate(main.getOnlinePlayers().get(p.getUniqueId()));
        player.setScoreboard(scoreboard);

        // If the game is available to join, let's give the player items.
        if(main.getGameStateManager().getActiveState().toIndex() < 3) {
            p.getInventory().clear();

            // Gives the team selector item if this is a team match,.
            ItemStack teamSelector = main.getSettings().getTeamSelectorItem();
            if(main.getProfile().isTeamMatch()) {
                p.getInventory().setItem(0, teamSelector);
            }
        }
    }

    /////////////////////////////////////////////////////////
    //    Helper methods that broadcast to all players     //
    /////////////////////////////////////////////////////////

    /**
     * Broadcasts the specified {@link Sound} to all online {@link Player}s.
     * NOTE: Pitch and Volume are 1.0f
     * @param {@link Sound} - The sound to broadcast to all players.
     */

    public static void broadcastSound(Sound sound) {
        Iterator<UHCPlayer> iterator = main.getOnlinePlayers().values().iterator();
        while(iterator.hasNext()) {
            UHCPlayer p = iterator.next();
            Player bP = Bukkit.getPlayer(p.getUUID());
            if(bP != null) {
                bP.playSound(bP.getLocation(), sound, 1F, 1F);
            }
        }
    }

    /**
     * Broadcasts the specified {@link String} to all online (@link Player}s.
     * @param {@link String} - The string to broadcast to all players.
     */

    public static void broadcastMessage(String msg) {
        Iterator<UHCPlayer> iterator = main.getOnlinePlayers().values().iterator();
        while(iterator.hasNext()) {
            UHCPlayer p = iterator.next();
            Player bP = Bukkit.getPlayer(p.getUUID());
            if(bP != null) {
                bP.sendMessage(msg);
            }
        }
    }

    /**
     * Broadcasts a specified message and {@link Sound} to all online {@link Player}s.
     * NOTE: Pitch and Volume are 1.0f.
     * @param {@link String} msg - The string to broadcast to all players.
     * @param {@link Sound} sound - The sound to broadcast to all players.
     */

    public static void broadcastMessageAndSound(String msg, Sound sound) {
        UHCSystem.broadcastMessageAndSound(msg, sound, 1F);
    }

    /**
     * Broadcasts a specified message and {@link Sound} to all online {@link Player}s.
     * @param {@link String} msg - The string to broadcast to all players.
     * @param {@link Sound} sound - The sound to broadcast to all players.
     * @param float volume - The volume AND pitch of the specified {@link Sound}.
     */

    public static void broadcastMessageAndSound(String msg, Sound sound, float volume) {
        Iterator<UHCPlayer> iterator = main.getOnlinePlayers().values().iterator();
        while(iterator.hasNext()) {
            UHCPlayer p = iterator.next();
            Player bP = Bukkit.getPlayer(p.getUUID());
            if(bP != null) {
                bP.sendMessage(msg);
                bP.playSound(bP.getLocation(), sound, volume, volume);
            }
        }
    }

}
