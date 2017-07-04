package io.skypvp.uhc.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.skypvp.uhc.SkyPVPUHC;
import io.skypvp.uhc.player.UHCPlayer;

public class OutOfCombatRequirement extends InGameRequirement {

	@Override
	public boolean isReached(CommandSender sender) {
		if(super.isReached(sender)) {
			Player p = (Player) sender;
			UHCPlayer uhcPlayer = SkyPVPUHC.get().getOnlinePlayers().get(p.getUniqueId());

			return (!uhcPlayer.isInCombat());
		}

		return false;
	}

	@Override
	public void onFailed(CommandSender sender) {
		if(sender instanceof Player) {
			sender.sendMessage(SkyPVPUHC.get().getMessages().getMessage("mustnt-be-in-combat"));
		}else {
			super.onFailed(sender);
		}
	}
}
