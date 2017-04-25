package com.mojang.ld22.entity;

import com.mojang.ld22.Game;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.gfx.MobSprite;
import com.mojang.ld22.item.ResourceItem;
import com.mojang.ld22.item.resource.Resource;
import com.mojang.ld22.screen.ModeMenu;
import com.mojang.ld22.screen.OptionsMenu;

public class Knight extends EnemyMob {
	private static MobSprite[][] sprites = MobSprite.compileMobSpriteAnimations(24, 14);
	
	public Knight(int lvl) {
		super(lvl, sprites, 9, 100);
		this.col0 = Color.get(-1, 0, 555, 359);
		this.col1 = Color.get(-1, 0, 555, 359);
		this.col2 = Color.get(-1, 0, 333, 59);
		this.col3 = Color.get(-1, 0, 333, 59);
		this.col4 = Color.get(-1, 0, 333, 59);
		/*if (OptionsMenu.diff == OptionsMenu.easy) {
			this.lvl = lvl;
			x = random.nextInt(64 * 16);
			y = random.nextInt(64 * 16);
			if (ModeMenu.creative) health = maxHealth = 1;
			else health = maxHealth = lvl * lvl * 11;
		}

		if (OptionsMenu.diff == OptionsMenu.norm) {
			this.lvl = lvl;
			x = random.nextInt(64 * 16);
			y = random.nextInt(64 * 16);
			if (ModeMenu.creative) health = maxHealth = 1;
			else health = maxHealth = lvl * lvl * 22;
		}

		if (OptionsMenu.diff == OptionsMenu.hard) {
			this.lvl = lvl;
			x = random.nextInt(64 * 16);
			y = random.nextInt(64 * 16);
			if (ModeMenu.creative) health = maxHealth = 1;
			else health = maxHealth = lvl * lvl * 33;
		}*/
	}
	/*
	public void tick() {
		super.tick();
		
		if (level.player != null && randomWalkTime == 0) {
			int xd = level.player.x - x;
			int yd = level.player.y - y;
			if (xd * xd + yd * yd < 100 * 100) {
				xa = 0;
				ya = 0;
				if (xd < 0) xa = -1;
				xe = xa;
				if (xd > 0) xa = +1;
				xe = xa;
				if (yd < 0) ya = -1;
				xe = xa;
				if (yd > 0) ya = +1;
				xe = xa;
			}
		}

		int speed = tickTime & 1;
		if (!move(xa * speed, ya * speed) || random.nextInt(200) == 0) {
			randomWalkTime = 60;
			xa = (random.nextInt(3) - 1) * random.nextInt(2);
			ya = (random.nextInt(3) - 1) * random.nextInt(2);
		}
		if (randomWalkTime > 0) randomWalkTime--;
	}
	*/
	public void render(Screen screen) {
		/*int xt = 24;
		int yt = 14;

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
		col0 = Color.get(-1, 000, 555, 10);
		col1 = Color.get(-1, 000, 555, 10);
		col2 = Color.get(-1, 000, 555, 10);
		col3 = Color.get(-1, 000, 555, 10);
		col4 = Color.get(-1, 000, 555, 10);
		
		if (lvl == 2) col = Color.get(-1, 000, 555, 220);
		else if (lvl == 3) col = Color.get(-1, 000, 555, 5);
		else if (lvl == 4) col = Color.get(-1, 000, 555, 400);
		else if (lvl == 5) col = Color.get(-1, 000, 555, 459);
		
		else if (level.dirtColor == 322) {

			if (Game.time == 0) {
				col = col0;
			}
			if (Game.time == 1) {
				col = col1;
				
			}
			if (Game.time == 2) {
				col = col2;
				
			}
			if (Game.time == 3) {
				col = col3;
				
			}
		} else {
			col = col4;
		}
		
		super.render(screen);
	}
	/*
	protected void touchedBy(Entity entity) {
		if (OptionsMenu.diff == OptionsMenu.easy) {
			if (entity instanceof Player) {
				entity.hurt(this, lvl, dir);
			}
		}
		if (OptionsMenu.diff == OptionsMenu.norm) {
			if (entity instanceof Player) {
				entity.hurt(this, lvl, dir);
			}
		}
		if (OptionsMenu.diff == OptionsMenu.hard) {
			if (entity instanceof Player) {
				entity.hurt(this, lvl * 2, dir);
			}
		}
	}*/

	public boolean canWool() {
		return true;
	}

	protected void die() {
		if (OptionsMenu.diff == OptionsMenu.easy) dropResource(1, 3, Resource.shard);
		if (OptionsMenu.diff == OptionsMenu.norm) dropResource(0, 2, Resource.shard);
		if (OptionsMenu.diff == OptionsMenu.hard) dropResource(0, 2, Resource.shard);
		
		super.die();
	}
}
