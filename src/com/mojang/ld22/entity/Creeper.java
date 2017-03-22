//new class, no comments.
package com.mojang.ld22.entity;

import com.mojang.ld22.Game;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.item.ResourceItem;
import com.mojang.ld22.item.resource.Resource;
import com.mojang.ld22.level.tile.Tile;
import com.mojang.ld22.screen.ModeMenu;
import com.mojang.ld22.screen.OptionsMenu;
import com.mojang.ld22.sound.Sound;

public class Creeper extends Mob {
	private int MAX_FUSE_TIME = 60;
	private int BLAST_RADIUS = 60;
	private int BLAST_DAMAGE = 10;

	private int xa, ya;
	private int lvl;
	private int randomWalkTime = 0;
	private int fuseTime = 0;
	private boolean fuseLit = false;

	public Creeper(int lvl) {
		if (lvl == 0) lvl = 1;
		this.col0 = Color.get(-1, 10, 50, 40);
		this.col1 = Color.get(-1, 20, 50, 40);
		this.col2 = Color.get(-1, 10, 50, 30);
		this.col3 = Color.get(-1, 0, 50, 30);
		this.col4 = Color.get(-1, 20, 50, 30);
		
		if (OptionsMenu.diff == OptionsMenu.easy) {
			this.lvl = lvl;
			x = random.nextInt(64 * 16);
			y = random.nextInt(64 * 16);
			if (ModeMenu.creative) health = maxHealth = 1;
			else health = maxHealth = lvl * lvl * 10;
		}

		if (OptionsMenu.diff == OptionsMenu.norm) {
			this.lvl = lvl;
			x = random.nextInt(64 * 16);
			y = random.nextInt(64 * 16);
			if (ModeMenu.creative) health = maxHealth = 1;
			else health = maxHealth = lvl * lvl * 20;
		}

		if (OptionsMenu.diff == OptionsMenu.hard) {
			this.lvl = lvl;
			x = random.nextInt(64 * 16);
			y = random.nextInt(64 * 16);
			if (ModeMenu.creative) health = maxHealth = 1;
			else health = maxHealth = lvl * lvl * 40;
		}
	}

	public boolean move(int xa, int ya) {
		boolean result = super.move(xa, ya);
		if (xa == 0 && ya == 0) walkDist = 0;
		return result;
	}

