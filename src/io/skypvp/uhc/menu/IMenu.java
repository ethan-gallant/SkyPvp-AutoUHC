package io.skypvp.uhc.menu;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public interface IMenu {

	// Show the menu to the viewer.
	public void show();

	// This should be called when the menu is closed.
	public void closed();

	// This is called when a click event regarding this menu is performed.
	public void clickPerformed(InventoryClickEvent evt);

	// This simply returns our inventory UI.
	public Inventory getUI();
}
