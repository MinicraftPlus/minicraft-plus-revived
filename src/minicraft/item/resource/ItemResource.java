package minicraft.item.resource;

import minicraft.crafting.Crafting;
import minicraft.entity.Player;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.screen.BookMenu;
import minicraft.screen.BookMenu;

public class ItemResource extends Resource {
	
	public ItemResource(String name, int sprite, int color) {
		super(name, sprite, color);
	}

	//This is what makes the book bring up the Book menu.
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, int attackDir) {
		if (name == "book") {
			player.game.setMenu(new BookMenu());
		} else if (name == "Antidious") {
			player.game.setMenu(new BookMenu(BookMenu.antVenomPages));
		}
		return false;
	}
}
