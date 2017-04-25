package com.mojang.ld22.entity;

import com.mojang.ld22.Game;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.gfx.MobSprite;
import com.mojang.ld22.item.ResourceItem;
import com.mojang.ld22.item.resource.Resource;
import com.mojang.ld22.level.tile.Tile;
import com.mojang.ld22.screen.ModeMenu;
import com.mojang.ld22.screen.OptionsMenu;
import com.mojang.ld22.sound.Sound;

public class Creeper extends EnemyMob {
	private static final MobSprite[][] sprites;
	private static final MobSprite[] walking, standing;
	static {
		MobSprite[] list = MobSprite.compileSpriteList(4, 18, 2, 2, 0, 3);
		walking = new MobSprite[] {list[1], list[2]};
		standing = new MobSprite[] {list[0], list[0]};
		sprites = new MobSprite[1][2];
		sprites[0] = standing;
	}
	
	private static final int MAX_FUSE_TIME = 60;
	private static final int BLAST_RADIUS = 60;
	private static final int BLAST_DAMAGE = 10;
	
	private int fuseTime = 0;
	private boolean fuseLit = false;
	
	public Creeper(int lvl) {
		super(lvl, sprites, 10, 50);
		
		this.col0 = Color.get(-1, 10, 50, 40);
		this.col1 = Color.get(-1, 20, 50, 40);
		this.col2 = Color.get(-1, 10, 50, 30);
		this.col3 = Color.get(-1, 0, 50, 30);
		this.col4 = Color.get(-1, 20, 50, 30);
	}
	
	public boolean move(int xa, int ya) {
		boolean result = super.move(xa, ya);
		dir = 0;
		if (xa == 0 && ya == 0) walkDist = 0;
		return result;
	}
	
	public void tick() {
		super.tick();
		
		if (fuseTime > 0) {
			fuseTime--; // fuse getting shorter...
			xa = ya = 0;
		} else if (fuseLit) { // fuseLit is set to true when fuseTime is set to max, so this happens after fuseTime hits zero, while fuse is lit.
			// blow up
			xa = ya = 0;
			int pdx = Math.abs(level.player.x - x);
			int pdy = Math.abs(level.player.y - y);
			if (pdx < BLAST_RADIUS && pdy < BLAST_RADIUS) {
				float pd = (float) Math.sqrt(pdx * pdx + pdy * pdy);
				int dmg = (int) (BLAST_DAMAGE * (1 - (pd / BLAST_RADIUS))) + OptionsMenu.diff;
				level.player.hurt(this, dmg, 0);
				level.player.payStamina(dmg * (OptionsMenu.diff == OptionsMenu.easy?1:2));
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

				die(); // dying now kind of kills everything. the super class will take care of it.
			} else {
				fuseTime = 0;
				fuseLit = false;
			}
		}
		
		//}
		/*
		if (OptionsMenu.diff == OptionsMenu.norm) {

			if (fuseTime == 0) {
				if (!fuseLit) {
					super.tick();
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
			// fuse time goes down twice, once where fuse is not lit, and once where it is, before it blows up.
			if (fuseTime == 0) {
				if (!fuseLit) {
					super.tick();
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

						die();
					} else {
						fuseTime = 0;
						fuseLit = false;
					}
				}
			} else {
				fuseTime--;
			}
		}*/
	}

	public void render(Screen screen) {
		/*int xt = 4;
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
		*/
		col0 = Color.get(-1, 10, 30, 20);
		col1 = Color.get(-1, 20, 40, 30);
		col2 = Color.get(-1, 10, 30, 20);
		col3 = Color.get(-1, 0, 20, 10);
		col4 = Color.get(-1, 20, 40, 30);
		
		if (isLight()) col0 = col1 = col2 = col3 = col4;
		
		
		if (fuseLit && fuseTime % 6 == 0) {
			col = Color.get(-1, 252);
		}
		else if (level.dirtColor == 322) {
			
			if (lvl == 2) col = Color.get(-1, 200, 262, 232);
			if (lvl == 3) col = Color.get(-1, 200, 272, 222);
			if (lvl == 4) col = Color.get(-1, 200, 292, 282);
			
			if (Game.time == 0) col = col0;
			if (Game.time == 1) col = col1;
			if (Game.time == 2) col = col2;
			if (Game.time == 3) col = col3;
		}
		else col = col4;
		
		this.sprites[0] = walkDist == 0 ? standing : walking;
		
		super.render(screen);
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
		dropResource(1, 4-OptionsMenu.diff, Resource.gunp);
		super.die();
	}
}
