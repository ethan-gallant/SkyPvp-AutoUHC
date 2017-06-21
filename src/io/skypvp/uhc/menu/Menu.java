package io.skypvp.uhc.menu;

import io.skypvp.uhc.SkyPVPUHC;
import io.skypvp.uhc.player.UHCPlayer;

import org.bukkit.inventory.Inventory;

public abstract class Menu implements IMenu {

	protected final SkyPVPUHC main;
	protected final UHCPlayer viewer;
	protected final Inventory ui;

	public Menu(SkyPVPUHC instance, UHCPlayer uhcPlayer, Inventory gui) {
		this.main = instance;
		this.viewer = uhcPlayer;
		this.ui = gui;
	}

	public Inventory getUI() {
		return this.ui;
	}
}
