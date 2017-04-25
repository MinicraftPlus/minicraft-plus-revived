package com.mojang.ld22.entity;

import com.mojang.ld22.Game;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.gfx.MobSprite;
import com.mojang.ld22.item.ResourceItem;
import com.mojang.ld22.item.resource.Resource;
import com.mojang.ld22.level.Level;
import com.mojang.ld22.screen.ModeMenu;
import com.mojang.ld22.screen.OptionsMenu;

public class MobAi extends Mob {
	
	int randomWalkTime, randomWalkChance, randomWalkDuration;
	int xa, ya;
	//int walkTime, speed; // these determines how fast or slow the mob moves:
	//walkTime slows the mob, by increasing the ticks between movements.
	//speed makes the mob faster by simply moving more per tick.
	
	public MobAi(MobSprite[][] sprites, int maxHealth, int rwTime, int rwChance) {
		super(sprites, maxHealth);
		randomWalkTime = 0;
		randomWalkDuration = rwTime;
		randomWalkChance = rwChance;
		xa = 0;
		ya = 0;
		walkTime = 2;
		//speed = 1;
	}
	
	public void tick() {
		super.tick();
		
		if(!move(xa * speed, ya * speed)) {
			xa = 0;
			ya = 0;
		}
		
		if (random.nextInt(randomWalkChance) == 0) { // if the mob could not or did not move, or a random small chance occurred...
			randomizeWalkDir(true); // set random walk direction.
		}
		
		if (randomWalkTime > 0) randomWalkTime--;
	}
	
	public void render(Screen screen) {
		int xo = x - 8;
		int yo = y - 11;
		
		if (hurtTime > 0) {
			col = Color.get(-1, 555);
		}/* else if(!(this instanceof EnemyMob && ((EnemyMob)this).lvl > 1)) {
			if(level.dirtColor == 322) {
				if(isLight())
					col = colors[1][Game.time];
				else
					col = colors[0][Game.time];
			}
			else col = colors[2][0];
		}*/
		
		MobSprite curSprite = sprites[dir][(walkDist >> 3) % sprites[dir].length];
		curSprite.render(screen, col, xo, yo);
	}
	
	public void randomizeWalkDir(boolean byChance) { // boolean specifies if this method, from where it's called, is called every tick, or after a random chance.
		if(!byChance && random.nextInt(randomWalkChance) != 0) return;
		
		randomWalkTime = randomWalkDuration; // set the mob to walk about in a random direction for a time
		
		// set the random direction; randir is from -1 to 1.
		xa = (random.nextInt(3) - 1);
		ya = (random.nextInt(3) - 1);
	}
	
	/*public boolean canWool() {
		return true;
	}*/
	
	protected void dropResource(int mincount, int maxcount, Resource... resources) {
		int count = random.nextInt(maxcount-mincount+1) + mincount;
		for (int i = 0; i < count; i++)
			for(Resource r: resources)
				dropResource(r);
	}
	protected void dropResource(Resource r) {
		level.add(new ItemEntity(new ResourceItem(r), x + random.nextInt(11) - 5, y + random.nextInt(11) - 5));
	}
	
	
	/** Start pos is a bit different for dungeons. */
	/*public boolean findStartPosDungeon(Level level) {
		int x = random.nextInt(level.w);
		int y = random.nextInt(level.h);
		int xx = x * 16 + 8;
		int yy = y * 16 + 8;

		if (level.player != null) {
			// don't spawn if the player is less than 3.75 blocks away
			int xd = level.player.x - xx;
			int yd = level.player.y - yy;

			if (xd * xd + yd * yd < 60 * 60) return false;
		}
		
		// Get the allowed density of mobs in the level, convert it from a tile to a real coordinate (?) higher in score mode.
		

		return false;
	}*/
	
	/** Tries once to find an appropriate spawn location for friendly mobs. */
	protected static boolean checkStartPos(Level level, int x, int y, int playerDist, int soloRadius) {
		if (level.player != null) {
			int xd = level.player.x - x;
			int yd = level.player.y - y;
			
			if (xd * xd + yd * yd < playerDist * playerDist) return false;
		}
		
		int r = level.monsterDensity * soloRadius; // get no-mob radius
		
		if (level.getEntities(x - r, y - r, x + r, y + r).size() > 0) return false;
		
		return level.getTile(x >> 4, y >> 4).maySpawn; // the last check.
	}
}
