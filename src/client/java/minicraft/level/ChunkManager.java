package minicraft.level;

import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;

public class ChunkManager {

	public static final int CHUNK_SIZE = 64;

	/**
	 * 2D array for tile IDs and data.
	 * Outer array is widthInChunks x heightInChunks and represents all chunks
	 * Inner arrays is CHUNK_SIZE x CHUNK_SIZE and represents tile ids and data
	 *
	 * In the future, can be changed to
	 * Map<Integer, Map<Integer, List<Short>>> (or some equivalent) to allow negative coordinates and a pseudo-infinite map
	 */
	public short[][] chunkedData, chunkedTiles;

	public int levelWidth, levelHeight;
	private int widthInChunks, heightInChunks;

	public ChunkManager(int levelWidth, int levelHeight) {
		this.levelWidth = levelWidth;
		this.levelHeight = levelHeight;
		// Create enough chunks to cover the number of tiles provided
		widthInChunks = (int)Math.ceil((double)levelWidth / CHUNK_SIZE);
		heightInChunks = (int)Math.ceil((double)levelHeight / CHUNK_SIZE);
		chunkedData = new short[widthInChunks * heightInChunks][CHUNK_SIZE * CHUNK_SIZE];
		chunkedTiles = new short[widthInChunks * heightInChunks][CHUNK_SIZE * CHUNK_SIZE];
	}

	/**
	 * Return the chunk index in which the tileX and tileY land, or null if out of bounds
	 */
	private int getChunkIndex(int tileX, int tileY) {
		if(tileX < 0 || tileY < 0 || tileX >= levelWidth || tileY >= levelHeight /* || (tileX + tileY * levelWidth / Chunk.SIZE) >= chunks.length */)
			return -1;
		return tileX / CHUNK_SIZE + (tileY / CHUNK_SIZE) * widthInChunks;
	}

	/**
	* Returns a tile. After finding the right chunk, mods x and y to the range 0-CHUNK_SIZE as to never be out of bounds
	*/
	public Tile getTile(int x, int y) {
		int c = getChunkIndex(x, y);
		if(c == -1) return Tiles.get("connector tile");
		return Tiles.get(chunkedTiles[c][(x % CHUNK_SIZE) + (y % CHUNK_SIZE) * CHUNK_SIZE]);
	}

	/**
	* Updates a tile. After finding the right chunk, mods x and y to the range 0-CHUNK_SIZE as to never be out of bounds
	*/
	public void setTile(int x, int y, Tile t, int dataVal) {
		int c = getChunkIndex(x, y);
		if(c == -1) return;

		int tileInd = (x % CHUNK_SIZE) + (y % CHUNK_SIZE) * CHUNK_SIZE;
		chunkedTiles[c][tileInd] = t.id;
		chunkedData[c][tileInd] = (short) dataVal;
	}

	public int getData(int x, int y) {
		int c = getChunkIndex(x, y);
		if(c == -1) return 0;

		return chunkedData[c][(x % CHUNK_SIZE) + (y % CHUNK_SIZE) * CHUNK_SIZE] & 0xFFFF;
	}

	public void setData(int x, int y, int val) {
		int c = getChunkIndex(x, y);
		if(c == -1) return;

		chunkedData[c][(x % CHUNK_SIZE) + (y % CHUNK_SIZE) * CHUNK_SIZE] = (short) val;
	}

}
