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
	
	public BookDisplay(String book) {
		page = 0;
		if(book == null) {
			book = defaultBook;
		}
		book = Localization.getLocalized(book);
		
		ArrayList<String[]> pages = new ArrayList<>();
		String[] splitContents = book.split("\0");
		for(String content: splitContents) {
			String[] remainder = {content};
			while(remainder[remainder.length-1].length() > 0) {
				remainder = Font.getLines(remainder[remainder.length-1], maxX-minX, maxY-minY, spacing);
				pages.add(Arrays.copyOf(remainder, remainder.length-1)); // removes the last element of remainder, which is the leftover.
			}
		}
		
		lines = pages.toArray(new String[pages.size()][]);
		
		Menu.Builder builder = new Menu.Builder(true, spacing, RelPos.CENTER)
			.setFrame(554, 1, 554);
		
		Menu pageCount = builder // the small rect for the title
			.setPositioning(new Point(Screen.w/2, 0), RelPos.BOTTOM)
			.setEntries(StringEntry.useLines(Color.BLACK, "Page", "Title"))
			.setSelection(1)
			.createMenu();
		
		builder
			.setPositioning(new Point(Screen.w/2, pageCount.getBounds().getBottom() + spacing), RelPos.BOTTOM)
			.setSize(maxX-minX + SpriteSheet.boxWidth*2, maxY-minY + SpriteSheet.boxWidth*2)
			.setShouldRender(false);
		
		menus = new Menu[lines.length+1];
		menus[0] = pageCount;
		for(int i = 0; i < lines.length; i++) {
			menus[i+1] = builder.setEntries(StringEntry.useLines(Color.BLACK, lines[i])).createMenu();
		}
		
		menus[page+1].shouldRender = true;
	}
	
	private void turnPage(int dir) {
		if(page+dir >= 0 && page+dir < lines.length) {
			menus[page+1].shouldRender = false;
			page += dir;
			menus[0].updateSelectedEntry(new StringEntry(page==0?"Title":page+"", Color.BLACK));
			menus[page+1].shouldRender = true;
		}
	}
	
	@Override
	public void tick(InputHandler input) {
		if (input.getKey("menu").clicked || input.getKey("exit").clicked)
			Game.exitMenu(); // this is what closes the book; TODO if books were editable, I would probably remake the book here with the edited pages.
		if (input.getKey("left").clicked) turnPage(-1); // this is what turns the page back
		if (input.getKey("right").clicked) turnPage(1); // this is what turns the page forward
	}
}
