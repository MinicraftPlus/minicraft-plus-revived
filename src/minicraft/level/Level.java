package minicraft.level;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import minicraft.Game;
import minicraft.entity.*;
import minicraft.gfx.Screen;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolType;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;
import minicraft.level.tile.TorchTile;
import minicraft.screen.ModeMenu;
import minicraft.screen.OptionsMenu;
import minicraft.screen.WorldSelectMenu;

public class Level {
	private Random random = new Random();

	public int w, h; // width and height of the level
	public Player player;
	
	public byte[] tiles; // an array of all the tiles in the world.
	public byte[] data; // an array of the data of the tiles in the world. // ?
	public List<Entity>[] entitiesInTiles; // An array of lists of entities in the world, by tile
	
	public int grassColor = 141;
	//public int dirtColor = 322;
	public int sandColor = 550;
	public int woolColor = 444;
	public int redwoolColor = 500;
	public int yellowwoolColor = 550;
	public int depth; // depth level of the level
	public int monsterDensity = 8; // affects the number of monsters that are on the level, bigger the number the less monsters spawn.
	public int maxMobCount;
	public int chestcount;

	public static List<String> ls = new ArrayList<String>();

	public List<Entity> entities = new ArrayList<Entity>(); // A list of all the entities in the world
	private List<Entity> rowSprites = new ArrayList<Entity>();
	private Comparator<Entity> spriteSorter = new Comparator<Entity>() { // creates a sorter for all the entities to be rendered.
		public int compare(Entity e0, Entity e1) { // compares 2 entities
			if (e1.y < e0.y) return +1; // If the y position of the first entity is less (higher up) than the second entity, then it will be moved up in sorting.
			if (e1.y > e0.y) return -1; // If the y position of the first entity is more (lower) than the second entity, then it will be moved down in sorting.
			return 0; // ends the method
		}
	};
	
	/// This is a solely debug method I made, to make printing repetetive stuff easier.
		// should be changed to accept prepend and entity, or a tile (as an Object). It will get the coordinates and class name from the object, and will divide coords by 16 if passed an entity.
	public void printLevelLoc(String prepend, int x, int y) {
		String[] levelNames = {"Sky", "Surface", "Iron", "Gold", "Lava", "Dungeon"};
		String levelName = levelNames[-1*depth+1];
		
		System.out.println(prepend + " on " + levelName + " level: x="+x + " y="+y);
	}
	
