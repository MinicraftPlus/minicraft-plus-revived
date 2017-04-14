package com.mojang.ld22.level.tile;

import com.mojang.ld22.entity.Entity;
import com.mojang.ld22.entity.ItemEntity;
import com.mojang.ld22.entity.Mob;
import com.mojang.ld22.entity.Player;
import com.mojang.ld22.entity.particle.SmashParticle;
import com.mojang.ld22.entity.particle.TextParticle;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.item.Item;
import com.mojang.ld22.item.ResourceItem;
import com.mojang.ld22.item.ToolItem;
import com.mojang.ld22.item.ToolType;
import com.mojang.ld22.item.resource.Resource;
import com.mojang.ld22.level.Level;
import com.mojang.ld22.level.tile.Tile;
import com.mojang.ld22.screen.ModeMenu;
import com.mojang.ld22.screen.OptionsMenu;
import com.mojang.ld22.sound.Sound;
import java.util.Random;

public class LightTile extends Tile {

	private Tile onType;
	private Random wRandom = new Random();
	int til = 0;
	public static int col0 = Color.get(141, 141, 252, 322);
	public static int col00 = Color.get(141, 141, 252, 322);
	public static int col1 = Color.get(552, 550, 440, 440);
	public static int col11 = Color.get(440, 550, 440, 322);
	public static int col2 = Color.get(10, 30, 151, 141);
	public static int col22 = Color.get(10, 30, 430, 141);
	public static int col222 = Color.get(10, 30, 320, 141);
	public static int col3 = Color.get(30, 40, 50, 550);
	int col4 = Color.get(5, 105, 115, 115);
	int col44 = Color.get(3, 105, 211, 322);
	int col444 = Color.get(3, 105, 440, 550);
	int col5 = Color.get(321, 321, 210, 210);
	int col6 = Color.get(10, 141, 555, 440);
	int col7 = Color.get(321, 0, 444, 555);
	int col8 = Color.get(210, 210, 430, 320);
	int col9 = Color.get(333, 333, 444, 444);
	int col10 = Color.get(320, 430, 430, 210);
	int col111 = Color.get(320, 430, 210, 430);
	int col12 = Color.get(444, 333, 333, 222);
	int col13 = Color.get(444, 333, 222, 333);
	int col14 = Color.get(222, 222, 220, 220);
	int col114 = Color.get(3, 222, 211, 321);
	int col1114 = Color.get(3, 222, 440, 550);
	int col15 = Color.get(444, 333, 444, 555);
	int col16 = Color.get(400, 500, 400, 500);
	int col17 = Color.get(13, 115, 13, 115);
	int col18 = Color.get(30, 40, 40, 50);
	int col19 = Color.get(550, 661, 440, 550);
	int col20 = Color.get(111, 111, 0, 111);
	int col21 = Color.get(20, 40, 50, 141);
	int col2222 = Color.get(20, 40, 50, 550);


	public LightTile(int id, Tile onType, int tile) {
		super(id);
		this.til = tile;
		if (tile == 0) {
				this.connectsToGrass = true;
  		}
  		if (tile == 1) {
				this.connectsToSand = true;
  		}
  		if (tile == 2) {
				this.connectsToGrass = true;
  		}
  		if (tile == 3) {
				this.connectsToSand = true;
  		}
  		if (tile == 4) {
				this.connectsToSand = true;
				this.connectsToWater = true;
  		}
  		if (tile == 5) {
				this.connectsToGrass = false;
  		}
  		if (tile == 6) {
				this.connectsToGrass = true;
  		}
  		if (tile == 7) {}
  		if (tile == 17) {
				this.connectsToSand = true;
				this.connectsToWater = true;
				this.connectsToLava = true;
  		}
  		if (tile == 25) {
				this.connectsToGrass = true;
  		}
  		if (tile == 26) {
				this.connectsToSand = true;
  		}
	}

