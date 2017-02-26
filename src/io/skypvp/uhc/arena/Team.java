package io.skypvp.uhc.arena;

import io.skypvp.uhc.player.UHCPlayer;

import java.util.HashSet;

import org.bukkit.inventory.ItemStack;

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
