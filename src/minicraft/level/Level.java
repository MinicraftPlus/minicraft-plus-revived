package minicraft.level;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.ToIntFunction;

import minicraft.core.Game;
import minicraft.core.Network;
import minicraft.core.Updater;
import minicraft.core.io.Settings;
import minicraft.entity.ClientTickable;
import minicraft.entity.Entity;
import minicraft.entity.ItemEntity;
import minicraft.entity.furniture.Chest;
import minicraft.entity.furniture.Crafter;
import minicraft.entity.furniture.DungeonChest;
import minicraft.entity.furniture.Lantern;
import minicraft.entity.furniture.Spawner;
import minicraft.entity.furniture.Tnt;
import minicraft.entity.mob.*;
import minicraft.entity.particle.Particle;
import minicraft.gfx.Point;
import minicraft.gfx.Rectangle;
import minicraft.gfx.Screen;
import minicraft.item.Inventory;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolType;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;
import minicraft.level.tile.TorchTile;

public class Level {
	private Random random = new Random();
	
	private static final String[] levelNames = {"Sky", "Surface", "Iron", "Gold", "Lava", "Dungeon"};
	public static String getLevelName(int depth) { return levelNames[-1*depth+1]; }
	public static String getDepthString(int depth) { return "Level "+(depth<0?"B"+(-depth):depth); }
	
	private static final int MOB_SPAWN_FACTOR = 100; // the chance of a mob actually trying to spawn when trySpawn is called equals: mobCount / maxMobCount * MOB_SPAWN_FACTOR. so, it basically equals the chance, 1/number, of a mob spawning when the mob cap is reached. I hope that makes sense...
	
	public int w, h; // width and height of the level
	
	public byte[] tiles; // an array of all the tiles in the world.
	public byte[] data; // an array of the data of the tiles in the world. // ?
	
	public final int depth; // depth level of the level
	public int monsterDensity = 16; // affects the number of monsters that are on the level, bigger the number the less monsters spawn.
	public int maxMobCount;
	public int chestCount;
	public int mobCount = 0;
	
	private Set<Entity> entities = java.util.Collections.synchronizedSet(new HashSet<>()); // A list of all the entities in the world
	private Set<Player> players = java.util.Collections.synchronizedSet(new HashSet<>()); // A list of all the players in the world
	private List<Entity> entitiesToAdd = new ArrayList<>(); /// entities that will be added to the level on next tick are stored here. This is for the sake of multithreading optimization. (hopefully)
	private List<Entity> entitiesToRemove = new ArrayList<>(); /// entities that will be removed from the level on next tick are stored here. This is for the sake of multithreading optimization. (hopefully)
	// creates a sorter for all the entities to be rendered.
	private static Comparator<Entity> spriteSorter = Comparator.comparingInt(new ToIntFunction<Entity>() {
		@Override
		public int applyAsInt(Entity e) { return e.y; }
	});
	
	public Entity[] getEntitiesToSave() {
		Entity[] allEntities = new Entity[entities.size()+entitiesToAdd.size()];
		Entity[] toAdd = entitiesToAdd.toArray(new Entity[entitiesToAdd.size()]);
		Entity[] current = getEntityArray();
		System.arraycopy(current, 0, allEntities, 0, current.length);
		System.arraycopy(toAdd, 0, allEntities, current.length, toAdd.length);
		
		return allEntities;
	}
	
	/// This is a solely debug method I made, to make printing repetitive stuff easier.
		// should be changed to accept prepend and entity, or a tile (as an Object). It will get the coordinates and class name from the object, and will divide coords by 16 if passed an entity.
	public void printLevelLoc(String prefix, int x, int y) { printLevelLoc(prefix, x, y, ""); }
	public void printLevelLoc(String prefix, int x, int y, String suffix) {
		String levelName = getLevelName(depth);
		
		System.out.println(prefix + " on " + levelName + " level ("+x+","+y+")" + suffix);
	}
	
