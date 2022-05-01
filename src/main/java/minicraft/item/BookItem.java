package minicraft.item;

import java.util.ArrayList;

import minicraft.core.Game;
import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.Sprite;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.util.BookData;
import minicraft.screen.BookDisplay;
import minicraft.screen.EditableBookDisplay;

public class BookItem extends Item {

	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<Item>();
		items.add(new BookItem("Book", new Sprite(0, 8, 0), null));
		items.add(new BookItem("Antidious", new Sprite(1, 8, 0), BookData.antVenomBook, true));
		return items;
	}

	private BookData book;
	private boolean editable;
	private String staticBook;
	private boolean hasTitlePage;
	private Sprite sprite;

	private BookItem(String title, Sprite sprite, String book) { this(title, sprite, book, false); }
	private BookItem(String title, Sprite sprite, String book, boolean hasTitlePage) {
		super(title, sprite);
		editable = false;
		this.staticBook = book;
		this.hasTitlePage = hasTitlePage;
		this.sprite = sprite;
	}
	public BookItem(BookData book, Sprite sprite) {
		super("Editable Book", sprite);
		editable = book.editable;
		this.book = book;
		if (!editable) {
			staticBook = book.content;
			hasTitlePage = true;
		}
		this.sprite = sprite;
	}

	public boolean isEditable() { return editable; }

	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir) {
		if (editable) Game.setDisplay(new EditableBookDisplay(book));
		else Game.setDisplay(new BookDisplay(staticBook, hasTitlePage));
		return true;
	}

	@Override
	public String getData() {
		return super.getData() + (editable ? "_" + book.id : "");
	}

	@Override
	public String getDescription() {
		return super.getDescription() + (editable ? "\n" + book.title : "");
	}

	@Override
	public boolean interactsWithWorld() { return false; }

	public BookItem clone() {
		return !editable ? new BookItem(getName(), sprite, staticBook, hasTitlePage) : new BookItem(book, sprite);
	}
}
