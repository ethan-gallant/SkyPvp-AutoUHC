package io.skypvp.uhc.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.skypvp.uhc.SkyPVPUHC;

public class InTeamGameRequirement extends InGameRequirement {

	@Override
	public boolean isReached(CommandSender sender) {
		return super.isReached(sender) && SkyPVPUHC.get().getProfile().isTeamMatch();
	}

	@Override
	public void onFailed(CommandSender sender) {
		if(sender instanceof Player) {
			sender.sendMessage(SkyPVPUHC.get().getMessages().getMessage("must-be-in-team-match"));
		}else {
			super.onFailed(sender);
		}
	}
}
