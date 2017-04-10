package com.mojang.ld22.entity;

import com.mojang.ld22.Game;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.item.ResourceItem;
import com.mojang.ld22.item.resource.Resource;
import com.mojang.ld22.screen.ModeMenu;
import com.mojang.ld22.screen.OptionsMenu;

// TODO compress the enemy mobs into an Enemy class... further, similar compression can happen in many places...
public class Zombie extends Mob {
	int xa, ya;
	private int lvl; // how tough the zombie is
	private int randomWalkTime = 0; //time till next walk

	public Zombie(int lvl) {
		
		this.lvl = lvl;
		col0 = Color.get(-1, 10, 152, 40);
		col1 = Color.get(-1, 20, 252, 50);
		col2 = Color.get(-1, 10, 152, 40);
		col3 = Color.get(-1, 0, 30, 20);
		col4 = Color.get(-1, 10, 42, 30);
		
		x = random.nextInt(64 * 16);
		y = random.nextInt(64 * 16);
		if (ModeMenu.creative) health = maxHealth = 1;
		else health = maxHealth = lvl * lvl * 5*((Double)(Math.pow(2, OptionsMenu.diff-1))).intValue(); // 5, 10, 20
	}

	public void tick() {
		super.tick();

		isenemy = true;

		if (level.player != null && randomWalkTime == 0) { // checks if player is on zombies level and if there is no time left on timer
			int xd = level.player.x - x;
			int yd = level.player.y - y;
			if (xd * xd + yd * yd < 100 * 100) {
				/// if player is less than 6.25 tiles away, then set move dir towards player
				xa = 0;
				ya = 0;
				if (xd < 0) xa = -1;
				if (xd > 0) xa = +1;
				if (yd < 0) ya = -1;
				if (yd > 0) ya = +1;
			}
		}
		
		// go google "java bitwise AND operator" for information about this next statement. -David
		int speed = tickTime & 1; // Speed is either 0 or 1 depending on the tickTime
		if (!move(xa * speed, ya * speed) || random.nextInt(200) == 0) { //moves the zombie, doubles as a check to see if it's still moving -OR- random chance out of 200
			randomWalkTime = 60; // sets the not-so-random walk time to 60
			//sets the acceleration to random i.e. idling code:
			xa = (random.nextInt(3) - 1) * random.nextInt(2);
			ya = (random.nextInt(3) - 1) * random.nextInt(2);
		}
		if (randomWalkTime > 0) randomWalkTime--; // if walk time is larger than 0, decrement it.
	}

	public void render(Screen screen) {
		// x,y coord of sprite in spritesheet:
		int xt = 0;
		int yt = 14;
		
		// change the 3 in (walkDist >> 3) to change the time it will take to switch sprites. (bigger number = longer time).
		 // These will either be a 1 or a 0 depending on the walk distance (Used for walking effect by mirroring the sprite)
		int flip1 = (walkDist >> 3) & 1;
		int flip2 = (walkDist >> 3) & 1;
		
		if (dir == 1) { // if facing up
			xt += 2; // change sprite to up
		}
		if (dir > 1) { // if facing left or down
			flip1 = 0; // controls flipping left and right
			flip2 = ((walkDist >> 4) & 1); // mirror sprite based on walk dist; animates slightly slower than the above
			if (dir == 2) { // if facing left
				flip1 = 1; // flip the sprite so it looks like we are facing left
			}
			xt += 4 + ((walkDist >> 3) & 1) * 2; // animation based on walk distance
		}
		
		/* where to draw the sprite relative to our position */
		int xo = x - 8;
		int yo = y - 11;

		int col0 = Color.get(-1, 10, 152, 40);
		int col1 = Color.get(-1, 20, 252, 50);
		int col2 = Color.get(-1, 10, 152, 40);
		int col3 = Color.get(-1, 0, 30, 20);
		int col4 = Color.get(-1, 10, 152, 40);

		if (isLight()) {
			col0 = Color.get(-1, 20, 252, 50);
			col1 = Color.get(-1, 20, 252, 50);
			col2 = Color.get(-1, 20, 252, 50);
			col3 = Color.get(-1, 20, 252, 50);
			col4 = Color.get(-1, 20, 252, 50);
		}

		if (level.dirtColor == 322) {

			if (Game.time == 0) {
				int col = col0;
				if (lvl == 2) col = Color.get(-1, 100, 522, 050);
				if (lvl == 3) col = Color.get(-1, 111, 444, 050);
				if (lvl == 4) col = Color.get(-1, 000, 111, 020);
				if (hurtTime > 0) {
					col = Color.get(-1, 555, 555, 555);
				}

				screen.render(xo + 8 * flip1, yo + 0, xt + yt * 32, col, flip1);
				screen.render(xo + 8 - 8 * flip1, yo + 0, xt + 1 + yt * 32, col, flip1);
				screen.render(xo + 8 * flip2, yo + 8, xt + (yt + 1) * 32, col, flip2);
				screen.render(xo + 8 - 8 * flip2, yo + 8, xt + 1 + (yt + 1) * 32, col, flip2);
			}
			if (Game.time == 1) {
				int col = col1;
				if (lvl == 2) col = Color.get(-1, 100, 522, 050);
				if (lvl == 3) col = Color.get(-1, 111, 444, 050);
				if (lvl == 4) col = Color.get(-1, 000, 111, 020);
				if (hurtTime > 0) {
					col = Color.get(-1, 555, 555, 555);
				}

				screen.render(xo + 8 * flip1, yo + 0, xt + yt * 32, col, flip1);
				screen.render(xo + 8 - 8 * flip1, yo + 0, xt + 1 + yt * 32, col, flip1);
				screen.render(xo + 8 * flip2, yo + 8, xt + (yt + 1) * 32, col, flip2);
				screen.render(xo + 8 - 8 * flip2, yo + 8, xt + 1 + (yt + 1) * 32, col, flip2);
			}
			if (Game.time == 2) {
				int col = col2;
				if (lvl == 2) col = Color.get(-1, 100, 522, 050);
				if (lvl == 3) col = Color.get(-1, 111, 444, 050);
				if (lvl == 4) col = Color.get(-1, 000, 111, 020);
				if (hurtTime > 0) {
					col = Color.get(-1, 555, 555, 555);
				}

				screen.render(xo + 8 * flip1, yo + 0, xt + yt * 32, col, flip1);
				screen.render(xo + 8 - 8 * flip1, yo + 0, xt + 1 + yt * 32, col, flip1);
				screen.render(xo + 8 * flip2, yo + 8, xt + (yt + 1) * 32, col, flip2);
				screen.render(xo + 8 - 8 * flip2, yo + 8, xt + 1 + (yt + 1) * 32, col, flip2);
			}
			if (Game.time == 3) {
				int col = col3;
				if (lvl == 2) col = Color.get(-1, 100, 522, 050);
				if (lvl == 3) col = Color.get(-1, 111, 444, 050);
				if (lvl == 4) col = Color.get(-1, 000, 111, 020);
				if (hurtTime > 0) {
					col = Color.get(-1, 555, 555, 555);
				}

				screen.render(xo + 8 * flip1, yo + 0, xt + yt * 32, col, flip1);
				screen.render(xo + 8 - 8 * flip1, yo + 0, xt + 1 + yt * 32, col, flip1);
				screen.render(xo + 8 * flip2, yo + 8, xt + (yt + 1) * 32, col, flip2);
				screen.render(xo + 8 - 8 * flip2, yo + 8, xt + 1 + (yt + 1) * 32, col, flip2);
			}
		}

		if (level.dirtColor != 322) {
			int col = col4;
			if (lvl == 2) col = Color.get(-1, 100, 522, 050);
			if (lvl == 3) col = Color.get(-1, 111, 444, 050);
			if (lvl == 4) col = Color.get(-1, 000, 111, 020);
			if (hurtTime > 0) {
				col = Color.get(-1, 555, 555, 555);
			}

			screen.render(xo + 8 * flip1, yo + 0, xt + yt * 32, col, flip1);
			screen.render(xo + 8 - 8 * flip1, yo + 0, xt + 1 + yt * 32, col, flip1);
			screen.render(xo + 8 * flip2, yo + 8, xt + (yt + 1) * 32, col, flip2);
			screen.render(xo + 8 - 8 * flip2, yo + 8, xt + 1 + (yt + 1) * 32, col, flip2);
		}
	}

