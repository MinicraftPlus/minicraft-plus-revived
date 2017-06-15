package minicraft.screen;

import minicraft.entity.Chest;
import minicraft.entity.Inventory;
import minicraft.entity.Player;
import minicraft.gfx.Screen;
import minicraft.Sound;
import minicraft.Game;

public class ContainerMenu extends Menu {
	private Player player; // The player that is looking inside the chest
	private Chest chest;
	//private Inventory container; // The inventory of the chest
	private int selected = 0; // The selected item
	//private String title; // The title of the chest
	private int oSelected; // the old selected option (this is used to temporarily save spots moving from chest to inventory & vice-versa)
	private int window = 0; // currently selected window (player's inventory, or chest's inventory)
	
	public ContainerMenu(Player player, Chest chest) {
		this.player = player;
		this.chest = chest;
	}

	public void tick() {
		if (input.getKey("menu").clicked) game.setMenu(null); // If the player selects the "menu" key, then it will exit the chest

		if (input.getKey("left").clicked) { //if the left key is pressed...
			window = 0; // The current window will be of the chest
			int tmp = selected; // temp integer will be the currently selected
			selected = oSelected; // selected will become the oSelected
			oSelected = tmp; // oSelected will become the temp integer (save spot for when you switch)
		}
		if (input.getKey("right").clicked) { //if the right key is pressed...
			window = 1; // The current window will be of the player's inventory
			int tmp = selected;
			selected = oSelected;
			oSelected = tmp;
		}
		
		// get player and container inventory references...again?
		Inventory i = window == 1 ? player.inventory : chest.inventory;
		Inventory i2 = window == 0 ? player.inventory : chest.inventory;
		
		int len = i.invSize(); // Size of the main inventory
		
		//selection fix
		if (selected < 0) selected = 0;
		if (selected >= len) selected = len - 1;
		//selection movement
		if (input.getKey("up").clicked) selected--;
		if (input.getKey("down").clicked) selected++;
		//selection sound effects
		if (input.getKey("up").clicked) Sound.pickup.play();
		if (input.getKey("down").clicked) Sound.pickup.play();
		//selection wrap around
		if (len == 0) selected = 0;
		if (selected < 0) selected += len;
		if (selected >= len) selected -= len;
		
		// If the "Attack" key is pressed and the inventory's size is bigger than 0...
		if (input.getKey("attack").clicked && len > 0) {
			if(Game.isValidClient()) {
				if(i == chest.inventory)
					Game.client.removeFromChest(chest, selected);
				else
					Game.client.addToChest(chest, i.get(selected)); // if the other menu is the chest, then we are adding to it (add==true)
			}
			else
				i2.add(oSelected, i.remove(selected)); // It will add the item to the new inventory, and remove it from the old one.
		}
	}
	
	public void render(Screen screen) {
		if (window == 1) screen.setOffset(6 * 8, 0); // Offsets the windows for when the player's inventory is selected
		renderFrame(screen, chest.name, 1, 1, 18, 11); // Renders the chest's window
		renderItemList(screen, 1, 1, 18, 11, chest.inventory.getItems(), window == 0 ? selected : -oSelected - 1); // renders all the items from the chest's inventory

		renderFrame(screen, "inventory", 19, 1, 15 + 20, 11); // renders the player's inventory
		renderItemList(screen, 19, 1, 15 + 20, 11, player.inventory.getItems(), window == 1 ? selected : -oSelected - 1); // renders all the items from the player's inventory
		screen.setOffset(0, 0); // Fixes the offset back to normal
	}
}
