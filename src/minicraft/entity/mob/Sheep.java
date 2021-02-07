package minicraft.entity.mob;

import minicraft.core.io.Settings;
import minicraft.gfx.Color;
import minicraft.gfx.MobSprite;
import minicraft.item.Items;

public class Sheep extends PassiveMob {
	private static MobSprite[][] sprites = MobSprite.compileMobSpriteAnimations(0, 28);
	
	/**
	 * Creates a sheep entity.
	 */
	public Sheep() {
		super(sprites);
	}
	
	public void die() {
		int min = 0, max = 0;
		if (Settings.get("diff").equals("Easy")) {min = 1; max = 3;}
		if (Settings.get("diff").equals("Normal")) {min = 1; max = 2;}
		if (Settings.get("diff").equals("Hard")) {min = 0; max = 2;}
		
		dropItem(min, max, Items.get("wool"));
		
		super.die();
	}
}
