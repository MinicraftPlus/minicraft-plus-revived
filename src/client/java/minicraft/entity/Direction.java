package minicraft.entity;

public enum Direction {

	NONE(0, 0), DOWN(0, 1), UP(0, -1), LEFT(-1, 0), RIGHT(1, 0);

	private final int x, y;

	Direction(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public static final Direction[] values = Direction.values();

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public static Direction getDirection(int xd, int yd) {
		if (xd == 0 && yd == 0)
			return Direction.NONE; // The attack was from the same entity, probably; or at least the exact same space.

		if (Math.abs(xd) > Math.abs(yd)) {
			// The x distance is more prominent than the y distance
			if (xd < 0)
				return Direction.LEFT;
			else
				return Direction.RIGHT;
		} else {
			if (yd < 0)
				return Direction.UP;
			else
				return Direction.DOWN;
		}
	}

	public static Direction getDirection(int dir) {
		return values[dir + 1];
	}

	public int getDir() {
		return ordinal() - 1;
	}
}
