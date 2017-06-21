package io.skypvp.uhc.command;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.command.CommandSender;

public abstract class CommandBase {

	// The actual command string.
	protected final String command;

	// The aliases that can be used to execute this command.
	private final HashSet<String> aliases;

	// The subcommands that could be executed from this one.
	protected final HashSet<CommandBase> subCommands;

	// The requirements to execute the command.
	protected final HashSet<Requirement> requirements;

	// The arguments and if they're required.
	protected final LinkedHashMap<String, Boolean> args;

	// The description of the plugin.
	private String description;

	// How the command is to be used.
	protected String usage;

	public CommandBase(final String cmd) {
		this.command = cmd;
		this.aliases = new HashSet<String>();
		this.subCommands = new HashSet<CommandBase>();
		this.requirements = new HashSet<Requirement>();
		this.args = new LinkedHashMap<String, Boolean>();
		this.description = null;
		this.usage = null;
	}

	/**
	 * Parses the usage string.
	 */

	protected void parseUsage() {
		this.usage = String.format("/%s", command);

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
	 * Adds a requirement to the command.
	 * @param Requirement requirement
	 */

	public void addRequirement(final Requirement requirement) {
		this.requirements.add(requirement);
	}

	/**
	 * Removes a requirement to the command.
	 * @param Requirement requirement
	 */

	public boolean removeRequirement(final Requirement requirement) {
		return requirements.contains(requirement) ? requirements.remove(requirement) : false;
	}

	/**
	 * Adds an argument to the HashMap.
	 * @param String name the name of the argument.
	 * @param required if the argument is optional or not.
	 */

	public void addArg(final String name, final boolean required) {
		this.args.put(name, required);
		this.parseUsage();
	}

	/**
	 * Removes an argument from the HashMap.
	 * @param String name the name of the argument to return.
	 * @return true/false flag if the argument was removed.
	 */

	public boolean removeArg(final String name) {
		// Checks if the argument exists.
		final boolean removed = this.args.containsKey(name);

		if(removed) {
			// Remove the argument from the args.
			this.args.remove(name);

			// Let's update the usage.
			this.parseUsage();
		}

		return removed;
	}

	/**
	 * Returns all the arguments for the command.
	 * @return LinkedHashMap<String, Boolean>
	 */

	public LinkedHashMap<String, Boolean> getArgs() {
		return this.args;
	}

	/**
	 * Set the aliases to a defined set.
	 * @param HashSet<String> aliases
	 */

	public void setAliases(final HashSet<String> setAliases) {
		this.aliases.clear();

		for(String alias : setAliases) {
			addAlias(alias);
		}
	}

	/**
	 * Adds an alias to the command.
	 * @param String alias
	 */

	public void addAlias(final String alias) {
		if(aliases.size() == 0 && usage == null) {
			this.usage = String.format("/%s", command);
		}
		this.aliases.add(alias);
	}

	/**
	 * Removes an alias from the command.
	 * @param String alias
	 */

	public void removeAlias(final String alias) {
		this.aliases.remove(alias);
	}


	/**
	 * Sets the description of the command.
	 * @param String desc
	 */

	public void setDescription(final String desc) {
		this.description = desc;
	}


	/**
	 * Gets the description of the command.
	 * @return String desc
	 */

	public String getDescription() {
		return this.description;
	}

	/**
	 * Sets the usage of the command.
	 * @param String usage
	 */

	public void setUsage(final String usage) {
		this.usage = usage;
	}


	/**
	 * Gets the usage of the command.
	 * @return String usage
	 */

	public String getUsage() {
		return this.usage;
	}

	/**
	 * This is called when all the requirements have been met and the command is being ran.
	 * @param CommandSender sender
	 * @param String[] args
	 */

	public abstract void run(final CommandSender sender, final String[] args);

	/**
	 * Returns true if all the requirements have been met.
	 * @param CommandSender sender
	 * @return true/false flag.
	 */

	public boolean canExecute(final CommandSender sender, final String[] arguments) {
		// Make sure all the requirements are reached.
		for(final Requirement req : requirements) {
			if(!req.isReached(sender)) {
				req.onFailed(sender);
				return false;
			}
		}

		// Make sure we have enough arguments.
		if(arguments != null && arguments.length < getRequiredArguments()) {
			// Send the sender that they don't have enough arguments.
			return false;
		}
		return true;
	}

	// 
	public int getRequiredArguments() {
		int requiredArgs = 0;

		for(Map.Entry<String, Boolean> entry : args.entrySet()) {
			if(entry.getValue()) {
				requiredArgs += 1;
			}
		}
		return requiredArgs;
	}

	/**
	 * Returns if an alias if an alias of the command.
	 * @param alias
	 * @return true/false flag
	 */

	public boolean isAlias(final String alias) {
		return aliases.contains(alias);
	}

	/**
	 * Adds a subcommand.
	 * @param CommandBase cmd
	 */

	public void addSubCommand(final CommandBase cmd) {
		this.subCommands.add(cmd);
	}

	/**
	 * Gets the sub command if one exists.
	 * @param String cmdLbl
	 * @return CommandBase cmd
	 */

	public CommandBase getSubCommand(final String cmdLbl) {
		for(final CommandBase cmd : subCommands) {
			if(cmd.getCommand().equalsIgnoreCase(cmdLbl) || cmd.isAlias(cmdLbl)) {
				return cmd;
			}
		}
		return null;
	}

	public boolean handleSubCommand(final CommandSender sender, final String[] args) {
		if(args != null && args.length > 0) {
			final CommandBase subCommand = getSubCommand(args[0]);
			if(subCommand != null) {
				final String[] newArgs = new String[args.length - 1];
				if((args.length - 1) > 0) {
					int currentArg = 1;
					for(final String arg : args) {
						newArgs[currentArg] = arg;
						currentArg++;
					}
				}
				subCommand.run(sender, newArgs);
				return true;
			}
		}
		return false;
	}

	/**
	 * Gets the requirements.
	 * @return HashSet<Requirement>
	 */

	public HashSet<Requirement> getRequirements() {
		return this.requirements;
	}

	/**
	 * Returns the command.
	 * @return
	 */

	public String getCommand() {
		return this.command;
	}
}