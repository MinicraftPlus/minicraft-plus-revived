package minicraft.level;

import minicraft.util.Hash2;

public class ChunkPoint {
	public int x, y, level;

	public ChunkPoint(int x, int y, int level) {
		this.x = x;
		this.y = y;
		this.level = level;
	}

	@Override
	public boolean equals(Object o) {
		if(!(o instanceof ChunkPoint)) return false;
		ChunkPoint p = (ChunkPoint) o;
		return x == p.x && y == p.y && level == p.level;
	}

	@Override
	public int hashCode() {
		return (int)(Hash2.hash2(level, x, y) % Integer.MAX_VALUE);
	}
}