	public void printTileLocs(Tile t) {
		for(int x = 0; x < w; x++)
			for(int y = 0; y < h; y++)
				if(getTile(x, y).id == t.id)
					printLevelLoc(t.name, x, y);
	}
	public void printEntityLocs(Entity e) {
		String entityName = e.getClass().getName().replace("minicraft.entity.","");
		for(int x = 0; x < w; x++)
			for(int y = 0; y < h; y++)
				for(Entity entity: getEntities(x, y, x, y))
					if(e.getClass().isAssignableFrom(entity.getClass()))
						printLevelLoc(entityName, x, y);
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
		
		entitiesInTiles = new ArrayList[w * h]; // This is actually an array of arrayLists (of entities), with one arraylist per tile.
		for (int i = 0; i < w * h; i++) {
			entitiesInTiles[i] = new ArrayList<Entity>(); // Adds a entity list in that tile.
		}
		
		if(level != -4 && level != 0)
			monsterDensity = 4;
		
		if(!makeWorld) return;
		
		if(Game.debug) System.out.println("Making level "+level+"...");
		
		if (level == 0) maps = LevelGen.createAndValidateTopMap(w, h); // If the level is 0 (surface), create a surface map for the level
		else if (level < 0 && level > -4) { // create an undergound map
			maps = LevelGen.createAndValidateUndergroundMap(w, h, -level);
			monsterDensity = 4; // lowers the monsterDensity value, which makes more enemies spawn
		} else if (level == -4) { // create a dungeon map
			maps = LevelGen.createAndValidateDungeon(w, h);
		} else { // if level is anything else, which is just sky, then...
			maps = LevelGen.createAndValidateSkyMap(w, h); // Sky level
			monsterDensity = 4;
		}
		
		tiles = maps[0]; // assigns the tiles in the map
		data = maps[1]; // assigns the data of the tiles
		
		maxMobCount = 300 + 100*OptionsMenu.diff;
		if(depth == 0) maxMobCount = maxMobCount * 2 / 3;
		if(depth == 1 || depth == -4) maxMobCount /= 2;
		
		if (parentLevel != null) { // If the level above this one is not null (aka, if this isn't the sky level)
			for (int y = 0; y < h; y++) { // loop through height
				for (int x = 0; x < w; x++) { // loop through width
					if (parentLevel.getTile(x, y) == Tiles.get("Stairs Down")) { // If the tile in the level above the current one is a stairs down then...
						setTile(x, y, Tiles.get("Stairs Up")); // set a stairs up tile in the same position on the current level
						if (level == -4) { /// make the obsidian wall formation around the stair to the dungeon level
							setTile(x - 1, y, Tiles.get("Obsidian"));
							setTile(x + 1, y, Tiles.get("Obsidian"));
							setTile(x + 2, y, Tiles.get("Obsidian Door"));
							setTile(x - 2, y, Tiles.get("Obsidian Door"));
							setTile(x, y - 1, Tiles.get("Obsidian"));
							setTile(x, y + 1, Tiles.get("Obsidian"));
							setTile(x, y + 2, Tiles.get("Obsidian Door"));
							setTile(x, y - 2, Tiles.get("Obsidian Door"));
							setTile(x - 1, y - 1, Tiles.get("Obsidian"));
							setTile(x - 1, y + 1, Tiles.get("Obsidian"));
							setTile(x + 1, y - 1, Tiles.get("Obsidian"));
							setTile(x + 1, y + 1, Tiles.get("Obsidian"));
							setTile(x + 3, y, Tiles.get("Obsidian"));
							setTile(x - 3, y, Tiles.get("Obsidian"));
							setTile(x + 3, y - 1, Tiles.get("Obsidian"));
							setTile(x - 3, y - 1, Tiles.get("Obsidian"));
							setTile(x + 3, y + 1, Tiles.get("Obsidian"));
							setTile(x - 3, y + 1, Tiles.get("Obsidian"));
							setTile(x + 4, y, Tiles.get("Obsidian"));
							setTile(x - 4, y, Tiles.get("Obsidian"));
							setTile(x + 4, y - 1, Tiles.get("Obsidian"));
							setTile(x - 4, y - 1, Tiles.get("Obsidian"));
							setTile(x + 4, y + 1, Tiles.get("Obsidian"));
							setTile(x - 4, y + 1, Tiles.get("Obsidian"));
							setTile(x, y + 3, Tiles.get("Obsidian"));
							setTile(x, y - 3, Tiles.get("Obsidian"));
							setTile(x + 1, y - 3, Tiles.get("Obsidian"));
							setTile(x - 1, y - 3, Tiles.get("Obsidian"));
							setTile(x + 1, y + 3, Tiles.get("Obsidian"));
							setTile(x - 1, y + 3, Tiles.get("Obsidian"));
							setTile(x, y + 4, Tiles.get("Obsidian"));
							setTile(x, y - 4, Tiles.get("Obsidian"));
							setTile(x + 1, y - 4, Tiles.get("Obsidian"));
							setTile(x - 1, y - 4, Tiles.get("Obsidian"));
							setTile(x + 1, y + 4, Tiles.get("Obsidian"));
							setTile(x - 1, y + 4, Tiles.get("Obsidian"));
							setTile(x - 2, y - 2, Tiles.get("Obsidian Wall"));
							setTile(x - 3, y - 2, Tiles.get("Obsidian Wall"));
							setTile(x - 3, y + 2, Tiles.get("Obsidian Wall"));
							setTile(x - 2, y + 1, Tiles.get("Obsidian Wall"));
							setTile(x + 2, y - 2, Tiles.get("Obsidian Wall"));
							setTile(x + 4, y - 2, Tiles.get("Obsidian Wall"));
							setTile(x + 4, y + 2, Tiles.get("Obsidian Wall"));
							setTile(x - 4, y - 2, Tiles.get("Obsidian Wall"));
							setTile(x - 4, y + 2, Tiles.get("Obsidian Wall"));
							setTile(x + 1, y - 2, Tiles.get("Obsidian Wall"));
							setTile(x - 2, y + 2, Tiles.get("Obsidian Wall"));
							setTile(x + 2, y + 3, Tiles.get("Obsidian Wall"));
							setTile(x + 2, y + 4, Tiles.get("Obsidian Wall"));
							setTile(x - 2, y - 3, Tiles.get("Obsidian Wall"));
							setTile(x - 2, y - 4, Tiles.get("Obsidian Wall"));
							setTile(x + 2, y - 3, Tiles.get("Obsidian Wall"));
							setTile(x + 2, y - 4, Tiles.get("Obsidian Wall"));
							setTile(x - 2, y + 3, Tiles.get("Obsidian Wall"));
							setTile(x - 2, y + 4, Tiles.get("Obsidian Wall"));
							setTile(x + 3, y - 2, Tiles.get("Obsidian Wall"));
							setTile(x + 3, y + 2, Tiles.get("Obsidian Wall"));
							setTile(x + 2, y + 2, Tiles.get("Obsidian Wall"));
							setTile(x - 1, y + 2, Tiles.get("Obsidian Wall"));
							setTile(x + 2, y - 1, Tiles.get("Obsidian Wall"));
							setTile(x + 2, y + 1, Tiles.get("Obsidian Wall"));
							setTile(x + 1, y + 2, Tiles.get("Obsidian Wall"));
							setTile(x - 2, y - 1, Tiles.get("Obsidian Wall"));
							setTile(x - 1, y - 2, Tiles.get("Obsidian Wall"));
						}
						if (level == 0) { // surface
							// TODO do it with this kind of approach.
							//setAreaTiles(x, y, 1, Tiles.get("Hard Rock"));
							//setTile(x, y, Tiles.get("Stairs Up"));
							/// surround the sky stairs with hard rock:
							setTile(x - 1, y, Tiles.get("Hard Rock"));
							setTile(x + 1, y, Tiles.get("Hard Rock"));
							setTile(x, y - 1, Tiles.get("Hard Rock"));
							setTile(x, y + 1, Tiles.get("Hard Rock"));
							setTile(x - 1, y - 1, Tiles.get("Hard Rock"));
							setTile(x - 1, y + 1, Tiles.get("Hard Rock"));
							setTile(x + 1, y - 1, Tiles.get("Hard Rock"));
							setTile(x + 1, y + 1, Tiles.get("Hard Rock"));
						}

						if (level != 0 && level != -4) {
							// any other level, the up-stairs should have dirt on all sides.
							setTile(x - 1, y, Tiles.get("dirt"));
							setTile(x + 1, y, Tiles.get("dirt"));
							setTile(x, y - 1, Tiles.get("dirt"));
							setTile(x, y + 1, Tiles.get("dirt"));
							setTile(x - 1, y - 1, Tiles.get("dirt"));
							setTile(x - 1, y + 1, Tiles.get("dirt"));
							setTile(x + 1, y - 1, Tiles.get("dirt"));
							setTile(x + 1, y + 1, Tiles.get("dirt"));
						}
					}
				}
			}
		}

		
		/// if the level is the dungeon, and we're not just loading the world...
		if (level == -4) {
			for (int i = 0; i < 10 * (w / 128); i++) {
				/// make DungeonChests!
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
						} else if (!xaxis) {
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
						chestcount++;
						addedchest = true;
					}
				}
			}
		}
		if (level < 0) {
			for (int i = 0; i < 18 / -level * (w / 128); i++) {
				/// for generating spawner dungeons
				MobAi m = null;
				int r = random.nextInt(5);
				if (r == 1) {
					m = new Skeleton(-level);
				} else if (r == 2 || r == 0) {
					m = new Slime(-level);
				} else {
					m = new Zombie(-level);
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
						Inventory inv = c.inventory;
						int chance = -level;
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

		if (level == 1) {
			AirWizard aw = new AirWizard(false);
			add(aw, w * 16 / 2, h * 16 / 2);
		}
		
		if (Game.debug) printTileLocs(Tiles.get("Stairs Down"));
	}

	public void tick() {
		int count = 0;
		
		for (int i = 0; i < w * h / 50; i++) {
			int xt = random.nextInt(w);
			int yt = random.nextInt(w);
			getTile(xt, yt).tick(this, xt, yt);
		}
		for (int i = 0; i < entities.size(); i++) {
			Entity e = entities.get(i);
			int xto = e.x >> 4;
			int yto = e.y >> 4;
			
			e.tick();
			
			if(e instanceof Mob) count++;
			
			int xt = e.x >> 4;
			int yt = e.y >> 4;
			
			/// this moves the entity's position in entitiesInTiles, since it's on a different tile now.
			if (xto != xt || yto != yt) {
				removeEntity(xto, yto, e);
				insertEntity(xt, yt, e);
			}
		}
		for(int i = 0; i < entities.size(); i++) {
			if(entities.get(i).removed) {// remove entites tagged for removal.
				remove(entities.get(i));
				i--;
			}
		}
		
		//if(Game.tickCount % 10 == 0) System.out.println("mob count: " + count);
		
		if(count < maxMobCount)
			trySpawn(1);
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
	public void dropItem(int x, int y, Item i) {
		add(new minicraft.entity.ItemEntity(i, x + random.nextInt(11) - 5, y + random.nextInt(11) - 5));
	}

	public void renderBackground(Screen screen, int xScroll, int yScroll) {
		int xo = xScroll >> 4; // latches to the nearest tile coordinate
		int yo = yScroll >> 4;
		int w = (screen.w) >> 4; // there used to be a "+15" as in below method
		int h = (screen.h) >> 4;
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
		int w = (screen.w + 15) >> 4;
		int h = (screen.h + 15) >> 4;

		screen.setOffset(xScroll, yScroll);
		for (int y = yo; y <= h + yo; y++) {
			for (int x = xo; x <= w + xo; x++) {
				if (x < 0 || y < 0 || x >= this.w || y >= this.h) continue;
				rowSprites.addAll(entitiesInTiles[x + y * this.w]);
			}
			if (rowSprites.size() > 0) {
				sortAndRender(screen, rowSprites);
			}
			rowSprites.clear();
		}
		screen.setOffset(0, 0);
	}

	public void renderLight(Screen screen, int xScroll, int yScroll) {
		int xo = xScroll >> 4;
		int yo = yScroll >> 4;
		int w = (screen.w + 15) >> 4;
		int h = (screen.h + 15) >> 4;

		screen.setOffset(xScroll, yScroll);
		int r = 4;
		for (int y = yo - r; y <= h + yo + r; y++) {
			for (int x = xo - r; x <= w + xo + r; x++) {
				if (x < 0 || y < 0 || x >= this.w || y >= this.h) continue;
				
				List<Entity> entities = entitiesInTiles[x + y * this.w];
				for (int i = 0; i < entities.size(); i++) {
					Entity e = entities.get(i);
					int lr = e.getLightRadius();
					if (lr > 0) screen.renderLight(e.x - 1, e.y - 4, lr * 8);
				}
				int lr = getTile(x, y).getLightRadius(this, x, y);
				if (lr > 0) screen.renderLight( x * 16 + 8, y * 16 + 8, lr * (player.potioneffects.containsKey("Light") ? 12 : 8)); // brightens all light sources by a factor of 1.5 when the player has the Light potion effect. (8 above is normal)
			}
		}
		screen.setOffset(0, 0);
	}
	
	private void sortAndRender(Screen screen, List<Entity> list) {
		Collections.sort(list, spriteSorter);
		for (int i = 0; i < list.size(); i++) {
			Entity e = list.get(i);
			if(e.level == this && !e.removed)
				e.render(screen);
			else
				remove(e);
		}
	}
	
	public Tile getTile(int x, int y) {
		if (x < 0 || y < 0 || x >= w || y >= h) return Tiles.get("rock");
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
		tiles[x + y * w] = t.id;
		data[x + y * w] = (byte) dataVal;
	}
	
	public int getData(int x, int y) {
		if (x < 0 || y < 0 || x >= w || y >= h) return 0;
		return data[x + y * w] & 0xff;
	}

	public void setData(int x, int y, int val) {
		if (x < 0 || y < 0 || x >= w || y >= h) return;
		data[x + y * w] = (byte) val;
	}
	
	public void add(Entity e) { add(e, e.x, e.y); }
	public void add(Entity entity, int x, int y) {
		if (entity instanceof Player) {
			player = (Player) entity;
		}
		entities.add(entity);
		entity.setLevel(this, x, y);
		
		if (Game.debug) {
			String clazz = entity.getClass().getCanonicalName();
			clazz = clazz.substring(clazz.lastIndexOf(".")+1);
			String[] searching = {"DungeonChest", "AirWizard"}; //can contain any number of class names I want to print when found.
			for(String search: searching) {
				try {
					if(Class.forName("minicraft.entity."+search).isAssignableFrom(entity.getClass())) {
						if (clazz.equals("AirWizard")) clazz += ((AirWizard)entity).secondform ? " II" : "";
						printLevelLoc("Adding " + clazz, entity.x>>4, entity.y>>4);
						break;
					}
				} catch(ClassNotFoundException ex) {
					ex.printStackTrace();
				}
			}
		}
		
		insertEntity(entity.x >> 4, entity.y >> 4, entity);
	}
	
	public void remove(Entity e) {
		entities.remove(e);
		e.level = null;
		int xto = e.x >> 4;
		int yto = e.y >> 4;
		removeEntity(xto, yto, e);
	}

	public void insertEntity(int x, int y, Entity e) {
		if (x < 0 || y < 0 || x >= w || y >= h) return;
		entitiesInTiles[x + y * w].add(e);
	}

	private void removeEntity(int x, int y, Entity e) {
		if (x < 0 || y < 0 || x >= w || y >= h) return;
		entitiesInTiles[x + y * w].remove(e);
	}

	public void trySpawn(int count) {
		for (int i = 0; i < count; i++) {
			int minLevel = 1, maxLevel = 1;
			if (depth < 0) {
				maxLevel = (-depth) + 1;
			}
			if (depth > 0) {
				minLevel = maxLevel = 4;
			}

			int lvl = random.nextInt(maxLevel - minLevel + 1) + minLevel;
			int rnd = random.nextInt(100);
			int nx = random.nextInt(w) * 16 + 8, ny = random.nextInt(h) * 16 + 8;
			
			//System.out.println("trySpawn on level " + depth + " of lvl " + lvl + " mob w/ rand " + rnd + " at tile " + nx + "," + ny);
			
			// spawns the enemy mobs; first part prevents enemy mob spawn on surface on first day, more or less.
			if ((Game.time == 3 && Game.pastDay1 || depth != 0) && EnemyMob.checkStartPos(this, nx, ny)) { // if night or underground, with a valid tile, spawn an enemy mob.
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
			}
			
			if(depth == 0 && PassiveMob.checkStartPos(this, nx, ny)) {
				// spawns the friendly mobs.
				if (rnd <= (Game.time==3?22:33)) add((new Cow()), nx, ny);
				else if (rnd >= 68) add((new Pig()), nx, ny);
				else add((new Sheep()), nx, ny);
			}
		}
	}

	public void removeAllEnemies() {
		for (int i = 0; i < entities.size(); i++) {
			Entity e = entities.get(i);
			if(e instanceof EnemyMob)
				if(e instanceof AirWizard == false || ModeMenu.creative) // don't remove the airwizard bosses! Unless in creative, since you can spawn more.
					e.remove();
		}
	}

	public List<Entity> getEntities(int x0, int y0, int x1, int y1) {
		List<Entity> result = new ArrayList<Entity>();
		int xt0 = (x0 >> 4) - 1;
		int yt0 = (y0 >> 4) - 1;
		int xt1 = (x1 >> 4) + 1;
		int yt1 = (y1 >> 4) + 1;
		for (int y = yt0; y <= yt1; y++) {
			for (int x = xt0; x <= xt1; x++) {
				if (x < 0 || y < 0 || x >= w || y >= h) continue;
				List<Entity> entities = entitiesInTiles[x + y * w];
				for (int i = 0; i < entities.size(); i++) {
					Entity e = entities.get(i);
					if (e.intersects(x0, y0, x1, y1)) result.add(e);
				}
			}
		}
		return result;
	}
	
	public Tile[] getAreaTiles(int x, int y, int r) {
		ArrayList<Tile> local = new ArrayList<Tile>();
		for(int yo = y-r; yo <= y+r; yo++)
			for(int xo = x-r; xo <= x+r; xo++)
				if(xo >= 0 && xo < w && yo >= 0 && yo < h)
					local.add(getTile(xo, yo));
		
		return local.toArray(new Tile[0]);
	}
	
	public void setAreaTiles(int xt, int yt, int r, Tile tile, int data) {
		for(int y = yt-r; y <= yt+r; y++)
			for(int x = xt-r; x <= xt+r; x++)
				setTile(x, y, tile, data);
	}
	
	public List<Point> getMatchingTiles(Tile search) {
		List<Point> matches = new ArrayList<Point>();
		for(int y = 0; y < h; y++)
			for(int x = 0; x < w; x++)
				if(getTile(x, y) == search)
					matches.add(new Point(x, y));
		
		return matches;
	}
	
	public boolean isLight(int x, int y) {
		for(Tile t: getAreaTiles(x, y, 3))
			if(t instanceof TorchTile)
				return true;
		
		return false;
	}
	
	public boolean noStairs(int x, int y) {
		return getTile(x, y) != Tiles.get("Stairs Down");
	}
}
