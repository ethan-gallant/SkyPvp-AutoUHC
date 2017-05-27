package io.skypvp.uhc.command;

import io.skypvp.uhc.SkyPVPUHC;

import org.bukkit.command.CommandSender;

public class UHCBaseCommand extends CommandBase {
    
    final SkyPVPUHC main;
    
    public UHCBaseCommand(SkyPVPUHC instance) {
        super("ultrahardcore");
        this.main = instance;
        
        this.addAlias("uhc");
    }

    @Override
    public void run(CommandSender sender, String[] args) {
        parseUsage();
        sender.sendMessage(getUsage());
    }

}
