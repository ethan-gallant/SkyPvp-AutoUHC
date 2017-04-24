package io.skypvp.uhc.command;

import org.bukkit.command.CommandSender;

public abstract class Requirement {
    
    // When the requirement is reached.
    public abstract boolean isReached(final CommandSender sender);
    
    // When the requirement isn't reached.
    public abstract void onFailed(final CommandSender sender);
}
