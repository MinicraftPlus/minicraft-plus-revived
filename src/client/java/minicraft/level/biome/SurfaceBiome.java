package minicraft.level.biome;

import java.util.Random; // TODO: Make single-instance tiles (stairs) into structures

import minicraft.level.ChunkManager;
import minicraft.level.noise.LevelNoise;
import minicraft.level.tile.Tiles;

public class SurfaceBiome extends Biome {
	public SurfaceBiome() {
		super(-0.25f, 0, 0.3f);
	}

	public void generate(ChunkManager map, int x, int y) {
		LevelNoise noise = map.getTileNoise(x, y);

		double val = Math.abs(noise.getScale64Noise(x, y, 0) - noise.getScale64Noise(x, y, 1));
		double mval = Math.abs(Math.abs(noise.getScale16Noise(x, y, 0) - noise.getScale16Noise(x, y, 1)) - noise.getScale16Noise(x, y, 2));

		if(val > 0.75 && mval < 0.35) {
			map.setTile(x, y, Tiles.get("rock"), 0);

			if (val > 0.85 && mval < 0.25 && new Random(System.nanoTime()).nextDouble() < 0.005)
				map.setTile(x, y, Tiles.get("Stairs Down"), 0);
		}
		else if(noise.getScale16Noise(x, y, 2) < -0.6 && noise.getTileNoise(x, y, 0) < 0.4)
			map.setTile(x, y, Tiles.get("tree"), 0);
		else if(noise.getScale32Noise(x, y, 2) + noise.getScale8Noise(x, y, 0) < -0.6 && noise.getTileNoise(x, y, 1) < -0.4)
			map.setTile(x, y, Tiles.get("flower"), (int)Math.abs(noise.getScale8Noise(x, y, 1)*24) % 12);
		else
			map.setTile(x, y, Tiles.get("grass"), 0);
	}
}
