package minicraft.level.structure;

import minicraft.entity.furniture.Crafter;
import minicraft.entity.furniture.DungeonChest;
import minicraft.entity.furniture.Furniture;
import minicraft.entity.furniture.KnightStatue;
import minicraft.entity.furniture.Lantern;
import minicraft.level.structure.SimpleStructure;
import minicraft.level.structure.ScatteredStructure;
import minicraft.level.structure.JigsawStructure;
import minicraft.level.structure.JigsawStructure.Connector;
import minicraft.level.structure.JigsawStructure.Direction;
import minicraft.level.structure.JigsawStructure.StructureWrapper;
import minicraft.level.structure.VillageHouse;

import java.util.HashSet;
import java.util.Collections;

public class Structures {
	public static final SimpleStructure dungeonGate;
	public static final SimpleStructure dungeonLock;
	public static final SimpleStructure dungeonBossRoom;
	public static final SimpleStructure lavaPool;
	public static final SimpleStructure ornateLavaPool;
	public static final SimpleStructure dungeonGarden;
	public static final SimpleStructure dungeonChest;
	public static final SimpleStructure dungeonSpawner;

	// All the "mobDungeon" structures are for the spawner structures
	public static final SimpleStructure mobDungeonCenter;
	public static final JigsawStructure mobDungeon;
	public static final JigsawStructure complexMobDungeon;

	public static final SimpleStructure airWizardHouse;

	// Used for random villages
	public static final ScatteredStructure village;
	public static final ScatteredStructure ruinedVillage;

	public static final Stairs skyStairsUp = new Stairs(true, 64);
	public static final Stairs skyStairsDown = new Stairs(false, 64);
	public static final Stairs surfaceStairsUp = new Stairs(true, 16);
	public static final Stairs surfaceStairsDown = new Stairs(false, 16);
	public static final Stairs stairsUp = new Stairs(true, 32);
	public static final Stairs stairsDown = new Stairs(false, 32);

