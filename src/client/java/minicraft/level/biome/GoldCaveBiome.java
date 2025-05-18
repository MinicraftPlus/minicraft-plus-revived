package minicraft.level.biome;

import java.util.Random; // TODO: Make single-instance tiles (stairs) into structures

import minicraft.level.ChunkManager;
import minicraft.level.LevelGen;
import minicraft.level.noise.LevelNoise;
import minicraft.level.tile.Tiles;

public class GoldCaveBiome extends Biome {
	public GoldCaveBiome() {
		super(0, 0, 0);
	}

	public void generate(ChunkManager map, int x, int y) {
		LevelNoise n16 = map.getTileNoise(x, y).get(1);
		LevelNoise n32 = map.getTileNoise(x, y).get(2);

		double val = Math.abs(n32.sample(x, y, 0) - n32.sample(x, y, 1));
		double mval = Math.abs(Math.abs(n16.sample(x, y, 0) - n16.sample(x, y, 1)) - n16.sample(x, y, 2));
		double nval = Math.abs(Math.abs(n16.sample(x, y, 3) - n16.sample(x, y, 4)) - n16.sample(x, y, 5));
		double wval = Math.abs(Math.abs(n16.sample(x, y, 6) - n16.sample(x, y, 7)) - n16.sample(x, y, 8));
		if (val > 0.25 && wval < 0.45)
			map.setTile(x, y, Tiles.get("water"), 0);
		else if(val > 0.125 && mval < 0.20 || nval < 0.25)
			map.setTile(x, y, Tiles.get("dirt"), 0);
		else if(n16.sample(x, y, 9) > 0.675)
			map.setTile(x, y, Tiles.get("gold Ore"), 0);
		else if(n32.sample(x, y, 3) > 0.825)
			map.setTile(x, y, Tiles.get("Lapis"), 0);
		else if (new Random(System.nanoTime()).nextDouble() < 0.0025 * 5)
			map.setTile(x, y, Tiles.get("Stairs Down"), 0);
		else
			map.setTile(x, y, Tiles.get("rock"), 0);
	}
}
