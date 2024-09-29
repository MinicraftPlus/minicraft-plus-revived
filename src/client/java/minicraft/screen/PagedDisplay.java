package minicraft.screen;

import minicraft.core.Game;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.MinicraftImage;
import minicraft.gfx.Point;
import minicraft.gfx.Screen;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.StringEntry;
import minicraft.util.DisplayString;
import minicraft.util.MyUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;

public class PagedDisplay extends Display {
	private static final int SPACING = 3;
	private static final int WIDTH = 8 * 32;
	private static final int HEIGHT = 8 * 18;

	// Pages of lines
	private final StringEntry[][] lines;
	private int page = 0;

	public PagedDisplay(DisplayString title, String content) {
		ArrayList<StringEntry[]> pages = new ArrayList<>();
		String[] blocks = content.split("\0");
		for (String block: blocks) {
			String[] remainder = {block};
			while (!remainder[remainder.length - 1].isEmpty()) {
				remainder = Font.getLines(remainder[remainder.length-1], WIDTH, HEIGHT, SPACING, true);
				// Removes the last element of remainder, which is the leftover.
				pages.add(Arrays.stream(Arrays.copyOf(remainder, remainder.length-1))
					.map(l -> new StringEntry(new DisplayString.StaticString(l)))
					.toArray(StringEntry[]::new));
			}
		}

		lines = pages.toArray(new StringEntry[pages.size()][]);
		Menu.Builder builder = new Menu.Builder(true, SPACING, RelPos.CENTER);
		menus = new Menu[2];
		menus[0] = builder // The small rect for the title
				.setPositioning(new Point(Screen.w/2, 0), RelPos.BOTTOM)
				.setEntries(new StringEntry(title, Color.BLACK), new StringEntry(getPageCounter(), Color.BLACK))
				.setSelection(1)
				.createMenu();
		menus[1] = builder
				.setPositioning(new Point(Screen.w/2, menus[0].getBounds().getBottom() + SPACING), RelPos.BOTTOM)
				.setSize(WIDTH + MinicraftImage.boxWidth*2, HEIGHT + MinicraftImage.boxWidth*2)
				.setDisplayLength((HEIGHT + SPACING) / (ListEntry.getHeight() + SPACING)) // Taking account of spacings
				.createMenu();
		menus[1].setEntries(lines[page]);
	}

	private DisplayString getPageCounter() {
		return Localization.getStaticDisplay("minicraft.displays.paged.page_counter",
			page + 1, lines.length);
	}

	private void turnPage(int dir) {
		int dest = MyUtils.clamp(page + dir, 0, lines.length - 1);
		if (dest != page) {
			page = dest;
			menus[0].updateSelectedEntry(new StringEntry(getPageCounter(), Color.BLACK));
			menus[1].setEntries(lines[page]);
		}
	}

	@Override
	public void tick(InputHandler input) {
		if (input.inputPressed("menu") || input.inputPressed("exit")) Game.exitDisplay(); // Close the menu.
		if (input.inputPressed("cursor-left")) turnPage(-1); // This is what turns the page back
		if (input.inputPressed("cursor-right")) turnPage(1); // This is what turns the page forward
	}
}
