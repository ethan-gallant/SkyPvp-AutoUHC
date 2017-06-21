package io.skypvp.uhc.player;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import io.skypvp.uhc.Globals;
import io.skypvp.uhc.UHCScoreboard;
import io.skypvp.uhc.arena.Team;
import io.skypvp.uhc.menu.Menu;
import io.skypvp.uhc.util.ConfigUtils;

public class UHCPlayer {

    public enum PlayerState {
        ACTIVE, FROZEN, SPECTATING
    }

    final UUID uuid;
    private PlayerState state;

    // These are database statistics.
    private int gamesPlayed;
    private int gamesWon;
    private int kills;
    private int deaths;

    // These are temporary game statistics.
    // Kills this player has gotten during the current match
    private int gameKills;

    // Here are variables that are for game management.
    private Menu activeMenu;
    private Team team;
    private UHCScoreboard scoreboard;
    private boolean inGame;
    private boolean inTeamChat;

    // Variables to handle combat-tagging.
    private UHCPlayer combatTagger;

    // The time in milliseconds when this player was tagged.
    private long tagTimeMs;

    // The last time the player was warned about being frozen.
    private long freezeCooldownMs;

    /**
     * Creates a new UHC representation of a {@link Player}.
     * @param {@link UUID} id - UUID of the player.
     * @param int gamesPlayed - The amount of games played.
     * @param int gamesWon - The amount of games won.
     * @param int kills - The amount of kills.
     * @param int deaths - The amount of deaths.
     */

    public UHCPlayer(UUID id, int gamesPlayed, int gamesWon, int kills, int deaths) {
        this.uuid = id;
        this.state = PlayerState.ACTIVE;
        this.gamesPlayed = gamesPlayed;
        this.gamesWon = gamesWon;
        this.kills = kills;
        this.gameKills = 0;
        this.deaths = deaths;
        this.activeMenu = null;
        this.team = null;
        this.scoreboard = null;
        this.inGame = true;
        this.inTeamChat = false;
        this.combatTagger = null;
        this.tagTimeMs = 0;
        this.freezeCooldownMs = System.currentTimeMillis();
    }

    /**
     * Switches the player to the {@link PlayerState.SPECTATING} state.
     * Launches the player upward with a flight effect.
     * Changes player {@link GameMode} to {@link GameMode.ADVENTURE}.
     */

    public void startSpectating() {
        final Player p = getBukkitPlayer();

        // Switch our state to "SPECTATING" and launch
        // us upward.
        setState(PlayerState.SPECTATING);
        p.setVelocity(new Vector(0, 1, 0));
        p.setAllowFlight(true);

        // We need to show ourselves to other spectators and hide from active players.
        for(UHCPlayer player : ConfigUtils.main.getOnlinePlayers().values()) {
            if(player.getState() != PlayerState.SPECTATING) {
                player.getBukkitPlayer().hidePlayer(p);
                p.showPlayer(player.getBukkitPlayer());
            }else {
                player.getBukkitPlayer().showPlayer(p);
                p.showPlayer(player.getBukkitPlayer());
            }
        }

        // Let's start flying and change our GameMode to "ADVENTURE"
        new BukkitRunnable() {

            public void run() {
                if(p != null) {
                    p.setFlying(true);
                    p.setGameMode(GameMode.ADVENTURE);
                }
            }
        }.runTaskLater(ConfigUtils.main, 10L);
    }

    /**
     * Prepares the player for a match.
     * Clears the player's inventory,
     * resets health to the default 20.0,
     * resets food level to 20.0,
     * clears all potion effects,
     * and disables team chat.
     */

    public void prepareForGame() {
        Player p = getBukkitPlayer();
        PlayerInventory inv = p.getInventory();

        // Let's clear out our inventory.
        inv.clear();
        inv.setHelmet(null);
        inv.setChestplate(null);
        inv.setLeggings(null);
        inv.setBoots(null);

        // Let's reset health, food level, and potion effects.
        p.setHealth(p.getMaxHealth());
        p.setFoodLevel(20);
        p.getActivePotionEffects().clear();

        inTeamChat = false;
    }

    /**
     * Sets the player's state.
     * @param {@link PlayerState} state
     */

    public void setState(PlayerState state) {
        this.state = state;
    }

    /**
     * Fetches the current state of this player.
     * @return {@link PlayerState}
     */

    public PlayerState getState() {
        return this.state;
    }

    /**
     * Sets the {@link UHCScoreboard} currently being used by
     * this player.
     * @param {@link UHCScoreboard} board
     */

    public void setScoreboard(UHCScoreboard board) {
        this.scoreboard = board;

        if(board != null) {
            scoreboard.build(getBukkitPlayer());
            getBukkitPlayer().setScoreboard(scoreboard.getBoard());
        }
    }

    /**
     * Fetches the {@link UHCScoreboard} currently being used by
     * this player.
     * @return {@link UHCScoreboard}
     */

    public UHCScoreboard getScoreboard() {
        return this.scoreboard;
    }