	public void render(Screen screen, Level level, int x, int y) {
		int col;
		int col2;
		boolean transitionColor2;
		boolean u;
		boolean d;
		boolean l;
		if(this.til == 0) {
			col = col0;
			col2 = col00;
			transitionColor2 = !level.getTile(x, y - 1).connectsToGrass;
			u = !level.getTile(x, y + 1).connectsToGrass;
			d = !level.getTile(x - 1, y).connectsToGrass;
			l = !level.getTile(x + 1, y).connectsToGrass;
			if(!transitionColor2 && !d) {
				screen.render(x * 16 + 0, y * 16 + 0, 0, col, 0);
			} else {
				screen.render(x * 16 + 0, y * 16 + 0, (d?11:12) + (transitionColor2?0:1) * 32, col2, 0);
			}

			if(!transitionColor2 && !l) {
				screen.render(x * 16 + 8, y * 16 + 0, 1, col, 0);
			} else {
				screen.render(x * 16 + 8, y * 16 + 0, (l?13:12) + (transitionColor2?0:1) * 32, col2, 0);
			}

			if(!u && !d) {
				screen.render(x * 16 + 0, y * 16 + 8, 2, col, 0);
			} else {
				screen.render(x * 16 + 0, y * 16 + 8, (d?11:12) + (u?2:1) * 32, col2, 0);
			}

			if(!u && !l) {
				screen.render(x * 16 + 8, y * 16 + 8, 3, col, 0);
			} else {
				screen.render(x * 16 + 8, y * 16 + 8, (l?13:12) + (u?2:1) * 32, col2, 0);
			}
		} else if(this.til == 1) {
			col = col1;
			col2 = col11;
			transitionColor2 = !level.getTile(x, y - 1).connectsToSand;
			u = !level.getTile(x, y + 1).connectsToSand;
			d = !level.getTile(x - 1, y).connectsToSand;
			l = !level.getTile(x + 1, y).connectsToSand;
			if(!transitionColor2 && !d) {
				screen.render(x * 16 + 0, y * 16 + 0, 0, col, 0);
			} else {
				screen.render(x * 16 + 0, y * 16 + 0, (d?11:12) + (transitionColor2?0:1) * 32, col2, 0);
			}

			if(!transitionColor2 && !l) {
				screen.render(x * 16 + 8, y * 16 + 0, 1, col, 0);
			} else {
				screen.render(x * 16 + 8, y * 16 + 0, (l?13:12) + (transitionColor2?0:1) * 32, col2, 0);
			}

			if(!u && !d) {
				screen.render(x * 16 + 0, y * 16 + 8, 2, col, 0);
			} else {
				screen.render(x * 16 + 0, y * 16 + 8, (d?11:12) + (u?2:1) * 32, col2, 0);
			}

			if(!u && !l) {
				screen.render(x * 16 + 8, y * 16 + 8, 3, col, 0);
			} else {
				screen.render(x * 16 + 8, y * 16 + 8, (l?13:12) + (u?2:1) * 32, col2, 0);
			}
		} else {
			boolean r;
			boolean su;
			boolean sd;
			boolean sl;
			boolean sr;
			int transitionColor21;
			if(this.til == 2) {
				col2 = Color.get(10, 30, 151, 141);
				col = col2;
				col2 = col22;
				transitionColor21 = col222;
				u = level.getTile(x, y - 1) == this;
				d = level.getTile(x - 1, y) == this;
				l = level.getTile(x + 1, y) == this;
				r = level.getTile(x, y + 1) == this;
				su = level.getTile(x - 1, y - 1) == this;
				sd = level.getTile(x + 1, y - 1) == this;
				sl = level.getTile(x - 1, y + 1) == this;
				sr = level.getTile(x + 1, y + 1) == this;
				if(u && su && d) {
					screen.render(x * 16 + 0, y * 16 + 0, 42, col, 0);
				} else {
					screen.render(x * 16 + 0, y * 16 + 0, 9, col, 0);
				}

				if(u && sd && l) {
					screen.render(x * 16 + 8, y * 16 + 0, 74, transitionColor21, 0);
				} else {
					screen.render(x * 16 + 8, y * 16 + 0, 10, col, 0);
				}

				if(r && sl && d) {
					screen.render(x * 16 + 0, y * 16 + 8, 74, transitionColor21, 0);
				} else {
					screen.render(x * 16 + 0, y * 16 + 8, 41, col2, 0);
				}

				if(r && sr && l) {
					screen.render(x * 16 + 8, y * 16 + 8, 42, col, 0);
				} else {
					screen.render(x * 16 + 8, y * 16 + 8, 106, transitionColor21, 0);
				}
			} else if(this.til == 3) {
				col = col3;
				screen.render(x * 16 + 0, y * 16 + 0, 72, col, 0);
				screen.render(x * 16 + 8, y * 16 + 0, 73, col, 0);
				screen.render(x * 16 + 0, y * 16 + 8, 104, col, 0);
				screen.render(x * 16 + 8, y * 16 + 8, 105, col, 0);
			} else if(this.til == 4) {
				this.wRandom.setSeed((long)((tickCount + (x / 2 - y) * 4311) / 10) * 54687121L + (long)x * 3271612L + (long)y * 3412987161L);
				col = this.col4;
				col2 = this.col44;
				transitionColor21 = this.col444;
				u = !level.getTile(x, y - 1).connectsToWater;
				d = !level.getTile(x, y + 1).connectsToWater;
				l = !level.getTile(x - 1, y).connectsToWater;
				r = !level.getTile(x + 1, y).connectsToWater;
				su = u && level.getTile(x, y - 1).connectsToSand;
				sd = d && level.getTile(x, y + 1).connectsToSand;
				sl = l && level.getTile(x - 1, y).connectsToSand;
				sr = r && level.getTile(x + 1, y).connectsToSand;
				if(!u && !l) {
					screen.render(x * 16 + 0, y * 16 + 0, this.wRandom.nextInt(4), col, this.wRandom.nextInt(4));
				} else {
					screen.render(x * 16 + 0, y * 16 + 0, (l?14:15) + (u?0:1) * 32, !su && !sl?col2:transitionColor21, 0);
				}

				if(!u && !r) {
					screen.render(x * 16 + 8, y * 16 + 0, this.wRandom.nextInt(4), col, this.wRandom.nextInt(4));
				} else {
					screen.render(x * 16 + 8, y * 16 + 0, (r?16:15) + (u?0:1) * 32, !su && !sr?col2:transitionColor21, 0);
				}

				if(!d && !l) {
					screen.render(x * 16 + 0, y * 16 + 8, this.wRandom.nextInt(4), col, this.wRandom.nextInt(4));
				} else {
					screen.render(x * 16 + 0, y * 16 + 8, (l?14:15) + (d?2:1) * 32, !sd && !sl?col2:transitionColor21, 0);
				}

				if(!d && !r) {
					screen.render(x * 16 + 8, y * 16 + 8, this.wRandom.nextInt(4), col, this.wRandom.nextInt(4));
				} else {
					screen.render(x * 16 + 8, y * 16 + 8, (r?16:15) + (d?2:1) * 32, !sd && !sr?col2:transitionColor21, 0);
				}
			} else if(this.til == 5) {
				col = this.col5;
				if(level.depth != 0) {
					col = Color.get(222, 222, 111, 111);
				}

				screen.render(x * 16 + 0, y * 16 + 0, 0, col, 0);
				screen.render(x * 16 + 8, y * 16 + 0, 1, col, 0);
				screen.render(x * 16 + 0, y * 16 + 8, 2, col, 0);
				screen.render(x * 16 + 8, y * 16 + 8, 3, col, 0);
			} else if(this.til == 6) {
				col = this.col6;
				screen.render(x * 16 + 0, y * 16 + 0, 33, col, 0);
				screen.render(x * 16 + 8, y * 16 + 0, 2, Color.get(141, 141, 252, 322), 0);
				screen.render(x * 16 + 0, y * 16 + 8, 2, Color.get(141, 141, 252, 322), 0);
				screen.render(x * 16 + 8, y * 16 + 8, 33, col, 0);
			} else {
				byte col21;
				if(this.til == 7) {
					col = this.col7;
					col21 = 2;
					screen.render(x * 16 + 0, y * 16 + 0, col21 + 64, col, 0);
					screen.render(x * 16 + 8, y * 16 + 0, col21 + 1 + 64, col, 0);
					screen.render(x * 16 + 0, y * 16 + 8, col21 + 96, col, 0);
					screen.render(x * 16 + 8, y * 16 + 8, col21 + 1 + 96, col, 0);
				} else if(this.til == 8) {
					col = this.col7;
					col21 = 0;
					screen.render(x * 16 + 0, y * 16 + 0, col21 + 64, col, 0);
					screen.render(x * 16 + 8, y * 16 + 0, col21 + 1 + 64, col, 0);
					screen.render(x * 16 + 0, y * 16 + 8, col21 + 96, col, 0);
					screen.render(x * 16 + 8, y * 16 + 8, col21 + 1 + 96, col, 0);
				} else if(this.til == 9) {
					col = this.col8;
					screen.render(x * 16 + 0, y * 16 + 0, 51, col, 0);
					screen.render(x * 16 + 8, y * 16 + 0, 51, col, 0);
					screen.render(x * 16 + 0, y * 16 + 8, 51, col, 0);
					screen.render(x * 16 + 8, y * 16 + 8, 51, col, 0);
				} else if(this.til == 10) {
					col = this.col9;
					screen.render(x * 16 + 0, y * 16 + 0, 83, col, 0);
					screen.render(x * 16 + 8, y * 16 + 0, 83, col, 0);
					screen.render(x * 16 + 0, y * 16 + 8, 83, col, 0);
					screen.render(x * 16 + 8, y * 16 + 8, 83, col, 0);
				} else if(this.til == 11) {
					col = this.col10;
					screen.render(x * 16 + 0, y * 16 + 0, 704, col, 0);
					screen.render(x * 16 + 8, y * 16 + 0, 705, col, 0);
					screen.render(x * 16 + 0, y * 16 + 8, 736, col, 0);
					screen.render(x * 16 + 8, y * 16 + 8, 737, col, 0);
				} else if(this.til == 12) {
					col = this.col111;
					screen.render(x * 16 + 0, y * 16 + 0, 706, col, 0);
					screen.render(x * 16 + 8, y * 16 + 0, 707, col, 0);
					screen.render(x * 16 + 0, y * 16 + 8, 738, col, 0);
					screen.render(x * 16 + 8, y * 16 + 8, 739, col, 0);
				} else if(this.til == 13) {
					col = this.col12;
					screen.render(x * 16 + 0, y * 16 + 0, 768, col, 0);
					screen.render(x * 16 + 8, y * 16 + 0, 769, col, 0);
					screen.render(x * 16 + 0, y * 16 + 8, 800, col, 0);
					screen.render(x * 16 + 8, y * 16 + 8, 801, col, 0);
				} else if(this.til == 14) {
					col = this.col13;
					screen.render(x * 16 + 0, y * 16 + 0, 770, col, 0);
					screen.render(x * 16 + 8, y * 16 + 0, 771, col, 0);
					screen.render(x * 16 + 0, y * 16 + 8, 802, col, 0);
					screen.render(x * 16 + 8, y * 16 + 8, 803, col, 0);
				} else if(this.til == 17) {
					col = this.col14;
					col2 = this.col114;
					transitionColor21 = this.col1114;
					u = !level.getTile(x, y - 1).connectsToLiquid();
					d = !level.getTile(x, y + 1).connectsToLiquid();
					l = !level.getTile(x - 1, y).connectsToLiquid();
					r = !level.getTile(x + 1, y).connectsToLiquid();
					su = u && level.getTile(x, y - 1).connectsToSand;
					sd = d && level.getTile(x, y + 1).connectsToSand;
					sl = l && level.getTile(x - 1, y).connectsToSand;
					sr = r && level.getTile(x + 1, y).connectsToSand;
					if(!u && !l) {
						screen.render(x * 16 + 0, y * 16 + 0, 0, col, 0);
					} else {
						screen.render(x * 16 + 0, y * 16 + 0, (l?14:15) + (u?0:1) * 32, !su && !sl?col2:transitionColor21, 0);
					}

					if(!u && !r) {
						screen.render(x * 16 + 8, y * 16 + 0, 1, col, 0);
					} else {
						screen.render(x * 16 + 8, y * 16 + 0, (r?16:15) + (u?0:1) * 32, !su && !sr?col2:transitionColor21, 0);
					}

					if(!d && !l) {
						screen.render(x * 16 + 0, y * 16 + 8, 2, col, 0);
					} else {
						screen.render(x * 16 + 0, y * 16 + 8, (l?14:15) + (d?2:1) * 32, !sd && !sl?col2:transitionColor21, 0);
					}

					if(!d && !r) {
						screen.render(x * 16 + 8, y * 16 + 8, 3, col, 0);
					} else {
						screen.render(x * 16 + 8, y * 16 + 8, (r?16:15) + (d?2:1) * 32, !sd && !sr?col2:transitionColor21, 0);
					}
				} else if(this.til == 18) {
					col = this.col15;
					screen.render(x * 16 + 0, y * 16 + 0, 17, col, 0);
					screen.render(x * 16 + 8, y * 16 + 0, 17, col, 0);
					screen.render(x * 16 + 0, y * 16 + 8, 17, col, 0);
					screen.render(x * 16 + 8, y * 16 + 8, 17, col, 0);
				} else if(this.til == 19) {
					col = this.col16;
					screen.render(x * 16 + 0, y * 16 + 0, 17, col, 0);
					screen.render(x * 16 + 8, y * 16 + 0, 17, col, 0);
					screen.render(x * 16 + 0, y * 16 + 8, 17, col, 0);
					screen.render(x * 16 + 8, y * 16 + 8, 17, col, 0);
				} else if(this.til == 20) {
					col = this.col17;
					screen.render(x * 16 + 0, y * 16 + 0, 17, col, 0);
					screen.render(x * 16 + 8, y * 16 + 0, 17, col, 0);
					screen.render(x * 16 + 0, y * 16 + 8, 17, col, 0);
					screen.render(x * 16 + 8, y * 16 + 8, 17, col, 0);
				} else if(this.til == 21) {
					col = this.col18;
					screen.render(x * 16 + 0, y * 16 + 0, 17, col, 0);
					screen.render(x * 16 + 8, y * 16 + 0, 17, col, 0);
					screen.render(x * 16 + 0, y * 16 + 8, 17, col, 0);
					screen.render(x * 16 + 8, y * 16 + 8, 17, col, 0);
				} else if(this.til == 22) {
					col = this.col19;
					screen.render(x * 16 + 0, y * 16 + 0, 17, col, 0);
					screen.render(x * 16 + 8, y * 16 + 0, 17, col, 0);
					screen.render(x * 16 + 0, y * 16 + 8, 17, col, 0);
					screen.render(x * 16 + 8, y * 16 + 8, 17, col, 0);
				} else if(this.til == 23) {
					col = this.col20;
					screen.render(x * 16 + 0, y * 16 + 0, 17, col, 0);
					screen.render(x * 16 + 8, y * 16 + 0, 17, col, 0);
					screen.render(x * 16 + 0, y * 16 + 8, 17, col, 0);
					screen.render(x * 16 + 8, y * 16 + 8, 17, col, 0);
				} else if(this.til == 25) {
					col = this.col21;
					col2 = col0;
					screen.render(x * 16 + 0, y * 16 + 0, 0, col2, 0);
					screen.render(x * 16 + 8, y * 16 + 0, 0, col2, 0);
					screen.render(x * 16 + 0, y * 16 + 8, 0, col2, 0);
					screen.render(x * 16 + 8, y * 16 + 8, 0, col2, 0);
					screen.render(x * 16 + 4, y * 16 + 4, 107, col, 0);
				} else if(this.til == 26) {
					col = this.col2222;
					col2 = col1;
					screen.render(x * 16 + 0, y * 16 + 0, 0, col2, 0);
					screen.render(x * 16 + 8, y * 16 + 0, 0, col2, 0);
					screen.render(x * 16 + 0, y * 16 + 8, 0, col2, 0);
					screen.render(x * 16 + 8, y * 16 + 8, 0, col2, 0);
					screen.render(x * 16 + 4, y * 16 + 4, 107, col, 0);
				}
			}
		}

	}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return this.til == 4?e.canSwim():(this.til == 17?e.canSwim():(this.til != 2?this.til != 3 && this.til != 4 && this.til != 12 && this.til != 14:false));
	}

	public void bumpedInto(Level level, int x, int y, Entity entity) {
		if(this.til == 3) {
			if(OptionsMenu.diff == OptionsMenu.easy) {
				entity.hurt((Tile)this, x, y, 1);
			}

			if(OptionsMenu.diff == OptionsMenu.norm) {
				entity.hurt((Tile)this, x, y, 1);
			}

			if(OptionsMenu.diff == OptionsMenu.hard) {
				entity.hurt((Tile)this, x, y, 2);
			}
		}

	}

	public void tick(Level level, int xt, int yt) {
		if(level.getTile(xt, yt + 1) != Tile.torchgrass && level.getTile(xt, yt - 1) != Tile.torchgrass && level.getTile(xt + 1, yt) != Tile.torchgrass && level.getTile(xt - 1, yt) != Tile.torchgrass && level.getTile(xt, yt + 2) != Tile.torchgrass && level.getTile(xt, yt - 2) != Tile.torchgrass && level.getTile(xt + 2, yt) != Tile.torchgrass && level.getTile(xt - 2, yt) != Tile.torchgrass && level.getTile(xt + 1, yt + 1) != Tile.torchgrass && level.getTile(xt - 1, yt + 1) != Tile.torchgrass && level.getTile(xt - 1, yt - 1) != Tile.torchgrass && level.getTile(xt + 1, yt - 1) != Tile.torchgrass && level.getTile(xt, yt + 1) != Tile.torchsand && level.getTile(xt, yt - 1) != Tile.torchsand && level.getTile(xt + 1, yt) != Tile.torchsand && level.getTile(xt - 1, yt) != Tile.torchsand && level.getTile(xt, yt + 2) != Tile.torchsand && level.getTile(xt, yt - 2) != Tile.torchsand && level.getTile(xt + 2, yt) != Tile.torchsand && level.getTile(xt - 2, yt) != Tile.torchsand && level.getTile(xt + 1, yt + 1) != Tile.torchsand && level.getTile(xt - 1, yt + 1) != Tile.torchsand && level.getTile(xt - 1, yt - 1) != Tile.torchsand && level.getTile(xt + 1, yt - 1) != Tile.torchsand && level.getTile(xt, yt + 1) != Tile.torchdirt && level.getTile(xt, yt - 1) != Tile.torchdirt && level.getTile(xt + 1, yt) != Tile.torchdirt && level.getTile(xt - 1, yt) != Tile.torchdirt && level.getTile(xt, yt + 2) != Tile.torchdirt && level.getTile(xt, yt - 2) != Tile.torchdirt && level.getTile(xt + 2, yt) != Tile.torchdirt && level.getTile(xt - 2, yt) != Tile.torchdirt && level.getTile(xt + 1, yt + 1) != Tile.torchdirt && level.getTile(xt - 1, yt + 1) != Tile.torchdirt && level.getTile(xt - 1, yt - 1) != Tile.torchdirt && level.getTile(xt + 1, yt - 1) != Tile.torchdirt && level.getTile(xt, yt + 1) != Tile.torchplank && level.getTile(xt, yt - 1) != Tile.torchplank && level.getTile(xt + 1, yt) != Tile.torchplank && level.getTile(xt - 1, yt) != Tile.torchplank && level.getTile(xt, yt + 2) != Tile.torchplank && level.getTile(xt, yt - 2) != Tile.torchplank && level.getTile(xt + 2, yt) != Tile.torchplank && level.getTile(xt - 2, yt) != Tile.torchplank && level.getTile(xt + 1, yt + 1) != Tile.torchplank && level.getTile(xt - 1, yt + 1) != Tile.torchplank && level.getTile(xt - 1, yt - 1) != Tile.torchplank && level.getTile(xt + 1, yt - 1) != Tile.torchplank && level.getTile(xt, yt + 1) != Tile.torchsbrick && level.getTile(xt, yt - 1) != Tile.torchsbrick && level.getTile(xt + 1, yt) != Tile.torchsbrick && level.getTile(xt - 1, yt) != Tile.torchsbrick && level.getTile(xt, yt + 2) != Tile.torchsbrick && level.getTile(xt, yt - 2) != Tile.torchsbrick && level.getTile(xt + 2, yt) != Tile.torchsbrick && level.getTile(xt - 2, yt) != Tile.torchsbrick && level.getTile(xt + 1, yt + 1) != Tile.torchsbrick && level.getTile(xt - 1, yt + 1) != Tile.torchsbrick && level.getTile(xt - 1, yt - 1) != Tile.torchsbrick && level.getTile(xt + 1, yt - 1) != Tile.torchsbrick && level.getTile(xt, yt + 1) != Tile.torchlo && level.getTile(xt, yt - 1) != Tile.torchlo && level.getTile(xt + 1, yt) != Tile.torchlo && level.getTile(xt - 1, yt) != Tile.torchlo && level.getTile(xt, yt + 2) != Tile.torchlo && level.getTile(xt, yt - 2) != Tile.torchlo && level.getTile(xt + 2, yt) != Tile.torchlo && level.getTile(xt - 2, yt) != Tile.torchlo && level.getTile(xt + 1, yt + 1) != Tile.torchlo && level.getTile(xt - 1, yt + 1) != Tile.torchlo && level.getTile(xt - 1, yt - 1) != Tile.torchlo && level.getTile(xt + 1, yt - 1) != Tile.torchlo && level.getTile(xt, yt + 1) != Tile.torchwool && level.getTile(xt, yt - 1) != Tile.torchwool && level.getTile(xt + 1, yt) != Tile.torchwool && level.getTile(xt - 1, yt) != Tile.torchwool && level.getTile(xt, yt + 2) != Tile.torchwool && level.getTile(xt, yt - 2) != Tile.torchwool && level.getTile(xt + 2, yt) != Tile.torchwool && level.getTile(xt - 2, yt) != Tile.torchwool && level.getTile(xt + 1, yt + 1) != Tile.torchwool && level.getTile(xt - 1, yt + 1) != Tile.torchwool && level.getTile(xt - 1, yt - 1) != Tile.torchwool && level.getTile(xt + 1, yt - 1) != Tile.torchwool && level.getTile(xt, yt + 1) != Tile.torchwoolred && level.getTile(xt, yt - 1) != Tile.torchwoolred && level.getTile(xt + 1, yt) != Tile.torchwoolred && level.getTile(xt - 1, yt) != Tile.torchwoolred && level.getTile(xt, yt + 2) != Tile.torchwoolred && level.getTile(xt, yt - 2) != Tile.torchwoolred && level.getTile(xt + 2, yt) != Tile.torchwoolred && level.getTile(xt - 2, yt) != Tile.torchwoolred && level.getTile(xt + 1, yt + 1) != Tile.torchwoolred && level.getTile(xt - 1, yt + 1) != Tile.torchwoolred && level.getTile(xt - 1, yt - 1) != Tile.torchwoolred && level.getTile(xt + 1, yt - 1) != Tile.torchwoolred && level.getTile(xt, yt + 1) != Tile.torchwoolblue && level.getTile(xt, yt - 1) != Tile.torchwoolblue && level.getTile(xt + 1, yt) != Tile.torchwoolblue && level.getTile(xt - 1, yt) != Tile.torchwoolblue && level.getTile(xt, yt + 2) != Tile.torchwoolblue && level.getTile(xt, yt - 2) != Tile.torchwoolblue && level.getTile(xt + 2, yt) != Tile.torchwoolblue && level.getTile(xt - 2, yt) != Tile.torchwoolblue && level.getTile(xt + 1, yt + 1) != Tile.torchwoolblue && level.getTile(xt - 1, yt + 1) != Tile.torchwoolblue && level.getTile(xt - 1, yt - 1) != Tile.torchwoolblue && level.getTile(xt + 1, yt - 1) != Tile.torchwoolblue && level.getTile(xt, yt + 1) != Tile.torchwoolgreen && level.getTile(xt, yt - 1) != Tile.torchwoolgreen && level.getTile(xt + 1, yt) != Tile.torchwoolgreen && level.getTile(xt - 1, yt) != Tile.torchwoolgreen && level.getTile(xt, yt + 2) != Tile.torchwoolgreen && level.getTile(xt, yt - 2) != Tile.torchwoolgreen && level.getTile(xt + 2, yt) != Tile.torchwoolgreen && level.getTile(xt - 2, yt) != Tile.torchwoolgreen && level.getTile(xt + 1, yt + 1) != Tile.torchwoolgreen && level.getTile(xt - 1, yt + 1) != Tile.torchwoolgreen && level.getTile(xt - 1, yt - 1) != Tile.torchwoolgreen && level.getTile(xt + 1, yt - 1) != Tile.torchwoolgreen && level.getTile(xt, yt + 1) != Tile.torchwoolyellow && level.getTile(xt, yt - 1) != Tile.torchwoolyellow && level.getTile(xt + 1, yt) != Tile.torchwoolyellow && level.getTile(xt - 1, yt) != Tile.torchwoolyellow && level.getTile(xt, yt + 2) != Tile.torchwoolyellow && level.getTile(xt, yt - 2) != Tile.torchwoolyellow && level.getTile(xt + 2, yt) != Tile.torchwoolyellow && level.getTile(xt - 2, yt) != Tile.torchwoolyellow && level.getTile(xt + 1, yt + 1) != Tile.torchwoolyellow && level.getTile(xt - 1, yt + 1) != Tile.torchwoolyellow && level.getTile(xt - 1, yt - 1) != Tile.torchwoolyellow && level.getTile(xt + 1, yt - 1) != Tile.torchwoolyellow && level.getTile(xt, yt + 1) != Tile.torchwoolblack && level.getTile(xt, yt - 1) != Tile.torchwoolblack && level.getTile(xt + 1, yt) != Tile.torchwoolblack && level.getTile(xt - 1, yt) != Tile.torchwoolblack && level.getTile(xt, yt + 2) != Tile.torchwoolblack && level.getTile(xt, yt - 2) != Tile.torchwoolblack && level.getTile(xt + 2, yt) != Tile.torchwoolblack && level.getTile(xt - 2, yt) != Tile.torchwoolblack && level.getTile(xt + 1, yt + 1) != Tile.torchwoolblack && level.getTile(xt - 1, yt + 1) != Tile.torchwoolblack && level.getTile(xt - 1, yt - 1) != Tile.torchwoolblack && level.getTile(xt + 1, yt - 1) != Tile.torchwoolblack) {
			if(this.til == 0) {
				level.setTile(xt, yt, Tile.grass, 0);
			} else if(this.til == 1) {
				level.setTile(xt, yt, Tile.sand, 0);
			} else if(this.til == 2) {
				level.setTile(xt, yt, Tile.tree, 0);
			} else if(this.til == 3) {
				level.setTile(xt, yt, Tile.cactus, 0);
			} else if(this.til == 4) {
				level.setTile(xt, yt, Tile.water, 0);
			} else if(this.til == 5) {
				level.setTile(xt, yt, Tile.dirt, 0);
			} else if(this.til == 6) {
				level.setTile(xt, yt, Tile.flower, 0);
			} else if(this.til == 7) {
				level.setTile(xt, yt, Tile.stairsUp, 0);
			} else if(this.til == 8) {
				level.setTile(xt, yt, Tile.stairsDown, 0);
			} else if(this.til == 9) {
				level.setTile(xt, yt, Tile.plank, 0);
			} else if(this.til == 10) {
				level.setTile(xt, yt, Tile.sbrick, 0);
			} else if(this.til == 11) {
				level.setTile(xt, yt, Tile.wdo, 0);
			} else if(this.til == 12) {
				level.setTile(xt, yt, Tile.wdc, 0);
			} else if(this.til == 13) {
				level.setTile(xt, yt, Tile.sdo, 0);
			} else if(this.til == 14) {
				level.setTile(xt, yt, Tile.sdc, 0);
			} else if(this.til == 15) {
				level.setTile(xt, yt, Tile.odo, 0);
			} else if(this.til == 16) {
				level.setTile(xt, yt, Tile.odc, 0);
			} else if(this.til == 17) {
				level.setTile(xt, yt, Tile.hole, 0);
			} else if(this.til == 18) {
				level.setTile(xt, yt, Tile.wool, 0);
			} else if(this.til == 19) {
				level.setTile(xt, yt, Tile.redwool, 0);
			} else if(this.til == 20) {
				level.setTile(xt, yt, Tile.bluewool, 0);
			} else if(this.til == 21) {
				level.setTile(xt, yt, Tile.greenwool, 0);
			} else if(this.til == 22) {
				level.setTile(xt, yt, Tile.yellowwool, 0);
			} else if(this.til == 23) {
				level.setTile(xt, yt, Tile.blackwool, 0);
			} else if(this.til == 24) {
				level.setTile(xt, yt, Tile.o, 0);
			} else if(this.til == 25) {
				level.setTile(xt, yt, Tile.treeSapling, 0);
			} else if(this.til == 26) {
				level.setTile(xt, yt, Tile.cactusSapling, 0);
			}
		}

		int age;
		int yn;
		if(this.til == 0) {
			if(this.random.nextInt(40) != 0) {
				return;
			}

			age = xt;
			yn = yt;
			if(this.random.nextBoolean()) {
				age = xt + (this.random.nextInt(2) * 2 - 1);
			} else {
				yn = yt + (this.random.nextInt(2) * 2 - 1);
			}

			if(level.getTile(age, yn) == Tile.dirt) {
				level.setTile(age, yn, this, 0);
			}

			if(level.getTile(age, yn) == Tile.lightdirt) {
				level.setTile(age, yn, this, 0);
			}
		}

		if(this.til == 4) {
			age = xt;
			yn = yt;
			if(this.random.nextBoolean()) {
				age = xt + (this.random.nextInt(2) * 2 - 1);
			} else {
				yn = yt + (this.random.nextInt(2) * 2 - 1);
			}

			if(level.getTile(age, yn) == Tile.hole) {
				level.setTile(age, yn, this, 0);
			}

			if(level.getTile(age, yn) == Tile.lighthole) {
				level.setTile(age, yn, this, 0);
			}
		}

		if(this.til == 25) {
			age = level.getData(xt, yt) + 1;
			if(age > 100) {
				level.setTile(xt, yt, Tile.lighttree, 0);
			} else {
				level.setData(xt, yt, age);
			}
		}

		if(this.til == 26) {
			age = level.getData(xt, yt) + 1;
			if(age > 100) {
				level.setTile(xt, yt, Tile.lightcac, 0);
			} else {
				level.setData(xt, yt, age);
			}
		}

	}

	public void hurt(Level level, int x, int y, Mob source, int dmg, int attackDir) {
		int count;
		byte i;
		int count1;
		int i1;
		if(this.til == 3) {
			count = level.getData(x, y) + dmg;
			if(ModeMenu.creative) {
				i = 1;
			} else {
				i = 10;
			}

			level.add(new SmashParticle(x * 16 + 8, y * 16 + 8));
			level.add(new TextParticle("" + dmg, x * 16 + 8, y * 16 + 8, Color.get(-1, 500, 500, 500)));
			if(count >= i) {
				count1 = this.random.nextInt(2) + 2;

				for(i1 = 0; i1 < count1; ++i1) {
					level.add(new ItemEntity(new ResourceItem(Resource.cactusFlower), x * 16 + this.random.nextInt(10) + 3, y * 16 + this.random.nextInt(10) + 3));
				}

				level.setTile(x, y, Tile.sand, 0);
			} else {
				level.setData(x, y, count);
			}
		}

		int var11;
		if(this.til == 2) {
			count = this.random.nextInt(100) == 0?1:0;

			for(var11 = 0; var11 < count; ++var11) {
				level.add(new ItemEntity(new ResourceItem(Resource.apple), x * 16 + this.random.nextInt(10) + 3, y * 16 + this.random.nextInt(10) + 3));
			}

			count = level.getData(x, y) + dmg;
			if(ModeMenu.creative) {
				i = 1;
			} else {
				i = 20;
			}

			level.add(new SmashParticle(x * 16 + 8, y * 16 + 8));
			level.add(new TextParticle("" + dmg, x * 16 + 8, y * 16 + 8, Color.get(-1, 500, 500, 500)));
			if(count >= i) {
				count1 = this.random.nextInt(2) + 1;

				for(i1 = 0; i1 < count1; ++i1) {
					level.add(new ItemEntity(new ResourceItem(Resource.wood), x * 16 + this.random.nextInt(10) + 3, y * 16 + this.random.nextInt(10) + 3));
				}

				count1 = this.random.nextInt(this.random.nextInt(4) + 1);

				for(i1 = 0; i1 < count1; ++i1) {
					level.add(new ItemEntity(new ResourceItem(Resource.acorn), x * 16 + this.random.nextInt(10) + 3, y * 16 + this.random.nextInt(10) + 3));
				}

				level.setTile(x, y, Tile.grass, 0);
			} else {
				level.setData(x, y, count);
			}
		}

		if(this.til == 6) {
			count = this.random.nextInt(2) + 1;

			for(var11 = 0; var11 < count; ++var11) {
				level.add(new ItemEntity(new ResourceItem(Resource.flower), x * 16 + this.random.nextInt(10) + 3, y * 16 + this.random.nextInt(10) + 3));
			}

			count = this.random.nextInt(2);

			for(var11 = 0; var11 < count; ++var11) {
				level.add(new ItemEntity(new ResourceItem(Resource.rose), x * 16 + this.random.nextInt(10) + 3, y * 16 + this.random.nextInt(10) + 3));
			}

			level.setTile(x, y, Tile.grass, 0);
		}

		if(this.til == 11) {
			level.setTile(x, y, Tile.lwdc, 0);
		}

		if(this.til == 12) {
			level.setTile(x, y, Tile.lwdo, 0);
		}

		if(this.til == 13) {
			level.setTile(x, y, Tile.lsdc, 0);
		}

		if(this.til == 14) {
			level.setTile(x, y, Tile.lsdo, 0);
		}

	}

	public boolean interact(Level level, int xt, int yt, Player player, Item item, int attackDir) {
		ToolItem tool;
		if(this.til == 0 && item instanceof ToolItem) {
			tool = (ToolItem)item;
			if(tool.type == ToolType.shovel && player.payStamina(4 - tool.level)) {
				level.setTile(xt, yt, Tile.dirt, 0);
				Sound.monsterHurt.play();
				if(this.random.nextInt(5) == 0) {
					level.add(new ItemEntity(new ResourceItem(Resource.seeds), xt * 16 + this.random.nextInt(10) + 3, yt * 16 + this.random.nextInt(10) + 3));
					level.add(new ItemEntity(new ResourceItem(Resource.seeds), xt * 16 + this.random.nextInt(10) + 3, yt * 16 + this.random.nextInt(10) + 3));
					return true;
				}
			}

			if(tool.type == ToolType.spade && player.payStamina(4 - tool.level)) {
				level.setTile(xt, yt, Tile.dirt, 0);
				Sound.monsterHurt.play();
				if(this.random.nextInt(5) == 0) {
					return true;
				}
			}

			if(tool.type == ToolType.hoe && player.payStamina(4 - tool.level)) {
				Sound.monsterHurt.play();
				if(this.random.nextInt(5) == 0) {
					level.add(new ItemEntity(new ResourceItem(Resource.seeds), xt * 16 + this.random.nextInt(10) + 3, yt * 16 + this.random.nextInt(10) + 3));
					return true;
				}

				level.setTile(xt, yt, Tile.farmland, 0);
				return true;
			}
		}

		if(this.til == 1 && item instanceof ToolItem) {
			tool = (ToolItem)item;
			if(tool.type == ToolType.shovel && player.payStamina(4 - tool.level)) {
				level.setTile(xt, yt, Tile.dirt, 0);
				level.add(new ItemEntity(new ResourceItem(Resource.sand), xt * 16 + this.random.nextInt(10) + 3, yt * 16 + this.random.nextInt(10) + 3));
				return true;
			}

			if(tool.type == ToolType.spade && player.payStamina(5 - tool.level)) {
				level.setTile(xt, yt, Tile.dirt, 0);
				level.add(new ItemEntity(new ResourceItem(Resource.sand), xt * 16 + this.random.nextInt(10) + 3, yt * 16 + this.random.nextInt(10) + 3));
				return true;
			}
		}

		if(this.til == 2 && item instanceof ToolItem) {
			tool = (ToolItem)item;
			if(tool.type == ToolType.axe && player.payStamina(4 - tool.level)) {
				this.hurt(level, xt, yt, player, this.random.nextInt(10) + tool.level * 5 + 10, attackDir);
				return true;
			}

			if(tool.type == ToolType.hatchet && player.payStamina(3 - tool.level)) {
				this.hurt(level, xt, yt, player, this.random.nextInt(7) + tool.level * 5 + 5, attackDir);
				return true;
			}
		}

		if(this.til == 5 && item instanceof ToolItem) {
			tool = (ToolItem)item;
			if(tool.type == ToolType.shovel && player.payStamina(4 - tool.level)) {
				level.setTile(xt, yt, Tile.hole, 0);
				level.add(new ItemEntity(new ResourceItem(Resource.dirt), xt * 16 + this.random.nextInt(10) + 3, yt * 16 + this.random.nextInt(10) + 3));
				Sound.monsterHurt.play();
				return true;
			}

			if(tool.type == ToolType.spade && player.payStamina(5 - tool.level)) {
				level.setTile(xt, yt, Tile.hole, 0);
				level.add(new ItemEntity(new ResourceItem(Resource.dirt), xt * 16 + this.random.nextInt(10) + 3, yt * 16 + this.random.nextInt(10) + 3));
				Sound.monsterHurt.play();
				return true;
			}

			if(tool.type == ToolType.hoe && player.payStamina(4 - tool.level)) {
				level.setTile(xt, yt, Tile.farmland, 0);
				Sound.monsterHurt.play();
				return true;
			}
		}

		if(this.til == 6 && item instanceof ToolItem) {
			tool = (ToolItem)item;
			if(tool.type == ToolType.shovel && player.payStamina(2 - tool.level)) {
				level.add(new ItemEntity(new ResourceItem(Resource.flower), xt * 16 + this.random.nextInt(10) + 3, yt * 16 + this.random.nextInt(10) + 3));
				level.add(new ItemEntity(new ResourceItem(Resource.rose), xt * 16 + this.random.nextInt(10) + 3, yt * 16 + this.random.nextInt(10) + 3));
				level.setTile(xt, yt, Tile.grass, 0);
				return true;
			}
		}

		if(this.til == 9 && item instanceof ToolItem) {
			tool = (ToolItem)item;
			if(tool.type == ToolType.axe && player.payStamina(4 - tool.level)) {
				level.setTile(xt, yt, Tile.hole, 0);
				level.add(new ItemEntity(new ResourceItem(Resource.plank), xt * 16 + this.random.nextInt(10) + 3, yt * 16 + this.random.nextInt(10) + 3));
				Sound.monsterHurt.play();
				return true;
			}

			if(tool.type == ToolType.hatchet && player.payStamina(4 - tool.level)) {
				level.setTile(xt, yt, Tile.hole, 0);
				level.add(new ItemEntity(new ResourceItem(Resource.plank), xt * 16 + this.random.nextInt(10) + 3, yt * 16 + this.random.nextInt(10) + 3));
				Sound.monsterHurt.play();
				return true;
			}
		}

		if(this.til == 11 && item instanceof ToolItem) {
			tool = (ToolItem)item;
			if(tool.type == ToolType.axe && player.payStamina(4 - tool.level)) {
				level.setTile(xt, yt, Tile.plank, 0);
				level.add(new ItemEntity(new ResourceItem(Resource.wdoor), xt * 16 + this.random.nextInt(10) + 3, yt * 16 + this.random.nextInt(10) + 3));
				Sound.monsterHurt.play();
				return true;
			}

			if(tool.type == ToolType.hatchet && player.payStamina(4 - tool.level)) {
				level.setTile(xt, yt, Tile.plank, 0);
				level.add(new ItemEntity(new ResourceItem(Resource.wdoor), xt * 16 + this.random.nextInt(10) + 3, yt * 16 + this.random.nextInt(10) + 3));
				Sound.monsterHurt.play();
				return true;
			}
		}

		if(this.til == 12 && item instanceof ToolItem) {
			tool = (ToolItem)item;
			if(tool.type == ToolType.axe && player.payStamina(4 - tool.level)) {
				level.setTile(xt, yt, Tile.plank, 0);
				level.add(new ItemEntity(new ResourceItem(Resource.wdoor), xt * 16 + this.random.nextInt(10) + 3, yt * 16 + this.random.nextInt(10) + 3));
				Sound.monsterHurt.play();
				return true;
			}

			if(tool.type == ToolType.hatchet && player.payStamina(4 - tool.level)) {
				level.setTile(xt, yt, Tile.plank, 0);
				level.add(new ItemEntity(new ResourceItem(Resource.wdoor), xt * 16 + this.random.nextInt(10) + 3, yt * 16 + this.random.nextInt(10) + 3));
				Sound.monsterHurt.play();
				return true;
			}
		}

		byte hd;
		int hd1;
		if(this.til == 13 && item instanceof ToolItem) {
			tool = (ToolItem)item;
			hd = 3;
			if(tool.type == ToolType.pickaxe && player.payStamina(4 - tool.level)) {
				if(hd == 0) {
					level.setTile(xt, yt, Tile.sbrick, 0);
					level.add(new ItemEntity(new ResourceItem(Resource.sdoor), xt * 16 + this.random.nextInt(10) + 3, yt * 16 + this.random.nextInt(10) + 3));
					Sound.monsterHurt.play();
					return true;
				}

				if(hd != 0) {
					hd1 = hd - 1;
				}
			}

			if(tool.type == ToolType.pick && player.payStamina(4 - tool.level)) {
				level.setTile(xt, yt, Tile.sbrick, 0);
				level.add(new ItemEntity(new ResourceItem(Resource.sdoor), xt * 16 + this.random.nextInt(10) + 3, yt * 16 + this.random.nextInt(10) + 3));
				Sound.monsterHurt.play();
				return true;
			}
		}

		if(this.til == 14 && item instanceof ToolItem) {
			tool = (ToolItem)item;
			hd = 3;
			if(tool.type == ToolType.pickaxe && player.payStamina(4 - tool.level)) {
				if(hd == 0) {
					level.setTile(xt, yt, Tile.sbrick, 0);
					level.add(new ItemEntity(new ResourceItem(Resource.sdoor), xt * 16 + this.random.nextInt(10) + 3, yt * 16 + this.random.nextInt(10) + 3));
					Sound.monsterHurt.play();
					return true;
				}

				if(hd != 0) {
					hd1 = hd - 1;
				}
			}

			if(tool.type == ToolType.pick && player.payStamina(4 - tool.level)) {
				level.setTile(xt, yt, Tile.sbrick, 0);
				level.add(new ItemEntity(new ResourceItem(Resource.sdoor), xt * 16 + this.random.nextInt(10) + 3, yt * 16 + this.random.nextInt(10) + 3));
				Sound.monsterHurt.play();
				return true;
			}
		}

		if(this.til == 18 && item instanceof ToolItem) {
			tool = (ToolItem)item;
			if(tool.type == ToolType.shovel && player.payStamina(3 - tool.level)) {
				level.setTile(xt, yt, Tile.hole, 0);
				level.add(new ItemEntity(new ResourceItem(Resource.wool), xt * 16 + this.random.nextInt(10) + 3, yt * 16 + this.random.nextInt(10) + 3));
				Sound.monsterHurt.play();
				return true;
			}

			if(tool.type == ToolType.spade && player.payStamina(4 - tool.level)) {
				level.setTile(xt, yt, Tile.hole, 0);
				level.add(new ItemEntity(new ResourceItem(Resource.wool), xt * 16 + this.random.nextInt(10) + 3, yt * 16 + this.random.nextInt(10) + 3));
				Sound.monsterHurt.play();
				return true;
			}
		}

		if(this.til == 19 && item instanceof ToolItem) {
			tool = (ToolItem)item;
			if(tool.type == ToolType.shovel && player.payStamina(3 - tool.level)) {
				level.setTile(xt, yt, Tile.hole, 0);
				level.add(new ItemEntity(new ResourceItem(Resource.redwool), xt * 16 + this.random.nextInt(10) + 3, yt * 16 + this.random.nextInt(10) + 3));
				Sound.monsterHurt.play();
				return true;
			}

			if(tool.type == ToolType.spade && player.payStamina(4 - tool.level)) {
				level.setTile(xt, yt, Tile.hole, 0);
				level.add(new ItemEntity(new ResourceItem(Resource.redwool), xt * 16 + this.random.nextInt(10) + 3, yt * 16 + this.random.nextInt(10) + 3));
				Sound.monsterHurt.play();
				return true;
			}
		}

		if(this.til == 20 && item instanceof ToolItem) {
			tool = (ToolItem)item;
			if(tool.type == ToolType.shovel && player.payStamina(3 - tool.level)) {
				level.setTile(xt, yt, Tile.hole, 0);
				level.add(new ItemEntity(new ResourceItem(Resource.bluewool), xt * 16 + this.random.nextInt(10) + 3, yt * 16 + this.random.nextInt(10) + 3));
				Sound.monsterHurt.play();
				return true;
			}

			if(tool.type == ToolType.spade && player.payStamina(4 - tool.level)) {
				level.setTile(xt, yt, Tile.hole, 0);
				level.add(new ItemEntity(new ResourceItem(Resource.bluewool), xt * 16 + this.random.nextInt(10) + 3, yt * 16 + this.random.nextInt(10) + 3));
				Sound.monsterHurt.play();
				return true;
			}
		}

		if(this.til == 21 && item instanceof ToolItem) {
			tool = (ToolItem)item;
			if(tool.type == ToolType.shovel && player.payStamina(3 - tool.level)) {
				level.setTile(xt, yt, Tile.hole, 0);
				level.add(new ItemEntity(new ResourceItem(Resource.greenwool), xt * 16 + this.random.nextInt(10) + 3, yt * 16 + this.random.nextInt(10) + 3));
				Sound.monsterHurt.play();
				return true;
			}

			if(tool.type == ToolType.spade && player.payStamina(4 - tool.level)) {
				level.setTile(xt, yt, Tile.hole, 0);
				level.add(new ItemEntity(new ResourceItem(Resource.greenwool), xt * 16 + this.random.nextInt(10) + 3, yt * 16 + this.random.nextInt(10) + 3));
				Sound.monsterHurt.play();
				return true;
			}
		}

		if(this.til == 22 && item instanceof ToolItem) {
			tool = (ToolItem)item;
			if(tool.type == ToolType.shovel && player.payStamina(3 - tool.level)) {
				level.setTile(xt, yt, Tile.hole, 0);
				level.add(new ItemEntity(new ResourceItem(Resource.yellowwool), xt * 16 + this.random.nextInt(10) + 3, yt * 16 + this.random.nextInt(10) + 3));
				Sound.monsterHurt.play();
				return true;
			}

			if(tool.type == ToolType.spade && player.payStamina(4 - tool.level)) {
				level.setTile(xt, yt, Tile.hole, 0);
				level.add(new ItemEntity(new ResourceItem(Resource.yellowwool), xt * 16 + this.random.nextInt(10) + 3, yt * 16 + this.random.nextInt(10) + 3));
				Sound.monsterHurt.play();
				return true;
			}
		}

		if(this.til == 23 && item instanceof ToolItem) {
			tool = (ToolItem)item;
			if(tool.type == ToolType.shovel && player.payStamina(3 - tool.level)) {
				level.setTile(xt, yt, Tile.hole, 0);
				level.add(new ItemEntity(new ResourceItem(Resource.blackwool), xt * 16 + this.random.nextInt(10) + 3, yt * 16 + this.random.nextInt(10) + 3));
				Sound.monsterHurt.play();
				return true;
			}

			if(tool.type == ToolType.spade && player.payStamina(4 - tool.level)) {
				level.setTile(xt, yt, Tile.hole, 0);
				level.add(new ItemEntity(new ResourceItem(Resource.blackwool), xt * 16 + this.random.nextInt(10) + 3, yt * 16 + this.random.nextInt(10) + 3));
				Sound.monsterHurt.play();
				return true;
			}
		}

		return false;
	}
}
