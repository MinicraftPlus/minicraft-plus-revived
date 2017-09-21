package minicraft.screen;

import minicraft.entity.Inventory;
import minicraft.entity.Player;
import minicraft.item.Item;

public class PlayerInvMenu extends InventoryMenu {
	private Player player;
	
	public PlayerInvMenu(Player player) {
		super(PlayerInvMenu.addActiveItem(player), "inventory");
		this.player = player;
	}
	
	private static Inventory addActiveItem(Player player) {
		if (player.activeItem != null) { // If the player has an active item, then...
			if (!ModeMenu.creative || player.inventory.count(player.activeItem) == 0) player.inventory.add(0, player.activeItem); // that active item will go into the inventory
			player.activeItem = null; // the player will not have an active item anymore.
		}
		return player.inventory;
	}
	
	public void tick() {
		if (input.getKey("menu").clicked) game.setMenu(null);
		
		super.tick();
		if (input.getKey("attack").clicked && options.length > 0) { // If your inventory is not empty, and the player presses the "Attack" key...
			player.activeItem = player.inventory.get(selected); // The item will be placed as the player's active item.
			player.inventory.remove(selected); // The item will be removed from the inventory.
			game.setMenu(null); // the game will go back to the gameplay.
		}
	}
	
	public Item getSelectedItem() {
		if(player.inventory.invSize() == 0)
			return null;
		return player.inventory.get(selected);
	}
}
