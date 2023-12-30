package minicraft.level;

import minicraft.core.Game;
import minicraft.core.Updater;
import minicraft.core.io.Localization;
import minicraft.core.io.Settings;
import minicraft.entity.Entity;
import minicraft.entity.ItemEntity;
import minicraft.entity.furniture.Chest;
import minicraft.entity.furniture.DungeonChest;
import minicraft.entity.furniture.Lantern;
import minicraft.entity.furniture.Spawner;
import minicraft.entity.mob.AirWizard;
import minicraft.entity.mob.Cow;
import minicraft.entity.mob.Creeper;
import minicraft.entity.mob.EnemyMob;
import minicraft.entity.mob.Knight;
import minicraft.entity.mob.Mob;
import minicraft.entity.mob.MobAi;
import minicraft.entity.mob.PassiveMob;
import minicraft.entity.mob.Pig;
import minicraft.entity.mob.Player;
import minicraft.entity.mob.Sheep;
import minicraft.entity.mob.Skeleton;
import minicraft.entity.mob.Slime;
import minicraft.entity.mob.Snake;
import minicraft.entity.mob.Zombie;
import minicraft.gfx.Point;
import minicraft.gfx.Rectangle;
import minicraft.gfx.Screen;
import minicraft.item.Item;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;
import minicraft.level.tile.TorchTile;
import minicraft.level.tile.TreeTile;
import minicraft.util.Logging;
import minicraft.util.MyUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

public class Level {
	private final Random random;

	private static final String[] levelNames = {"Sky", "Surface", "Iron", "Gold", "Lava", "Dungeon"};

	public static String getLevelName(int depth) {
		return levelNames[-1 * depth + 1];
	}

	public static String getDepthString(int depth) {
		return Localization.getLocalized("minicraft.displays.loading.message.level", depth < 0 ? "B" + (-depth) : depth);
	}

	private static final int MOB_SPAWN_FACTOR = 100; // The chance of a mob actually trying to spawn when trySpawn is called equals: mobCount / maxMobCount * MOB_SPAWN_FACTOR. so, it basically equals the chance, 1/number, of a mob spawning when the mob cap is reached. I hope that makes sense...

	public int w, h; // Width and height of the level
	private final long seed; // The used seed that was used to generate the world

	public short[] tiles; // An array of all the tiles in the world.
	public short[] data; // An array of the data of the tiles in the world.

	public final TreeTile.TreeType[] treeTypes; // An array of tree types

	public final int depth; // Depth level of the level
	public int monsterDensity = 16; // Affects the number of monsters that are on the level, bigger the number the less monsters spawn.
	public int maxMobCount;
	public int chestCount;
	public int mobCount = 0;

	private final Object entityLock = new Object(); // I will be using this lock to avoid concurrency exceptions in entities and sparks set
	private final Set<Entity> entities = java.util.Collections.synchronizedSet(new HashSet<>()); // A list of all the entities in the world
	private final Set<Player> players = java.util.Collections.synchronizedSet(new HashSet<>()); // A list of all the players in the world
	private final List<Entity> entitiesToAdd = new ArrayList<>(); /// entities that will be added to the level on next tick are stored here. This is for the sake of multithreading optimization. (hopefully)
	private final List<Entity> entitiesToRemove = new ArrayList<>(); /// entities that will be removed from the level on next tick are stored here. This is for the sake of multithreading optimization. (hopefully)

	// Creates a sorter for all the entities to be rendered.
	//private static Comparator<Entity> spriteSorter = Comparator.comparingInt(e -> e.y); // Broken
	@SuppressWarnings("Convert2Lambda")
	private static Comparator<Entity> spriteSorter = Comparator.comparingInt(new ToIntFunction<Entity>() {
		@Override
		public int applyAsInt(Entity e) {
			return e.y;
		}
	});

	public Entity[] getEntitiesToSave() {
		Entity[] allEntities = new Entity[entities.size() + entitiesToAdd.size()];
		Entity[] toAdd = entitiesToAdd.toArray(new Entity[0]);
		Entity[] current = getEntityArray();
		System.arraycopy(current, 0, allEntities, 0, current.length);
		System.arraycopy(toAdd, 0, allEntities, current.length, toAdd.length);

		return allEntities;
	}

	// This is a solely a debug method I made, to make printing repetitive stuff easier.
	// Should be changed to accept prepend and entity, or a tile (as an Object). It will get the coordinates and class name from the object, and will divide coords by 16 if passed an entity.
	public void printLevelLoc(String prefix, int x, int y) {
		printLevelLoc(prefix, x, y, "");
	}

	public void printLevelLoc(String prefix, int x, int y, String suffix) {
		String levelName = getLevelName(depth);

		Logging.WORLDNAMED.info("{} on {} level ({}, {}){}", prefix, levelName, x, y, suffix);
	}

	public void printTileLocs(Tile t) {
		for (int x = 0; x < w; x++)
			for (int y = 0; y < h; y++)
				if (getTile(x, y).id == t.id)
					printLevelLoc(t.name, x, y);
	}

	public void printEntityLocs(Class<? extends Entity> c) {
		int numfound = 0;
		for (Entity entity : getEntityArray()) {
			if (c.isAssignableFrom(entity.getClass())) {
				printLevelLoc(entity.toString(), entity.x >> 4, entity.y >> 4);
				numfound++;
			}
		}

		Logging.WORLDNAMED.info("Found " + numfound + " entities in level of depth " + depth);
	}

