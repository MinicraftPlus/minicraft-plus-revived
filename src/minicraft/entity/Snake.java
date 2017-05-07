package minicraft.entity;

import minicraft.Game;
import minicraft.gfx.Color;
import minicraft.gfx.MobSprite;
import minicraft.gfx.Screen;
import minicraft.item.resource.Resource;
import minicraft.screen.OptionsMenu;

public class Snake extends EnemyMob {
	private static MobSprite[][] sprites = MobSprite.compileMobSpriteAnimations(18, 18);
	private static int[] lvlcols = {
		Color.get(-1, 000, 444, 30),
		Color.get(-1, 000, 555, 220),
		Color.get(-1, 000, 555, 5),
		Color.get(-1, 000, 555, 400),
		Color.get(-1, 000, 555, 459)
	};
	
	public Snake(int lvl) {
		super(lvl, sprites, lvlcols, lvl>1?8:7, 100);
		/*
		col0 = Color.get(-1, 0, 40, 444);
		col1 = Color.get(-1, 0, 30, 555);
		col2 = Color.get(-1, 0, 20, 333);
		col3 = Color.get(-1, 0, 10, 222);
		col4 = Color.get(-1, 0, 20, 444);
		*/
	}
	/*
	public void render(Screen screen) {
		if (isLight()) {
			col0 = Color.get(-1, 000, 555, 50);
			col1 = Color.get(-1, 000, 555, 40);
			col2 = Color.get(-1, 000, 555, 30);
			col3 = Color.get(-1, 000, 555, 20);
			col4 = Color.get(-1, 000, 555, 30);
		} else {
			col0 = Color.get(-1, 000, 444, 50);
			col1 = Color.get(-1, 000, 555, 40);
			col2 = Color.get(-1, 000, 333, 30);
			col3 = Color.get(-1, 000, 222, 20);
			col4 = Color.get(-1, 000, 444, 30);
		}
		
		if (lvl == 2) col = Color.get(-1, 000, 555, 220);
		else if (lvl == 3) col = Color.get(-1, 000, 555, 5);
		else if (lvl == 4) col = Color.get(-1, 000, 555, 400);
		else if (lvl == 5) col = Color.get(-1, 000, 555, 459);
		
		else if (level.dirtColor == 322) {
			if (Game.time == 0) col = col0;
			if (Game.time == 1) col = col1;
			if (Game.time == 2) col = col2;
			if (Game.time == 3) col = col3;
		} else col = col4;
		
		super.render(screen);
	}
	*/
	protected void touchedBy(Entity entity) {
		if(entity instanceof Player) {
			int damage = lvl + OptionsMenu.diff;
			entity.hurt(this, damage, dir);
		}
	}
	/*
	public boolean canWool() {
		return true;
	}*/

	protected void die() {
		int num = OptionsMenu.diff == OptionsMenu.hard ? 0 : 1;
		dropResource(num, num+1, Resource.scale);
		
		super.die();
	}
}
