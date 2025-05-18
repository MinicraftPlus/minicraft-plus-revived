package minicraft.level.biome;

import java.util.ArrayList;
import java.util.HashMap;

import minicraft.level.noise.LevelNoise;
import minicraft.level.ChunkManager;

public abstract class Biome {
	protected static final HashMap<Integer, BiomeCollection> BIOMES_LAYERS = new HashMap<>();

	private float temperature, height, humidity;

	public Biome(int level, String id, float temperature, float height, float humidity) {
		while(!BIOMES_LAYERS.containsKey(level))
			BIOMES_LAYERS.put(level, new BiomeCollection());
		this.temperature = temperature;
		this.height = height;
		this.humidity = humidity;
		BIOMES_LAYERS.get(level).addBiome(id, this);
	}

	public abstract void generate(ChunkManager map, int x, int y);

	public static BiomeCollection getLayerBiomes(int layer) {
		return BIOMES_LAYERS.get(layer);
	}

	public static class BiomeCollection {
		private HashMap<String, Biome> biomes;

		public BiomeCollection() {}

		public Biome getBiome(String name) {
			return biomes.get(name);
		}

		public Biome getClosestBiome(float temperature, float height, float humidity) {
			float minDist = Float.POSITIVE_INFINITY;
			Biome nearest = null;
			for (Biome b : biomes.values()) {
				float x = b.temperature - temperature;
				float y = b.height - height;
				float z = b.humidity - humidity;
				float dist = x * x + y * y + z * z;
				if (dist < minDist) {
					minDist = dist;
					nearest = b;
				}
			}
			return nearest;
		}

		protected void addBiome(String id, Biome b) {
			biomes.put(id, b);
		}
	}
}
