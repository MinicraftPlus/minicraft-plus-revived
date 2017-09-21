package minicraft.entity;

import minicraft.gfx.Color;
import minicraft.gfx.MobSprite;
import minicraft.item.Items;
import minicraft.screen.Displays;
import minicraft.screen.OptionsMenu;

public class Pig extends PassiveMob {
	private static MobSprite[][] sprites = MobSprite.compileMobSpriteAnimations(16, 14);
	
	public Pig() {
		super(sprites, Color.get(-1, 000, 555, 522));
	}
	
	protected void die() {
		int min = 0, max = 0;
		if (Displays.options.getEntry("diff").getValue().equals("easy")) {min = 1; max = 3;}
		if (Displays.options.getEntry("diff").getValue().equals("norm")) {min = 1; max = 2;}
		if (Displays.options.getEntry("diff").getValue().equals("hard")) {min = 0; max = 2;}
		
		dropItem(min, max, Items.get("raw pork"));
		
		super.die();
	}
}
