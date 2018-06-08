package minicraft.entity.mob;

import java.util.ArrayList;
import java.util.List;

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
	private static final int BLAST_RADIUS = 60;
	private static final int BLAST_DAMAGE = 10;
	
	private int fuseTime = 0;
	private boolean fuseLit = false;
	
	public Creeper(int lvl) { super(lvl, sprites, lvlcols, 10, 50); }
	
	public boolean move(int xa, int ya) {
		boolean result = super.move(xa, ya);
		dir = Direction.DOWN;
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
			
			boolean hurtOne = false; // tells if any players were hurt
			
			for(Entity e: level.getEntitiesOfClass(Mob.class)) {
				Mob mob = (Mob) e;
				int pdx = Math.abs(mob.x - x);
				int pdy = Math.abs(mob.y - y);
				if(pdx < BLAST_RADIUS && pdy < BLAST_RADIUS) {
					float pd = (float) Math.sqrt(pdx * pdx + pdy * pdy);
					int dmg = (int) (BLAST_DAMAGE * (1 - (pd / BLAST_RADIUS))) + Settings.getIdx("diff");
					mob.hurt(this, dmg);
					if(mob instanceof Player) {
						((Player) mob).payStamina(dmg * (Settings.get("diff").equals("Easy") ? 1 : 2));
						hurtOne = true;
					}
				}
			}
			
			if (hurtOne) {
				
				Sound.explode.play();
				
				// figure out which tile the mob died on
				int xt = x >> 4;
				int yt = (y - 2) >> 4;
				
				// change tile to an appropriate crater
				
				// basically, this sets all tiles within a certain radius to a hole, unless they have a Spawner on them or stairs (stairs check happens in Level class). All entities on the reset tiles which are not allowed to occupy a hole tile are then removed (or killed, in the case of mobs).
				
				int radius = lvl*2/3;
				List<Entity> entitiesInRange = level.getEntitiesInTiles(xt, yt, radius);
				Point[] tilePositions = level.getAreaTilePositions(xt, yt, radius);
				
				ArrayList<Entity> skipEntities = new ArrayList<>();
				for(Entity e: entitiesInRange)
					if(e instanceof Spawner)
						skipEntities.add(e);
				
				if(skipEntities.size() == 0)
					level.setAreaTiles(xt, yt, radius, Tiles.get("hole"), 0);
				else {
					for(Point pos : tilePositions) {
						boolean match = false;
						for(Entity e: skipEntities) {
							if(e.x>>4 == pos.x && e.y>>4 == pos.y) {
								match = true;
								break;
							}
						}
						if(!match)
							level.setAreaTiles(pos.x, pos.y, 0, Tiles.get("hole"), 0);
					}
				}
				
				for(Entity e : entitiesInRange) {
					if(e == this) continue;
					Point ePos = new Point(e.x>>4, e.y>>4);
					
					for(Point p: tilePositions) {
						if(!p.equals(ePos)) continue;
						
						if(!level.getTile(p.x, p.y).mayPass(level, p.x, p.y, e)) {
							if(e instanceof Mob)
								((Mob) e).kill();
							else
								e.remove();
						}
					}
				}
				
				die(); // dying now kind of kills everything. the super class will take care of it.
			} else {
				fuseTime = 0;
				fuseLit = false;
			}
		}
	}

	public void render(Screen screen) {
		if (fuseLit && fuseTime % 6 == 0) {
			super.lvlcols[lvl-1] = Color.get(-1, 252);
		}
		else
			super.lvlcols[lvl-1] = Creeper.lvlcols[lvl-1];
		
		sprites[0] = walkDist == 0 ? standing : walking;
		
		super.render(screen);
	}

	protected void touchedBy(Entity entity) {
		if (entity instanceof Player) {
			if (fuseTime == 0) {
				Sound.fuse.play();
				fuseTime = MAX_FUSE_TIME;
				fuseLit = true;
			}
			((Player)entity).hurt(this, 1);
		}
	}
	
	public boolean canWool() { return false; }
	
	protected void die() {
		dropItem(1, 4-Settings.getIdx("diff"), Items.get("Gunpowder"));
		super.die();
	}
	
	protected String getUpdateString() {
		String updates = super.getUpdateString() + ";";
		updates += "fuseTime,"+fuseTime+
		";fuseLit,"+fuseLit;
		
		return updates;
	}
	
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
