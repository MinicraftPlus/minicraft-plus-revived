//new class, no comments.
package com.mojang.ld22.entity;

import com.mojang.ld22.Game;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.item.ResourceItem;
import com.mojang.ld22.item.resource.Resource;
import com.mojang.ld22.screen.ModeMenu;
import com.mojang.ld22.screen.OptionsMenu;

public class Skeleton extends Mob {
	int xa, ya, xe, ye;
	//private int lvl;
	private int randomWalkTime;
	public int arrowtime;
	public int artime;

	public Skeleton(int lvl) {
		xe = xa;
		ye = ya;
		if(lvl == 0) lvl = 1;
		randomWalkTime = 0;
		arrowtime = 70 / (lvl + 1);
		artime = arrowtime;
		
		this.lvl = lvl;
		col0 = Color.get(-1, 111, 40, 444);
		col1 = Color.get(-1, 222, 50, 555);
		col2 = Color.get(-1, 111, 40, 444);
		col3 = Color.get(-1, 0, 30, 333);
		col4 = Color.get(-1, 111, 40, 444);
		
		if (OptionsMenu.diff == OptionsMenu.easy) {
			this.lvl = lvl;
			x = random.nextInt(64 * 16);
			y = random.nextInt(64 * 16);
			if (ModeMenu.creative) health = maxHealth = 1;
			else health = maxHealth = lvl * lvl * 6;
		}

		if (OptionsMenu.diff == OptionsMenu.norm) {
			this.lvl = lvl;
			x = random.nextInt(64 * 16);
			y = random.nextInt(64 * 16);
			if (ModeMenu.creative) health = maxHealth = 1;
			else health = maxHealth = lvl * lvl * 12;
		}

		if (OptionsMenu.diff == OptionsMenu.hard) {
			this.lvl = lvl;
			x = random.nextInt(64 * 16);
			y = random.nextInt(64 * 16);
			if (ModeMenu.creative) health = maxHealth = 1;
			else health = maxHealth = lvl * lvl * 24;
		}
	}

	public void tick() {
		super.tick();

		isenemy = true;

		if (level.player != null && randomWalkTime == 0) {
			boolean done = false;

			artime--;

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

				if (artime < 1) {
					switch (dir) {
						case 0:
							level.add(new Arrow(this, 0, 1, lvl, done));
							artime = arrowtime;
							break;
						case 1:
							level.add(new Arrow(this, 0, -1, lvl, done));
							artime = arrowtime;
							break;
						case 2:
							level.add(new Arrow(this, -1, 0, lvl, done));
							artime = arrowtime;
							break;
						case 3:
							level.add(new Arrow(this, 1, 0, lvl, done));
							artime = arrowtime;

							break;
						default:
							break;
					}
				}
			}
		}

