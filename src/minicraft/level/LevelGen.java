package minicraft.level;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Random;

import minicraft.core.Game;
import minicraft.core.io.Settings;
import minicraft.level.tile.Tiles;
import minicraft.screen.WorldGenDisplay;

import org.jetbrains.annotations.Nullable;

public class LevelGen {
	private static long worldSeed = 0;
	private static final Random random = new Random(worldSeed);
	public double[] values; //An array of doubles, used to help making noise for the map
	private int w, h; // width and height of the map
	private static final int stairRadius = 15;
	
	/** This creates noise to create random values for level generation */
	public LevelGen(int w, int h, int featureSize) {
		this.w = w;
		this.h = h;
		
		values = new double[w * h];
		
		/// feature size likely determines how big the biomes are, in some way. It tends to be 16 or 32, in the code below. 
		for (int y = 0; y < w; y += featureSize) {
			for (int x = 0; x < w; x += featureSize) {
				setSample(x, y, random.nextFloat() * 2 - 1); // this method sets the random value from -1 to 1 at the given coordinate.
			}
		}
		
		int stepSize = featureSize;
		double scale = 2 / w;
		double scaleMod = 1;
		do {
			int halfStep = stepSize / 2;
			for (int y = 0; y < h; y += stepSize) { 
				for (int x = 0; x < w; x += stepSize) { // this loops through the values again, by a given increment...
					double a = sample(x, y); // fetches the value at the coordinate set previously (it fetches the exact same ones that were just set above)
					double b = sample(x + stepSize, y); // fetches the value at the next coordinate over. This could possibly loop over at the end, and fetch the first value in the row instead.
					double c = sample(x, y + stepSize); // fetches the next value down, possibly looping back to the top of the column. 
					double d = sample(x + stepSize, y + stepSize); // fetches the value one down, one right.
					
					/**
					 * This could probably use some explaining... Note: the number values are probably only good the first time around...
					 * 
					 * This starts with taking the average of the four numbers from before (they form a little square in adjacent tiles), each of which holds a value from -1 to 1.
					 * Then, it basically adds a 5th number, generated the same way as before. However, this 5th number is multiplied by a few things first...
					 * ...by stepSize, aka featureSize, and scale, which is 2/size the first time. featureSize is 16 or 32, which is a multiple of the common level size, 128.
					 * Precisely, it is 128 / 8, or 128 / 4, respectively with 16 and 32. So, the equation becomes size / const * 2 / size, or, simplified, 2 / const.
					 * For a feature size of 32, stepSize * scale = 2 / 4 = 1/2. featureSize of 16, it's 2 / 8 = 1/4. Later on, this gets closer to 4 / 4 = 1, so... the 5th value may not change much at all in later iterations for a feature size of 32, which means it has an effect of 1, which is actually quite significant to the value that is set.
					 * So, it tends to decrease the 5th -1 or 1 number, sometimes making it of equal value to the other 4 numbers, sort of. It will usually change the end result by 0.5 to 0.25, perhaps; at max.
					 */
					double e = (a + b + c + d) / 4.0 + (random.nextFloat() * 2 - 1) * stepSize * scale;
					setSample(x + halfStep, y + halfStep, e); // this sets the value that is right in the middle of the other 4 to an average of the four, plus a 5th number, which makes it slightly off, differing by about 0.25 or so on average, the first time around.
				}
			}
			
			// this loop does the same as before, but it takes into account some of the half steps we set in the last loop.
			for (int y = 0; y < h; y += stepSize) {
				for (int x = 0; x < w; x += stepSize) {
					double a = sample(x, y); // middle (current) tile
					double b = sample(x + stepSize, y); // right tile
					double c = sample(x, y + stepSize); // bottom tile
					double d = sample(x + halfStep, y + halfStep); // mid-right, mid-bottom tile
					double e = sample(x + halfStep, y - halfStep); // mid-right, mid-top tile
					double f = sample(x - halfStep, y + halfStep); // mid-left, mid-bottom tile
					
					// the 0.5 at the end is because we are going by half-steps..?
					// the H is for the right and surrounding mids, and g is the bottom and surrounding mids. 
					double H = (a + b + d + e) / 4.0 + (random.nextFloat() * 2 - 1) * stepSize * scale * 0.5; // adds middle, right, mr-mb, mr-mt, and random.
					double g = (a + c + d + f) / 4.0 + (random.nextFloat() * 2 - 1) * stepSize * scale * 0.5; // adds middle, bottom, mr-mb, ml-mb, and random.
					setSample(x + halfStep, y, H); // sets the H to the mid-right 
					setSample(x, y + halfStep, g); // sets the g to the mid-bottom
				}
			}
			
			/**
			 * THEN... this stuff is set to repeat the system all over again!
			 * The featureSize is halved, allowing access to further unset mids, and the scale changes...
			 * The scale increases the first time, x1.8, but the second time it's x1.1, and after that probably a little less than 1. So, it generally increases a bit, maybe to 4 / w at tops. This results in the 5th random value being more significant than the first 4 ones in later iterations. 
			 */
			stepSize /= 2;
			scale *= (scaleMod + 0.8);
			scaleMod *= 0.3;
		} while (stepSize > 1); // this stops when the stepsize is < 1, aka 0 b/c it's an int. At this point there are no more mid values.
	}
	
