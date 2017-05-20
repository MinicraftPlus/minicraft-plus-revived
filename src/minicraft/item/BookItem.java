package minicraft.item;

import java.util.ArrayList;
import java.util.Arrays;
import minicraft.entity.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.gfx.ConnectorSprite;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;
import minicraft.screen.BookMenu;

public class BookItem extends Item {
	
	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<Item>();
		items.add(new BookItem("Book", Color.get(-1, 200, 531, 430), BookMenu.defaultBook));
		items.add(new BookItem("Antidious", Color.get(-1, 100, 300, 500), BookMenu.antVenomBook));
		return items;
	}
	
	protected String book; // TODO this is not saved yet; it could be, for editable books.
	
	private BookItem(String title, int color, String book) {
		super(title, new Sprite(14, 4, color));
		this.book = book;
	}
	/*
	public int getColor() {
		return Color.get(-1, 200, 531, 430);
	}

	public int getSprite() {
		return 14 + 4 * 32;
	}
	*//*
	public void renderIcon(Screen screen, int x, int y) {
		screen.render(x, y, getSprite(), getColor(), 0);
	}
	
	public void renderInventory(Screen screen, int x, int y) {
		screen.render(x, y, getSprite(), getColor(), 0);
		Font.draw(name, screen, x + 8, y, Color.get(-1, 555));
	}
	
	public String getName() {
		return "Book";
	}
	*/
	
	/* I've decided that books shouldn't be stackable.
	public boolean matches(Item other) {
		if(other instanceof BookItem) {
			return Arrays.deepEquals(pages, other.pages) && title.equals(other.title);
		}
		return false;
	}*/
	
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, int attackDir) {
		player.game.setMenu(new BookMenu(book));
		return true;
	}
	
	public BookItem clone() {
		return new BookItem(name, sprite.color, book);
	}
}
