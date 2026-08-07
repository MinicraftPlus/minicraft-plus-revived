package minicraft.level.biome;

import minicraft.level.ChunkManager;
import minicraft.level.noise.LevelNoise;
import minicraft.level.tile.Tiles;

public class ForestBiome extends Biome {
	public ForestBiome() {
		super(-0.1f, 0, 0.5f);
	}

	public void generate(ChunkManager map, int x, int y) {
		LevelNoise noise = map.getTileNoise(x, y);

		double val = Math.abs(noise.getScale64Noise(x, y, 0) - noise.getScale64Noise(x, y, 1));

		/*if(val < 0.1)
			map.setTile(x, y, Tiles.get("water"), 0);
		else*/ if(noise.getTileNoise(x, y, 0) < 0.6)
			map.setTile(x, y, Tiles.get("tree"), 0);
		else
			map.setTile(x, y, Tiles.get("grass"), 0);
	}
}
