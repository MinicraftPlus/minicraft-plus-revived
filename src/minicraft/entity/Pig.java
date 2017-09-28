package minicraft.entity;

import minicraft.Settings;
import minicraft.gfx.Color;
import minicraft.gfx.MobSprite;
import minicraft.item.Items;

public class Pig extends PassiveMob {
	private static MobSprite[][] sprites = MobSprite.compileMobSpriteAnimations(16, 14);
	
	public Pig() {
		super(sprites, Color.get(-1, 000, 555, 522));
	}
	
	protected void die() {
		int min = 0, max = 0;
		if (Settings.get("diff").equals("easy")) {min = 1; max = 3;}
		if (Settings.get("diff").equals("norm")) {min = 1; max = 2;}
		if (Settings.get("diff").equals("hard")) {min = 0; max = 2;}
		
		dropItem(min, max, Items.get("raw pork"));
		
		super.die();
	}
}
