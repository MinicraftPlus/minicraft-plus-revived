package minicraft.screen;

import java.util.ArrayList;
import java.util.Arrays;

import minicraft.core.Game;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Point;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteSheet;
import minicraft.screen.entry.StringEntry;

public class BookDisplay extends Display {
	
	// null characters "\0" denote page breaks.
	private static final String defaultBook = " \n \0"+"There is nothing of use here."+"\0 \0"+"Still nothing... :P";
	
	private static final int spacing = 3;
	private static final int minX = 15, maxX = 15+8*32, minY = 8*5, maxY = 8*5+8*16;
	
	private String[][] lines;
	private int page;
	
	private final boolean hasTitle;
	private final boolean showPageCount;
	private final int pageOffset;
	
	public BookDisplay(String book) { this(book, false); }
	public BookDisplay(String book, boolean hasTitle) {// this(book, hasTitle, !hasTitle); }
	//public BookDisplay(String book, boolean hasTitle, boolean hideCountIfOnePage) {
		page = 0;
		if(book == null) {
			book = defaultBook;
			hasTitle = false;
		}
		book = Localization.getLocalized(book);
		this.hasTitle = hasTitle;
		
		ArrayList<String[]> pages = new ArrayList<>();
		String[] splitContents = book.split("\0");
		for(String content: splitContents) {
			String[] remainder = {content};
			while(remainder[remainder.length-1].length() > 0) {
				remainder = Font.getLines(remainder[remainder.length-1], maxX-minX, maxY-minY, spacing, true);
				pages.add(Arrays.copyOf(remainder, remainder.length-1)); // removes the last element of remainder, which is the leftover.
			}
		}
		
		lines = pages.toArray(new String[pages.size()][]);
		
		showPageCount = hasTitle || lines.length != 1;
		pageOffset = showPageCount ? 1 : 0;
		
		Menu.Builder builder = new Menu.Builder(true, spacing, RelPos.CENTER);
		
		Menu pageCount = builder // the small rect for the title
			.setPositioning(new Point(Screen.w/2, 0), RelPos.BOTTOM)
			.setEntries(StringEntry.useLines(Color.BLACK, "Page", hasTitle?"Title":"1/"+lines.length))
			.setSelection(1)
			.createMenu();
		
		builder
			.setPositioning(new Point(Screen.w/2, pageCount.getBounds().getBottom() + spacing), RelPos.BOTTOM)
			.setSize(maxX-minX + SpriteSheet.boxWidth*2, maxY-minY + SpriteSheet.boxWidth*2)
			.setShouldRender(false);
		
		menus = new Menu[lines.length+pageOffset];
		if(showPageCount) menus[0] = pageCount;
		for(int i = 0; i < lines.length; i++) {
			menus[i+pageOffset] = builder.setEntries(StringEntry.useLines(Color.WHITE, lines[i])).createMenu();
		}
		
		menus[page+pageOffset].shouldRender = true;
	}
	
	private void turnPage(int dir) {
		if(page+dir >= 0 && page+dir < lines.length) {
			menus[page+pageOffset].shouldRender = false;
			page += dir;
			if(showPageCount) menus[0].updateSelectedEntry(new StringEntry(page==0 && hasTitle?"Title":(page+1)+"/"+lines.length, Color.BLACK));
			menus[page+pageOffset].shouldRender = true;
		}
	}
	
	@Override
	public void tick(InputHandler input) {
		if (input.getKey("menu").clicked || input.getKey("exit").clicked)
			Game.exitMenu(); // this is what closes the book; TODO if books were editable, I would probably remake the book here with the edited pages.
		if (input.getKey("cursor-left").clicked) turnPage(-1); // this is what turns the page back
		if (input.getKey("cursor-right").clicked) turnPage(1); // this is what turns the page forward
	}
}
