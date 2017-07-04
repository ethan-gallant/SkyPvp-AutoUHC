package io.skypvp.uhc.arena;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.wimbli.WorldBorder.BorderData;

import io.skypvp.uhc.Globals;
import io.skypvp.uhc.SkyPVPUHC;
import io.skypvp.uhc.UHCSystem;
import io.skypvp.uhc.WorldHandler;
import io.skypvp.uhc.player.UHCPlayer;
import io.skypvp.uhc.player.UHCPlayer.PlayerState;
import io.skypvp.uhc.scenario.Scenario;
import io.skypvp.uhc.scenario.ScenarioType;
import net.md_5.bungee.api.ChatColor;

public class UHCGame {

    final SkyPVPUHC main;
    private HashSet<Scenario> scenarios;
    
    private int playersAlive;
    private int teamsAlive;

    /**
     * Creates a new container for handling scenarios and
     * various temporary match variables.
     * @param {@link SkyPVPUHC} instance of the main class of the plugin.
     */

    public UHCGame(SkyPVPUHC instance) {
        this.main = instance;
        this.scenarios = new HashSet<Scenario>();
        this.playersAlive = 0;
        this.teamsAlive = 0;
    }

    /**
     * Sets up the scenarios inside of our "scenarios" {@link HashSet}.
     * NOTE: Should only be called once per match.
     * This method uses reflection to create new instances.
     */

    public void setupScenarios() {
        // Let's load our scenarios.
        scenarios.clear();

        for(ScenarioType type : main.getProfile().getScenarios()) {
            Class<? extends Scenario> clazz = null;
            try {
                clazz = ScenarioType.getScenarioClassByType(type);
                Object[] constrNeeds = {main};
                Scenario scenario = clazz.getDeclaredConstructor(SkyPVPUHC.class).newInstance(constrNeeds);
                scenarios.add(scenario);
                main.sendConsoleMessage(ChatColor.DARK_GREEN + String.format("Successfully created new %s instance.", scenario.getType().name()));
            } catch (IllegalArgumentException e) {
                main.sendConsoleMessage(main.getMessages().color(
                        String.format("&cERROR: &4Could not instantiate new Scenario from type. Error: %s", 
                                e.getMessage())));
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }

        spawnPlayers();
    }

    /**
     * Activates all scenarios inside of the "scenarios" {@link HashSet}.
     * NOTE: This is usually called at the beginning of the Grace Period state.
     */

    public void activateScenarios() {
        for(Scenario scenario : scenarios) {
            scenario.activate();
        }
    }

    /**
     * Spawns all {@link UHCPlayer}s somewhere random inside of the
     * game world created by the {@link WorldHandler}
     */

    public void spawnPlayers() {
        if(!main.getProfile().isTeamMatch()) {
            for(UHCPlayer p : main.getOnlinePlayers().values()) {
                Player player = p.getBukkitPlayer();
                BorderData border = main.getWorldBorder().getWorldBorder(Globals.GAME_WORLD_NAME);
                double minX = border.getX() - (border.getRadiusX() - 10);
                double maxX = border.getX() + (border.getRadiusX() - 10);
                double minZ = border.getZ() - (border.getRadiusZ() - 10);
                double maxZ = border.getZ() + (border.getRadiusZ() - 10);
                player.teleport(UHCSystem.getRandomSpawnPoint(minX, maxX, minZ, maxZ));
            }
        }else {
            for(Team team : UHCSystem.getTeams()) {
                BorderData border = main.getWorldBorder().getWorldBorder(Globals.GAME_WORLD_NAME);
                double minX = border.getX() - (border.getRadiusX() - 10);
                double maxX = border.getX() + (border.getRadiusX() - 10);
                double minZ = border.getZ() - (border.getRadiusZ() - 10);
                double maxZ = border.getZ() + (border.getRadiusZ() - 10);

                Location spawn = UHCSystem.getRandomSpawnPoint(minX, maxX, minZ, maxZ);
                UHCPlayer[] members = team.getMembers().toArray(new UHCPlayer[team.getMembers().size()]);
                if(members.length > 0) {
                    members[0].getBukkitPlayer().teleport(spawn);

                    for(int i = 1; i < members.length; i++) {
                        minX = spawn.getX() - 5;
                        maxX = spawn.getX() + 5;
                        minZ = spawn.getZ() - 5;
                        maxZ = spawn.getZ() + 5;
                        members[i].getBukkitPlayer().teleport(UHCSystem.getRandomSpawnPoint(minX, maxX, minZ, maxZ));
                    }
                }
            }
        }
    }

    /**
     * Resets the data members associated with this class.
     */

    public void reset() {
        // Let's clear up the scenarios.
        for(Scenario scenario : scenarios) {
            scenario.deactivate();
        }

        // Let's reset the variables for the next match.
        scenarios.clear();
        teamsAlive = 0;
        playersAlive = 0;
    }

    /**
     * Checks if a {@link ScenarioType} is active.
     * @param {@link ScenarioType} type
     * @return if the scenario is active or not (true/false)
     */

    public boolean isScenarioActive(ScenarioType type) {
        for(Scenario scenario : scenarios) {
            if(scenario.getType() == type) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns a {@link HashSet} of {@link Scenario}s
     * @return HashSet of Scenarios
     */

    public HashSet<Scenario> getScenarios() {
        return this.scenarios;
    }
    
    /**
     * Sets the amount of teams alive.
     * @param int teams
     */
    
    public void setAliveTeams(int teams) {
        this.teamsAlive = teams;
    }
    
    /**
     * Recalculates the amount of teams currently alive.
     * NOTE: This will set the amount of teams alive to 0 before recalculating.
     */
    
    public void recalculateAliveTeams() {
        this.teamsAlive = 0;
        
        for(Team team : UHCSystem.getTeams()) {
            for(UHCPlayer p : team.getMembers()) {
                if(p.getState() == PlayerState.ACTIVE) {
                    teamsAlive++;
                    break;
                }
            }
        }
    }
    
    public int getAliveTeams() {
        return this.teamsAlive;
    }
    
    /**
     * Sets the amount of players alive.
     * @param int players
     */
    
    public void setAlivePlayers(int players) {
        this.playersAlive = players;
    }
    
    /**
     * Recalculates the amount of players currently alive.
     * NOTE: This will set the amount of players alive to 0 before recalculating.
     */
    
    public void recalculateAlivePlayers() {
        this.playersAlive = 0;
        Iterator<UHCPlayer> iterator = main.getOnlinePlayers().values().iterator();
        
        while(iterator.hasNext()) {
            UHCPlayer next = iterator.next();
            
            if(next.getState() == PlayerState.ACTIVE) {
                playersAlive++;
            }
        }
    }
    
    /**
     * Fetches the amount of players currently alive.
     * @return int
     */
    
    public int getAlivePlayers() {
        return this.playersAlive;
    }

    /**
     * Returns a {@link HashSet} of {@link UHCPlayer}s who are
     * currently in-game.
     * @return
     */

    public HashSet<UHCPlayer> getPlayers() {
        HashSet<UHCPlayer> players = new HashSet<UHCPlayer>();
        for(UHCPlayer p : main.getOnlinePlayers().values()) {
            if(p.isInGame()) players.add(p);
        }

        return players;
    }

}
