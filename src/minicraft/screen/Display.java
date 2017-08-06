package minicraft.screen;

import java.awt.Point;
import java.util.Arrays;
import java.util.List;
import minicraft.Game;
import minicraft.InputHandler;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.FontStyle;
import minicraft.gfx.Rectangle;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteSheet;

public abstract class Display {
	protected Game game;
	protected InputHandler input;
	
	protected String[] text = null;
	private int spacing = Font.textHeight();
	private FontStyle style = null;
	
	public void init(Game game, InputHandler input) {
		this.input = input;
		this.game = game;
	}
	
	private Rectangle[] frames = null;
	private String title = "";
	
	private int titleColor, sideColor, midColor;
	
	protected Display() {
		setFrameColors(Color.get(-1, 1, 5, 445), Color.get(005, 005), Color.get(5, 5, 5, 550)); // this will probably be the case very frequently, so it's the defualt.
	}
	
	protected Display setFrameBounds(Rectangle rect) { return setFrameBounds(rect, true); }
	protected Display setFrameBounds(Rectangle[] rects) { return setFrameBounds(rects, true); }
	protected Display setFrameBounds(Rectangle rect, boolean convertSize) { return setFrameBounds((new Rectangle[] {rect}), true); }
	protected Display setFrameBounds(Rectangle[] rects, boolean convertSize) {
		if(!convertSize)
			frames = rects;
		else if(rects != null) { /// the rect "coordinates" are in "sprite pixels"; that means that they actually [SpriteSheet.boxWidth] times bigger in actual screen coordinates. so, we'll transform them:
			frames = new Rectangle[rects.length];
			for(int i = 0; i < rects.length; i++)
				frames[i] = new Rectangle(rects[i].getLeft()*SpriteSheet.boxWidth, rects[i].getTop()*SpriteSheet.boxWidth, rects[i].getWidth()*SpriteSheet.boxWidth, rects[i].getHeight()*SpriteSheet.boxWidth);
		}
		
		return this;
	}
	protected Display setFrameColors(int sideCol, int midCol, int titleCol) {
		titleColor = titleCol;
		sideColor = sideCol;
		midColor = midCol;
		return this;
	}
	
	protected Display setTitle(String title) { return setTitle(title, false); }
	protected Display setTitle(String title, boolean keepTitleColor) {
		this.title = title;
		if(frame == null && !keepTitleColor)
			titleColor = Color.get(-1, 555); // this is a little convience thing, so that if I have no frame, the title color defaults to white.
		return this;
	}
	
	/** sets the lines of text to display. The complexity is all to prevent the object that set the value from maintaining a reference to the new text array, which would happen if it was set directly. Though... maybe I want that to be possible...? Nah, it would be nice for convenience, but it ruins encapsulation... *sigh*... */
	/*protected Display setText(String[] text) { return setText(Arrays.asList(text)); }
	protected Display setText(List<String> text) {
		this.text = text.toArray(new String[text.size()]);
		return this;
	}*/
	protected Display setTextStyle(FontStyle style) { this.style = style; return this; }
	protected Display setLineSpacing(int spacing) { this.spacing = spacing; return this; }
	
	/** The behavior method. At this level, nothing is known of the behavior of the display, so it is abstract. */
	public abstract void tick();
	
	public void render(Screen screen) {
		renderFrame(screen);
		
		if(title != null && frame == null) // draw the title centered at the top of the screen, if there's no frame.
			Font.drawCentered(title, screen, SpriteSheet.boxWidth, titleColor);
		
		if(text != null) {
			if(style == null) style = new FontStyle(Color.get(-1, 555));
			for(int i = 0; i < text.length; i++)
				renderLine(screen, style, i);
		}
	}
	
	public void renderLine(Screen screen, FontStyle style, int lineIndex) {
		style.drawParagraphLine(text, lineIndex, screen);
	}
	
	/** This renders the blue frame you see when you open up the crafting/inventory menus.
	 *  The width & height are based on 4 points (Staring x & y positions (0), and Ending x & y positions (1)). */
	protected void renderFrame(Screen screen) {
		if(frame == null) return;
		
		for (int y = frame.getTop(); y <= frame.getBottom(); y+=SpriteSheet.boxWidth) { // loop through the height of the frame
			for (int x = frame.getLeft(); x <= frame.getRight(); x+=SpriteSheet.boxWidth) { // loop through the width of the frame
				
				boolean xend = x == frame.getLeft() || x == frame.getRight();
				boolean yend = y == frame.getTop() || y == frame.getBottom();
				int spriteoffset = (xend && yend ? 0 : (yend ? 1 : 2)); // determines which sprite to use
				int mirrors = ( x == frame.getRight() ? 1 : 0 ) + ( y == frame.getBottom() ? 2 : 0 ); // gets mirroring
				
				int color = xend || yend ? sideColor : midColor;//sideColor; // gets the color; slightly different in upper right corner, and middle is all blue.
				
				screen.render(x, y, spriteoffset + 13*32, color, mirrors);
			}
		}
		
		if(title.length() > 0)
			Font.draw(title, screen, frame.getLeft() + SpriteSheet.boxWidth, frame.getTop(), titleColor);
	}
}
