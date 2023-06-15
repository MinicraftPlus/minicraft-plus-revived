package minicraft.level.tile;

import minicraft.level.Level;

public interface BoostablePlant {
	/**
	 * Whether arcane fertilizer is able to be performed on the tile.
	 */
	boolean isValidBoostablePlantTarget(Level level, int x, int y);

	/**
	 * Whether arcane fertilizer can successfully be performed on the tile.
	 */
	boolean isPlantBoostSuccess(Level level, int x, int y);

	/**
	 * Performing arcane fertilizer with effects on the tile.
	 */
	void performPlantBoost(Level level, int x, int y);
}
