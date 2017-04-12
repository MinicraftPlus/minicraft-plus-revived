package com.mojang.ld22.entity;

import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.screen.ContainerMenu;
import com.mojang.ld22.screen.OptionsMenu;

public class Chest extends Furniture {
	public Inventory inventory; // Inventory of the chest
	
	public Chest() {this("Chest");}
	public Chest(String name) {
		super(name); // Name of the chest
		
		inventory = new Inventory();
		
		// chest colors
		if (canLight()) {
			col0 = Color.get(-1, 220, 331, 552);
			col1 = Color.get(-1, 220, 331, 552);
			col2 = Color.get(-1, 220, 331, 552);
			col3 = Color.get(-1, 220, 331, 552);
		} else {
			col0 = Color.get(-1, 110, 220, 441);
			col1 = Color.get(-1, 220, 331, 552);
			col2 = Color.get(-1, 110, 220, 441);
			col3 = Color.get(-1, 000, 110, 330);
		}
		
		col = Color.get(-1, 220, 331, 552);
		sprite = 1; // Location of the sprite
	}
	
	/** This is what occurs when the player uses the "Menu" command near this */
	public boolean use(Player player, int attackDir) {
		player.game.setMenu(new ContainerMenu(player, this));//player, name, inventory));
		return true;
	}
}