	private void updateMobCap() {
		maxMobCount = 150 + 150 * Settings.getIdx("diff");
		if (depth == 1) maxMobCount /= 2;
		if (depth == 0 || depth == -4 || depth == -5) maxMobCount = maxMobCount * 2 / 3;
	}

	public Level(int w, int h, long seed, int level, Level parentLevel, boolean makeWorld) {
		depth = level;
		this.w = w;
		this.h = h;
		this.seed = seed;
		random = new Random(seed);
		short[][] maps; // Multidimensional array (an array within a array), used for the map

		treeTypes = new TreeTile.TreeType[w * h];
		{
			LevelGen noise1 = new LevelGen(w, h, 32);
			LevelGen noise2 = new LevelGen(w, h, 32);
			TreeTile.TreeType[] types = TreeTile.TreeType.values();
			for (int y = 0; y < h; y++) { // Loop through height
				for (int x = 0; x < w; x++) { // Loop through width
					// Randomly selecting a tree type.
					int i = x + y * w;
					double val = Math.abs(noise1.values[i] - noise2.values[i]) * 3 - 2;
					// This calculates a sort of distance based on the current coordinate.
					double xd = x / (w - 1.0) * 2 - 1;
					double yd = y / (h - 1.0) * 2 - 1;
					if (xd < 0) xd = -xd;
					if (yd < 0) yd = -yd;
					double dist = Math.max(xd, yd);
					dist = dist * dist * dist * dist;
					dist = dist * dist * dist * dist;
					val += 1 - dist * 20;
					val += 1.5; // Assuming the range of value is from 0 to 2.
					val *= types.length / 2.0;
					val += 1; // Incrementing index.
					// The original val mainly falls in small interval instead of averagely.
					val = 1.0 / (3 * types.length) * Math.pow(val - 5, 2); // Quadratically bloating the value.
					int idx = (int) Math.round(val - 1); // Decrementing index.
					treeTypes[x + y * w] = (idx >= types.length || idx < 0) ? TreeTile.TreeType.OAK // Oak by default.
						: types[idx];
				}
			}
		}

		if (level != -4 && level != 0)
			monsterDensity = 8;

		updateMobCap();

		if (!makeWorld) {
			int arrsize = w * h;
			tiles = new short[arrsize];
			data = new short[arrsize];
			return;
		}

		Logging.WORLD.debug("Making level " + level + "...");

		maps = LevelGen.createAndValidateMap(w, h, level, seed);
		if (maps == null) {
			Logging.WORLD.error("Level generation: Returned maps array is null");
			return;
		}

		tiles = maps[0]; // Assigns the tiles in the map
		data = maps[1]; // Assigns the data of the tiles


		if (level < 0)
			generateSpawnerStructures();

		if (level == 0)
			generateVillages();

		if (level == -4)
			generateDungeonStructures();

		if (parentLevel != null) { // If the level above this one is not null (aka, if this isn't a sky level)
			for (int y = 0; y < h; y++) { // Loop through height
				for (int x = 0; x < w; x++) { // Loop through width
					if (parentLevel.getTile(x, y) == Tiles.get("Stairs Down")) { // If the tile in the level above the current one is a stairs down then...
						if (level == -4) { /// Make the obsidian wall formation around the stair in the dungeon level
							Structure.dungeonGate.draw(this, x, y); // Te gate should not intersect with the boss room.
							Structure.dungeonBossRoom.draw(this, w / 2, h / 2); // Generating the boss room at the center.
						} else if (level == 0) { // Surface
							Logging.WORLD.trace("Setting tiles around " + x + "," + y + " to hard rock");
							setAreaTiles(x, y, 1, Tiles.get("Hard Rock"), 0); // surround the sky stairs with hard rock
						} else // Any other level, the up-stairs should have dirt on all sides.
							setAreaTiles(x, y, 1, Tiles.get("dirt"), 0);

						setTile(x, y, Tiles.get("Stairs Up")); // Set a stairs up tile in the same position on the current level
					}
				}
			}
		} else { // This is a sky level
			boolean placedHouse = false;
			while (!placedHouse) {

				int x = random.nextInt(this.w - 7);
				int y = random.nextInt(this.h - 5);

				if (this.getTile(x - 3, y - 2) == Tiles.get("Cloud") && this.getTile(x + 3, y - 2) == Tiles.get("Cloud")) {
					if (this.getTile(x - 3, y + 2) == Tiles.get("Cloud") && this.getTile(x + 3, y + 2) == Tiles.get("Cloud")) {
						Structure.airWizardHouse.draw(this, x, y);

						placedHouse = true;
					}
				}
			}
		}

		checkChestCount(false);

		checkAirWizard();

		if (Logging.logLevel) printTileLocs(Tiles.get("Stairs Down"));
	}

	public Level(int w, int h, int level, Level parentLevel, boolean makeWorld) {
		this(w, h, 0, level, parentLevel, makeWorld);
	}

	/**
	 * Level which the world is contained in
	 */
	public Level(int w, int h, int level, Level parentLevel) {
		this(w, h, level, parentLevel, true);
	}

	public long getSeed() {
		return seed;
	}

