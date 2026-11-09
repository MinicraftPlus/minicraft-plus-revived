package minicraft.level;

import minicraft.level.tile.Tile;

public class TilePoint {
	public int x, y;
	public Tile t;

	public TilePoint(int x, int y, Tile tile) {
		this.x = x;
		this.y = y;
		this.t = tile;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof TilePoint)) return false;
		TilePoint p = (TilePoint) o;
		return x == p.x && y == p.y && t.id == p.t.id;
	}

	@Override
	public int hashCode() {
		return x + y * 51 + t.id * 131;
	}
}
