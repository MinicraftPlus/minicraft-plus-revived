package minicraft.level.structure;

import minicraft.entity.furniture.Furniture;
import minicraft.gfx.Point;
import minicraft.gfx.Rectangle;
import minicraft.level.Level;
import minicraft.level.ChunkPoint;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public abstract class MultiStructure extends GridGenStructure {
	public MultiStructure(int gridSize) {
		super(gridSize);
	}

	protected abstract Map<Structure, Set<Point>> placementsAt(Level level, int x, int y);

	public Set<ChunkPoint> affectedChunks(Level level, int xt, int yt) {
		HashSet<ChunkPoint> out = new HashSet<>();
		Map<Structure, Set<Point>> placements = placementsAt(level, xt, yt);

		for(Structure s : placements.keySet())
			for(Point p : placements.get(s))
				out.addAll(s.affectedChunks(level, p.x, p.y));

		return out;
	}

	public Set<Rectangle> getBounds(Level level, int xt, int yt) {
		HashSet<Rectangle> out = new HashSet<>();
		Map<Structure, Set<Point>> placements = placementsAt(level, xt, yt);

		for(Structure s : placements.keySet())
			for(Point p : placements.get(s))
				out.addAll(s.getBounds(level, p.x, p.y));

		return out;
	}

	public void draw(Level level, int xt, int yt) {
		Map<Structure, Set<Point>> placements = placementsAt(level, xt, yt);
		for(Structure s : placements.keySet())
			for(Point p : placements.get(s))
				s.draw(level, p.x, p.y);
	}
}
