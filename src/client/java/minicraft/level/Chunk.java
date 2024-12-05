package minicraft.level;

import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;

public class Chunk {
	public static final int SIZE = 32;

	public short[] data, tiles;

	public Chunk() {
		data = new short[SIZE * SIZE];
		tiles = new short[SIZE * SIZE];
	}

	/**
	* Returns a tile. Mods x and y to the range 0-SIZE as to never be out of bounds
	*/
	public Tile getTile(int x, int y) {
		return Tiles.get(tiles[(x % SIZE) + (y % SIZE) * SIZE]);
	}

	/**
	* Updates a tile. Mods x and y to the range 0-SIZE as to never be out of bounds
	*/
	public void setTile(int x, int y, Tile t, int dataVal) {
		int index = (x % SIZE) + (y % SIZE) * SIZE;
		tiles[index] = t.id;
		data[index] = (short) dataVal;
	}

	public int getData(int x, int y) {
		return data[(x % SIZE) + (y % SIZE) * SIZE] & 0xFFFF;
	}

	public void setData(int x, int y, int val) {
		data[(x % SIZE) + (y % SIZE) * SIZE] = (short) val;
	}
}
