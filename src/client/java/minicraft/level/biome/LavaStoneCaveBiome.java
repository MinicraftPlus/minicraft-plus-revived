package minicraft.level.biome;

import java.util.Random; // TODO: Make single-instance tiles (stairs) into structures

import minicraft.level.ChunkManager;
import minicraft.level.noise.LevelNoise;
import minicraft.level.tile.Tiles;

public class LavaStoneCaveBiome extends Biome {
	public LavaStoneCaveBiome() {
		super(0.3f, 0, 0.6f);
	}

	public void generate(ChunkManager map, int x, int y) {
		LevelNoise noise = map.getTileNoise(x, y);

		double val = Math.abs(noise.getScale16Noise(x, y, 0) - noise.getScale32Noise(x, y, 1));
		double mval = Math.abs(Math.abs(noise.getScale16Noise(x, y, 0) - noise.getScale16Noise(x, y, 1)) - noise.getScale16Noise(x, y, 2));
		double nval = Math.abs(Math.abs(noise.getScale16Noise(x, y, 3) - noise.getScale16Noise(x, y, 4)) - noise.getScale16Noise(x, y, 5));
		double wval = Math.abs(Math.abs(noise.getScale16Noise(x, y, 6) - noise.getScale16Noise(x, y, 7)) - noise.getScale16Noise(x, y, 8));
		if (val > 0.25 && wval < 0.45)
			map.setTile(x, y, Tiles.get("lava"), 0);
		else if(val > 0.125 && mval < 0.20 || nval < 0.25)
			map.setTile(x, y, Tiles.get("Ashed Dirt"), 0);
		else if(noise.getScale16Noise(x, y, 9) > 0.7)
			map.setTile(x, y, Tiles.get("gem Ore"), 0);
		else if(noise.getScale32Noise(x, y, 3) > 0.85)
			map.setTile(x, y, Tiles.get("Lava Stone"), 0);
		else if (new Random(System.nanoTime()).nextDouble() < 0.002)
			map.setTile(x, y, Tiles.get("Stairs Down"), 0);
		else
			map.setTile(x, y, Tiles.get("hard rock"), 0);
	}
}
