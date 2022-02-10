package minicraft.level.tile;

import java.util.ArrayList;

import minicraft.level.tile.farming.FarmTile;
import minicraft.level.tile.farming.PotatoTile;
import minicraft.level.tile.farming.WheatTile;
import org.tinylog.Logger;

public final class Tiles {
	/// Idea: to save tile names while saving space, I could encode the names in base 64 in the save file...^M
    /// Then, maybe, I would just replace the id numbers with id names, make them all private, and then make a get(String) method, parameter is tile name.
	
	public static ArrayList<String> oldids = new ArrayList<>();
	
	private static ArrayList<Tile> tiles = new ArrayList<>();
	
	public static void initTileList() {
		Logger.debug("Initializing tile list...");
		
		// 
		for (int i = 0; i < 32768; i++)
			tiles.add(null);

		tiles.set(0, new GrassTile("Grass"));
		tiles.set(1, new DirtTile("Dirt"));
		tiles.set(2, new FlowerTile("Flower"));
		tiles.set(3, new HoleTile("Hole"));
		tiles.set(4, new StairsTile("Stairs Up", true));
		tiles.set(5, new StairsTile("Stairs Down", false));
		tiles.set(6, new WaterTile("Water"));
		// This is out of order because of lava buckets
		tiles.set(17, new LavaTile("Lava"));

		tiles.set(7, new RockTile("Rock"));
		tiles.set(8, new TreeTile("Tree"));
		tiles.set(9, new SaplingTile("Tree Sapling", Tiles.get("Grass"), Tiles.get("Tree")));
		tiles.set(10, new SandTile("Sand"));
		tiles.set(11, new CactusTile("Cactus"));
		tiles.set(12, new SaplingTile("Cactus Sapling", Tiles.get("Sand"), Tiles.get("Cactus")));
		tiles.set(13, new OreTile(OreTile.OreType.Iron));
		tiles.set(14, new OreTile(OreTile.OreType.Gold));
		tiles.set(15, new OreTile(OreTile.OreType.Gem));
		tiles.set(16, new OreTile(OreTile.OreType.Lapis));
		tiles.set(18, new LavaBrickTile("Lava Brick"));
		tiles.set(19, new ExplodedTile("Explode"));
		tiles.set(20, new FarmTile("Farmland"));
		tiles.set(21, new WheatTile("Wheat"));
		tiles.set(22, new HardRockTile("Hard Rock"));
		tiles.set(23, new InfiniteFallTile("Infinite Fall"));
		tiles.set(24, new CloudTile("Cloud"));
		tiles.set(25, new CloudCactusTile("Cloud Cactus"));
		tiles.set(26, new DoorTile(Tile.Material.Wood));
		tiles.set(27, new DoorTile(Tile.Material.Stone));
		tiles.set(28, new DoorTile(Tile.Material.Obsidian));
		tiles.set(29, new FloorTile(Tile.Material.Wood));
		tiles.set(30, new FloorTile(Tile.Material.Stone));
		tiles.set(31, new FloorTile(Tile.Material.Obsidian));
		tiles.set(32, new WallTile(Tile.Material.Wood));
		tiles.set(33, new WallTile(Tile.Material.Stone));
		tiles.set(34, new WallTile(Tile.Material.Obsidian));
		tiles.set(35, new WoolTile(WoolTile.WoolType.NORMAL));
		tiles.set(36, new PathTile("Path"));
		tiles.set(37, new WoolTile(WoolTile.WoolType.RED));
		tiles.set(38, new WoolTile(WoolTile.WoolType.BLUE));
		tiles.set(39, new WoolTile(WoolTile.WoolType.GREEN));
		tiles.set(40, new WoolTile(WoolTile.WoolType.YELLOW));
		tiles.set(41, new WoolTile(WoolTile.WoolType.BLACK));
		tiles.set(42, new PotatoTile("Potato"));
		tiles.set(43, new MaterialTile(Tile.Material.Stone));
		tiles.set(44, new MaterialTile(Tile.Material.Obsidian));
		tiles.set(45, new DecorTile(Tile.Material.Stone));
		tiles.set(46, new DecorTile(Tile.Material.Obsidian));

		// WARNING: don't use this tile for anything!
		tiles.set(256, new ConnectTile());
		
		for(int i = 0; i < tiles.size(); i++) {
			if(tiles.get(i) == null) continue;
			tiles.get(i).id = (byte)i;
		}
	}
	

	protected static void add(int id, Tile tile) {
		tiles.set(id, tile);
		System.out.println("Adding " + tile.name + " to tile list with id " + id);
		tile.id = (short) id;
	}

