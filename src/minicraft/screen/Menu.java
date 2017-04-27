package minicraft.screen;

import minicraft.Game;
import minicraft.InputHandler;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import java.util.List;

/** TODO Most all menus have a scroll feature... it should be in this class.*/
public abstract class Menu {
	protected Game game;
	protected InputHandler input;
	
	public void init(Game game, InputHandler input) {
		this.input = input;
		this.game = game;
	}

	public abstract void tick();
	
	public abstract void render(Screen screen);
	
	public void renderItemList(Screen screen, int xo, int yo, int x1, int y1,
	  List<? extends ListItem> listItems, int selected) {
		boolean renderCursor = true;
		if (selected < 0) {
			selected = -selected - 1;
			renderCursor = false;
		}
		int w = x1 - xo;
		int h = y1 - yo - 1;
		int i0 = 0;
		int i1 = listItems.size();
		if (i1 > h) i1 = h;
		int io = selected - h / 2;
		if (io > listItems.size() - h) io = listItems.size() - h;
		if (io < 0) io = 0;
		
		for (int i = i0; i < i1; i++) {
			listItems.get(i + io).renderInventory(screen, (1 + xo) * 8, (i + 1 + yo) * 8);
		}
		
		if (renderCursor) {
			int yy = selected + 1 - io + yo;
			Font.draw(">", screen, (xo + 0) * 8, yy * 8, Color.get(-1, 555, 555, 555));
			Font.draw("<", screen, (xo + w) * 8, yy * 8, Color.get(-1, 555, 555, 555));
		}
	}
	
	/** This renders the blue frame you see when you open up the crafting/inventory menus.
	 *  The width & height are based on 4 points (Staring x & y positions (0), and Ending x & y positions (1)). */
	protected static final void renderMenuFrame(Screen screen, String title, int x0, int y0, int x1, int y1, int sideColor, int midColor, int titleColor) {
		for (int y = y0; y <= y1; y++) { // loop through the height of the frame
			for (int x = x0; x <= x1; x++) { // loop through the width of the frame
				
				boolean xend = x == x0 || x == x1;
				boolean yend = y == y0 || y == y1;
				int spriteoffset = (xend && yend ? 0 : (yend ? 1 : 2)); // determines which sprite to use
				int mirrors = ( x == x1 ? 1 : 0 ) + ( y == y1 ? 2 : 0 ); // gets mirroring
				
				int color = xend || yend ? sideColor : midColor;//sideColor; // gets the color; slightly different in upper right corner, and middle is all blue.
				
				screen.render(x * 8, y * 8, spriteoffset + 13 * 32, color, mirrors);
			}
		}

		Font.draw(title, screen, x0 * 8 + 8, y0 * 8, titleColor);
	}
	
	/// the default, blue menu frame.
	protected void renderFrame(Screen screen, String title, int x0, int y0, int x1, int y1) {
		renderMenuFrame(screen, title, x0, y0, x1, y1, Color.get(-1, 1, 5, 445), Color.get(005, 005), Color.get(5, 5, 5, 550));
	}
}
