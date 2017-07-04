package io.skypvp.uhc.arena.state;

import org.bukkit.entity.Player;

import com.onarandombox.MultiverseCore.api.MVWorldManager;

import io.skypvp.uhc.Globals;
import io.skypvp.uhc.SkyPVPUHC;
import io.skypvp.uhc.UHCSystem;
import io.skypvp.uhc.WorldHandler;
import net.md_5.bungee.api.ChatColor;

public class SetupGameState extends GameState {

	final WorldHandler worldHdl;
	final MVWorldManager worldMgr;

	public SetupGameState(SkyPVPUHC instance, GameStateManager stateMgr) {
		super(instance, "setup", stateMgr);
		this.worldHdl = main.getWorldHandler();
		this.worldMgr = worldHdl.getWorldManager();
	}

	public void onEnter() {
		main.sendConsoleMessage(ChatColor.YELLOW + "Setting up UHC system for play... Please wait...");

		// We are doing work to prepare the server for a new game.
		// We cannot have any players online to do so.
		for(Player p : main.getServer().getOnlinePlayers()) {
			stateMgr.sendPlayerToRandomLobby(p);
		}

		// Let's delete the game world.
		if(worldMgr.getMVWorld(Globals.GAME_WORLD_NAME) != null) {
			worldHdl.deleteGameWorld();
		}
	}

	public void run() {
	    // Let's reset some variables
	    stateMgr.setForceStartPlayers(-1);
	    stateMgr.setAdminForcedStart(false);
	    
		// Let's generate the game world.
		worldHdl.createGameWorld();

		// Let's reset the game system.
		UHCSystem.reset();
	}

	/**
	 * We always want to move on away from this GameState
	 */

	public boolean canMoveOn() {
		return true;
	}


	public boolean canContinue() {
		return false;
	}

	public void onExit() {}

}
