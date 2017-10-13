package minicraft.screen;

import java.util.ArrayList;
import java.util.Arrays;

import minicraft.Game;
import minicraft.InputHandler;
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
	//private static java.awt.Rectangle textArea = new java.awt.Rectangle(15, 8*5, 8*32, 8*16);
	private static final int minX = 15, maxX = 15+8*32, minY = 8*5, maxY = 8*5+8*16;
	//private static final FontStyle style = new FontStyle(Color.BLACK).xCenterBounds(minX, maxX).yCenterBounds(minY, maxY);
	
	private String[][] lines;
	private int page;
	
	/*public Menu getMenu() {
		return new Menu(this,
			new Frame("", new Rectangle(14, 0, 21, 3, Rectangle.CORNERS)),
			new Frame("", new Rectangle(1, 4, 34, 20, Rectangle.CORNERS))
		).setFrameColors(Color.DARK_GRAY, Color.get(554, 554), Color.get(-1, 1, 554, 554));
	}*/
	
	public BookDisplay(String book) {
		//super(new Menu.Builder(3, StringEntry.useLines(Font.getLines())));
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
			//.setBounds(new Rectangle(14*8, 0, 21*8, 3*8, Rectangle.CORNERS))
			.setPositioning(new Point(Screen.w/2, 0), RelPos.BOTTOM)
			.setEntries(StringEntry.useLines(Color.BLACK, "Page", "Title"))
			.setSelection(1)
			.createMenu();
		
		// now create a menu for each page TODO this could be optimized to only update the entries, perhaps...
		
		builder
			//.setBounds(new Rectangle(1*8, 4*8, 34*8, 20*8, Rectangle.CORNERS))
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
	
	/*@Override
	public void render(Screen screen) {
		super.render(screen);
		
		if(page != 0) style.setXPos(minX); // center text on the title page
		else style.setXPos(-1); // don't center after title page
		
		style.yCenterBounds(minY, maxY);
		
		Font.drawParagraph(lines[page], screen, style, spacing); // draw the current page
	}*/
}
