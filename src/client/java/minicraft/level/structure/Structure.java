package minicraft.level.structure;

import minicraft.entity.furniture.Furniture;
import minicraft.gfx.Point;
import minicraft.gfx.Rectangle;
import minicraft.level.Level;
import minicraft.level.ChunkPoint;

import java.util.Set;
import java.util.function.Consumer;

// this stores structures that can be drawn at any location.
public abstract class Structure {

	public Structure() {}

	public Structure(Structure struct) {}

	public abstract Set<Point> occurencesInChunk(Level level, int xc, int yc);
	public abstract Set<ChunkPoint> affectedChunks(Level level, int xt, int yt);
	public abstract Set<Rectangle> getBounds(Level level, int xt, int yt);

	public abstract void draw(Level level, int xt, int yt);
}
