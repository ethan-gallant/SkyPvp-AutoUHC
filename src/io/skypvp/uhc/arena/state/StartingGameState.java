package io.skypvp.uhc.arena.state;

import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.DisplaySlot;

import io.skypvp.uhc.SkyPVPUHC;
import io.skypvp.uhc.UHCScoreboard;
import io.skypvp.uhc.UHCSystem;
import io.skypvp.uhc.player.UHCPlayer;
import io.skypvp.uhc.player.UHCPlayer.PlayerState;

public class StartingGameState extends TimedGameState {

    // The last second displayed to the player.
    private int lastSecondShown;

    public StartingGameState(SkyPVPUHC instance, GameStateManager stateMgr) {
        super(instance, stateMgr);
        this.lastSecondShown = -1;
    }

    public void run() {
        if(timer.getMinutes() == 0 && timer.getSeconds() <= 5) {
            // Because this method is called every tick, we need to make sure
            // that we haven't shown the same second. (1 second = 20 ticks)
            if(lastSecondShown != timer.getSeconds()) {
                String msg = main.getMessages().getRawMessage("game-will-start");
                msg = msg.replaceAll("\\{seconds\\}", String.valueOf(timer.getSeconds()));
                main.getMessages().color(msg);
                UHCSystem.broadcastMessageAndSound(msg, main.getSettings().getCountdownSound());
                lastSecondShown = timer.getSeconds();
            }
        }
    }

    public void onEnter() {
        super.onEnter();
        String msg = main.getMessages().getMessage("lobby-timer-begun");
        UHCSystem.broadcastMessageAndSound(msg, main.getSettings().getStateUpdateSound());
        this.lastSecondShown = -1;
    }

    public void onExit() {
        // Let's make it sunny!
        main.getWorldHandler().getGameWorld().getCBWorld().setTime(500L);

        // Let's handle the players.
        for(UHCPlayer player : main.getOnlinePlayers().values()) {
            player.setState(PlayerState.FROZEN);
            player.setInGame(true);
            player.getBukkitPlayer().getInventory().clear();

            // We need to make sure that player is assigned to a team if this is
            // a team match and we're not using random teams.
            if(main.getProfile().isTeamMatch() && !main.getProfile().usesRandomTeams()) {
                UHCSystem.assignPlayerToRandomTeam(player);
            }

            for(ItemStack item : main.getProfile().getStartingItems()) {
                player.getBukkitPlayer().getInventory().addItem(item.clone());
            }

            if(player.getTeam() != null) {
                player.getTeam().giveGear(player);

                // Let's default to having team chat on.
                player.setInTeamChat(true);
            }

            UHCScoreboard scoreboard = new UHCScoreboard(main, "gameScoreboard", DisplaySlot.SIDEBAR);
            scoreboard.generate(player);
            player.setScoreboard(scoreboard);
        }

        main.getGame().setupScenarios();
    }

    public void onFailure() {
        String msg = main.getMessages().getMessage("not-enough-players");
        UHCSystem.broadcastMessageAndSound(msg, main.getSettings().getErrorSound());
    }

}
