package io.skypvp.uhc.command;

import io.skypvp.uhc.SkyPVPUHC;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InTeamGameRequirement extends InGameRequirement {

    @Override
    public boolean isReached(CommandSender sender) {
        return super.isReached(sender) && SkyPVPUHC.game.isTeamMatch();
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