	private double sample(int x, int y) {
		return values[(x & (w - 1)) + (y & (h - 1)) * w];
	} // this merely returns the value, like Level.getTile(x, y).
	
	private void setSample(int x, int y, double value) {
		/**
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
	
	@Nullable
	static byte[][] createAndValidateMap(int w, int h, int level) {
		worldSeed = WorldGenDisplay.getSeed();
		
		if (level == 1)
			return createAndValidateSkyMap(w, h);
		if (level == 0)
			return createAndValidateTopMap(w, h);
		if (level == -4)
			return createAndValidateDungeon(w, h);
		
		if (level > -4 && level < 0)
			return createAndValidateUndergroundMap(w, h, -level);
		
		System.err.println("LevelGen ERROR: level index is not valid. Could not generate a level.");
		
		return null;
	}
	
	private static byte[][] createAndValidateTopMap(int w, int h) {
		random.setSeed(worldSeed);
		do {
			byte[][] result = createTopMap(w, h);
			
			int[] count = new int[256];
			
			for (int i = 0; i < w * h; i++) {
				count[result[0][i] & 0xff]++;
			}
			if (count[Tiles.get("rock").id & 0xff] < 100) continue;
			if (count[Tiles.get("sand").id & 0xff] < 100) continue;
			if (count[Tiles.get("grass").id & 0xff] < 100) continue;
			if (count[Tiles.get("tree").id & 0xff] < 100) continue;
			if (count[Tiles.get("Stairs Down").id & 0xff] < w / 21)
				continue; // size 128 = 6 stairs min
			
			return result;
			
		} while (true);
	}
	
	private static byte[][] createAndValidateUndergroundMap(int w, int h, int depth) {
		random.setSeed(worldSeed);
		do {
			byte[][] result = createUndergroundMap(w, h, depth);
			
			int[] count = new int[256];
			
			for (int i = 0; i < w * h; i++) {
				count[result[0][i] & 0xff]++;
			}
			if (count[Tiles.get("rock").id & 0xff] < 100) continue;
			if (count[Tiles.get("dirt").id & 0xff] < 100) continue;
			if (count[(Tiles.get("iron Ore").id & 0xff) + depth - 1] < 20) continue;
			
			if (depth < 3 && count[Tiles.get("Stairs Down").id & 0xff] < w / 32)
				continue; // size 128 = 4 stairs min
			
			return result;
			
		} while (true);
	}
	
	private static byte[][] createAndValidateDungeon(int w, int h) {
		random.setSeed(worldSeed);
		int attempt = 0;
		do {
			byte[][] result = createDungeon(w, h);
			
			int[] count = new int[256];
			
			for (int i = 0; i < w * h; i++) {
				count[result[0][i] & 0xff]++;
			}
			if (count[Tiles.get("Obsidian").id & 0xff] < 100) continue;
			if (count[Tiles.get("Obsidian Wall").id & 0xff] < 100) continue;
			
			return result;
			
		} while (true);
	}

	private static byte[][] createAndValidateSkyMap(int w, int h) {
		random.setSeed(worldSeed);
		int attempt = 0;
		do {
			byte[][] result = createSkyMap(w, h);
			
			int[] count = new int[256];
			
			for (int i = 0; i < w * h; i++) {
				count[result[0][i] & 0xff]++;
			}
			if (count[Tiles.get("cloud").id & 0xff] < 2000) continue;
			if (count[Tiles.get("Stairs Down").id & 0xff] < w / 64)
				continue; // size 128 = 2 stairs min
			
			return result;
			
		} while (true);
	}
	
	private static byte[][] createTopMap(int w, int h) { // create surface map?
		// creates a bunch of value maps, some with small size...
		LevelGen mnoise1 = new LevelGen(w, h, 16);
		LevelGen mnoise2 = new LevelGen(w, h, 16);
		LevelGen mnoise3 = new LevelGen(w, h, 16);
		// ...and some with larger size.
		LevelGen noise1 = new LevelGen(w, h, 32);
		LevelGen noise2 = new LevelGen(w, h, 32);
		
		byte[] map = new byte[w * h];
		byte[] data = new byte[w * h];

		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int i = x + y * w;
				
				double val = Math.abs(noise1.values[i] - noise2.values[i]) * 3 - 2;
				double mval = Math.abs(mnoise1.values[i] - mnoise2.values[i]);
				mval = Math.abs(mval - mnoise3.values[i]) * 3 - 2;
				
				// this calculates a sort of distance based on the current coordinate.
				double xd = x / (w - 1.0) * 2 - 1;
				double yd = y / (h - 1.0) * 2 - 1;
				if (xd < 0) xd = -xd;
				if (yd < 0) yd = -yd;
				double dist = xd >= yd ? xd : yd;
				dist = dist * dist * dist * dist;
				dist = dist * dist * dist * dist;
				val += 1 - dist*20;
				
				switch ((String) Settings.get("Type")) {
					case "Island":
						
						if (val < -0.5) {
							if (Settings.get("Theme").equals("Hell"))
								map[i] = Tiles.get("lava").id;
							else
								map[i] = Tiles.get("water").id;
						} else if (val > 0.5 && mval < -1.5) {
							map[i] = Tiles.get("rock").id;
						} else {
							map[i] = Tiles.get("grass").id;
						}
						
						break;
					case "Box":
						
						if (val < -1.5) {
							if (Settings.get("Theme").equals("Hell")) {
								map[i] = Tiles.get("lava").id;
							} else {
								map[i] = Tiles.get("water").id;
							}
						} else if (val > 0.5 && mval < -1.5) {
							map[i] = Tiles.get("rock").id;
						} else {
							map[i] = Tiles.get("grass").id;
						}
						
						break;
					case "Mountain":
						
						if (val < -0.4) {
							map[i] = Tiles.get("grass").id;
						} else if (val > 0.5 && mval < -1.5) {
							if (Settings.get("Theme").equals("Hell")) {
								map[i] = Tiles.get("lava").id;
							} else {
								map[i] = Tiles.get("water").id;
							}
						} else {
							map[i] = Tiles.get("rock").id;
						}
						break;
					
					case "Irregular":
						if (val < -0.5 && mval < -0.5) {
							if (Settings.get("Theme").equals("Hell")) {
								map[i] = Tiles.get("lava").id;
							}
							if (!Settings.get("Theme").equals("Hell")) {
								map[i] = Tiles.get("water").id;
							}
						} else if (val > 0.5 && mval < -1.5) {
							map[i] = Tiles.get("rock").id;
						} else {
							map[i] = Tiles.get("grass").id;
						}
						break;
				}
			}
		}
		
		if (Settings.get("Theme").equals("Desert")) {
			
			for (int i = 0; i < w * h / 200; i++) {
				int xs = random.nextInt(w);
				int ys = random.nextInt(h);
				for (int k = 0; k < 10; k++) {
					int x = xs + random.nextInt(21) - 10;
					int y = ys + random.nextInt(21) - 10;
					for (int j = 0; j < 100; j++) {
						int xo = x + random.nextInt(5) - random.nextInt(5);
						int yo = y + random.nextInt(5) - random.nextInt(5);
						for (int yy = yo - 1; yy <= yo + 1; yy++)
							for (int xx = xo - 1; xx <= xo + 1; xx++)
								if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
									if (map[xx + yy * w] == Tiles.get("grass").id) {
										map[xx + yy * w] = Tiles.get("sand").id;
									}
								}
					}
				}
			}
		}
		
		if (!Settings.get("Theme").equals("Desert")) {
			
			for (int i = 0; i < w * h / 2800; i++) {
				int xs = random.nextInt(w);
				int ys = random.nextInt(h);
				for (int k = 0; k < 10; k++) {
					int x = xs + random.nextInt(21) - 10;
					int y = ys + random.nextInt(21) - 10;
					for (int j = 0; j < 100; j++) {
						int xo = x + random.nextInt(5) - random.nextInt(5);
						int yo = y + random.nextInt(5) - random.nextInt(5);
						for (int yy = yo - 1; yy <= yo + 1; yy++)
							for (int xx = xo - 1; xx <= xo + 1; xx++)
								if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
									if (map[xx + yy * w] == Tiles.get("grass").id) {
										map[xx + yy * w] = Tiles.get("sand").id;
									}
								}
					}
				}
			}
		}
		
		if (Settings.get("Theme").equals("Forest")) {
			for (int i = 0; i < w * h / 200; i++) {
				int x = random.nextInt(w);
				int y = random.nextInt(h);
				for (int j = 0; j < 200; j++) {
					int xx = x + random.nextInt(15) - random.nextInt(15);
					int yy = y + random.nextInt(15) - random.nextInt(15);
					if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
						if (map[xx + yy * w] == Tiles.get("grass").id) {
							map[xx + yy * w] = Tiles.get("tree").id;
						}
					}
				}
			}
		}
		if (!Settings.get("Theme").equals("Forest") && !Settings.get("Theme").equals("Plain")) {
			for (int i = 0; i < w * h / 1200; i++) {
				int x = random.nextInt(w);
				int y = random.nextInt(h);
				for (int j = 0; j < 200; j++) {
					int xx = x + random.nextInt(15) - random.nextInt(15);
					int yy = y + random.nextInt(15) - random.nextInt(15);
					if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
						if (map[xx + yy * w] == Tiles.get("grass").id) {
							map[xx + yy * w] = Tiles.get("tree").id;
						}
					}
				}
			}
		}
		
		if (Settings.get("Theme").equals("Plain")) {
			for (int i = 0; i < w * h / 2800; i++) {
				int x = random.nextInt(w);
				int y = random.nextInt(h);
				for (int j = 0; j < 200; j++) {
					int xx = x + random.nextInt(15) - random.nextInt(15);
					int yy = y + random.nextInt(15) - random.nextInt(15);
					if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
						if (map[xx + yy * w] == Tiles.get("grass").id) {
							map[xx + yy * w] = Tiles.get("tree").id;
						}
					}
				}
			}
		}
		if (!Settings.get("Theme").equals("Plain")) {
			for (int i = 0; i < w * h / 400; i++) {
				int x = random.nextInt(w);
				int y = random.nextInt(h);
				for (int j = 0; j < 200; j++) {
					int xx = x + random.nextInt(15) - random.nextInt(15);
					int yy = y + random.nextInt(15) - random.nextInt(15);
					if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
						if (map[xx + yy * w] == Tiles.get("grass").id) {
							map[xx + yy * w] = Tiles.get("tree").id;
						}
					}
				}
			}
		}
		
		for (int i = 0; i < w * h / 400; i++) {
			int x = random.nextInt(w);
			int y = random.nextInt(h);
			int col = random.nextInt(4);
			for (int j = 0; j < 30; j++) {
				int xx = x + random.nextInt(5) - random.nextInt(5);
				int yy = y + random.nextInt(5) - random.nextInt(5);
				if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
					if (map[xx + yy * w] == Tiles.get("grass").id) {
						map[xx + yy * w] = Tiles.get("flower").id;
						data[xx + yy * w] = (byte) (col + random.nextInt(4) * 16); // data determines which way the flower faces
					}
				}
			}
		}
		
		for (int i = 0; i < w * h / 100; i++) {
			int xx = random.nextInt(w);
			int yy = random.nextInt(h);
			if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
				if (map[xx + yy * w] == Tiles.get("sand").id) {
					map[xx + yy * w] = Tiles.get("cactus").id;
				}
			}
		}
		
		int count = 0;
		
		//if (Game.debug) System.out.println("generating stairs for surface level...");
		
		stairsLoop:
		for (int i = 0; i < w * h / 100; i++) { // loops a certain number of times, more for bigger world sizes.
			int x = random.nextInt(w - 2) + 1;
			int y = random.nextInt(h - 2) + 1;
			
			// the first loop, which checks to make sure that a new stairs tile will be completely surrounded by rock. 
			for (int yy = y - 1; yy <= y + 1; yy++)
				for (int xx = x - 1; xx <= x + 1; xx++)
					if (map[xx + yy * w] != Tiles.get("rock").id)
						continue stairsLoop;
			
			// this should prevent any stairsDown tile from being within 30 tiles of any other stairsDown tile.
			for (int yy = Math.max(0, y - stairRadius); yy <= Math.min(h - 1, y + stairRadius); yy++)
				for (int xx = Math.max(0, x - stairRadius); xx <= Math.min(w - 1, x + stairRadius); xx++)
					if (map[xx + yy * w] == Tiles.get("Stairs Down").id)
						continue stairsLoop;
			
			map[x + y * w] = Tiles.get("Stairs Down").id;
			
			count++;
			if (count >= w / 21) break;
		}
		
		//System.out.println("min="+min);
		//System.out.println("max="+max);
		//average /= w*h;
		//System.out.println(average);
		
		return new byte[][]{map, data};
	}
	
	private static byte[][] createDungeon(int w, int h) {
		LevelGen noise1 = new LevelGen(w, h, 8);
		LevelGen noise2 = new LevelGen(w, h, 8);
		
		byte[] map = new byte[w * h];
		byte[] data = new byte[w * h];
		
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int i = x + y * w;
				
				double val = Math.abs(noise1.values[i] - noise2.values[i]) * 3 - 2;
				
				double xd = x / (w - 1.1) * 2 - 1;
				double yd = y / (h - 1.1) * 2 - 1;
				if (xd < 0) xd = -xd;
				if (yd < 0) yd = -yd;
				double dist = xd >= yd ? xd : yd;
				dist = dist * dist * dist * dist;
				dist = dist * dist * dist * dist;
				val = -val * 1 - 2.2;
				val += 1 - dist * 2;
				
				if (val < -0.35) {
					map[i] = Tiles.get("Obsidian Wall").id;
				} else {
					map[i] = Tiles.get("Obsidian").id;
				}
			}
		}
		
		lavaLoop:
		for (int i = 0; i < w * h / 450; i++) {
			int x = random.nextInt(w - 2) + 1;
			int y = random.nextInt(h - 2) + 1;
			
			for (int yy = y - 1; yy <= y + 1; yy++)
				for (int xx = x - 1; xx <= x + 1; xx++) {
					if (map[xx + yy * w] != Tiles.get("Obsidian Wall").id) continue lavaLoop;
				}
			
			Structure.lavaPool.draw(map, x, y, w);
		}
		
		return new byte[][]{map, data};
	}
	
	private static byte[][] createUndergroundMap(int w, int h, int depth) {
		LevelGen mnoise1 = new LevelGen(w, h, 16);
		LevelGen mnoise2 = new LevelGen(w, h, 16);
		LevelGen mnoise3 = new LevelGen(w, h, 16);
		
		LevelGen nnoise1 = new LevelGen(w, h, 16);
		LevelGen nnoise2 = new LevelGen(w, h, 16);
		LevelGen nnoise3 = new LevelGen(w, h, 16);
		
		LevelGen wnoise1 = new LevelGen(w, h, 16);
		LevelGen wnoise2 = new LevelGen(w, h, 16);
		LevelGen wnoise3 = new LevelGen(w, h, 16);
		
		LevelGen noise1 = new LevelGen(w, h, 32);
		LevelGen noise2 = new LevelGen(w, h, 32);
		
		byte[] map = new byte[w * h];
		byte[] data = new byte[w * h];
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int i = x + y * w;
				
				/// for the x=0 or y=0 i's, values[i] is always between -1 and 1.
				/// so, val is between -2 and 4.
				/// the rest are between -2 and 7.
				
				double val = Math.abs(noise1.values[i] - noise2.values[i]) * 3 - 2;
				
				double mval = Math.abs(mnoise1.values[i] - mnoise2.values[i]);
				mval = Math.abs(mval - mnoise3.values[i]) * 3 - 2;
				
				double nval = Math.abs(nnoise1.values[i] - nnoise2.values[i]);
				nval = Math.abs(nval - nnoise3.values[i]) * 3 - 2;
				
				double wval = Math.abs(wnoise1.values[i] - wnoise2.values[i]);
				wval = Math.abs(nval - wnoise3.values[i]) * 3 - 2;
				
				double xd = x / (w - 1.0) * 2 - 1;
				double yd = y / (h - 1.0) * 2 - 1;
				if (xd < 0) xd = -xd;
				if (yd < 0) yd = -yd;
				double dist = xd >= yd ? xd : yd;
				dist = Math.pow(dist, 8);
				val += 1 - dist * 20;
				
				if (val > -1 && wval < -1 + (depth) / 2 * 3) {
					if (depth == 3) map[i] = Tiles.get("lava").id;
					else if (depth == 1) map[i] = Tiles.get("dirt").id;
					else map[i] = Tiles.get("water").id;
				} else if (val > -2 && (mval < -1.7 || nval < -1.4)) {
					map[i] = Tiles.get("dirt").id;
				} else {
					map[i] = Tiles.get("rock").id;
				}
			}
		}
		{
			int r = 2;
			for (int i = 0; i < w * h / 400; i++) {
				int x = random.nextInt(w);
				int y = random.nextInt(h);
				for (int j = 0; j < 30; j++) {
					int xx = x + random.nextInt(5) - random.nextInt(5);
					int yy = y + random.nextInt(5) - random.nextInt(5);
					if (xx >= r && yy >= r && xx < w - r && yy < h - r) {
						if (map[xx + yy * w] == Tiles.get("rock").id) {
							map[xx + yy * w] = (byte) ((Tiles.get("iron Ore").id & 0xff) + depth - 1);
						}
					}
				}
				for (int j = 0; j < 10; j++) {
					int xx = x + random.nextInt(3) - random.nextInt(2);
					int yy = y + random.nextInt(3) - random.nextInt(2);
					if (xx >= r && yy >= r && xx < w - r && yy < h - r) {
						if (map[xx + yy * w] == Tiles.get("rock").id) {
							map[xx + yy * w] = (byte) (Tiles.get("Lapis").id & 0xff);
						}
					}
				}
			}
		}
		
		if (depth > 2) {
			int r = 1;
			int xx = 60;
			int yy = 60;
			for (int i = 0; i < w * h / 380; i++) {
				for (int j = 0; j < 10; j++) {
					if (xx < w - r && yy < h - r) {
						Structure.dungeonLock.draw(map, xx, yy, w);
						/// The "& 0xff" is a common way to convert a byte to an unsigned int, which basically prevents negative values... except... this doesn't do anything if you flip it back to a byte again...
						map[xx + yy * w] = (byte) (Tiles.get("Stairs Down").id & 0xff);
					}
				}
			}
		}
		
		if (depth < 3) {
			int count = 0;
			stairsLoop:
			for (int i = 0; i < w * h / 100; i++) {
				int x = random.nextInt(w - 20) + 10;
				int y = random.nextInt(h - 20) + 10;
				
				for (int yy = y - 1; yy <= y + 1; yy++)
					for (int xx = x - 1; xx <= x + 1; xx++)
						if (map[xx + yy * w] != Tiles.get("rock").id) continue stairsLoop;
				
				// this should prevent any stairsDown tile from being within 30 tiles of any other stairsDown tile.
				for (int yy = Math.max(0, y - stairRadius); yy <= Math.min(h - 1, y + stairRadius); yy++)
					for (int xx = Math.max(0, x - stairRadius); xx <= Math.min(w - 1, x + stairRadius); xx++)
						if (map[xx + yy * w] == Tiles.get("Stairs Down").id) continue stairsLoop;
				
				map[x + y * w] = Tiles.get("Stairs Down").id;
				count++;
				if (count >= w / 32) break;
			}
		}
		
		return new byte[][]{map, data};
	}
	
	private static byte[][] createSkyMap(int w, int h) {
		LevelGen noise1 = new LevelGen(w, h, 8);
		LevelGen noise2 = new LevelGen(w, h, 8);
		
		byte[] map = new byte[w * h];
		byte[] data = new byte[w * h];
		
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int i = x + y * w;
				
				double val = Math.abs(noise1.values[i] - noise2.values[i]) * 3 - 2;
				
				double xd = x / (w - 1.0) * 2 - 1;
				double yd = y / (h - 1.0) * 2 - 1;
				if (xd < 0) xd = -xd;
				if (yd < 0) yd = -yd;
				double dist = xd >= yd ? xd : yd;
				dist = dist * dist * dist * dist;
				dist = dist * dist * dist * dist;
				val = -val * 1 - 2.2;
				val += 1 - dist * 20;
				
				if (val < -0.25) {
					map[i] = Tiles.get("Infinite Fall").id;
				} else {
					map[i] = Tiles.get("cloud").id;
				}
			}
		}
		
		stairsLoop:
		for (int i = 0; i < w * h / 50; i++) {
			int x = random.nextInt(w - 2) + 1;
			int y = random.nextInt(h - 2) + 1;
			
			for (int yy = y - 1; yy <= y + 1; yy++)
				for (int xx = x - 1; xx <= x + 1; xx++) {
					if (map[xx + yy * w] != Tiles.get("cloud").id) continue stairsLoop;
				}
			
			map[x + y * w] = Tiles.get("Cloud Cactus").id;
		}
		
		int count = 0;
		stairsLoop:
		for (int i = 0; i < w * h; i++) {
			int x = random.nextInt(w - 2) + 1;
			int y = random.nextInt(h - 2) + 1;
			
			for (int yy = y - 1; yy <= y + 1; yy++)
				for (int xx = x - 1; xx <= x + 1; xx++) {
					if (map[xx + yy * w] != Tiles.get("cloud").id) continue stairsLoop;
				}
			
			// this should prevent any stairsDown tile from being within 30 tiles of any other stairsDown tile.
			for (int yy = Math.max(0, y - stairRadius); yy <= Math.min(h - 1, y + stairRadius); yy++)
				for (int xx = Math.max(0, x - stairRadius); xx <= Math.min(w - 1, x + stairRadius); xx++)
					if (map[xx + yy * w] == Tiles.get("Stairs Down").id) continue stairsLoop;
			
			map[x + y * w] = Tiles.get("Stairs Down").id;
			count++;
			if (count >= w / 64) break;
		}
		
		return new byte[][]{map, data};
	}
	
	public static void main(String[] args) {
		LevelGen.worldSeed = 0x100;
		
		// Fixes to get this method to work
		
		// AirWizard needs this in constructor
		Game.gameDir = "";
		
		Tiles.initTileList();
		// End of fixes
		
		int idx = -1;
		
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
			maplvls[0] = 0;
		}
		
		//noinspection InfiniteLoopStatement
		while (true) {
			int w = 128;
			int h = 128;
			
			int lvl = maplvls[idx++ % maplvls.length];
			if (lvl > 1 || lvl < -4) continue;
			byte[][] fullmap = LevelGen.createAndValidateMap(w, h, lvl);
			if (fullmap == null) continue;
			byte[] map = fullmap[0];
			
			BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
			int[] pixels = new int[w * h];
			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {
					int i = x + y * w;
					
					if (map[i] == Tiles.get("water").id) pixels[i] = 0x000080;
					if (map[i] == Tiles.get("iron Ore").id) pixels[i] = 0x000080;
					if (map[i] == Tiles.get("gold Ore").id) pixels[i] = 0x000080;
					if (map[i] == Tiles.get("gem Ore").id) pixels[i] = 0x000080;
					if (map[i] == Tiles.get("grass").id) pixels[i] = 0x208020;
					if (map[i] == Tiles.get("rock").id) pixels[i] = 0xa0a0a0;
					if (map[i] == Tiles.get("dirt").id) pixels[i] = 0x604040;
					if (map[i] == Tiles.get("sand").id) pixels[i] = 0xa0a040;
					if (map[i] == Tiles.get("Stone Bricks").id) pixels[i] = 0xa0a040;
					if (map[i] == Tiles.get("tree").id) pixels[i] = 0x003000;
					if (map[i] == Tiles.get("Obsidian Wall").id) pixels[i] = 0x0aa0a0;
					if (map[i] == Tiles.get("Obsidian").id) pixels[i] = 0x000000;
					if (map[i] == Tiles.get("lava").id) pixels[i] = 0xff2020;
					if (map[i] == Tiles.get("cloud").id) pixels[i] = 0xa0a0a0;
					if (map[i] == Tiles.get("Stairs Down").id) pixels[i] = 0xffffff;
					if (map[i] == Tiles.get("Stairs Up").id) pixels[i] = 0xffffff;
					if (map[i] == Tiles.get("Cloud Cactus").id) pixels[i] = 0xff00ff;
				}
			}
			img.setRGB(0, 0, w, h, pixels, 0, w);
			JOptionPane.showMessageDialog(null, null, "Another Map", JOptionPane.PLAIN_MESSAGE, new ImageIcon(img.getScaledInstance(w * 4, h * 4, Image.SCALE_AREA_AVERAGING)));
			if (LevelGen.worldSeed == 0x100)
				LevelGen.worldSeed = 0xAAFF20;
			else
				LevelGen.worldSeed = 0x100;
		}
	}
}
