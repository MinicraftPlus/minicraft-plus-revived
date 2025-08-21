package minicraft.level.biome;

import minicraft.level.ChunkManager;
import minicraft.level.tile.Tiles;

public class RiverBiome extends Biome {
	public RiverBiome() {
		super(0, -0.1f, 0.1f, 0.2f);
	}

	public void generate(ChunkManager map, int x, int y) {
		map.setTile(x, y, Tiles.get("water"), 0);
	}
}