	public void printTileLocs(Tile t) {
		for(int x = 0; x < w; x++)
			for(int y = 0; y < h; y++)
				if(getTile(x, y).id == t.id)
					printLevelLoc(t.name, x, y);
	}
	public void printEntityLocs(Class<? extends Entity> c) {
		int numfound = 0;
		for(Entity entity: getEntityArray()) {
			if(c.isAssignableFrom(entity.getClass())) {
				printLevelLoc(entity.toString(), entity.x>>4, entity.y>>4);
				numfound++;
			}
		}
		
		System.out.println("found " + numfound + " entities in level of depth " + depth);
	}
	
	private void updateMobCap() {
		maxMobCount = 150 + 150 * Settings.getIdx("diff");
		if(depth == 1) maxMobCount /= 2;
		if(depth == 0 || depth == -4) maxMobCount = maxMobCount * 2 / 3;
	}
	
	@SuppressWarnings("unchecked") // @SuppressWarnings ignores the warnings (yellow underline) in this method.
	/** Level which the world is contained in */
	public Level(int w, int h, int level, Level parentLevel) {this(w, h, level, parentLevel, true); }
	public Level(int w, int h, int level, Level parentLevel, boolean makeWorld) {
		depth = level;
		this.w = w;
		this.h = h;
		byte[][] maps; // multidimensional array (an array within a array), used for the map
		int saveTile;
		
		if(level != -4 && level != 0)
			monsterDensity = 8;
	
		updateMobCap();
		
		if(!makeWorld) {
			int arrsize = w * h;
			tiles = new byte[arrsize];
			data = new byte[arrsize];
			return;
		}
		
		if(Game.debug) System.out.println("Making level "+level+"...");
		
		maps = LevelGen.createAndValidateMap(w, h, level);
		if(maps == null) {
			System.err.println("Level Gen ERROR: returned maps array is null");
			return;
		}
		
		tiles = maps[0]; // assigns the tiles in the map
		data = maps[1]; // assigns the data of the tiles
		
		if (parentLevel != null) { // If the level above this one is not null (aka, if this isn't the sky level)
			for (int y = 0; y < h; y++) { // loop through height
				for (int x = 0; x < w; x++) { // loop through width
					if (parentLevel.getTile(x, y) == Tiles.get("Stairs Down")) { // If the tile in the level above the current one is a stairs down then...
						setTile(x, y, Tiles.get("Stairs Up")); // set a stairs up tile in the same position on the current level
						if (level == -4) /// make the obsidian wall formation around the stair to the dungeon level
							Structure.dungeonGate.draw(this, x, y);
						
						else if (level == 0) { // surface
							if (Game.debug) System.out.println("setting tiles around "+x+","+y+" to hard rock");
							setAreaTiles(x, y, 1, Tiles.get("Hard Rock"), 0); // surround the sky stairs with hard rock; won't overwrite the stairs
						}
						else // any other level, the up-stairs should have dirt on all sides.
							setAreaTiles(x, y, 1, Tiles.get("dirt"), 0); // won't overwrite the stairs
					}
				}
			}
		}
		
		checkChestCount(false);
		
		if (level < 0)
			generateSpawnerStructures();
		
		checkAirWizard();
		
		if (Game.debug) printTileLocs(Tiles.get("Stairs Down"));
	}
	
	public void checkAirWizard() {
		checkAirWizard(true);
	}
	private void checkAirWizard(boolean check) {
		if (depth == 1 && !AirWizard.beaten) { // add the airwizard to the surface
			
			boolean found = false;
			if(check) {
				for(Entity e: entitiesToAdd)
					if(e instanceof AirWizard)
						found = true;
				for(Entity e: entities)
					if(e instanceof AirWizard)
						found = true;
			}
			
			if (!found) {
				AirWizard aw = new AirWizard(false);
				add(aw, w/2, h/2, true);
			}
		}
	}
	
