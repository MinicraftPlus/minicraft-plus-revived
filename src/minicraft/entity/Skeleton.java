package minicraft.entity;

import minicraft.Game;
import minicraft.gfx.Color;
import minicraft.gfx.MobSprite;
import minicraft.gfx.Screen;
import minicraft.item.resource.Resource;
import minicraft.screen.OptionsMenu;

public class Skeleton extends EnemyMob {
	private static MobSprite[][] sprites = MobSprite.compileMobSpriteAnimations(8, 16);
	private static int[] lvlcols = {
		Color.get(-1, 111, 40, 444),
		Color.get(-1, 100, 522, 555),
		Color.get(-1, 111, 444, 555),
		Color.get(-1, 000, 111, 555)
	};
	
	public int arrowtime;
	public int artime;
	
	public Skeleton(int lvl) {
		super(lvl, sprites, lvlcols, 6, true, 100, 45, 200);
		
		arrowtime = 300 / (lvl + 5);
		artime = arrowtime;
		/*
		col0 = Color.get(-1, 111, 40, 444);
		col1 = Color.get(-1, 222, 50, 555);
		col2 = Color.get(-1, 111, 40, 444);
		col3 = Color.get(-1, 0, 30, 333);
		col4 = Color.get(-1, 111, 40, 444);*/
	}

	public void tick() {
		super.tick();
		
		if (level.player != null && randomWalkTime == 0) {
			boolean done = false;
			artime--;
			
			int xd = level.player.x - x;
			int yd = level.player.y - y;
			if (xd * xd + yd * yd < 100 * 100) {
				if (artime < 1) {
					int xdir = 0, ydir = 0;
					if(dir == 0) ydir = 1;
					if(dir == 1) ydir = -1;
					if(dir == 2) xdir = -1;
					if(dir == 3) xdir = 1;
					level.add(new Arrow(this, xdir, ydir, lvl, done));
					artime = arrowtime;
				}
			}
		}
	}
	/*
	public void render(Screen screen) {
		if (isLight()) {
			col0 = col1 = col2 = col3 = col4 = Color.get(-1, 222, 50, 555);
		} else {
			col0 = Color.get(-1, 111, 40, 444);
			col1 = Color.get(-1, 222, 50, 555);
			col2 = Color.get(-1, 111, 40, 444);
			col3 = Color.get(-1, 000, 30, 333);
			col4 = Color.get(-1, 111, 40, 444);
		}
		
		if (lvl == 2) col = Color.get(-1, 100, 522, 555);
		else if (lvl == 3) col = Color.get(-1, 111, 444, 555);
		else if (lvl == 4) col = Color.get(-1, 000, 111, 555);
		
		else if (level.dirtColor == 322) {
			if (Game.time == 0) col = col0;
			if (Game.time == 1) col = col1;
			if (Game.time == 2) col = col2;
			if (Game.time == 3) col = col3;
		} else col = col4;
		
		super.render(screen);
	}
	
	public boolean canWool() {
		return true;
	}*/

	protected void die() {
		int[] diffrands = {20, 20, 30};
		int[] diffvals = {13, 18, 28};
		int diff = OptionsMenu.diff;
		
		int count = random.nextInt(3 - diff) + 1;
		int bookcount = random.nextInt(1) + 1;
		int rand = random.nextInt(diffrands[diff]);
		if (rand <= diffvals[diff])
			dropResource(count, Resource.bone, Resource.arrow);
		else if (diff == 0 && rand < 19 || diff != 0)
			dropResource(bookcount, Resource.bookant, Resource.arrow);
		else if (diff == 0) // rare chance of 10 arrows on easy mode
			dropResource(10, Resource.arrow);
		
		super.die();
	}
}
