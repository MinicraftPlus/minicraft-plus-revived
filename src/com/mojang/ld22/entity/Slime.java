package com.mojang.ld22.entity;

import com.mojang.ld22.Game;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.item.ResourceItem;
import com.mojang.ld22.item.resource.Resource;
import com.mojang.ld22.screen.ModeMenu;
import com.mojang.ld22.screen.OptionsMenu;

public class Slime extends Mob {
	private int xa, ya; // x and y acceleration
	private int jumpTime = 0; // jumpTimer, also acts as a rest timer before the next jump
	//private int lvl; // how tough the slime is

	public Slime(int lvl) {
		this.col0 = Color.get(-1, 20, 40, 10);
		this.col1 = Color.get(-1, 20, 30, 40);
		this.col2 = Color.get(-1, 20, 40, 10);
		this.col3 = Color.get(-1, 10, 20, 40);
		this.col4 = Color.get(-1, 10, 20, 30);
		
		this.lvl = lvl;
		 // gives it a random x,y position anywhere between (0 to 1023) [Tile position (0 to 64)]
		x = random.nextInt(64 * 16);
		y = random.nextInt(64 * 16);
		// Health based on level and difficulty:
		if (ModeMenu.creative) health = maxHealth = 1;
		else health = maxHealth = lvl * lvl * (OptionsMenu.diff * OptionsMenu.diff + 1);
	}
	
	public void tick() {
		super.tick(); // ticks the Entity.java part of this class

		isenemy = true; // it is indeed an enemy.

		int speed = 1; // the speed of the slime/ length of jump
		if (!move(xa * speed, ya * speed) || random.nextInt(40) == 0) { // moves the slime... doubles as a check to see if it's still moving -OR- random chance out of 40
			if (jumpTime <= -10) { // if jump is equal or less than negative ten
				xa = (random.nextInt(3) - 1); // Sets direction randomly from -1 to 1
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
				}
				
				if (xa != 0 || ya != 0) jumpTime = 10; // if slime has it's direction, jump!
			}
		}

