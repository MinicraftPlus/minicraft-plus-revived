package minicraft.entity.mob;

import minicraft.core.io.Settings;
import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.item.Items;
import minicraft.level.tile.GrassTile;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;

public class Cow extends PassiveMob {
	private static LinkedSprite[][] sprites = Mob.compileMobSpriteAnimations(0, 0, "cow");

	/**
	 * Creates the cow with the right sprites and color.
	 */
	public Cow() {
		super(sprites, 5);
	}

	public void die() {
		int min = 0, max = 0;
		if (Settings.get("diff").equals("minicraft.settings.difficulty.easy")) {
			min = 1;
			max = 3;
		}
		if (Settings.get("diff").equals("minicraft.settings.difficulty.normal")) {
			min = 1;
			max = 2;
		}
		if (Settings.get("diff").equals("minicraft.settings.difficulty.hard")) {
			min = 0;
			max = 1;
		}

		dropItem(min, max, Items.get("leather"), Items.get("raw beef"));

		super.die();
	}

	@Override
	public void tick() {
		super.tick();
		Tile tile = level.getTile(x >> 4, y >> 4);
		if (tile instanceof GrassTile && random.nextInt(1000) == 0) { // Grazing without any benefits.
			level.setTile(x >> 4, y >> 4, Tiles.get("dirt"));
		}
	}
}
