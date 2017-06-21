package io.skypvp.uhc.menu;

import io.skypvp.uhc.Globals;
import io.skypvp.uhc.Messages;
import io.skypvp.uhc.SkyPVPUHC;
import io.skypvp.uhc.UHCSystem;
import io.skypvp.uhc.arena.Team;
import io.skypvp.uhc.player.UHCPlayer;
import io.skypvp.uhc.player.event.UHCPlayerChangeTeamEvent;

import java.util.ArrayList;
import java.util.HashMap;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class TeamSelectorMenu extends Menu {

	final Messages msgs;
	private final HashMap<Integer, Team> teams;

	public TeamSelectorMenu(SkyPVPUHC instance, UHCPlayer uhcPlayer) {
		super(instance, uhcPlayer, Bukkit.createInventory(null, 9 * (int) Math.ceil(((double) UHCSystem.getTeams().size()) / 9),
				instance.getMessages().color(instance.getMessages().getRawMessage("teams"))));
		this.msgs = instance.getMessages();
		this.teams = new HashMap<Integer, Team>();
	}

	@Override
	public void show() {
		ui.clear();
		teams.clear();

		int slot = 0;
		int offset = 0;
		for(Team team : UHCSystem.getTeams()) {
			ArrayList<String> lore = new ArrayList<String>();
			lore.add(msgs.color(msgs.getRawMessage("members")));

			for(UHCPlayer p : team.getMembers()) {
				ChatColor color = (p.equals(viewer)) ? Globals.CLIENT_COLOR : ChatColor.GREEN;
				lore.add(color + p.getBukkitPlayer().getDisplayName());
			}

			lore.add(" ");

			String players = msgs.getRawMessage("teamRoster");
			players = players.replaceAll("\\{members\\}", String.valueOf(team.getMembers().size()));
			players = players.replaceAll("\\{maxMembers\\}", String.valueOf(main.getProfile().getTeamSize()));
			lore.add(msgs.color(players));

			if(team.getMembers().size() < main.getProfile().getTeamSize()) {
				lore.add(msgs.color(msgs.getRawMessage("available")));
			}else {
				lore.add(msgs.color(msgs.getRawMessage("full")));
			}

			ItemStack item = UHCSystem.nameAndLoreItem(team.getIcon(), msgs.color(UHCSystem.getTeamNameWithPrefix(team)), lore);

			ui.setItem((slot + offset), item);
			teams.put((slot + offset), team);

			if((slot + 1) < 9) {
				slot++;
			}else if((slot + 1) >= 9) {
				slot = 0;
				offset += 9;
			}
		}

	}

	@Override
	public void closed() {
		ui.clear();
		teams.clear();
	}

	@Override
	public void clickPerformed(InventoryClickEvent evt) {
		int slot = evt.getSlot();
		Team team = teams.get(slot);

		if(!main.getProfile().usesRandomTeams()) {
			if(team != null && team.getMembers().size() < main.getProfile().getTeamSize()) {
				Team pTeam = viewer.getTeam();
				if(pTeam != null) pTeam.removeMember(viewer);

				viewer.setTeam(team);
				team.addMember(viewer);
				main.getServer().getPluginManager().callEvent(new UHCPlayerChangeTeamEvent(viewer, team, pTeam));
			}
		}
	}

}