		int speed = tickTime & 1;
		if (!move(xa * speed, ya * speed) || random.nextInt(200) == 0) {
			randomWalkTime = 45;
			xa = (random.nextInt(3) - 1) * random.nextInt(2);
			ya = (random.nextInt(3) - 1) * random.nextInt(2);
		}
		if (randomWalkTime > 0) randomWalkTime--;
	}

	public void render(Screen screen) {
		int xt = 8;
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

		int col0 = Color.get(-1, 111, 40, 444);

		int col1 = Color.get(-1, 222, 50, 555);

		int col2 = Color.get(-1, 111, 40, 444);

		int col3 = Color.get(-1, 000, 30, 333);

		int col4 = Color.get(-1, 111, 40, 444);

		if (isLight()) {
			col0 = Color.get(-1, 222, 50, 555);

			col1 = Color.get(-1, 222, 50, 555);

			col2 = Color.get(-1, 222, 50, 555);

			col3 = Color.get(-1, 222, 50, 555);

			col4 = Color.get(-1, 222, 50, 555);
		} else {
			col0 = Color.get(-1, 111, 40, 444);

			col1 = Color.get(-1, 222, 50, 555);

			col2 = Color.get(-1, 111, 40, 444);

			col3 = Color.get(-1, 000, 30, 333);

			col4 = Color.get(-1, 111, 40, 444);
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
	}

	public boolean canWool() {
		return true;
	}

	protected void die() {
		super.die();

		if (OptionsMenu.diff == OptionsMenu.easy) {
			int count = random.nextInt(3) + 1;
			int bookcount = random.nextInt(1) + 1;
			int rand = random.nextInt(20);
			if (rand <= 13)
				for (int i = 0; i < count; i++) {
					level.add(
							new ItemEntity(
									new ResourceItem(Resource.bone),
									x + random.nextInt(11) - 5,
									y + random.nextInt(11) - 5));
					level.add(
							new ItemEntity(
									new ResourceItem(Resource.arrow),
									x + random.nextInt(11) - 5,
									y + random.nextInt(11) - 5));
				}
			else if (rand >= 14 && rand != 19)
				for (int i = 0; i < bookcount; i++) {
					level.add(
							new ItemEntity(
									new ResourceItem(Resource.arrow),
									x + random.nextInt(11) - 5,
									y + random.nextInt(11) - 5));
					level.add(
							new ItemEntity(
									new ResourceItem(Resource.bookant),
									x + random.nextInt(11) - 5,
									y + random.nextInt(11) - 5));
				}
			else if (rand == 19) // rare chance of 10 arrows
			for (int i = 0; i < 10; i++) {
					level.add(
							new ItemEntity(
									new ResourceItem(Resource.arrow),
									x + random.nextInt(11) - 5,
									y + random.nextInt(11) - 5));
				}
		}
		if (OptionsMenu.diff == OptionsMenu.norm) {
			int count = random.nextInt(2) + 1;
			int bookcount = random.nextInt(1) + 1;
			int rand = random.nextInt(20);
			if (rand <= 18)
				for (int i = 0; i < count; i++) {
					level.add(
							new ItemEntity(
									new ResourceItem(Resource.arrow),
									x + random.nextInt(11) - 5,
									y + random.nextInt(11) - 5));
					level.add(
							new ItemEntity(
									new ResourceItem(Resource.bone),
									x + random.nextInt(11) - 5,
									y + random.nextInt(11) - 5));
				}
			else if (rand >= 19)
				for (int i = 0; i < bookcount; i++) {
					level.add(
							new ItemEntity(
									new ResourceItem(Resource.arrow),
									x + random.nextInt(11) - 5,
									y + random.nextInt(11) - 5));
					level.add(
							new ItemEntity(
									new ResourceItem(Resource.bookant),
									x + random.nextInt(11) - 5,
									y + random.nextInt(11) - 5));
				}
		}
		if (OptionsMenu.diff == OptionsMenu.hard) {
			int count = random.nextInt(1) + 1;
			int bookcount = random.nextInt(1) + 1;
			int rand = random.nextInt(30);
			if (rand <= 28)
				for (int i = 0; i < count; i++) {
					level.add(
							new ItemEntity(
									new ResourceItem(Resource.arrow),
									x + random.nextInt(11) - 5,
									y + random.nextInt(11) - 5));
					level.add(
							new ItemEntity(
									new ResourceItem(Resource.bone),
									x + random.nextInt(11) - 5,
									y + random.nextInt(11) - 5));
				}
			else if (rand >= 29)
				for (int i = 0; i < bookcount; i++) {
					level.add(
							new ItemEntity(
									new ResourceItem(Resource.arrow),
									x + random.nextInt(11) - 5,
									y + random.nextInt(11) - 5));
					level.add(
							new ItemEntity(
									new ResourceItem(Resource.bookant),
									x + random.nextInt(11) - 5,
									y + random.nextInt(11) - 5));
				}
		}

		if (level.player != null) {
			level.player.score += (50 * lvl) * Game.multiplier;
		}

		Game.multiplier++;
		Game.multipliertime = Game.mtm -= 5;
	}
}