	// Structures are oriented top->bottom for each line, & left->right for each character
	// i.e. how it appears in code is how it appears in-game
	static {
		// Deep dungeons
		{
			dungeonGate = new SimpleStructure(64);
			dungeonGate.setData("O:Obsidian,D:Obsidian Door,W:Obsidian Wall",
				"WWDWW\n" +
				"WOOOW\n" +
				"DOOOD\n" +
				"WOOOW\n" +
				"WWDWW"
			);
			dungeonGate.addFurniture(-1, -1, new Lantern(Lantern.Type.IRON));

			dungeonLock = new SimpleStructure(64);
			dungeonLock.setData("O:Obsidian,W:Obsidian Wall",
				"WWWWW\n" +
				"WOOOW\n" +
				"WOOOW\n" +
				"WOOOW\n" +
				"WWWWW"
			);
			dungeonBossRoom = new SimpleStructure(80);
			dungeonBossRoom.setData("O:Obsidian Boss Floor,D:Obsidian Boss Door,W:Obsidian Boss Wall",
				"WWWWDWWWW\n" +
				"WOOOOOOOW\n" +
				"WOOOOOOOW\n" +
				"WOOOOOOOW\n" +
				"DOOOOOOOD\n" +
				"WOOOOOOOW\n" +
				"WOOOOOOOW\n" +
				"WOOOOOOOW\n" +
				"WWWWDWWWW"
			);
			dungeonBossRoom.addFurniture(0, 0, new KnightStatue(5000));

			dungeonSpawner = new SimpleStructure(80);
			dungeonSpawner.setData("F:Grass,W:Obsidian Wall,O:Ornate Obsidian,D:Obsidian Door",
				"WWWDWWW\n" +
				"WOOOOOW\n" +
				"WOFFFOW\n" +
				"DOFFFOD\n" +
				"WOFFFOW\n" +
				"WOOOOOW\n" +
				"WWWDWWW"
			);

			lavaPool = new SimpleStructure(16);
			lavaPool.setData("L:Lava",
				"LL\n" +
				"LL"
			);

			ornateLavaPool = new SimpleStructure(32);
			ornateLavaPool.setData("L:Lava,W:Obsidian Wall,O:Ornate Obsidian,D:Obsidian Door",
				"WWWDWWW\n" +
				"WOOOOOW\n" +
				"WOLLLOW\n" +
				"DOLLLOD\n" +
				"WOLLLOW\n" +
				"WOOOOOW\n" +
				"WWWDWWW"
			);

			dungeonGarden = new SimpleStructure(32);
			dungeonGarden.setData("F:Flower,W:Obsidian Wall,O:Ornate Obsidian,D:Obsidian Door",
				"WWWDWWW\n" +
				"WOOOOOW\n" +
				"WOFFFOW\n" +
				"DOFFFOD\n" +
				"WOFFFOW\n" +
				"WOOOOOW\n" +
				"WWWDWWW"
			);

			dungeonChest = new SimpleStructure(48);
			dungeonChest.setData("F:Grass,W:Obsidian Wall,O:Ornate Obsidian,D:Obsidian Door",
				"WWWDWWW\n" +
				"WOOOOOW\n" +
				"WOFFFOW\n" +
				"DOFFFOD\n" +
				"WOFFFOW\n" +
				"WOOOOOW\n" +
				"WWWDWWW"
			);
			dungeonChest.addFurniture(0, 0, new DungeonChest(null));
		}

		HashSet<Connector> northConnectors = new HashSet<>();
		HashSet<Connector> eastConnectors = new HashSet<>();
		HashSet<Connector> centerConnectors = new HashSet<>();
		// Mob Dungeons
		{
			mobDungeonCenter = new SimpleStructure(32);
			mobDungeonCenter.setData("B:Stone Bricks,W:Stone Wall",
				"WWBWW\n" +
				"WBBBW\n" +
				"BBBBB\n" +
				"WBBBW\n" +
				"WWBWW"
			);
			SimpleStructure mobDungeonNorth = new SimpleStructure(32);
			mobDungeonNorth.setData("B:Stone Bricks,W:Stone Wall",
				"WWBWW\n" +
				"WBBBW\n" +
				"WBBBW\n" +
				"WBBBW\n" +
				"WBBBW"
			);
			SimpleStructure mobDungeonSouth = new SimpleStructure(32);
			mobDungeonSouth.setData("B:Stone Bricks,W:Stone Wall",
				"WBBBW\n" +
				"WBBBW\n" +
				"WBBBW\n" +
				"WBBBW\n" +
				"WWBWW"
			);
			SimpleStructure mobDungeonEast = new SimpleStructure(32);
			mobDungeonEast.setData("B:Stone Bricks,W:Stone Wall",
				"WWWWW\n" +
				"BBBBW\n" +
				"BBBBB\n" +
				"BBBBW\n" +
				"WWWWW"
			);
			SimpleStructure mobDungeonWest = new SimpleStructure(32);
			mobDungeonWest.setData("B:Stone Bricks,W:Stone Wall",
				"WWWWW\n" +
				"WBBBB\n" +
				"BBBBB\n" +
				"WBBBB\n" +
				"WWWWW"
			);

			mobDungeon = new JigsawStructure(64, 2, 5);
			centerConnectors.add(new Connector(Direction.NORTH, 0, -2));
			centerConnectors.add(new Connector(Direction.EAST, 2, 0));
			centerConnectors.add(new Connector(Direction.SOUTH, 0, 2));
			centerConnectors.add(new Connector(Direction.WEST, -2, 0));
			mobDungeon.setCenterPiece(new SpawnerRoom(), new HashSet<Connector>(centerConnectors), 1);

			northConnectors.add(new Connector(Direction.SOUTH, 0, 2));
			mobDungeon.addOptionalPiece(mobDungeonNorth, new HashSet<Connector>(northConnectors));
			northConnectors.add(new Connector(Direction.NORTH, 0, -2));

			HashSet<Connector> southConnectors = new HashSet<>();
			southConnectors.add(new Connector(Direction.NORTH, 0, -2));
			mobDungeon.addOptionalPiece(mobDungeonSouth, new HashSet<Connector>(southConnectors));

			eastConnectors.add(new Connector(Direction.WEST, -2, 0));
			mobDungeon.addOptionalPiece(mobDungeonEast, new HashSet<Connector>(eastConnectors));
			eastConnectors.add(new Connector(Direction.EAST, 2, 0));

			HashSet<Connector> westConnectors = new HashSet<>();
			westConnectors.add(new Connector(Direction.EAST, 2, 0));
			mobDungeon.addOptionalPiece(mobDungeonWest, new HashSet<Connector>(westConnectors));
		}

		complexMobDungeon = new JigsawStructure(128, 12, 20);
		setupComplexMobDungeon();

		airWizardHouse = new SimpleStructure(128);
		airWizardHouse.setData("F:Wood Planks,W:Wood Wall,D:Wood Door",
			"WWWWWWW\n" +
			"WFFFFFW\n" +
			"WFFFFFW\n" +
			"WFFFFFW\n" +
			"WWWDWWW"
		);
		airWizardHouse.addFurniture(-2, 0, new Lantern(Lantern.Type.GOLD));
		airWizardHouse.addFurniture(0, 0, new Crafter(Crafter.Type.Enchanter));

		village = new ScatteredStructure(128, 16, 3, 6);
		village.add(new VillageHouse(128, false), 3, 6);
		ruinedVillage = new ScatteredStructure(128, 16, 3, 6);
		ruinedVillage.add(new VillageHouse(128, true), 3, 6);
	}

