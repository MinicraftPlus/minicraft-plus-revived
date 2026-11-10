package minicraft.level;

import minicraft.core.Game;
import minicraft.core.World;
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
import minicraft.item.DyeItem;
import minicraft.item.Item;
import minicraft.level.biome.Biome;
import minicraft.level.biome.Biomes;
import minicraft.level.noise.LevelNoise;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;
import minicraft.level.tile.TorchTile;
import minicraft.level.tile.TreeTile;
import minicraft.level.tile.TreeTile.TreeType;
import minicraft.level.structure.Structure;
import minicraft.level.structure.Structures;
import minicraft.util.Logging;
import minicraft.util.MyUtils;
import minicraft.util.Simplex;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
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
	private final Simplex simplex;

	public static final int SKY_LEVEL = 1, SURFACE_LEVEL = 0, IRON_LEVEL = -1, GOLD_LEVEL = -2, GEM_LEVEL = -3, DUNGEON_LEVEL = -4;

	private static final String[] levelNames = { "Sky", "Surface", "Iron", "Gold", "Lava", "Dungeon" };
	public static String getLevelName(int depth) {
		return levelNames[-1 * depth + 1];
	}

	public static String getDepthString(int depth) {
		return Localization.getLocalized("minicraft.displays.loading.message.level", depth < 0 ? "B" + (-depth) : depth);
	}

	private static final int MOB_SPAWN_FACTOR = 100; // The chance of a mob actually trying to spawn when trySpawn is called equals: mobCount / maxMobCount * MOB_SPAWN_FACTOR. so, it basically equals the chance, 1/number, of a mob spawning when the mob cap is reached. I hope that makes sense...

	public int w, h; // Width and height of the level
	private final long seed; // The used seed that was used to generate the world

	public ChunkManager chunkManager; // A collection of chunks with it's own interface
	private Level parentLevel = null; // reference to parent level

	public final int depth; // Depth level of the level
	public int monsterDensity = 16; // Affects the number of monsters that are on the level, bigger the number the less monsters spawn.
	public int maxMobCount;
	public int chestCount;
	public int mobCount = 0;

	// Entities
	private final Object entityLock = new Object(); // I will be using this lock to avoid concurrency exceptions in entities and sparks set
	private final Set<Entity> entities = java.util.Collections.synchronizedSet(new HashSet<>()); // A list of all the entities in the world
	private final Set<Player> players = java.util.Collections.synchronizedSet(new HashSet<>()); // A list of all the players in the world
	private final List<Entity> entitiesToAdd = new ArrayList<>(); /// entities that will be added to the level on next tick are stored here. This is for the sake of multithreading optimization. (hopefully)
	private final List<Entity> entitiesToRemove = new ArrayList<>(); /// entities that will be removed from the level on next tick are stored here. This is for the sake of multithreading optimization. (hopefully)

	private final ChunkGenerator chunkGenerator;

	// Creates a sorter for all the entities to be rendered.
	//private static Comparator<Entity> spriteSorter = Comparator.comparingInt(e -> e.y); // Broken
	@SuppressWarnings("Convert2Lambda")
	private static Comparator<Entity> spriteSorter = Comparator.comparingInt(new ToIntFunction<Entity>() {
		@Override
		public int applyAsInt(Entity e) {
			return e.y;
		}
	});

	// Create a level object!
	public Level(int w, int h, long seed, int level, Level parentLevel, boolean makeWorld) {
		depth = level;
		this.w = w;
		this.h = h;
		this.seed = seed;
		this.parentLevel = parentLevel;
		random = new Random(seed);
		simplex = new Simplex(seed);

		chunkGenerator = new ChunkGenerator(this);
		new Thread(chunkGenerator, "Level " + level + " Chunk Generator").start();

		if (level != -4 && level != 0)
			monsterDensity = 8;

		updateMobCap();

		if (!makeWorld) {
			chunkManager = new ChunkManager();
			return;
		}

		Logging.WORLD.debug("Making level " + level + "...");

		chunkManager = new ChunkManager();
		if (chunkManager == null) {
			Logging.WORLD.error("Level generation: Returned chunks array is null");
			return;
		}

		checkAirWizard();
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

	// Save data
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
		for (Point p : chunkManager.getAllChunks())
			for (int x = p.x * ChunkManager.CHUNK_SIZE; x < (p.x + 1) * ChunkManager.CHUNK_SIZE; x++)
				for (int y = p.y * ChunkManager.CHUNK_SIZE; y < (p.y + 1) * ChunkManager.CHUNK_SIZE; y++)
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

	// Pick mob cap based on difficulty level & depth
	private void updateMobCap() {
		maxMobCount = 150 + 150 * Settings.getIdx("diff");
		if (depth == 1) maxMobCount /= 2;
		if (depth == 0 || depth == -4 || depth == -5) maxMobCount = maxMobCount * 2 / 3;
	}

	public long getSeed() {
		return seed;
	}

	// Generate just enough of this level to find a valid spawn for the player
	public List<Point> prepareForSpawn() {
		int radius = 0;
		List<Point> spawnOptions;
		// Look for at least 64 spawnable tiles...
		int targetNumOptions = 64;
		do {
			for(int x = -radius; x <= radius; x++)
				for(int y = -radius; y <= radius; y++)
					while(chunkManager.getChunkStage(x, y) < ChunkManager.CHUNK_STAGE_ASSIGNED_TILES)
						chunkGenerator.advanceChunk(x, y);

			spawnOptions = getMatchingTiles(Tiles.get("grass"));
			if(spawnOptions.size() < targetNumOptions && radius >= 1)
				spawnOptions.addAll(getMatchingTiles((t, x, y) -> t.maySpawn()));

			radius++;
		} while (spawnOptions.size() < targetNumOptions && radius < 8);

		// Couldn't find ANYTHING??
		// Add default 0, 0
		if(spawnOptions.isEmpty()) {
			Logging.WORLD.debug("No valid spawnpoints found! " + radius);
			spawnOptions.add(new Point(0, 0));
		}

		return spawnOptions;
	}

	public ChunkGenerator getChunkGenerator() {
		return chunkGenerator;
	}

	// Check if there isn't an air wizard where there needs to be
	public void checkAirWizard() {
		if (depth != 1 || AirWizard.beaten)
			return;

		boolean found = false;
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

		if (!found) {
			AirWizard aw = new AirWizard();
			add(aw, w / 2, h / 2, true);
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

			for(Player player : players) {
				for (int i = 0; i < 300; i++) { // remain using tick rate based on W and H, TODO: add tickrate constant (or variable) when removing w and h, currently tick 10 tiles in surrounding chunks
					int xt = random.nextInt(ChunkManager.CHUNK_SIZE * 2) - ChunkManager.CHUNK_SIZE + (player.x >> 4);
					int yt = random.nextInt(ChunkManager.CHUNK_SIZE * 2) - ChunkManager.CHUNK_SIZE + (player.y >> 4);
					getTile(xt, yt).tick(this, xt, yt);
				}
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
		for (Player player : players)
			if (Math.abs(player.x - entity.x) < 128 && Math.abs(player.y - entity.y) < 76)
				return true;

		return false;
	}

	public double distanceOfClosestPlayer(Entity entity) {
		double sq_distance = Double.POSITIVE_INFINITY;
		for (Player player : players) {
			sq_distance = Math.min(sq_distance, (entity.x-player.x)*(entity.x-player.x) + (entity.y-player.y)*(entity.y-player.y));
		}

		return Math.sqrt(sq_distance);
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

	// Render tiles of level
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

	// Render sprites of level
	public void renderSprites(Screen screen, int xScroll, int yScroll) {
		int xo = xScroll >> 4; // Latches to the nearest tile coordinate
		int yo = yScroll >> 4;
		int w = (Screen.w + 15) >> 4;
		int h = (Screen.h + 15) >> 4;

		screen.setOffset(xScroll, yScroll);
		sortAndRender(screen, getEntitiesInTiles(xo - 1, yo - 1, xo + w + 1, yo + h + 1));

		screen.setOffset(0, 0);
	}

	// Update lighting data
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
				int lr = getTile(x, y).getLightRadius(this, x, y);
				if (lr > 0) screen.renderLight((x << 4) + 8, (y << 4) + 8, lr * brightness);
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

	public TreeTile.TreeType getTreeType(int x, int y) {
		double featureSize = 32;
		float a = simplex.noise3(x / featureSize, y / featureSize, -100),
		      b = simplex.noise3(x / featureSize, y / featureSize, -200);

		int idx = (int) Math.round(Math.abs(a - b) * 3 - 2);
		return (idx >= TreeType.values().length || idx < 0) ? TreeType.OAK : TreeType.values()[idx];
	}

	public Tile getTile(int x, int y) {
		return chunkManager.getTile(x, y);
	}

	public void setTile(int x, int y, Tile t) {
		setTile(x, y, t, t.getDefaultData());
	}

	public void setTile(int x, int y, Tile t, int dataVal) {
		chunkManager.setTile(x, y, t, dataVal);
		getTile(x, y).onTileSet(this, x, y);
	}

	public int getData(int x, int y) {
		return chunkManager.getData(x, y);
	}

	public void setData(int x, int y, int val) {
		chunkManager.setData(x, y, val);
	}

	public Biome getBiome(int x, int y) {
		return chunkManager.getBiome(x, y);
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
			x = (x << 4) + 8;
			y = (y << 4) + 8;
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
				int nx = (random.nextInt(ChunkManager.CHUNK_SIZE * 2) - ChunkManager.CHUNK_SIZE) * 16 + player.x,
					ny = (random.nextInt(ChunkManager.CHUNK_SIZE * 2) - ChunkManager.CHUNK_SIZE) * 16 + player.y;

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
					else { // Sheep spawning
						double colorRnd = random.nextDouble();
						if (colorRnd < 0.8) { // 80% for default color, i.e. white
							add((new Sheep()), nx, ny);
						} else if (colorRnd < 0.85) { // 5% for black
							add((new Sheep(DyeItem.DyeColor.BLACK)), nx, ny);
						} else if (colorRnd < 0.9) { // 5% for gray
							add((new Sheep(DyeItem.DyeColor.GRAY)), nx, ny);
						} else if (colorRnd < 0.95) { // 5% for light gray
							add((new Sheep(DyeItem.DyeColor.LIGHT_GRAY)), nx, ny);
						} else if (colorRnd < 0.98) { // 3% for brown
							add((new Sheep(DyeItem.DyeColor.BROWN)), nx, ny);
						} else { // 2% for pink
							add((new Sheep(DyeItem.DyeColor.PINK)), nx, ny);
						}
					}

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
				if(entity == null)
					continue;
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

	public void setAreaTiles(int xt, int yt, int r, Tile tile, int data, TileCheck condition) {
		for (int y = yt - r; y <= yt + r; y++) {
			for (int x = xt - r; x <= xt + r; x++) {
				if (condition.check(getTile(x, y), x, y))
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
		for (Point p : chunkManager.getAllChunks())
			for (int x = p.x * ChunkManager.CHUNK_SIZE; x < (p.x + 1) * ChunkManager.CHUNK_SIZE; x++)
				for (int y = p.y * ChunkManager.CHUNK_SIZE; y < (p.y + 1) * ChunkManager.CHUNK_SIZE; y++)
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

	public String toString() {
		return "Level(depth=" + depth + ")";
	}
}
