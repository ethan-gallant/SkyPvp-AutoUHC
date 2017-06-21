package io.skypvp.uhc.arena;

import java.util.HashSet;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import io.skypvp.uhc.UHCSystem;
import io.skypvp.uhc.player.UHCPlayer;
import io.skypvp.uhc.util.ConfigUtils;

public class Team {

	private final String name;
	private final ItemStack icon;
	private final HashSet<UHCPlayer> members;

	public Team(String name, ItemStack icon) {
		this.name = name;
		this.icon = icon;
		this.members = new HashSet<UHCPlayer>();
	}

	/**
	 * Gives armor and other gear to a {@link UHCPlayer}
	 * @param UHCPlayer member
	 */

	public void giveGear(UHCPlayer member) {
		// Let's give this player the appropriate armor.
		ItemStack helmet = createArmorPiece(Material.LEATHER_HELMET);
		ItemStack chestplate = createArmorPiece(Material.LEATHER_CHESTPLATE);
		ItemStack leggings = createArmorPiece(Material.LEATHER_LEGGINGS);
		ItemStack boots = createArmorPiece(Material.LEATHER_BOOTS);

		Player p = member.getBukkitPlayer();
		p.getInventory().setHelmet(helmet);
		p.getInventory().setChestplate(chestplate);
		p.getInventory().setLeggings(leggings);
		p.getInventory().setBoots(boots);

		// Let's give this player the team locator compass.
		String compassName = ConfigUtils.main.getMessages().getColoredString("finderCompass");
		ItemStack compass = UHCSystem.nameItem(new ItemStack(Material.COMPASS), compassName);
		p.getInventory().addItem(compass);
	}

	private ItemStack createArmorPiece(Material type) {
		@SuppressWarnings("deprecation")
		DyeColor color = DyeColor.getByDyeData(icon.getData().getData());
		ItemStack piece = new ItemStack(type, 1);
		String pieceName = type.name().substring(type.name().indexOf("_") + 1, type.name().length()).toLowerCase();
		pieceName = pieceName.substring(0, 1).toUpperCase().concat(pieceName.substring(1, pieceName.length()));
		String itemName = ChatColor.translateAlternateColorCodes('&', String.format("%s %s", getName(), pieceName));
		piece = UHCSystem.nameItem(piece, itemName);

		LeatherArmorMeta pieceMeta = (LeatherArmorMeta) piece.getItemMeta();
		pieceMeta.setColor(color.getColor());
		piece.setItemMeta(pieceMeta);

		return piece;
	}

	/**
	 * Returns raw uncolored name from config.
	 * @return String
	 */

	public String getName() {
		return this.name;
	}

	public ItemStack getIcon() {
		return this.icon.clone();
	}

	/**
	 * Adds a player to the 'members' HashSet and
	 * sets the UHCPlayer's team object.
	 * @param UHCPlayer player
	 */

	public void addMember(UHCPlayer player) {
		player.setTeam(this);
		members.add(player);
	}

	/**
	 * Removes a player from the 'members' HashSet and
	 * resets the UHCPlayer's team object.
	 * @param UHCPlayer player
	 * @return true if removed, false if not removed.
	 */

	public boolean removeMember(UHCPlayer player) {
		boolean removed = members.remove(player);
		if(removed) player.setTeam(null);
		return removed;
	}

	public HashSet<UHCPlayer> getMembers() {
		return this.members;
	}
}
