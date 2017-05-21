package minicraft.level.tile;

import java.util.ArrayList;


public final class Tiles {
	/// idea: to save tile names while saving space, I could encode the names in base 64 in the save file...^M
    /// then, maybe, I would just replace the id numbers with id names, make them all private, and then make a get(String) method, parameter is tile name.
	
	public static ArrayList<String> oldids = new ArrayList<String>();
	
	private static ArrayList<Tile> tiles = new ArrayList<Tile>();
	
	//public static Tile[] tiles = new Tile[256];
	
	
	public static void initTileList() {
		CactusTile.addInstances();
		CloudCactusTile.addInstances();
		CloudTile.addInstances();
		DirtTile.addInstances();
		DoorTile.addInstances();
		ExplodedTile.addInstances();
		FarmTile.addInstances();
		FloorTile.addInstances();
		FlowerTile.addInstances();
		GrassTile.addInstances();
		HardRockTile.addInstances();
		HoleTile.addInstances();
		InfiniteFallTile.addInstances();
		WaterTile.addInstances();
		LavaBrickTile.addInstances();
		LavaTile.addInstances();
		OreTile.addInstances();
		RockTile.addInstances();
		TreeTile.addInstances();
		SandTile.addInstances();
		SaplingTile.addInstances();
		StairsTile.addInstances();
		//TorchTile.addInstances();
		WallTile.addInstances();
		WheatTile.addInstances();
		WoolTile.addInstances();
	}
	
	
	protected static void add(Tile tile) {
		tiles.add(tile);
		System.out.println("adding " + tile.name + " to tile list.");
		tile.id = (byte)(tiles.size()-1);
	}
	protected static void addAll(Tile[] tiles) {
		for(Tile t: tiles)
			add(t);
	}
	
