package minicraft.entity;

import minicraft.Game;
import minicraft.gfx.Color;
import minicraft.gfx.MobSprite;
import minicraft.gfx.Screen;
import minicraft.item.resource.Resource;
import minicraft.screen.ModeMenu;
import minicraft.screen.OptionsMenu;

public class Slime extends EnemyMob {
	private static MobSprite[][] sprites;
	static {
		MobSprite[] list = MobSprite.compileSpriteList(0, 18, 2, 2, 0, 2);
		sprites = new MobSprite[1][2];
		sprites[0] = list;
	}
	
	private int jumpTime = 0; // jumpTimer, also acts as a rest timer before the next jump
	
	public Slime(int lvl) {
		super(lvl, sprites, 1, true, 50, 60, 40);
		
		col0 = Color.get(-1, 20, 40, 10);
		col1 = Color.get(-1, 20, 30, 40);
		col2 = Color.get(-1, 20, 40, 10);
		col3 = Color.get(-1, 10, 20, 40);
		col4 = Color.get(-1, 10, 20, 30);
	}
	
	public void tick() {
		super.tick();
		
		/// jumpTime from 0 to -10 (or less) is the slime deciding where to jump.
		/// 10 to 0 is it jumping.
		
		if(jumpTime <= -10 && (xa != 0 || ya != 0))
			jumpTime = 10;
		
		jumpTime--;
		if(jumpTime == 0) {
			xa = ya = 0;
		}
	}
	
	public void randomizeWalkDir(boolean byChance) {
		if(jumpTime > 0) return; // direction cannot be changed if slime is already jumping.
		super.randomizeWalkDir(byChance);
	}
	
	public boolean move(int xa, int ya) {
		dir = 0;
		return super.move(xa, ya);
	}
	
	public void render(Screen screen) {
		
		col0 = Color.get(-1, 20, 40, 222);
		col1 = Color.get(-1, 30, 252, 333);
		col2 = Color.get(-1, 20, 40, 222);
		col3 = Color.get(-1, 10, 20, 111);
		col4 = Color.get(-1, 20, 40, 222);

		if (isLight()) {
			col0 = col1 = col2 = col3 = col4 = Color.get(-1, 30, 252, 333);
		}
		
		if (lvl == 2) col = Color.get(-1, 100, 522, 555);
		else if (lvl == 3) col = Color.get(-1, 111, 444, 555);
		else if (lvl == 4) col = Color.get(-1, 000, 111, 224);
		
		else if (level.dirtColor == 322) {
			if (Game.time == 0) col = col0;
			if (Game.time == 1) col = col1;
			if (Game.time == 2) col = col2;
			if (Game.time == 3) col = col3;
		} else col = col4;
		
		int oldy = y;
		if(jumpTime > 0) {
			walkDist = 8; // set to jumping sprite.
			y -= 4; // raise up a bit.
		}
		else walkDist = 0; // set to ground sprite.
		
		dir = 0;
		
		super.render(screen);
		
		y = oldy;
	}
	
	protected void die() {
		dropResource(1, ModeMenu.score ? 2 : 4 - OptionsMenu.diff, Resource.slime);
		
		super.die(); // Parent death call
	}
	
	public boolean canWool() {
		return true;
	}
}
