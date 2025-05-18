package minicraft.level.noise;

import minicraft.util.Simplex;

public class LevelNoise {
	private final int w, h, layers, scale;
	private final Simplex noise;

	private double[] values;

	private static final int NOISE_LAYER_DIFF = 100;

	public LevelNoise(long seed, int xOffset, int yOffset, int zOffset, int w, int h, int layers, int scale) {
		noise = new Simplex(seed);
		this.w = w;
		this.h = h;
		this.layers = layers;
		this.scale = scale;

		values = new double[w * h * layers];

		for (int x = 0; x < w; x++) {
			for(int y = 0; y < h; y++) {
				for(int z = 0; z < layers; z++) {
					values[x + y * w + z * w * h] = noise.noise3((x + xOffset) / (double)scale, (y + yOffset) / (double)scale, (z + zOffset) * NOISE_LAYER_DIFF);
				}
			}
		}
	}

	public double sample(int x, int y, int layer) {
		if(layer < 0 || layer >= layers)
			throw new IndexOutOfBoundsException("Layer out of bounds!");
		return values[(x & (w - 1)) + (y & (h - 1)) * w + layer * w * h];
	}

	public int getScale() { return scale; }
	public int getLayers() { return layers; }
}