    /**
     * Sets the {@link Menu} that is being displayed to this
     * player.
     * NOTE: This DOES NOT open a menu or show it to the player,
     * it simply is a pointer to keep track of a one currently
     * displayed.
     * @param {@link Menu}
     */

    public void setActiveMenu(Menu menu) {
        this.activeMenu = menu;
    }

    /**
     * Obtains the {@link Menu} that is being shown to this player.
     * @return {@link Menu}
     */

    public Menu getActiveMenu() {
        return this.activeMenu;
    }

    /**
     * Sets the {@link Team} that this player is on.
     * @param {@link Team} team
     */

    public void setTeam(Team team) {
        this.team = team;
    }

    /**
     * Fetches the {@link Team} that this player is on.
     * @return {@link Team}
     */

    public Team getTeam() {
        return this.team;
    }

    /**
     * Sets if this player is using team chat or not.
     * @param boolean flag (true/false)
     */

    public void setInTeamChat(boolean flag) {
        this.inTeamChat = flag;
    }

    /**
     * Fetches if this player is using team chat.
     * @return boolean flag (true/false)
     */

    public boolean isInTeamChat() {
        return this.inTeamChat;
    }

    /**
     * Sets if this player is in-game or not.
     * @param boolean flag (true/false)
     */

    public void setInGame(boolean flag) {
        this.inGame = flag;
    }

    /**
     * Fetches if this player is in-game or not.
     * @return boolean flag (true/false)
     */

    public boolean isInGame() {
        return this.inGame;
    }

    /////////////////////////////////////////////////////////
    //          Methods relating to player statistics      //
    /////////////////////////////////////////////////////////

    /**
     * Increments the amount of games played.
     * NOTE: This should be called once a match is complete.
     */

    public void incrementGamesPlayed() {
        gamesPlayed++;
    }

    /**
     * Fetches the amount of games played.
     * @return int
     */

    public int getGamesPlayed() {
        return this.gamesPlayed;
    }

    /**
     * Increments the amount of games won.
     */

    public void incrementGamesWon() {
        gamesWon++;
    }

    /**
     * Fetches the amount of games won.
     * @return int
     */

    public int getGamesWon() {
        return this.gamesWon;
    }

    /**
     * Increments the amount of kills.
     * NOTE: Increments the value returned by {@link #getGameKills()} 
     * if this player is currently in-game.
     */

    public void incrementKills() {
        kills++;
        if(inGame) gameKills++;
    }

    /**
     * Fetches the amount of kills.
     * @return int
     */

    public int getKills() {
        return this.kills;
    }

    /**
     * Fetches the amount of kills completed during this match.
     * NOTE: Read note on {@link #incrementKills()}
     * @return int
     */

    public int getGameKills() {
        return this.gameKills;
    }

    /**
     * Increments the amount of deaths.
     */

    public void incrementDeaths() {
        deaths++;
    }

    /**
     * Fetches the amount of deaths.
     * @return int
     */

    public int getDeaths() {
        return this.deaths;
    }

    /**
     * Fetches this player's kill-death ratio.
     * @return int (kills divided by deaths)
     */

    public int getKillDeathRatio() {
        int d = (deaths > 0) ? deaths : 1;
        return kills / d;
    }

    /**
     * Builds a MySQL statement used to execute a {@link PreparedStatement}
     * @return {@link String}
     */

    public String toMySQLUpdate() {
        return String.format(Globals.TABLE_ENTRY_UPDATE,
                Globals.TABLE_NAME, gamesPlayed, gamesWon, 
                kills, deaths, uuid.toString());
    }

    /////////////////////////////////////////////////////////

    /**
     * Sets the player that this UHCPlayer is in combat with.
     * Expects a valid UHCPlayer instance or null.
     * @param UHCPlayer p
     */

    public void setInCombatWith(UHCPlayer p) {
        this.combatTagger = p;
        this.tagTimeMs = (p != null) ? System.currentTimeMillis() : 0;
    }

    /**
     * Returns if this player is in combat or not.
     * @return true/false flag
     */

    public boolean isInCombat() {
        if(combatTagger == null) return false;

        if(combatTagger != null) {
            if(System.currentTimeMillis() >= tagTimeMs + Globals.COMBAT_TAG_TIME) {
                setInCombatWith(null);
                return false;
            }

            return true;
        }

        return false;
    }

    /**
     * Resets the freeze cooldown to the current
     * System time in milliseconds.
     */

    public void resetFreezeCooldown() {
        this.freezeCooldownMs = System.currentTimeMillis();
    }

    /**
     * Returns the last time (in milliseconds)
     * that this player was warned about being frozen.
     * @return long
     */

    public long getLastFreezeWarning() {
        return this.freezeCooldownMs;
    }

    /**
     * Obtains the {@link Player} object we are
     * representing.
     * @return {@link Player}
     */

    public Player getBukkitPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    /**
     * Obtains the {@link UUID} of the player
     * we are representing.
     * @return {@link UUID}
     */

    public UUID getUUID() {
        return this.uuid;
    }

}
