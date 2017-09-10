package minicraft.entity;

import minicraft.gfx.Color;
import minicraft.gfx.MobSprite;
import minicraft.item.Items;
import minicraft.screen.OptionsMenu;

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
		super(lvl, sprites, lvlcols, 10, 100);
	}

	protected void die() {
		int num = lvl / 3;
		if (OptionsMenu.diff == OptionsMenu.easy) dropItem(1, 3+(lvl==0?-2:lvl==1?0:num), Items.get("shard"));
		if (OptionsMenu.diff == OptionsMenu.norm) dropItem(0, 2+num, Items.get("shard"));
		if (OptionsMenu.diff == OptionsMenu.hard) dropItem(num, 2+num, Items.get("shard"));
		
		super.die();
	}
	
	public int getMaxLevel() {
		return 5;
	}
}
