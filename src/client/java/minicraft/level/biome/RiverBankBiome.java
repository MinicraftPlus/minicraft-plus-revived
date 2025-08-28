package minicraft.level.biome;

import minicraft.level.ChunkManager;
import minicraft.level.tile.Tiles;
import minicraft.level.noise.LevelNoise;

public class RiverBankBiome extends Biome {
	public RiverBankBiome() {
		super(0, -0.1f, 0.1f, 0.3f);
	}

	public void generate(ChunkManager map, int x, int y) {
		map.setTile(x, y, Tiles.get("sand"), 0);
	}

	@Override
	public float getGenerationWeight(LevelNoise noise, int x, int y) {
		int l = noise.getLayers();
		return Math.min(0.55f / (float) Math.abs(noise.getScale128Noise(x, y, l - 1) - noise.getScale128Noise(x, y, l - 2)), 3.0f);
	}
}