	protected void touchedBy(Entity entity) { // if the entity touches the player
		// hurts the player, damage is based on lvl.
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
	}

	public boolean canWool() {
		return true;
	}

	protected void die() {
		super.die();

		if (OptionsMenu.diff == OptionsMenu.easy) {
			int count = random.nextInt(3) + 2; // Random amount of cloth to drop.
			for (int i = 0; i < count; i++) {
				level.add(
						new ItemEntity(
								new ResourceItem(Resource.cloth),
								x + random.nextInt(11) - 5,
								y + random.nextInt(11) - 5)); // creates cloth
			}
		}
		if (OptionsMenu.diff == OptionsMenu.norm) {
			int count = random.nextInt(2) + 2;
			for (int i = 0; i < count; i++) {
				level.add(
						new ItemEntity(
								new ResourceItem(Resource.cloth),
								x + random.nextInt(11) - 5,
								y + random.nextInt(11) - 5));
			}
		}
		if (OptionsMenu.diff == OptionsMenu.hard) {
			int count = random.nextInt(1) + 1;
			for (int i = 0; i < count; i++) {
				level.add(
						new ItemEntity(
								new ResourceItem(Resource.cloth),
								x + random.nextInt(11) - 5,
								y + random.nextInt(11) - 5));
			}
		}
		
		if(random.nextInt(60) == 2) {
			level.add(new ItemEntity(new ResourceItem(Resource.ironIngot), x + random.nextInt(11) - 5, y + random.nextInt(11) - 5));
		}
		
		if(random.nextInt(40) == 19) {
			int rand = random.nextInt(3);
			if(rand == 0) {
				level.add(new ItemEntity(new ResourceItem(Resource.greenclothes), x + random.nextInt(11) - 5, y + random.nextInt(11) - 5));
			} else if(rand == 1) {
				level.add(new ItemEntity(new ResourceItem(Resource.redclothes), x + random.nextInt(11) - 5, y + random.nextInt(11) - 5));
			} else if(rand == 2) {
				level.add(new ItemEntity(new ResourceItem(Resource.blueclothes), x + random.nextInt(11) - 5, y + random.nextInt(11) - 5));
			}
		}
		
		if (level.player != null) { // if player is on zombie level
			level.player.score += (50 * lvl) * Game.multiplier; // add score for zombie death
		}
		
		// TODO implement Game.setMultiplier (maybe make it addMultiplier() and resetMultiplier()).
		Game.multiplier++; // add to multiplier
		Game.multipliertime = Game.mtm -= 5;
	}
}
