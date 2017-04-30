package minicraft.screen;

import minicraft.entity.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.sound.Sound;
import minicraft.item.ResourceItem;
import minicraft.item.Item;
import java.util.List;
import java.util.Arrays;

public class PlayerInvMenu extends InventoryMenu {
	private Player player;
	//private int selected = 0;

	public PlayerInvMenu(Player player) {
		super(player.inventory, "inventory", 1, 22);
		this.player = player;
		
		if (player.activeItem != null) { // If the player has an active item, then...
			player.inventory.add(0, player.activeItem); // that active item will go into the inventory
			player.activeItem = null; // the player will not have an active item anymore.
			options = player.inventory.getItemNames();
		}
		
	}

	public void tick() {
		if (input.getKey("menu").clicked) game.setMenu(null);
		
		super.tick();/*
		if (input.getKey("up").clicked) selected--;
		if (input.getKey("down").clicked) selected++;
		if (input.getKey("up").clicked) Sound.pickup.play();
		if (input.getKey("down").clicked) Sound.pickup.play();
		
		int len = player.inventory.invSize();
		if (len == 0) selected = 0;
		if (selected < 0) selected += len;
		if (selected >= len) selected -= len;
		*/
		if (input.getKey("attack").clicked && options.length > 0) { // If your inventory is not empty, and the player presses the "Attack" key...
			player.activeItem = player.inventory.remove(selected); // The item will be removed from the inventory, and placed as the player's active item.
			game.setMenu(null); // the game will go back to the gameplay
		}
	}

	public void render(Screen screen) {
		//renderFrame(screen, "inventory", 1, 1, 22, 11); // renders the blue box for the inventory
		super.render(screen);
		/*List<Item> items = player.inventory.getItems();
		for(int i = 0; i < dispSize; i++)
			items.get(offset+i).renderIcon(screen, 2*8, 8*(2+i));
		//renderItemList(screen, 1, 1, 22, 11, player.inventory.getItems(), selected); // renders the icon's and names of all the items in your inventory.
		*/
	}
}
