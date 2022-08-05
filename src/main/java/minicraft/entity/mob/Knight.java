package minicraft.entity.mob;

import minicraft.core.io.Settings;
import minicraft.gfx.SpriteLinker.LinkedSpriteSheet;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.item.Items;

public class Knight extends EnemyMob {
	private static LinkedSpriteSheet[] sprites = new LinkedSpriteSheet[] {
		new LinkedSpriteSheet(SpriteType.Entity, "creeper").setSpritePos(0, 0),
		new LinkedSpriteSheet(SpriteType.Entity, "creeper").setSpritePos(0, 2),
		new LinkedSpriteSheet(SpriteType.Entity, "creeper").setSpritePos(0, 4),
		new LinkedSpriteSheet(SpriteType.Entity, "creeper").setSpritePos(0, 6)
	};

	/**
	 * Creates a knight of a given level.
	 * @param lvl The knights level.
	 */
	public Knight(int lvl) {
		super(lvl, sprites, 9, 100);
	}

	public void die() {
		if (Settings.get("diff").equals("minicraft.settings.difficulty.easy"))
			dropItem(1, 3, Items.get("shard"));
		else
			dropItem(0, 2, Items.get("shard")
			);

		if(random.nextInt(24/lvl/(Settings.getIdx("diff")+1)) == 0)
			dropItem(1, 1, Items.get("key"));

		super.die();
	}
}
