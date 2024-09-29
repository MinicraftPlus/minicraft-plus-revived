package minicraft.screen;

import minicraft.core.Game;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.MinicraftImage;
import minicraft.gfx.Point;
import minicraft.gfx.Screen;
import minicraft.screen.entry.StringEntry;
import minicraft.util.DisplayString;

import java.util.ArrayList;
import java.util.Arrays;

public class BookDisplay extends Display {

	// null characters "\0" denote page breaks.
	private static final DisplayString defaultBook =
		Localization.getStaticDisplay("minicraft.displays.book.default_book");

	private static final int SPACING = 3;
	private static final int WIDTH = 8 * 32;
	private static final int HEIGHT = 8 * 16;

	// First array is page and second is line.
	private String[][] lines;
	private int page;

	private final boolean hasTitle;
	private final boolean showPageCount;
	private final int pageOffset;

	public BookDisplay(String book, boolean hasTitle) {// this(book, hasTitle, !hasTitle); }
		//public BookDisplay(String book, boolean hasTitle, boolean hideCountIfOnePage) {
		page = 0;

		if (book == null) {
			book = defaultBook.toString();
			hasTitle = false;
		}

		this.hasTitle = hasTitle;

		ArrayList<String[]> pages = new ArrayList<>();
		String[] splitContents = book.split("\0");
		for (String content : splitContents) {
			String[] remainder = { content };
			while (remainder[remainder.length - 1].length() > 0) {
				remainder = Font.getLines(remainder[remainder.length - 1], WIDTH, HEIGHT, SPACING, true);
				pages.add(Arrays.copyOf(remainder, remainder.length - 1)); // Removes the last element of remainder, which is the leftover.
			}
		}

		lines = pages.toArray(new String[pages.size()][]);

		showPageCount = hasTitle || lines.length != 1;
		pageOffset = showPageCount ? 1 : 0;

		Menu.Builder builder = new Menu.Builder(true, SPACING, RelPos.CENTER);

		Menu pageCount = builder // The small rect for the title
			.setPositioning(new Point(Screen.w / 2, 0), RelPos.BOTTOM)
			.setEntries(StringEntry.useLines(Color.BLACK, false,
				Localization.getLocalized("minicraft.displays.book.page_counter"),
				hasTitle ? Localization.getLocalized("minicraft.displays.book.page_counter.title") :
					Localization.getLocalized("minicraft.displays.book.page_counter.value",
						1, lines.length)))
				.setSelection(1)
			.createMenu();

		builder
			.setPositioning(new Point(Screen.w / 2, pageCount.getBounds().getBottom() + SPACING), RelPos.BOTTOM)
			.setSize(WIDTH + MinicraftImage.boxWidth * 2, HEIGHT + MinicraftImage.boxWidth * 2)
			.setShouldRender(false);

		menus = new Menu[lines.length + pageOffset];
		if (showPageCount) menus[0] = pageCount;
		for (int i = 0; i < lines.length; i++) {
			menus[i + pageOffset] = builder.setEntries(StringEntry.useLines(Color.WHITE, lines[i])).createMenu();
		}

		menus[page + pageOffset].shouldRender = true;
	}

	private void turnPage(int dir) {
		if (page + dir >= 0 && page + dir < lines.length) {
			menus[page + pageOffset].shouldRender = false;
			page += dir;
			if (showPageCount)
				menus[0].updateSelectedEntry(new StringEntry(page == 0 && hasTitle ?
					Localization.getStaticDisplay("minicraft.displays.book.page_counter.title") :
					Localization.getStaticDisplay("minicraft.displays.book.page_counter.value",
						page + 1, lines.length), Color.BLACK));
			menus[page + pageOffset].shouldRender = true;
		}
	}

	@Override
	public void tick(InputHandler input) {
		if (input.inputPressed("menu") || input.inputPressed("exit")) Game.exitDisplay(); // Close the menu.
		if (input.inputPressed("cursor-left")) turnPage(-1); // This is what turns the page back
		if (input.inputPressed("cursor-right")) turnPage(1); // This is what turns the page forward
	}
}
