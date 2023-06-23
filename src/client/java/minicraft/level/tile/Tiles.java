package minicraft.level.tile;

import minicraft.core.CrashHandler;
import minicraft.level.tile.farming.FarmTile;
import minicraft.level.tile.farming.PotatoTile;
import minicraft.level.tile.farming.WheatTile;
import minicraft.util.Logging;

import java.util.HashMap;

public final class Tiles {
	/// Idea: to save tile names while saving space, I could encode the names in base 64 in the save file...^M
	/// Then, maybe, I would just replace the id numbers with id names, make them all private, and then make a get(String) method, parameter is tile name.
	// Suggestion: similar to Items, making public static final fields and registration system to reduce runtime problems and for standardization.

	public static final HashMap<Short, String> oldids = new HashMap<>();

	private static final HashMap<Short, Tile> tiles = new HashMap<>();

	public static void initTileList() {
		Logging.TILES.debug("Initializing tile list...");

		tiles.put((short)0, new GrassTile("Grass"));
		tiles.put((short)1, new DirtTile("Dirt"));
		tiles.put((short)2, new FlowerTile("Flower"));
		tiles.put((short)3, new HoleTile("Hole"));
		tiles.put((short)4, new StairsTile("Stairs Up", true));
		tiles.put((short)5, new StairsTile("Stairs Down", false));
		tiles.put((short)6, new WaterTile("Water"));
		// This is out of order because of lava buckets
		tiles.put((short)17, new LavaTile("Lava"));

		tiles.put((short)7, new RockTile("Rock"));
		tiles.put((short)8, new TreeTile("Tree"));
		tiles.put((short)9, new SaplingTile("Tree Sapling", Tiles.get("Grass"), Tiles.get("Tree")));
		tiles.put((short)10, new SandTile("Sand"));
		tiles.put((short)11, new CactusTile("Cactus"));
		tiles.put((short)12, new SaplingTile("Cactus Sapling", Tiles.get("Sand"), Tiles.get("Cactus")));
		tiles.put((short)13, new OreTile(OreTile.OreType.Iron));
		tiles.put((short)14, new OreTile(OreTile.OreType.Gold));
		tiles.put((short)15, new OreTile(OreTile.OreType.Gem));
		tiles.put((short)16, new OreTile(OreTile.OreType.Lapis));
		tiles.put((short)18, new LavaBrickTile("Lava Brick"));
		tiles.put((short)19, new ExplodedTile("Explode"));
		tiles.put((short)20, new FarmTile("Farmland"));
		tiles.put((short)21, new WheatTile("Wheat"));
		tiles.put((short)22, new HardRockTile("Hard Rock"));
		tiles.put((short)23, new InfiniteFallTile("Infinite Fall"));
		tiles.put((short)24, new CloudTile("Cloud"));
		tiles.put((short)25, new OreTile(OreTile.OreType.Cloud));
		tiles.put((short)26, new DoorTile(Tile.Material.Wood));
		tiles.put((short)27, new DoorTile(Tile.Material.Stone));
		tiles.put((short)28, new DoorTile(Tile.Material.Obsidian));
		tiles.put((short)29, new FloorTile(Tile.Material.Wood));
		tiles.put((short)30, new FloorTile(Tile.Material.Stone));
		tiles.put((short)31, new FloorTile(Tile.Material.Obsidian));
		tiles.put((short)32, new WallTile(Tile.Material.Wood));
		tiles.put((short)33, new WallTile(Tile.Material.Stone));
		tiles.put((short)34, new WallTile(Tile.Material.Obsidian));
		tiles.put((short)35, new WoolTile(WoolTile.WoolType.NORMAL));
		tiles.put((short)36, new PathTile("Path"));
		tiles.put((short)37, new WoolTile(WoolTile.WoolType.RED));
		tiles.put((short)38, new WoolTile(WoolTile.WoolType.BLUE));
		tiles.put((short)39, new WoolTile(WoolTile.WoolType.GREEN));
		tiles.put((short)40, new WoolTile(WoolTile.WoolType.YELLOW));
		tiles.put((short)41, new WoolTile(WoolTile.WoolType.BLACK));
		tiles.put((short)42, new PotatoTile("Potato"));
		tiles.put((short)43, new MaterialTile(Tile.Material.Stone));
		tiles.put((short)44, new MaterialTile(Tile.Material.Obsidian));
		tiles.put((short)45, new DecorTile(Tile.Material.Stone));
		tiles.put((short)46, new DecorTile(Tile.Material.Obsidian));
		tiles.put((short)47, new BossWallTile());
		tiles.put((short)48, new BossFloorTile());
		tiles.put((short)49, new BossDoorTile());

		// WARNING: don't use this tile for anything!
		tiles.put((short)255, new ConnectTile());

		for(short i = 0; i < 256; i++) {
			if(tiles.get(i) == null) continue;
			tiles.get(i).id = i;
		}
	}


	static void add(int id, Tile tile) {
		tiles.put((short)id, tile);
		Logging.TILES.debug("Adding " + tile.name + " to tile list with id " + id);
		tile.id = (short) id;
	}

