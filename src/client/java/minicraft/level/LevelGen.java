package minicraft.level;

import minicraft.core.io.Settings;
import minicraft.gfx.Point;

import minicraft.level.biome.Biome;
import minicraft.level.biome.Biomes;
import minicraft.level.noise.LevelNoise;

import minicraft.core.Game;

import minicraft.level.tile.Tiles;
import minicraft.util.Simplex;
import org.tinylog.Logger;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LevelGen {
	private static long worldSeed = 0;
	private static final Random random = new Random(worldSeed);
	private static final Simplex noise = new Simplex(worldSeed);
	public double[] values; // An array of doubles, used to help making noise for the map
	private final int w, h; // Width and height of the map
	private static final int stairRadius = 15;

	private static final int NOISE_LAYER_DIFF = 100;

	public static final int SKY_LEVEL = 1, SURFACE_LEVEL = 0, IRON_LEVEL = -1, GOLD_LEVEL = -2, GEM_LEVEL = -3, DUNGEON_LEVEL = -4;

	/**
	 * This creates noise to create random values for level generation
	 */
	public LevelGen(int w, int h, int featureSize) { this(0, 0, w, h, featureSize, 0); }

	public LevelGen(int w, int h, int featureSize, int layer) { this(0, 0, w, h, featureSize, layer); }

	public LevelGen(int xOffset, int yOffset, int w, int h, int featureSize) { this(xOffset, yOffset, w, h, featureSize, 0); }

	public LevelGen(int xOffset, int yOffset, int w, int h, int featureSize, int layer) {
		this.w = w;
		this.h = h;

		values = new double[w * h];

		for(int x = 0; x < w; x++)
			for(int y = 0; y < h; y++) {
				setSample(x, y, noise.noise3((x + xOffset) / (double)featureSize, (y + yOffset) / (double)featureSize, layer * NOISE_LAYER_DIFF));
			}
	}

	private double sample(int x, int y) {
		return values[(x & (w - 1)) + (y & (h - 1)) * w];
	} // This merely returns the value, like Level.getTile(x, y).

	private void setSample(int x, int y, double value) {
		/*
		 * This method is short, but difficult to understand. This is what I think it does:
		 *
		 * The values array is like a 2D array, but formatted into a 1D array; so the basic "x + y * w" is used to access a given value.
		 *
		 * The value parameter is a random number, above set to be a random decimal from -1 to 1.
		 *
		 * From above, we can see that the x and y values passed in range from 0 to the width/height, and increment by a certain constant known as the "featureSize".
		 * This implies that the locations chosen from this array, to put the random value in, somehow determine the size of biomes, perhaps.
		 * The x/y value is taken and AND'ed with the size-1, which could be 127. This just caps the value at 127; however, it shouldn't be higher in the first place, so it is merely a safety measure.
		 *
		 * In other words, this is just "values[x + y * w] = value;"
		 */
		values[(x & (w - 1)) + (y & (h - 1)) * w] = value;
	}

	static void advanceChunk(ChunkManager chunkManager, int x, int y, int level, long seed) {
		int stage = chunkManager.getChunkStage(x, y);
		final int S = ChunkManager.CHUNK_SIZE;
		LevelNoise noise = chunkManager.getChunkNoise(x, y);
		switch(stage) {
			case ChunkManager.CHUNK_STAGE_NONE:
				chunkManager.setChunkNoise(x, y, new LevelNoise(seed, S * x, S * y, 0, S, S));
				chunkManager.setChunkStage(x, y, ChunkManager.CHUNK_STAGE_PRIMED_NOISE);
				break;
			case ChunkManager.CHUNK_STAGE_PRIMED_NOISE:
				for(int tileX = 0; tileX < S; tileX++)
					for(int tileY = 0; tileY < S; tileY++) {
						Biome biome = Biomes.getLayerBiomes(level).getClosestBiome(noise, tileX, tileY);
						chunkManager.setBiome(tileX + S * x, tileY + S * y, biome);
					}
				chunkManager.setChunkStage(x, y, ChunkManager.CHUNK_STAGE_ASSIGNED_BIOMES);
				break;
			case ChunkManager.CHUNK_STAGE_ASSIGNED_BIOMES:
				for(int tileX = 0; tileX < S; tileX++)
					for(int tileY = 0; tileY < S; tileY++) {
						chunkManager.getBiome(tileX + S * x, tileY + S * y).generate(chunkManager, tileX + S * x, tileY + S * y);
					}
				chunkManager.setChunkStage(x, y, ChunkManager.CHUNK_STAGE_UNFINISHED_STAIRS);
				break;
		}
	}

	static ChunkManager createAndValidateMap(int w, int h, int level, long seed) {
		ChunkManager cm = new ChunkManager();
		for(int i = 0; i < w / ChunkManager.CHUNK_SIZE; i++)
			for(int j = 0; j < h / ChunkManager.CHUNK_SIZE; j++)
				for(int k = ChunkManager.CHUNK_STAGE_NONE; k < ChunkManager.CHUNK_STAGE_UNFINISHED_STAIRS; k++)
					advanceChunk(cm, i, j, level, seed);
		return cm;
	}

	public static void main(String[] args) {
		LevelGen.worldSeed = 0x100;
		Biomes.initBiomeList();

		// Fixes to get this method to work

		// AirWizard needs this in constructor
		Game.gameDir = "";

		Tiles.initTileList();
		// End of fixes

		int idx = -2;

		int[] maplvls = new int[args.length];
		boolean valid = true;
		if (maplvls.length > 0) {
			for (int i = 0; i < args.length; i++) {
				try {
					int lvlnum = Integer.parseInt(args[i]);
					maplvls[i] = lvlnum;
				} catch (Exception ex) {
					valid = false;
					break;
				}
			}
		} else valid = false;

		if (!valid) {
			maplvls = new int[1];
		}

		//noinspection InfiniteLoopStatement
		while (true) {
			int w = 1024;
			int h = 1024;

			//noinspection ConstantConditions
			int lvl = maplvls[idx++ % maplvls.length];
			if (lvl > 1 || lvl < -4) continue;

			ChunkManager map = LevelGen.createAndValidateMap(w, h, lvl, LevelGen.worldSeed);

			if (map == null) continue;

			BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
			int[] pixels = new int[w * h];
			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {
					int i = x + y * w;

					if (map.getTile(x, y) == Tiles.get("water")) pixels[i] = 0x000080;
					if (map.getTile(x, y) == Tiles.get("iron Ore")) pixels[i] = 0x000080;
					if (map.getTile(x, y) == Tiles.get("gold Ore")) pixels[i] = 0x000080;
					if (map.getTile(x, y) == Tiles.get("gem Ore")) pixels[i] = 0x000080;
					if (map.getTile(x, y) == Tiles.get("grass")) pixels[i] = 0x208020;
					if (map.getTile(x, y) == Tiles.get("rock")) pixels[i] = 0xa0a0a0;
					if (map.getTile(x, y) == Tiles.get("dirt")) pixels[i] = 0x604040;
					if (map.getTile(x, y) == Tiles.get("sand")) pixels[i] = 0xa0a040;
					if (map.getTile(x, y) == Tiles.get("Stone Bricks")) pixels[i] = 0xa0a040;
					if (map.getTile(x, y) == Tiles.get("tree")) pixels[i] = 0x003000;
					if (map.getTile(x, y) == Tiles.get("flower")) pixels[i] = 0xa050ff;
					if (map.getTile(x, y) == Tiles.get("cactus")) pixels[i] = 0x105000;
					if (map.getTile(x, y) == Tiles.get("Obsidian Wall")) pixels[i] = 0x0aa0a0;
					if (map.getTile(x, y) == Tiles.get("Obsidian")) pixels[i] = 0x000000;
					if (map.getTile(x, y) == Tiles.get("lava")) pixels[i] = 0xffff2020;
					if (map.getTile(x, y) == Tiles.get("cloud")) pixels[i] = 0xa0a0a0;
					if (map.getTile(x, y) == Tiles.get("Stairs Down")) pixels[i] = 0xff0000ff;
					if (map.getTile(x, y) == Tiles.get("Stairs Up")) pixels[i] = 0xff00ffff;
					if (map.getTile(x, y) == Tiles.get("Cloud Cactus")) pixels[i] = 0xffff00ff;
					if (map.getTile(x, y) == Tiles.get("Ornate Obsidian")) pixels[i] = 0x000f0a;
					if (map.getTile(x, y) == Tiles.get("Raw Obsidian")) pixels[i] = 0x0a0080;

					// if(map.getTileNoise(x, y) == null)
						// map.setChunkNoise(x / ChunkManager.CHUNK_SIZE, y / ChunkManager.CHUNK_SIZE, new LevelNoise(LevelGen.worldSeed, x, y, 0, 64, 64));
					// pixels[i] = (int)Math.max(Math.min(map.getTileNoise(x, y).getHeight(x, y)*127+128, 255), 0);
				}
			}
			img.setRGB(0, 0, w, h, pixels, 0, w);

			try {
				ImageIO.write(img, "png", new File("./seed" + LevelGen.worldSeed + ".png"));
			} catch(IOException ignored) {}

			int op = JOptionPane.showOptionDialog(null, null, "Map With Seed " + worldSeed, JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
				new ImageIcon(img.getScaledInstance(w, h, Image.SCALE_AREA_AVERAGING)), new String[] { "Next", "0x100", "0xAAFF20" }, "Next");
			if (op == 1) LevelGen.worldSeed = 0x100;
			else if (op == 2) LevelGen.worldSeed = 0xAAFF20;
			else LevelGen.worldSeed++;
		}
	}

	// Used to easily interface with a list of chunks
}
