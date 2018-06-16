package minicraft.item;

import java.util.ArrayList;

import minicraft.core.Game;
import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Sprite;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.screen.BookData;
import minicraft.screen.BookDisplay;

public class BookItem extends Item {
	
	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<Item>();
		items.add(new BookItem("Book", Color.get(-1, 200, 531, 430), null));
		items.add(new BookItem("Antidious", Color.get(-1, 100, 300, 500), BookData.antVenomBook, true));
		return items;
	}
	
	protected String book; // TODO this is not saved yet; it could be, for editable books.
	private final boolean hasTitlePage;
	
	private BookItem(String title, int color, String book) { this(title, color, book, false); }
	private BookItem(String title, int color, String book, boolean hasTitlePage) {
		super(title, new Sprite(14, 4, color));
		this.book = book;
		this.hasTitlePage = hasTitlePage;
	}
	
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir) {
		Game.setMenu(new BookDisplay(book, hasTitlePage));
		return true;
	}
	
	@Override
	public boolean interactsWithWorld() { return false; }
	
	public BookItem clone() {
		return new BookItem(getName(), sprite.color, book, hasTitlePage);
	}
}
