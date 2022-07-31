package minicraft.entity.mob;

import minicraft.core.io.Settings;
import minicraft.gfx.MobSprite;
import minicraft.item.Items;

public class Knight extends EnemyMob {
	private static MobSprite[][][] sprites;
	static {
		sprites = new MobSprite[4][4][2];
		for (int i = 0; i < 4; i++) {
			MobSprite[][] list  = MobSprite.compileMobSpriteAnimations(0, 8 + (i * 2));
			sprites[i] = list;
		}
	}


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