	public void checkChestCount() {
		checkChestCount(true);
	}
	private void checkChestCount(boolean check) {
		/// if the level is the dungeon, and we're not just loading the world...
		if (depth != -4) return;
		
		int numChests = 0;
		
		if(check) {
			for(Entity e: entitiesToAdd)
				if(e instanceof DungeonChest)
					numChests++;
			for(Entity e: entities)
				if(e instanceof DungeonChest)
					numChests++;
			if (Game.debug) System.out.println("found " + numChests + " chests.");
		}
		
		/// make DungeonChests!
		for (int i = numChests; i < 10 * (w / 128); i++) {
			DungeonChest d = new DungeonChest();
			boolean addedchest = false;
			while(!addedchest) { // keep running until we successfully add a DungeonChest
				//pick a random tile:
				int x2 = random.nextInt(16 * w) / 16;
				int y2 = random.nextInt(16 * h) / 16;
				if (getTile(x2, y2) == Tiles.get("Obsidian")) {
					boolean xaxis = random.nextBoolean();
					if (xaxis) {
						for (int s = x2; s < w - s; s++) {
							if (getTile(s, y2) == Tiles.get("Obsidian Wall")) {
								d.x = s * 16 - 24;
								d.y = y2 * 16 - 24;
							}
						}
					} else { // y axis
						for (int s = y2; s < y2 - s; s++) {
							if (getTile(x2, s) == Tiles.get("Obsidian Wall")) {
								d.x = x2 * 16 - 24;
								d.y = s * 16 - 24;
							}
						}
					}
					if (d.x == 0 && d.y == 0) {
						d.x = x2 * 16 - 8;
						d.y = y2 * 16 - 8;
					}
					if (getTile(d.x / 16, d.y / 16) == Tiles.get("Obsidian Wall")) {
						setTile(d.x / 16, d.y / 16, Tiles.get("Obsidian"));
					}
					add(d);
					chestCount++;
					addedchest = true;
				}
			}
		}
	}

	public void tick(boolean fullTick) {
		int count = 0;
		
		while(entitiesToAdd.size() > 0) {
			Entity entity = entitiesToAdd.get(0);
			boolean inLevel = entities.contains(entity);
			
			if(!inLevel) {
				if(Game.isValidServer())
					Game.server.broadcastEntityAddition(entity, true);
				
				if (!Game.isValidServer() || !(entity instanceof Particle)) {
					if (Game.debug) printEntityStatus("Adding ", entity, "furniture.DungeonChest", "mob.AirWizard", "mob.Player");
					
					entities.add(entity);
					if(entity instanceof Player)
						players.add((Player)entity);
				}
			}
			entitiesToAdd.remove(entity);
		}
		
		if(fullTick && (!Game.isValidServer() || getPlayers().length > 0)) {
			// this prevents any entity (or tile) tick action from happening on a server level with no players.
			
			if (!Game.isValidClient()) {
				for (int i = 0; i < w * h / 50; i++) {
					int xt = random.nextInt(w);
					int yt = random.nextInt(w);
					getTile(xt, yt).tick(this, xt, yt);
					if(Game.isValidServer())
						Game.server.broadcastTileUpdate(this, xt, yt);
				}
			}
			
			// entity loop
			for (Entity e : getEntityArray()) {
				if (e == null) continue;
				
				if (Game.hasConnectedClients() && e instanceof Player && !(e instanceof RemotePlayer)) {
					if (Game.debug)
						System.out.println("SERVER is removing regular player " + e + " from level " + this);
					e.remove();
				}
				if (Game.isValidServer() && e instanceof Particle) {
					// there is no need to track this.
					if (Game.debug)
						System.out.println("SERVER warning: found particle in entity list: " + e + ". Removing from level " + this);
					e.remove();
				}
				
				if (e.isRemoved()) continue;
				
				if(e != Game.player) { // player is ticked separately, others are ticked on server
					if(!Game.isValidClient())
						e.tick(); /// the main entity tick call.
					else if(e instanceof ClientTickable)
						((ClientTickable)e).clientTick();
				}
				
				if (e.isRemoved()) continue;
				
				if (Game.hasConnectedClients()) // this means it's a server
					Game.server.broadcastEntityUpdate(e);
				
				if (e instanceof Mob) count++;
			}
			
			
			for (Entity e : getEntityArray())
				if (e.isRemoved() || e.getLevel() != this)
					remove(e);
		}
		
		while(count > maxMobCount) {
			Entity removeThis = (Entity)entities.toArray()[(random.nextInt(entities.size()))];
			if(removeThis instanceof MobAi) {
				remove(removeThis);
				count--;
			}
		}
		
		while(entitiesToRemove.size() > 0) {
			Entity entity = entitiesToRemove.get(0);
			
			if(Game.isValidServer() && !(entity instanceof Particle) && entity.getLevel() == this)
				Game.server.broadcastEntityRemoval(entity, this, true);
			
			if(Game.debug) printEntityStatus("Removing ", entity, "mob.Player");
			
			entity.remove(this); // this will safely fail if the entity's level doesn't match this one.
			entities.remove(entity);
			
			if(entity instanceof Player)
				players.remove(entity);
			entitiesToRemove.remove(entity);
		}
		
		mobCount = count;
		
		if(Game.isValidServer() && players.size() == 0)
			return; // don't try to spawn any mobs when there's no player on the level, on a server.
		
		if(fullTick && count < maxMobCount && !Game.isValidClient())
			trySpawn();
	}
	
