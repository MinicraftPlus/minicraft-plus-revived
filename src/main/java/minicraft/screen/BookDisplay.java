package minicraft.screen;

import java.util.ArrayList;
import java.util.Arrays;

import minicraft.core.Game;
import minicraft.core.io.InputHandler;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Point;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteSheet;
import minicraft.screen.entry.StringEntry;
import minicraft.util.BookData;

public class BookDisplay extends Display {
	private static final int spacing = 3;
	private static final int minX = 15, maxX = 15+8 * 32, minY = 8*5, maxY = 8*5 + 8*16;

	// First array is page and second is line.
	private String[][] lines;
	private int page;

	public BookDisplay(BookData book) {
		page = 0;

		ArrayList<String[]> pages = new ArrayList<>();
		String[] splitContents = book.content.split("\0");
		for (String content: splitContents) {
			String[] remainder = {content};
			while (remainder[remainder.length-1].length() > 0) {
				remainder = Font.getLines(remainder[remainder.length-1], maxX-minX, maxY-minY, spacing, true);
				pages.add(Arrays.copyOf(remainder, remainder.length-1)); // Removes the last element of remainder, which is the leftover.
			}
		}

		lines = pages.toArray(new String[pages.size()][]);

		Menu.Builder builder = new Menu.Builder(true, spacing, RelPos.CENTER);

		Menu pageCount = builder // The small rect for the title
			.setPositioning(new Point(Screen.w/2, 0), RelPos.BOTTOM)
			.setEntries(StringEntry.useLines(Color.BLACK, "Page 1/" + lines.length, book.title))
			.setSelection(1)
			.createMenu();

		builder
			.setPositioning(new Point(Screen.w/2, pageCount.getBounds().getBottom() + spacing), RelPos.BOTTOM)
			.setSize(maxX-minX + SpriteSheet.boxWidth*2, maxY-minY + SpriteSheet.boxWidth*2)
			.setShouldRender(false);

		menus = new Menu[lines.length + 1];
		menus[0] = pageCount;
		for (int i = 0; i < lines.length; i++) {
			menus[i+1] = builder.setEntries(StringEntry.useLines(Color.WHITE, lines[i])).createMenu();
		}

		menus[page+1].shouldRender = true;
	}

	private void turnPage(int dir) {
		if (page + dir >= 0 && page + dir < lines.length) {
			menus[page+1].shouldRender = false;
			page += dir;
			menus[0].updateEntry(0, new StringEntry("Page " + (page + 1) + "/" + lines.length, Color.BLACK));
			menus[page+1].shouldRender = true;
		}
	}

	@Override
	public void tick(InputHandler input) {
		if (input.getKey("menu").clicked || input.getKey("exit").clicked) Game.exitDisplay(); // Close the menu.
		if (input.getKey("cursor-left").clicked) turnPage(-1); // This is what turns the page back
		if (input.getKey("cursor-right").clicked) turnPage(1); // This is what turns the page forward
	}
}
