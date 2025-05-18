package minicraft.level.biome;

import java.util.ArrayList;

import minicraft.level.ChunkManager;
import minicraft.level.LevelGen;
import minicraft.level.noise.LevelNoise;
import minicraft.level.tile.Tiles;

public class SkyBiome extends Biome {
	private static final SkyBiome INSTANCE = new SkyBiome();

	public SkyBiome() {
		super(LevelGen.SKY_LEVEL, "Sky", 0, 0, 0);
		Biome.BIOMES_LAYERS.put(LevelGen.SURFACE_LEVEL, Biome.BIOMES_LAYERS.get(LevelGen.SKY_LEVEL));
		Biome.BIOMES_LAYERS.put(LevelGen.IRON_LEVEL, Biome.BIOMES_LAYERS.get(LevelGen.SKY_LEVEL));
		Biome.BIOMES_LAYERS.put(LevelGen.GOLD_LEVEL, Biome.BIOMES_LAYERS.get(LevelGen.SKY_LEVEL));
		Biome.BIOMES_LAYERS.put(LevelGen.GEM_LEVEL, Biome.BIOMES_LAYERS.get(LevelGen.SKY_LEVEL));
		Biome.BIOMES_LAYERS.put(LevelGen.DUNGEON_LEVEL, Biome.BIOMES_LAYERS.get(LevelGen.SKY_LEVEL));
	}

	public void generate(ChunkManager map, int x, int y) {
		LevelNoise noise = map.getTileNoise(x, y).get(0);
		double val = Math.abs(noise.sample(x, y, 0) - noise.sample(x, y, 1));

		if(val < 0.25)
			map.setTile(x, y, Tiles.get("Infinite Fall"), 0);
		else {
			map.setTile(x, y, Tiles.get("cloud"), 0);
			if (noise.sample(x, y, 3) < 0.0005)
				map.setTile(x, y, Tiles.get("Cloud Cactus"), 0);
			if (noise.sample(x, y, 4) < 0.0001)
				map.setTile(x, y, Tiles.get("Stairs Down"), 0);
		}
	}
}
