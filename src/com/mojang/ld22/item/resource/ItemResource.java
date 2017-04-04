package com.mojang.ld22.item.resource;

import com.mojang.ld22.Game;
import com.mojang.ld22.crafting.Crafting;
import com.mojang.ld22.entity.Player;
import com.mojang.ld22.level.Level;
import com.mojang.ld22.level.tile.Tile;
import com.mojang.ld22.screen.BookAntVenomMenu;
import com.mojang.ld22.screen.BookTestMenu;

public class ItemResource extends Resource {
	
	public ItemResource(String name, int sprite, int color) {
		super(name, sprite, color);
	}

	//This is what makes the book bring up the Book menu.
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, int attackDir) {
		if (name == "book") {
			player.game.setMenu(new BookTestMenu(Crafting.workbenchRecipes, player));
			// Don't worry about the Crafting.workbench thing, It's what makes it work :P

		} else if (name == "Antidious") {
			player.game.setMenu(new BookAntVenomMenu(Crafting.workbenchRecipes, player));
			// Don't worry about the Crafting.workbench thing, It's what makes it work :P
		}
		return false;
	}
}