	public void checkAirWizard() {
		checkAirWizard(true);
	}

	private void checkAirWizard(boolean check) {
		if (depth == 1 && !AirWizard.beaten) { // Add the airwizard to the surface

			boolean found = false;
			if (check) {
				for (Entity e : entitiesToAdd)
					if (e instanceof AirWizard) {
						found = true;
						break;
					}
				for (Entity e : entities)
					if (e instanceof AirWizard) {
						found = true;
						break;
					}
			}

			if (!found) {
				AirWizard aw = new AirWizard();
				add(aw, w / 2, h / 2, true);
			}
		}
	}

	public void checkChestCount() {
		checkChestCount(true);
	}

	private void checkChestCount(boolean check) {
		// If the level is the dungeon, and we're not just loading the world...
		if (depth != -4) return;

		int numChests = 0;

		if (check) {
			for (Entity e : entitiesToAdd)
				if (e instanceof DungeonChest)
					numChests++;
			for (Entity e : entities)
				if (e instanceof DungeonChest)
					numChests++;
			Logging.WORLDNAMED.debug("Found " + numChests + " chests.");
		}

		/// Make DungeonChests!
		for (int i = numChests; i < 10 * (w / 128); i++) {
			DungeonChest d = new DungeonChest(true);
			boolean addedchest = false;
			while (!addedchest) { // Keep running until we successfully add a DungeonChest

				// Pick a random tile:
				int x2 = random.nextInt(16 * w) / 16;
				int y2 = random.nextInt(16 * h) / 16;
				if (getTile(x2, y2) == Tiles.get("Grass")) {
					boolean xaxis = random.nextBoolean();
					if (xaxis) {
						for (int s = x2; s < w - s; s++) {
							if (getTile(s, y2) == Tiles.get("Obsidian Wall") || getTile(s, y2) == Tiles.get("Ornate Obsidian")) {
								d.x = s * 20 - 16;
								d.y = y2 * 24 - 14;
							}
						}
					} else { // y axis
						for (int s = y2; s < h - s; s++) {
							if (getTile(x2, s) == Tiles.get("Obsidian Wall") || getTile(x2, s) == Tiles.get("Ornate Obsidian")) {
								d.x = x2 * 23 - 14;
								d.y = s * 21 - 16;
							}
						}
					}
					if (d.x == 0 && d.y == 0) {
						d.x = x2 * 16 - 8;
						d.y = y2 * 16 - 8;
					}

					add(d);
					chestCount++;
					addedchest = true;
				}
			}
		}
	}

	private void tickEntity(Entity entity) {
		if (entity == null) return;

		if (!entity.isRemoved()) entity.handleDespawn();
		if (entity.isRemoved()) {
			remove(entity);
			return;
		}

		if (entity != Game.player) { // Player is ticked separately, others are ticked on server
			entity.tick(); /// The main entity tick call.
		}

		if (entity.isRemoved() || entity.getLevel() != this) {
			remove(entity);
		}
	}

	public void tick(boolean fullTick) {
		int count = 0;

		while (entitiesToAdd.size() > 0) {
			Entity entity = entitiesToAdd.get(0);
			boolean inLevel = entities.contains(entity);

			if (!inLevel) {
				if (Logging.logLevel)
					printEntityStatus("Adding ", entity, "furniture.DungeonChest", "mob.AirWizard", "mob.Player");

				synchronized (entityLock) {
					entities.add(entity);
					if (entity instanceof Player) {
						players.add((Player) entity);
					}
				}
			}

			entitiesToAdd.remove(entity);
		}

		if (fullTick) {
			// This prevents any entity (or tile) tick action from happening on a server level with no players.

			for (int i = 0; i < w * h / 50; i++) {
				int xt = random.nextInt(w);
				int yt = random.nextInt(w);
				getTile(xt, yt).tick(this, xt, yt);
			}

			// Entity loop
			for (Entity e : entities) {
				tickEntity(e);
				if (e instanceof Mob) count++;
			}
		}

		while (entitiesToRemove.size() > 0) {
			Entity entity = entitiesToRemove.get(0);

			if (Logging.logLevel) printEntityStatus("Removing ", entity, "mob.Player");

			entity.remove(this); // This will safely fail if the entity's level doesn't match this one.
			synchronized (entityLock) {
				entities.remove(entity);
			}

			if (entity instanceof Player)
				players.remove(entity);
			entitiesToRemove.remove(entity);
		}

		mobCount = count;

		if (fullTick && count < maxMobCount)
			trySpawn();
	}

	public boolean entityNearPlayer(Entity entity) {
		for (Player player : players) {
			if (Math.abs(player.x - entity.x) < 128 && Math.abs(player.y - entity.y) < 76) {
				return true;
			}
		}
		return false;
	}

	public double distanceOfClosestPlayer(Entity entity) {
		double distance = Math.hypot(w, h);
		for (Player player : players) {
			double d = Math.hypot(Math.abs(entity.x - player.x), Math.abs(entity.y - player.y));
			if (d < distance) distance = d;
		}

		return distance;
	}

