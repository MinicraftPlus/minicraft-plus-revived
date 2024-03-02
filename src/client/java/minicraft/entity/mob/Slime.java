package minicraft.entity.mob;

import minicraft.core.Game;
import minicraft.core.io.Settings;
import minicraft.entity.Direction;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.item.Items;

public class Slime extends EnemyMob {
	private static LinkedSprite[][][] sprites = new LinkedSprite[][][] {
		new LinkedSprite[][] { Mob.compileSpriteList(0, 0, 2, 2, 0, 2, "slime") },
		new LinkedSprite[][] { Mob.compileSpriteList(0, 2, 2, 2, 0, 2, "slime") },
		new LinkedSprite[][] { Mob.compileSpriteList(0, 4, 2, 2, 0, 2, "slime") },
		new LinkedSprite[][] { Mob.compileSpriteList(0, 6, 2, 2, 0, 2, "slime") }
	};

	private int jumpTime = 0; // jumpTimer, also acts as a rest timer before the next jump

	/**
	 * Creates a slime of the given level.
	 * @param lvl Slime's level.
	 */
	public Slime(int lvl) {
		super(lvl, sprites, 1, true, 50, 60, 40);
	}

	@Override
	public void tick() {
		super.tick();

		/// jumpTime from 0 to -10 (or less) is the slime deciding where to jump.
		/// 10 to 0 is it jumping.

		if (jumpTime <= -10 && (xmov != 0 || ymov != 0))
			jumpTime = 10;

		jumpTime--;
		if (jumpTime == 0) {
			xmov = ymov = 0;
		}
	}

	@Override
	public void randomizeWalkDir(boolean byChance) {
		if (jumpTime > 0) return; // Direction cannot be changed if slime is already jumping.
		super.randomizeWalkDir(byChance);
	}

	@Override
	public boolean move(int xd, int yd) {
		boolean result = super.move(xd, yd);
		dir = Direction.DOWN;
		return result;
	}

	@Override
	public void render(Screen screen) {
		int oldy = y;
		if (jumpTime > 0) {
			walkDist = 8; // Set to jumping sprite.
			y -= 4; // Raise up a bit.
		} else walkDist = 0; // Set to ground sprite.

		dir = Direction.DOWN;

		super.render(screen);

		y = oldy;
	}

	public void die() {
		dropItem(1, Game.isMode("minicraft.settings.mode.score") ? 2 : 4 - Settings.getIdx("diff"), Items.get("slime"));

		super.die(); // Parent death call
	}
}
