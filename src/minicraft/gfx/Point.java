package minicraft.gfx;

public class Point {
	
	public int x, y;
	
	public Point() { this(0, 0); }
	public Point(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public Point(Point model) {
		x = model.x;
		y = model.y;
	}
	
	public String toString() {
		return "("+x+","+y+")";
	}
}
