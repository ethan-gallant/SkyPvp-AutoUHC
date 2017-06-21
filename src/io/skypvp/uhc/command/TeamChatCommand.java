package io.skypvp.uhc.command;

import io.skypvp.uhc.SkyPVPUHC;
import io.skypvp.uhc.player.UHCPlayer;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeamChatCommand extends CommandBase {

	public TeamChatCommand() {
		super("teamchat");

		this.addAlias("tc");
		this.setDescription(ChatColor.YELLOW + "Toggle team chat whilst in a team UHC game.");
		this.addRequirement(new InTeamGameRequirement());
		this.addRequirement(new PermissionRequirement("uhc.teamchat"));
	}

	@Override
	public void run(CommandSender sender, String[] args) {
		// This is a safe casting because this method isn't called unless a player is executing this command.
		// Refer to: InTeamGameRequirement.java.
		Player p = (Player) sender;
		UHCPlayer uhcPlayer = SkyPVPUHC.onlinePlayers.get(p.getUniqueId());

		if(uhcPlayer != null) {
			if(uhcPlayer.isInTeamChat()) {
				uhcPlayer.setInTeamChat(false);
				sender.sendMessage(ChatColor.GREEN + "You are no longer speaking in team chat.");
			}else {
				uhcPlayer.setInTeamChat(true);
				sender.sendMessage(ChatColor.YELLOW + "You are now speaking in team chat.");
			}
		}else {
			sender.sendMessage(ChatColor.RED + "An unexpected error has occurred.");
			throw new NullPointerException("Player tried to toggle team chat while a UHCPlayer instance for them does not exist!");
		}
	}

}
