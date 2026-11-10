package minicraft.level.structure;

import minicraft.level.ChunkManager;
import minicraft.level.Level;
import minicraft.gfx.Point;
import minicraft.util.Hash2;

import java.util.HashSet;
import java.util.Set;

/*
 * Abstract subset of Structure that generates randomly using a grid
 * */
public abstract class GridGenStructure extends Structure {
	private final int gridSize;
	public long seedOffset = 0;

	public GridGenStructure(int gridSize) {
		this.gridSize = gridSize;
	}

	public Set<Point> occurencesInChunk(Level level, int xc, int yc) {
		int s = ChunkManager.CHUNK_SIZE;
		// Get affected range of tiles
		int mintx = xc * s, maxtx = mintx + s - 1;
		int minty = yc * s, maxty = minty + s - 1;
		// Get affected range of grid cells
		int mingx = Math.floorDiv(mintx, gridSize),
			maxgx = Math.floorDiv(maxtx, gridSize),
			mingy = Math.floorDiv(minty, gridSize),
			maxgy = Math.floorDiv(maxty, gridSize);
		// Find any randomly generated occurences of structure in the given grid & tile range
		HashSet<Point> points = new HashSet<>();
		for(int x = mingx; x <= maxgx; x++)
			for(int y = mingy; y <= maxgy; y++) {
				long hash = Math.abs(Hash2.hash2(level.getSeed() ^ seedOffset ^ (level.depth * 593993), x, y) % (gridSize * gridSize));
				int yt = (int)(hash / gridSize) + y * gridSize, xt = (int)(hash % gridSize) + x * gridSize;
				if(xt >= mintx && xt <= maxtx && yt >= minty && yt <= maxty)
					points.add(new Point(xt, yt));
			}

		return points;
	}

	@Override
	public int hashCode() {
		return (int)seedOffset;
	}
}
