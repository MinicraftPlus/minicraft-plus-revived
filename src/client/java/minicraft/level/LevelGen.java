package minicraft.level;

import minicraft.core.Game;
import minicraft.core.io.Settings;
import minicraft.gfx.Point;
import minicraft.gfx.Rectangle;
import minicraft.level.tile.Tiles;
import minicraft.screen.RelPos;
import minicraft.util.Logging;
import minicraft.util.Simplex;

import org.tinylog.Logger;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class LevelGen {
	private static long worldSeed = 0;
	private static final Random random = new Random(worldSeed);
	private static final Simplex noise = new Simplex(worldSeed);
	public double[] values; // An array of doubles, used to help making noise for the map
	private final int w, h; // Width and height of the map
	private static final int stairRadius = 15;

	private static final int NOISE_LAYER_DIFF = 100;

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
				setSample(x, y, noise.noise3((x + xOffset) / (float)featureSize, (y + yOffset) / (float)featureSize, layer * NOISE_LAYER_DIFF));
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

	static void generateChunk(ChunkManager chunkManager, int x, int y, int level, long seed) {
		worldSeed = seed;

		if(level == 1)
			generateSkyChunk(chunkManager, x, y);
		else if(level == 0)
			generateTopChunk(chunkManager, x, y);
		else if(level == -4)
			generateDungeonChunk(chunkManager, x, y);
		else if(level > -4 && level < 0)
			generateUndergroundChunk(chunkManager, x, y, -level);
		else
			Logger.tag("LevelGen").error("Level index is not valid. Could not generate a chunk at " + x + ", " + y + " on level " + level + " with seed " + seed);

		chunkManager.setChunkStage(x, y, ChunkManager.CHUNK_STAGE_UNFINISHED_STAIRS);
	}

	static ChunkManager createAndValidateMap(int w, int h, int level, long seed) {
		ChunkManager cm = new ChunkManager();
		for(int i = 0; i < w / ChunkManager.CHUNK_SIZE; i++)
			for(int j = 0; j < h / ChunkManager.CHUNK_SIZE; j++)
				generateChunk(cm, i, j, level, seed);
		return cm;
	}

	private static void generateTopChunk(ChunkManager map, int chunkX, int chunkY) {
		random.setSeed(worldSeed);
		noise.setSeed(worldSeed);

		// Brevity
		int S = ChunkManager.CHUNK_SIZE;

		// creates a bunch of value maps, some with small size...
		LevelGen mnoise1 = new LevelGen(chunkX * S, chunkY * S, S, S, 16, 0);
		LevelGen mnoise2 = new LevelGen(chunkX * S, chunkY * S, S, S, 16, 1);
		LevelGen mnoise3 = new LevelGen(chunkX * S, chunkY * S, S, S, 16, 2);

		// ...and some with larger size.
		LevelGen noise1 = new LevelGen(chunkX * S, chunkY * S, S, S, 32, 3);
		LevelGen noise2 = new LevelGen(chunkX * S, chunkY * S, S, S, 32, 4);

    List<Point> rocks = new ArrayList<>();

		int tileX = chunkX * S, tileY = chunkY * S;
		for(int y = tileY; y < tileY + S; y++)
			for(int x = tileX; x < tileX + S; x++) {
				int i = (x - tileX) + (y - tileY) * S;
				double val = Math.abs(noise1.values[i] - noise2.values[i]) * 3 - 1;
				double mval = Math.abs(mnoise1.values[i] - mnoise2.values[i]);
				mval = Math.abs(mval - mnoise3.values[i]) * 3 - 2;

				// This calculates a sort of distance based on the current coordinate.

				switch ((String) Settings.get("Type")) {
					case "minicraft.settings.type.island":

						if (val < -0.5) {
							if (Settings.get("Theme").equals("minicraft.settings.theme.hell"))
								map.setTile(x, y, Tiles.get("lava"), 0);
							else
								map.setTile(x, y, Tiles.get("water"), 0);
						} else if (val > 0.5 && mval < -1.5) {
							rocks.add(new Point(x,y));
							map.setTile(x, y, Tiles.get("rock"), 0);
						} else {
							map.setTile(x, y, Tiles.get("grass"), 0);
						}

						break;
					case "minicraft.settings.type.box":

						if (val < -1.5) {
							if (Settings.get("Theme").equals("minicraft.settings.theme.hell")) {
								map.setTile(x, y, Tiles.get("lava"), 0);
							} else {
								map.setTile(x, y, Tiles.get("water"), 0);
							}
						} else if (val > 0.5 && mval < -1.5) {
							rocks.add(new Point(x,y));
							map.setTile(x, y, Tiles.get("rock"), 0);
						} else {
							map.setTile(x, y, Tiles.get("grass"), 0);
						}

						break;
					case "minicraft.settings.type.mountain":

						if (val < -0.4) {
							map.setTile(x, y, Tiles.get("grass"), 0);
						} else if (val > 0.5 && mval < -1.5) {
							if (Settings.get("Theme").equals("minicraft.settings.theme.hell")) {
								map.setTile(x, y, Tiles.get("lava"), 0);
							} else {
								map.setTile(x, y, Tiles.get("water"), 0);
							}
						} else {
							rocks.add(new Point(x,y));
							map.setTile(x, y, Tiles.get("rock"), 0);
						}
						break;

					case "minicraft.settings.type.irregular":
						if (val < -0.5 && mval < -0.5) {
							if (Settings.get("Theme").equals("minicraft.settings.theme.hell")) {
								map.setTile(x, y, Tiles.get("lava"), 0);
							}
							if (!Settings.get("Theme").equals("minicraft.settings.theme.hell")) {
								map.setTile(x, y, Tiles.get("water"), 0);
							}
						} else if (val > 0.5 && mval < -1.5) {
							rocks.add(new Point(x,y));
							map.setTile(x, y, Tiles.get("rock"), 0);
						} else {
							map.setTile(x, y, Tiles.get("grass"), 0);
						}
						break;
				}

			}

		if (Settings.get("Theme").equals("minicraft.settings.theme.desert")) {
			for (int i = 0; i < S * S / 200; i++) {
				int xs = random.nextInt(S);
				int ys = random.nextInt(S);
				for (int k = 0; k < 10; k++) {
					int x = xs + random.nextInt(21) - 10;
					int y = ys + random.nextInt(21) - 10;
					for (int j = 0; j < 100; j++) {
						int xo = x + random.nextInt(5) - random.nextInt(5);
						int yo = y + random.nextInt(5) - random.nextInt(5);
						for (int yy = yo - 1; yy <= yo + 1; yy++)
							for (int xx = xo - 1; xx <= xo + 1; xx++)
								if (xx >= 0 && yy >= 0 && xx < S && yy < S) {
									if (map.getTile(xx + tileX, yy + tileY) == Tiles.get("grass")) {
										map.setTile(xx + tileX, yy + tileY, Tiles.get("sand"), 0);
									}
								}
					}
				}
			}
		}

		if (!Settings.get("Theme").equals("minicraft.settings.theme.desert")) {
			for (int i = 0; i < S * S / 2800; i++) {
				int xs = random.nextInt(S);
				int ys = random.nextInt(S);
				for (int k = 0; k < 10; k++) {
					int x = xs + random.nextInt(21) - 10;
					int y = ys + random.nextInt(21) - 10;
					for (int j = 0; j < 100; j++) {
						int xo = x + random.nextInt(5) - random.nextInt(5);
						int yo = y + random.nextInt(5) - random.nextInt(5);
						for (int yy = yo - 1; yy <= yo + 1; yy++)
							for (int xx = xo - 1; xx <= xo + 1; xx++)
								if (xx >= 0 && yy >= 0 && xx < S && yy < S) {
									if (map.getTile(xx + tileX, yy + tileY) == Tiles.get("grass")) {
										map.setTile(xx + tileX, yy + tileY, Tiles.get("sand"), 0);
									}
								}
					}
				}
			}
		}

		if (Settings.get("Theme").equals("minicraft.settings.theme.forest")) {
			for (int i = 0; i < S * S / 200; i++) {
				int x = random.nextInt(S);
				int y = random.nextInt(S);
				for (int j = 0; j < 200; j++) {
					int xx = x + random.nextInt(15) - random.nextInt(15);
					int yy = y + random.nextInt(15) - random.nextInt(15);
					if (xx >= 0 && yy >= 0 && xx < S && yy < S) {
						if (map.getTile(xx + tileX, yy + tileY) == Tiles.get("grass")) {
							map.setTile(xx + tileX, yy + tileY, Tiles.get("tree"), 0);
						}
					}
				}
			}
		}
		if (!Settings.get("Theme").equals("minicraft.settings.theme.forest") && !Settings.get("Theme").equals("minicraft.settings.theme.plain")) {
			for (int i = 0; i < S * S / 1200; i++) {
				int x = random.nextInt(S);
				int y = random.nextInt(S);
				for (int j = 0; j < 200; j++) {
					int xx = x + random.nextInt(15) - random.nextInt(15);
					int yy = y + random.nextInt(15) - random.nextInt(15);
					if (xx >= 0 && yy >= 0 && xx < S && yy < S) {
						if (map.getTile(xx + tileX, yy + tileY) == Tiles.get("grass")) {
							map.setTile(xx + tileX, yy + tileY, Tiles.get("tree"), 0);
						}
					}
				}
			}
		}

		if (Settings.get("Theme").equals("minicraft.settings.theme.plain")) {
			for (int i = 0; i < S * S / 2800; i++) {
				int x = random.nextInt(S);
				int y = random.nextInt(S);
				for (int j = 0; j < 200; j++) {
					int xx = x + random.nextInt(15) - random.nextInt(15);
					int yy = y + random.nextInt(15) - random.nextInt(15);
					if (xx >= 0 && yy >= 0 && xx < S && yy < S) {
						if (map.getTile(xx + tileX, yy + tileY) == Tiles.get("grass")) {
							map.setTile(xx + tileX, yy + tileY, Tiles.get("tree"), 0);
						}
					}
				}
			}
		}
		if (!Settings.get("Theme").equals("minicraft.settings.theme.plain")) {
			for (int i = 0; i < S * S / 400; i++) {
				int x = random.nextInt(S);
				int y = random.nextInt(S);
				for (int j = 0; j < 200; j++) {
					int xx = x + random.nextInt(15) - random.nextInt(15);
					int yy = y + random.nextInt(15) - random.nextInt(15);
					if (xx >= 0 && yy >= 0 && xx < S && yy < S) {
						if (map.getTile(xx + tileX, yy + tileY) == Tiles.get("grass")) {
							map.setTile(xx + tileX, yy + tileY, Tiles.get("tree"), 0);
						}
					}
				}
			}
		}

		for (int i = 0; i < S * S / 400; i++) {
			int x = random.nextInt(S);
			int y = random.nextInt(S);
			int col = random.nextInt(4) * random.nextInt(4);
			for (int j = 0; j < 30; j++) {
				int xx = x + random.nextInt(5) - random.nextInt(5);
				int yy = y + random.nextInt(5) - random.nextInt(5);
				if (xx >= 0 && yy >= 0 && xx < S && yy < S) {
					if (map.getTile(xx + tileX, yy + tileY) == Tiles.get("grass")) {
						map.setTile(xx + tileX, yy + tileY, Tiles.get("flower"), col + random.nextInt(3)); // Data determines what the flower is
					}
				}
			}
		}

		for (int i = 0; i < S * S / 100; i++) {
			int xx = random.nextInt(S);
			int yy = random.nextInt(S);
			if (xx < S && yy < S) {
				if (map.getTile(xx + tileX, yy + tileY) == Tiles.get("sand")) {
					map.setTile(xx + tileX, yy + tileY, Tiles.get("cactus"), 0);
				}
			}
		}

		int count = 0;

		stairsLoop:
		while(!rocks.isEmpty() && count < S / 21) { // Loops a certain number of times, more for bigger world sizes.
			int i = random.nextInt(rocks.size());

			Point p = rocks.get(i);
			rocks.remove(i);

			// The first loop, which checks to make sure that a new stairs tile will be completely surrounded by rock.
			for (int yy = p.y - 1; yy <= p.y + 1; yy++)
				for (int xx = p.x - 1; xx <= p.x + 1; xx++)
					if (map.getTile(xx, yy) != Tiles.get("rock"))
						continue stairsLoop;

			// This should prevent any stairsDown tile from being within 30 tiles of any other stairsDown tile.
			for (int yy = p.y - stairRadius; yy <= p.y + stairRadius; yy++)
				for (int xx = p.x - stairRadius; xx <= p.x + stairRadius; xx++)
					if (map.getTile(xx, yy) == Tiles.get("Stairs Down"))
						continue stairsLoop;

			map.setTile(p.x, p.y, Tiles.get("Stairs Down"), 0);
			count++;
		}
	}

	private static void generateDungeonChunk(ChunkManager map, int chunkX, int chunkY) {
		int S = ChunkManager.CHUNK_SIZE;
		LevelGen noise1 = new LevelGen(chunkX * S, chunkY * S, S, S, 10, 0);
		LevelGen noise2 = new LevelGen(chunkX * S, chunkY * S, S, S, 10, 1);

		int tileX = chunkX * S, tileY = chunkY * S;
		for(int y = tileY; y < tileY + S; y++) {
			for(int x = tileX; x < tileX + S; x++) {
				int i = (x - tileX) + (y - tileY) * S;

				double val = Math.abs(noise1.values[i] - noise2.values[i]) * -3 + 3.5;

				if (val < -0.05) {
					map.setTile(x, y, Tiles.get("Obsidian Wall"), 0);
				} else if (val >= -0.05 && val < -0.03) {
					map.setTile(x, y, Tiles.get("Lava"), 0);
				} else {
					if (random.nextInt(2) == 1) {
						if (random.nextInt(2) == 1) {
							map.setTile(x, y, Tiles.get("Obsidian"), 0);
						} else {
							map.setTile(x, y, Tiles.get("Raw Obsidian"), 0);
						}
					} else {
						map.setTile(x, y, Tiles.get("dirt"), 0);
					}
				}
			}
		}

		decorLoop:
		for (int i = 0; i < S * S / 450; i++) {
			int x = random.nextInt(S - 2) + 1;
			int y = random.nextInt(S - 2) + 1;

			for (int yy = y - 1; yy <= y + 1; yy++) {
				for (int xx = x - 1; xx <= x + 1; xx++) {
					if (map.getTile(xx, yy) != Tiles.get("Obsidian"))
						continue decorLoop;
				}
			}

			if (x > 8 && y > 8) {
				if (x < S - 8 && y < S - 8) {
					if (random.nextInt(2) == 0)
						Structure.ornateLavaPool.draw(map, x, y);
				}
			}
		}
	}

	private static void generateUndergroundChunk(ChunkManager map, int chunkX, int chunkY, int depth) {
		random.setSeed(worldSeed);
		noise.setSeed(worldSeed);
		int S = ChunkManager.CHUNK_SIZE;
		LevelGen mnoise1 = new LevelGen(chunkX * S, chunkY * S, S, S, 16, depth * 11 + 0);
		LevelGen mnoise2 = new LevelGen(chunkX * S, chunkY * S, S, S, 16, depth * 11 + 1);
		LevelGen mnoise3 = new LevelGen(chunkX * S, chunkY * S, S, S, 16, depth * 11 + 2);

		LevelGen nnoise1 = new LevelGen(chunkX * S, chunkY * S, S, S, 16, depth * 11 + 3);
		LevelGen nnoise2 = new LevelGen(chunkX * S, chunkY * S, S, S, 16, depth * 11 + 4);
		LevelGen nnoise3 = new LevelGen(chunkX * S, chunkY * S, S, S, 16, depth * 11 + 5);

		LevelGen wnoise1 = new LevelGen(chunkX * S, chunkY * S, S, S, 16, depth * 11 + 6);
		LevelGen wnoise2 = new LevelGen(chunkX * S, chunkY * S, S, S, 16, depth * 11 + 7);
		LevelGen wnoise3 = new LevelGen(chunkX * S, chunkY * S, S, S, 16, depth * 11 + 8);

		LevelGen noise1 = new LevelGen(chunkX * S, chunkY * S, S, S, 32, depth * 11 + 9);
		LevelGen noise2 = new LevelGen(chunkX * S, chunkY * S, S, S, 32, depth * 11 + 10);

		int tileX = chunkX * S, tileY = chunkY * S;
		for(int y = tileY; y < tileY + S; y++) {
			for(int x = tileX; x < tileX + S; x++) {
				int i = (x - tileX) + (y - tileY) * S;

				double val = Math.abs(noise1.values[i] - noise2.values[i]) * 3 - 2;

				double mval = Math.abs(mnoise1.values[i] - mnoise2.values[i]);
				mval = Math.abs(mval - mnoise3.values[i]) * 3 - 2;

				double nval = Math.abs(nnoise1.values[i] - nnoise2.values[i]);
				nval = Math.abs(nval - nnoise3.values[i]) * 3 - 2;

				double wval = Math.abs(wnoise1.values[i] - wnoise2.values[i]);
				wval = Math.abs(wval - wnoise3.values[i]) * 3 - 2;

				if (val > -1 && wval < -1 + (depth) / 2.0 * 3) {
					if (depth == 3) map.setTile(x, y, Tiles.get("lava"), 0);
					else if (depth == 1) map.setTile(x, y, Tiles.get("dirt"), 0);
					else map.setTile(x, y, Tiles.get("water"), 0);

				} else if (val > -2 && (mval < -1.7 || nval < -1.4)) {
					map.setTile(x, y, Tiles.get("dirt"), 0);

				} else {
					map.setTile(x, y, Tiles.get("rock"), 0);
				}
			}
		}
		{
			int r = 2;
			for (int i = 0; i < S * S / 200; i++) {
				int x = tileX + random.nextInt(S);
				int y = tileY + random.nextInt(S);
				for (int j = 0; j < 30; j++) {
					int xx = x + random.nextInt(5) - random.nextInt(5);
					int yy = y + random.nextInt(5) - random.nextInt(5);
					// if (xx >= r && yy >= r && xx < S - r && yy < S - r) {
						if (map.getTile(xx, yy) == Tiles.get("rock")) {
							map.setTile(xx, yy, Tiles.get((short)(Tiles.get("iron Ore").id + depth - 1)), 0);
						}
					// }
				}
				for (int j = 0; j < 10; j++) {
					int xx = x + random.nextInt(3) - random.nextInt(2);
					int yy = y + random.nextInt(3) - random.nextInt(2);
					// if (xx >= r && yy >= r && xx < S - r && yy < S - r) {
						if (map.getTile(xx, yy) == Tiles.get("rock")) {
							map.setTile(xx, yy, Tiles.get("Lapis"), 0);
						}
					// }
				}
			}
		}

		if (depth < 3) {
			int count = 0;
			stairsLoop:
			for (int i = 0; i < S * S / 100; i++) {
				int x = tileX + random.nextInt(S);
				int y = tileY + random.nextInt(S);

				for (int yy = y - 1; yy <= y + 1; yy++)
					for (int xx = x - 1; xx <= x + 1; xx++)
						if (map.getTile(xx, yy) != Tiles.get("rock")) continue stairsLoop;

				// This should prevent any stairsDown tile from being within 30 tiles of any other stairsDown tile.
				for (int yy = Math.max(0, y - stairRadius); yy <= Math.min(S - 1, y + stairRadius); yy++)
					for (int xx = Math.max(0, x - stairRadius); xx <= Math.min(S - 1, x + stairRadius); xx++)
						if (map.getTile(xx, yy) == Tiles.get("Stairs Down")) continue stairsLoop;

				map.setTile(x, y, Tiles.get("Stairs Down"), 0);
				count++;
				if (count >= S / 32) break;
			}
		}

	}

	private static void generateSkyChunk(ChunkManager map, int chunkX, int chunkY) {
		int S = ChunkManager.CHUNK_SIZE;
		LevelGen noise1 = new LevelGen(S * chunkX, S * chunkY, S, S, 8, 0);
		LevelGen noise2 = new LevelGen(S * chunkX, S * chunkY, S, S, 8, 1);

		int tileX = chunkX * S, tileY = chunkY * S;
		for(int y = tileY; y < tileY + S; y++) {
			for(int x = tileX; x < tileX + S; x++) {
				int i = (x - tileX) + (y - tileY) * S;

				double val = Math.abs(noise1.values[i] - noise2.values[i]) * 3 - 2;

				double dist = 0;
				val = -val * 1 - 2.2;
				val += 1.75 - dist * 20;

				if (val < -0.25) {
					map.setTile(x, y, Tiles.get("Infinite Fall"), 0);
				} else {
					map.setTile(x, y, Tiles.get("cloud"), 0);
				}
			}
		}

		stairsLoop:
		for (int i = 0; i < S * S / 50; i++) {
			int x = tileX + random.nextInt(S - 2) + 1;
			int y = tileY + random.nextInt(S - 2) + 1;

			for (int yy = y - 1; yy <= y + 1; yy++) {
				for (int xx = x - 1; xx <= x + 1; xx++) {
					if (map.getTile(xx, yy) == Tiles.get("Infinite Fall")) continue stairsLoop;
				}
			}

			map.setTile(x, y, Tiles.get("Cloud Cactus"), 0);
		}

		int count = 0;
		stairsLoop:
		for (int i = 0; i < S * S; i++) {
			int x = random.nextInt(S - 2) + 1;
			int y = random.nextInt(S - 2) + 1;

			for (int yy = y - 1; yy <= y + 1; yy++) {
				for (int xx = x - 1; xx <= x + 1; xx++) {
					if (map.getTile(xx, yy) != Tiles.get("cloud")) continue stairsLoop;
				}
			}

			// This should prevent any stairsDown tile from being within 30 tiles of any other stairsDown tile.
			for (int yy = Math.max(0, y - stairRadius); yy <= Math.min(S - 1, y + stairRadius); yy++)
				for (int xx = Math.max(0, x - stairRadius); xx <= Math.min(S - 1, x + stairRadius); xx++)
					if (map.getTile(xx, yy) == Tiles.get("Stairs Down")) continue stairsLoop;

			map.setTile(x, y, Tiles.get("Stairs Down"), 0);
			count++;
			if (count >= S / 64) break;
		}
	}

	public static void main(String[] args) {
		LevelGen.worldSeed = 0x100;

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
			int w = 128;
			int h = 128;

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
					if (map.getTile(x, y) == Tiles.get("Obsidian Wall")) pixels[i] = 0x0aa0a0;
					if (map.getTile(x, y) == Tiles.get("Obsidian")) pixels[i] = 0x000000;
					if (map.getTile(x, y) == Tiles.get("lava")) pixels[i] = 0xffff2020;
					if (map.getTile(x, y) == Tiles.get("cloud")) pixels[i] = 0xa0a0a0;
					if (map.getTile(x, y) == Tiles.get("Stairs Down")) pixels[i] = 0xffffffff;
					if (map.getTile(x, y) == Tiles.get("Stairs Up")) pixels[i] = 0xffffffff;
					if (map.getTile(x, y) == Tiles.get("Cloud Cactus")) pixels[i] = 0xffff00ff;
					if (map.getTile(x, y) == Tiles.get("Ornate Obsidian")) pixels[i] = 0x000f0a;
					if (map.getTile(x, y) == Tiles.get("Raw Obsidian")) pixels[i] = 0x0a0080;
				}
			}
			img.setRGB(0, 0, w, h, pixels, 0, w);
			int op = JOptionPane.showOptionDialog(null, null, "Map With Seed " + worldSeed, JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
				new ImageIcon(img.getScaledInstance(w * 4, h * 4, Image.SCALE_AREA_AVERAGING)), new String[] { "Next", "0x100", "0xAAFF20" }, "Next");
			if (op == 1) LevelGen.worldSeed = 0x100;
			else if (op == 2) LevelGen.worldSeed = 0xAAFF20;
			else LevelGen.worldSeed++;
		}
	}

	// Used to easily interface with a list of chunks
}