	public void printEntityStatus(String entityMessage, Entity entity, String... searching) {
		// "searching" can contain any number of class names I want to print when found.
		String clazz = entity.getClass().getCanonicalName();
		clazz = clazz.substring(clazz.lastIndexOf(".") + 1);
		for (String search : searching) {
			try {
				if (Class.forName("minicraft.entity." + search).isAssignableFrom(entity.getClass())) {
					printLevelLoc(entityMessage + clazz, entity.x >> 4, entity.y >> 4, ": " + entity);
					break;
				}
			} catch (ClassNotFoundException ex) {
				ex.printStackTrace();
			}
		}
	}

	public void dropItem(int x, int y, int mincount, int maxcount, Item... items) {
		dropItem(x, y, mincount + random.nextInt(maxcount - mincount + 1), items);
	}

	public void dropItem(int x, int y, int count, Item... items) {
		for (int i = 0; i < count; i++)
			dropItem(x, y, items);
	}

	public void dropItem(int x, int y, Item... items) {
		for (Item i : items)
			dropItem(x, y, i);
	}

	public ItemEntity dropItem(int x, int y, Item i) {
		int ranx, rany;

		do {
			ranx = x + random.nextInt(11) - 5;
			rany = y + random.nextInt(11) - 5;
		} while (ranx >> 4 != x >> 4 || rany >> 4 != y >> 4);
		ItemEntity ie = new ItemEntity(i, ranx, rany);
		add(ie);
		return ie;
	}

	public void renderBackground(Screen screen, int xScroll, int yScroll) {
		int xo = xScroll >> 4; // Latches to the nearest tile coordinate
		int yo = yScroll >> 4;
		int w = (Screen.w) >> 4; // There used to be a "+15" as in below method
		int h = (Screen.h) >> 4;
		screen.setOffset(xScroll, yScroll);
		for (int y = yo; y <= h + yo; y++) {
			for (int x = xo; x <= w + xo; x++) {
				getTile(x, y).render(screen, this, x, y);
			}
		}
		screen.setOffset(0, 0);
	}

	public void renderSprites(Screen screen, int xScroll, int yScroll) {
		int xo = xScroll >> 4; // Latches to the nearest tile coordinate
		int yo = yScroll >> 4;
		int w = (Screen.w + 15) >> 4;
		int h = (Screen.h + 15) >> 4;

		screen.setOffset(xScroll, yScroll);
		sortAndRender(screen, getEntitiesInTiles(xo - 1, yo - 1, xo + w + 1, yo + h + 1));

		screen.setOffset(0, 0);
	}

	public void renderLight(Screen screen, int xScroll, int yScroll, int brightness) {
		int xo = xScroll >> 4;
		int yo = yScroll >> 4;
		int w = (Screen.w + 15) >> 4;
		int h = (Screen.h + 15) >> 4;

		screen.setOffset(xScroll, yScroll);

		// this specifies the maximum radius that the game will stop rendering the light from the source object once off screen
		int r = 8;

		List<Entity> entities = getEntitiesInTiles(xo - r, yo - r, w + xo + r, h + yo + r);
		for (Entity e : entities) {
			int lr = e.getLightRadius();
			if (lr > 0) screen.renderLight(e.x - 1, e.y - 4, lr * brightness);
		}

		for (int y = yo - r; y <= h + yo + r; y++) {
			for (int x = xo - r; x <= w + xo + r; x++) {
				if (x < 0 || y < 0 || x >= this.w || y >= this.h) continue;

				int lr = getTile(x, y).getLightRadius(this, x, y);
				if (lr > 0) screen.renderLight(x * 16 + 8, y * 16 + 8, lr * brightness);
			}
		}
		screen.setOffset(0, 0);
	}

	private void sortAndRender(Screen screen, List<Entity> list) {
		list.sort(spriteSorter);
		for (Entity e : list) {
			if (e.getLevel() == this && !e.isRemoved())
				e.render(screen);
			else
				remove(e);
		}
	}

	public Tile getTile(int x, int y) {
		if (x < 0 || y < 0 || x >= w || y >= h /* || (x + y * w) >= tiles.length*/) return Tiles.get("connector tile");
		int id = tiles[x + y * w];
		if (id < 0) id += 256;
		return Tiles.get(id);
	}

	public void setTile(int x, int y, String tilewithdata) {
		if (!tilewithdata.contains("_")) {
			setTile(x, y, Tiles.get(tilewithdata));
			return;
		}
		String name = tilewithdata.substring(0, tilewithdata.indexOf("_"));
		int data = Tiles.get(name).getData(tilewithdata.substring(name.length() + 1));
		setTile(x, y, Tiles.get(name), data);
	}

	public void setTile(int x, int y, Tile t) {
		setTile(x, y, t, t.getDefaultData());
	}

	public void setTile(int x, int y, Tile t, int dataVal) {
		if (x < 0 || y < 0 || x >= w || y >= h) return;

		tiles[x + y * w] = t.id;
		data[x + y * w] = (short) dataVal;
	}

	public int getData(int x, int y) {
		if (x < 0 || y < 0 || x >= w || y >= h) return 0;
		return data[x + y * w] & 0xFFFF;
	}

	public void setData(int x, int y, int val) {
		if (x < 0 || y < 0 || x >= w || y >= h) return;
		data[x + y * w] = (short) val;
	}

	public void add(Entity e) {
		if (e == null) return;
		add(e, e.x, e.y);
	}

	public void add(Entity entity, int x, int y) {
		add(entity, x, y, false);
	}

