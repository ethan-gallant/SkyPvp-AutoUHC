package io.skypvp.uhc.command;

import java.util.HashSet;
import java.util.Map;

import org.bukkit.command.CommandSender;

public abstract class SubCommandBase extends CommandBase {

	// The parent command.
	private final CommandBase parentCommand;

	public SubCommandBase(final String command, final CommandBase parentCommand) {
		super(command);
		this.parentCommand = parentCommand;
	}

	public CommandBase getParentCommand() {
		return this.parentCommand;
	}

	public void parseUsage() {
		this.usage = String.format("/%s %s", parentCommand.getCommand(), getCommand());

		if(subCommands.size() > 0) {
			this.usage = this.usage.concat(" <command>");
		}

		for(Map.Entry<String, Boolean> entry : args.entrySet()) {
			final String name = entry.getKey();
			final boolean required = entry.getValue();

			if(required) {
				this.usage = this.usage.concat(String.format(" {%s}", name));
			}else {
				this.usage = this.usage.concat(String.format(" [%s]", name));
			}
		}
	}

	/**
	 * Returns true if all the requirements have been met.
	 * @param CommandSender sender
	 * @return true/false flag.
	 */

	@Override
	public boolean canExecute(final CommandSender sender, final String[] arguments) {
		// Make sure all the requirements are reached.
		HashSet<Requirement> allRequirements = requirements;
		allRequirements.addAll(parentCommand.getRequirements());

		for(final Requirement req : allRequirements) {
			if(!req.isReached(sender)) {
				req.onFailed(sender);
				return false;
			}
		}

		// Make sure we have enough arguments.
		if(arguments != null && arguments.length < getRequiredArguments() || arguments != null && arguments.length > args.size()) {
			// Send the sender that they don't have enough arguments.
			return false;
		}
		return true;
	}

	@Override
	public abstract void run(CommandSender sender, String[] args);
}
