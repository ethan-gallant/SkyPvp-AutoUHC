package io.skypvp.uhc.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.skypvp.uhc.Messages;
import io.skypvp.uhc.SkyPVPUHC;
import io.skypvp.uhc.arena.state.GameStateManager;
import io.skypvp.uhc.arena.state.LobbyWaitState;
import io.skypvp.uhc.player.UHCPlayer;
import net.md_5.bungee.api.ChatColor;

public class VoteStartCommand extends CommandBase {

	public VoteStartCommand() {
		super("votestart");

		this.addAlias("vs");
		this.setDescription(ChatColor.YELLOW + "Vote to force-start the game.");
		this.addRequirement(new PlayerRequirement());
		this.addRequirement(new PermissionRequirement("uhc.votestart"));
	}

	@Override
	public void run(CommandSender sender, String[] args) {
		// This is a safe casting because this method isn't called unless a player is executing this command.
		// Refer to: PlayerRequirement.java.
		Player p = (Player) sender;
		Messages msgs = SkyPVPUHC.get().getMessages();
		UHCPlayer uhcPlayer = SkyPVPUHC.get().getOnlinePlayers().get(p.getUniqueId());

		if(uhcPlayer != null) {
			GameStateManager gsm = SkyPVPUHC.get().getGameStateManager();
			if(gsm.getActiveState() instanceof LobbyWaitState && gsm.getForceStartPlayers() == -1) {
				((LobbyWaitState) gsm.getActiveState()).voteForForceStart(p.getUniqueId());
			}else {
				sender.sendMessage(msgs.getMessage("force-start-unavailable"));
			}
		}else {
			sender.sendMessage(msgs.getMessage("unexpected-error"));
			throw new NullPointerException("Player tried to vote start while a UHCPlayer instance for them does not exist!");
		}
	}

}
