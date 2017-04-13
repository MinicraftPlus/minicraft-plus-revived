package com.mojang.ld22.item.resource;

import com.mojang.ld22.entity.Player;
import com.mojang.ld22.level.Level;
import com.mojang.ld22.level.tile.Tile;

public class FoodResource extends Resource {
	private int heal; // the amount of hunger the food "satisfies" you by.
	private int staminaCost; // the amount of stamina it costs to consume the food.

	public FoodResource(String name, int sprite, int color, int heal, int staminaCost) {
		super(name, sprite, color);
		this.heal = heal;
		this.staminaCost = staminaCost;
	}
	
	/** What happens when the player uses the item on a tile */
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, int attackDir) {
		if (player.hunger < 10) { // if the player has hunger to fill
			if (player.payStamina(staminaCost)) { // if the stamina payment was successful...
				player.hunger += heal; // restore the hunger
				if (player.hunger > 10) { // make sure the hunger doesn't go above ten.
					player.hunger = 10;
					return true;
				} else {
					return true;
				}
			}
			return false;
		}
		return false;
	}
}
