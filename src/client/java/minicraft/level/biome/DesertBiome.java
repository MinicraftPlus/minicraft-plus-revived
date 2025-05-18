package minicraft.level.biome;

import java.util.Random;

import minicraft.level.ChunkManager;
import minicraft.level.LevelGen;
import minicraft.level.noise.LevelNoise;
import minicraft.level.tile.Tiles;

public class DesertBiome extends Biome {
	public DesertBiome() {
		super(0.25f, 0, -0.5f);
	}

	public void generate(ChunkManager map, int x, int y) {
		LevelNoise n16 = map.getTileNoise(x, y).get(1);
		LevelNoise n32 = map.getTileNoise(x, y).get(2);
		LevelNoise n8 = map.getTileNoise(x, y).get(3);
		LevelNoise n1 = map.getTileNoise(x, y).get(4);
		double val = Math.abs(n32.sample(x, y, 0) - n32.sample(x, y, 1));
		double mval = Math.abs(Math.abs(n16.sample(x, y, 0) - n16.sample(x, y, 1)) - n16.sample(x, y, 2));

		if(val < 0.10)
			map.setTile(x, y, Tiles.get("water"), 0);
		else if(val > 0.5 && mval < 0.5) {
			map.setTile(x, y, Tiles.get("rock"), 0);

			if (val > 0.65 && mval < 0.35 && new Random(System.nanoTime()).nextDouble() < 0.01 * 5)
				map.setTile(x, y, Tiles.get("Stairs Down"), 0);
		}
		else if(n8.sample(x, y, 0) < -0.25 && n1.sample(x, y, 0) < -0.4)
			map.setTile(x, y, Tiles.get("cactus"), 0);
		else
			map.setTile(x, y, Tiles.get("sand"), 0);
	}
}
