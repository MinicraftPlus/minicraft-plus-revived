package minicraft.level.biome;

import minicraft.level.ChunkManager;
import minicraft.level.noise.LevelNoise;
import minicraft.level.tile.Tiles;
import minicraft.level.structure.Structures;

public class ForestBiome extends Biome {
	public ForestBiome() {
		super(-0.1f, 0, 0.5f);
		structures.add(Structures.skyStairsUp);
		structures.add(Structures.village);
	}

	public void generate(ChunkManager map, int x, int y) {
		LevelNoise noise = map.getTileNoise(x, y);

		double val = Math.abs(noise.getScale64Noise(x, y, 0) - noise.getScale64Noise(x, y, 1));

		if(noise.getTileNoise(x, y, 0) < 0.4)
			map.setTile(x, y, Tiles.get("tree"), 0);
		else
			map.setTile(x, y, Tiles.get("grass"), 0);
	}
}
