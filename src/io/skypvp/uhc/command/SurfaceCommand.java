package io.skypvp.uhc.command;

import io.skypvp.uhc.Messages;
import io.skypvp.uhc.SkyPVPUHC;
import io.skypvp.uhc.player.UHCPlayer;

import java.util.ArrayList;
import java.util.UUID;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SurfaceCommand extends CommandBase {

	final SkyPVPUHC main;
	private ArrayList<UUID> users;

	public SurfaceCommand(SkyPVPUHC instance) {
		super("surface");
		this.main = instance;
		this.users = new ArrayList<UUID>();

		this.setDescription(ChatColor.YELLOW + "Teleport to the surface.");
		this.addRequirement(new OutOfCombatRequirement());
		this.addRequirement(new PermissionRequirement("uhc.surface"));
	}

	/**
	 * Gets the highest block from the x and y of the Location entered.
	 * @param Location loc - Location with the x and y coordinates to be used.
	 * @return The highest block or null.
	 */

	public Block getHighestBlock(Location loc) {
		Location checkLoc = new Location(loc.getWorld(), loc.getX(), loc.getWorld().getMaxHeight(), loc.getZ());

		while(checkLoc.getBlock().getType() == Material.AIR) {
			checkLoc = checkLoc.subtract(0, 1, 0);

			if(checkLoc.getBlock().getType() != Material.AIR) {
				break;
			}
		}

		return checkLoc.getBlock();
	}

	@Override
	public void run(CommandSender sender, String[] args) {
		// This is a safe casting because this method isn't called unless a player is executing this command.
		// Refer to: InGameRequirement.java
		Player p = (Player) sender;
		UHCPlayer uhcPlayer = SkyPVPUHC.get().getOnlinePlayers().get(p.getUniqueId());

		if(uhcPlayer != null) {
			final Messages msgs = main.getMessages();

			if(!users.contains(p.getUniqueId())) {
				Block b = getHighestBlock(p.getLocation());
				if(b == p.getLocation().getBlock()) {
					p.sendMessage(msgs.color(msgs.getRawMessage("alreadySurfaced")));
				}else {
					p.teleport(new Location(p.getLocation().getWorld(), b.getLocation().getX(), b.getLocation().getY() + 1.0, 
							b.getLocation().getZ()));
					users.add(p.getUniqueId());
					p.sendMessage(msgs.color(msgs.getRawMessage("teleportedToSurface")));
				}
			}else {
				p.sendMessage(msgs.color(msgs.getRawMessage("surfaceAlreadyUsed")));
			}

		}else {
			sender.sendMessage(ChatColor.RED + "An unexpected error has occurred.");
			throw new NullPointerException("Player tried to jump to the surface, but their UHCPlayer object does not exist!");
		}
	}

	/**
	 * Fetches an ArrayList of UUID of who have used the surface command.
	 * @return ArrayList<UUID>
	 */

	public ArrayList<UUID> getUsers() {
		return this.users;
	}

}
