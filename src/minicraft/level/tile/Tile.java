package minicraft.level.tile;

import java.util.Random;
import minicraft.entity.Entity;
import minicraft.entity.Mob;
import minicraft.entity.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Screen;
import minicraft.item.Item;
import minicraft.item.resource.Resource;
import minicraft.level.Level;

public class Tile {
	public enum OreType {
        IRON, LAPIS, GOLD, GEM
    }
	
	public static int tickCount = 0; //A global tickCount used in the Lava & water tiles.
	protected Random random = new Random();
	
	/// idea: to save tile names while saving space, I could encode the names in base 64 in the save file...^M
    /// then, maybe, I would just replace the id numbers with id names, make them all private, and then make a get(String) method, parameter is tile name.
	
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
	
	// light/torch versions; ALL are ONLY used to render torch effects. Don't worry, I can fix that. ;)
	public static Tile lightgrass = new LightTile(100, grass, 0);
	public static Tile lightsand = new LightTile(101, sand, 1);
	public static Tile lighttree = new LightTile(102, tree, 2);
	public static Tile lightcac = new LightTile(103, cactus, 3);
	public static Tile lightwater = new LightTile(104, water, 4);
	public static Tile lightdirt = new LightTile(105, dirt, 5);
	public static Tile lightflower = new LightTile(107, flower, 6);
	public static Tile lightstairsUp = new LightTile(108, stairsUp, 7);
	public static Tile lightstairsDown = new LightTile(109, stairsDown, 8);
	public static Tile lightplank = new LightTile(110, plank, 9);
	public static Tile lightsbrick = new LightTile(111, sbrick, 10);
	public static Tile lwdo = new LightTile(112, wdo, 11);
	public static Tile lwdc = new LightTile(113, wdc, 12);
	public static Tile lsdo = new LightTile(114, sdo, 13);
	public static Tile lsdc = new LightTile(115, sdc, 14);
	public static Tile lodo = new LightTile(116, odo, 15);
	public static Tile lodc = new LightTile(117, odc, 16);
	public static Tile lighthole = new LightTile(119, hole, 17);
	public static Tile lightwool = new LightTile(57, wool, 18);
	public static Tile lightrwool = new LightTile(58, redwool, 19);
	public static Tile lightbwool = new LightTile(59, bluewool, 20);
	public static Tile lightgwool = new LightTile(60, greenwool, 21);
	public static Tile lightywool = new LightTile(61, yellowwool, 22);
	public static Tile lightblwool = new LightTile(62, blackwool, 23);
	public static Tile lighto = new LightTile(63, o, 24);
	public static Tile lightts = new LightTile(64, treeSapling, 25);
	public static Tile lightcs = new LightTile(65, cactusSapling, 26);

	public static Tile torchgrass = new TorchTile(44, lightgrass);
	public static Tile torchsand = new TorchTile(40, lightsand);
	public static Tile torchdirt = new TorchTile(46, lightdirt);
	public static Tile torchplank = new TorchTile(47, lightplank);
	public static Tile torchsbrick = new TorchTile(48, lightsbrick);
	public static Tile torchlo = new TorchTile(49, lighto);
	public static Tile torchwool = new TorchTile(50, wool);
	public static Tile torchwoolred = new TorchTile(51, lightrwool);
	public static Tile torchwoolblue = new TorchTile(52, lightbwool);
	public static Tile torchwoolgreen = new TorchTile(53, lightgwool);
	public static Tile torchwoolyellow = new TorchTile(54, lightywool);
	public static Tile torchwoolblack = new TorchTile(55, lightblwool);

	public static Tile ironOre = new OreTile(19, OreType.IRON, Color.get(-1, 100, 322, 544));
	public static Tile lapisOre = new OreTile(24, OreType.LAPIS, Color.get(-1, 005, 115, 115));
	public static Tile goldOre = new OreTile(20, OreType.GOLD, Color.get(-1, 110, 440, 553));
	public static Tile gemOre = new OreTile(21, OreType.GEM, Color.get(-1, 101, 404, 545));
	public static Tile cloudCactus = new CloudCactusTile(22); // "ore" in the sky.
	public static Tile infiniteFall = new InfiniteFallTile(16); // Air tile in the sky.

	public final byte id;

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
	public void bumpedInto(Level level, int xt, int yt, Entity entity) {
	}
	
	/** Update method */
	public void tick(Level level, int xt, int yt) {
	}
	
	/** What happens when you are inside the tile (ex: lava) */
	public void steppedOn(Level level, int xt, int yt, Entity entity) {
	}
	
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
}
