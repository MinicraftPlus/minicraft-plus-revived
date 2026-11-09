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

public class Stairs extends GridGenStructure {

	private final boolean up;

	public Stairs(boolean up, int grid) {
		super(grid);
		this.up = up;
	}

	public Set<ChunkPoint> affectedChunks(Level level, int xt, int yt) {
		int S = ChunkManager.CHUNK_SIZE;
		HashSet<ChunkPoint> affected = new HashSet<>();
		affected.add(new ChunkPoint(Math.floorDiv(xt, S), Math.floorDiv(yt, S), level.depth));
		affected.add(new ChunkPoint(Math.floorDiv(xt, S), Math.floorDiv(yt, S), up ? (level.depth + 1) : (level.depth - 1)));
		if(up && level.depth == 0) {
			System.out.printf("Chunks affected at %d, %d (L%d) are %d, %d (L%d)\n", xt, yt, level.depth, Math.floorDiv(xt, S), Math.floorDiv(yt, S), level.depth + 1);
		}
		return affected;
	}

	public Set<Rectangle> getBounds(Level level, int x, int y) {
		return Collections.singleton(new Rectangle(x, y, 1, 1, Rectangle.CORNER_DIMS));
	}

	public void draw(Level level, int xt, int yt) {
		Level dest = Game.levels[World.lvlIdx(up ? (level.depth + 1) : (level.depth-1))];
		Level lo = up ? level : dest;
		Level hi = up ? dest : level;

		if(lo.getTile(xt, yt) == Tiles.get("Stairs Down"))
			return;
		// On levels beneath sky, only allow stairs when it replaces rock
		if(hi.depth < 1 && hi.getTile(xt, yt) != Tiles.get("rock"))
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
}
