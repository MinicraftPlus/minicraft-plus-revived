package minicraft.entity;

public enum Axis2D {
	X, Y;

	public Direction getDirection(int displacement) {
		if (this == X)
			return Direction.getDirection(displacement, 0);
		else
			return Direction.getDirection(0, displacement);
	}
}