	static {
		for(int i = 0; i < 32768; i++)
			oldids.add(null);
		
		oldids.set(0, "grass");
		oldids.set(1, "rock");
		oldids.set(2, "water");
		oldids.set(3, "flower");
		oldids.set(4, "tree");
		oldids.set(5, "dirt");
		oldids.set(41, "wool");
		oldids.set(42, "red wool");
		oldids.set(43, "blue wool");
		oldids.set(45, "green wool");
		oldids.set(127, "yellow wool");
		oldids.set(56, "black wool");
		oldids.set(6, "sand");
		oldids.set(7, "cactus");
		oldids.set(8, "hole");
		oldids.set(9, "tree Sapling");
		oldids.set(10, "cactus Sapling");
		oldids.set(11, "farmland");
		oldids.set(12, "wheat");
		oldids.set(13, "lava");
		oldids.set(14, "stairs Down");
		oldids.set(15, "stairs Up");
		oldids.set(17, "cloud");
		oldids.set(30, "explode");
		oldids.set(31, "Wood Planks");
		oldids.set(33, "plank wall");
		oldids.set(34, "stone wall");
		oldids.set(35, "wood door");
		oldids.set(36, "wood door");
		oldids.set(37, "stone door");
		oldids.set(38, "stone door");
		oldids.set(39, "lava brick");
		oldids.set(32, "Stone Bricks");
		oldids.set(120, "Obsidian");
		oldids.set(121, "Obsidian wall");
		oldids.set(122, "Obsidian door");
		oldids.set(123, "Obsidian door");
		oldids.set(18, "hard Rock");
		oldids.set(19, "iron Ore");
		oldids.set(24, "Lapis");
		oldids.set(20, "gold Ore");
		oldids.set(21, "gem Ore");
		oldids.set(22, "cloud Cactus");
		oldids.set(16, "infinite Fall");
		
		// Light/torch versions, for compatibility with before 1.9.4-dev3. (were removed in making dev3)
		oldids.set(100, "grass");
		oldids.set(101, "sand");
		oldids.set(102, "tree");
		oldids.set(103, "cactus");
		oldids.set(104, "water");
		oldids.set(105, "dirt");
		oldids.set(107, "flower");
		oldids.set(108, "stairs Up");
		oldids.set(109, "stairs Down");
		oldids.set(110, "Wood Planks");
		oldids.set(111, "Stone Bricks");
		oldids.set(112, "wood door");
		oldids.set(113, "wood door");
		oldids.set(114, "stone door");
		oldids.set(115, "stone door");
		oldids.set(116, "Obsidian door");
		oldids.set(117, "Obsidian door");
		oldids.set(119, "hole");
		oldids.set(57, "wool");
		oldids.set(58, "red wool");
		oldids.set(59, "blue wool");
		oldids.set(60, "green wool");
		oldids.set(61, "yellow wool");
		oldids.set(62, "black wool");
		oldids.set(63, "Obsidian");
		oldids.set(64, "tree Sapling");
		oldids.set(65, "cactus Sapling");
		
		oldids.set(44, "torch grass");
		oldids.set(40, "torch sand");
		oldids.set(46, "torch dirt");
		oldids.set(47, "torch wood planks");
		oldids.set(48, "torch stone bricks");
		oldids.set(49, "torch Obsidian");
		oldids.set(50, "torch wool");
		oldids.set(51, "torch red wool");
		oldids.set(52, "torch blue wool");
		oldids.set(53, "torch green wool");
		oldids.set(54, "torch yellow wool");
		oldids.set(55, "torch black wool");
	}
	
	private static int overflowCheck = 0;
	public static Tile get(String name) {
		//System.out.println("Getting from tile list: " + name);
		
		name = name.toUpperCase();
		
		overflowCheck++;
		
		if(overflowCheck > 50) {
			System.out.println("STACKOVERFLOW prevented in Tiles.get(), on: " + name);
			System.exit(1);
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

		for(Tile t: tiles) {
			if(t == null) continue;
			if(t.name.equals(name)) {
				getting = t;
				break;
			}
		}
		
		if(getting == null) {
			System.out.println("TILES.GET: Invalid tile requested: " + name);
			getting = tiles.get(0);
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

		if(tiles.get(id) != null) {
			return tiles.get(id);
		}
		else if(id >= 32767) {
			return TorchTile.getTorchTile(get(id - 32767));
		}
		else {
			System.out.println("TILES.GET: Unknown tile id requested: " + id);
			return tiles.get(0);
		}
	}
	
	public static boolean containsTile(int id) {
		return tiles.get(id) != null;
	}
	
	public static String getName(String descriptName) {
		if(!descriptName.contains("_")) return descriptName;
		int data;
		String[] parts = descriptName.split("_");
		descriptName = parts[0];
		data = Integer.parseInt(parts[1]);
		return get(descriptName).getName(data);
	}
}
