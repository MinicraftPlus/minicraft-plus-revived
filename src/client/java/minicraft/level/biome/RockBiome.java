package minicraft.level.biome;

import java.util.Random; // TODO: Make single-instance tiles (stairs) into structures

import minicraft.level.ChunkManager;
import minicraft.level.noise.LevelNoise;
import minicraft.level.tile.Tiles;

public class RockBiome extends Biome {
	public RockBiome() {
		super(0, 1.2f, 0, 0.6f);
	}

	public void generate(ChunkManager map, int x, int y) {
		LevelNoise noise = map.getTileNoise(x, y);
		if (noise.getHeight(x, y) > 0.35 && new Random(System.nanoTime()).nextDouble() < 0.002)
			map.setTile(x, y, Tiles.get("Stairs Down"), 0);
		else
			map.setTile(x, y, Tiles.get("rock"), 0);
	}
}
