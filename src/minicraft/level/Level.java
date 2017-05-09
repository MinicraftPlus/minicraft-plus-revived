package minicraft.level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import minicraft.Game;
import minicraft.entity.AirWizard;
import minicraft.entity.Anvil;
import minicraft.entity.Chest;
import minicraft.entity.Cow;
import minicraft.entity.Creeper;
import minicraft.entity.DungeonChest;
import minicraft.entity.EnemyMob;
import minicraft.entity.Entity;
import minicraft.entity.Inventory;
import minicraft.entity.Knight;
import minicraft.entity.Lantern;
import minicraft.entity.PassiveMob;
import minicraft.entity.Pig;
import minicraft.entity.Player;
import minicraft.entity.Sheep;
import minicraft.entity.Skeleton;
import minicraft.entity.Slime;
import minicraft.entity.Snake;
import minicraft.entity.Spawner;
import minicraft.entity.Tnt;
import minicraft.entity.Zombie;
import minicraft.gfx.Screen;
import minicraft.item.FurnitureItem;
import minicraft.item.ResourceItem;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.item.resource.Resource;
import minicraft.level.LevelGen;
import minicraft.level.tile.DirtTile;
import minicraft.level.tile.Tile;
import minicraft.level.tile.TorchTile;
import minicraft.screen.ModeMenu;
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
	private void printLevelLoc(String prepend, int x, int y) {
		String[] levelNames = {"Sky", "Surface", "Iron", "Gold", "Lava", "Dungeon"};
		String levelName = levelNames[-1*depth+1];
		
		System.out.println(prepend + " on " + levelName + " level: x="+x + " y="+y);
	}
	
	private void printTileLocs(Tile t) {
		java.lang.reflect.Field[] fields = Tile.class.getFields();
		String tileName = "";
		for(java.lang.reflect.Field f: fields) {
			Tile t2 = null;
			boolean match = false;
			try {
				match = Tile.class.isAssignableFrom(f.getType()) && ((Tile)f.get(t2)).id == t.id;
			} catch(IllegalAccessException ex) {
				ex.printStackTrace();
			}
			if(match) {
				tileName = f.getName();
				break;
			}
		}
		for(int x = 0; x < w; x++)
			for(int y = 0; y < h; y++)
				if(getTile(x, y).id == t.id)
					printLevelLoc(tileName, x, y);
	}
	private void printEntityLocs(Entity e) {
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
		// set the dirt colors
		/*if (level != 0) {
			if (DirtTile.dirtc == 0) {
				dirtColor = 222;
				DirtTile.dirtc++;
			}
			if (DirtTile.dirtc == 1) {
				dirtColor = 222;
			}
		}
		if (level == 0) {
			if (DirtTile.dirtc == 0) {
				dirtColor = 322;
			}
			if (DirtTile.dirtc == 0) {
				if (level == 0) {
					dirtColor = 322;
				}
				if (level != 0) {
					dirtColor = 222;
				}
				DirtTile.dirtc++;
			}
		}
		
		
		if (level == 1) {
			dirtColor = 444;
		}*/
		
		//dirtColor = DirtTile.dCol(depth);
		
		entitiesInTiles = new ArrayList[w * h]; // This is actually an array of arrayLists (of entities), with one arraylist per tile.
		for (int i = 0; i < w * h; i++) {
			entitiesInTiles[i] = new ArrayList<Entity>(); // Adds a entity list in that tile.
		}
		
		if(level != -4 && level != 0)
			monsterDensity = 4;
		
		if(makeWorld) {
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
		
		if (parentLevel != null) { // If the level above this one is not null (aka, if this isn't the sky level)
			for (int y = 0; y < h; y++) { // loop through height
				for (int x = 0; x < w; x++) { // loop through width
					if (parentLevel.getTile(x, y) == Tile.stairsDown) { // If the tile in the level above the current one is a stairs down then...
						setTile(x, y, Tile.stairsUp, 0); // set a stairs up tile in the same position on the current level
						if (level == -4) { /// make the obsidian wall formation around the stair to the dungeon level
							setTile(x - 1, y, Tile.o, 0);
							setTile(x + 1, y, Tile.o, 0);
							setTile(x + 2, y, Tile.odc, 0);
							setTile(x - 2, y, Tile.odc, 0);
							setTile(x, y - 1, Tile.o, 0);
							setTile(x, y + 1, Tile.o, 0);
							setTile(x, y + 2, Tile.odc, 0);
							setTile(x, y - 2, Tile.odc, 0);
							setTile(x - 1, y - 1, Tile.o, 0);
							setTile(x - 1, y + 1, Tile.o, 0);
							setTile(x + 1, y - 1, Tile.o, 0);
							setTile(x + 1, y + 1, Tile.o, 0);
							setTile(x + 3, y, Tile.o, 0);
							setTile(x - 3, y, Tile.o, 0);
							setTile(x + 3, y - 1, Tile.o, 0);
							setTile(x - 3, y - 1, Tile.o, 0);
							setTile(x + 3, y + 1, Tile.o, 0);
							setTile(x - 3, y + 1, Tile.o, 0);
							setTile(x + 4, y, Tile.o, 0);
							setTile(x - 4, y, Tile.o, 0);
							setTile(x + 4, y - 1, Tile.o, 0);
							setTile(x - 4, y - 1, Tile.o, 0);
							setTile(x + 4, y + 1, Tile.o, 0);
							setTile(x - 4, y + 1, Tile.o, 0);
							setTile(x, y + 3, Tile.o, 0);
							setTile(x, y - 3, Tile.o, 0);
							setTile(x + 1, y - 3, Tile.o, 0);
							setTile(x - 1, y - 3, Tile.o, 0);
							setTile(x + 1, y + 3, Tile.o, 0);
							setTile(x - 1, y + 3, Tile.o, 0);
							setTile(x, y + 4, Tile.o, 0);
							setTile(x, y - 4, Tile.o, 0);
							setTile(x + 1, y - 4, Tile.o, 0);
							setTile(x - 1, y - 4, Tile.o, 0);
							setTile(x + 1, y + 4, Tile.o, 0);
							setTile(x - 1, y + 4, Tile.o, 0);
							setTile(x - 2, y - 2, Tile.ow, 0);
							setTile(x - 3, y - 2, Tile.ow, 0);
							setTile(x - 3, y + 2, Tile.ow, 0);
							setTile(x - 2, y + 1, Tile.ow, 0);
							setTile(x + 2, y - 2, Tile.ow, 0);
							setTile(x + 4, y - 2, Tile.ow, 0);
							setTile(x + 4, y + 2, Tile.ow, 0);
							setTile(x - 4, y - 2, Tile.ow, 0);
							setTile(x - 4, y + 2, Tile.ow, 0);
							setTile(x + 1, y - 2, Tile.ow, 0);
							setTile(x - 2, y + 2, Tile.ow, 0);
							setTile(x + 2, y + 3, Tile.ow, 0);
							setTile(x + 2, y + 4, Tile.ow, 0);
							setTile(x - 2, y - 3, Tile.ow, 0);
							setTile(x - 2, y - 4, Tile.ow, 0);
							setTile(x + 2, y - 3, Tile.ow, 0);
							setTile(x + 2, y - 4, Tile.ow, 0);
							setTile(x - 2, y + 3, Tile.ow, 0);
							setTile(x - 2, y + 4, Tile.ow, 0);
							setTile(x + 3, y - 2, Tile.ow, 0);
							setTile(x + 3, y + 2, Tile.ow, 0);
							setTile(x + 2, y + 2, Tile.ow, 0);
							setTile(x - 1, y + 2, Tile.ow, 0);
							setTile(x + 2, y - 1, Tile.ow, 0);
							setTile(x + 2, y + 1, Tile.ow, 0);
							setTile(x + 1, y + 2, Tile.ow, 0);
							setTile(x - 2, y - 1, Tile.ow, 0);
							setTile(x - 1, y - 2, Tile.ow, 0);
						}
						if (level == 0) { // surface
							/// surround the sky stairs with hard rock:
							setTile(x - 1, y, Tile.hardRock, 0);
							setTile(x + 1, y, Tile.hardRock, 0);
							setTile(x, y - 1, Tile.hardRock, 0);
							setTile(x, y + 1, Tile.hardRock, 0);
							setTile(x - 1, y - 1, Tile.hardRock, 0);
							setTile(x - 1, y + 1, Tile.hardRock, 0);
							setTile(x + 1, y - 1, Tile.hardRock, 0);
							setTile(x + 1, y + 1, Tile.hardRock, 0);
						}

						if (level != 0 && level != -4) {
							// any other level, the up-stairs should have dirt on all sides.
							setTile(x - 1, y, Tile.dirt, 0);
							setTile(x + 1, y, Tile.dirt, 0);
							setTile(x, y - 1, Tile.dirt, 0);
							setTile(x, y + 1, Tile.dirt, 0);
							setTile(x - 1, y - 1, Tile.dirt, 0);
							setTile(x - 1, y + 1, Tile.dirt, 0);
							setTile(x + 1, y - 1, Tile.dirt, 0);
							setTile(x + 1, y + 1, Tile.dirt, 0);
						}
					}
				}
			}
		}

		
		/// if the level is the dungeon, and we're not just loading the world...
		if (level == -4 && !WorldSelectMenu.loadworld) {
			for (int i = 0; i < 10 * (w / 128); i++) {
				/// make DungeonChests!
				DungeonChest d = new DungeonChest();
				boolean addedchest = false;
				while(!addedchest) { // keep running until we successfully add a DungeonChest
					//pick a random tile:
					int x2 = this.random.nextInt(16 * w) / 16;
					int y2 = this.random.nextInt(16 * h) / 16;
					if (this.getTile(x2, y2) == Tile.o) {
						boolean xaxis = this.random.nextBoolean();
						if (xaxis) {
							for (int s = x2; s < w - s; s++) {
								if (this.getTile(s, y2) == Tile.ow) {
									d.x = s * 16 - 24;
									d.y = y2 * 16 - 24;
								}
							}
						} else if (!xaxis) {
							for (int s = y2; s < y2 - s; s++) {
								if (this.getTile(x2, s) == Tile.ow) {
									d.x = x2 * 16 - 24;
									d.y = s * 16 - 24;
								}
							}
						}
						if (d.x == 0 && d.y == 0) {
							d.x = x2 * 16 - 8;
							d.y = y2 * 16 - 8;
						}
						if (this.getTile(d.x / 16, d.y / 16) == Tile.ow) {
							this.setTile(d.x / 16, d.y / 16, Tile.o, 0);
						}
						this.add(d);
						this.chestcount++;
						addedchest = true;
					}
				}
			}
		}
		if (level < 0 && !WorldSelectMenu.loadworld) {
			for (int i = 0; i < 18 / -level * (w / 128); i++) {
				/// for generating spawner dungeons
				String m = "";
				int r = this.random.nextInt(5);
				if (r == 1) {
					m = "Skeleton";
				} else if (r == 2 || r == 0) {
					m = "Slime";
				} else {
					m = "Zombie";
				}
				
				Spawner sp = new Spawner(m, -level);
				int x3 = this.random.nextInt(16 * w) / 16;
				int y3 = this.random.nextInt(16 * h) / 16;
				if (this.getTile(x3, y3) == Tile.dirt) {
					boolean xaxis2 = this.random.nextBoolean();
					
					if (xaxis2) {
						for (int s2 = x3; s2 < w - s2; s2++) {
							if (this.getTile(s2, y3) == Tile.rock) {
							sp.x = s2 * 16 - 24;
							sp.y = y3 * 16 - 24;
							}
						}
					} else {
						for (int s2 = y3; s2 < y3 - s2; s2++) {
							if (this.getTile(x3, s2) == Tile.rock) {
							sp.x = x3 * 16 - 24;
							sp.y = s2 * 16 - 24;
							}
						}
					}
					
					if (sp.x == 0 && sp.y == 0) {
							sp.x = x3 * 16 - 8;
							sp.y = y3 * 16 - 8;
					}

					if (this.getTile(sp.x / 16, sp.y / 16) == Tile.rock) {
						this.setTile(sp.x / 16, sp.y / 16, Tile.dirt, 0);
					}

					for (int xx = 0; xx < 5; xx++) {
						for (int yy = 0; yy < 5; yy++) {
							if (this.noStairs(sp.x / 16 - 2 + xx, sp.y / 16 - 2 + yy)) {
								this.setTile(sp.x / 16 - 2 + xx, sp.y / 16 - 2 + yy, Tile.sbrick, 0);

								if((xx < 1 || yy < 1 || xx > 3 || yy > 3) && (xx != 2 || yy != 0) && (xx != 2 || yy != 4) && (xx != 0 || yy != 2) && (xx != 4 || yy != 2)) {
									this.setTile(sp.x / 16 - 2 + xx, sp.y / 16 - 2 + yy, Tile.stonewall, 0);
								}
							}
						}
					}

					this.add(sp);
					for(int rpt = 0; rpt < 2; rpt++) {
						if (random.nextInt(2) != 0) continue;
						Chest c = new Chest();
						Inventory inv = c.inventory;
						int chance = -level;
						inv.tryAdd(9/chance, new Tnt());
				  		inv.tryAdd(10/chance, new Anvil());
				  		inv.tryAdd(7/chance, new Lantern(Lantern.Type.NORM));
				  		inv.tryAdd(3/chance, Resource.bread, 2);
				  		inv.tryAdd(4/chance, Resource.bread, 3);
				  		inv.tryAdd(7/chance, Resource.larmor, 1);
				  		inv.tryAdd(50/chance, Resource.goldapple, 1);
				  		inv.tryAdd(3/chance, Resource.lapisOre, 2);
				  		inv.tryAdd(4/chance, Resource.glass, 2);
				  		inv.tryAdd(4/chance, Resource.gunp, 3);
				  		inv.tryAdd(4/chance, Resource.gunp, 3);
				  		inv.tryAdd(4/chance, Resource.torch, 4);
				  		inv.tryAdd(14/chance, Resource.swimpotion, 1);
				  		inv.tryAdd(16/chance, Resource.hastepotion, 1);
				  		inv.tryAdd(14/chance, Resource.lightpotion, 1);
				  		inv.tryAdd(14/chance, Resource.speedpotion, 1);
				  		inv.tryAdd(16/chance, Resource.iarmor, 1);
				  		inv.tryAdd(5/chance, Resource.sbrick, 4);
				  		inv.tryAdd(5/chance, Resource.sbrick, 6);
				  		inv.tryAdd(4/chance, Resource.string, 3);
				  		inv.tryAdd(4/chance, Resource.bone, 2);
				  		inv.tryAdd(3/chance, Resource.bone, 1);
				  		inv.tryAdd(6/chance, ToolType.hatchet, 2);
				  		inv.tryAdd(6/chance, ToolType.pick, 2);
				  		inv.tryAdd(6/chance, ToolType.spade, 2);
				  		inv.tryAdd(7/chance, ToolType.claymore, 1);
				  		inv.tryAdd(5/chance, Resource.torch, 3);
				  		inv.tryAdd(6/chance, Resource.torch, 6);
						inv.tryAdd(6/chance, Resource.torch, 6);
				  		inv.tryAdd(7/chance, Resource.steak, 3);
				  		inv.tryAdd(9/chance, Resource.steak, 4);
				  		inv.tryAdd(7/chance, Resource.gem, 3);
				  		inv.tryAdd(7/chance, Resource.gem, 5);
				  		inv.tryAdd(7/chance, Resource.gem, 4);
				  		inv.tryAdd(10/chance, Resource.yellowclothes, 1);
				  		inv.tryAdd(10/chance, Resource.blackclothes, 1);
				  		inv.tryAdd(12/chance, Resource.orangeclothes, 1);
				  		inv.tryAdd(12/chance, Resource.cyanclothes, 1);
				  		inv.tryAdd(12/chance, Resource.purpleclothes, 1);
				  		inv.tryAdd(4/chance, Resource.arrow, 5);
						if (inv.invSize() < 1) {
							inv.add(new ResourceItem(Resource.potion, 1));
							inv.add(new ResourceItem(Resource.coal, 3));
							inv.add(new ResourceItem(Resource.apple, 3));
							inv.add(new ResourceItem(Resource.dirt, 7));
				  		}
						
						// chance = -level
						this.add(c, sp.x - 16, sp.y - 16);
					}
				}
			}
		}

		if (level == 1 && !WorldSelectMenu.loadworld) {
			AirWizard aw = new AirWizard(false);
			add(aw, w * 16 / 2, h * 16 / 2);
		}
		
		if(Game.debug) {
			// print stair locations
			for (int x = 0; x < w; x++)
				for (int y = 0; y < h; y++)
					if(getTile(x, y) == Tile.stairsDown)
						printLevelLoc("Stairs down", x, y);
			System.out.println();
		}
		}
	}

	public void tick() {
		trySpawn(1);
		
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
	}
	
	public void dropResource(int x, int y, int count, Resource... resources) {
		for (int i = 0; i < count; i++)
			dropResource(x, y, resources);
	}
	public void dropResource(int x, int y, Resource[] resources) {
		for(Resource r: resources)
			dropResource(x, y, r);
	}
	public void dropResource(int x, int y, Resource r) {
		add(new minicraft.entity.ItemEntity(new ResourceItem(r), x + random.nextInt(11) - 5, y + random.nextInt(11) - 5));
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
		if (x < 0 || y < 0 || x >= w || y >= h) return Tile.rock;
		int id = tiles[x + y * w];
		if(id < 0) id += 256;
		return Tile.tiles[id];
	}

	public void setTile(int x, int y, Tile t, int dataVal) {
		if (x < 0 || y < 0 || x >= w || y >= h) return;
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
				//System.out.println("adding enemy mob...");
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
				//System.out.println("adding passive mob...");
				if (rnd <= (Game.time==3?22:33)) add((new Cow()), nx, ny);
				else if (rnd >= 68) add((new Pig()), nx, ny);
				else add((new Sheep()), nx, ny);
			}
		}
	}

	public void removeAllEnemies() {
		for (int i = 0; i < this.entities.size(); i++) {
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
	
	public boolean isLight(int x, int y) {
		for(Tile t: getAreaTiles(x, y, 3))
			if(t instanceof TorchTile)
				return true;
		
		return false;
	}
	
	public boolean noStairs(int x, int y) {
		return getTile(x, y) != Tile.stairsDown;
	}
}
