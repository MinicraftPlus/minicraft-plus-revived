package minicraft.level.biome;

import java.util.ArrayList;
import java.util.HashMap;

import minicraft.level.noise.LevelNoise;
import minicraft.level.ChunkManager;

public abstract class Biome {
	private float temperature, height, humidity;

	public Biome(float temperature, float height, float humidity) {
		this.temperature = temperature;
		this.height = height;
		this.humidity = humidity;
	}

	public float getTemperature() { return temperature; }
	public float getHeight() { return height; }
	public float getHumidity() { return humidity; }

	public abstract void generate(ChunkManager map, int x, int y);
}