	public void printEntityStatus(String entityMessage, Entity entity, String... searching) {
		// "searching" can contain any number of class names I want to print when found.
		String clazz = entity.getClass().getCanonicalName();
		clazz = clazz.substring(clazz.lastIndexOf(".")+1);
		for(String search: searching) {
			try {
				if(Class.forName("minicraft.entity."+search).isAssignableFrom(entity.getClass())) {
					if (clazz.equals("AirWizard")) clazz += ((AirWizard)entity).secondform ? " II" : "";
					printLevelLoc(Network.onlinePrefix()+entityMessage + clazz, entity.x>>4, entity.y>>4, ": " + entity);
					break;
				}
			} catch(ClassNotFoundException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public void dropItem(int x, int y, int mincount, int maxcount, Item... items) {
		dropItem(x, y, mincount+random.nextInt(maxcount-mincount+1), items);
	}
	public void dropItem(int x, int y, int count, Item... items) {
		for (int i = 0; i < count; i++)
			dropItem(x, y, items);
	}
	public void dropItem(int x, int y, Item... items) {
		for(Item i: items)
			dropItem(x, y, i);
	}
	public ItemEntity dropItem(int x, int y, Item i) {
		if(Game.isValidClient())
			System.err.println("dropping item on client: "+i);
		int ranx, rany;
		do {
			ranx = x + random.nextInt(11) - 5;
			rany = y + random.nextInt(11) - 5;
		} while(ranx >> 4 != x >> 4 || rany >> 4 != y >> 4);
		ItemEntity ie = new ItemEntity(i, ranx, rany);
		add(ie);
		return ie;
	}

	public void renderBackground(Screen screen, int xScroll, int yScroll) {
		int xo = xScroll >> 4; // latches to the nearest tile coordinate
		int yo = yScroll >> 4;
		int w = (Screen.w) >> 4; // there used to be a "+15" as in below method
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
		int xo = xScroll >> 4; // latches to the nearest tile coordinate
		int yo = yScroll >> 4;
		int w = (Screen.w + 15) >> 4;
		int h = (Screen.h + 15) >> 4;
		
		screen.setOffset(xScroll, yScroll);
		sortAndRender(screen, getEntitiesInTiles(xo, yo, xo + w, yo + h));
		
		screen.setOffset(0, 0);
	}

	public void renderLight(Screen screen, int xScroll, int yScroll, int brightness) {
		int xo = xScroll >> 4;
		int yo = yScroll >> 4;
		int w = (Screen.w + 15) >> 4;
		int h = (Screen.h + 15) >> 4;

		screen.setOffset(xScroll, yScroll);
		int r = 4;
		
		List<Entity> entities = getEntitiesInTiles(xo - r, yo - r, w + xo + r, h + yo + r);
		for(Entity e: entities) {
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
		for (int i = 0; i < list.size(); i++) {
			Entity e = list.get(i);
			if(e.getLevel() == this && !e.isRemoved())
				e.render(screen);
			else
				remove(e);
		}
	}
	
	public Tile getTile(int x, int y) {
		if (x < 0 || y < 0 || x >= w || y >= h/* || (x + y * w) >= tiles.length*/) return Tiles.get("rock");
		int id = tiles[x + y * w];
		if(id < 0) id += 256;
		return Tiles.get(id);
	}
	
	public void setTile(int x, int y, String tilewithdata) {
		if(!tilewithdata.contains("_")) {
			setTile(x, y, Tiles.get(tilewithdata));
			return;
		}
		String name = tilewithdata.substring(0, tilewithdata.indexOf("_"));
		int data = Tiles.get(name).getData(tilewithdata.substring(name.length()+1));
		setTile(x, y, Tiles.get(name), data);
	}
	public void setTile(int x, int y, Tile t) {
		setTile(x, y, t, t.getDefaultData());
	}
	public void setTile(int x, int y, Tile t, int dataVal) {
		if (x < 0 || y < 0 || x >= w || y >= h) return;
		//if (Game.debug) printLevelLoc("setting tile from " + Tiles.get(tiles[x+y*w]).name + " to " + t.name, x, y);
		
		if(Game.isValidClient() && !Game.isValidServer()) {
			System.out.println("Client requested a tile update for the " + t.name + " tile at " + x + "," + y);
		} else {
			tiles[x + y * w] = t.id;
			data[x + y * w] = (byte) dataVal;
		}
		
		if(Game.isValidServer())
			Game.server.broadcastTileUpdate(this, x, y);
	}
	
	public int getData(int x, int y) {
		if (x < 0 || y < 0 || x >= w || y >= h) return 0;
		return data[x + y * w] & 0xff;
	}
	
	public void setData(int x, int y, int val) {
		if (x < 0 || y < 0 || x >= w || y >= h) return;
		data[x + y * w] = (byte) val;
	}
	
	public void add(Entity e) { if(e==null) return; add(e, e.x, e.y); }
	public void add(Entity entity, int x, int y) { add(entity, x, y, false); }
	public void add(Entity entity, int x, int y, boolean tileCoords) {
		if(entity == null) return;
		if(tileCoords) {
			x = x*16+8;
			y = y*16+8;
		}
		entity.setLevel(this, x, y);
		
		entitiesToRemove.remove(entity); // to make sure the most recent request is satisfied.
		if(!entitiesToAdd.contains(entity))
			entitiesToAdd.add(entity);
	}
	
	public void remove(Entity e) {
		entitiesToAdd.remove(e);
		if(!entitiesToRemove.contains(e))
			entitiesToRemove.add(e);
	}
	
	private void trySpawn() {
		int spawnSkipChance = (int) (MOB_SPAWN_FACTOR * Math.pow(mobCount, 2) / Math.pow(maxMobCount, 2));
		if(spawnSkipChance > 0 && random.nextInt(spawnSkipChance) != 0)
			return; // hopefully will make mobs spawn a lot slower.
		
		boolean spawned = false;
		for (int i = 0; i < 30 && !spawned; i++) {
			int minLevel = 1, maxLevel = 1;
			if (depth < 0) {
				maxLevel = (-depth) + (Math.random() > 0.75 ? 1 : 0);
			}
			if (depth > 0) {
				minLevel = maxLevel = 4;
			}
			
			
			int lvl = random.nextInt(maxLevel - minLevel + 1) + minLevel;
			int rnd = random.nextInt(100);
			int nx = random.nextInt(w) * 16 + 8, ny = random.nextInt(h) * 16 + 8;
			
			//System.out.println("trySpawn on level " + depth + " of lvl " + lvl + " mob w/ rand " + rnd + " at tile " + nx + "," + ny);
			
			// spawns the enemy mobs; first part prevents enemy mob spawn on surface on first day, more or less.
			if ((Updater.getTime() == Updater.Time.Night && Updater.pastDay1 || depth != 0) && EnemyMob.checkStartPos(this, nx, ny)) { // if night or underground, with a valid tile, spawn an enemy mob.
				if(depth != -4) { // normal mobs
					if (rnd <= 40) add((new Slime(lvl)), nx, ny);
					else if (rnd <= 75) add((new Zombie(lvl)), nx, ny);
					else if (rnd >= 85) add((new Skeleton(lvl)), nx, ny);
					else add((new Creeper(lvl)), nx, ny);
				} else { // special dungeon mobs
					if (rnd <= 40) add((new Snake(lvl)), nx, ny);
					else if (rnd <= 75) add((new Knight(lvl)), nx, ny);
					else if (rnd >= 85) add((new Snake(lvl)), nx, ny);
					else add((new Knight(lvl)), nx, ny);
				}
				
				spawned = true;
			}
			
			if(depth == 0 && PassiveMob.checkStartPos(this, nx, ny)) {
				// spawns the friendly mobs.
				if (rnd <= (Updater.getTime()==Updater.Time.Night?22:33)) add((new Cow()), nx, ny);
				else if (rnd >= 68) add((new Pig()), nx, ny);
				else add((new Sheep()), nx, ny);
				
				spawned = true;
			}
		}
	}

	public void removeAllEnemies() {
		for (Entity e: getEntityArray()) {
			if(e instanceof EnemyMob)
				if(!(e instanceof AirWizard) || Game.isMode("creative")) // don't remove the airwizard bosses! Unless in creative, since you can spawn more.
					e.remove();
		}
	}
	
	public void clearEntities() {
		if(!Game.ISONLINE)
			entities.clear();
		else
			for(Entity e: getEntityArray())
				e.remove();
	}
	
	public Entity[] getEntityArray() {
		return entities.toArray(new Entity[0]);
	}
	
	public List<Entity> getEntitiesInTiles(int xt, int yt, int radius) { return getEntitiesInTiles(xt, yt, radius, false); }
	@SafeVarargs
	public final List<Entity> getEntitiesInTiles(int xt, int yt, int radius, boolean includeGiven, Class<? extends Entity>... entityClasses) { return getEntitiesInTiles(xt-radius, yt-radius, xt+radius, yt+radius, includeGiven, entityClasses); }
	public List<Entity> getEntitiesInTiles(int xt0, int yt0, int xt1, int yt1) { return getEntitiesInTiles(xt0, yt0, xt1, yt1, false); }
	@SafeVarargs
	public final List<Entity> getEntitiesInTiles(int xt0, int yt0, int xt1, int yt1, boolean includeGiven, Class<? extends Entity>... entityClasses) {
		List<Entity> contained = new ArrayList<>();
		for(Entity e: getEntityArray()) {
			int xt = e.x >> 4;
			int yt = e.y >> 4;
			if(xt >= xt0 && xt <= xt1 && yt >= yt0 && yt <= yt1) {
				boolean matches = false;
				for(int i = 0; !matches && i < entityClasses.length; i++)
					if(entityClasses[i].isAssignableFrom(e.getClass()))
						matches = true;
				
				if(matches == includeGiven)
					contained.add(e);
			}
		}
		
		return contained;
	}
	
	public List<Entity> getEntitiesInRect(Rectangle area) {
		List<Entity> result = new ArrayList<>();
		for(Entity e: getEntityArray()) {
			if (e.isTouching(area))
				result.add(e);
		}
		return result;
	}
	
	/// finds all entities that are an instance of the given entity.
	public Entity[] getEntitiesOfClass(Class<? extends Entity> targetClass) {
		ArrayList<Entity> matches = new ArrayList<>();
		for(Entity e: getEntityArray()) {
			if(targetClass.isAssignableFrom(e.getClass()))
				matches.add(e);
		}
		
		return matches.toArray(new Entity[0]);
	}
	
	public Player[] getPlayers() {
		return players.toArray(new Player[players.size()]);
	}
	
	public Player getClosestPlayer(int x, int y) {
		Player[] players = getPlayers();
		if(players.length == 0)
			return null;
		
		Player closest = players[0];
		int xd = closest.x - x;
		int yd = closest.y - y;
		for(int i = 1; i < players.length; i++) {
			int curxd = players[i].x - x;
			int curyd = players[i].y - y;
			if(xd*xd + yd*yd > curxd*curxd + curyd*curyd) {
				closest = players[i];
				xd = curxd;
				yd = curyd;
			}
		}
		
		return closest;
	}
	
	public Point[] getAreaTilePositions(int x, int y, int r) { return getAreaTilePositions(x, y, r, r); }
	public Point[] getAreaTilePositions(int x, int y, int rx, int ry) {
		ArrayList<Point> local = new ArrayList<>();
		for(int yp = y-ry; yp <= y+ry; yp++)
			for(int xp = x-rx; xp <= x+rx; xp++)
				if(xp >= 0 && xp < w && yp >= 0 && yp < h)
					local.add(new Point(xp, yp));
		return local.toArray(new Point[local.size()]);
	}
	
	public Tile[] getAreaTiles(int x, int y, int r) { return getAreaTiles(x, y, r, r); }
	public Tile[] getAreaTiles(int x, int y, int rx, int ry) {
		ArrayList<Tile> local = new ArrayList<>();
		
		for(Point p: getAreaTilePositions(x, y, rx, ry))
			local.add(getTile(p.x, p.y));
		
		return local.toArray(new Tile[local.size()]);
	}
	
	public void setAreaTiles(int xt, int yt, int r, Tile tile, int data) { setAreaTiles(xt, yt, r, tile, data, false); }
	public void setAreaTiles(int xt, int yt, int r, Tile tile, int data, boolean overwriteStairs) {
		for(int y = yt-r; y <= yt+r; y++) {
			for (int x = xt - r; x <= xt + r; x++) {
				if(overwriteStairs || (!getTile(x, y).name.toLowerCase().contains("stairs")))
					setTile(x, y, tile, data);
			}
		}
	}
	
	@FunctionalInterface
	public interface TileCheck {
		boolean check(Tile t, int x, int y);
	}
	
	public List<Point> getMatchingTiles(Tile search) { return getMatchingTiles((t, x, y) -> t.equals(search)); }
	public List<Point> getMatchingTiles(Tile... search) {
		return getMatchingTiles((t, x, y) -> {
			for(Tile poss: search)
				if(t.equals(poss))
					return true;
			return false;
		});
	}
	public List<Point> getMatchingTiles(TileCheck condition) {
		List<Point> matches = new ArrayList<>();
		for(int y = 0; y < h; y++)
			for(int x = 0; x < w; x++)
				if(condition.check(getTile(x, y), x, y))
					matches.add(new Point(x, y));
		
		return matches;
	}
	
	public boolean isLight(int x, int y) {
		for(Tile t: getAreaTiles(x, y, 3))
			if(t instanceof TorchTile)
				return true;
		
		return false;
	}
	
	private boolean noStairs(int x, int y) {
		return getTile(x, y) != Tiles.get("Stairs Down");
	}
	
	
	private void generateSpawnerStructures() {
		for (int i = 0; i < 18 / -depth * (w / 128); i++) {
			/// for generating spawner dungeons
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
					for (int s2 = y3; s2 < y3 - s2; s2++) {
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
				
				for (int xx = 0; xx < 5; xx++) {
					for (int yy = 0; yy < 5; yy++) {
						if (noStairs(sp.x / 16 - 2 + xx, sp.y / 16 - 2 + yy)) {
							setTile(sp.x / 16 - 2 + xx, sp.y / 16 - 2 + yy, Tiles.get("Stone Bricks"));
							
							if((xx < 1 || yy < 1 || xx > 3 || yy > 3) && (xx != 2 || yy != 0) && (xx != 2 || yy != 4) && (xx != 0 || yy != 2) && (xx != 4 || yy != 2)) {
								setTile(sp.x / 16 - 2 + xx, sp.y / 16 - 2 + yy, Tiles.get("Stone Wall"));
							}
						}
					}
				}
				
				add(sp);
				for(int rpt = 0; rpt < 2; rpt++) {
					if (random.nextInt(2) != 0) continue;
					Chest c = new Chest();
					Inventory inv = c.getInventory();
					int chance = -depth;
					inv.tryAdd(9/chance, new Tnt());
					inv.tryAdd(10/chance, new Crafter(Crafter.Type.Anvil));
					inv.tryAdd(7/chance, new Lantern(Lantern.Type.NORM));
					inv.tryAdd(3/chance, Items.get("bread"), 2);
					inv.tryAdd(4/chance, Items.get("bread"), 3);
					inv.tryAdd(7/chance, Items.get("Leather Armor"), 1);
					inv.tryAdd(50/chance, Items.get("Gold Apple"), 1);
					inv.tryAdd(3/chance, Items.get("Lapis"), 2);
					inv.tryAdd(4/chance, Items.get("glass"), 2);
					inv.tryAdd(4/chance, Items.get("Gunpowder"), 3);
					inv.tryAdd(4/chance, Items.get("Gunpowder"), 3);
					inv.tryAdd(4/chance, Items.get("Torch"), 4);
					inv.tryAdd(14/chance, Items.get("swim potion"), 1);
					inv.tryAdd(16/chance, Items.get("haste potion"), 1);
					inv.tryAdd(14/chance, Items.get("light potion"), 1);
					inv.tryAdd(14/chance, Items.get("speed potion"), 1);
					inv.tryAdd(16/chance, Items.get("Iron Armor"), 1);
					inv.tryAdd(5/chance, Items.get("Stone Brick"), 4);
					inv.tryAdd(5/chance, Items.get("Stone Brick"), 6);
					inv.tryAdd(4/chance, Items.get("string"), 3);
					inv.tryAdd(4/chance, Items.get("bone"), 2);
					inv.tryAdd(3/chance, Items.get("bone"), 1);
					inv.tryAdd(7/chance, ToolType.Claymore, 1);
					inv.tryAdd(5/chance, Items.get("Torch"), 3);
					inv.tryAdd(6/chance, Items.get("Torch"), 6);
					inv.tryAdd(6/chance, Items.get("Torch"), 6);
					inv.tryAdd(7/chance, Items.get("steak"), 3);
					inv.tryAdd(9/chance, Items.get("steak"), 4);
					inv.tryAdd(7/chance, Items.get("gem"), 3);
					inv.tryAdd(7/chance, Items.get("gem"), 5);
					inv.tryAdd(7/chance, Items.get("gem"), 4);
					inv.tryAdd(10/chance, Items.get("yellow clothes"), 1);
					inv.tryAdd(10/chance, Items.get("black clothes"), 1);
					inv.tryAdd(12/chance, Items.get("orange clothes"), 1);
					inv.tryAdd(12/chance, Items.get("cyan clothes"), 1);
					inv.tryAdd(12/chance, Items.get("purple clothes"), 1);
					inv.tryAdd(4/chance, Items.get("arrow"), 5);
					
					if (inv.invSize() < 1) {
						inv.add(Items.get("potion"), 1);
						inv.add(Items.get("coal"), 3);
						inv.add(Items.get("apple"), 3);
						inv.add(Items.get("dirt"), 7);
					}
					
					// chance = -level
					add(c, sp.x - 16, sp.y - 16);
				}
			}
		}
	}
	
	public String toString() {
		return "Level(depth="+depth+")";
	}
}