	static {
		/*
		tiles.put("grass", new GrassTile()); ///_grass_0_ // creates a grass tile with the Id of 0, (I don't need to explain the other simple ones)
		tiles.put("rock", new RockTile()); ///_rock_1_
		tiles.put("water", new WaterTile()); ///_water_2_
		tiles.put("flower", new FlowerTile()); ///_flower_3_
		tiles.put("tree", new TreeTile()); ///_tree_4_
		tiles.put("dirt", new DirtTile()); ///_dirt_5_
		//wool
		tiles.put("wool", new WoolTile(null)); ///_wool_41_
		tiles.put("redwool", new WoolTile(WoolTiles.get("WoolColor").RED)); ///_redwool_42_
		tiles.put("bluewool", new WoolTile(WoolTiles.get("WoolColor").BLUE)); ///_bluewool_43_
		tiles.put("greenwool", new WoolTile(WoolTiles.get("WoolColor").GREEN)); ///_greenwool_45_
		tiles.put("yellowwool", new WoolTile(WoolTiles.get("WoolColor").YELLOW)); ///_yellowwool_127_
		tiles.put("blackwool", new WoolTile(WoolTiles.get("WoolColor").BLACK)); ///_blackwool_56_
		
		tiles.put("sand", new SandTile()); ///_sand_6_
		tiles.put("cactus", new CactusTile()); ///_cactus_7_
		tiles.put("hole", new HoleTile()); ///_hole_8_
		
		tiles.put("farmland", new FarmTile()); ///_farmland_11_ // farmland (tilled dirt)
		tiles.put("wheat", new WheatTile()); ///_wheat_12_
		tiles.put("lava", new LavaTile()); ///_lava_13_
		tiles.put("stairsDown", new StairsTile(false)); ///_stairsDown_14_
		tiles.put("stairsUp", new StairsTile(true)); ///_stairsUp_15_
		tiles.put("cloud", new CloudTile()); ///_cloud_17_
		tiles.put("explode", new ExplodedTile()); ///_explode_30_
		tiles.put("Wood Planks", new FloorTile(Material.Wood)); ///_plank_31_
		tiles.put("plankwall", new WallTile(Material.Wood)); ///_plankwall_33_
		tiles.put("stonewall", new WallTile(Material.Stone)); ///_stonewall_34_
		tiles.put("wd", new DoorTile(Material.Wood)); ///_wdo_35_
		//tiles.put("wd", new DoorClosedTile(Material.Wood)); ///_wdc_36_
		tiles.put("sd", new DoorTile(Material.Stone)); ///_sdo_37_
		//tiles.put("sd", new DoorClosedTile(Material.Stone)); ///_sdc_38_
		tiles.put("lavabrick", new LavaBrickTile()); ///_lavabrick_39_
		tiles.put("sbrick", new FloorTile(Material.Stone)); ///_sbrick_32_
		tiles.put("o", new FloorTile(Material.Obsidian)); ///_o_120_
		tiles.put("Obsidian Wall", new WallTile(Material.Obsidian)); ///_ow_121_
		tiles.put("od", new DoorTile(Material.Obsidian)); ///_odc_122_
		//tiles.put("od", new DoorOpenTile(Material.Obsidian)); ///_odo_123_
		tiles.put("hardRock", new HardRockTile()); ///_hardRock_18_
		
		tiles.put("ironOre", new OreTile(OreTiles.get("OreType").IRON)); ///_ironOre_19_
		tiles.put("lapisOre", new OreTile(OreTiles.get("OreType").LAPIS)); ///_lapisOre_24_
		tiles.put("goldOre", new OreTile(OreTiles.get("OreType").GOLD)); ///_goldOre_20_
		tiles.put("gemOre", new OreTile(OreTiles.get("OreType").GEM)); ///_gemOre_21_
		tiles.put("cloudCactus", new CloudCactusTile()); ///_cloudCactus_22_ // "ore" in the sky.
		tiles.put("infiniteFall", new InfiniteFallTile()); ///_infiniteFall_16_ // Air tile in the sky..?
		
		tiles.put("treeSapling", new SaplingTile("grass", tree)); ///_treeSapling_9_
		tiles.put("cactusSapling", new SaplingTile(sand, cactus)); ///_cactusSapling_10_
		
		for(String key: tiles.keySet().toArray(new String[0]))
			tiles.get(key).name = key;
		*/
		
		for(int i = 0; i < 256; i++)
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
		
		// light/torch versions, for compatibility with before 1.9.4-dev3. (were removed in making dev3)
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
		oldids.set(47, "torch plank");
		oldids.set(48, "torch stone brick");
		oldids.set(49, "torch Obsidian");
		oldids.set(50, "torch wool");
		oldids.set(51, "torch red wool");
		oldids.set(52, "torch blue wool");
		oldids.set(53, "torch green wool");
		oldids.set(54, "torch yellow wool");
		oldids.set(55, "torch black wool");
	}
	
	static int overflowCheck = 0;
	public static Tile get(String name) {
		/// IMPORTANT: note that having a tile object for each tile is probably inefficient, and will probably need to be replaced by a more processor-friendly system...
		//System.out.println("getting from tile list: " + name);
		
		overflowCheck++;
		
		if(overflowCheck > 50) {
			System.out.println("STACKOVERFLOW prevented in Tiles.get(), on: " + name);
			System.exit(1);
		}
		
		Tile getting = null;
		for(Tile t: tiles) {
			if(t.name.compareToIgnoreCase(name) == 0) {
				getting = t;
				break;
			}
		}
		
		if(getting == null) {
			System.out.println("TILES.GET: invalid tile requested: " + name);
			getting = get("grass");
		}
		
		overflowCheck = 0;
		return getting;
	}
	
	public static Tile get(int id) {
		//System.out.println("requesting tile by id: " + id);
		
		String name = oldids.get(id);
		if(name != null && name.length() > 0)
			return get(name);
		else {
			System.out.println("TILES.GET: unknown tile id requested: " + id);
			return get("grass");
		}
	}
}
