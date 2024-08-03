package minicraft.item;

import minicraft.core.Game;
import minicraft.core.io.Localization;
import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.screen.BookDisplay;
import minicraft.util.BookData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class BookItem extends Item {

	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<Item>();
		items.add(new BookItem("Book", new LinkedSprite(SpriteType.Item, "book"), () -> Localization.getLocalized("minicraft.displays.book.default_book")));
		items.add(new BookItem("Antidious", new LinkedSprite(SpriteType.Item, "antidious_book"), () -> BookData.antVenomBook.collect(), true));
		return items;
	}

	@FunctionalInterface
	public static interface BookContent {
		public abstract String collect();
	}

	protected BookContent book; // TODO this is not saved yet; it could be, for editable books.
	private final boolean hasTitlePage;

	private BookItem(String title, LinkedSprite sprite, BookContent book) {
		this(title, sprite, book, false);
	}

	private BookItem(String title, LinkedSprite sprite, BookContent book, boolean hasTitlePage) {
		super(title, sprite);
		this.book = book;
		this.hasTitlePage = hasTitlePage;
	}

	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir) {
		Game.setDisplay(new BookDisplay(book.collect(), hasTitlePage));
		return true;
	}

	@Override
	public boolean interactsWithWorld() {
		return false;
	}

	public @NotNull BookItem copy() {
		return new BookItem(getName(), sprite, book, hasTitlePage);
	}
}