	public void add(Entity entity, int x, int y, boolean tileCoords) {
		if (entity == null) return;
		if (tileCoords) {
			x = x * 16 + 8;
			y = y * 16 + 8;
		}
		entity.setLevel(this, x, y);

		entitiesToRemove.remove(entity); // To make sure the most recent request is satisfied.
		if (!entitiesToAdd.contains(entity))
			entitiesToAdd.add(entity);
	}

	public void remove(Entity e) {
		entitiesToAdd.remove(e);
		if (!entitiesToRemove.contains(e))
			entitiesToRemove.add(e);
	}

	/**
	 * Natural spawn.
	 */
	private void trySpawn() {
		int spawnSkipChance = (int) (MOB_SPAWN_FACTOR * Math.pow(mobCount, 2) / Math.pow(maxMobCount, 2));
		if (spawnSkipChance > 0 && random.nextInt(spawnSkipChance) != 0)
			return; // Hopefully will make mobs spawn a lot slower.

		boolean spawned = false;
		for (Player player : players) {
			assert player.getLevel().depth == depth;
			int lvl = -MyUtils.clamp(player.getLevel().depth, -4, 0);
			for (int i = 0; i < 30 && !spawned; i++) {
				int rnd = random.nextInt(100);
				int nx = random.nextInt(w) * 16 + 8, ny = random.nextInt(h) * 16 + 8;
				double distance = Math.hypot(Math.abs(nx - player.x), Math.abs(ny - player.y));
				if (distance < 160) continue; // Spawns only far from 10 tiles away.

				//System.out.println("trySpawn on level " + depth + " of lvl " + lvl + " mob w/ rand " + rnd + " at tile " + nx + "," + ny);

				// Spawns the enemy mobs; first part prevents enemy mob spawn on surface on first day, more or less.
				if ((Updater.getTime() == Updater.Time.Night && Updater.pastDay1 || depth != 0) && EnemyMob.checkStartPos(this, nx, ny)
					&& !isLight(nx, ny)) { // if night or underground, with a valid tile and dim place, spawn an enemy mob.

					if (depth != -4) { // Normal mobs
						if (rnd <= 40) add((new Slime(lvl)), nx, ny);
						else if (rnd <= 75) add((new Zombie(lvl)), nx, ny);
						else if (rnd >= 85) add((new Skeleton(lvl)), nx, ny);
						else add((new Creeper(lvl)), nx, ny);

					} else { // Special dungeon mobs
						if (rnd <= 40) add((new Snake(lvl)), nx, ny);
						else if (rnd <= 75) add((new Knight(lvl)), nx, ny);
						else if (rnd >= 85) add((new Snake(lvl)), nx, ny);
						else add((new Knight(lvl)), nx, ny);

					}

					spawned = true;
					continue; // Only 1 mob is spawned at the same time.
				}

				if (depth == 0 && PassiveMob.checkStartPos(this, nx, ny)) {
					// Spawns the friendly mobs.
					if (rnd <= (Updater.getTime() == Updater.Time.Night ? 22 : 33)) add((new Cow()), nx, ny);
					else if (rnd >= 68) add((new Pig()), nx, ny);
					else add((new Sheep()), nx, ny);

					spawned = true;
				}
			}
		}
	}

	public void removeAllEnemies() {
		for (Entity e : getEntityArray()) {
			if (e instanceof EnemyMob)
				if (!(e instanceof AirWizard) || Game.isMode("minicraft.settings.mode.creative")) // Don't remove the airwizard bosses! Unless in creative, since you can spawn more.
					e.remove();
		}
	}

	public void clearEntities() {
		entities.clear();
	}

	public Entity[] getEntityArray() {
		Entity[] entityArray;
		int index = 0;

		synchronized (entityLock) {
			entityArray = new Entity[entities.size()];
			for (Entity entity : entities) {
				entityArray[index++] = entity;
			}
		}

		return entityArray;
	}

	public List<Entity> getEntitiesInTiles(int xt, int yt, int radius) {
		return getEntitiesInTiles(xt, yt, radius, false);
	}

	@SafeVarargs
	public final List<Entity> getEntitiesInTiles(int xt, int yt, int radius, boolean includeGiven, Class<? extends Entity>... entityClasses) {
		return getEntitiesInTiles(xt - radius, yt - radius, xt + radius, yt + radius, includeGiven, entityClasses);
	}

	/**
	 * Get entities in a certain area on the level.
	 *
	 * @param xt0 Left
	 * @param yt0 Top
	 * @param xt1 Right
	 * @param yt1 Bottom
	 */
	public List<Entity> getEntitiesInTiles(int xt0, int yt0, int xt1, int yt1) {
		return getEntitiesInTiles(xt0, yt0, xt1, yt1, false);
	}

