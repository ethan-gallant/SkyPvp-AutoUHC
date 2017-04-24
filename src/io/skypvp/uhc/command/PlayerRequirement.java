package io.skypvp.uhc.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlayerRequirement extends Requirement {

	@Override
	public boolean isReached(CommandSender sender) {
		return (sender instanceof Player);
	}

	@Override
	public void onFailed(CommandSender sender) {
		sender.sendMessage("You must be a player to execute this command.");
	}

}
