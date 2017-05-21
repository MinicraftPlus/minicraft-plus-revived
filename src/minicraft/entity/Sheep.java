package minicraft.entity;

import minicraft.gfx.Color;
import minicraft.gfx.MobSprite;
import minicraft.item.Items;
import minicraft.screen.OptionsMenu;

public class Sheep extends PassiveMob {
	private static MobSprite[][] sprites = MobSprite.compileMobSpriteAnimations(10, 18);
	
	public Sheep() {
		super(sprites, Color.get(-1, 000, 555, 432));
		/*
		col0 = Color.get(-1, 000, 444, 321);
		col1 = Color.get(-1, 000, 555, 432);
		col2 = Color.get(-1, 000, 333, 210);
		col3 = Color.get(-1, 000, 222, 100);
		col4 = Color.get(-1, 000, 444, 432);
		*/
	}
	/*
	public void render(Screen screen) {
		if (isLight()) {
			col0 = col1 = col2 = col3 = col4 = Color.get(-1, 000, 555, 432);
		} else {
			col0 = Color.get(-1, 000, 444, 321);
			col1 = Color.get(-1, 000, 555, 432);
			col2 = Color.get(-1, 000, 333, 210);
			col3 = Color.get(-1, 000, 222, 100);
			col4 = Color.get(-1, 000, 444, 432);
		}
		
		if (level.dirtColor == 322) {
			if (Game.time == 0) col = col0;
			if (Game.time == 1) col = col1;
			if (Game.time == 2) col = col2;
			if (Game.time == 3) col = col3;
		} else col = col4;
		
		super.render(screen);
	}
	
	public boolean canWool() {
		return true;
	}
	*/
	protected void die() {
		int min = 0, max = 0;
		if (OptionsMenu.diff == OptionsMenu.easy) {min = 1; max = 3;}
		if (OptionsMenu.diff == OptionsMenu.norm) {min = 1; max = 2;}
		if (OptionsMenu.diff == OptionsMenu.hard) {min = 0; max = 2;}
		
		dropItem(min, max, Items.get("wool"));
		
		super.die();
	}
}
