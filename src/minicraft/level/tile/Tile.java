package minicraft.level.tile;

import java.util.Random;
import java.util.HashMap;
import minicraft.entity.Entity;
import minicraft.entity.Mob;
import minicraft.entity.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Screen;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.level.Level;

public class Tile {
	public static int tickCount = 0; //A global tickCount used in the Lava & water tiles.
	protected Random random = new Random();
	
	/// idea: to save tile names while saving space, I could encode the names in base 64 in the save file...^M
    /// then, maybe, I would just replace the id numbers with id names, make them all private, and then make a get(String) method, parameter is tile name.
	
	public static HashMap<Integer, Integer> oldids = new HashMap<Integer, Integer>();
	
	public static Tile[] tiles = new Tile[256];
	public static Tile grass = new GrassTile(0); // creates a grass tile with the Id of 0, (I don't need to explain the other simple ones)
	public static Tile rock = new RockTile(1);
	public static Tile water = new WaterTile(2);
	public static Tile flower = new FlowerTile(3);
	public static Tile tree = new TreeTile(4);
	public static Tile dirt = new DirtTile(5);
	//wool
	public static Tile wool = new WoolTile(41, null);
	public static Tile redwool = new WoolTile(42, WoolTile.WoolColor.RED);
	public static Tile bluewool = new WoolTile(43, WoolTile.WoolColor.BLUE);
	public static Tile greenwool = new WoolTile(45, WoolTile.WoolColor.GREEN);
	public static Tile yellowwool = new WoolTile(127, WoolTile.WoolColor.YELLOW);
	public static Tile blackwool = new WoolTile(56, WoolTile.WoolColor.BLACK);
	public static Tile sand = new SandTile(6);
	public static Tile cactus = new CactusTile(7);
	public static Tile hole = new HoleTile(8);
	public static Tile treeSapling = new SaplingTile(9, grass, tree);
	public static Tile cactusSapling = new SaplingTile(10, sand, cactus);
	public static Tile farmland = new FarmTile(11); // farmland (tilled dirt)
	public static Tile wheat = new WheatTile(12);
	public static Tile lava = new LavaTile(13);
	public static Tile stairsDown = new StairsTile(14, false);
	public static Tile stairsUp = new StairsTile(15, true);
	public static Tile cloud = new CloudTile(17);
	public static Tile explode = new ExplodedTile(30);
	public static Tile plank = new PlankTile(31);
	public static Tile plankwall = new WoodWallTile(33);
	public static Tile stonewall = new StoneWallTile(34);
	public static Tile wdo = new WoodDoorOpenTile(35);
	public static Tile wdc = new WoodDoorClosedTile(36);
	public static Tile sdo = new StoneDoorOpenTile(37);
	public static Tile sdc = new StoneDoorClosedTile(38);
	public static Tile lavabrick = new LavaBrickTile(39);
	public static Tile sbrick = new StoneBrickTile(32);
	public static Tile o = new ObsidianBrick(120);
	public static Tile ow = new ObsidianWallTile(121);
	public static Tile odc = new ObsidianDoorClosedTile(122);
	public static Tile odo = new ObsidianDoorOpenTile(123);
	public static Tile hardRock = new HardRockTile(18);
	
	public static Tile ironOre = new OreTile(19, OreTile.OreType.IRON, Color.get(-1, 100, 322, 544));
	public static Tile lapisOre = new OreTile(24, OreTile.OreType.LAPIS, Color.get(-1, 005, 115, 115));
	public static Tile goldOre = new OreTile(20, OreTile.OreType.GOLD, Color.get(-1, 110, 440, 553));
	public static Tile gemOre = new OreTile(21, OreTile.OreType.GEM, Color.get(-1, 101, 404, 545));
	public static Tile cloudCactus = new CloudCactusTile(22); // "ore" in the sky.
	public static Tile infiniteFall = new InfiniteFallTile(16); // Air tile in the sky..?

	public final byte id;
	
