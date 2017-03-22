package com.mojang.ld22.entity;

import com.mojang.ld22.Game;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.item.ResourceItem;
import com.mojang.ld22.item.resource.Resource;
import com.mojang.ld22.screen.ModeMenu;
import com.mojang.ld22.screen.OptionsMenu;

public class Zombie extends Mob {
	int xa;
	int ya;
	int xe = xa;
	int ye = ya;
	private int lvl;
	private int randomWalkTime = 0;

	public Zombie(int lvl) {
		
		this.lvl = lvl;
		this.col0 = Color.get(-1, 10, 152, 40);
		this.col1 = Color.get(-1, 20, 252, 50);
		this.col2 = Color.get(-1, 10, 152, 40);
		this.col3 = Color.get(-1, 0, 30, 20);
		this.col4 = Color.get(-1, 10, 42, 30);
		
		if (OptionsMenu.diff == OptionsMenu.easy) {
			this.lvl = lvl;
			x = random.nextInt(64 * 16);
			y = random.nextInt(64 * 16);
			if (ModeMenu.creative) health = maxHealth = 1;
			else health = maxHealth = lvl * lvl * 5;
		}

		if (OptionsMenu.diff == OptionsMenu.norm) {
			this.lvl = lvl;
			x = random.nextInt(64 * 16);
			y = random.nextInt(64 * 16);
			if (ModeMenu.creative) health = maxHealth = 1;
			else health = maxHealth = lvl * lvl * 10;
		}

		if (OptionsMenu.diff == OptionsMenu.hard) {
			this.lvl = lvl;
			x = random.nextInt(64 * 16);
			y = random.nextInt(64 * 16);
			if (ModeMenu.creative) health = maxHealth = 1;
			else health = maxHealth = lvl * lvl * 20;
		}
	}

	public void tick() {
		super.tick();

		isenemy = true;

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
		int xt = 0;
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
		} else {
			col0 = Color.get(-1, 10, 152, 40);

			col1 = Color.get(-1, 20, 252, 50);

			col2 = Color.get(-1, 10, 152, 40);

			col3 = Color.get(-1, 0, 30, 20);

			col4 = Color.get(-1, 10, 152, 40);
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
	}

	public boolean canWool() {
		return true;
	}

	protected void die() {
		super.die();

		if (OptionsMenu.diff == OptionsMenu.easy) {
			int count = random.nextInt(3) + 2;
			for (int i = 0; i < count; i++) {
				level.add(
						new ItemEntity(
								new ResourceItem(Resource.cloth),
								x + random.nextInt(11) - 5,
								y + random.nextInt(11) - 5));
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
		
		if (level.player != null) {
			level.player.score += (50 * lvl) * Game.multiplyer;
		}

		Game.multiplyer++;
		Game.multiplyertime = Game.mtm -= 5;
	}
}
