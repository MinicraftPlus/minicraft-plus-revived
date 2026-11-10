package minicraft.level.structure;

import minicraft.core.Game;
import minicraft.core.World;
import minicraft.entity.furniture.Furniture;
import minicraft.gfx.Point;
import minicraft.gfx.Rectangle;
import minicraft.level.ChunkManager;
import minicraft.level.ChunkPoint;
import minicraft.level.Level;
import minicraft.level.tile.Tiles;
import minicraft.util.Hash2;

import java.util.HashSet;
import java.util.Set;
import java.util.Collections;
import java.util.function.Consumer;

public class Stairs extends Structure {

	private final boolean up;
	private final int gridSize;

	public Stairs(boolean up, int grid) {
		this.up = up;
		this.gridSize = grid;
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
				long hash = Math.abs(Hash2.hash2(level.getSeed() ^ 593993 * (level.depth + (up ? 1 : 0)), x, y) % (gridSize * gridSize));
				int yt = (int)(hash / gridSize) + y * gridSize, xt = (int)(hash % gridSize) + x * gridSize;
				if(xt >= mintx && xt <= maxtx && yt >= minty && yt <= maxty)
					points.add(new Point(xt, yt));
			}

		return points;
	}

	public Set<ChunkPoint> affectedChunks(Level level, int xt, int yt) {
		int S = ChunkManager.CHUNK_SIZE;
		HashSet<ChunkPoint> affected = new HashSet<>();
		affected.add(new ChunkPoint(Math.floorDiv(xt, S), Math.floorDiv(yt, S), level.depth));
		affected.add(new ChunkPoint(Math.floorDiv(xt, S), Math.floorDiv(yt, S), up ? (level.depth + 1) : (level.depth - 1)));
		return affected;
	}

	public Set<Rectangle> getBounds(Level level, int x, int y) {
		return Collections.singleton(new Rectangle(x, y, 1, 1, Rectangle.CORNER_DIMS));
	}

	public void draw(Level level, int xt, int yt) {
		Level dest = Game.levels[World.lvlIdx(up ? (level.depth + 1) : (level.depth-1))];
		Level lo = up ? level : dest;
		Level hi = up ? dest : level;

		// On levels beneath sky, only allow stairs when it replaces rock or recorrects a surface stair
		if(hi.depth < 1 && hi.getTile(xt, yt) != Tiles.get("rock") && hi.getTile(xt, yt) != Tiles.get("Stairs Down"))
			return;
		// On levels going to sky, only allow stairs when it is in the air
		if(hi.depth == 1 && (hi.getTile(xt, yt) != Tiles.get("cloud") || lo.getTile(xt, yt) != Tiles.get("Grass")))
			return;

		// Level specific variation
		if(lo.depth == -4)
			Structures.dungeonGate.draw(lo, xt, yt);
		else if(hi.depth == 1)
			lo.setAreaTiles(xt, yt, 1, Tiles.get("Hard Rock"), 0);
		else
			lo.setAreaTiles(xt, yt, 1, Tiles.get("dirt"), 0);
		lo.setTile(xt, yt, Tiles.get("Stairs Up"));
		hi.setTile(xt, yt, Tiles.get("Stairs Down"));
	}

	@Override
	public int hashCode() { // random big-ish primes
		return up ? 593993 : 604997;
	}
}