	static {
		// light/torch versions, for compatibility with before 1.9.4-dev3. (were removed in making dev3)
		oldids.put(100, (int)grass.id);
		oldids.put(101, (int)sand.id);
		oldids.put(102, (int)tree.id);
		oldids.put(103, (int)cactus.id);
		oldids.put(104, (int)water.id);
		oldids.put(105, (int)dirt.id);
		oldids.put(107, (int)flower.id);
		oldids.put(108, (int)stairsUp.id);
		oldids.put(109, (int)stairsDown.id);
		oldids.put(110, (int)plank.id);
		oldids.put(111, (int)sbrick.id);
		oldids.put(112, (int)wdo.id);
		oldids.put(113, (int)wdc.id);
		oldids.put(114, (int)sdo.id);
		oldids.put(115, (int)sdc.id);
		oldids.put(116, (int)odo.id);
		oldids.put(117, (int)odc.id);
		oldids.put(119, (int)hole.id);
		oldids.put(57, (int)wool.id);
		oldids.put(58, (int)redwool.id);
		oldids.put(59, (int)bluewool.id);
		oldids.put(60, (int)greenwool.id);
		oldids.put(61, (int)yellowwool.id);
		oldids.put(62, (int)blackwool.id);
		oldids.put(63, (int)o.id);
		oldids.put(64, (int)treeSapling.id);
		oldids.put(65, (int)cactusSapling.id);
		
		oldids.put(44, grass.id-128);
		oldids.put(40, sand.id-128);
		oldids.put(46, dirt.id-128);
		oldids.put(47, plank.id-128);
		oldids.put(48, sbrick.id-128);
		oldids.put(49, o.id-128);
		oldids.put(50, wool.id-128);
		oldids.put(51, redwool.id-128);
		oldids.put(52, bluewool.id-128);
		oldids.put(53, greenwool.id-128);
		oldids.put(54, yellowwool.id-128);
		oldids.put(55, blackwool.id-128);
	}
	
	public boolean connectsToGrass = false;
	public boolean connectsToSand = false;
	public boolean connectsToLava = false;
	public boolean connectsToWater = false;
	public int light;
	public boolean maySpawn;
	
	public Tile(int id) {
		this.id = (byte) id;
		if (tiles[id] != null) throw new RuntimeException("Duplicate tile ids!"); // You cannot have over-lapping ids
		tiles[id] = this;
		
		Tile.oldids.put(id, id); // so that you don't have to check if it's already in there later
		
		light = 1;
		maySpawn = false;
	}
	
	public String setDataChar() {
		return null;
	}
	
	/** Render method, used in sub-classes */
	public void render(Screen screen, Level level, int x, int y) {}
	
	/** Returns if the player can walk on it, overrides in sub-classes  */
	public boolean mayPass(Level level, int x, int y, Entity e) {
		return true;
	}

	public boolean canLight() {
		return false;
	}
	
	/** Gets the light radius of a tile, Bigger number = bigger circle */
	public int getLightRadius(Level level, int x, int y) {
		return 0;
	}
	
	public void hurt(Level level, int x, int y, Mob source, int dmg, int attackDir) {}
	
	/** What happens when you run into the tile (ex: run into a cactus) */
	public void bumpedInto(Level level, int xt, int yt, Entity entity) {}
	
	/** Update method */
	public void tick(Level level, int xt, int yt) {}
	
	/** What happens when you are inside the tile (ex: lava) */
	public void steppedOn(Level level, int xt, int yt, Entity entity) {}
	
	/** What happens when you hit an item on a tile (ex: Pickaxe on rock) */
	public boolean interact(Level level, int xt, int yt, Player player, Item item, int attackDir) {
		return false;
	}
	
	public boolean use(Level level, int xt, int yt, Player player, int attackDir) {
		return false;
	}
	
	/** Sees if the tile connects to Water or Lava. */
	public boolean connectsToLiquid() {
		return connectsToWater || connectsToLava;
	}
	
	public boolean matches(Tile other) {
		return id == other.id;
	}
	
	public final String getName() { return getName(false); }
	public final String getName(boolean getFieldName) {
		String tileName = "";
		
		if(getFieldName) {
			java.lang.reflect.Field[] fields = getClass().getFields();
			for(java.lang.reflect.Field f: fields) {
				Tile t2 = null;
				boolean match = false;
				try {
					match = getClass().isAssignableFrom(f.getType()) && ((Tile)f.get(t2)).id == this.id;
				} catch(IllegalAccessException ex) {
					ex.printStackTrace();
				}
				if(match) {
					tileName = f.getName();
					break;
				}
			}
		}
		else {
			/// get from class name
			tileName = getClass().getName();
			tileName = tileName.substring(tileName.lastIndexOf(".")+1, tileName.lastIndexOf("Tile"));
		}
		
		return tileName;
	}
}