	/**
	 * Get entities in a certain area on the level, and filter them by class.
	 *
	 * @param xt0           Left
	 * @param yt0           Top
	 * @param xt1           Right
	 * @param yt1           Bottom
	 * @param includeGiven  If we should accept entities that match the provided entityClasses. If false, we ignore the provided entityClasses.
	 * @param entityClasses Entities to accept.
	 * @return A list of entities in the area.
	 */
	@SafeVarargs
	public final List<Entity> getEntitiesInTiles(int xt0, int yt0, int xt1, int yt1, boolean includeGiven, Class<? extends Entity>... entityClasses) {
		List<Entity> contained = new ArrayList<>();
		for (Entity e : getEntityArray()) {
			int xt = e.x >> 4;
			int yt = e.y >> 4;

			// Check if entity is in area.
			if (xt >= xt0 && xt <= xt1 && yt >= yt0 && yt <= yt1) {
				boolean matches = false;

				// Look through all entity classes to see if they match the current entity we are at.
				for (int i = 0; !matches && i < entityClasses.length; i++)
					// If the current entity and an entity class match.
					matches = entityClasses[i].isAssignableFrom(e.getClass());

				// Add if the current entity matches an entity class and includeGiven is true.
				// If includeGiven is false, add if it doesn't match.
				if (matches == includeGiven)
					contained.add(e);
			}
		}

		return contained;
	}

	/**
	 * Check if there is an entity on the specified tile.
	 *
	 * @param x The x position of the tile.
	 * @param y The y position of the tile
	 * @return True if there is an entity on the tile.
	 */
	public final boolean isEntityOnTile(int x, int y) {
		for (Entity e : getEntityArray()) {
			int xt = e.x >> 4;
			int yt = e.y >> 4;

			if (xt == x && yt == y) {
				return true;
			}
		}
		return false;
	}

	public List<Entity> getEntitiesInRect(Rectangle area) {
		List<Entity> result = new ArrayList<>();
		for (Entity e : getEntityArray()) {
			if (e.isTouching(area))
				result.add(e);
		}
		return result;
	}

	public List<Entity> getEntitiesInRect(Predicate<Entity> filter, Rectangle area) {
		List<Entity> result = new LinkedList<>();
		for (Entity entity : entities) {
			if (filter.test(entity) && entity.isTouching(area)) {
				result.add(entity);
			}
		}
		return result;
	}

	/// Finds all entities that are an instance of the given entity.
	public Entity[] getEntitiesOfClass(Class<? extends Entity> targetClass) {
		ArrayList<Entity> matches = new ArrayList<>();
		for (Entity e : getEntityArray()) {
			if (targetClass.isAssignableFrom(e.getClass()))
				matches.add(e);
		}

		return matches.toArray(new Entity[0]);
	}

	public Player[] getPlayers() {
		return players.toArray(new Player[0]);
	}

	public Player getClosestPlayer(int x, int y) {
		Player[] players = getPlayers();
		if (players.length == 0)
			return null;

		Player closest = players[0];
		int xd = closest.x - x;
		int yd = closest.y - y;
		for (int i = 1; i < players.length; i++) {
			int curxd = players[i].x - x;
			int curyd = players[i].y - y;
			if (xd * xd + yd * yd > curxd * curxd + curyd * curyd) {
				closest = players[i];
				xd = curxd;
				yd = curyd;
			}
		}

		return closest;
	}

	/**
	 * Calculates maximum position can be reached by an entity with the front boundary of hit box by tile hit box.
	 *
	 * @param sgn                    One-dimensional direction of displacement
	 * @param d                      Displacement vector
	 * @param hitBoxLeft             The left boundary of hit box
	 * @param hitBoxRight            The right boundary of hit box
	 * @param hitBoxFront            The front boundary of hit box
	 * @param frontTilePassableCheck The check of whether the front boundary of hit box hits the tile hit box;
	 *                               the first parameter takes the front tile position and second one takes the horizontal position
	 * @return The maximum front position can be reached by tile hit box check
	 */
	public static int calculateMaxFrontClosestTile(int sgn, int d, int hitBoxLeft, int hitBoxRight, int hitBoxFront,
												   BiPredicate<Integer, Integer> frontTilePassableCheck) {
		int hitBoxFront1 = hitBoxFront + d; // After maximum movement
		int hitBoxLeftTile = hitBoxLeft >> 4;
		int hitBoxRightTile = hitBoxRight >> 4;
		int hitBoxFrontTile = hitBoxFront >> 4;
		int hitBoxFrontTile1 = hitBoxFront1 >> 4;
		int maxFrontTile = hitBoxFrontTile1; // Value for full tile movement
		// Skips the current tile by adding 1.
		mainLoop:
		for (int front = hitBoxFrontTile + sgn; sgn < 0 ? front >= hitBoxFrontTile1 : front <= hitBoxFrontTile1; front += sgn) {
			for (int horTile = hitBoxLeftTile; horTile <= hitBoxRightTile; horTile++) {
				if (!frontTilePassableCheck.test(front, horTile)) {
					maxFrontTile = front - sgn; // Rolls back a tile by subtracting 1.
					break mainLoop; // Tile hit box check stops.
				}
			}
		}

		return hitBoxFront +
			(sgn > 0 ? Math.min(d, (maxFrontTile << 4) - hitBoxFront + (1 << 4) - 1) : Math.max(d, (maxFrontTile << 4) - hitBoxFront));
	}

	public Point[] getAreaTilePositions(int x, int y, int r) {
		return getAreaTilePositions(x, y, r, r);
	}

	public Point[] getAreaTilePositions(int x, int y, int rx, int ry) {
		ArrayList<Point> local = new ArrayList<>();
		for (int yp = y - ry; yp <= y + ry; yp++)
			for (int xp = x - rx; xp <= x + rx; xp++)
				if (xp >= 0 && xp < w && yp >= 0 && yp < h)
					local.add(new Point(xp, yp));
		return local.toArray(new Point[0]);
	}

