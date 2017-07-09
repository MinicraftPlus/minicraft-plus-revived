package minicraft.entity;

import minicraft.gfx.Color;
import minicraft.gfx.Sprite;
import minicraft.screen.ContainerMenu;

public class Chest extends Furniture {
	public Inventory inventory; // Inventory of the chest
	
	public Chest() {this("Chest", Color.get(-1, 220, 331, 552));}
	public Chest(String name, int color) {
		super(name, new Sprite(2, 8, 2, 2, color), 3, 3); // Name of the chest
		
		inventory = new Inventory(); // initialize the inventory.
		
		// chest colors
		/*if (canLight()) {
			col0 = Color.get(-1, 220, 331, 552);
			col1 = Color.get(-1, 220, 331, 552);
			col2 = Color.get(-1, 220, 331, 552);
			col3 = Color.get(-1, 220, 331, 552);
		} else {
			col0 = Color.get(-1, 110, 220, 441);
			col1 = Color.get(-1, 220, 331, 552);
			col2 = Color.get(-1, 110, 220, 441);
			col3 = Color.get(-1, 000, 110, 330);
		}*/
		
		//col = Color.get(-1, 220, 331, 552);
		//sprite = 1; // Location of the sprite
	}
	
	/** This is what occurs when the player uses the "Menu" command near this */
	public boolean use(Player player, int attackDir) {
		player.game.setMenu(new ContainerMenu(player, this));
		return true;
	}
}
