package minicraft.gfx;

public class Dimension {

	public int width, height;

	public Dimension() {
		this(0, 0);
	}

	public Dimension(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public Dimension(Dimension model) {
		width = model.width;
		height = model.height;
	}

	public String toString() {
		return width + "x" + height;
	}
}
