package minicraft.screen;

import java.util.ArrayList;
import java.util.Arrays;

import minicraft.Game;
import minicraft.InputHandler;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.FontStyle;
import minicraft.gfx.Rectangle;
import minicraft.gfx.Screen;

public class BookDisplay extends Display {
	
	// null characters "\0" denote page breaks.
	private static final String defaultBook = " \n \0"+"There is nothing of use here."+"\0 \0"+"Still nothing... :P";
	
	private static final int spacing = 3;
	//private static java.awt.Rectangle textArea = new java.awt.Rectangle(15, 8*5, 8*32, 8*16);
	private static final int minX = 15, maxX = 15+8*32, minY = 8*5, maxY = 8*5+8*16;
	private static final FontStyle style = new FontStyle(Color.get(-1, 000)).xCenterBounds(minX, maxX).yCenterBounds(minY, maxY);
	
	private String[][] lines;
	private int page;
	
	@Override
	public Menu getMenu() {
		return new Menu(this,
			new Frame("", new Rectangle(14, 0, 21, 3, Rectangle.CORNERS)),
			new Frame("", new Rectangle(1, 4, 34, 20, Rectangle.CORNERS))
		).setFrameColors(Color.get(-1, 222), Color.get(554, 554), Color.get(-1, 1, 554, 554));
	}
	
	public BookDisplay(String book) {
		/*super();
		setFrames(new Frame[] {
			(), // renders the tiny, page number display frame.
			() // renders the big text content display frame.
		});
		//setTextStyle(new FontStyle());
		*/
		page = 0;
		if(book == null)
			book = defaultBook;
		
		ArrayList<String[]> pages = new ArrayList<String[]>();
		String[] splitContents = book.split("\0");
		for(String content: splitContents) {
			String[] remainder = {content};
			while(remainder[remainder.length-1].length() > 0) {
				remainder = Font.getLines(remainder[remainder.length-1], maxX-minX, maxY-minY, spacing);
				pages.add(Arrays.copyOf(remainder, remainder.length-1)); // removes the last element of remainder, which is the leftover.
			}
		}
		
		lines = pages.toArray(new String[pages.size()][]);
	}
	
	public void tick(InputHandler input) {
		if (input.getKey("menu").clicked || input.getKey("exit").clicked)
			Game.setMenu((Menu)null); // this is what closes the book; TODO if books were editable, I would probably remake the book here with the edited pages.
		if (input.getKey("left").clicked && page > 0) page--; // this is what turns the page back
		if (input.getKey("right").clicked && page < lines.length-1) page++; // this is what turns the page forward
	}
	
	public void render(Screen screen) {
		//renderFrames(screen);
		//new Frame("", new Rectangle(14, 0, 21, 3, Rectangle.CORNERS)).setColors(Color.get(-1, 222), Color.get(554, 554), Color.get(-1, 1, 554, 554)).render(screen);
		//new Frame("", new Rectangle(1, 4, 34, 20, Rectangle.CORNERS)).setColors(Color.get(-1, 222), Color.get(554, 554), Color.get(-1, 1, 554, 554)).render(screen);
		
		// This draws the text "Page" at the top of the screen
		Font.draw("Page", screen, 8 * 15 + 8, 1 * 8 - 2, Color.get(-1, 0));
		
		// This is makes the numbers appear below "Page" // ...but it doesn't work...
		String pagenum = page==0?"Title": page+"";
		Font.drawCentered(pagenum, screen, /*11*11 + (page==0 ? 4 : 21-3*digits(page)), */2 * 8, Color.get(-1, 0));
		
		if(page != 0) style.setXPos(minX); // center text on the title page
		else style.setXPos(-1); // don't center after title page
		
		Font.drawParagraph(lines[page], screen, style, spacing); // draw the current page
	}
}
