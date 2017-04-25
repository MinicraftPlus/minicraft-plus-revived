package com.mojang.ld22.entity;

import com.mojang.ld22.Game;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.gfx.MobSprite;
import com.mojang.ld22.item.ResourceItem;
import com.mojang.ld22.item.resource.Resource;
import com.mojang.ld22.level.Level;
import com.mojang.ld22.level.tile.Tile;
import com.mojang.ld22.screen.ModeMenu;
import com.mojang.ld22.screen.OptionsMenu;

public class PassiveMob extends MobAi {
	
	public PassiveMob(MobSprite[][] sprites) {this(sprites, 3);}
	public PassiveMob(MobSprite[][] sprites, int healthFactor) {
		super(sprites/*, colors*/, 5 + healthFactor * OptionsMenu.diff, 45, 40);
		//walkTime = 2; // half's the speed of passive mobs.
	}
	
	/*public void tick() {
		super.tick();
		
		if (!move(xa * speed, ya * speed) && ) {
			
		}
	}*/
	
	public void randomizeWalkDir(boolean byChance) {
		if(xa == 0 && ya == 0 && random.nextInt(5) == 0 || byChance || !byChance && random.nextInt(randomWalkChance) == 0) {
			randomWalkTime = randomWalkDuration;
			// multiple at end ups the chance of not moving by 50%.
			xa = (random.nextInt(3) - 1) * random.nextInt(2);
			ya = (random.nextInt(3) - 1) * random.nextInt(2);
		}
	}
	
	/*public void render(Screen screen) {
		/*int xt = 16;
		int yt = 16;

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

		int xo = x - 8;
		int yo = y - 11;
		
	}*/
	
	/*public boolean canWool() {
		return true;
	}*/
	
	protected void die() {
		if (level.player != null) {
			level.player.score += 15;
		}
		
		super.die();
	}
	
	/** Tries once to find an appropriate spawn location for friendly mobs. */
	public static boolean checkStartPos(Level level, int x, int y) {
		
		int r = (ModeMenu.score ? 22 : 15) + (Game.time == 3 ? 0 : 5); // get no-mob radius by
		
		if(!MobAi.checkStartPos(level, x, y, 80, r))
			return false;
		
		Tile tile = level.getTile(x >> 4, y >> 4);
		if (tile == Tile.grass || tile == Tile.lightgrass || tile == Tile.flower || tile == Tile.lightflower) {
			return true;
		}

		return false;
	}
	
	/*
	// very similar to above; but the mob density is different.
	public boolean findStartPosLight(Level level) {
		int x = random.nextInt(level.w);
		int y = random.nextInt(level.h);
		//tile to entity coords:
		int xx = x * 16 + 8;
		int yy = y * 16 + 8;

		if (level.player != null) {
			int xd = level.player.x - xx;
			int yd = level.player.y - yy;

			if (xd * xd + yd * yd < 80 * 80) return false;
		}

		if (!ModeMenu.score) {
			r = level.monsterDensity * 15;
		} else {
			r = level.monsterDensity * 22;
		}
		if (level.getEntities(xx - r, yy - r, xx + r, yy + r).size() > 0) return false;

		//makes it so that cows only spawn on grass or flowers.
		if (level.getTile(x, y).mayPass(level, x, y, this)) {
			if (tile == Tile.grass || tile == Tile.lightgrass || tile == Tile.flower || tile == Tile.lightflower) {
				this.x = xx;
				this.y = yy;
				return true;
			}
		}

		return false;
	}*/
}
