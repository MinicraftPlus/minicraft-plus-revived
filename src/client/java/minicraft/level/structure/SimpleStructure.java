package minicraft.level.structure;

import minicraft.entity.furniture.Furniture;
import minicraft.entity.furniture.DungeonChest;
import minicraft.gfx.Point;
import minicraft.gfx.Rectangle;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;
import minicraft.level.TilePoint;
import minicraft.level.ChunkManager;
import minicraft.level.ChunkPoint;
import minicraft.util.Hash2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;

// this represents structures spanning a single dimension. with tile & furniture about an origin
public class SimpleStructure extends GridGenStructure {

	private HashSet<TilePoint> tiles;
	private HashMap<Point, Furniture> furniture;
	private Rectangle bounds;

	public SimpleStructure(int gridSize) {
		super(gridSize);
		tiles = new HashSet<>();
		furniture = new HashMap<>();
		this.bounds = new Rectangle();
	}

	public void setTile(int x, int y, Tile tile) {
		tiles.add(new TilePoint(x, y, tile));
		bounds.addPoint(new Point(x, y));
	}

	public void addFurniture(int x, int y, Furniture furniture) {
		this.furniture.put(new Point(x, y), furniture);
	}

	public Set<ChunkPoint> affectedChunks(Level level, int xt, int yt) {
		int S = ChunkManager.CHUNK_SIZE;
		HashSet<ChunkPoint> affected = new HashSet<>();
		for(TilePoint p : tiles)
			affected.add(new ChunkPoint(Math.floorDiv(p.x+xt, S), Math.floorDiv(p.y+yt, S), level.depth));
		return affected;
	}

	public Set<Rectangle> getBounds(Level level, int x, int y) {
		Rectangle rel = new Rectangle(bounds);
		rel.translate(x, y);
		return Collections.singleton(rel);
	}

	public void draw(Level level, int xt, int yt) {
		for (TilePoint p : tiles)
			level.setTile(xt + p.x, yt + p.y, p.t);

		for (Point p : furniture.keySet()) {
			Furniture fur = furniture.get(p).copy();
			if(fur instanceof DungeonChest) {
				Random r = new Random(Hash2.hash2(level.getSeed(), xt, yt));
				((DungeonChest) fur).populateInv(r);
			}
			level.add(fur, xt + p.x, yt + p.y, true);
		}
	}

	public void setData(String keys, String data) {
		// Keys are single letters, each letter represents a tile
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
		seedOffset = width * 13 + height;

		for (int i = 0; i < dataLines.length; i++) {
			for (int c = 0; c < dataLines[i].length(); c++) {
				if (dataLines[i].charAt(c) != '*') {
					String key = keyPairs.get(String.valueOf(dataLines[i].charAt(c)));
					seedOffset ^= key.hashCode() << (c + i * width);
					Tile tile = Tiles.get(key);
					this.setTile(-width / 2 + c, -height / 2 + i, tile);
				}
			}
		}
	}
}
