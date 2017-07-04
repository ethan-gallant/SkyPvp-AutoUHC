package io.skypvp.uhc.command;

import io.skypvp.uhc.SkyPVPUHC;

import java.util.Arrays;
import java.util.HashSet;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

public class CommandPool implements Listener {

	final SkyPVPUHC main;
	private final UHCBaseCommand uhcBaseCmd;
	private final KickPlayerCommand kickPlayerCmd;
	private final VoteStartCommand vsCmd;
	private final TeamChatCommand tcCmd;
	private final SurfaceCommand surfCmd;
	private static HashSet<CommandBase> commands;

	public CommandPool(final SkyPVPUHC instance) {
		this.main = instance;
		CommandPool.commands = new HashSet<CommandBase>();

		// Let's setup our base command.
		uhcBaseCmd = new UHCBaseCommand(instance);
		commands.add(uhcBaseCmd);

		// Let's setup the kick command.
		kickPlayerCmd = new KickPlayerCommand(instance, uhcBaseCmd);
		uhcBaseCmd.addSubCommand(kickPlayerCmd);

		// Let's setup the vote start command.
		vsCmd = new VoteStartCommand();
		commands.add(vsCmd);

		// Let's setup the surface command.
		this.surfCmd = new SurfaceCommand(instance);
		commands.add(surfCmd);

		// Let's add the team chat command.
		tcCmd = new TeamChatCommand();
		commands.add(tcCmd);
	}
	
	/**
	 * Processes match resets to prevent memory leaks.
	 */
	
	public void processMatchReset() {
	    surfCmd.getUsers().clear();
	}

	@EventHandler
	public void onPlayerExecuteCommand(final PlayerCommandPreprocessEvent evt) {
		final Player player = evt.getPlayer();
		boolean handled = handleCommand(player, evt.getMessage());
		
		// We didn't list our commands in the plugin.yml, so if we handled
		// the command internally, let's just cancel the event so the server doesn't
		// send an "unknown command" message.
		if(handled) evt.setCancelled(true);
	}

	private boolean handleCommand(final CommandSender sender, String msg) {
		String cmd = msg;
		String[] args = null;

		if(msg.contains(" ") && !msg.endsWith(" ")) {
			final String[] elements = msg.split(" ");
			cmd = elements[0].substring(1, elements[0].length());
			args = Arrays.copyOfRange(elements, 1, elements.length);
		}else {
			cmd = cmd.substring(1, cmd.length());
		}

		for(final CommandBase command : commands) {
			if(command.getCommand().equalsIgnoreCase(cmd) || command.isAlias(cmd)) {
				if(command.canExecute(sender, args)) {
					if(!command.handleSubCommand(sender, args)) {
						command.run(sender, args);
					}

					return true;
				}else {
					return false;
				}
			}
		}

		return false;
	}

	@EventHandler
	public void onServerExecuteCommand(final ServerCommandEvent evt) {
		final CommandSender sender = evt.getSender();
		handleCommand(sender, evt.getCommand());
	}

	public static void addCommand(final CommandBase cmd) {
		CommandPool.commands.add(cmd);
	}

	public HashSet<CommandBase> getCommands() {
		return CommandPool.commands;
	}

}
