package minicraft.gfx;

import java.awt.Dimension;
import java.awt.Point;

public class Rectangle {
	
	public static final int DIMS = 0;
	public static final int CORNERS = 1;
	public static final int CENTER = 2;
	
	private int x, y, w, h;
	
	public Rectangle(int x, int y, int x1, int y1) { this(x, y, x1, y1, DIMS); }
	public Rectangle(int x, int y, int x1, int y1, int type) {
		if(type < 0 || type > 2) type = 0;
		
		if (type != CENTER) { // x and y are the coords of the top left corner.
			this.x = x;
			this.y = y;
		} else { // x and y are the coords of the center.
			this.x = x - x1/2;
			this.y = y - y1/2;
		}
		
		if (type != CORNERS) { // x1 and y1 are the width and height.
			this.w = x1;
			this.h = y1;
		} else { // x1 and y1 are the coords of the bottom right corner.
			this.w = x1 - x;
			this.h = y1 - y;
		}
	}
	
	public Rectangle(Point p, Dimension d) {
		x = p.x;
		y = p.y;
		w = d.width;
		h = d.height;
	}
	
	public int getLeft() { return x; }
	public int getRight() { return x + w; }
	public int getTop() { return y; }
	public int getBottom() { return y + h; }
	
	public int getWidth() { return w; }
	public int getHeight() { return h; }
	
	public boolean intersects(Rectangle other) {
		return !( getLeft() > other.getRight() // left side is past the other right side
		  || other.getLeft() > getRight() // other left side is past the right side
		  || getBottom() > other.getTop() // top is below the other bottom
		  || other.getBottom() > getTop() // other top is below the bottom
		);
	}
}
