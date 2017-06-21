package io.skypvp.uhc;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.configuration.ConfigurationSection;

public class Messages {

	final SkyPVPUHC main;
	final Settings settings;
	final ConfigurationSection msgs;

	public Messages(SkyPVPUHC instance) {
		this.main = instance;
		this.settings = instance.getSettings();
		this.msgs = settings.getMessagesSection();
	}

	public String getRawMessage(String msgKey) {
		String msg = msgs.getString(msgKey);
		if(msg == null) {
			main.sendConsoleMessage(ChatColor.DARK_RED + "Encountered an error while obtaining predefined chat message.");
			throw new IllegalArgumentException(String.format("Message Key %s does not exist.", msgKey));
		}

		return msg;
	}

	public String getMessage(String msgKey) {
		String msg = getRawMessage(msgKey);
		if(settings.wantPrefixMessages()) {
			msg = getPrefix().concat(" ").concat(msg);
		}

		return color(msg);
	}

	public String constructMessage(String message) {
		if(settings.wantPrefixMessages()) {
			message = getPrefix().concat(" ").concat(message);
		}

		return color(message);
	}

	public String getColoredString(String msgKey) {
		String msg = msgs.getString(msgKey);
		if(msg == null) {
			main.sendConsoleMessage(ChatColor.DARK_RED + "Encountered an error while obtaining predefined chat message.");
			throw new IllegalArgumentException(String.format("Message Key %s does not exist.", msgKey));
		}

		return color(msg);
	}

	public String color(String message) {
		return ChatColor.translateAlternateColorCodes('&', (message != null) ? message : "&c!!!ERROR!! COULD NOT OBTAIN MESSAGE");
	}

	public String getPrefix() {
		return getRawMessage("prefix");
	}
}
