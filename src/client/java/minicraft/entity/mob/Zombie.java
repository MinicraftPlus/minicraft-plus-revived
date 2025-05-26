package minicraft.entity.mob;

import minicraft.core.io.Settings;
import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.item.Items;

public class Zombie extends EnemyMob {
	private static LinkedSprite[][][] sprites = new LinkedSprite[][][] {
		Mob.compileMobSpriteAnimations(0, 0, "zombie"),
		Mob.compileMobSpriteAnimations(0, 2, "zombie"),
		Mob.compileMobSpriteAnimations(0, 4, "zombie"),
		Mob.compileMobSpriteAnimations(0, 6, "zombie")
	};

	/**
	 * Creates a zombie of the given level.
	 * @param lvl Zombie's level.
	 */
	public Zombie(int lvl) {
		super(lvl, sprites, 5, 100);
	}

	public void die() {
		if (Settings.get("diff").equals("minicraft.settings.difficulty.easy")) dropItem(2, 4, Items.getStackOf("cloth"));
		if (Settings.get("diff").equals("minicraft.settings.difficulty.normal")) dropItem(1, 3, Items.getStackOf("cloth"));
		if (Settings.get("diff").equals("minicraft.settings.difficulty.hard")) dropItem(1, 2, Items.getStackOf("cloth"));

		if (random.nextInt(60) == 2) {
			level.dropItem(x, y, Items.getStackOf("iron"));
		}

		if (random.nextInt(40) == 19) {
			int rand = random.nextInt(3);
			if (rand == 0) {
				level.dropItem(x, y, Items.getStackOf("green clothes"));
			} else if (rand == 1) {
				level.dropItem(x, y, Items.getStackOf("red clothes"));
			} else if (rand == 2) {
				level.dropItem(x, y, Items.getStackOf("blue clothes"));
			}
		}

		if (random.nextInt(100) < 4) {
			level.dropItem(x, y, Items.getStackOf("Potato"));
		}

		super.die();
	}
}
