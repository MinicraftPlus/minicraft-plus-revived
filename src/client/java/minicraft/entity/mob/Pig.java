package minicraft.entity.mob;

import minicraft.core.io.Settings;
import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.item.Items;

public class Pig extends PassiveMob {
	private static LinkedSprite[][] sprites = Mob.compileMobSpriteAnimations(0, 0, "pig");

	/**
	 * Creates a pig.
	 */
	public Pig() {
		super(sprites);
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
			max = 2;
		}

		dropItem(min, max, Items.get("raw pork"));

		super.die();
	}
}
