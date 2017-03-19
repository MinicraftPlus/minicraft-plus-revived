package com.mojang.ld22.item;

import com.mojang.ld22.crafting.Crafting;
import com.mojang.ld22.entity.Player;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Font;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.level.Level;
import com.mojang.ld22.level.tile.Tile;
import com.mojang.ld22.screen.BookTestMenu;

public class BookItem extends Item {
	public int getColor() {
		return Color.get(-1, 200, 531, 430);
	}

	public int getSprite() {
		return 14 + 4 * 32;
	}

	public void renderIcon(Screen screen, int x, int y) {
		screen.render(x, y, getSprite(), getColor(), 0);
	}

	public void renderInventory(Screen screen, int x, int y) {
		screen.render(x, y, getSprite(), getColor(), 0);
		Font.draw(getName(), screen, x + 8, y, Color.get(-1, 555, 555, 555));
	}

	public String getName() {
		return "Book";
	}

	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, int attackDir) {
		player.game.setMenu(new BookTestMenu(Crafting.workbenchRecipes, player));
		//Don't mind the Crafting.workbench thing, It's what makes it work :)
		return true;
	}
}
