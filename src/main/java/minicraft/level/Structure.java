package minicraft.level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.tinylog.Logger;

import minicraft.entity.furniture.Furniture;
import minicraft.entity.furniture.Furnitures;
import minicraft.gfx.Point;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;
import minicraft.saveload.Load;

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

	public void setData(Map<String, String> keys, List<String> data) {
		int width = data.get(0).length();
		int height = data.size();

		for (int i = 0; i < data.size(); i++) {
			for (int c = 0; c < data.get(i).length(); c++) {
				if (data.get(i).charAt(c) != ' ') {
					Tile tile = Tiles.get(keys.get(String.valueOf(data.get(i).charAt(c))));
					this.setTile(-width / 2 + i, - height / 2 + c, tile);
				}
			}
		}
	}

	public static Structure load(String id) {
		Structure struct = new Structure();

		try {
			JSONObject obj = Load.loadJsonFile("/resources/structures/" + id + ".json");
	
			JSONObject keyObj = obj.getJSONObject("key");
			JSONArray dataObj = obj.getJSONArray("data");
			
			HashMap<String, String> keys = new HashMap<>();
			Iterator<String> keySet = keyObj.keySet().iterator();
	
			while (keySet.hasNext()) {
				String k = keySet.next();
				keys.put(k, keyObj.getString(k));
			}
	
			List<String> data = new ArrayList<>();
			for (int i = 0; i < dataObj.length(); i++) data.add(dataObj.getString(i));
	
			struct.setData(keys, data);
	
			if (obj.has("furniture")) {
				JSONArray furnitureObj = obj.getJSONArray("furniture");
	
				for (int i = 0; i < furnitureObj.length(); i++) {
					JSONObject furniture = furnitureObj.getJSONObject(i);
					int x = furniture.getInt("x");
					int y = furniture.getInt("y");
					Furniture fur = Furnitures.get(furniture.getString("id"));
	
					if (fur != null) {
						struct.addFurniture(x, y, fur.clone());
					}
				}
			}

			Logger.debug("Loaded Structure: " + id);
		} catch(Exception e) {
			System.out.println("Failed to load structure " + id);
			System.exit(1);
		}

		return struct;
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
		dungeonGate = load("dungeon_gate");
		dungeonLock = load("dungeon_lock");
		lavaPool = load("lava_pool");
		mobDungeonCenter = load("mob_dungeon_center");
		mobDungeonNorth = load("mob_dungeon_north");
		mobDungeonSouth = load("mob_dungeon_south");
		mobDungeonEast = load("mob_dungeon_east");
		mobDungeonWest = load("mob_dungeon_west");
		airWizardHouse = load("airwizard_house");
		villageHouseNormal = load("village_house_normal");
		villageHouseTwoDoor = load("village_house_two_door");
		villageRuinedOverlay1 = load("village_ruined_overlay1");
		villageRuinedOverlay2 = load("village_ruined_overlay2");
	}
}