	static {
		oldids.put((short) 0, "grass");
		oldids.put((short) 1, "rock");
		oldids.put((short) 2, "water");
		oldids.put((short) 3, "flower");
		oldids.put((short) 4, "tree");
		oldids.put((short) 5, "dirt");
		oldids.put((short) 41, "wool");
		oldids.put((short) 42, "red wool");
		oldids.put((short) 43, "blue wool");
		oldids.put((short) 45, "green wool");
		oldids.put((short) 127, "yellow wool");
		oldids.put((short) 56, "black wool");
		oldids.put((short) 6, "sand");
		oldids.put((short) 7, "cactus");
		oldids.put((short) 8, "hole");
		oldids.put((short) 9, "tree Sapling");
		oldids.put((short) 10, "cactus Sapling");
		oldids.put((short) 11, "farmland");
		oldids.put((short) 12, "wheat");
		oldids.put((short) 13, "lava");
		oldids.put((short) 14, "stairs Down");
		oldids.put((short) 15, "stairs Up");
		oldids.put((short) 17, "cloud");
		oldids.put((short) 30, "explode");
		oldids.put((short) 31, "Wood Planks");
		oldids.put((short) 33, "plank wall");
		oldids.put((short) 34, "stone wall");
		oldids.put((short) 35, "wood door");
		oldids.put((short) 36, "wood door");
		oldids.put((short) 37, "stone door");
		oldids.put((short) 38, "stone door");
		oldids.put((short) 39, "lava brick");
		oldids.put((short) 32, "Stone Bricks");
		oldids.put((short) 120, "Obsidian");
		oldids.put((short) 121, "Obsidian wall");
		oldids.put((short) 122, "Obsidian door");
		oldids.put((short) 123, "Obsidian door");
		oldids.put((short) 18, "hard Rock");
		oldids.put((short) 19, "iron Ore");
		oldids.put((short) 24, "Lapis");
		oldids.put((short) 20, "gold Ore");
		oldids.put((short) 21, "gem Ore");
		oldids.put((short) 22, "cloud Cactus");
		oldids.put((short) 16, "infinite Fall");

		// Light/torch versions, for compatibility with before 1.9.4-dev3. (were removed in making dev3)
		oldids.put((short) 100, "grass");
		oldids.put((short) 101, "sand");
		oldids.put((short) 102, "tree");
		oldids.put((short) 103, "cactus");
		oldids.put((short) 104, "water");
		oldids.put((short) 105, "dirt");
		oldids.put((short) 107, "flower");
		oldids.put((short) 108, "stairs Up");
		oldids.put((short) 109, "stairs Down");
		oldids.put((short) 110, "Wood Planks");
		oldids.put((short) 111, "Stone Bricks");
		oldids.put((short) 112, "wood door");
		oldids.put((short) 113, "wood door");
		oldids.put((short) 114, "stone door");
		oldids.put((short) 115, "stone door");
		oldids.put((short) 116, "Obsidian door");
		oldids.put((short) 117, "Obsidian door");
		oldids.put((short) 119, "hole");
		oldids.put((short) 57, "wool");
		oldids.put((short) 58, "red wool");
		oldids.put((short) 59, "blue wool");
		oldids.put((short) 60, "green wool");
		oldids.put((short) 61, "yellow wool");
		oldids.put((short) 62, "black wool");
		oldids.put((short) 63, "Obsidian");
		oldids.put((short) 64, "tree Sapling");
		oldids.put((short) 65, "cactus Sapling");

		oldids.put((short) 44, "torch grass");
		oldids.put((short) 40, "torch sand");
		oldids.put((short) 46, "torch dirt");
		oldids.put((short) 47, "torch wood planks");
		oldids.put((short) 48, "torch stone bricks");
		oldids.put((short) 49, "torch Obsidian");
		oldids.put((short) 50, "torch wool");
		oldids.put((short) 51, "torch red wool");
		oldids.put((short) 52, "torch blue wool");
		oldids.put((short) 53, "torch green wool");
		oldids.put((short) 54, "torch yellow wool");
		oldids.put((short) 55, "torch black wool");
	}

	private static int overflowCheck = 0; // A crash should be used for unintended overflow instead as it breaks the game.
	public static Tile get(String name) {
		//System.out.println("Getting from tile list: " + name);

		name = name.toUpperCase();

		overflowCheck++;

		if(overflowCheck > 50) {
			CrashHandler.crashHandle(new StackOverflowError("Tiles#get: " + name), new CrashHandler.ErrorInfo("Tile fetching Stacking",
				CrashHandler.ErrorInfo.ErrorType.SERIOUS, "STACKOVERFLOW prevented in Tiles.get(), on: " + name));
		}

		//System.out.println("Fetching tile " + name);

		Tile getting = null;

		boolean isTorch = false;
		if(name.startsWith("TORCH")) {
			isTorch = true;
			name = name.substring(6); // Cuts off torch prefix.
		}

		if(name.contains("_")) {
			name = name.substring(0, name.indexOf("_"));
		}

		for(Tile t: tiles.values()) {
			if(t == null) continue;
			if(t.name.equals(name)) {
				getting = t;
				break;
			}
		}

		if(getting == null) {
			Logging.TILES.info("Invalid tile requested: " + name);
			getting = tiles.get((short)0);
		}

		if(isTorch) {
			getting = TorchTile.getTorchTile(getting);
		}

		overflowCheck = 0;
		return getting;
	}

	public static Tile get(int id) {
		//System.out.println("Requesting tile by id: " + id);
		if(id < 0) id += 32768;

		if(tiles.get((short)id) != null) {
			return tiles.get((short)id);
		}
		else if(id >= 32767) {
			return TorchTile.getTorchTile(get(id - 32767));
		}
		else {
			Logging.TILES.info("Unknown tile id requested: " + id);
			return tiles.get((short)0);
		}
	}

	public static boolean containsTile(int id) {
		return tiles.get((short)id) != null;
	}

	public static String getName(String descriptName) {
		if(!descriptName.contains("_")) return descriptName;
		int data;
		String[] parts = descriptName.split("_");
		descriptName = parts[0];
		data = Integer.parseInt(parts[1]);
		return get(descriptName).getName(data);
	}

	public static HashMap<Short, Tile> getAll() {
		return new HashMap<>(tiles);
	}
}
