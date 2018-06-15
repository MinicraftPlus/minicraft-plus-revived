package minicraft.screen;

import minicraft.core.MyUtils;
import minicraft.gfx.Dimension;
import minicraft.gfx.Point;
import minicraft.gfx.Rectangle;

// stands for "Relative Position"
public enum RelPos {
	TOP_LEFT, TOP, TOP_RIGHT,
	LEFT, CENTER, RIGHT,
	BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT;
	
	public int xIndex, yIndex;
	//private int xIndex, yIndex;
	
	// I think this way, the enums will all be constructed before this gets called, so there won't be any mishaps with number of values.
	static {
		//int length = values().length;
		for(RelPos rp: RelPos.values()) {
			int ord = rp.ordinal();
			//int pos = length - 1 - ord; // reverses it, to fit with the indexes easily pointing to the upper right corner of where to draw.
			rp.xIndex = ord % 3;
			rp.yIndex = ord / 3;
		}
	}
	
	public static RelPos getPos(int xIndex, int yIndex) {
		return values()[MyUtils.clamp(xIndex, 0, 2) + MyUtils.clamp(yIndex, 0, 2)*3];
	}
	
	public RelPos getOpposite() {
		int nx = -(xIndex-1) + 1;
		int ny = -(yIndex-1) + 1;
		return getPos(nx, ny);
	}
	
	/// this method returns a Rectangle of the given size, such that it is in the corresponding position relative to the given anchor.
	/*@NotNull
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
	}*/
	
	/** positions the given rect around the given anchor. The double size is what aligns it to a point rather than a rect. */
	public Point positionRect(Dimension rectSize, Point anchor) {
		Rectangle bounds = new Rectangle(anchor.x, anchor.y, rectSize.width*2, rectSize.height*2, Rectangle.CENTER_DIMS);
		return positionRect(rectSize, bounds);
	}
	// the point is returned as a rectangle with the given dimension and the found location, within the provided dummy rectangle.
	public Rectangle positionRect(Dimension rectSize, Point anchor, Rectangle dummy) {
		Point pos = positionRect(rectSize, anchor);
		dummy.setSize(rectSize, RelPos.TOP_LEFT);
		dummy.setPosition(pos, RelPos.TOP_LEFT);
		return dummy;
	}
	
	/** positions the given rect to a relative position in the container. */ 
	public Point positionRect(Dimension rectSize, Rectangle container) {
		Point tlcorner = container.getCenter();
		
		// this moves the inner box correctly
		tlcorner.x += ((xIndex -1) * container.getWidth() / 2) - (xIndex * rectSize.width / 2);
		tlcorner.y += ((yIndex -1) * container.getHeight() / 2) - (yIndex * rectSize.height / 2);
		/*
		if(xPos == -1) // minus (half container width - half width) OR minus half container width
			anchor.x = container.getLeft(); // -1 * c.w/2 - 0*w/2
		else if(xPos == 0) // nothing OR minus half width
			anchor.x -= rectSize.width/2; // 0 * c.w/2 - 1*w/2
		else if(xPos == 1) // minus half width, plus half container width OR plus half container width - width
			anchor.x = container.getRight() - rectSize.width; // 1 * c.w/2 - 2*w/2*/
		
		return tlcorner;
	}
	
	// the point is returned as a rectangle with the given dimension and the found location, within the provided dummy rectangle.
	public Rectangle positionRect(Dimension rectSize, Rectangle container, Rectangle dummy) {
		Point pos = positionRect(rectSize, container);
		dummy.setSize(rectSize, RelPos.TOP_LEFT);
		dummy.setPosition(pos, RelPos.TOP_LEFT);
		return dummy;
	}
	
	/*public Point positionSubRect(final Dimension innerDim, Dimension container, Point anchor) {
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
	}*/
}
