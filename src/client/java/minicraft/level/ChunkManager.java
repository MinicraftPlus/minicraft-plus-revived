package minicraft.level;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import minicraft.gfx.Point;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;

public class ChunkManager {

	public static final int CHUNK_SIZE = 64;
	public static final int CHUNK_STAGE_UNFINISHED_STAIRS = 3;
	public static final int CHUNK_STAGE_DONE = 4;

	/**
	 * A data structure where
	 * [x][y] input points to CHUNK_SIZE x CHUNK_SIZE list of TileDat
	 */
	public Map<Integer, Map<Integer, Chunk>> chunks;

	public ChunkManager() {
		chunks = new HashMap<>();
	}

	public Set<Point> getAllChunks() {
		HashSet<Point> out = new HashSet<>();
		for(int x : chunks.keySet())
			for(int y : chunks.get(x).keySet())
				out.add(new Point(x, y));
		return out;
	}

	/**
	 * Return the chunk  in which the tileX and tileY land
	 */
	private Chunk getChunk(int tileX, int tileY) {
		int cX = Math.floorDiv(tileX, CHUNK_SIZE), cY = Math.floorDiv(tileY, CHUNK_SIZE);
		// If [cX][cY] are not keys in chunks, put them there
		if(!chunks.containsKey(cX))
			chunks.put(cX, new HashMap<>());
		if(!chunks.get(cX).containsKey(cY))
			chunks.get(cX).put(cY, new Chunk());
		return chunks.get(cX).get(cY);
	}

	/**
	* Returns a tile. After finding the right chunk, mods x and y to the range 0-CHUNK_SIZE as to never be out of bounds
	*/
	public Tile getTile(int x, int y) {
		return Tiles.get(getChunk(x, y).getTileDat(x, y).id);
	}

	/**
	* Updates a tile. After finding the right chunk, mods x and y to the range 0-CHUNK_SIZE as to never be out of bounds
	*/
	public void setTile(int x, int y, Tile t, int dataVal) {
		TileDat dat = getChunk(x, y).getTileDat(x, y);
		dat.id = t.id;
		dat.data = (short) dataVal;
	}

	public int getData(int x, int y) {
		return getChunk(x, y).getTileDat(x, y).data;
	}

	public void setData(int x, int y, int val) {
		getChunk(x, y).getTileDat(x, y).data = (short) val;
	}

	public int getChunkStage(int chunkX, int chunkY) {
		// If [cX][cY] are not keys in chunks, stage must be 0
		if(!chunks.containsKey(chunkX) || !chunks.get(chunkX).containsKey(chunkY))
			return 0;
		return chunks.get(chunkX).get(chunkY).stage;
	}

	public void setChunkStage(int chunkX, int chunkY, int stage) {
		// If [chunkX][chunkY] are not keys in chunks, put them there
		if(!chunks.containsKey(chunkX))
			chunks.put(chunkX, new HashMap<>());
		if(!chunks.get(chunkX).containsKey(chunkY))
			chunks.get(chunkX).put(chunkY, new Chunk());
		chunks.get(chunkX).get(chunkY).stage = (short)stage;
	}

	private static class Chunk {
		protected TileDat[] tiles;
		protected short stage = 0;
		public Chunk() {
			tiles = new TileDat[CHUNK_SIZE * CHUNK_SIZE];
		}

		public TileDat getTileDat(int tileX, int tileY) {
			int index = Math.floorMod(tileX, CHUNK_SIZE) + Math.floorMod(tileY, CHUNK_SIZE) * CHUNK_SIZE;
			if(tiles[index] == null)
				tiles[index] = new TileDat((short)0);
			return tiles[index];
		}
	}

	private static class TileDat {
		protected short id, data;

		public TileDat(short id) {
			this.id = id;
			this.data = 0;
		}
	}
}
