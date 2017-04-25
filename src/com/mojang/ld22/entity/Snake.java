package com.mojang.ld22.entity;

import com.mojang.ld22.Game;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.MobSprite;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.item.ResourceItem;
import com.mojang.ld22.item.resource.Resource;
import com.mojang.ld22.screen.ModeMenu;
import com.mojang.ld22.screen.OptionsMenu;

public class Snake extends EnemyMob {
	private static MobSprite[][] sprites = MobSprite.compileMobSpriteAnimations(18, 18);
	
	public Snake(int lvl) {
		super(lvl, sprites, lvl>1?8:7, 100);
		
		col0 = Color.get(-1, 0, 40, 444);
		col1 = Color.get(-1, 0, 30, 555);
		col2 = Color.get(-1, 0, 20, 333);
		col3 = Color.get(-1, 0, 10, 222);
		col4 = Color.get(-1, 0, 20, 444);
	}
	
	public void render(Screen screen) {
		/*int xt = 18;
		int yt = 18;

		int flip1 = (walkDist >> 3) & 1;
		int flip2 = (walkDist >> 3) & 1;

		if (dir == 1) {
			xt += 2;
		}
		if (dir > 1) {

			flip1 = 0;
			flip2 = ((walkDist >> 4) & 1 / 2);
			if (dir == 2) {
				flip1 = 1;
				flip2 = 1;
			}
			xt += 4 + ((walkDist >> 3) & 1) * 2;
		}
		*/
		//int xo = x - 4;
		//int yo = y - 11;

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

	protected void touchedBy(Entity entity) {
		//super.touchedBy(entity);
		if(entity instanceof Player) {
			int damage;
			if (lvl == 1)
				damage = 1;
			else
				damage = lvl - 1 + OptionsMenu.diff;
			
			entity.hurt(this, damage, dir);
		}
		
		//if (OptionsMenu.diff == OptionsMenu.easy) {
		
		
	
		//}
		//if (OptionsMenu.diff == OptionsMenu.norm) {
		/*
		if (lvl > 2) {
			entity.hurt(this, 2, dir);
		} else {
			entity.hurt(this, 1, dir);
		}
		
		//}
		//if (OptionsMenu.diff == OptionsMenu.hard) {
		
				entity.hurt(this, lvl + 1, dir);
		*/
		//}
	}

	public boolean canWool() {
		return true;
	}

	protected void die() {
		//if (OptionsMenu.diff == OptionsMenu.easy) {
		int num = OptionsMenu.diff == OptionsMenu.hard ? 0 : 1;
		dropResource(num, num+1, Resource.scale);
		
		super.die();
		//}
		/*if (OptionsMenu.diff == OptionsMenu.norm) {
			int count = random.nextInt(1) + 1;
			for (int i = 0; i < count; i++) {
				level.add(
						new ItemEntity(
								new ResourceItem(Resource.scale),
								x + random.nextInt(11) - 5,
								y + random.nextInt(11) - 5));
			}
			if (level.player != null) {
				level.player.score += 50 * lvl;
			}
		}
		if (OptionsMenu.diff == OptionsMenu.hard) {
			int count = random.nextInt(1);
			for (int i = 0; i < count; i++) {
				level.add(
						new ItemEntity(
								new ResourceItem(Resource.scale),
								x + random.nextInt(11) - 5,
								y + random.nextInt(11) - 5));
			}
			if (level.player != null) {
				level.player.score += 50 * lvl;
			}
		}*/
	}
}
