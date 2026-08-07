package minicraft.level.biome;

import java.util.Random;

import minicraft.level.ChunkManager;
import minicraft.level.noise.LevelNoise;
import minicraft.level.tile.Tiles;

public class DesertBiome extends Biome {
	public DesertBiome() {
		super(0.3f, 0, -0.6f);
	}

	public void generate(ChunkManager map, int x, int y) {
		LevelNoise noise = map.getTileNoise(x, y);
		double val = Math.abs(noise.getScale64Noise(x, y, 0) - noise.getScale64Noise(x, y, 1));
		double mval = Math.abs(Math.abs(noise.getScale16Noise(x, y, 0) - noise.getScale16Noise(x, y, 1)) - noise.getScale16Noise(x, y, 2));

		/*if(val < 0.07)
			map.setTile(x, y, Tiles.get("water"), 0);
		else*/ if(val > 0.5 && mval < 0.5) {
			map.setTile(x, y, Tiles.get("rock"), 0);

			if (val > 0.65 && mval < 0.35 && new Random(System.nanoTime()).nextDouble() < 0.005)
				map.setTile(x, y, Tiles.get("Stairs Down"), 0);
		}
		else if(noise.getScale8Noise(x, y, 0) < -0.25 && noise.getTileNoise(x, y, 1) < -0.4)
			map.setTile(x, y, Tiles.get("cactus"), 0);
		else
			map.setTile(x, y, Tiles.get("sand"), 0);
	}
}
