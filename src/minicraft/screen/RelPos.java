package minicraft.screen;

public enum RelPos {
	TOP_LEFT, TOP, TOP_RIGHT,
	LEFT, CENTER, RIGHT,
	BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT;
	
	public int positioningIndex() {
		return values.length - 1 - ordinal(); // reverses it, to fit with the indexes easily pointing to the upper right corner of where to draw.
	}
	
	private static final MenuData.RelPos[] values = MenuData.RelPos.values();
}
