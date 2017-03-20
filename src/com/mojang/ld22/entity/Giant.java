//new class, no comments.
package com.mojang.ld22.entity;

import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.item.ResourceItem;
import com.mojang.ld22.item.resource.Resource;
import com.mojang.ld22.screen.OptionsMenu;

public class Giant extends Mob {
	int xa;
	int ya;
	int xe = xa;
	int ye = ya;
	private int lvl;
	private int randomWalkTime = 0;
	int wait = 0;

	int p1 = 539;
	int p2 = 540;
	int p3 = 541;
	int p4 = 572;
	int p5 = 571;
	int p6 = 602;
	int p7 = 573;
	int p8 = 606;
	int p9 = 604;

	public Giant(int lvl) {

		this.lvl = lvl;
		int lvls = lvl + random.nextInt(5);
		int eh = lvls * 5 * lvls;
		int eh2 = lvls * lvls + eh;
		x = random.nextInt(64 * 16);
		y = random.nextInt(64 * 16);
		health = maxHealth = 1000 + eh + eh2;
	}

	public void tick() {
		super.tick();
		xr = 6;
		yr = 8;
		isenemy = true;

		wait++;

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

	public void render(Screen screen) {
		int xt = 25;
		int yt = 16;

		int flip1 = (walkDist >> 3) & 1;
		int flip2 = (walkDist >> 3) & 1;

		/*
		 * Blue = West = dir2
		 * Grey = South = dir0
		 * Red = North = dir1
		 * Yellow = East = dir3 & higher
		 */

		//int col1 = Color.get(-1, 10, 152, 40);
		int col1 = Color.get(-1, 111, 222, 333);

		if (dir == 0) {
			col1 = Color.get(-1, 111, 222, 333);
		}

		if (dir == 1) {
			xt += 2;
			col1 = Color.get(-1, 100, 200, 300);
		}
		if (dir > 1) {

			flip1 = 1;
			flip2 = ((walkDist >> 4) & 1);
			col1 = Color.get(-1, 110, 220, 330);
			if (dir == 2) {
				flip1 = 1;
				col1 = Color.get(-1, 5, 105, 205);
			}
			xt += 2 + ((walkDist >> 4) & 1) * 2;
		}

		int xo = x - 8;
		int yo = y - 11;

		if (hurtTime > 0) {
			col1 = Color.get(-1, 555, 555, 555);
		}

		if (flip1 == 1) {
			screen.render(xo + 8, yo + 0, p1, col1, flip1); // 1

			screen.render(xo + 8 - 8, yo + 0, p2, col1, flip1); // 2

			screen.render(xo + 16, yo + 0, p3, col1, flip1); // 3

			screen.render(xo + 8 - 8, yo + 8, p4, col1, flip1); // 4

			screen.render(xo + 8, yo + 8, p5, col1, flip1); // 5

			screen.render(xo + 16, yo + 8, p7, col1, flip1); // 6

			screen.render(xo + 0, yo + 16, p9, col1, flip1); // 7

			screen.render(xo + 8, yo + 16, p8, col1, flip1); // 8

			//screen.render(xo + 16, yo + 16, p6, col1, flip1); // 9, invisible for giant
		} else if (flip1 != 1) {
			screen.render(xo + 8, yo + 0, p1, col1, flip1); // 2

			screen.render(xo + 16, yo + 0, p2, col1, flip1); // 1

			screen.render(xo + 8 - 8, yo + 0, p3, col1, flip1); // 3

			screen.render(xo + 16, yo + 8, p4, col1, flip1); // 4

			screen.render(xo + 8, yo + 8, p5, col1, flip1); // 5

			screen.render(xo + 8 - 8, yo + 8, p7, col1, flip1); // 6

			screen.render(xo + 16, yo + 16, p9, col1, flip1); // 7

			screen.render(xo + 8, yo + 16, p8, col1, flip1); // 8
		}
	}

	protected void touchedBy(Entity entity) {
		if (OptionsMenu.diff == OptionsMenu.easy) {
			if (entity instanceof Player) {
				entity.hurt(this, 3, dir);
			}
		}
		if (OptionsMenu.diff == OptionsMenu.norm) {
			if (entity instanceof Player) {
				entity.hurt(this, 3, dir);
			}
		}
		if (OptionsMenu.diff == OptionsMenu.hard) {
			if (entity instanceof Player) {
				entity.hurt(this, 4, dir);
			}
		}
	}

	public boolean canWool() {
		return true;
	}

	protected void die() {
		super.die();

		int count = random.nextInt(6) + 2;
		for (int i = 0; i < count; i++) {
			level.add(
					new ItemEntity(
							new ResourceItem(Resource.cloth),
							x + random.nextInt(11) - 5,
							y + random.nextInt(11) - 5));
		}

		if (level.player != null) {
			level.player.score += 200 * lvl;
		}
	}
}
