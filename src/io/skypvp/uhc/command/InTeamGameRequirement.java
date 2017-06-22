package io.skypvp.uhc.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.skypvp.uhc.SkyPVPUHC;
import net.md_5.bungee.api.ChatColor;

public class InTeamGameRequirement extends InGameRequirement {

	@Override
	public boolean isReached(CommandSender sender) {
		return super.isReached(sender) && SkyPVPUHC.get().getProfile().isTeamMatch();
	}

	@Override
	public void onFailed(CommandSender sender) {
		if(sender instanceof Player) {
			sender.sendMessage(ChatColor.RED + "You can only execute that command while in a team game.");
		}else {
			super.onFailed(sender);
		}
	}
}