		jumpTime--; //lower jump time by 1
		if (jumpTime == 0) { // when the jump has ended...
			xa = ya = 0; // reset direction to 0
		}
	}
	
	protected void die() {
		super.die(); // Parent death call
		
		int count = random.nextInt(ModeMenu.score ? 2 : 4 - OptionsMenu.diff) + 1; // Random amount of slime(item) to drop
		for (int i = 0; i < count; i++) {
			level.add(new ItemEntity(new ResourceItem(Resource.slime), x + random.nextInt(11) - 5, y + random.nextInt(11) - 5)); //creates slime items
		}
		
		if (level.player != null) {
			level.player.score += (25 * lvl) * Game.multiplier; // add score for slime death
		}

		Game.multiplier++; // increment game multiplier
		Game.multipliertime = Game.mtm -= 5;
	}
	
	public void render(Screen screen) {
		/* the coordinates of the Slime texture in the png file: */
		int xt = 0;
		int yt = 18;
		
		/* where to draw the sprite relative to the slime's position */
		int xo = x - 8;
		int yo = y - 11;
		
		if (jumpTime > 0) { // if jumping
			xt += 2; // change sprite
			yo -= 4; // draw sprite a little higher
		}

		int col0 = Color.get(-1, 20, 40, 222);
		int col1 = Color.get(-1, 30, 252, 333);
		int col2 = Color.get(-1, 20, 40, 222);
		int col3 = Color.get(-1, 10, 20, 111);
		int col4 = Color.get(-1, 20, 40, 222);

		if (isLight()) {
			col0 = Color.get(-1, 30, 252, 333);
			col1 = Color.get(-1, 30, 252, 333);
			col2 = Color.get(-1, 30, 252, 333);
			col3 = Color.get(-1, 30, 252, 333);
			col4 = Color.get(-1, 30, 252, 333);
		}

		if (level.dirtColor == 322) { // ?
			
			if (Game.time == 0) {
				int col = col0;

				if (lvl == 2) col = Color.get(-1, 100, 522, 555);
				if (lvl == 3) col = Color.get(-1, 111, 444, 555);
				if (lvl == 4) col = Color.get(-1, 000, 111, 224);

				if (hurtTime > 0) { // if being hurt
					col = Color.get(-1, 555, 555, 555); // make color white
				}
				
				/* Draws the sprite as 4 different 8*8 images instead of one 16*16 image, really weird, probably an artifact from the zombies and the player's render code */
				/* Well, it draws the 8*8 images because the screen.render() method is stupid :) - David */
				screen.render(xo + 0, yo + 0, xt + yt * 32, col, 0);
				screen.render(xo + 8, yo + 0, xt + 1 + yt * 32, col, 0);
				screen.render(xo + 0, yo + 8, xt + (yt + 1) * 32, col, 0);
				screen.render(xo + 8, yo + 8, xt + 1 + (yt + 1) * 32, col, 0);
			}
			if (Game.time == 1) { // day time
				int col = col1; // color is day color
				
				if (lvl == 2) col = Color.get(-1, 100, 522, 555);
				if (lvl == 3) col = Color.get(-1, 111, 444, 555);
				if (lvl == 4) col = Color.get(-1, 000, 111, 224);

				if (hurtTime > 0) { // white if hit
					col = Color.get(-1, 555, 555, 555);
				}

				screen.render(xo + 0, yo + 0, xt + yt * 32, col, 0);
				screen.render(xo + 8, yo + 0, xt + 1 + yt * 32, col, 0);
				screen.render(xo + 0, yo + 8, xt + (yt + 1) * 32, col, 0);
				screen.render(xo + 8, yo + 8, xt + 1 + (yt + 1) * 32, col, 0);
			}
			if (Game.time == 2) {
				int col = col2;

				if (lvl == 2) col = Color.get(-1, 100, 522, 555);
				if (lvl == 3) col = Color.get(-1, 111, 444, 555);
				if (lvl == 4) col = Color.get(-1, 000, 111, 224);

				if (hurtTime > 0) {
					col = Color.get(-1, 555, 555, 555);
				}

				screen.render(xo + 0, yo + 0, xt + yt * 32, col, 0);
				screen.render(xo + 8, yo + 0, xt + 1 + yt * 32, col, 0);
				screen.render(xo + 0, yo + 8, xt + (yt + 1) * 32, col, 0);
				screen.render(xo + 8, yo + 8, xt + 1 + (yt + 1) * 32, col, 0);
			}
			if (Game.time == 3) {
				int col = col3;

				if (lvl == 2) col = Color.get(-1, 100, 522, 555);
				if (lvl == 3) col = Color.get(-1, 111, 444, 555);
				if (lvl == 4) col = Color.get(-1, 000, 111, 224);

				if (hurtTime > 0) {
					col = Color.get(-1, 555, 555, 555);
				}

				screen.render(xo + 0, yo + 0, xt + yt * 32, col, 0);
				screen.render(xo + 8, yo + 0, xt + 1 + yt * 32, col, 0);
				screen.render(xo + 0, yo + 8, xt + (yt + 1) * 32, col, 0);
				screen.render(xo + 8, yo + 8, xt + 1 + (yt + 1) * 32, col, 0);
			}
		}

		if (level.dirtColor != 322) {
			int col = col4;

			if (lvl == 2) col = Color.get(-1, 100, 522, 555);
			if (lvl == 3) col = Color.get(-1, 111, 444, 555);
			if (lvl == 4) col = Color.get(-1, 000, 111, 224);

			if (hurtTime > 0) {
				col = Color.get(-1, 555, 555, 555);
			}

			screen.render(xo + 0, yo + 0, xt + yt * 32, col, 0);
			screen.render(xo + 8, yo + 0, xt + 1 + yt * 32, col, 0);
			screen.render(xo + 0, yo + 8, xt + (yt + 1) * 32, col, 0);
			screen.render(xo + 8, yo + 8, xt + 1 + (yt + 1) * 32, col, 0);
		}
	}
	
	public boolean canWool() {
		return true;
	}
	
	protected void touchedBy(Entity entity) {
		super.touchedBy(entity);
		int damage = OptionsMenu.diff == OptionsMenu.hard ? 2 : 1;
		if (entity instanceof Player) { // if we touch the player
			entity.hurt(this, lvl*damage, dir); // attack
		}
	}
}
