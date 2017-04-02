package com.mojang.ld22.item.resource;

import com.mojang.ld22.Game;
import com.mojang.ld22.crafting.Crafting;
import com.mojang.ld22.entity.Player;
import com.mojang.ld22.level.Level;
import com.mojang.ld22.level.tile.Tile;
import com.mojang.ld22.screen.BookAntVenomMenu;
import com.mojang.ld22.screen.BookTestMenu;

public class ItemResource extends Resource {
	//public int dur = -1; // TODO make this non-static.

	public ItemResource(String name, int sprite, int color) {
		super(name, sprite, color);
		//if(name == "Fish Rod") dur = 15;
	}

	//This is what makes the book bring up the Book menu.
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, int attackDir) {
		if (name == "book") {
			player.game.setMenu(new BookTestMenu(Crafting.workbenchRecipes, player));
			// Don't worry about the Crafting.workbench thing, It's what makes it work :P

		} else if (name == "Antidious") {
			player.game.setMenu(new BookAntVenomMenu(Crafting.workbenchRecipes, player));
			// Don't worry about the Crafting.workbench thing, It's what makes it work :P
		}/* else if (name == "Fish Rod") {
			if (tile == Tile.water || tile == Tile.lightwater) {
				if(Game.debug) System.out.println("Fishing...");
				player.goFishing(player.x - 5, player.y - 5);
				//dur--;
				//return dur <= 0;
				//if(dur == 0 && player.activeItem == this) player.activeItem = null;
			}
		}*/
		return false;
	}
}
