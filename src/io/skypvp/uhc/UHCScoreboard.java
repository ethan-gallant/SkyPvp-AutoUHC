package io.skypvp.uhc;

import io.skypvp.uhc.arena.Team;
import io.skypvp.uhc.arena.UHCGame.GameState;
import io.skypvp.uhc.player.UHCPlayer;
import io.skypvp.uhc.timer.MatchTimer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public class UHCScoreboard {
	
	final SkyPVPUHC main;
	final Database database;
	final HashMap<Integer, String> lines;
	private final String title, scoreboardType;
	private DisplaySlot slot;
	private Scoreboard scoreboard;
	private Objective obj;
	
	public UHCScoreboard(final SkyPVPUHC instance, final String scoreboard, final DisplaySlot slot) {
		this.main = instance;
		this.title = main.getSettings().getScoreboardHeader();
		this.scoreboardType = scoreboard;
		this.database = instance.getSettings().getDatabase();
		this.lines = new HashMap<Integer, String>();
		this.slot = slot;
	}
	
	private String getTimerName() {
		if(main.getGame().getState() != GameState.STARTING) {
			return main.getGame().getTimer().getName();
		}
		return "";
	}
	
	private String handleTimerString(MatchTimer timer) {
		String clockTime = timer.toString();
		if(timer == UHCSystem.getLobbyTimer()) {
			if(main.getGame().getState() != GameState.STARTING) {
				clockTime = "Waiting...";
				return clockTime;
			}
		}
		
		if(timer.getMinutes() == 0 && timer.getSeconds() <= 5) {
			clockTime = ChatColor.RED + clockTime;
		}else {
			clockTime = ChatColor.GREEN + clockTime;
		}
		
		if(timer == UHCSystem.getLobbyTimer()) {
			return timer.getName().concat(": ").concat(clockTime);
		}else {
			return clockTime;
		}
	}

	public void generate(UHCPlayer player) {
		lines.clear();
		List<String> typeLines = main.getSettings().getScoreboardSection().getStringList(scoreboardType);
		
		for(String line : typeLines) {
			if(line.isEmpty()) {
				blankLine();
			}else {
				boolean isModeLine = line.indexOf("{mode}") != -1;
				line = line.replaceAll("\\{server\\}", main.getSettings().getServerName());
				line = line.replaceAll("\\{onlinePlayers\\}", String.valueOf(main.getServer().getOnlinePlayers().size()));
				line = line.replaceAll("\\{maxPlayers\\}", String.valueOf(main.getProfile().getMaxPlayers()));
				line = line.replaceAll("\\{kdr\\}", String.valueOf(player.getKillDeathRatio()));
				line = line.replaceAll("\\{kills\\}", String.valueOf(player.getKills()));
				line = line.replaceAll("\\{deaths\\}", String.valueOf(player.getDeaths()));
				line = line.replaceAll("\\{gameKills\\}", String.valueOf(player.getGameKills()));
				line = line.replaceAll("\\{tGameKills\\}", String.valueOf(getGameKills(player)));
				line = line.replaceAll("\\{timerName\\}", String.valueOf(getTimerName()));
				line = line.replaceAll("\\{timer\\}", handleTimerString(main.getGame().getTimer()));
				line = line.replaceAll("\\{lobbyTimer\\}", handleTimerString(UHCSystem.getLobbyTimer()));
				line = line.replaceAll("\\{mode\\}", (main.getGame().isTeamMatch()) ? main.getMessages().getRawMessage("team") : main.getMessages().getRawMessage("solo"));
				addLine(ChatColor.translateAlternateColorCodes('&', line));
				
				if(isModeLine && main.getGame().isTeamMatch()) {
					Team team = player.getTeam();
					if(team != null) {
						addLine(ChatColor.translateAlternateColorCodes('&', team.getName()));
					}else {
						addLine(ChatColor.translateAlternateColorCodes('&', main.getMessages().getRawMessage("not-selected")));
					}
					
					blankLine();
				}
			}
		}
	}
	
	public int getGameKills(UHCPlayer player) {
		if(!player.isInGame()) return 0;
		int kills = 0;
		
		for(UHCPlayer uhcPlayer : main.getOnlinePlayers().values()) {
			kills += uhcPlayer.getGameKills();
		}
		
		return kills;
	}
	
	public void addLine(final String line) {
		lines.put(lines.size(), line);
	}
	
	public void setLine(final int line, String text) {
		lines.put(line, text);
	}
	
	public void removeLine(final int line) {
		if(lines.containsKey(line)) {
			lines.remove(line);
		}
	}
	
	public void blankLine() {
		String str = " ";
		int blanks = 0;
		for(Map.Entry<Integer, String> entry : lines.entrySet()) {
			final String line = entry.getValue();
			if(line.length() - line.replaceAll(" ", "").length() > 0) {
				blanks++;
			}
		}
		for(int i = 0; i != blanks; i++) {
			str = str.concat(" ");
		}
		addLine(str);
	}
	
	private void organizeLines() {
		int lineNum = lines.size();
		
		for(final Map.Entry<Integer, String> entry : lines.entrySet()) {
			final String text = entry.getValue();
			final Score score = obj.getScore(text);
			score.setScore(lineNum);
			lineNum -= 1;
		}
	}
	
	public void build(final Player player) {
		// Updating this to handle the uncolored lines.
		scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
		obj = scoreboard.registerNewObjective("board", "dummy");
		obj.setDisplayName(ChatColor.translateAlternateColorCodes('&', title));
		obj.setDisplaySlot(slot);
		organizeLines();
		if(player != null) player.setScoreboard(scoreboard);
	}
	
	public String getTitle() {
		return this.title;
	}
	
	public DisplaySlot getDisplaySlot() {
		return this.slot;
	}
	
	public Scoreboard getBoard() {
		return this.scoreboard;
	}
	
	public HashMap<Integer, String> getLines() {
		return this.lines;
	}
}
