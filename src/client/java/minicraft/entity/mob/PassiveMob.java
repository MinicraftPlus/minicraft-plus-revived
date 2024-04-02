package minicraft.entity.mob;

import minicraft.core.Game;
import minicraft.core.Updater;
import minicraft.core.io.Settings;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;

public class PassiveMob extends MobAi {
	protected int color;

	/**
	 * Constructor for a non-hostile (passive) mob.
	 * healthFactor = 3.
	 * @param sprites The mob's sprites.
	 */
	public PassiveMob(LinkedSprite[][] sprites) {
		this(sprites, 3);
	}

	/**
	 * Constructor for a non-hostile (passive) mob.
	 * @param sprites The mob's sprites.
	 * @param healthFactor Determines the mobs health. Will be multiplied by the difficulty
	 * 	and then added with 5.
	 */
	public PassiveMob(LinkedSprite[][] sprites, int healthFactor) {
		super(sprites, 5 + healthFactor * Settings.getIdx("diff"), 5 * 60 * Updater.normSpeed, 45, 40);
	}

	@Override
	public void handleDespawn() {
		if (isWithinLight()) return; // Do not despawn when it is within light.
		super.handleDespawn();
	}

	@Override
	public void render(Screen screen) {
		super.render(screen);
	}

	@Override
	public void randomizeWalkDir(boolean byChance) {
		if (xmov == 0 && ymov == 0 && random.nextInt(5) == 0 || byChance || random.nextInt(randomWalkChance) == 0) {
			randomWalkTime = randomWalkDuration;

			// Multiple at end ups the chance of not moving by 50%.
			xmov = (random.nextInt(3) - 1) * random.nextInt(2);
			ymov = (random.nextInt(3) - 1) * random.nextInt(2);
		}
	}

	public void die() {
		super.die(15);
	}

	/**
	 * Checks a given position in a given level to see if the mob can spawn there.
	 * Passive mobs can only spawn on grass or flower tiles.
	 * @param level The level which the mob wants to spawn in.
	 * @param x X map spawn coordinate.
	 * @param y Y map spawn coordinate.
	 * @return true if the mob can spawn here, false if not.
	 */
	public static boolean checkStartPos(Level level, int x, int y) {

		int r = (Game.isMode("minicraft.settings.mode.score") ? 22 : 15) + (Updater.getTime() == Updater.Time.Night ? 0 : 5); // Get no-mob radius by

		if (!MobAi.checkStartPos(level, x, y, 80, r))
			return false;

		Tile tile = level.getTile(x >> 4, y >> 4);
		return tile == Tiles.get("grass") || tile == Tiles.get("flower");

	}

	@Override
	public int getMaxLevel() {
		return 1;
	}
}
