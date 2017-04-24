package io.skypvp.uhc.command;

import io.skypvp.uhc.SkyPVPUHC;
import io.skypvp.uhc.arena.UHCGame.GameState;
import io.skypvp.uhc.player.UHCPlayer;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VoteStartCommand extends CommandBase {
    
    public VoteStartCommand() {
        super("votestart");
        
        this.addAlias("vs");
        this.addRequirement(new PlayerRequirement());
        this.addRequirement(new PermissionRequirement("uhc.votestart"));
    }

    @Override
    public void run(CommandSender sender, String[] args) {
        // This is a safe casting because this method isn't called unless a player is executing this command.
        // Refer to: PlayerRequirement.java.
        Player p = (Player) sender;
        UHCPlayer uhcPlayer = SkyPVPUHC.onlinePlayers.get(p.getUniqueId());
        
        if(uhcPlayer != null) {
            if(SkyPVPUHC.game.getState() == GameState.WAITING) {
                
            }else {
                sender.sendMessage(ChatColor.RED + "The game is already in progress!");
            }
        }else {
            sender.sendMessage(ChatColor.RED + "An unexpected error has occurred.");
            throw new NullPointerException("Player tried to vote start while a UHCPlayer instance for them does not exist!");
        }
    }

}
