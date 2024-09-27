package minicraft.entity;

import minicraft.gfx.Screen;
import minicraft.level.Level;
import minicraft.level.tile.ExplodedTile;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;

// This is a kind of tile entity. Maybe this should be savable.
public class ExplosionTileTicker extends Entity {
	private static final int EXPLOSION_TIME = 18; // 18 ticks == 0.3 second

	private final Level level;
	private final int x, y, r;

	private int tick = 0;

	public ExplosionTileTicker(Level level, int x, int y, int r) {
		super(0, 0);
		this.level = level;
		this.x = x;
		this.y = y;
		this.r = r;
		level.setAreaTiles(x, y, r, Tiles.get("explode"), 0, ExplosionTileTicker::explodeBlacklistCheck);
	}

	public static void addTicker(Level level, int x, int y, int r) {
		level.add(new ExplosionTileTicker(level, x, y, r), x * 16 + 8, y * 16 + 8);
	}

	private static boolean explodeBlacklistCheck(Tile tile, int x, int y) {
		return !Tiles.explosionBlacklist.contains(tile.id);
	}

	private static boolean explodedTileCheck(Tile tile, int x, int y) {
		return tile instanceof ExplodedTile;
	}

	@Override
	public void render(Screen screen) {}

	@Override
	public void tick() {
		if (tick == EXPLOSION_TIME) { // Does the explosion
			if (level.depth != 1) {
				level.setAreaTiles(x, y, r, Tiles.get("hole"), 0, ExplosionTileTicker::explodedTileCheck);
			} else {
				level.setAreaTiles(x, y, r, Tiles.get("Infinite Fall"), 0, ExplosionTileTicker::explodedTileCheck);
			}

			remove();
		}

		tick++;
	}
}
