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
	
	public PlayerInvMenu(Player player) {
		super(player.inventory, "inventory");
		this.player = player;
		
		if (player.activeItem != null) { // If the player has an active item, then...
			player.inventory.add(0, player.activeItem); // that active item will go into the inventory
			options.add(0, " "+player.activeItem.getName());
			player.activeItem = null; // the player will not have an active item anymore.
		}
	}

	public void tick() {
		if (input.getKey("menu").clicked) game.setMenu(null);
		
		super.tick();
		if (input.getKey("attack").clicked && options.size() > 0) { // If your inventory is not empty, and the player presses the "Attack" key...
			player.activeItem = player.inventory.remove(selected); // The item will be removed from the inventory, and placed as the player's active item.
			game.setMenu(null); // the game will go back to the gameplay
		}
	}
}
