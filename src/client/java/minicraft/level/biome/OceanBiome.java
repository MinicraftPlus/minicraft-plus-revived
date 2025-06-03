package minicraft.level.biome;

import minicraft.level.ChunkManager;
import minicraft.level.tile.Tiles;
import minicraft.level.noise.LevelNoise;

public class OceanBiome extends Biome {
	public OceanBiome() {
		super(0, -1f, 0.2f);
	}

	public void generate(ChunkManager map, int x, int y) {
		map.setTile(x, y, Tiles.get("water"), 0);
	}

	@Override
	public float getGenerationWeight(LevelNoise noise, int x, int y) {
		return (float)Math.pow(-noise.getHeight(x, y)*4, 5);
	}
}
