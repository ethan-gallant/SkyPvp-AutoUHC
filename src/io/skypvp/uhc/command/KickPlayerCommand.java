package io.skypvp.uhc.command;

import io.skypvp.uhc.SkyPVPUHC;
import io.skypvp.uhc.arena.Profile;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KickPlayerCommand extends SubCommandBase {
    
    final SkyPVPUHC main;
    final Profile profile;
    
    public KickPlayerCommand(SkyPVPUHC instance, UHCBaseCommand baseCmd) {
        super("kick", baseCmd);
        this.main = instance;
        this.profile = main.getProfile();
        
        this.setDescription(ChatColor.YELLOW + "Kicks a player back to the lobby and prevents them from rejoining.");
        this.addArg("player", true);
    }
    
    /**
     * Processes a request to kick a {@link org.bukkit.entity.Player} from the match.
     * If a {@link org.bukkit.entity.Player} is found with the name provided, they are kicked
     * back to a UHC lobby and prevented from joining for the duration of the match.
     * @param {@link org.bukkit.command.CommandSender} sender
     * @param String[] args
     */
    
    private void handleKickRequest(CommandSender sender, String[] args) {
        Player pSender = (sender instanceof Player) ? (Player) sender : null;
        
        if(pSender != null && args[0].equalsIgnoreCase(pSender.getDisplayName()) || args[0].equalsIgnoreCase(pSender.getName())) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cError: &4You cannot kick yourself!"));
            return;
        }
        
        for(Player p : main.getServer().getOnlinePlayers()) {
            if(p.getDisplayName().equalsIgnoreCase(args[0]) || p.getName().equalsIgnoreCase(args[0])) {
                // This is our person to kick!
                main.getGameStateManager().sendPlayerToRandomLobby(p);
                
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("&d&s &awas kicked from the match.", p.getDisplayName())));
                return;
            }
        }
        
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cError: &4Player not found!"));
    }

    @Override
    public void run(CommandSender sender, String[] args) {
        Player pSender = (sender instanceof Player) ? (Player) sender : null;
        
        if(sender.hasPermission("uhc.admin") || sender.isOp()) {
            // Whoever the sender is has permission to run this command.
            handleKickRequest(sender, args);
        }else if(profile.getOwner() != null && pSender != null && profile.getOwner().equals(pSender.getUniqueId().toString())) {
            // This player is the creator of this match, they have permission.
            handleKickRequest(sender, args);
        }else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                    "&cError: &4You don't have permission to execute that command."));
        }
    }

}
