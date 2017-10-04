package minicraft.screen;

import java.awt.Dimension;
import java.awt.Point;

import minicraft.gfx.Rectangle;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

// stands for "Relative Position"
public enum RelPos {
	TOP_LEFT, TOP, TOP_RIGHT,
	LEFT, CENTER, RIGHT,
	BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT;
	
	public int xPos, yPos;
	private int xIndex, yIndex;
	
	RelPos() {
		int ord = ordinal();
		int pos = values().length - 1 - ord; // reverses it, to fit with the indexes easily pointing to the upper right corner of where to draw.
		xPos = ord % 3;
		yPos = ord / 3;
		
		xIndex = pos % 3;
		yIndex = pos / 3;
	}
	
	/// this method returns a Rectangle of the given size, such that it is in the corresponding position relative to the given anchor.
	@NotNull
	@Contract(pure = true)
	public Rectangle getRect(Dimension size, Point anchor) { return getRect(size.width, size.height, anchor.x, anchor.y); }
	public Rectangle getRect(int width, int height, int anchorX, int anchorY) {
		int x = anchorX - width/2 * xIndex;
		int y = anchorY - height/2 * yIndex;
		
		return new Rectangle(x, y, width, height, Rectangle.DIMS);
	}
}
