package minicraft.level.biome;

import java.util.HashMap;
import minicraft.level.LevelGen;

public class Biomes {
	protected static final HashMap<Integer, BiomeCollection> BIOMES_LAYERS = new HashMap<>();

	public static void initBiomeList() {
		// Logging.SOMETHING.debug("Initializing biome list...");

		BIOMES_LAYERS.put(LevelGen.SKY_LEVEL, new BiomeCollection());
		BIOMES_LAYERS.put(LevelGen.SURFACE_LEVEL, new BiomeCollection());
		BIOMES_LAYERS.put(LevelGen.IRON_LEVEL, new BiomeCollection());
		BIOMES_LAYERS.put(LevelGen.GOLD_LEVEL, new BiomeCollection());
		BIOMES_LAYERS.put(LevelGen.GEM_LEVEL, new BiomeCollection());
		BIOMES_LAYERS.put(LevelGen.DUNGEON_LEVEL, new BiomeCollection());

		getLayerBiomes(LevelGen.SKY_LEVEL).addBiome("Sky", new SkyBiome());
		getLayerBiomes(LevelGen.SURFACE_LEVEL).addBiome("Surface", new SurfaceBiome());
		getLayerBiomes(LevelGen.IRON_LEVEL).addBiome("IronCave", new IronCaveBiome());
		getLayerBiomes(LevelGen.GOLD_LEVEL).addBiome("GoldCave", new GoldCaveBiome());
		getLayerBiomes(LevelGen.GEM_LEVEL).addBiome("GemCave", new GemCaveBiome());
		getLayerBiomes(LevelGen.DUNGEON_LEVEL).addBiome("Dungeon", new DungeonBiome());
	}

	public static BiomeCollection getLayerBiomes(int layer) {
		return BIOMES_LAYERS.get(layer);
	}

	public static class BiomeCollection {
		private HashMap<String, Biome> biomes;

		public BiomeCollection() { biomes = new HashMap<>(); }

		public Biome getBiome(String name) {
			return biomes.get(name);
		}

		public Biome getClosestBiome(float temperature, float height, float humidity) {
			float minDist = Float.POSITIVE_INFINITY;
			Biome nearest = null;
			for (Biome b : biomes.values()) {
				float x = b.getTemperature() - temperature;
				float y = b.getHeight() - height;
				float z = b.getHumidity() - humidity;
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
