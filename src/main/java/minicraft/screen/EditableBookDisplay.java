package minicraft.screen;

import java.util.ArrayList;
import java.util.Arrays;

import minicraft.core.Game;
import minicraft.core.io.InputHandler;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.FontStyle;
import minicraft.gfx.Point;
import minicraft.gfx.Screen;
import minicraft.screen.entry.InputEntry;
import minicraft.screen.entry.StringEntry;
import minicraft.screen.entry.TextAreaEntry;
import minicraft.util.BookData;

public class EditableBookDisplay extends Display {

	// null characters "\0" denote page breaks.
	private static final int spacing = 3;

	private BookData book;
	private int page;
	private TextAreaEntry textAreaEntry;
	private int renderCursor;
	private String[][] pages;

	private InputEntry titleEntry;
	private Menu.Builder titleEntryBuilder;
	private Menu nullMenu;
	private String titlePattern = "[^\\p{Cntrl}]*";

	public EditableBookDisplay(BookData book) {
		this.book = book;
		page = 0;
		selection = 0;

		textAreaEntry = new TextAreaEntry(book.title, null, 28, 0, 0, book.content);
		updatePages(textAreaEntry.getLines(), false);
		textAreaEntry.setChangeListener(o -> {
			updatePages(textAreaEntry.getLines());
		});
		textAreaEntry.setPageUpListener(o -> {
			if (page != 0) page--;
		});
		textAreaEntry.setPageDownListener(o -> {
			if (page + 1 != pages.length) page++;
		});

		titleEntry = new InputEntry("Title", titlePattern, 0, book.title);
		titleEntryBuilder = new Menu.Builder(true, spacing, RelPos.CENTER, titleEntry);

		Menu.Builder builder = new Menu.Builder(true, spacing, RelPos.CENTER);

		Menu pageCount = builder // The small rect for the title
			.setPositioning(new Point(Screen.w/2, 0), RelPos.BOTTOM)
			.setEntries(StringEntry.useLines(Color.BLACK, "Page 1/"+(pages.length), book.title))
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
	}

	private void updatePages(String[] lines) { updatePages(lines, true); }
	private void updatePages(String[] lines, boolean updateMenu1) {
		ArrayList<String[]> pages = new ArrayList<>();
		for (int l = 0; l < lines.length; l+=16) {
			pages.add(Arrays.copyOfRange(lines, l, l+16 > lines.length ? lines.length : l+16));
		}
		this.pages = pages.toArray(new String[0][]);
		renderCursor = textAreaEntry.getRenderCursor();
		page = renderCursor / (Short.MAX_VALUE*16);
		if (updateMenu1) menus[2].updateEntry(0, new StringEntry("Page " + (page+1) + "/" + this.pages.length, Color.BLACK));
	}

	@Override
	public void tick(InputHandler input) {
		if (menus[0] != nullMenu) {
			if (input.getKey("exit").clicked) {
				menus[0] = nullMenu;
				return;
			} else if (input.getKey("attack").clicked) {
				book.title = titleEntry.getUserInput();
				menus[2].updateEntry(1, new StringEntry(book.title, Color.BLACK));
				titleEntry = new InputEntry("Title", titlePattern, 0, book.title);
				menus[0] = nullMenu;
				return;
			}
			titleEntry.tick(input);
			menus[0] = titleEntryBuilder.createMenu(); // This resizes menu
			return;
		}
		if (input.getKey("exit").clicked) {
			book.content = textAreaEntry.getUserInput();
			BookData.saveBook(book);
			Game.exitDisplay();
			return;
		} else if (input.getKey("CTRL-SHIFT-S").clicked) {
			book.editable = false;
			// This should apply book author in multiplayer is added
			book.content = textAreaEntry.getUserInput();
			BookData.saveBook(book);
			Game.exitDisplay();
			return;
		} else if (input.getKey("SHIFT-5").clicked) {
			menus[0] = titleEntryBuilder.createMenu();
			return;
		}
		textAreaEntry.tick(input);
	}

	@Override
	public void render(Screen screen) {
		super.render(screen);
		if (menus[0] == nullMenu) {
			Font.drawParagraph(screen, new FontStyle(Color.BLACK).setXPos(32).setYPos(50), 0, pages[page]);
			Font.draw("_", screen, 32 + (renderCursor % Short.MAX_VALUE) * 8, 50 + (renderCursor / Short.MAX_VALUE) * 8);
		}
	}
}
