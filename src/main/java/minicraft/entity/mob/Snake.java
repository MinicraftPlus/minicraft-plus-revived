package minicraft.entity.mob;

import minicraft.core.io.Settings;
import minicraft.entity.Entity;
import minicraft.gfx.SpriteLinker.LinkedSpriteSheet;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.item.Items;

public class Snake extends EnemyMob {
	private static LinkedSpriteSheet[] sprites = new LinkedSpriteSheet[] {
		new LinkedSpriteSheet(SpriteType.Entity, "snake"),
		new LinkedSpriteSheet(SpriteType.Entity, "snake").setSpritePos(0, 2),
		new LinkedSpriteSheet(SpriteType.Entity, "snake").setSpritePos(0, 4),
		new LinkedSpriteSheet(SpriteType.Entity, "snake").setSpritePos(0, 6)
	};

	public Snake(int lvl) {
		super(lvl, sprites, lvl > 1 ? 8 : 7, 100);
	}

	@Override
	protected void touchedBy(Entity entity) {
		if (entity instanceof Player) {
			int damage = lvl + Settings.getIdx("diff");
			((Player)entity).hurt(this, damage);
		}
	}

	public void die() {
		int num = Settings.get("diff").equals("minicraft.settings.difficulty.hard") ? 1 : 0;
		dropItem(num, num + 1, Items.get("scale"));

		if (random.nextInt(24 / lvl / (Settings.getIdx("diff") + 1)) == 0)
			dropItem(1, 1, Items.get("key"));

		super.die();
	}
}
