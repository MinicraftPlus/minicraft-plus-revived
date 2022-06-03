package minicraft.item;

import java.util.ArrayList;

import minicraft.core.Game;
import minicraft.core.io.Localization;
import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.Sprite;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.util.BookData;
import minicraft.screen.BookDisplay;

public class WrittenBookItem extends Item {

	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<Item>();
		items.add(new WrittenBookItem(BookData.antVenomBook, new Sprite(1, 8, 0)));
		return items;
	}

	private BookData book;

	public WrittenBookItem(BookData book) { this(book, new Sprite(3, 8, 0)); }
	public WrittenBookItem(BookData book, Sprite sprite) {
		super("Written Book", sprite);
		this.book = book;
	}

	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir) {
		Game.setDisplay(new BookDisplay(book));
		return true;
	}

	@Override
	public String getData() {
		return super.getData() + "_" + book.toString();
	}

	@Override
	public String getDescription() {
		return super.getDescription() + "\nTitle: " + book.title + "\nAuthor: " + book.author;
	}

	@Override
	public boolean interactsWithWorld() { return false; }

	public WrittenBookItem clone() {
		return new WrittenBookItem(book, sprite);
	}
}
