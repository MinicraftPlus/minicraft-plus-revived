package minicraft.screen;

import minicraft.gfx.*;

public class Frame {
	
	private Rectangle bounds = null;
	private String title = "";
	
	private int titleColor, midColor, sideColor;
	private boolean setColors = false;
	
	public Frame(String title, Rectangle bounds) { this(title, bounds, true); }
	public Frame(String title, Rectangle bounds, boolean convertSize) {
		this.title = title;
		setBounds(bounds, convertSize);
		//titleColor = Color.get(-1, 555); // the default title color, if there's not a frame.
		setColors(Color.get(5, 5, 5, 550), Color.get(5, 5), Color.get(-1, 1, 5, 445)); // this will probably be the case very frequently, so it's the defualt, if there's a frame.
	}
	
	public Frame setBounds(Rectangle rect) { return setBounds(rect, true); }
	public Frame setBounds(Rectangle rect, boolean convertSize) {
		if(!convertSize)
			frame = rect;
		else if(rect != null) /// the rect "coordinates" are in "sprite pixels"; that means that they actually [SpriteSheet.boxWidth] times bigger in actual screen coordinates. so, we'll transform them:
			frame = new Rectangle(rect.getLeft()*SpriteSheet.boxWidth, rect.getTop()*SpriteSheet.boxWidth, rect.getWidth()*SpriteSheet.boxWidth, rect.getHeight()*SpriteSheet.boxWidth);
		
		//if(frame != null && !setColors)
			//setColors(Color.get(5, 5, 5, 550), Color.get(5, 5), Color.get(-1, 1, 5, 445)); // this will probably be the case very frequently, so it's the defualt, if there's a frame.
		
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
		
		// draws a title for the first frame in the array.
		if(title.length() > 0)
			Font.draw(title, screen, frame.getLeft() + SpriteSheet.boxWidth, frame.getTop(), titleColor);
	}
	
}
