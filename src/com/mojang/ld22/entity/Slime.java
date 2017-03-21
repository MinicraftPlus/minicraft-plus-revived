package com.mojang.ld22.entity;

import com.mojang.ld22.Game;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.item.ResourceItem;
import com.mojang.ld22.item.resource.Resource;
import com.mojang.ld22.screen.ModeMenu;
import com.mojang.ld22.screen.OptionsMenu;

public class Slime extends Mob {
	private int xa, ya;
	private int jumpTime = 0;
	private int lvl;

	public Slime(int lvl) {
		this.col0 = Color.get(-1, 20, 40, 10);
		this.col1 = Color.get(-1, 20, 30, 40);
		this.col2 = Color.get(-1, 20, 40, 10);
		this.col3 = Color.get(-1, 10, 20, 40);
		this.col4 = Color.get(-1, 10, 20, 30);
		
		if (OptionsMenu.diff == OptionsMenu.easy) {
			this.lvl = lvl;
			x = random.nextInt(64 * 16);
			y = random.nextInt(64 * 16);
			if (ModeMenu.creative) health = maxHealth = 1;
			else health = maxHealth = lvl * lvl * 3;
		}

		if (OptionsMenu.diff == OptionsMenu.norm) {
			this.lvl = lvl;
			x = random.nextInt(64 * 16);
			y = random.nextInt(64 * 16);
			if (ModeMenu.creative) health = maxHealth = 1;
			else health = maxHealth = lvl * lvl * 5;
		}

		if (OptionsMenu.diff == OptionsMenu.hard) {
			this.lvl = lvl;
			x = random.nextInt(64 * 16);
			y = random.nextInt(64 * 16);
			if (ModeMenu.creative) health = maxHealth = 1;
			else health = maxHealth = lvl * lvl * 10;
		}
	}

	public void tick() {
		super.tick();

		isenemy = true;

		int speed = 1;
		if (!move(xa * speed, ya * speed) || random.nextInt(40) == 0) {
			if (jumpTime <= -10) {
				xa = (random.nextInt(3) - 1);
				ya = (random.nextInt(3) - 1);

				if (level.player != null) {
					int xd = level.player.x - x;
					int yd = level.player.y - y;
					if (xd * xd + yd * yd < 50 * 50) {
						if (xd < 0) xa = -1;
						if (xd > 0) xa = +1;
						if (yd < 0) ya = -1;
						if (yd > 0) ya = +1;
					}
				}

				if (xa != 0 || ya != 0) jumpTime = 10;
			}
		}

		jumpTime--;
		if (jumpTime == 0) {
			xa = ya = 0;
		}
	}

	protected void die() {
		super.die();

		if (OptionsMenu.diff == OptionsMenu.easy) {
			int count = random.nextInt(3) + 1;
			for (int i = 0; i < count; i++) {
				level.add(
						new ItemEntity(
								new ResourceItem(Resource.slime),
								x + random.nextInt(11) - 5,
								y + random.nextInt(11) - 5));
			}
		}
		if (OptionsMenu.diff == OptionsMenu.norm) {
			int count = random.nextInt(2) + 1;
			for (int i = 0; i < count; i++) {
				level.add(
						new ItemEntity(
								new ResourceItem(Resource.slime),
								x + random.nextInt(11) - 5,
								y + random.nextInt(11) - 5));
			}
		}
		if (OptionsMenu.diff == OptionsMenu.hard) {
			int count = random.nextInt(1) + 1;
			for (int i = 0; i < count; i++) {
				level.add(
						new ItemEntity(
								new ResourceItem(Resource.slime),
								x + random.nextInt(11) - 5,
								y + random.nextInt(11) - 5));
			}
		}

		if (level.player != null) {
			level.player.score += (25 * lvl) * Game.multiplyer;
		}

		Game.multiplyer++;
		Game.multiplyertime = Game.mtm -= 5;
	}

	public void render(Screen screen) {
		int xt = 0;
		int yt = 18;

		int xo = x - 8;
		int yo = y - 11;

		if (jumpTime > 0) {
			xt += 2;
			yo -= 4;
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
		} else {
			col0 = Color.get(-1, 20, 40, 222);

			col1 = Color.get(-1, 30, 252, 333);

			col2 = Color.get(-1, 20, 40, 222);

			col3 = Color.get(-1, 10, 20, 111);

			col4 = Color.get(-1, 20, 40, 222);
		}

		if (level.dirtColor == 322) {

			if (Game.time == 0) {
				int col = col0;

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
			if (Game.time == 1) {
				int col = col1;

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
}