	public void tick() {
		super.tick();

		isenemy = true;

		if (OptionsMenu.diff == OptionsMenu.easy) {

			if (fuseTime == 0) {
				if (!fuseLit) {
					if (level.player != null && randomWalkTime == 0) {
						int xd = level.player.x - x;
						int yd = level.player.y - y;
						if (xd * xd + yd * yd < 50 * 50) {
							xa = 0;
							ya = 0;
							if (xd < 0) xa = -1;
							if (xd > 0) xa = +1;
							if (yd < 0) ya = -1;
							if (yd > 0) ya = +1;
						}
					}

					int speed = tickTime & 1;
					if (!move(xa * speed, ya * speed) || random.nextInt(200) == 0) {
						randomWalkTime = 60;
						xa = (random.nextInt(3) - 1) * random.nextInt(2);
						ya = (random.nextInt(3) - 1) * random.nextInt(2);
					}
					if (randomWalkTime > 0) randomWalkTime--;
				} else {
					// blow up
					int pdx = Math.abs(level.player.x - x);
					int pdy = Math.abs(level.player.y - y);
					if (pdx < BLAST_RADIUS && pdy < BLAST_RADIUS) {
						float pd = (float) Math.sqrt(pdx * pdx + pdy * pdy);
						int dmg = (int) (BLAST_DAMAGE * (1 - (pd / BLAST_RADIUS))) + 0;
						level.player.hurt(this, dmg, 0);
						level.player.payStamina(dmg * 1);
						Sound.explode.play();

						// figure out which tile the mob died on
						int xt = x >> 4;
						int yt = (y - 2) >> 4;

						// change tile to an appropriate crater
						if (lvl == 4) {
							level.setTile(xt, yt, Tile.infiniteFall, 0);
						} else if (lvl == 3) {
							level.setTile(xt, yt, Tile.lava, 0);
						} else {
							level.setTile(xt, yt, Tile.hole, 0);
						}

						super.die();
					} else {
						fuseTime = 0;
						fuseLit = false;
					}
				}
			} else {
				fuseTime--;
			}
		}

		if (OptionsMenu.diff == OptionsMenu.norm) {

			if (fuseTime == 0) {
				if (!fuseLit) {
					if (level.player != null && randomWalkTime == 0) {
						int xd = level.player.x - x;
						int yd = level.player.y - y;
						if (xd * xd + yd * yd < 50 * 50) {
							xa = 0;
							ya = 0;
							if (xd < 0) xa = -1;
							if (xd > 0) xa = +1;
							if (yd < 0) ya = -1;
							if (yd > 0) ya = +1;
						}
					}

					int speed = tickTime & 1;
					if (!move(xa * speed, ya * speed) || random.nextInt(200) == 0) {
						randomWalkTime = 60;
						xa = (random.nextInt(3) - 1) * random.nextInt(2);
						ya = (random.nextInt(3) - 1) * random.nextInt(2);
					}
					if (randomWalkTime > 0) randomWalkTime--;
				} else {
					// blow up
					int pdx = Math.abs(level.player.x - x);
					int pdy = Math.abs(level.player.y - y);
					if (pdx < BLAST_RADIUS && pdy < BLAST_RADIUS) {
						float pd = (float) Math.sqrt(pdx * pdx + pdy * pdy);
						int dmg = (int) (BLAST_DAMAGE * (1 - (pd / BLAST_RADIUS))) + 1;
						level.player.hurt(this, dmg, 0);
						level.player.payStamina(dmg * 2);
						Sound.explode.play();

						// figure out which tile the mob died on
						int xt = x >> 4;
						int yt = (y - 2) >> 4;

						// change tile to an appropriate crater
						if (lvl == 4) {
							level.setTile(xt, yt, Tile.infiniteFall, 0);
						} else if (lvl == 3) {
							level.setTile(xt, yt, Tile.lava, 0);
						} else {
							level.setTile(xt, yt, Tile.hole, 0);
						}

						super.die();
					} else {
						fuseTime = 0;
						fuseLit = false;
					}
				}
			} else {
				fuseTime--;
			}
		}

		if (OptionsMenu.diff == OptionsMenu.hard) {

			if (fuseTime == 0) {
				if (!fuseLit) {
					if (level.player != null && randomWalkTime == 0) {
						int xd = level.player.x - x;
						int yd = level.player.y - y;
						if (xd * xd + yd * yd < 50 * 50) {
							xa = 0;
							ya = 0;
							if (xd < 0) xa = -1;
							if (xd > 0) xa = +1;
							if (yd < 0) ya = -1;
							if (yd > 0) ya = +1;
						}
					}

					int speed = tickTime & 1;
					if (!move(xa * speed, ya * speed) || random.nextInt(200) == 0) {
						randomWalkTime = 60;
						xa = (random.nextInt(3) - 1) * random.nextInt(2);
						ya = (random.nextInt(3) - 1) * random.nextInt(2);
					}
					if (randomWalkTime > 0) randomWalkTime--;
				} else {
					// blow up
					int pdx = Math.abs(level.player.x - x);
					int pdy = Math.abs(level.player.y - y);
					if (pdx < BLAST_RADIUS && pdy < BLAST_RADIUS) {
						float pd = (float) Math.sqrt(pdx * pdx + pdy * pdy);
						int dmg = (int) (BLAST_DAMAGE * (1 - (pd / BLAST_RADIUS))) + 2;
						level.player.hurt(this, dmg, 0);
						level.player.payStamina(dmg * 2);
						Sound.explode.play();

						// figure out which tile the mob died on
						int xt = x >> 4;
						int yt = (y - 2) >> 4;

						// change tile to an appropriate crater
						if (lvl == 4) {
							level.setTile(xt, yt, Tile.infiniteFall, 0);
						} else if (lvl == 3) {
							level.setTile(xt, yt, Tile.lava, 0);
						} else {
							level.setTile(xt, yt, Tile.hole, 0);
						}

						super.die();
					} else {
						fuseTime = 0;
						fuseLit = false;
					}
				}
			} else {
				fuseTime--;
			}
		}
	}