	private static void setupComplexMobDungeon() {
		SimpleStructure hHallway = new SimpleStructure(32);
		HashSet<Connector> hConnectors = new HashSet<Connector>();
		hConnectors.add(new Connector(Direction.EAST, 2, 0));
		hConnectors.add(new Connector(Direction.WEST, -2, 0));
		hHallway.setData("B:Stone Bricks,W:Stone Wall",
			"WWWWW\n" +
			"BBBBB\n" +
			"WWWWW\n"
		);

		SimpleStructure vHallway = new SimpleStructure(32);
		HashSet<Connector> vConnectors = new HashSet<Connector>();
		vConnectors.add(new Connector(Direction.NORTH, 0, -2));
		vConnectors.add(new Connector(Direction.SOUTH, 0, 2));
		vHallway.setData("B:Stone Bricks,W:Stone Wall",
			"WBW\n" +
			"WBW\n" +
			"WBW\n" +
			"WBW\n" +
			"WBW"
		);

		HashSet<Connector> cConnectors = new HashSet<>();
		cConnectors.add(new Connector(Direction.NORTH, 0, -2));
		cConnectors.add(new Connector(Direction.EAST, 2, 0));
		cConnectors.add(new Connector(Direction.SOUTH, 0, 2));
		cConnectors.add(new Connector(Direction.WEST, -2, 0));

		complexMobDungeon.setCenterPiece(new SpawnerRoom(), cConnectors, 4);
		complexMobDungeon.addPiece(hHallway, hConnectors, 0, 10);
		complexMobDungeon.addPiece(vHallway, vConnectors, 0, 10);

		StructureWrapper nwElbow = new StructureWrapper(
			"B:Stone Bricks,W:Stone Wall", "WBW\nBBW\nWWW",
			new Connector(Direction.WEST, -1, 0),
			new Connector(Direction.NORTH, 0, -1));
		complexMobDungeon.addPiece(nwElbow.structure, nwElbow.connectors, 0, 10);
		StructureWrapper swElbow = new StructureWrapper(
			"B:Stone Bricks,W:Stone Wall", "WWW\nBBW\nWBW",
			new Connector(Direction.WEST, -1, 0),
			new Connector(Direction.SOUTH, 0, 1));
		complexMobDungeon.addPiece(swElbow.structure, swElbow.connectors, 0, 10);
		StructureWrapper neElbow = new StructureWrapper(
			"B:Stone Bricks,W:Stone Wall", "WBW\nWBB\nWWW",
			new Connector(Direction.EAST, 1, 0),
			new Connector(Direction.NORTH, 0, -1));
		complexMobDungeon.addPiece(neElbow.structure, neElbow.connectors, 0, 10);
		StructureWrapper seElbow = new StructureWrapper(
			"B:Stone Bricks,W:Stone Wall", "WWW\nWBB\nWBW",
			new Connector(Direction.EAST, 1, 0),
			new Connector(Direction.SOUTH, 0, 1));
		complexMobDungeon.addPiece(seElbow.structure, seElbow.connectors, 0, 10);
	}
}
