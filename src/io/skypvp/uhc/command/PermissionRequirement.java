package io.skypvp.uhc.command;

import org.bukkit.command.CommandSender;
import net.md_5.bungee.api.ChatColor;

public class PermissionRequirement extends Requirement {

	private final String permission;

	public PermissionRequirement(final String perm) {
		this.permission = perm;
	}

	@Override
	public boolean isReached(CommandSender sender) {
		return sender.hasPermission(permission) || sender.isOp();
	}

	@Override
	public void onFailed(CommandSender sender) {
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cError: &4You don't have permission to execute that command."));
	}

	public String getPermission() {
		return this.permission;
	}
}
