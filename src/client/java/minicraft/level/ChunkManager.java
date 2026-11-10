package minicraft.level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import minicraft.gfx.Point;
import minicraft.level.biome.Biome;
import minicraft.level.noise.LevelNoise;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;
import minicraft.level.structure.Structure;

public class ChunkManager {

	public static final int CHUNK_SIZE = 64;
	public static final int CHUNK_STAGE_NONE = 0;
	public static final int CHUNK_STAGE_PRIMED_NOISE = 1;
	public static final int CHUNK_STAGE_ASSIGNED_BIOMES = 2;
	public static final int CHUNK_STAGE_ASSIGNED_TILES = 3;
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

	public void unloadChunk(int cX, int cY) {
		if(chunks.containsKey(cX) && chunks.get(cX).containsKey(cY))
			chunks.get(cX).remove(cY);
	}

	private Chunk getChunk(int cX, int cY) {
		// If [cX][cY] are not keys in chunks, put them there
		if(!chunks.containsKey(cX))
			chunks.put(cX, new HashMap<>());
		if(!chunks.get(cX).containsKey(cY))
			chunks.get(cX).put(cY, new Chunk());
		return chunks.get(cX).get(cY);
	}

	/**
	 * Return the chunk  in which the tileX and tileY land
	 */
	private Chunk getTileChunk(int tileX, int tileY) {
		int cX = Math.floorDiv(tileX, CHUNK_SIZE), cY = Math.floorDiv(tileY, CHUNK_SIZE);
		return getChunk(cX, cY);
	}

	/**
	* Returns a tile. After finding the right chunk, mods x and y to the range 0-CHUNK_SIZE as to never be out of bounds
	*/
	public Tile getTile(int x, int y) {
		return Tiles.get(getTileChunk(x, y).getTileDat(x, y).id);
	}

	/**
	* Updates a tile. After finding the right chunk, mods x and y to the range 0-CHUNK_SIZE as to never be out of bounds
	*/
	public void setTile(int x, int y, Tile t, int dataVal) {
		TileDat dat = getTileChunk(x, y).getTileDat(x, y);
		dat.id = t.id;
		dat.data = (short) dataVal;
	}

	public int getData(int x, int y) {
		return getTileChunk(x, y).getTileDat(x, y).data;
	}

	public Biome getBiome(int x, int y) {
		return getTileChunk(x, y).getTileDat(x, y).getBiome();
	}

	public void setBiome(int x, int y, Biome b) {
		getTileChunk(x, y).setTileBiome(x, y, b);
	}

	public void setData(int x, int y, int val) {
		getTileChunk(x, y).getTileDat(x, y).data = (short) val;
	}

	/** Get generation data & information */
	public int getChunkStage(int chunkX, int chunkY) {
		// If [cX][cY] are not keys in chunks, stage must be 0
		if(!chunks.containsKey(chunkX) || !chunks.get(chunkX).containsKey(chunkY))
			return CHUNK_STAGE_NONE;
		return chunks.get(chunkX).get(chunkY).stage;
	}

	public HashSet<Biome> getChunkBiomes(int chunkX, int chunkY) {
		if(!chunks.containsKey(chunkX) || !chunks.get(chunkX).containsKey(chunkY))
			return new HashSet<Biome>();
		return chunks.get(chunkX).get(chunkY).biomes;
	}

	public HashMap<Structure, HashSet<Point>> getChunkLoadedStructures(int chunkX, int chunkY) {
		return getChunk(chunkX, chunkY).loadedStructures;
	}

	public LevelNoise getChunkNoise(int chunkX, int chunkY) {
		return getChunk(chunkX, chunkY).noise;
	}

	public LevelNoise getTileNoise(int x, int y) {
		return getTileChunk(x, y).noise;
	}

	public void setChunkStage(int chunkX, int chunkY, int stage) {
		getChunk(chunkX, chunkY).stage = (short)stage;

		// Let go of noise if no longer needed
		if(stage == CHUNK_STAGE_ASSIGNED_TILES)
			chunks.get(chunkX).get(chunkY).noise = null;
	}

	public void setChunkNoise(int chunkX, int chunkY, LevelNoise noise) {
		getChunk(chunkX, chunkY).noise = noise;
	}

	private static class Chunk {
		public HashMap<Structure, HashSet<Point>> loadedStructures = new HashMap<>();
		protected TileDat[] tiles;
		protected short stage = 0;
		protected LevelNoise noise = null;
		protected HashSet<Biome> biomes = new HashSet<>();
		public Chunk() {
			tiles = new TileDat[CHUNK_SIZE * CHUNK_SIZE];
		}

		public TileDat getTileDat(int tileX, int tileY) {
			int index = Math.floorMod(tileX, CHUNK_SIZE) + Math.floorMod(tileY, CHUNK_SIZE) * CHUNK_SIZE;
			if(tiles[index] == null) {
				tiles[index] = new TileDat((short)0);
			}
			return tiles[index];
		}

		public void setTileBiome(int tileX, int tileY, Biome b) {
			getTileDat(tileX, tileY).biome = b;
			biomes.add(b);
		}
	}

	private static class TileDat {
		protected Biome biome;
		protected short id, data;

		public TileDat(short id) {
			this.id = id;
			this.data = 0;
		}

		public Biome getBiome() {
			return biome;
		}
	}
}
