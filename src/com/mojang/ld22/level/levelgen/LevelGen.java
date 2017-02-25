package com.mojang.ld22.level.levelgen;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import com.mojang.ld22.Game;
import com.mojang.ld22.level.tile.Tile;
import com.mojang.ld22.screen.WorldGenMenu;

public class LevelGen {
	private static final Random random = new Random();
	public double[] values;
	private int w, h;
	private static int d = 0;
	
	public LevelGen(int w, int h, int featureSize) {
		this.w = w;
		this.h = h;

		values = new double[w * h];

		for (int y = 0; y < w; y += featureSize) {
			for (int x = 0; x < w; x += featureSize) {
				setSample(x, y, random.nextFloat() * 2 - 1);
			}
		}

		int stepSize = featureSize;
		double scale = 2 / w;
		double scaleMod = 1;
		do {
			int halfStep = stepSize / 2;
			for (int y = 0; y < w; y += stepSize) {
				for (int x = 0; x < w; x += stepSize) {
					double a = sample(x, y);
					double b = sample(x + stepSize, y);
					double c = sample(x, y + stepSize);
					double d = sample(x + stepSize, y + stepSize);

					double e = (a + b + c + d) / 4.0 + (random.nextFloat() * 2 - 1) * stepSize * scale;
					setSample(x + halfStep, y + halfStep, e);
				}
			}
			for (int y = 0; y < w; y += stepSize) {
				for (int x = 0; x < w; x += stepSize) {
					double a = sample(x, y);
					double b = sample(x + stepSize, y);
					double c = sample(x, y + stepSize);
					double d = sample(x + halfStep, y + halfStep);
					double e = sample(x + halfStep, y - halfStep);
					double f = sample(x - halfStep, y + halfStep);

					double H = (a + b + d + e) / 4.0 + (random.nextFloat() * 2 - 1) * stepSize * scale * 0.5;
					double g = (a + c + d + f) / 4.0 + (random.nextFloat() * 2 - 1) * stepSize * scale * 0.5;
					setSample(x + halfStep, y, H);
					setSample(x, y + halfStep, g);
				}
			}
			stepSize /= 2;
			scale *= (scaleMod + 0.8);
			scaleMod *= 0.3;
		} while (stepSize > 1);
	}

	private double sample(int x, int y) {
		return values[(x & (w - 1)) + (y & (h - 1)) * w];
	}

	private void setSample(int x, int y, double value) {
		values[(x & (w - 1)) + (y & (h - 1)) * w] = value;
	}

	public static byte[][] createAndValidateTopMap(int w, int h) {
		int attempt = 0;
		do {
			byte[][] result = createTopMap(w, h);

			int[] count = new int[256];

			for (int i = 0; i < w * h; i++) {
				count[result[0][i] & 0xff]++;
			}
			if (count[Tile.rock.id & 0xff] < 100) continue;
			if (count[Tile.sand.id & 0xff] < 100) continue;
			if (count[Tile.grass.id & 0xff] < 100) continue;
			if (count[Tile.tree.id & 0xff] < 100) continue;
			if (WorldGenMenu.sized == 128){
			if (count[Tile.stairsDown.id & 0xff] < 6) continue;
			} else {
			if (count[Tile.stairsDown.id & 0xff] < 12) continue;
			}

			return result;

		} while (true);
	}
	

	public static byte[][] createAndValidateUndergroundMap(int w, int h, int depth) {
		int attempt = 0;
		do {
			byte[][] result = createUndergroundMap(w, h, depth);

			int[] count = new int[256];

			for (int i = 0; i < w * h; i++) {
				count[result[0][i] & 0xff]++;
			}
			if (count[Tile.rock.id & 0xff] < 100) continue;
			if (count[Tile.dirt.id & 0xff] < 100) continue;
			if (count[(Tile.ironOre.id & 0xff) + depth - 1] < 20) continue;
			if (WorldGenMenu.sized == 128){
			if (depth < 3) if (count[Tile.stairsDown.id & 0xff] < 4) continue;
			} else {
			if (depth < 3) if (count[Tile.stairsDown.id & 0xff] < 8) continue;
			}

			return result;

		} while (true);
	}
	
	public static byte[][] createAndValidateDungeon(int w, int h) {
		int attempt = 0;
		do {
			byte[][] result = createDungeon(w, h);

			int[] count = new int[256];

			for (int i = 0; i < w * h; i++) {
				count[result[0][i] & 0xff]++;
			}
			if (count[Tile.o.id & 0xff] < 100) continue;
			if (count[Tile.ow.id & 0xff] < 100) continue;

			return result;

		} while (true);
	}

