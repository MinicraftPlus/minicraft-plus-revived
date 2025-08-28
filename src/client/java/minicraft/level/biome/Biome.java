package minicraft.level.biome;

import minicraft.level.ChunkManager;
import minicraft.level.noise.LevelNoise;

public abstract class Biome {
	private float temperature, height, humidity, rarity;

	public Biome(float temperature, float height, float humidity, float rarity) {
		this.temperature = temperature;
		this.height = height;
		this.humidity = humidity;
		this.rarity = rarity;
	}

	public Biome(float temperature, float height, float humidity) {
		this(temperature, height, humidity, 1.f);
	}

	public float getTemperature() { return temperature; }
	public float getHeight() { return height; }
	public float getHumidity() { return humidity; }
	public float getRarity() { return rarity; }

	public float getGenerationWeight(LevelNoise noise, int tx, int ty) {
		float x = (float)noise.getTemperature(tx, ty) - temperature;
		float y = (float)noise.getHeight(tx, ty) - height;
		float z = (float)noise.getHumidity(tx, ty) - humidity;
		return rarity / (x * x + y * y + z * z);
	}

	public abstract void generate(ChunkManager map, int x, int y);
}
