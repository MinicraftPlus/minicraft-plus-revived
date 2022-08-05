package minicraft.entity.mob;

import minicraft.core.Game;
import minicraft.core.io.Settings;
import minicraft.entity.Arrow;
import minicraft.gfx.SpriteLinker.LinkedSpriteSheet;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.item.Items;

public class Skeleton extends EnemyMob {
	private static LinkedSpriteSheet[] sprites = new LinkedSpriteSheet[] {
		new LinkedSpriteSheet(SpriteType.Entity, "skeleton"),
		new LinkedSpriteSheet(SpriteType.Entity, "skeleton").setSpritePos(0, 2),
		new LinkedSpriteSheet(SpriteType.Entity, "skeleton").setSpritePos(0, 4),
		new LinkedSpriteSheet(SpriteType.Entity, "skeleton").setSpritePos(0, 6)
	};

	private int arrowtime;
	private int artime;

	/**
	 * Creates a skeleton of a given level.
	 * @param lvl The skeleton's level.
	 */
	public Skeleton(int lvl) {
		super(lvl, sprites, 6, true, 100, 45, 200);

		arrowtime = 500 / (lvl + 5);
		artime = arrowtime;
	}

	@Override
	public void tick() {
		super.tick();

		if (skipTick()) return;

		Player player = getClosestPlayer();
		if (player != null && randomWalkTime == 0 && !Game.isMode("minicraft.settings.mode.creative")) { // Run if there is a player nearby, the skeleton has finished their random walk, and gamemode is not creative.
			artime--;

			int xd = player.x - x;
			int yd = player.y - y;
			if (xd * xd + yd * yd < 100 * 100) {
				if (artime < 1) {
					level.add(new Arrow(this, dir, lvl));
					artime = arrowtime;
				}
			}
		}
	}

	public void die() {
		int[] diffrands = {20, 20, 30};
		int[] diffvals = {13, 18, 28};
		int diff = Settings.getIdx("diff");

		int count = random.nextInt(3 - diff) + 1;
		int bookcount = random.nextInt(1) + 1;
		int rand = random.nextInt(diffrands[diff]);

		if (rand <= diffvals[diff])
			level.dropItem(x, y, count, Items.get("bone"), Items.get("arrow"));
		else if (diff == 0 && rand >= 19) // Rare chance of 10 arrows on easy mode
			level.dropItem(x, y, 10, Items.get("arrow"));
		else
			level.dropItem(x, y, bookcount, Items.get("Antidious"), Items.get("arrow"));

		super.die();
	}
}
