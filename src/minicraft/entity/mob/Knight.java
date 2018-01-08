package minicraft.entity.mob;

import minicraft.core.io.Settings;
import minicraft.gfx.Color;
import minicraft.gfx.MobSprite;
import minicraft.item.Items;

public class Knight extends EnemyMob {
	private static MobSprite[][] sprites = MobSprite.compileMobSpriteAnimations(24, 14);
	private static int[] lvlcols = {
		Color.get(-1, 000, 555, 10),
		Color.get(-1, 000, 555, 220),
		Color.get(-1, 000, 555, 5),
		Color.get(-1, 000, 555, 400),
		Color.get(-1, 000, 555, 459)
	};
	
	public Knight(int lvl) {
		super(lvl, sprites, lvlcols, 9, 100);
	}

	protected void die() {
		if (Settings.get("diff").equals("Easy"))
			dropItem(1, 3, Items.get("shard"));
		else
			dropItem(0, 2, Items.get("shard")
			);
		
		if(random.nextInt(30/lvl/(Settings.getIdx("diff")+1)) == 0)
			dropItem(1, 1, Items.get("key"));
		
		super.die();
	}
	
	public int getMaxLevel() {
		return 5;
	}
}
