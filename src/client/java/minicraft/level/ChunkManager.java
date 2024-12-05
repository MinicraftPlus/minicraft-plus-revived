package minicraft.level;

import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;

public class ChunkManager {
	public Chunk[] chunks;
	public int levelWidth, levelHeight;
	private int widthInChunks, heightInChunks;

	public ChunkManager(int levelWidth, int levelHeight) {
		this.levelWidth = levelWidth;
		this.levelHeight = levelHeight;
		// Create enough chunks to cover the number of tiles provided
		widthInChunks = (int)Math.ceil((double)levelWidth / Chunk.SIZE);
		heightInChunks = (int)Math.ceil((double)levelHeight / Chunk.SIZE);
		chunks = new Chunk[widthInChunks * heightInChunks];
		for(int i = 0; i < widthInChunks * heightInChunks; i++)
			chunks[i] = new Chunk();
	}

	/**
	 * Return the chunk in which the tileX and tileY land, or null if out of bounds
	 */
	public Chunk getChunk(int tileX, int tileY) {
		if(tileX < 0 || tileY < 0 || tileX >= levelWidth || tileY >= levelHeight /* || (tileX + tileY * levelWidth / Chunk.SIZE) >= chunks.length */)
			return null;
		int cInd = tileX / Chunk.SIZE + (tileY / Chunk.SIZE) * widthInChunks;
		return chunks[cInd];
	}

	public Tile getTile(int x, int y) {
		Chunk c = getChunk(x, y);
		if(c == null) return Tiles.get("connector tile");
		return c.getTile(x, y);
	}

	public void setTile(int x, int y, Tile t, int dataVal) {
		Chunk c = getChunk(x, y);
		if(c == null) return;
		c.setTile(x, y, t, dataVal);
	}

	public int getData(int x, int y) {
		Chunk c = getChunk(x, y);
		if(c == null) return 0;
		return c.getData(x, y);
	}

	public void setData(int x, int y, int val) {
		Chunk c = getChunk(x, y);
		if(c == null) return;
		c.setData(x, y, val);
	}

}
