package minicraft.screen;

import minicraft.core.Game;
import minicraft.core.io.InputHandler;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.FontStyle;
import minicraft.gfx.Point;
import minicraft.gfx.Screen;
import minicraft.item.WrittenBookItem;
import minicraft.item.Items;
import minicraft.screen.entry.InputEntry;
import minicraft.screen.entry.StringEntry;
import minicraft.screen.entry.BookInputEntry;
import minicraft.util.BookData;
import minicraft.util.BookData.EditableBookData;

public class EditableBookDisplay extends Display {

	// null characters "\0" denote page breaks.
	private static final int spacing = 3;

	private EditableBookData book;
	private BookInputEntry bookInputEntry;
	private int cursorX;
	private int cursorY;

	private InputEntry titleEntry;
	private Menu.Builder titleEntryBuilder;
	private Menu nullMenu;
	private String titlePattern = "[^\\p{Cntrl}]*";

	public EditableBookDisplay(EditableBookData book) {
		this.book = book;

		bookInputEntry = new BookInputEntry(book.title, book.content);
		bookInputEntry.setChangeListener(o -> {
			refreshEntryData();
		});

		titleEntry = new InputEntry("Title", titlePattern, 0, book.title);
		titleEntryBuilder = new Menu.Builder(true, spacing, RelPos.CENTER, titleEntry);

		Menu.Builder builder = new Menu.Builder(true, spacing, RelPos.CENTER);

		Menu pageCount = builder // The small rect for the title
			.setPositioning(new Point(Screen.w/2, 0), RelPos.BOTTOM)
			.setEntries(StringEntry.useLines(Color.BLACK, "Page 1/"+bookInputEntry.getPageNum(), book.title.length() == 0? "UNTITLED": book.title))
			.createMenu();

		menus = new Menu[3];
		menus[1] = builder
			.setPositioning(new Point(Screen.w/2, pageCount.getBounds().getBottom() + spacing), RelPos.BOTTOM)
			.setEntries()
			.setSize(240, 150)
			.createMenu();
		menus[2] = pageCount;
		titleEntryBuilder.setPositioning(new Point(Screen.w/2, Screen.h/2), RelPos.CENTER)
			.setEntries(titleEntry);
		nullMenu = builder.setSelectable(false)
			.setShouldRender(false)
			.createMenu();
		menus[0] = nullMenu;

		refreshEntryData();
	}

	private void refreshEntryData() {
		cursorX = bookInputEntry.getCursorX();
		cursorY = bookInputEntry.getCursorY();
		menus[2].updateEntry(0, new StringEntry("Page " + bookInputEntry.getCurrentPageNum() + "/" + bookInputEntry.getPageNum(), Color.BLACK));
	}

	@Override
	public void tick(InputHandler input) {
		if (menus[0] != nullMenu) {
			if (input.getKey("exit").clicked) {
				menus[0] = nullMenu;
				return;
			} else if (input.getKey("attack").clicked) {
				book.title = titleEntry.getUserInput();
				menus[2].updateEntry(1, new StringEntry(book.title.length() == 0? "UNTITLED": book.title, Color.BLACK));
				titleEntry = new InputEntry("Title", titlePattern, 0, book.title);
				menus[0] = nullMenu;
				return;
			}
			titleEntry.tick(input);
			menus[0] = titleEntryBuilder.createMenu(); // This resizes menu
			return;
		}

		if (input.getKey("exit").clicked) {
			book.content = bookInputEntry.getUserInput();
			Game.exitDisplay();
			return;
		} else if (input.getKey("CTRL-SHIFT-S").clicked) {
			Game.player.activeItem = null;

			// This should apply book author when multiplayer is added
			Game.player.getInventory().add(new WrittenBookItem(new BookData(book.title, bookInputEntry.getUserInput(), "Player")));
			Game.exitDisplay();
			return;
		} else if (input.getKey("SHIFT-5").clicked) {
			menus[0] = titleEntryBuilder.createMenu();
			return;
		}
		bookInputEntry.tick(input);
	}

	@Override
	public void render(Screen screen) {
		super.render(screen);
		if (menus[0] == nullMenu) {
			bookInputEntry.render(screen, new FontStyle(Color.BLACK).setXPos(32).setYPos(50));
			Font.draw("_", screen, 32 + cursorX * 8, 50 + cursorY * 8);
		}
	}
}
