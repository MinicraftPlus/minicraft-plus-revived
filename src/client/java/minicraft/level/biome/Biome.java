package minicraft.level.biome;

import minicraft.level.ChunkManager;

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

	public abstract void generate(ChunkManager map, int x, int y);
}
