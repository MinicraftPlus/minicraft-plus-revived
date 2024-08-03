package minicraft.entity.furniture;

import minicraft.core.Game;
import minicraft.core.Updater;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.mob.Cow;
import minicraft.entity.mob.Creeper;
import minicraft.entity.mob.EnemyMob;
import minicraft.entity.mob.Knight;
import minicraft.entity.mob.MobAi;
import minicraft.entity.mob.Pig;
import minicraft.entity.mob.Player;
import minicraft.entity.mob.Sheep;
import minicraft.entity.mob.Skeleton;
import minicraft.entity.mob.Slime;
import minicraft.entity.mob.Snake;
import minicraft.entity.mob.Zombie;
import minicraft.entity.particle.FireParticle;
import minicraft.entity.particle.TextParticle;
import minicraft.gfx.Color;
import minicraft.gfx.Point;
import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.item.FurnitureItem;
import minicraft.item.Item;
import minicraft.item.PotionType;
import minicraft.item.PowerGloveItem;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.Random;

public class Spawner extends Furniture {

	private final Random rnd = new Random();

	private static final int ACTIVE_RADIUS = 8 << 4;
	private static final int minSpawnInterval = 200, maxSpawnInterval = 500;
	private static final int minMobSpawnChance = 10; // 1 in minMobSpawnChance chance of calling trySpawn every interval.

	public MobAi mob;
	private int health, lvl, maxMobLevel;
	private int spawnTick;

	/**
	 * Initializes the spawners variables to the corresponding values from the mob.
	 * @param m The mob which this spawner will spawn.
	 */
	private void initMob(MobAi m) {
		mob = m;
		sprite.setColor(col = mob.col);

		if (m instanceof EnemyMob) {
			lvl = ((EnemyMob) mob).lvl;
			maxMobLevel = mob.getMaxLevel();
		} else {
			lvl = 1;
			maxMobLevel = 1;
		}

		if (lvl > maxMobLevel) {
			lvl = maxMobLevel;
		}
	}

	/**
	 * Creates a new spawner for the mob m.
	 * @param m Mob which will be spawned.
	 */
	public Spawner(MobAi m) {
		super(getClassName(m.getClass()) + " Spawner", new LinkedSprite(SpriteType.Entity, "spawner"), m instanceof Cow ? new LinkedSprite(SpriteType.Item, "cow_spawner") :
			m instanceof Pig ? new LinkedSprite(SpriteType.Item, "pig_spawner") :
				m instanceof Sheep ? new LinkedSprite(SpriteType.Item, "sheep_spawner") :
					m instanceof Slime ? new LinkedSprite(SpriteType.Item, "slime_spawner") :
						m instanceof Zombie ? new LinkedSprite(SpriteType.Item, "zombie_spawner") :
							m instanceof Creeper ? new LinkedSprite(SpriteType.Item, "creeper_spawner") :
								m instanceof Skeleton ? new LinkedSprite(SpriteType.Item, "skeleton_spawner") :
									m instanceof Snake ? new LinkedSprite(SpriteType.Item, "snake_spawner") :
										m instanceof Knight ? new LinkedSprite(SpriteType.Item, "knight_spawner") :
											new LinkedSprite(SpriteType.Item, "air_wizard_spawner"), 7, 2);
		health = 100;
		initMob(m);
		resetSpawnInterval();
	}

	/**
	 * Returns the classname of a class.
	 * @param c The class.
	 * @return String representation of the classname.
	 */
	private static String getClassName(Class<?> c) {
		String fullName = c.getCanonicalName();
		return fullName.substring(fullName.lastIndexOf(".") + 1);
	}

	@Override
	public void tick() {
		super.tick();

		spawnTick--;
		if (spawnTick <= 0) {
			int chance = (int) (minMobSpawnChance * Math.pow(level.mobCount, 2) / Math.pow(level.maxMobCount, 2)); // This forms a quadratic function that determines the mob spawn chance.
			if (chance <= 0 || random.nextInt(chance) == 0)
				trySpawn();
			resetSpawnInterval();
		}
	}

