package minicraft.level.noise;

import java.security.InvalidParameterException;

import minicraft.util.Simplex;

public class LevelNoise {
	private final int w, h, layers;
	private final Simplex noise;

	// Store lists of noise at different scales
	private double[] noise1;
	private double[] noise4;
	private double[] noise8;
	private double[] noise16;
	private double[] noise32;
	private double[] noise64;
	private double[] noise128;
	private double[] noise512;
	private double[] noise2048;
	private double[] noise8192;

	private static final int NOISE_LAYER_DIFF = 100;

	public LevelNoise(long seed, int xOffset, int yOffset, int zOffset, int w, int h, int layers) {

		if(layers < 3)
			throw new InvalidParameterException("LevelNoise must have at least 3 layers of noise available (temperature, height, humidity)");

		noise = new Simplex(seed);
		this.w = w;
		this.h = h;
		this.layers = layers;

		noise1 = new double[w * h * layers];
		noise4 = new double[w * h * layers];
		noise8 = new double[w * h * layers];
		noise16 = new double[w * h * layers];
		noise32 = new double[w * h * layers];
		noise64 = new double[w * h * layers];
		noise128 = new double[w * h * layers];
		noise512 = new double[w * h * layers];
		noise2048 = new double[w * h * layers];
		noise8192 = new double[w * h * layers];

		for (int x = 0; x < w; x++) {
			for(int y = 0; y < h; y++) {
				for(int z = 0; z < layers; z++) {
					noise1[x + y * w + z * w * h] = noise.noise3((x + xOffset) / 1.0, (y + yOffset) / 1.0, (z + zOffset + layers * 0) * NOISE_LAYER_DIFF);
					noise4[x + y * w + z * w * h] = noise.noise3((x + xOffset) / 4.0, (y + yOffset) / 4.0, (z + zOffset + layers * 1) * NOISE_LAYER_DIFF);
					noise8[x + y * w + z * w * h] = noise.noise3((x + xOffset) / 8.0, (y + yOffset) / 8.0, (z + zOffset + layers * 2) * NOISE_LAYER_DIFF);
					noise16[x + y * w + z * w * h] = noise.noise3((x + xOffset) / 16.0, (y + yOffset) / 16.0, (z + zOffset + layers * 3) * NOISE_LAYER_DIFF);
					noise32[x + y * w + z * w * h] = noise.noise3((x + xOffset) / 32.0, (y + yOffset) / 32.0, (z + zOffset + layers * 4) * NOISE_LAYER_DIFF);
					noise64[x + y * w + z * w * h] = noise.noise3((x + xOffset) / 64.0, (y + yOffset) / 64.0, (z + zOffset + layers * 5) * NOISE_LAYER_DIFF);
					noise128[x + y * w + z * w * h] = noise.noise3((x + xOffset) / 128.0, (y + yOffset) / 128.0, (z + zOffset + layers * 6) * NOISE_LAYER_DIFF);
					noise512[x + y * w + z * w * h] = noise.noise3((x + xOffset) / 512.0, (y + yOffset) / 512.0, (z + zOffset + layers * 7) * NOISE_LAYER_DIFF);
					noise2048[x + y * w + z * w * h] = noise.noise3((x + xOffset) / 2048.0, (y + yOffset) / 2048.0, (z + zOffset + layers * 8) * NOISE_LAYER_DIFF);
					noise8192[x + y * w + z * w * h] = noise.noise3((x + xOffset) / 8192.0, (y + yOffset) / 8192.0, (z + zOffset + layers * 9) * NOISE_LAYER_DIFF);
				}
			}
		}
	}

	public LevelNoise(long seed, int xOffset, int yOffset, int zOffset, int w, int h) {
		this(seed, xOffset, yOffset, zOffset, w, h, 10);
	}

	private double sample(double[] values, int x, int y, int layer) {
		if(layer < 0 || layer >= layers)
			throw new IndexOutOfBoundsException("Layer out of bounds!");
		return values[(x & (w - 1)) + (y & (h - 1)) * w + layer * w * h];
	}

	public int getLayers() { return layers; }

	public double getTileNoise(int x, int y, int layer) { return sample(noise1, x, y, layer); }
	public double getScale4Noise(int x, int y, int layer) { return sample(noise4, x, y, layer); }
	public double getScale8Noise(int x, int y, int layer) { return sample(noise8, x, y, layer); }
	public double getScale16Noise(int x, int y, int layer) { return sample(noise16, x, y, layer); }
	public double getScale32Noise(int x, int y, int layer) { return sample(noise32, x, y, layer); }
	public double getScale64Noise(int x, int y, int layer) { return sample(noise64, x, y, layer); }
	public double getScale128Noise(int x, int y, int layer) { return sample(noise128, x, y, layer); }
	public double getScale512Noise(int x, int y, int layer) { return sample(noise512, x, y, layer); }
	public double getScale2048Noise(int x, int y, int layer) { return sample(noise2048, x, y, layer); }
	public double getScale8192Noise(int x, int y, int layer) { return sample(noise8192, x, y, layer); }

	public double octave(int x, int y, int layer, double scale1, double scale4, double scale8, double scale16, double scale32, double scale64, double scale128, double scale512, double scale2048, double scale8192) {
		return getTileNoise(x, y, layer) * scale1
			+ getScale4Noise(x, y, layer) * scale4
			+ getScale8Noise(x, y, layer) * scale8
			+ getScale16Noise(x, y, layer) * scale16
			+ getScale32Noise(x, y, layer) * scale32
			+ getScale64Noise(x, y, layer) * scale64
			+ getScale128Noise(x, y, layer) * scale128
			+ getScale512Noise(x, y, layer) * scale512
			+ getScale2048Noise(x, y, layer) * scale2048
			+ getScale8192Noise(x, y, layer) * scale8192;
	}

	public double getTemperature(int x, int y) {
		return octave(x, y, layers - 1, 0.01, 0.02, 0.04, 0.08, 0.16, 0.32, 0.64, 0.05, 0.04, 0.01);
	}

	public double getHeight(int x, int y) {
		return octave(x, y, layers - 2, 0.005, 0.01, 0.02, 0.01, 0.02, 0.05, 0.1, 0.2, 0.7, 0.2);
	}

	public double getHumidity(int x, int y) {
		return octave(x, y, layers - 3, 0.02, 0.04, 0.07, 0.1, 0.4, 0.3, 0.1, 0.05, 0.02, 0.01);
	}
}
