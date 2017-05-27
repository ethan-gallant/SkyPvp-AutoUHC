package io.skypvp.uhc.arena.state;

import io.skypvp.uhc.Globals;
import io.skypvp.uhc.SkyPVPUHC;
import io.skypvp.uhc.WorldHandler;
import io.skypvp.uhc.timer.TimerUtils;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.entity.Player;

import com.onarandombox.MultiverseCore.api.MVWorldManager;

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
        // Let's generate the game world.
        worldHdl.createGameWorld();
        
        // Let's reset the game system.
        main.getGame().reset();
        
        // Let's setup the lobby timer.
        stateMgr.setTimer(TimerUtils.createTimer(main, "Starting", main.getSettings().getStartTime()));
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

    @Override
    public void onExit() {
        // TODO Auto-generated method stub

    }

}
