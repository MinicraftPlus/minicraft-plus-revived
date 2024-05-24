package minicraft.entity.mob;

import minicraft.core.Game;
import minicraft.core.io.Settings;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.ExplosionTileTicker;
import minicraft.entity.furniture.Spawner;
import minicraft.gfx.Point;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.item.Items;
import minicraft.level.tile.Tiles;

import java.util.ArrayList;
import java.util.List;

public class Creeper extends EnemyMob {
	private static LinkedSprite[][][] sprites = new LinkedSprite[][][] {
		new LinkedSprite[][] { Mob.compileSpriteList(0, 0, 2, 2, 0, 2, "creeper") },
		new LinkedSprite[][] { Mob.compileSpriteList(0, 2, 2, 2, 0, 2, "creeper") },
		new LinkedSprite[][] { Mob.compileSpriteList(0, 4, 2, 2, 0, 2, "creeper") },
		new LinkedSprite[][] { Mob.compileSpriteList(0, 6, 2, 2, 0, 2, "creeper") }
	};

	private static final int MAX_FUSE_TIME = 60;
	private static final int TRIGGER_RADIUS = 64;
	private static final int BLAST_DAMAGE = 50;

	private int fuseTime = 0;
	private boolean fuseLit = false;

	public Creeper(int lvl) {
		super(lvl, sprites, 10, 50);
	}

	@Override
	public boolean move(int xd, int yd) {
		boolean result = super.move(xd, yd);
		dir = Direction.DOWN;
		if (xd == 0 && yd == 0) walkDist = 0;
		return result;
	}

	@Override
	public void tick() {
		super.tick();

		if (Game.isMode("minicraft.settings.mode.creative"))
			return; // Creeper should not explode if player is in creative mode

		if (fuseTime > 0) {
			fuseTime--; // Fuse getting shorter...
			xmov = ymov = 0;
		} else if (fuseLit) { // fuseLit is set to true when fuseTime is set to max, so this happens after fuseTime hits zero, while fuse is lit.
			xmov = ymov = 0;

			boolean playerInRange = false; // Tells if any players are within the blast

			// Find if the player is in range and store it in playerInRange.
			for (Entity e : level.getEntitiesOfClass(Mob.class)) {
				Mob mob = (Mob) e;
				int pdx = Math.abs(mob.x - x);
				int pdy = Math.abs(mob.y - y);
				if (pdx < TRIGGER_RADIUS && pdy < TRIGGER_RADIUS) {
					if (mob instanceof Player) {
						playerInRange = true;
					}
				}
			}

			// Handles what happens when it blows up.
			// It will only blow up if there are any players nearby.
			if (playerInRange) {
				// Play explosion sound
				Sound.play("explode");

				// Figure out which tile the mob died on
				int xt = x >> 4;
				int yt = (y - 2) >> 4;

				// Used for calculations
				int radius = lvl;

				// The total amount of damage we want to apply.
				int lvlDamage = BLAST_DAMAGE * lvl;

				// Hurt all the entities
				List<Entity> entitiesInRange = level.getEntitiesInTiles(xt, yt, radius);
				List<Entity> spawners = new ArrayList<>();
				Point[] tilePositions = level.getAreaTilePositions(xt, yt, radius);

				for (Entity entity : entitiesInRange) { // Hurts entities in range
					if (entity instanceof Mob) {
						Mob mob = (Mob) entity;
						int distx = Math.abs(mob.x - x);
						int disty = Math.abs(mob.y - y);
						float distDiag = (float) Math.sqrt(distx * distx + disty * disty);
						mob.hurt(this, (int) (lvlDamage * (1 / (distDiag + 1)) + Settings.getIdx("diff")));
					} else if (entity instanceof Spawner) {
						spawners.add(entity);
					}

					if (entity == this) continue;
					Point ePos = new Point(entity.x >> 4, entity.y >> 4);
					for (Point p : tilePositions) {
						if (!p.equals(ePos)) continue;
						if (!level.getTile(p.x, p.y).mayPass(level, p.x, p.y, entity))
							entity.die();
					}
				}
				for (Point tilePosition : tilePositions) { // Destroys tiles in range
					boolean hasSpawner = false;
					for (Entity spawner : spawners) {
						if (spawner.x >> 4 == tilePosition.x && spawner.y >> 4 == tilePosition.y) { // Check if current tile has a spawner on it
							hasSpawner = true;
							break;
						}
					}
					if (!hasSpawner) {
						ExplosionTileTicker.addTicker(level, tilePosition.x, tilePosition.y, 0);
					}
				}

				die(); // Dying now kind of kills everything. the super class will take care of it.
			} else {
				// If there aren't any players it will defuse itself and won't blow up.
				fuseTime = 0;
				fuseLit = false;
			}
		}
	}

	@Override
	public void render(Screen screen) {
		/*if (fuseLit && fuseTime % 6 == 0) {
			super.lvlcols[lvl-1] = Color.get(-1, 252);
		}
		else
			super.lvlcols[lvl-1] = Creeper.lvlcols[lvl-1];

		sprites[0] = walkDist == 0 ? standing : walking;*/

		super.render(screen);
	}

	@Override
	protected void touchedBy(Entity entity) {
		if (Game.isMode("minicraft.settings.mode.creative")) return;

		if (entity instanceof Player) {
			if (fuseTime == 0 && !fuseLit) {
				Sound.play("fuse");
				fuseTime = MAX_FUSE_TIME;
				fuseLit = true;
			}
			((Player) entity).hurt(this, 1);
		}
	}

	public boolean canWool() {
		return false;
	}

	public void die() {
		// Only drop items if the creeper has not exploded
		if (!fuseLit) dropItem(1, 4 - Settings.getIdx("diff"), Items.get("Gunpowder"));
		super.die();
	}
}
