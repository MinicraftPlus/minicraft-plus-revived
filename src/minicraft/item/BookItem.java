package minicraft.item;

import java.util.ArrayList;

import minicraft.Game;
import minicraft.entity.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Sprite;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.screen.BookDisplay;
import minicraft.screen.Displays;

public class BookItem extends Item {
	
	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<Item>();
		items.add(new BookItem("Book", Color.get(-1, 200, 531, 430), null));
		items.add(new BookItem("Antidious", Color.get(-1, 100, 300, 500), Displays.antVenomBook));
		return items;
	}
	
	protected String book; // TODO this is not saved yet; it could be, for editable books.
	
	private BookItem(String title, int color, String book) {
		super(title, new Sprite(14, 4, color));
		this.book = book;
	}
	
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, int attackDir) {
		Game.setMenu(new BookDisplay(book));
		return true;
	}
	
	public BookItem clone() {
		return new BookItem(name, sprite.color, book);
	}
}
