//new class, no comments.
package com.mojang.ld22.entity;

import com.mojang.ld22.Game;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.item.ResourceItem;
import com.mojang.ld22.item.resource.Resource;
import com.mojang.ld22.screen.ModeMenu;
import com.mojang.ld22.screen.OptionsMenu;

public class Sheep extends Mob {
	int xa, ya, xe, ye;
	private int lvl;
	private int randomWalkTime;
	
	public Sheep(int lvl) {
		xe = xa;
		ye = ya;
		if (lvl == 0) lvl = 1;
		randomWalkTime = 0;
		
		col0 = Color.get(-1, 0, 444, 321);
		col1 = Color.get(-1, 0, 555, 432);
		col2 = Color.get(-1, 0, 333, 210);
		col3 = Color.get(-1, 0, 222, 100);
		col4 = Color.get(-1, 0, 444, 432);
		
		if (OptionsMenu.diff == OptionsMenu.easy) {
			this.lvl = lvl;
			x = random.nextInt(64 * 16);
			y = random.nextInt(64 * 16);
			health = maxHealth = lvl * lvl * 10;
			if (ModeMenu.creative) health = maxHealth = 1;
		}

		if (OptionsMenu.diff == OptionsMenu.norm) {
			this.lvl = lvl;
			x = random.nextInt(64 * 16);
			y = random.nextInt(64 * 16);
			health = maxHealth = lvl * lvl * 15;
			if (ModeMenu.creative) health = maxHealth = 1;
		}

		if (OptionsMenu.diff == OptionsMenu.hard) {
			this.lvl = lvl;
			x = random.nextInt(64 * 16);
			y = random.nextInt(64 * 16);
			health = maxHealth = lvl * lvl * 29;
			if (ModeMenu.creative) health = maxHealth = 1;
		}
	}

	public void tick() {
		super.tick();

		isenemy = true;

		if (health < maxHealth) {
			if (level.player != null && randomWalkTime == 0) {
				int xd = level.player.x - x;
				int yd = level.player.y - y;
				if (xd * xd + yd * yd < 200 * 200) {
					xa = 0;
					ya = 0;
					if (xd < 0) xa = +1;
					xe = xa;
					if (xd > 0) xa = -1;
					xe = xa;
					if (yd < 0) ya = +1;
					xe = xa;
					if (yd > 0) ya = -1;
					xe = xa;
				}
			}
		}

		int speed = tickTime & 1;
		if (!move(xa * speed, ya * speed) || random.nextInt(40) == 0) {
			randomWalkTime = 45;
			xa = (random.nextInt(3) - 1) * random.nextInt(2);
			ya = (random.nextInt(3) - 1) * random.nextInt(2);
		}
		if (randomWalkTime > 0) randomWalkTime--;
	}

	public void render(Screen screen) {
		int xt = 10;
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

		int xo = x - 8;
		int yo = y - 11;

		int col0 = Color.get(-1, 000, 444, 321);

		int col1 = Color.get(-1, 000, 555, 432);

		int col2 = Color.get(-1, 000, 333, 210);

		int col3 = Color.get(-1, 000, 222, 100);

		int col4 = Color.get(-1, 000, 444, 432);

		if (isLight()) {
			col0 = Color.get(-1, 000, 555, 432);

			col1 = Color.get(-1, 000, 555, 432);

			col2 = Color.get(-1, 000, 555, 432);

			col3 = Color.get(-1, 000, 555, 432);

			col4 = Color.get(-1, 000, 555, 432);
		} else {
			col0 = Color.get(-1, 000, 444, 321);

			col1 = Color.get(-1, 000, 555, 432);

			col2 = Color.get(-1, 000, 333, 210);

			col3 = Color.get(-1, 000, 222, 100);

			col4 = Color.get(-1, 000, 444, 432);
		}

		if (level.dirtColor == 322) {
			if (Game.time == 0) {
				int col = col0;

				if (lvl == 2) col = Color.get(-1, 100, 522, 555);
				if (lvl == 3) col = Color.get(-1, 111, 444, 555);
				if (lvl == 4) col = Color.get(-1, 000, 111, 555);
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

				if (lvl == 2) col = Color.get(-1, 100, 522, 555);
				if (lvl == 3) col = Color.get(-1, 111, 444, 555);
				if (lvl == 4) col = Color.get(-1, 000, 111, 555);
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

				if (lvl == 2) col = Color.get(-1, 100, 522, 555);
				if (lvl == 3) col = Color.get(-1, 111, 444, 555);
				if (lvl == 4) col = Color.get(-1, 000, 111, 555);
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

				if (lvl == 2) col = Color.get(-1, 100, 522, 555);
				if (lvl == 3) col = Color.get(-1, 111, 444, 555);
				if (lvl == 4) col = Color.get(-1, 000, 111, 555);
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

			if (lvl == 2) col = Color.get(-1, 100, 522, 555);
			if (lvl == 3) col = Color.get(-1, 111, 444, 555);
			if (lvl == 4) col = Color.get(-1, 000, 111, 555);
			if (hurtTime > 0) {
				col = Color.get(-1, 555, 555, 555);
			}

			screen.render(xo + 8 * flip1, yo + 0, xt + yt * 32, col, flip1);
			screen.render(xo + 8 - 8 * flip1, yo + 0, xt + 1 + yt * 32, col, flip1);
			screen.render(xo + 8 * flip2, yo + 8, xt + (yt + 1) * 32, col, flip2);
			screen.render(xo + 8 - 8 * flip2, yo + 8, xt + 1 + (yt + 1) * 32, col, flip2);
		}
	}

	public boolean canWool() {
		return true;
	}

	protected void die() {
		super.die();

		if (OptionsMenu.diff == OptionsMenu.easy) {
			int count = random.nextInt(3) + 1;
			for (int i = 0; i < count; i++) {
				level.add(
						new ItemEntity(
								new ResourceItem(Resource.wool),
								x + random.nextInt(11) - 5,
								y + random.nextInt(11) - 5));
			}
			if (level.player != null) {
				level.player.score += 10 * lvl;
			}
		}
		if (OptionsMenu.diff == OptionsMenu.norm) {
			int count = random.nextInt(2) + 1;
			for (int i = 0; i < count; i++) {
				level.add(
						new ItemEntity(
								new ResourceItem(Resource.wool),
								x + random.nextInt(11) - 5,
								y + random.nextInt(11) - 5));
			}
			if (level.player != null) {
				level.player.score += 10 * lvl;
			}
		}
		if (OptionsMenu.diff == OptionsMenu.hard) {
			int count = random.nextInt(2);
			for (int i = 0; i < count; i++) {
				level.add(
						new ItemEntity(
								new ResourceItem(Resource.wool),
								x + random.nextInt(11) - 5,
								y + random.nextInt(11) - 5));
			}
			if (level.player != null) {
				level.player.score += 10 * lvl;
			}
		}
	}
}
