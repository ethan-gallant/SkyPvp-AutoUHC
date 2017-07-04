package io.skypvp.uhc.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.skypvp.uhc.SkyPVPUHC;

public class PlayerRequirement extends Requirement {

	@Override
	public boolean isReached(CommandSender sender) {
		return (sender instanceof Player);
	}

	@Override
	public void onFailed(CommandSender sender) {
		sender.sendMessage(SkyPVPUHC.get().getMessages().getMessage("must-be-player"));
	}

}
