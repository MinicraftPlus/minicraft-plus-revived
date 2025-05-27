package minicraft.level.biome;

import minicraft.level.ChunkManager;
import minicraft.level.tile.Tiles;

public class OceanBiome extends Biome {
	public OceanBiome() {
		super(0, -1f, 0.2f);
	}

	public void generate(ChunkManager map, int x, int y) {
		map.setTile(x, y, Tiles.get("water"), 0);
	}
}
