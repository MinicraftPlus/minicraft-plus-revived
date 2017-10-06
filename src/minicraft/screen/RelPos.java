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
	
	// I think this way, the enums will all be constructed before this gets called, so there won't be any mishaps with number of values.
	static {
		int length = values().length;
		for(RelPos rp: RelPos.values()) {
			int ord = rp.ordinal();
			int pos = length - 1 - ord; // reverses it, to fit with the indexes easily pointing to the upper right corner of where to draw.
			rp.xPos = ord % 3;
			rp.yPos = ord / 3;
			
			rp.xIndex = pos % 3;
			rp.yIndex = pos / 3;
		}
	}
	
	/// this method returns a Rectangle of the given size, such that it is in the corresponding position relative to the given anchor.
	@NotNull
	@Contract(pure = true)
	public Rectangle getRect(Dimension size, Point anchor) {
		return new Rectangle(positionRect(size, anchor), size);
	}
	public Rectangle getRect(int width, int height, int anchorX, int anchorY) {
		return new Rectangle(positionRect(width, height, anchorX, anchorY), new Dimension(width, height));
	}
	
	public Point positionRect(Dimension size, Point anchor) {
		return positionRect(size.width, size.height, anchor.x, anchor.y);
	}
	public Point positionRect(int width, int height, int anchorX, int anchorY) {
		int x = anchorX - width/2 * xIndex;
		int y = anchorY - height/2 * yIndex;
		
		return new Point(x, y);
	}
	
	
	public Point positionSubRect(final Dimension innerDim, Dimension container, Point anchor) {
		Dimension workingDim = new Dimension(innerDim);
		// set the right dims to use RelPos.getRect, which positions the title
		if(xPos != 1) // is on left or right
			workingDim.width = container.width;
		if(yPos != 1) // is on top or bottom
			workingDim.height = container.height;
		
		Point innerAnchor = positionRect(workingDim, anchor);
		
		// correct for not drawing past right border
		if(xPos == 2) // on right
			innerAnchor.x -= innerDim.width;
		
		// correct for not drawing past bottom border
		if(yPos == 2) // on bottom
			innerAnchor.y -= innerDim.height;
		
		return innerAnchor;
	}
}
