package minicraft.gfx;

import minicraft.screen.RelPos;

public class Rectangle {

	public static final int CORNER_DIMS = 0;
	public static final int CORNERS = 1;
	public static final int CENTER_DIMS = 2;

	private int x, y, w, h;

	public Rectangle() {
	} // 0 all.

	public Rectangle(int x, int y, int x1, int y1, int type) {
		if (type < 0 || type > 2) type = 0;

		if (type != CENTER_DIMS) { // x and y are the coords of the top left corner.
			this.x = x;
			this.y = y;
		} else { // x and y are the coords of the center.
			this.x = x - x1 / 2;
			this.y = y - y1 / 2;
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
		this(false, p, d);
	}

	public Rectangle(boolean isCenter, Point p, Dimension d) {
		this(p.x, p.y, d.width, d.height, isCenter ? CENTER_DIMS : CORNER_DIMS);
	}

	public Rectangle(Rectangle model) {
		x = model.x;
		y = model.y;
		w = model.w;
		h = model.h;
	}

	public int getLeft() {
		return x;
	}

	public int getRight() {
		return x + w;
	}

	public int getTop() {
		return y;
	}

	public int getBottom() {
		return y + h;
	}

	public int getWidth() {
		return w;
	}

	public int getHeight() {
		return h;
	}

	public Point getCenter() {
		return new Point(x + w / 2, y + h / 2);
	}

	public Dimension getSize() {
		return new Dimension(w, h);
	}

	public Point getPosition(RelPos relPos) {
		Point p = new Point(x, y);
		p.x += relPos.xIndex * w / 2;
		p.y += relPos.yIndex * h / 2;
		return p;
	}

	public boolean intersects(Rectangle other) {
		return !(getLeft() > other.getRight() // Left side is past the other right side
			|| other.getLeft() > getRight() // Other left side is past the right side
			|| getBottom() < other.getTop() // Other top is below the bottom
			|| other.getBottom() < getTop() // Top is below the other bottom
		);
	}

	public void setPosition(Point p, RelPos relPos) {
		setPosition(p.x, p.y, relPos);
	}

	public void setPosition(int x, int y, RelPos relPos) {
		this.x = x - relPos.xIndex * w / 2;
		this.y = y - relPos.yIndex * h / 2;
	}

	public void translate(int xoff, int yoff) {
		x += xoff;
		y += yoff;
	}

	public void setSize(Dimension d, RelPos anchor) {
		setSize(d.width, d.height, anchor);
	}

	public void setSize(int width, int height, RelPos anchor) {
		Point p = getPosition(anchor);
		this.w = width;
		this.h = height;
		setPosition(p, anchor);
	}

	public String toString() {
		return super.toString() + "[center=" + getCenter() + "; size=" + getSize() + "]";
	}
}
