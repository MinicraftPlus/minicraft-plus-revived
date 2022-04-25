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
	
	public EditableBookDisplay(BookData book) {
		this.book = book;
		page = 0;
		
		textAreaEntry = new TextAreaEntry(book.title, null, 20, 0, 0, book.content);
		updatePages(textAreaEntry.getLines());
		textAreaEntry.setChangeListener(o -> {
			updatePages(textAreaEntry.getLines());
		});
		textAreaEntry.setPageUpListener(o -> {
			if (page != 0) page--;
		});
		textAreaEntry.setPageDownListener(o -> {
			if (page+1 != pages.length) page++;
		});
		
		Menu.Builder builder = new Menu.Builder(true, spacing, RelPos.CENTER);
		
		Menu pageCount = builder // The small rect for the title
			.setPositioning(new Point(Screen.w/2, 0), RelPos.BOTTOM)
			.setEntries(StringEntry.useLines(Color.BLACK, "Page 1/"+(pages.length+1), book.title))
			.createMenu();
		
		menus = new Menu[2];
		menus[0] = builder
			.setPositioning(new Point(Screen.w/2, pageCount.getBounds().getBottom() + spacing), RelPos.BOTTOM)
			.setSize(300, 150)
			.createMenu();
		menus[1] = pageCount;
	}
	
	private void updatePages(String[] lines) {
		ArrayList<String[]> pages = new ArrayList<>();
		for (int l = 0; l<lines.length; l+=6)
			pages.add(Arrays.copyOfRange(lines, l, l+6));
		this.pages = pages.toArray(new String[0][]);
		renderCursor = textAreaEntry.getRenderCursor();
		page = renderCursor/(Short.MAX_VALUE*6);
		menus[1].updateSelectedEntry(new StringEntry("Page "+page+"/"+(this.pages.length+1), Color.BLACK));
	}
	
	@Override
	public void tick(InputHandler input) {
		if (input.getKey("menu").clicked || input.getKey("exit").clicked) {
			book.content = textAreaEntry.getUserInput();
			BookData.saveBook(book);
			Game.exitDisplay();
			return;
		}
		textAreaEntry.tick(input);
	}

	@Override
	public void render(Screen screen) {
		super.render(screen);
		Font.drawParagraph(screen, new FontStyle(Color.BLACK).setXPos(50).setYPos(50), 0, pages[page]);
	}
}