	public Tile[] getAreaTiles(int x, int y, int r) {
		return getAreaTiles(x, y, r, r);
	}

	public Tile[] getAreaTiles(int x, int y, int rx, int ry) {
		ArrayList<Tile> local = new ArrayList<>();

		for (Point p : getAreaTilePositions(x, y, rx, ry))
			local.add(getTile(p.x, p.y));

		return local.toArray(new Tile[0]);
	}

	public void setAreaTiles(int xt, int yt, int r, Tile tile, int data) {
		setAreaTiles(xt, yt, r, tile, data, false);
	}

	public void setAreaTiles(int xt, int yt, int r, Tile tile, int data, boolean overwriteStairs) {
		for (int y = yt - r; y <= yt + r; y++) {
			for (int x = xt - r; x <= xt + r; x++) {
				if (overwriteStairs || (!getTile(x, y).name.toLowerCase().contains("stairs")))
					setTile(x, y, tile, data);
			}
		}
	}

	public void setAreaTiles(int xt, int yt, int r, Tile tile, int data, String[] blacklist) {
		for (int y = yt - r; y <= yt + r; y++) {
			for (int x = xt - r; x <= xt + r; x++) {
				if (!Arrays.asList(blacklist).contains(getTile(x, y).name.toLowerCase()))
					setTile(x, y, tile, data);
			}
		}
	}

	@FunctionalInterface
	public interface TileCheck {
		boolean check(Tile t, int x, int y);
	}

	public List<Point> getMatchingTiles(Tile search) {
		return getMatchingTiles((t, x, y) -> t.equals(search));
	}

	public List<Point> getMatchingTiles(Tile... search) {
		return getMatchingTiles((t, x, y) -> {
			for (Tile poss : search)
				if (t.equals(poss))
					return true;
			return false;
		});
	}

	public List<Point> getMatchingTiles(TileCheck condition) {
		List<Point> matches = new ArrayList<>();
		for (int y = 0; y < h; y++)
			for (int x = 0; x < w; x++)
				if (condition.check(getTile(x, y), x, y))
					matches.add(new Point(x, y));

		return matches;
	}

	public boolean isLight(int x, int y) {
		for (Tile t : getAreaTiles(x, y, 3))
			if (t instanceof TorchTile)
				return true;
		for (Entity e : getEntitiesInRect(e -> e instanceof Lantern, new Rectangle(x, y, 8, 8, Rectangle.CENTER_DIMS))) {
			int xx = (e.x >> 4) - x, yy = (e.y >> 4) - y, rr = e.getLightRadius() - 1;
			if (xx * xx + yy * yy < rr * rr)
				return true;
		}

		return false;
	}

	private void generateSpawnerStructures() {
		if (depth != -4) {
			for (int i = 0; i < 18 / -depth * (w / 128); i++) {

				/// For generating spawner dungeons
				MobAi m;
				int r = random.nextInt(5);

				if (r == 1) {
					m = new Skeleton(-depth);
				} else if (r == 2 || r == 0) {
					m = new Slime(-depth);
				} else {
					m = new Zombie(-depth);
				}

				Spawner sp = new Spawner(m);
				int x3 = random.nextInt(16 * w) / 16;
				int y3 = random.nextInt(16 * h) / 16;
				if (getTile(x3, y3) == Tiles.get("dirt")) {
					boolean xaxis2 = random.nextBoolean();

					if (xaxis2) {
						for (int s2 = x3; s2 < w - s2; s2++) {
							if (getTile(s2, y3) == Tiles.get("rock")) {
								sp.x = s2 * 16 - 24;
								sp.y = y3 * 16 - 24;
							}
						}
					} else {
						for (int s2 = y3; s2 < h - s2; s2++) {
							if (getTile(x3, s2) == Tiles.get("rock")) {
								sp.x = x3 * 16 - 24;
								sp.y = s2 * 16 - 24;
							}
						}
					}

					if (sp.x == 0 && sp.y == 0) {
						sp.x = x3 * 16 - 8;
						sp.y = y3 * 16 - 8;
					}

					if (getTile(sp.x / 16, sp.y / 16) == Tiles.get("rock")) {
						setTile(sp.x / 16, sp.y / 16, Tiles.get("dirt"));
					}

					Structure.mobDungeonCenter.draw(this, sp.x / 16, sp.y / 16);

					if (getTile(sp.x / 16, sp.y / 16 - 4) == Tiles.get("dirt")) {
						Structure.mobDungeonNorth.draw(this, sp.x / 16, sp.y / 16 - 5);
					}
					if (getTile(sp.x / 16, sp.y / 16 + 4) == Tiles.get("dirt")) {
						Structure.mobDungeonSouth.draw(this, sp.x / 16, sp.y / 16 + 5);
					}
					if (getTile(sp.x / 16 + 4, sp.y / 16) == Tiles.get("dirt")) {
						Structure.mobDungeonEast.draw(this, sp.x / 16 + 5, sp.y / 16);
					}
					if (getTile(sp.x / 16 - 4, sp.y / 16) == Tiles.get("dirt")) {
						Structure.mobDungeonWest.draw(this, sp.x / 16 - 5, sp.y / 16);
					}

					add(sp);
					for (int rpt = 0; rpt < 2; rpt++) {
						if (random.nextInt(2) != 0) continue;
						Chest c = new Chest();
						int chance = -depth;

						c.populateInvRandom("minidungeon", chance);

						add(c, sp.x - 16 + rpt * 32, sp.y - 16);
					}
				}
			}
		} else {
			for (int i = 0; i < 18 * (w / 128); i++) {

				/// For generating spawner dungeons
				MobAi m;
				int r = random.nextInt(2);

				if (r == 1) {
					m = new Knight(2);
				} else {
					m = new Snake(2);
				}

				Spawner sp = new Spawner(m);
				int x3 = random.nextInt(16 * w) / 16;
				int y3 = random.nextInt(16 * h) / 16;
				if (getTile(x3, y3) == Tiles.get("Obsidian")) {
					boolean xaxis2 = random.nextBoolean();

					if (xaxis2) {
						for (int s2 = x3; s2 < w - s2; s2++) {
							if (getTile(s2, y3) == Tiles.get("Obsidian Wall")) {
								sp.x = s2 * 16 - 24;
								sp.y = y3 * 16 - 24;
							}
						}
					} else {
						for (int s2 = y3; s2 < h - s2; s2++) {
							if (getTile(x3, s2) == Tiles.get("Obsidian Wall")) {
								sp.x = x3 * 16 - 24;
								sp.y = s2 * 16 - 24;
							}
						}
					}

					if (sp.x == 0 && sp.y == 0) {
						sp.x = x3 * 16 - 8;
						sp.y = y3 * 16 - 8;
					}

					if (getTile(sp.x / 16, sp.y / 16) == Tiles.get("Obsidian Wall")) {
						setTile(sp.x / 16, sp.y / 16, Tiles.get("dirt"));
					}

					Structure.dungeonSpawner.draw(this, sp.x / 16, sp.y / 16);

					add(sp);
					for (int rpt = 0; rpt < 2; rpt++) {
						if (random.nextInt(2) != 0) continue;
						DungeonChest c = new DungeonChest(true);
						chestCount++;

						add(c, sp.x - 16 + rpt * 32, sp.y - 16);
					}
				}
			}
		}
	}

