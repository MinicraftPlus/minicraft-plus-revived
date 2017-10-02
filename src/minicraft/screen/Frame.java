package minicraft.screen;

import minicraft.gfx.*;

public class Frame {
	
	private Rectangle bounds = null;
	private String title = "";
	
	private int titleColor, midColor, sideColor;
	private boolean setColors = false;
	
	public Frame(String title, Rectangle bounds) { this(title, bounds, true); }
	public Frame(String title, Rectangle bounds, boolean convertSize) {
		this(title, bounds, convertSize, Color.get(5, 5, 5, 550), Color.get(5, 5), Color.get(-1, 1, 5, 445)); // this will probably be the case very frequently, so it's the defualt, if there's a bounds.
	}
	
	public Frame(String title, Rectangle bounds, boolean convertSize, int titleCol, int midCol, int sideCol) {
		this.title = title;
		setBounds(bounds, convertSize);
		//titleColor = Color.WHITE; // the default title color, if there's not a bounds.
		setColors(titleCol, midCol, sideCol);
	}
	
	public Frame setTitle(String title) { this.title = title; return this; }
	
	public Frame setBounds(Rectangle rect) { return setBounds(rect, true); }
	public Frame setBounds(Rectangle rect, boolean convertSize) {
		if(!convertSize)
			bounds = rect;
		else if(rect != null) /// the rect "coordinates" are in "sprite pixels"; that means that they actually [SpriteSheet.boxWidth] times bigger in actual screen coordinates. so, we'll transform them:
			bounds = new Rectangle(rect.getLeft()*SpriteSheet.boxWidth, rect.getTop()*SpriteSheet.boxWidth, rect.getWidth()*SpriteSheet.boxWidth, rect.getHeight()*SpriteSheet.boxWidth);
		
		//if(bounds != null && !setColors)
			//setColors(Color.get(5, 5, 5, 550), Color.get(5, 5), Color.get(-1, 1, 5, 445)); // this will probably be the case very frequently, so it's the defualt, if there's a bounds.
		
		return this;
	}
	
	public Frame setColors(int titleCol, int midCol, int sideCol) {
		setColors = true;
		
		titleColor = titleCol;
		midColor = midCol;
		sideColor = sideCol;
		
		return this;
	}
	
	public void render(Screen screen) {
		if(bounds == null) return;
		
		for (int y = bounds.getTop(); y <= bounds.getBottom(); y+=SpriteSheet.boxWidth) { // loop through the height of the bounds
			for (int x = bounds.getLeft(); x <= bounds.getRight(); x+=SpriteSheet.boxWidth) { // loop through the width of the bounds
				
				boolean xend = x == bounds.getLeft() || x == bounds.getRight();
				boolean yend = y == bounds.getTop() || y == bounds.getBottom();
				int spriteoffset = (xend && yend ? 0 : (yend ? 1 : 2)); // determines which sprite to use
				int mirrors = ( x == bounds.getRight() ? 1 : 0 ) + ( y == bounds.getBottom() ? 2 : 0 ); // gets mirroring
				
				int color = xend || yend ? sideColor : midColor;//sideColor; // gets the color; slightly different in upper right corner, and middle is all blue.
				
				screen.render(x, y, spriteoffset + 13*32, color, mirrors);
			}
		}
		
		// draws a title in a standardized spot.
		if(title.length() > 0)
			Font.draw(title, screen, bounds.getLeft() + SpriteSheet.boxWidth, bounds.getTop(), titleColor);
	}
	
}
