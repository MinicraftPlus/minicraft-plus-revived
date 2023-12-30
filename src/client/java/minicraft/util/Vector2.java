package minicraft.util;

public class Vector2 {
	public double x, y;

	public Vector2(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public Vector2 normalized() {
		double max = Math.max(x, y);
		return new Vector2(x / max, y / max);
	}

	public static Vector2 normalize(Vector2 vec) {
		double max = Math.max(vec.x, vec.y);
		return new Vector2(vec.x / max, vec.y / max);
	}

	public static double distance(double x, double y) {
		return Math.sqrt(x * x + y * y);
	}
}