	/**
	 * Resets the spawner so it can spawn another mob.
	 */
	private void resetSpawnInterval() {
		spawnTick = rnd.nextInt(maxSpawnInterval - minSpawnInterval + 1) + minSpawnInterval;
	}

	/**
	 * Tries to spawn a new mob.
	 */
	private void trySpawn() {
		if (level == null) return;
		if (level.mobCount >= level.maxMobCount) return; // Can't spawn more entities
		if (mob instanceof EnemyMob) {
			if (level.depth >= 0 && Updater.tickCount > Updater.sleepEndTime && Updater.tickCount < Updater.sleepStartTime)
				return; // Do not spawn if it is on the surface or above and it is under daylight.
			if (level.isLight(x >> 4, y >> 4))
				return;
		}

		Player player = getClosestPlayer();
		if (player == null) return;
		int xd = player.x - x;
		int yd = player.y - y;

		if (xd * xd + yd * yd > ACTIVE_RADIUS * ACTIVE_RADIUS) return;

		MobAi newmob;
		try {
			if (mob instanceof EnemyMob)
				newmob = mob.getClass().getConstructor(int.class).newInstance(lvl);
			else
				newmob = mob.getClass().getDeclaredConstructor().newInstance();
		} catch (Exception ex) {
			Logger.tag("Spawner").error("Spawner ERROR: could not spawn mob; error initializing mob instance:");
			ex.printStackTrace();
			return;
		}

		Point pos = new Point(x >> 4, y >> 4);
		Point[] areaPositions = level.getAreaTilePositions(pos.x, pos.y, 1);
		ArrayList<Point> validPositions = new ArrayList<>();
		for (Point p : areaPositions)
			if (!(!level.getTile(p.x, p.y).mayPass(level, p.x, p.y, newmob) || mob instanceof EnemyMob && level.getTile(p.x, p.y).getLightRadius(level, p.x, p.y) > 0))
				validPositions.add(p);

		if (validPositions.size() == 0) return; // Cannot spawn mob.

		Point spawnPos = validPositions.get(random.nextInt(validPositions.size()));

		newmob.x = spawnPos.x << 4;
		newmob.y = spawnPos.y << 4;

		level.add(newmob);
		Sound.play("monsterhurt");
		for (int i = 0; i < 6; i++) {
			int randX = rnd.nextInt(16);
			int randY = rnd.nextInt(12);
			level.add(new FireParticle(x - 8 + randX, y - 6 + randY));
		}
	}

	@Override
	public boolean interact(Player player, Item item, Direction attackDir) {
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;

			Sound.play("monsterhurt");

			int dmg;
			if (Game.isMode("minicraft.settings.mode.creative"))
				dmg = health;
			else {
				dmg = tool.level + random.nextInt(2);

				if (tool.type == ToolType.Pickaxe)
					dmg += random.nextInt(5) + 2;

				if (player.potioneffects.containsKey(PotionType.Haste))
					dmg *= 2;
			}

			health -= dmg;
			level.add(new TextParticle("" + dmg, x, y, Color.get(-1, 200, 300, 400)));
			if (health <= 0) {
				level.remove(this);
				Sound.play("death");
				player.addScore(500);
			}

			return true;
		}

		if (item instanceof PowerGloveItem && Game.isMode("minicraft.settings.mode.creative")) {
			level.remove(this);
			if (!(player.activeItem instanceof PowerGloveItem))
				player.getLevel().dropItem(player.x, player.y, player.activeItem);
			player.activeItem = new FurnitureItem(this);
			return true;
		}

		if (item == null) return use(player);

		return false;
	}

	@Override
	public boolean use(Player player) {
		if (Game.isMode("minicraft.settings.mode.creative") && mob instanceof EnemyMob) {
			lvl++;
			if (lvl > maxMobLevel) lvl = 1;
			try {
				EnemyMob newmob = (EnemyMob) mob.getClass().getConstructor(int.class).newInstance(lvl);
				initMob(newmob);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return true;
		}

		return false;
	}

	@Override
	public @NotNull Furniture copy() {
		return new Spawner(mob);
	}
}
