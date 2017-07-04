package io.skypvp.uhc.command;

import org.bukkit.command.CommandSender;

import io.skypvp.uhc.SkyPVPUHC;

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
		sender.sendMessage(SkyPVPUHC.get().getMessages().getMessage("no-permission"));
	}

	public String getPermission() {
		return this.permission;
	}
}
