package minicraft.level.biome;

import minicraft.level.ChunkManager;
import minicraft.level.tile.Tiles;
import minicraft.level.noise.LevelNoise;

public class RiverBiome extends Biome {
	public RiverBiome() {
		super(0, -0.1f, 0.1f, 0.2f);
	}

	public void generate(ChunkManager map, int x, int y) {
		map.setTile(x, y, Tiles.get("water"), 0);
	}

	@Override
	public float getGenerationWeight(LevelNoise noise, int x, int y) {
		int l = noise.getLayers();
		return Math.min(0.5f / (float) Math.abs(noise.getScale128Noise(x, y, l - 1) - noise.getScale128Noise(x, y, l - 2)), 10.f);
	}
}