	private void generateVillages() {
		int lastVillageX = 0;
		int lastVillageY = 0;

		for (int i = 0; i < w / 128 * 2; i++) {
			// Makes 2-8 villages based on world size

			for (int t = 0; t < 10; t++) {
				// Tries 10 times for each one

				int x = random.nextInt(w);
				int y = random.nextInt(h);

				// Makes sure the village isn't to close to the previous village
				if (getTile(x, y) == Tiles.get("grass") && (Math.abs(x - lastVillageX) > 16 && Math.abs(y - lastVillageY) > 16)) {
					lastVillageX = x;
					lastVillageY = y;

					// A number between 2 and 4
					int numHouses = random.nextInt(3) + 2;

					// Loops for each house in the village
					for (int hs = 0; hs < numHouses; hs++) {
						boolean hasChest = random.nextBoolean();
						boolean twoDoors = random.nextBoolean();
						int overlay = random.nextInt(2) + 1;

						// Basically just gets what offset this house should have from the center of the village
						int xo = hs == 0 || hs == 3 ? -4 : 4;
						int yo = hs < 2 ? -4 : 4;

						xo += random.nextInt(5) - 2;
						yo += random.nextInt(5) - 2;

						if (twoDoors) {
							Structure.villageHouseTwoDoor.draw(this, x + xo, y + yo);
						} else {
							Structure.villageHouseNormal.draw(this, x + xo, y + yo);
						}

						// Make the village look ruined
						if (overlay == 1) {
							Structure.villageRuinedOverlay1.draw(this, x + xo, y + yo);
						} else { // overlay == 2
							Structure.villageRuinedOverlay2.draw(this, x + xo, y + yo);
						}

						// Add a chest to some of the houses
						if (hasChest) {
							Chest c = new Chest();
							c.populateInvRandom("villagehouse", 1);
							add(c, (x + random.nextInt(2) + xo) << 4, (y + random.nextInt(2) + yo) << 4);
						}
					}

					break;
				}
			}
		}
	}

	private void generateDungeonStructures() {
		for (int i = 0; i < Math.sqrt(w); i++) {
			int x = random.nextInt(w - 2) + 1;
			int y = random.nextInt(h - 2) + 1;

			if (x > 8 && y > 8) {
				if (x < w - 8 && y < w - 8) {
					if (random.nextInt(2) == 1) {
						Structure.dungeonGarden.draw(this, x, y);
					} else {
						Structure.dungeonChest.draw(this, x, y);
					}
				}
			}
		}
	}

	/**
	 * Regenerating/repairing the boss room in the dungeon.
	 */
	public void regenerateBossRoom() {
		if (depth == -4) {
			Structure.dungeonBossRoom.draw(tiles, w / 2, h / 2, w); // Generating the boss room at the center.
			for (int x = w / 2 - 4; x < w / 2 + 5; x++) { // Resetting tile data.
				for (int y = h / 2 - 4; y < h / 2 + 5; y++) {
					setData(x, y, 0);
				}
			}
		}
	}

	public String toString() {
		return "Level(depth=" + depth + ")";
	}
}
