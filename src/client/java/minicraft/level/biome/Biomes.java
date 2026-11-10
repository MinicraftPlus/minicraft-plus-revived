package minicraft.level.biome;

import java.util.HashMap;
import minicraft.level.Level;
import minicraft.level.noise.LevelNoise;

public class Biomes {
	protected static final HashMap<Integer, BiomeCollection> BIOMES_LAYERS = new HashMap<>();

	public static void initBiomeList() {
		// Logging.SOMETHING.debug("Initializing biome list...");

		BIOMES_LAYERS.put(Level.SKY_LEVEL, new BiomeCollection());
		BIOMES_LAYERS.put(Level.SURFACE_LEVEL, new BiomeCollection());
		BIOMES_LAYERS.put(Level.IRON_LEVEL, new BiomeCollection());
		BIOMES_LAYERS.put(Level.GOLD_LEVEL, new BiomeCollection());
		BIOMES_LAYERS.put(Level.GEM_LEVEL, new BiomeCollection());
		BIOMES_LAYERS.put(Level.DUNGEON_LEVEL, new BiomeCollection());

		getLayerBiomes(Level.SKY_LEVEL).addBiome("Sky", new SkyBiome());
		getLayerBiomes(Level.SURFACE_LEVEL).addBiome("Surface", new SurfaceBiome());
		getLayerBiomes(Level.SURFACE_LEVEL).addBiome("Desert", new DesertBiome());
		getLayerBiomes(Level.SURFACE_LEVEL).addBiome("Forest", new ForestBiome());
		getLayerBiomes(Level.SURFACE_LEVEL).addBiome("Ocean", new OceanBiome());
		getLayerBiomes(Level.SURFACE_LEVEL).addBiome("River", new RiverBiome());
		getLayerBiomes(Level.SURFACE_LEVEL).addBiome("RiverBank", new RiverBankBiome());
		getLayerBiomes(Level.SURFACE_LEVEL).addBiome("Rock", new RockBiome());
		getLayerBiomes(Level.IRON_LEVEL).addBiome("IronCave", new IronCaveBiome());
		getLayerBiomes(Level.GOLD_LEVEL).addBiome("GoldCave", new GoldCaveBiome());
		getLayerBiomes(Level.GEM_LEVEL).addBiome("GemCave", new GemCaveBiome());
		getLayerBiomes(Level.DUNGEON_LEVEL).addBiome("Dungeon", new DungeonBiome());
	}

	public static BiomeCollection getLayerBiomes(int layer) {
		return BIOMES_LAYERS.get(layer);
	}

	public static class BiomeCollection {
		private HashMap<String, Biome> biomes;
		private HashMap<Biome, String> names;

		public BiomeCollection() {
			biomes = new HashMap<>();
			names = new HashMap<>();
		}

		public Biome getBiome(String name) {
			return biomes.get(name);
		}

		public String getName(Biome biome) {
			if(names.keySet().contains(biome))
				return names.get(biome);
			return "Unknown";
		}

		public Biome getClosestBiome(LevelNoise noise, int x, int y) {
			float maxWeight = Float.NEGATIVE_INFINITY;
			Biome closest = null;
			for (Biome b : biomes.values()) {
				float weight = b.getGenerationWeight(noise, x, y);
				if (weight > maxWeight) {
					maxWeight = weight;
					closest = b;
				}
			}
			return closest;
		}

		protected void addBiome(String id, Biome b) {
			biomes.put(id, b);
			names.put(b, id);
		}
	}
}