	public static byte[][] createAndValidateSkyMap(int w, int h) {
		int attempt = 0;
		do {
			byte[][] result = createSkyMap(w, h);

			int[] count = new int[256];

			for (int i = 0; i < w * h; i++) {
				count[result[0][i] & 0xff]++;
			}
			if (count[Tile.cloud.id & 0xff] < 2000) continue;
			if (count[Tile.stairsDown.id & 0xff] < 2) continue;

			return result;

		} while (true);
	}

	private static byte[][] createTopMap(int w, int h) {
		LevelGen mnoise1 = new LevelGen(w, h, 16);
		LevelGen mnoise2 = new LevelGen(w, h, 16);
		LevelGen mnoise3 = new LevelGen(w, h, 16);

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

				double xd = x / (w - 1.0) * 2 - 1;
				double yd = y / (h - 1.0) * 2 - 1;
				if (xd < 0) xd = -xd;
				if (yd < 0) yd = -yd;
				double dist = xd >= yd ? xd : yd;
				dist = dist * dist * dist * dist;
				dist = dist * dist * dist * dist;
				val = val + 1 - dist * 20;

				if (WorldGenMenu.type == WorldGenMenu.island){
				
				if (val < -0.5) {
					if (WorldGenMenu.theme == WorldGenMenu.hell){
					map[i] = Tile.lava.id;
					}
					if (WorldGenMenu.theme != WorldGenMenu.hell){
					map[i] = Tile.water.id;
					}
				} else if (val > 0.5 && mval < -1.5) {
					map[i] = Tile.rock.id;
				} else {
					map[i] = Tile.grass.id;
				}
				
				}
				else if (WorldGenMenu.type == WorldGenMenu.box){
					
					if (val < -1.5) {
						if (WorldGenMenu.theme == WorldGenMenu.hell){
							map[i] = Tile.lava.id;
							}
							if (WorldGenMenu.theme != WorldGenMenu.hell){
							map[i] = Tile.water.id;
							}
					} else if (val > 0.5 && mval < -1.5) {
						map[i] = Tile.rock.id;
					} else {
						map[i] = Tile.grass.id;
					}
					
					}
				else if (WorldGenMenu.type == WorldGenMenu.mount){
					
					if (val < -0.4) {
							map[i] = Tile.grass.id;
					} else if (val > 0.5 && mval < -1.5) {
						if (WorldGenMenu.theme != WorldGenMenu.hell){
						map[i] = Tile.water.id;
						}
						if (WorldGenMenu.theme == WorldGenMenu.hell){
						map[i] = Tile.lava.id;
						}
					} else {
						map[i] = Tile.rock.id;
					}
					
					}
				if (WorldGenMenu.type == WorldGenMenu.irreg){
					
				if (val < -0.5 && mval < -0.5) {
					if (WorldGenMenu.theme == WorldGenMenu.hell){
					map[i] = Tile.lava.id;
					}
					if (WorldGenMenu.theme != WorldGenMenu.hell){
					map[i] = Tile.water.id;
					}
				} else if (val > 0.5 && mval < -1.5) {
					map[i] = Tile.rock.id;
				} else {
					map[i] = Tile.grass.id;
				}
				
				}
			}
		}

		if (WorldGenMenu.theme == WorldGenMenu.desert){

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
								if (map[xx + yy * w] == Tile.grass.id) {
									map[xx + yy * w] = Tile.sand.id;
								}
							}
				}
				}
			}
		}

		if (WorldGenMenu.theme != WorldGenMenu.desert){

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
								if (map[xx + yy * w] == Tile.grass.id) {
									map[xx + yy * w] = Tile.sand.id;
								}
							}
				}
				}
			}
		}
		
		/*
		 * for (int i = 0; i < w * h / 2800; i++) { int xs = random.nextInt(w); int ys = random.nextInt(h); for (int k = 0; k < 10; k++) { int x = xs + random.nextInt(21) - 10; int y = ys + random.nextInt(21) - 10; for (int j = 0; j < 100; j++) { int xo = x + random.nextInt(5) - random.nextInt(5); int yo = y + random.nextInt(5) - random.nextInt(5); for (int yy = yo - 1; yy <= yo + 1; yy++) for (int xx = xo - 1; xx <= xo + 1; xx++) if (xx >= 0 && yy >= 0 && xx < w && yy < h) { if (map[xx + yy * w] == Tile.grass.id) { map[xx + yy * w] = Tile.dirt.id; } } } } }
		 */

		if (WorldGenMenu.theme == WorldGenMenu.forest){
		for (int i = 0; i < w * h / 200; i++) {
			int x = random.nextInt(w);
			int y = random.nextInt(h);
			for (int j = 0; j < 200; j++) {
				int xx = x + random.nextInt(15) - random.nextInt(15);
				int yy = y + random.nextInt(15) - random.nextInt(15);
				if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
					if (map[xx + yy * w] == Tile.grass.id) {
						map[xx + yy * w] = Tile.tree.id;
					}
				}
			}
			}
		}
		if (WorldGenMenu.theme != WorldGenMenu.forest && WorldGenMenu.theme != WorldGenMenu.plain){
		for (int i = 0; i < w * h / 1200; i++) {
			int x = random.nextInt(w);
			int y = random.nextInt(h);
			for (int j = 0; j < 200; j++) {
				int xx = x + random.nextInt(15) - random.nextInt(15);
				int yy = y + random.nextInt(15) - random.nextInt(15);
				if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
					if (map[xx + yy * w] == Tile.grass.id) {
						map[xx + yy * w] = Tile.tree.id;
					}
				}
			}
			}
		}

		if (WorldGenMenu.theme == WorldGenMenu.plain){
		for (int i = 0; i < w * h / 2800; i++) {
			int x = random.nextInt(w);
			int y = random.nextInt(h);
			for (int j = 0; j < 200; j++) {
				int xx = x + random.nextInt(15) - random.nextInt(15);
				int yy = y + random.nextInt(15) - random.nextInt(15);
				if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
					if (map[xx + yy * w] == Tile.grass.id) {
						map[xx + yy * w] = Tile.tree.id;
					}
				}
			}
			}
		}
		if (WorldGenMenu.theme != WorldGenMenu.plain){
		for (int i = 0; i < w * h / 400; i++) {
			int x = random.nextInt(w);
			int y = random.nextInt(h);
			for (int j = 0; j < 200; j++) {
				int xx = x + random.nextInt(15) - random.nextInt(15);
				int yy = y + random.nextInt(15) - random.nextInt(15);
				if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
					if (map[xx + yy * w] == Tile.grass.id) {
						map[xx + yy * w] = Tile.tree.id;
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
					if (map[xx + yy * w] == Tile.grass.id) {
						map[xx + yy * w] = Tile.flower.id;
						data[xx + yy * w] = (byte) (col + random.nextInt(4) * 16);
					}
				}
			}
		}

		for (int i = 0; i < w * h / 100; i++) {
			int xx = random.nextInt(w);
			int yy = random.nextInt(h);
			if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
				if (map[xx + yy * w] == Tile.sand.id) {		
					map[xx + yy * w] = Tile.cactus.id;
				}
			}
		}

		int count = 0;
		stairsLoop: for (int i = 0; i < w * h / 100; i++) {
			int x = random.nextInt(w - 2) + 1;
			int y = random.nextInt(h - 2) + 1;

			for (int yy = y - 1; yy <= y + 1; yy++)
				for (int xx = x - 1; xx <= x + 1; xx++) {
					if (map[xx + yy * w] != Tile.rock.id) continue stairsLoop;
				}

			map[x + y * w] = Tile.stairsDown.id;
			count++;
			if (WorldGenMenu.sized == 128){
			if (count == 6) break;
			} else {
			if (count == 12) break;
			}
		}

		return new byte[][] { map, data };
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
				val = val + 1 - dist * 2;

				if (val < -0.35) {
					map[i] = Tile.ow.id;
				} else {
					map[i] = Tile.o.id;
				}
			}
		}

		stairsLoop: for (int i = 0; i < w * h / 450; i++) {
			int x = random.nextInt(w - 2) + 1;
			int y = random.nextInt(h - 2) + 1;

			for (int yy = y - 1; yy <= y + 1; yy++)
				for (int xx = x - 1; xx <= x + 1; xx++) {
					if (map[xx + yy * w] != Tile.ow.id) continue stairsLoop;
				}

			map[x + y * w] = Tile.lava.id;
			map[x + (y + 1) * w] = Tile.lava.id;
			map[x + 1 + (y + 1) * w] = Tile.lava.id;
			map[x + 1 + (y) * w] = Tile.lava.id;
		}

		int count = 0;
		stairsLoop: for (int i = 0; i < w * h; i++) {
			int x = random.nextInt(w - 2) + 1;
			int y = random.nextInt(h - 2) + 1;

			for (int yy = y - 1; yy <= y + 1; yy++)
				for (int xx = x - 1; xx <= x + 1; xx++) {
					if (map[xx + yy * w] != Tile.ow.id) continue stairsLoop;
				}

			map[x + y * w] = Tile.ow.id;

					
			count++;
			if (count == 2) break;
		}

		return new byte[][] { map, data };
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
				dist = dist * dist * dist * dist;
				dist = dist * dist * dist * dist;
				val = val + 1 - dist * 20;

				if (val > -1 && wval < -1 + (depth) / 2 * 3) {
					if (depth == 3)
						map[i] = Tile.lava.id;
					else if (depth == 1)
						map[i] = Tile.dirt.id;
					else
						map[i] = Tile.water.id;
				}else if (val > -2 && (mval < -1.7 || nval < -1.4)) {
					map[i] = Tile.dirt.id;
				}else {
					map[i] = Tile.rock.id;
			
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
						if (map[xx + yy * w] == Tile.rock.id) {
							map[xx + yy * w] = (byte) ((Tile.ironOre.id & 0xff) + depth - 1);

						}
					}
				}
					for (int j = 0; j < 10; j++) {
						int xx = x + random.nextInt(3) - random.nextInt(2);
						int yy = y + random.nextInt(3) - random.nextInt(2);
						if (xx >= r && yy >= r && xx < w - r && yy < h - r) {
							if (map[xx + yy * w] == Tile.rock.id) {
								map[xx + yy * w] = (byte) ((Tile.lapisOre.id & 0xff));

							}
						}
					}
			}
		}
		
		if (depth > 2) {
			{
			int r = 1;
			for (int i = 0; i < w * h / 380; i++) {
				for (int j = 0; j < 10; j++) {
					int xx = 60;
					int yy = 60;
					if (xx >= r && yy >= r && xx < w - r && yy < h - r) {
							map[xx + yy * w] = (byte) ((Tile.ow.id & 0xff));
							map[xx + 1 + yy * w] = (byte) ((Tile.ow.id & 0xff));
							map[xx + (yy + 1) * w] = (byte) ((Tile.ow.id & 0xff));
							map[xx + 2 + yy * w] = (byte) ((Tile.ow.id & 0xff));
							map[xx + (yy + 2) * w] = (byte) ((Tile.ow.id & 0xff));
							map[xx + 3 + yy * w] = (byte) ((Tile.ow.id & 0xff));
							map[xx + (yy + 3) * w] = (byte) ((Tile.ow.id & 0xff));
							map[xx + 4 + yy * w] = (byte) ((Tile.ow.id & 0xff));
							map[xx + (yy + 4) * w] = (byte) ((Tile.ow.id & 0xff));
							map[xx + 4 + (yy + 1) * w] = (byte) ((Tile.ow.id & 0xff));
							map[xx + 4 + (yy + 2) * w] = (byte) ((Tile.ow.id & 0xff));
							map[xx + 4 + (yy + 3) * w] = (byte) ((Tile.ow.id & 0xff));
							map[xx + 4 + (yy + 4) * w] = (byte) ((Tile.ow.id & 0xff));
							map[xx + 3 + (yy + 1) * w] = (byte) ((Tile.o.id & 0xff));
							map[xx + 3 + (yy + 2) * w] = (byte) ((Tile.o.id & 0xff));
							map[xx + 3 + (yy + 3) * w] = (byte) ((Tile.o.id & 0xff));
							map[xx + 3 + (yy + 4) * w] = (byte) ((Tile.ow.id & 0xff));
							map[xx + 2 + (yy + 1) * w] = (byte) ((Tile.o.id & 0xff));
							map[xx + 2 + (yy + 2) * w] = (byte) ((Tile.stairsDown.id & 0xff));
							map[xx + 2 + (yy + 3) * w] = (byte) ((Tile.o.id & 0xff));
							map[xx + 2 + (yy + 4) * w] = (byte) ((Tile.ow.id & 0xff));
							map[xx + 1 + (yy + 1) * w] = (byte) ((Tile.o.id & 0xff));
							map[xx + 1 + (yy + 2) * w] = (byte) ((Tile.o.id & 0xff));
							map[xx + 1 + (yy + 3) * w] = (byte) ((Tile.o.id & 0xff));
							map[xx + 1 + (yy + 4) * w] = (byte) ((Tile.ow.id & 0xff));

					}
				}
			}
		}
		}
			
			
		if (depth < 3) {
			int count = 0;
			stairsLoop: for (int i = 0; i < w * h / 100; i++) {
				int x = random.nextInt(w - 20) + 10;
				int y = random.nextInt(h - 20) + 10;

				for (int yy = y - 1; yy <= y + 1; yy++)
					for (int xx = x - 1; xx <= x + 1; xx++) {
						if (map[xx + yy * w] != Tile.rock.id) continue stairsLoop;
					}

				map[x + y * w] = Tile.stairsDown.id;
				count++;
				if (WorldGenMenu.sized == 128){
				if (count == 4) break;
				} else {
				if (count == 8) break;
				}
			}
		}

		return new byte[][] { map, data };
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
				val = val + 1 - dist * 20;

				if (val < -0.25) {
					map[i] = Tile.infiniteFall.id;
				} else {
					map[i] = Tile.cloud.id;
				}
			}
		}

		stairsLoop: for (int i = 0; i < w * h / 50; i++) {
			int x = random.nextInt(w - 2) + 1;
			int y = random.nextInt(h - 2) + 1;

			for (int yy = y - 1; yy <= y + 1; yy++)
				for (int xx = x - 1; xx <= x + 1; xx++) {
					if (map[xx + yy * w] != Tile.cloud.id) continue stairsLoop;
				}

			map[x + y * w] = Tile.cloudCactus.id;
		}

		int count = 0;
		stairsLoop: for (int i = 0; i < w * h; i++) {
			int x = random.nextInt(w - 2) + 1;
			int y = random.nextInt(h - 2) + 1;

			for (int yy = y - 1; yy <= y + 1; yy++)
				for (int xx = x - 1; xx <= x + 1; xx++) {
					if (map[xx + yy * w] != Tile.cloud.id) continue stairsLoop;
				}

			map[x + y * w] = Tile.stairsDown.id;
			count++;
			if (count == 2) break;
		}

		return new byte[][] { map, data };
	}

	public static void main(String[] args) {
		int d = 0;
		while (true) {
			int w = 256;
			int h = w;

		//byte[] map = LevelGen.createAndValidateTopMap(w, h)[0];
			 byte[] map = LevelGen.createAndValidateUndergroundMap(w, h, (d++ % 3) + 1)[0];
			 //byte[] map = LevelGen.createAndValidateSkyMap(w, h)[0];
			//byte[] map = LevelGen.createAndValidateDungeon(w, h)[0];

			 
			BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
			int[] pixels = new int[w * h];
			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {
					int i = x + y * w;

					if (map[i] == Tile.water.id) pixels[i] = 0x000080;
					if (map[i] == Tile.ironOre.id) pixels[i] = 0x000080;
					if (map[i] == Tile.goldOre.id) pixels[i] = 0x000080;
					if (map[i] == Tile.gemOre.id) pixels[i] = 0x000080;
					if (map[i] == Tile.grass.id) pixels[i] = 0x208020;
					if (map[i] == Tile.rock.id) pixels[i] = 0xa0a0a0;
					if (map[i] == Tile.dirt.id) pixels[i] = 0x604040;
					if (map[i] == Tile.sand.id) pixels[i] = 0xa0a040;
					if (map[i] == Tile.sbrick.id) pixels[i] = 0xa0a040;
					if (map[i] == Tile.tree.id) pixels[i] = 0x003000;
					if (map[i] == Tile.ow.id) pixels[i] = 0x0aa0a0;
					if (map[i] == Tile.o.id) pixels[i] = 0x000000;
					if (map[i] == Tile.lava.id) pixels[i] = 0xff2020;
					if (map[i] == Tile.cloud.id) pixels[i] = 0xa0a0a0;
					if (map[i] == Tile.stairsDown.id) pixels[i] = 0xffffff;
					if (map[i] == Tile.stairsUp.id) pixels[i] = 0xffffff;
					if (map[i] == Tile.cloudCactus.id) pixels[i] = 0xff00ff;
				}
			}
			img.setRGB(0, 0, w, h, pixels, 0, w);
			JOptionPane.showMessageDialog(null, null, "Another", JOptionPane.YES_NO_OPTION, new ImageIcon(img.getScaledInstance(w * 2, h * 2, Image.SCALE_AREA_AVERAGING)));
		}
	}
}