package minicraft.gfx;

public class Insets {
	
	public int left, top, right, bottom;
	
	public Insets() { this(0); }
	public Insets(int dist) { this(dist, dist, dist, dist); }
	public Insets(int left, int top, int right, int bottom) {
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
	}
	
	public Rectangle addInsets(Rectangle r) {
		return new Rectangle(r.getLeft()-left, r.getTop()-top, r.getRight()+right, r.getBottom()+bottom, Rectangle.CORNERS);
	}
	
	public Rectangle subtractInsets(Rectangle r) {
		return new Rectangle(r.getLeft()+left, r.getTop()+top, r.getRight()-right, r.getBottom()-bottom, Rectangle.CORNERS);
	}
	
	public Dimension addInsets(Dimension d) {
		return new Dimension(d.width + left + right, d.height + top + bottom);
	}
	
	public Dimension subtractInsets(Dimension d) {
		return new Dimension(d.width - left - right, d.height - top - bottom);
	}
}
