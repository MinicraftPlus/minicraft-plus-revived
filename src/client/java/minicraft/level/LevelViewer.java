package minicraft.level;

import minicraft.core.Game;
import minicraft.level.tile.Tiles;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Random;

public class LevelViewer {

	public static void main(String[] args) {
		long worldSeed = 0x100;
		Random rand = new Random();

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

		int lvl = maplvls[idx++ % maplvls.length];

		//noinspection InfiniteLoopStatement
		while (true) {
			int w = 320;
			int h = 320;

			//noinspection ConstantConditions
			if (lvl > 1 || lvl < -4) continue;

			ChunkManager map = LevelGen.createAndValidateMap(w, h, lvl, worldSeed);
			Level parentLevel = lvl + 1 > 1 ? null : new Level(w, h, worldSeed, lvl + 1, null, false);
			map = Level.genStructuresToMap(map, new Level(w, h, worldSeed, lvl, parentLevel, false));

			if (map == null) continue;

			BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
			int[] pixels = new int[w * h];
			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {
					int i = x + y * w;

					if (map.getTile(x, y) == Tiles.get("water")) pixels[i] = 0x000080;
					if (map.getTile(x, y) == Tiles.get("iron Ore")) pixels[i] = 0x000080;
					if (map.getTile(x, y) == Tiles.get("flower")) pixels[i] = 0x000080;
					if (map.getTile(x, y) == Tiles.get("gold Ore")) pixels[i] = 0x000080;
					if (map.getTile(x, y) == Tiles.get("gem Ore")) pixels[i] = 0x000080;
					if (map.getTile(x, y) == Tiles.get("grass")) pixels[i] = 0x208020;
					if (map.getTile(x, y) == Tiles.get("rock")) pixels[i] = 0xa0a0a0;
					if (map.getTile(x, y) == Tiles.get("flower")) pixels[i] = 0xa0a0f0;
					if (map.getTile(x, y) == Tiles.get("hard rock")) pixels[i] = 0x707070;
					if (map.getTile(x, y) == Tiles.get("dirt")) pixels[i] = 0x604040;
					if (map.getTile(x, y) == Tiles.get("sand")) pixels[i] = 0xa0a040;
					if (map.getTile(x, y) == Tiles.get("Stone Bricks")) pixels[i] = 0xa0a040;
					if (map.getTile(x, y) == Tiles.get("Stone Wall")) pixels[i] = 0xa0a040;
					if (map.getTile(x, y) == Tiles.get("tree")) pixels[i] = 0x003000;
					if (map.getTile(x, y) == Tiles.get("Obsidian Wall")) pixels[i] = 0xa0a040;
					if (map.getTile(x, y) == Tiles.get("Obsidian")) pixels[i] = 0x000000;
					if (map.getTile(x, y) == Tiles.get("lava")) pixels[i] = 0xffff2020;
					if (map.getTile(x, y) == Tiles.get("cloud")) pixels[i] = 0xa0a0a0;
					if (map.getTile(x, y) == Tiles.get("Stairs Down")) pixels[i] = 0xffffffff;
					if (map.getTile(x, y) == Tiles.get("Stairs Up")) pixels[i] = 0xffffffff;
					if (map.getTile(x, y) == Tiles.get("Cloud Cactus")) pixels[i] = 0xffff00ff;
					if (map.getTile(x, y) == Tiles.get("Ornate Obsidian")) pixels[i] = 0x000f0a;
					if (map.getTile(x, y) == Tiles.get("Raw Obsidian")) pixels[i] = 0x0a0080;
					if (map.getTile(x, y) == Tiles.get((short)47)) pixels[i] = 0xa0a040; // Boss Room Tile
					if (map.getTile(x, y) == Tiles.get((short)48)) pixels[i] = 0xa0a040; // Boss Room Tile
					if (map.getTile(x, y) == Tiles.get("Wood Wall")) pixels[i] = 0xa0a040;
					if (map.getTile(x, y) == Tiles.get("Wood Planks")) pixels[i] = 0xa0a040;
					if (map.getTile(x, y) == Tiles.get("ashed dirt")) pixels[i] = 0x303030;
				}
			}
			img.setRGB(0, 0, w, h, pixels, 0, w);
			int op = JOptionPane.showOptionDialog(null, null, "LevelViewer " + Game.VERSION + " | Seed: " + worldSeed + " | Level: " + lvl, JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
				new ImageIcon(img.getScaledInstance(w * 4, h * 4, Image.SCALE_AREA_AVERAGING)),
				new String[] { "Next Seed", "Prev Seed", "Next Level", "Prev Level", "Min (0x100)", "Max (0xAAFF20)", "Random Seed", "Close" }, "Next");
			if (op == 0) worldSeed++;
			else if (op == 1) worldSeed--;
			else if (op == 2) {
				lvl--;
				if (lvl < -4) lvl = 1;
			}
			else if (op == 3) {
				lvl++;
				if (lvl > 1) lvl = -4;
			}
			else if (op == 4) worldSeed = 0x100;
			else if (op == 5) worldSeed = 0xAAFF20;
			else if (op == 6) worldSeed = rand.nextLong(0xAAFF20);
			else if (op == 7) return;
			else worldSeed++;
		}
	}
}
