package minicraft.item;

import java.util.ArrayList;

import minicraft.core.Game;
import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.SpriteLinker.LinkedSpriteSheet;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.util.BookData;
import minicraft.screen.BookDisplay;

public class BookItem extends Item {

	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<Item>();
		items.add(new BookItem("Book", new LinkedSpriteSheet(SpriteType.Item, "book"), null));
		items.add(new BookItem("Antidious", new LinkedSpriteSheet(SpriteType.Item, "antidious_book"), () -> BookData.antVenomBook.collect(), true));
		return items;
	}

	@FunctionalInterface
	public static interface BookContent {
		public abstract String collect();
	}

	protected BookContent book; // TODO this is not saved yet; it could be, for editable books.
	private final boolean hasTitlePage;

	private BookItem(String title, LinkedSpriteSheet sprite, BookContent book) { this(title, sprite, book, false); }
	private BookItem(String title, LinkedSpriteSheet sprite, BookContent book, boolean hasTitlePage) {
		super(title, sprite);
		this.book = book;
		this.hasTitlePage = hasTitlePage;
	}

	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir) {
		Game.setDisplay(new BookDisplay(book.collect(), hasTitlePage));
		return true;
	}

	@Override
	public boolean interactsWithWorld() { return false; }

	public BookItem clone() {
		return new BookItem(getName(), sprite, book, hasTitlePage);
	}
}
