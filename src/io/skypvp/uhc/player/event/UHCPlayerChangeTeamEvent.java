package io.skypvp.uhc.player.event;

import io.skypvp.uhc.arena.Team;
import io.skypvp.uhc.player.UHCPlayer;

public class UHCPlayerChangeTeamEvent extends UHCPlayerEvent {
	
	private final Team newTeam;
	private final Team oldTeam;
	
	public UHCPlayerChangeTeamEvent(UHCPlayer player, Team newTeam, Team oldTeam) {
		super(player);
		this.newTeam = newTeam;
		this.oldTeam = oldTeam;
	}
	
	public UHCPlayer getPlayer() {
		return this.player;
	}
	
	/**
	 * Returns the team the player switched to.
	 * @return Team
	 */
	
	public Team getTeam() {
		return this.newTeam;
	}
	
	public Team getPreviousTeam() {
		return this.oldTeam;
	}

}
