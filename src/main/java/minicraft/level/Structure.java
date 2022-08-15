package minicraft.level;

import java.util.HashMap;
import java.util.HashSet;

import minicraft.entity.furniture.Crafter;
import minicraft.entity.furniture.Furniture;
import minicraft.entity.furniture.KnightStatue;
import minicraft.entity.furniture.Lantern;
import minicraft.gfx.Point;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;

// this stores structures that can be drawn at any location.
public class Structure {

	private HashSet<TilePoint> tiles;
	private HashMap<Point, Furniture> furniture;

	public Structure() {
		tiles = new HashSet<>();
		furniture = new HashMap<>();
	}
	public Structure(Structure struct) {
		this.tiles = struct.tiles;
		this.furniture = struct.furniture;
	}

	public void setTile(int x, int y, Tile tile) {
		tiles.add(new TilePoint(x, y, tile));
	}
	public void addFurniture(int x, int y, Furniture furniture) {
		this.furniture.put(new Point(x, y), furniture);
	}

	public void draw(Level level, int xt, int yt) {
		for (TilePoint p: tiles)
			 level.setTile(xt+p.x, yt+p.y, p.t);

		for (Point p: furniture.keySet())
			 level.add(furniture.get(p).clone(), xt+p.x, yt+p.y, true);
	}

	public void draw(short[] map, int xt, int yt, int mapWidth) {
		for (TilePoint p: tiles)
			map[(xt + p.x) + (yt + p.y) * mapWidth] = p.t.id;
	}

	public void setData(String keys, String data) {
		// So, the keys are single letters, each letter represents a tile
		HashMap<String, String> keyPairs = new HashMap<>();
		String[] stringKeyPairs = keys.split(",");

		// Puts all the keys in the keyPairs HashMap
		for (int i = 0; i < stringKeyPairs.length; i++) {
			String[] thisKey = stringKeyPairs[i].split(":");
			keyPairs.put(thisKey[0], thisKey[1]);
		}

		String[] dataLines = data.split("\n");
		int width = dataLines[0].length();
		int height = dataLines.length;

		for (int i = 0; i < dataLines.length; i++) {
			for (int c = 0; c < dataLines[i].length(); c++) {
				if (dataLines[i].charAt(c) != '*') {
					Tile tile = Tiles.get(keyPairs.get(String.valueOf(dataLines[i].charAt(c))));
					this.setTile(-width / 2 + i, - height / 2 + c, tile);
				}
			}
		}
	}

	static class TilePoint {
		int x, y;
		Tile t;

