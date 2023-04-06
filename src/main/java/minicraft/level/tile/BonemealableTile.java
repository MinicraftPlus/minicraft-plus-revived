package minicraft.level.tile;

import minicraft.level.Level;

public interface BonemealableTile {
	/**
	 * Whether bonemeal is able to be performed on the tile.
	 */
	boolean isValidBonemealTarget(Level level, int x, int y);

	/**
	 * Whether bonemeal can successfully be performed on the tile.
	 */
	boolean isBonemealSuccess(Level level, int x, int y);

	/**
	 * Performing bonemeal with effects on the tile.
	 */
	void performBonemeal(Level level, int x, int y);
}
