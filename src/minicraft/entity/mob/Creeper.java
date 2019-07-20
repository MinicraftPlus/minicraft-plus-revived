package minicraft.entity.mob;

import java.util.*;

import minicraft.core.io.Settings;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.furniture.Spawner;
import minicraft.gfx.Color;
import minicraft.gfx.MobSprite;
import minicraft.gfx.Point;
import minicraft.gfx.Screen;
import minicraft.item.Items;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;
import org.jetbrains.annotations.NotNull;

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
	private static int[] lvlcols = {
		Color.get(-1, 20, 40, 30),
		Color.get(-1, 200, 262, 232),
		Color.get(-1, 200, 272, 222),
		Color.get(-1, 200, 292, 282)
	};
	
	private static final int MAX_FUSE_TIME = 60;
	private static final int TRIGGER_RADIUS = 60;
	private static final int BLAST_DAMAGE = 100;
	
	private int fuseTime = 0;
	private boolean fuseLit = false;
	
	public Creeper(int lvl) { super(lvl, sprites, lvlcols, 10, 50); }
	
	@Override
	public boolean move(int xa, int ya) {
		boolean result = super.move(xa, ya);
		dir = Direction.DOWN;
		if (xa == 0 && ya == 0) walkDist = 0;
		return result;
	}
	
	@Override
	public void tick() {
		super.tick();
		
		if (fuseTime > 0) {
			fuseTime--; // fuse getting shorter...
			xa = ya = 0;
		} else if (fuseLit) { // fuseLit is set to true when fuseTime is set to max, so this happens after fuseTime hits zero, while fuse is lit.
			xa = ya = 0;
			
			boolean playerInRange = false; // tells if any players are within the blast
			
			for(Entity e: level.getEntitiesOfClass(Mob.class)) {
				Mob mob = (Mob) e;
				int pdx = Math.abs(mob.x - x);
				int pdy = Math.abs(mob.y - y);
				if(pdx < TRIGGER_RADIUS && pdy < TRIGGER_RADIUS) {
					if (mob instanceof Player) {
						playerInRange = true;
					}
				}
			}

			// basically, if there aren't any players it "defuses" itself and doesn't blow up
			if (playerInRange) {
				// blow up
				
				Sound.explode.play();
				
				// figure out which tile the mob died on
				int xt = x >> 4;
				int yt = (y - 2) >> 4;

				// hurt all the entities
				int radius = lvl*2/3;
				List<Entity> entitiesInRange = level.getEntitiesInTiles(xt, yt, radius);
				List<Entity> spawners = new ArrayList<>();

				for (Entity entity : entitiesInRange) {
					if (entity instanceof Mob) {
						Mob mob = (Mob) entity;
						int distx = Math.abs(mob.x - x);
						int disty = Math.abs(mob.y - y);
						float distDiag = (float) Math.sqrt(distx * distx + disty * disty);
						mob.hurt(this, (int) (BLAST_DAMAGE * (1 - (distDiag / TRIGGER_RADIUS))) + Settings.getIdx("diff"));
					} else if (entity instanceof Spawner) {
						spawners.add(entity);
					}
				}

				Point[] tilePositions = level.getAreaTilePositions(xt, yt, radius);
				for (int p = 0; p < tilePositions.length; p++) {
					boolean hasSpawner = false;
					for (Entity spawner : spawners) {
						if (spawner.x >> 4 == tilePositions[p].x && spawner.y >> 4 == tilePositions[p].y) {
							hasSpawner = true;
							break;
						}
					}
					if (!hasSpawner) {
						if (level.depth != 1) {
							level.setAreaTiles(tilePositions[p].x, tilePositions[p].y, 0, Tiles.get("hole"), 0);
						} else {
							level.setAreaTiles(tilePositions[p].x, tilePositions[p].y, 0, Tiles.get("Infinite Fall"), 0);
						}

					}
				}

				for (Entity entity : entitiesInRange) {
					if (entity == this) continue;
					Point ePos = new Point(entity.x>>4, entity.y>>4);
					for(Point p: tilePositions) {
						if (!p.equals(ePos)) continue;
						if (!level.getTile(p.x, p.y).mayPass(level, p.x, p.y, entity))
							entity.die();
					}
				}
				
				die(); // dying now kind of kills everything. the super class will take care of it.
			} else {
				fuseTime = 0;
				fuseLit = false;
			}
		}
	}

	@Override
	public void render(Screen screen) {
		if (fuseLit && fuseTime % 6 == 0) {
			super.lvlcols[lvl-1] = Color.get(-1, 252);
		}
		else
			super.lvlcols[lvl-1] = Creeper.lvlcols[lvl-1];
		
		sprites[0] = walkDist == 0 ? standing : walking;
		
		super.render(screen);
	}

	@Override
	protected void touchedBy(Entity entity) {
		if (entity instanceof Player) {
			if (fuseTime == 0 && !fuseLit) {
				Sound.fuse.play();
				fuseTime = MAX_FUSE_TIME;
				fuseLit = true;
			}
			((Player)entity).hurt(this, 1);
		}
	}
	
	public boolean canWool() { return false; }
	
	public void die() {
		dropItem(1, 4-Settings.getIdx("diff"), Items.get("Gunpowder"));
		super.die();
	}
	
	@Override
	protected String getUpdateString() {
		String updates = super.getUpdateString() + ";";
		updates += "fuseTime,"+fuseTime+
		";fuseLit,"+fuseLit;
		
		return updates;
	}
	
	@Override
	protected boolean updateField(String field, String val) {
		if(super.updateField(field, val)) return true;
		switch(field) {
			case "fuseTime":
				fuseTime = Integer.parseInt(val);
				return true;
			
			case "fuseLit":
			 	boolean wasLit = fuseLit;
				fuseLit = Boolean.parseBoolean(val);
				if(fuseLit && !wasLit)
					Sound.fuse.play();
		}
		
		return false;
	}
}