		public TilePoint(int x, int y, Tile tile) {
			this.x = x;
			this.y = y;
			this.t = tile;
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof TilePoint)) return false;
			TilePoint p = (TilePoint) o;
			return x == p.x && y == p.y && t.id == p.t.id;
		}

		@Override
		public int hashCode() {
			return x + y * 51 + t.id * 131;
		}
	}

	static final Structure dungeonGate;
	static final Structure dungeonLock;
	static final Structure dungeonBossRoom;
	static final Structure lavaPool;

	// All the "mobDungeon" structures are for the spawner structures
	static final Structure mobDungeonCenter;
	static final Structure mobDungeonNorth;
	static final Structure mobDungeonSouth;
	static final Structure mobDungeonEast;
	static final Structure mobDungeonWest;

	static final Structure airWizardHouse;

	// Used for random villages
	static final Structure villageHouseNormal;
	static final Structure villageHouseTwoDoor;

	static final Structure villageRuinedOverlay1;
	static final Structure villageRuinedOverlay2;

	// Ok, because of the way the system works, these structures are rotated 90 degrees clockwise when placed
	// Then it's flipped on the vertical
	static {
		dungeonGate = new Structure();
		dungeonGate.setData("O:Obsidian,D:Obsidian Door,W:Obsidian Wall",
					"WWDWW\n" +
					"WOOOW\n" +
					"DOOOD\n" +
					"WOOOW\n" +
					"WWDWW"
		);
		dungeonGate.addFurniture(-1, -1, new Lantern(Lantern.Type.IRON));

		dungeonLock = new Structure();
		dungeonLock.setData("O:Obsidian,W:Obsidian Wall",
					"WWWWW\n" +
					"WOOOW\n" +
					"WOOOW\n" +
					"WOOOW\n" +
					"WWWWW"
		);
		dungeonBossRoom = new Structure();
		dungeonBossRoom.setData("O:Ornate Obsidian,W:Obsidian Boss Wall",
			"WWWWWWWWW\n" +
				"WOOOOOOOW\n" +
				"WOOOOOOOW\n" +
				"WOOOOOOOW\n" +
				"WOOOOOOOW\n" +
				"WOOOOOOOW\n" +
				"WOOOOOOOW\n" +
				"WOOOOOOOW\n" +
				"WWWWWWWWW"
		);
		dungeonBossRoom.addFurniture(0,0,new KnightStatue(5000));

		lavaPool = new Structure();
		lavaPool.setData("L:Lava",
					"LL\n" +
					"LL"
		);

		mobDungeonCenter = new Structure();
		mobDungeonCenter.setData("B:Stone Bricks,W:Stone Wall",
					"WWBWW\n" +
					"WBBBW\n" +
					"BBBBB\n" +
					"WBBBW\n" +
					"WWBWW"
		);
		mobDungeonNorth = new Structure();
		mobDungeonNorth.setData("B:Stone Bricks,W:Stone Wall",
					"WWWWW\n" +
					"WBBBB\n" +
					"BBBBB\n" +
					"WBBBB\n" +
					"WWWWW"
		);
		mobDungeonSouth = new Structure();
		mobDungeonSouth.setData("B:Stone Bricks,W:Stone Wall",
					"WWWWW\n" +
					"BBBBW\n" +
					"BBBBB\n" +
					"BBBBW\n" +
					"WWWWW"
		);
		mobDungeonEast = new Structure();
		mobDungeonEast.setData("B:Stone Bricks,W:Stone Wall",
					"WBBBW\n" +
					"WBBBW\n" +
					"WBBBW\n" +
					"WBBBW\n" +
					"WWBWW"
		);
		mobDungeonWest = new Structure();
		mobDungeonWest.setData("B:Stone Bricks,W:Stone Wall",
					"WWBWW\n" +
					"WBBBW\n" +
					"WBBBW\n" +
					"WBBBW\n" +
					"WBBBW"
		);

		airWizardHouse = new Structure();
		airWizardHouse.setData("F:Wood Planks,W:Wood Wall,D:Wood Door",
					"WWWWWWW\n" +
					"WFFFFFW\n" +
					"DFFFFFW\n" +
					"WFFFFFW\n" +
					"WWWWWWW"
		);
		airWizardHouse.addFurniture(-2, 0, new Lantern(Lantern.Type.GOLD));
		airWizardHouse.addFurniture(0, 0, new Crafter(Crafter.Type.Enchanter));

		villageHouseNormal = new Structure();
		villageHouseNormal.setData("F:Wood Planks,W:Wood Wall,D:Wood Door,G:Grass",
					"WWWWW\n" +
					"WFFFW\n" +
					"WFFFD\n" +
					"WFFFG\n" +
					"WWWWW"
		);

		villageHouseTwoDoor = new Structure();
		villageHouseTwoDoor.setData("F:Wood Planks,W:Wood Wall,D:Wood Door,G:Grass",
					"WWWWW\n" +
					"WFFFW\n" +
					"DFFFW\n" +
					"WFFFW\n" +
					"WWDWW"
		);

		villageRuinedOverlay1 = new Structure();
		villageRuinedOverlay1.setData("G:Grass,F:Wood Planks",
					"**FG*\n" +
					"F*GG*\n" +
					"*G**F\n" +
					"G*G**\n" +
					"***G*"
		);

		villageRuinedOverlay2 = new Structure();
		villageRuinedOverlay2.setData("G:Grass,F:Wood Planks",
					"F**G*\n" +
					"*****\n" +
					"*GG**\n" +
					"F**G*\n" +
					"*F**G"
		);
	}
}
