package com.mojang.ld22.entity;

import com.mojang.ld22.Game;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.MobSprite;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.item.ResourceItem;
import com.mojang.ld22.item.resource.Resource;
import com.mojang.ld22.screen.ModeMenu;
import com.mojang.ld22.screen.OptionsMenu;

public class Skeleton extends EnemyMob {
	private static MobSprite[][] sprites = MobSprite.compileMobSpriteAnimations(8, 16);
	
	public int arrowtime;
	public int artime;
	
	public Skeleton(int lvl) {
		super(lvl, sprites, 6, true, 100, 45, 200);
		
		arrowtime = 300 / (lvl + 5);
		artime = arrowtime;
		
		col0 = Color.get(-1, 111, 40, 444);
		col1 = Color.get(-1, 222, 50, 555);
		col2 = Color.get(-1, 111, 40, 444);
		col3 = Color.get(-1, 0, 30, 333);
		col4 = Color.get(-1, 111, 40, 444);
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

	public void render(Screen screen) {
		/*int xt = 8;
		int yt = 16;

		int flip1 = (walkDist >> 3) & 1;
		int flip2 = (walkDist >> 3) & 1;

		if (dir == 1) {
			xt += 2;
		}
		if (dir > 1) {

			flip1 = 0;
			flip2 = ((walkDist >> 4) & 1);
			if (dir == 2) {
				flip1 = 1;
			}
			xt += 4 + ((walkDist >> 3) & 1) * 2;
		}
		
		int xo = x - 8;
		int yo = y - 11;
		*/

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
	/*
	protected void touchedBy(Entity entity) {
		super.touchedBy(entity);
		if (OptionsMenu.diff == OptionsMenu.easy) {
			if (entity instanceof Player) {
				//entity.hurt(this, lvl, dir);
			}
		}
		if (OptionsMenu.diff == OptionsMenu.norm) {
			if (entity instanceof Player) {
				//entity.hurt(this, lvl, dir);
			}
		}
		if (OptionsMenu.diff == OptionsMenu.hard) {
			if (entity instanceof Player) {
				//entity.hurt(this, lvl * 2, dir);
			}
		}
	}*/

	public boolean canWool() {
		return true;
	}

	protected void die() {
		if (OptionsMenu.diff == OptionsMenu.easy) {
			int count = random.nextInt(3) + 1;
			int bookcount = random.nextInt(1) + 1;
			int rand = random.nextInt(20);
			if (rand <= 13) {
				dropResource(Resource.bone);
				dropResource(Resource.arrow);
			} else if (rand >= 14 && rand != 19)
				for (int i = 0; i < bookcount; i++) {
					dropResource(Resource.arrow);
					dropResource(Resource.bookant);
				}
			else if (rand == 19) // rare chance of 10 arrows
			for (int i = 0; i < 10; i++) {
					dropResource(Resource.arrow);
				}
		}
		if (OptionsMenu.diff == OptionsMenu.norm) {
			int count = random.nextInt(2) + 1;
			int bookcount = random.nextInt(1) + 1;
			int rand = random.nextInt(20);
			if (rand <= 18) {
				dropResource(Resource.bone);
				dropResource(Resource.arrow);
			} else if (rand >= 19) {
				dropResource(Resource.arrow);
				dropResource(Resource.bookant);
			}
		}
		if (OptionsMenu.diff == OptionsMenu.hard) {
			int count = random.nextInt(1) + 1;
			int bookcount = random.nextInt(1) + 1;
			int rand = random.nextInt(30);
			if (rand <= 28) {
				dropResource(Resource.bone);
				dropResource(Resource.arrow);
			} else if (rand >= 29)
				for (int i = 0; i < bookcount; i++) {
					dropResource(Resource.arrow);
					dropResource(Resource.bookant);
				}
		}
		
		super.die();
	}
}
