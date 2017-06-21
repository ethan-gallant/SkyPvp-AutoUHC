package io.skypvp.uhc.arena.state;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import io.skypvp.uhc.Settings;
import io.skypvp.uhc.SkyPVPUHC;
import io.skypvp.uhc.UHCSystem;

public class LobbyWaitState extends GameState {

	private ArrayList<UUID> forcestartVotes;

	// The last time (in milliseconds) that we broadcasted that we were
	// waiting for more players.
	private long lastWaitNoticeMs;

	public LobbyWaitState(SkyPVPUHC instance, GameStateManager stateMgr) {
		super(instance, "lobbyWait", stateMgr);
		this.forcestartVotes = new ArrayList<UUID>();
		this.lastWaitNoticeMs = System.currentTimeMillis();
	}

	public void run() {
		// Every 3 seconds, let's broadcast that we're waiting for more players.
		if(System.currentTimeMillis() > lastWaitNoticeMs + 3000) {
			UHCSystem.broadcastMessage(main.getMessages().getMessage("waitingForPlayers"));
		}
	}

	@Override
	public boolean canMoveOn() {
		int onlinePlayers = main.getOnlinePlayers().keySet().size();
		double perct = ((double) forcestartVotes.size()) / onlinePlayers;

		Settings settings = main.getSettings();
		boolean teamMode = main.getProfile().isTeamMatch();
		return (perct >= 0.75) || ((teamMode && onlinePlayers >= settings.getMinimumTeamGamePlayers()) ||
				!teamMode && onlinePlayers >= settings.getMinimumSoloGamePlayers());
	}

	/**
	 * If {@link #canMoveOn()} fails, let's continue calling {@link #run()}
	 * @return The opposite of {@link #canMoveOn()}.
	 */

	public boolean canContinue() {
		return !canMoveOn();
	}

	public void onEnter() {}

	public void onExit() {}

	////////////////////////////////////////////////////////////
	// 			CODE RELATING TO FORCE-START VOTING           //
	////////////////////////////////////////////////////////////

	/**
	 * Attempts to send a vote for force-start.
	 * @param {@link UUID} - The uuid of the {@link UHCPlayer} who's voting.
	 * @return true/false, if the vote was successful or not.
	 */

	public boolean voteForForceStart(UUID uuid) {
		boolean voted = hasVotedForForceStart(uuid);
		Player p = Bukkit.getPlayer(uuid);
		if(p == null) return false;

		if(!voted) {
			int onlinePlayers = main.getOnlinePlayers().keySet().size();
			if(main.getProfile().isTeamMatch() && onlinePlayers >= main.getSettings().getMinimumTeamGamePlayers() 
					|| !main.getProfile().isTeamMatch() && onlinePlayers >= main.getSettings().getMinimumSoloGamePlayers()) {
				// There's enough players online.
				if(stateMgr.getActiveState() == this) {
					forcestartVotes.add(uuid);
					p.sendMessage(main.getMessages().color(main.getMessages().getMessage("voteSuccess")));

					double perct = ((double) forcestartVotes.size()) / onlinePlayers;
					if(perct >= 0.75) {
						// Great, we have enough voters!
						String forceStartMsg = main.getMessages().color(main.getMessages().getMessage("forceStartSuccess"));
						Iterator<UUID> iterator = main.getOnlinePlayers().keySet().iterator();
						while(iterator.hasNext()) {
							UUID uid = iterator.next();
							Player bP = Bukkit.getPlayer(uid);
							if(bP != null) {
								bP.sendMessage(forceStartMsg);
							}
						}
					}
				}
			}else {
				p.sendMessage(main.getMessages().color(main.getMessages().getMessage("notEnoughPlayers")));
				return false;
			}

			return true;
		}

		return false;
	}

	/**
	 * Checks if a {@link UUID} is inside of the force start votes {@link ArrayList}
	 * @param {@link UUID}
	 * @return true/false
	 */

	public boolean hasVotedForForceStart(UUID uuid) {
		return this.forcestartVotes.contains(uuid);
	}

	/**
	 * Fetches the full {@link ArrayList} of the {@link UUID}s who have voted
	 * for force-start.
	 * @return {@link ArrayList}<{@link UUID}>
	 */

	public ArrayList<UUID> getForceStartVotes() {
		return this.forcestartVotes;
	}

	////////////////////////////////////////////////////////////

}
