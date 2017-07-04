package io.skypvp.uhc;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import io.skypvp.uhc.arena.Team;
import io.skypvp.uhc.arena.state.GameStateManager;
import io.skypvp.uhc.database.HikariDatabase;
import io.skypvp.uhc.player.UHCPlayer;
import io.skypvp.uhc.player.UHCPlayer.PlayerState;
import io.skypvp.uhc.timer.MatchTimer;
import net.md_5.bungee.api.ChatColor;

public class UHCScoreboard {

	final SkyPVPUHC main;
	final HikariDatabase database;
	final GameStateManager gsm;
	final Messages msgs;
	final HashMap<Integer, String> lines;
	private final String title, scoreboardType;
	private DisplaySlot slot;
	private Scoreboard scoreboard;
	private Objective obj;

	public UHCScoreboard(final SkyPVPUHC instance, final String scoreboard, final DisplaySlot slot) {
		this.main = instance;
        this.database = instance.getSettings().getDatabase();
        this.gsm = main.getGameStateManager();
        this.msgs = main.getMessages();
		this.title = main.getSettings().getScoreboardHeader();
		this.scoreboardType = scoreboard;
		this.lines = new HashMap<Integer, String>();
		this.slot = slot;
	}

	private String getTimerName() {
		if(gsm.getActiveState().toIndex() != 2 && gsm.getTimer() != null) {
			return gsm.getTimer().getName();
		}
		
		return "";
	}
	
	private String handleModeString(UHCPlayer player) {
	    if(main.getProfile().isTeamMatch()) {
	        Team team = player.getTeam();
	        String teamName = (team != null) ? team.getName() : msgs.getRawMessage("not-selected");
	        String msg = String.format("%s %s", msgs.getRawMessage("team"),
	                teamName);
	        return msgs.color(msg);
	    }
	    
	    return "";
	}

	private String handleTimerString(MatchTimer timer) {
        String clockTime = "";
        
		if(timer == null) {
			if(main.getGameStateManager().getActiveState().toIndex() != 2) {
				clockTime = "Waiting...";
				return clockTime;
			}
		}
		
		clockTime = timer.toString();

		if(timer.getMinutes() == 0 && timer.getSeconds() <= 5) {
			clockTime = ChatColor.RED + clockTime;
		}else {
			clockTime = ChatColor.GREEN + clockTime;
		}

		if(timer.getName().equals("Starting")) {
			return timer.getName().concat(": ").concat(clockTime);
		}
		
		return clockTime;
	}
	
	/**
	 * Creates a line with the mode of the match and the
	 * current date with the format defined in the config.
	 * @return String like so: Teams 07/03/17
	 */
	
	public String createDateLine() {
	    Date date = new Date();
	    SimpleDateFormat sdf = new SimpleDateFormat(msgs.getRawMessage("date-format"));
	    String curDate = sdf.format(date);
	    String mode = (main.getProfile().isTeamMatch()) ? msgs.getRawMessage("teammode") 
	            : msgs.getRawMessage("solo-mode");
	    
	    return ChatColor.GRAY + String.format("%s %s", mode, curDate);
	}

	public void generate(UHCPlayer player) {
		lines.clear();
		List<String> typeLines = main.getSettings().getScoreboardSection().getStringList(scoreboardType);

		for(String line : typeLines) {
			if(line.isEmpty()) {
				blankLine();
			}else {
				boolean isModeLine = line.indexOf("{mode}") != -1;
				if(gsm.getActiveState().toIndex() < 2 
				        && isModeLine && !main.getProfile().isTeamMatch()) {
				    return;  
				}else if(gsm.getActiveState().toIndex() > 2 && isModeLine) {
		            if(main.getProfile().isTeamMatch()) {
		                addLine(msgs.color(String.format("%s: &a%d", msgs.getRawMessage("teammode"), 
		                        main.getGame().getAliveTeams())));
		            }
		            
		            addLine(msgs.color(String.format("%s: &a%d", "Players",
		                    main.getGame().getAlivePlayers())));
		            blankLine();
		        }
				
				line = line.replaceAll("\\{server\\}", main.getSettings().getServerName());
				line = line.replaceAll("\\{onlinePlayers\\}", String.valueOf(main.getServer().getOnlinePlayers().size()));
				line = line.replaceAll("\\{maxPlayers\\}", String.valueOf(main.getProfile().getMaxPlayers()));
				line = line.replaceAll("\\{kdr\\}", String.valueOf(player.getKillDeathRatio()));
				line = line.replaceAll("\\{kills\\}", String.valueOf(player.getKills()));
				line = line.replaceAll("\\{deaths\\}", String.valueOf(player.getDeaths()));
				line = line.replaceAll("\\{gameKills\\}", String.valueOf(player.getGameKills()));
				line = line.replaceAll("\\{tGameKills\\}", String.valueOf(getGameKills(player)));
				line = line.replaceAll("\\{timerName\\}", String.valueOf(getTimerName()));
				line = line.replaceAll("\\{timer\\}", handleTimerString(main.getGameStateManager().getTimer()));
				line = line.replaceAll("\\{mode\\}", handleModeString(player));
				line = line.replaceAll("\\{date\\}", createDateLine());
				addLine(ChatColor.translateAlternateColorCodes('&', line));

				if(isModeLine && main.getProfile().isTeamMatch()) {
					Team team = player.getTeam();
					if(team != null) {
					    Iterator<UHCPlayer> iterator = team.getMembers().iterator();
					    
					    while(iterator.hasNext()) {
					        UHCPlayer member = iterator.next();
					        ChatColor color = ChatColor.YELLOW;
					        
					        if(member.getState() == PlayerState.SPECTATING) {
					            color = ChatColor.GRAY;
					        }else if(member.getBukkitPlayer().getHealth() < member.getBukkitPlayer().getMaxHealth()) {
					            color = ChatColor.RED;
					        }
					        
					        addLine(String.format("%s%s", color, 
					                ChatColor.stripColor(member.getBukkitPlayer().getName())));
					    }
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
		scoreboard = (scoreboard == null) ? Bukkit.getScoreboardManager().getNewScoreboard() : scoreboard;
		if(obj != null) obj.unregister();
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
