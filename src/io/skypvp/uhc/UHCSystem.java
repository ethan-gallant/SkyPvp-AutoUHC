package io.skypvp.uhc;

import io.skypvp.uhc.arena.Team;
import io.skypvp.uhc.arena.UHCGame;
import io.skypvp.uhc.arena.UHCGame.GameState;
import io.skypvp.uhc.menu.Menu;
import io.skypvp.uhc.player.UHCPlayer;
import io.skypvp.uhc.timer.MatchTimer;
import io.skypvp.uhc.timer.TimerUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.DisplaySlot;

import net.minecraft.util.io.netty.util.internal.ThreadLocalRandom;

public class UHCSystem {
	
	private static SkyPVPUHC main;
	private static HashSet<Team> teams = new HashSet<Team>();
	private static HashSet<ItemStack> restrictedItems = new HashSet<ItemStack>();
	private static MatchTimer lobbyTimer;
	
	public static void setLobbyTimer(SkyPVPUHC instance) {
		UHCSystem.main = instance;
		lobbyTimer = TimerUtils.createTimer(instance, "Starting", main.getSettings().getStartTime());
	}
	
	public static MatchTimer getLobbyTimer() {
		return UHCSystem.lobbyTimer;
	}
	
	public static Location getRandomSpawnPoint(double minX, double maxX, double minZ, double maxZ) {
		World world = main.getWorldHandler().getGameWorld().getCBWorld();
		double x, z;
		Block highestBlock;
		
		do {
			x = ThreadLocalRandom.current().nextDouble(minX, maxX);
			z = ThreadLocalRandom.current().nextDouble(minZ, maxZ);
			highestBlock = world.getHighestBlockAt((int) x, (int) z);
		} while(Arrays.asList(Material.LAVA, Material.WATER, Material.CACTUS, Material.WEB).contains(highestBlock.getType()));
		
		return new Location(world, x, highestBlock.getLocation().getY() + 1.0, z);
	}
	
	public static void addTeam(Team team) {
		teams.add(team);
	}
	
	public static HashSet<Team> getTeams() {
		return teams;
	}
	
	public static void addRestrictedItem(ItemStack item) {
		restrictedItems.add(item);
	}
	
	public static boolean isRestrictedItem(ItemStack item) {
		for(ItemStack restricted : restrictedItems) {
			if(item.isSimilar(restricted)) {
				return true;
			}
		}
		
		return false;
	}
	
	public static HashSet<ItemStack> getRestrictedItems() {
		return restrictedItems;
	}
	
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
	
	public static void openMenu(UHCPlayer player, Menu menu) {
		player.setActiveMenu(menu);
		menu.show();
		player.getBukkitPlayer().openInventory(menu.getUI());
	}
	
	public static ItemStack nameItem(ItemStack item, String name) {
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(name);
		item.setItemMeta(meta);
		return item;
	}
	
	public static ItemStack nameAndLoreItem(ItemStack item, String name, List<String> lore) {
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(name);
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}
	
	public static String getTeamNameWithPrefix(Team team) {
		String teamColor = team.getName().substring(0, 2);
		return teamColor.concat("Team ").concat(team.getName().substring(2, team.getName().length()));
	}
	
	public static void handleLobbyArrival(SkyPVPUHC main, UHCPlayer player) {
		Player p = player.getBukkitPlayer();
		UHCScoreboard scoreboard = new UHCScoreboard(main, "lobbyScoreboard", DisplaySlot.SIDEBAR);
		scoreboard.generate(main.getOnlinePlayers().get(p.getUniqueId()));
		player.setScoreboard(scoreboard);
		
		// If the game is available to join, let's give the player items.
		if(main.getGame().getState() == GameState.WAITING) {
			ItemStack kitSelector = main.getSettings().getKitSelectorItem();
			ItemStack teamSelector = main.getSettings().getTeamSelectorItem();
			p.getInventory().clear();
			if(main.getGame().isTeamMatch()) p.getInventory().setItem(0, teamSelector);
			p.getInventory().setItem(8, kitSelector);
		}
	}
	
	public static void broadcastSound(Sound sound) {
		for(UHCPlayer p : main.getOnlinePlayers().values()) {
			p.getBukkitPlayer().playSound(p.getBukkitPlayer().getLocation(), sound, 1F, 1F);
		}
	}
	
	public static void broadcastMessage(String msg) {
		for(UHCPlayer p : main.getOnlinePlayers().values()) {
			p.getBukkitPlayer().sendMessage(msg);
		}
	}
	
	public static void broadcastMessageAndSound(String msg, Sound sound) {
		for(UHCPlayer p : main.getOnlinePlayers().values()) {
			p.getBukkitPlayer().sendMessage(msg);
			p.getBukkitPlayer().playSound(p.getBukkitPlayer().getLocation(), sound, 1F, 1F);
		}
	}
	
	public static boolean isOre(Material type) {
		return Arrays.asList(Material.DIAMOND_ORE, 
			Material.IRON_ORE, 
			Material.COAL_ORE, 
			Material.EMERALD_ORE,
			Material.LAPIS_ORE,
			Material.GOLD_ORE,
		Material.REDSTONE_ORE).contains(type);
	}
	
	public static ItemStack getSmeltedItemVersion(ItemStack rawItem) {
		HashMap<Material, Material> dict = new HashMap<Material, Material>();
		dict.put(Material.IRON_ORE, Material.IRON_INGOT);
		dict.put(Material.GOLD_ORE, Material.GOLD_INGOT);
		dict.put(Material.RAW_BEEF, Material.COOKED_BEEF);
		dict.put(Material.RAW_CHICKEN, Material.COOKED_CHICKEN);
		dict.put(Material.RAW_FISH, Material.COOKED_FISH);
		dict.put(Material.POTATO_ITEM, Material.BAKED_POTATO);
		dict.put(Material.PORK, Material.GRILLED_PORK);
		
		Material cookedType = dict.get(rawItem.getType());
		if(cookedType == null) {
			return rawItem;
		}else {
			return new ItemStack(cookedType, rawItem.getAmount());
		}
	}
	
	public static boolean canStartGame() {
		UHCGame game = main.getGame();
		int onlinePlayers = main.getOnlinePlayers().keySet().size();
		if((game.isTeamMatch() && onlinePlayers >= main.getSettings().getMinimumTeamGamePlayers()) || (!game.isTeamMatch() && 
				onlinePlayers >= main.getSettings().getMinimumSoloGamePlayers())) {
			return true;
		}
		
		return false;
	}
}
