package minicraft.level.biome;

import java.util.ArrayList;
import java.util.Random; // TODO: Make single-instance tiles (stairs) into structures

import minicraft.level.ChunkManager;
import minicraft.level.LevelGen;
import minicraft.level.noise.LevelNoise;
import minicraft.level.tile.Tiles;

public class SkyBiome extends Biome {
	public SkyBiome() {
		super(0, 0, 0);
	}

	public void generate(ChunkManager map, int x, int y) {
		LevelNoise noise = map.getTileNoise(x, y).get(1);
		double val = Math.abs(noise.sample(x, y, 0) - noise.sample(x, y, 1));

		if(val < 0.15)
			map.setTile(x, y, Tiles.get("Infinite Fall"), 0);
		else if (noise.sample(x, y, 3) < -0.65)
			map.setTile(x, y, Tiles.get("Cloud Cactus"), 0);
		else if (new Random(System.nanoTime()).nextDouble() < 0.0001 * 5)
			map.setTile(x, y, Tiles.get("Stairs Down"), 0);
		else
			map.setTile(x, y, Tiles.get("cloud"), 0);
	}
}
