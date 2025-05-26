package minicraft.item;

import minicraft.core.Game;
import minicraft.core.io.Localization;
import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.item.component.ComponentMap;
import minicraft.item.component.ComponentTypes;
import minicraft.item.component.type.BookContentComponent;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.screen.BookDisplay;
import minicraft.util.BookData;

import java.util.ArrayList;

public class BookItem extends Item {

	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<>();
		items.add(new BookItem("Book", new LinkedSprite(SpriteType.Item, "book"), () -> Localization.getLocalized("minicraft.displays.book.default_book")));
		items.add(new BookItem("Antidious", new LinkedSprite(SpriteType.Item, "antidious_book"), () -> BookData.antVenomBook.collect(), true));
		return items;
	}

	@FunctionalInterface
	public interface BookContent {
		String collect();
	}

	private BookItem(String title, LinkedSprite sprite, BookContent book) {
		this(title, sprite, book, false);
	}

	private BookItem(String title, LinkedSprite sprite, BookContent book, boolean hasTitlePage) {
		super(title, sprite, ComponentMap.builder().add(ComponentTypes.BOOK_CONTENT, new BookContentComponent(book, hasTitlePage)).build());
	}

	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir, ItemStack stack) {
		BookContentComponent component = stack.get(ComponentTypes.BOOK_CONTENT);
		Game.setDisplay(new BookDisplay(component.content().collect(), component.hasTitlePage()));
		return true;
	}

	@Override
	public boolean interactsWithWorld(ItemStack stack) {
		return false;
	}
}
