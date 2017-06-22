package io.skypvp.uhc.command;

import io.skypvp.uhc.SkyPVPUHC;
import io.skypvp.uhc.player.UHCPlayer;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
			sender.sendMessage(ChatColor.RED + "You must not be in combat to execute that command.");
		}else {
			super.onFailed(sender);
		}
	}
}
