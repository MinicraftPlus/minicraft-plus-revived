package com.mojang.ld22.entity;

import com.mojang.ld22.Game;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.gfx.MobSprite;
import com.mojang.ld22.item.ResourceItem;
import com.mojang.ld22.item.resource.Resource;
import com.mojang.ld22.screen.ModeMenu;
import com.mojang.ld22.screen.OptionsMenu;

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
		
		//randomWalkChance = 40;
		
		col0 = Color.get(-1, 20, 40, 10);
		col1 = Color.get(-1, 20, 30, 40);
		col2 = Color.get(-1, 20, 40, 10);
		col3 = Color.get(-1, 10, 20, 40);
		col4 = Color.get(-1, 10, 20, 30);
		
		//this.lvl = lvl;
		 // gives it a random x,y position anywhere between (0 to 1023) [Tile position (0 to 64)]
		//x = random.nextInt(64 * 16);
		//y = random.nextInt(64 * 16);
		// Health based on level and difficulty:
	}
	
	//static int fail = 9500;
	//public void tick() {tick(true);}
	public void tick() {
		super.tick();
		//speed = 1;
		//fail--;
		//if(fail <= 0) System.exit(-1);
		
		/*
			jumpTime from 0 to -10 (or less) is the slime deciding where to jump.
			10 to 0 is it jumping.
		*/
		
		if(jumpTime <= -10 && (xa != 0 || ya != 0)) //{
			jumpTime = 10;
			//xa = random.nextInt(3) - 1;
			//ya = random.nextInt(3) - 1;
			//super.tick();
			
			//if (level.player != null && !Bed.inBed) { // checks if player is on zombies level and if there is no time left on randonimity timer
				
				/*int xd = level.player.x - x;
				int yd = level.player.y - y;
				if (xd * xd + yd * yd < detectDist * detectDist) {
					/// if player is less than 6.25 tiles away, then set move dir towards player
					//xa = 0;
					//ya = 0;
					if (xd < 0) xa = -1;
					if (xd > 0) xa = +1;
					if (yd < 0) ya = -1;
					if (yd > 0) ya = +1;
				}*/
			//}
		//}
		
		jumpTime--;
		if(jumpTime == 0) {
			xa = ya = 0;
		}
		//fail = 100;
		//((Mob)this).tick(); // ticks the Mob.java part of this class (hopefully)
		
		
		//int speed = 1; // the speed of the slime/ length of jump
		//if (!move(xa * speed, ya * speed) || random.nextInt(40) == 0) { // moves the slime... doubles as a check to see if it's still moving -OR- random chance out of 40
			//if (jumpTime <= -10) { // if jump is equal or less than negative ten
				//super.tick();
				/*xa = (random.nextInt(3) - 1); // Sets direction randomly from -1 to 1
				ya = (random.nextInt(3) - 1);

				if (level.player != null) {
					/// if there's a player less than 3.125 tiles away, then prepare to jump in that direction.
					int xd = level.player.x - x;
					int yd = level.player.y - y;
					if (xd * xd + yd * yd < 50 * 50) {
						if (xd < 0) xa = -1;
						if (xd > 0) xa = +1;
						if (yd < 0) ya = -1;
						if (yd > 0) ya = +1;
					}
				}*/
				
				/*if (xa != 0 || ya != 0) jumpTime = 10; // if slime has it's direction, jump!
			} else
				((MobAi)this).tick(); //attempt at skipping to super-super class; methods aren't as picky anyways, so maybe it will work...
		//}
		
		jumpTime--; //lower jump time by 1
		if (jumpTime == 0) { // when the jump has ended...
			xa = ya = 0; // reset direction to 0
		}*/
		dir = 0;
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
		/* the coordinates of the Slime texture in the png file: */
		/*int xt = 0;
		int yt = 18;
		
		/* where to draw the sprite relative to the slime's position */
		//int xo = x - 8;
		//int yo = y - 11;
		
		/*if (jumpTime > 0) { // if jumping
			//xt += 2; // change sprite
			//yo -= 4; // draw sprite a little higher
		}*/

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
		//int count = random.nextInt() + 1; // Random amount of slime(item) to drop
		dropResource(1, ModeMenu.score ? 2 : 4 - OptionsMenu.diff, Resource.slime);
		
		super.die(); // Parent death call
	}
	
	public boolean canWool() {
		return true;
	}
	/*
	protected void touchedBy(Entity entity) {
		super.touchedBy(entity);
		int damage = OptionsMenu.diff == OptionsMenu.hard ? 2 : 1;
		if (entity instanceof Player) { // if we touch the player
			entity.hurt(this, lvl*damage, dir); // attack
		}
	}*/
}