	public void render(Screen screen) {
		int xt = 4;
		int yt = 18;

		if (walkDist > 0) {
			if (random.nextInt(2) == 0) {
				xt += 2;
			} else {
				xt += 4;
			}
		} else {
			xt = 4;
		}

		int xo = x - 8;
		int yo = y - 11;

		int col0 = Color.get(-1, 10, 30, 20);

		int col1 = Color.get(-1, 20, 40, 30);

		int col2 = Color.get(-1, 10, 30, 20);

		int col3 = Color.get(-1, 0, 20, 10);

		int col4 = Color.get(-1, 20, 40, 30);

		if (isLight()) {
			col0 = Color.get(-1, 20, 40, 30);

			col1 = Color.get(-1, 20, 40, 30);

			col2 = Color.get(-1, 20, 40, 30);

			col3 = Color.get(-1, 20, 40, 30);

			col4 = Color.get(-1, 20, 40, 30);
		} else {
			col0 = Color.get(-1, 10, 30, 20);

			col1 = Color.get(-1, 20, 40, 30);

			col2 = Color.get(-1, 10, 30, 20);

			col3 = Color.get(-1, 0, 20, 10);

			col4 = Color.get(-1, 20, 40, 30);
		}

		if (level.dirtColor == 322) {

			if (Game.time == 0) {
				int col = col0;
				if (lvl == 2) col = Color.get(-1, 200, 262, 232);
				if (lvl == 3) col = Color.get(-1, 200, 272, 222);
				if (lvl == 4) col = Color.get(-1, 200, 292, 282);

				if (fuseLit) {
					if (fuseTime % 6 == 0) {
						col = Color.get(-1, 252, 252, 252);
					}
				}

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
				if (lvl == 2) col = Color.get(-1, 200, 262, 232);
				if (lvl == 3) col = Color.get(-1, 200, 272, 222);
				if (lvl == 4) col = Color.get(-1, 200, 292, 282);

				if (fuseLit) {
					if (fuseTime % 6 == 0) {
						col = Color.get(-1, 252, 252, 252);
					}
				}

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
				if (lvl == 2) col = Color.get(-1, 200, 262, 232);
				if (lvl == 3) col = Color.get(-1, 200, 272, 222);
				if (lvl == 4) col = Color.get(-1, 200, 292, 282);

				if (fuseLit) {
					if (fuseTime % 6 == 0) {
						col = Color.get(-1, 252, 252, 252);
					}
				}

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
				if (lvl == 2) col = Color.get(-1, 200, 262, 232);
				if (lvl == 3) col = Color.get(-1, 200, 272, 222);
				if (lvl == 4) col = Color.get(-1, 200, 292, 282);

				if (fuseLit) {
					if (fuseTime % 6 == 0) {
						col = Color.get(-1, 252, 252, 252);
					}
				}

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
			if (lvl == 2) col = Color.get(-1, 200, 262, 232);
			if (lvl == 3) col = Color.get(-1, 200, 272, 222);
			if (lvl == 4) col = Color.get(-1, 200, 292, 282);

			if (fuseLit) {
				if (fuseTime % 6 == 0) {
					col = Color.get(-1, 252, 252, 252);
				}
			}

			if (hurtTime > 0) {
				col = Color.get(-1, 555, 555, 555);
			}

			screen.render(xo + 0, yo + 0, xt + yt * 32, col, 0);
			screen.render(xo + 8, yo + 0, xt + 1 + yt * 32, col, 0);
			screen.render(xo + 0, yo + 8, xt + (yt + 1) * 32, col, 0);
			screen.render(xo + 8, yo + 8, xt + 1 + (yt + 1) * 32, col, 0);
		}
	}

	protected void touchedBy(Entity entity) {
		if (entity instanceof Player) {
			if (fuseTime == 0) {
				Sound.fuse.play();
				fuseTime = MAX_FUSE_TIME;
				fuseLit = true;
			}
			entity.hurt(this, 1, dir);
		}
	}

	protected void die() {
		super.die();

		if (OptionsMenu.diff == OptionsMenu.easy) {
			int count = random.nextInt(3) + 1;
			for (int i = 0; i < count; i++) {
				level.add(
						new ItemEntity(
								new ResourceItem(Resource.gunp),
								x + random.nextInt(11) - 5,
								y + random.nextInt(11) - 5));
			}
		}
		if (OptionsMenu.diff == OptionsMenu.norm) {
			int count = random.nextInt(2) + 1;
			for (int i = 0; i < count; i++) {
				level.add(
						new ItemEntity(
								new ResourceItem(Resource.gunp),
								x + random.nextInt(11) - 5,
								y + random.nextInt(11) - 5));
			}
		}
		if (OptionsMenu.diff == OptionsMenu.hard) {
			int count = random.nextInt(1) + 1;
			for (int i = 0; i < count; i++) {
				level.add(
						new ItemEntity(
								new ResourceItem(Resource.gunp),
								x + random.nextInt(11) - 5,
								y + random.nextInt(11) - 5));
			}
		}
		if (level.player != null) {
			level.player.score += (50 * lvl) * Game.multiplyer;
		}

		Game.multiplyer++;
		Game.multiplyertime = Game.mtm -= 5;
	}
}
