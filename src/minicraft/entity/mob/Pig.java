package minicraft.entity.mob;

import minicraft.core.io.Settings;
import minicraft.gfx.Color;
import minicraft.gfx.MobSprite;
import minicraft.item.Items;

public class Pig extends PassiveMob {
	private static MobSprite[][] sprites = MobSprite.compileMobSpriteAnimations(0, 30);
	
	/**
	 * Creates a pig.
	 */
	public Pig() {
		super(sprites);
	}
	
	public void die() {
		int min = 0, max = 0;
		if (Settings.get("diff").equals("Easy")) {min = 1; max = 3;}
		if (Settings.get("diff").equals("Normal")) {min = 1; max = 2;}
		if (Settings.get("diff").equals("Hard")) {min = 0; max = 2;}
		
		dropItem(min, max, Items.get("raw pork"));
		
		super.die();
	}
}
