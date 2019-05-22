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
import minicraft.screen.BookEditableDisplay;

public class BookItem extends Item {
	
	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<Item>();
		items.add(new BookItem("Record 23", Color.get(-1, 200, 531, 430), BookData.record23, true));
		items.add(new BookItem("Antidious", Color.get(-1, 100, 300, 500), BookData.antVenomBook, true));
		items.add(new BookItem("Paul's Story", Color.get(-1, 131, 242, 353), BookData.theStoryOfPaul, false));
		items.add(new BookItem("Editable Book", Color.get(-1, 202, 303, 404), "type here", false, true));
		return items;
	}
	
	private String text; // TODO this is not saved yet; it could be, for editable books.
	public final boolean hasTitlePage;
	public final boolean editable;
	
	private BookItem(String title, int color, String text, boolean hasTitlePage) { this(title, color, text, hasTitlePage, false); }
	private BookItem(String title, int color, String text, boolean hasTitlePage, boolean editable) {
		super(title, new Sprite(14, 4, color));
		this.text = text;
		this.hasTitlePage = hasTitlePage;
		this.editable = editable;
	}
	
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir) {
		Game.setMenu(editable ? new BookEditableDisplay(this) : new BookDisplay(text, hasTitlePage));
		return true;
	}
	
	@Override
	public boolean interactsWithWorld() { return false; }
	
	public BookItem clone() {
		return new BookItem(getName(), sprite.color, text, hasTitlePage, editable);
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public String getText() {
		return this.text;
	}
	
	/*public static BookItem createBasicBook(String text) {
		return new BookItem("", Color.get(-1, 0), text, false, false);
	}*/
}
