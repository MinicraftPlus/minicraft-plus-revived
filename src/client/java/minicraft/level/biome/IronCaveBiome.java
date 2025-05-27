package minicraft.level.biome;

import java.util.Random; // TODO: Make single-instance tiles (stairs) into structures

import minicraft.level.ChunkManager;
import minicraft.level.noise.LevelNoise;
import minicraft.level.tile.Tiles;

public class IronCaveBiome extends Biome {
	public IronCaveBiome() {
		super(0, 0, 0);
	}

	public void generate(ChunkManager map, int x, int y) {
		LevelNoise noise = map.getTileNoise(x, y);

		double val = Math.abs(noise.getScale32Noise(x, y, 0) - noise.getScale32Noise(x, y, 1));
		double mval = Math.abs(Math.abs(noise.getScale16Noise(x, y, 0) - noise.getScale16Noise(x, y, 1)) - noise.getScale16Noise(x, y, 2));
		double nval = Math.abs(Math.abs(noise.getScale16Noise(x, y, 3) - noise.getScale16Noise(x, y, 4)) - noise.getScale16Noise(x, y, 5));
		double wval = Math.abs(Math.abs(noise.getScale16Noise(x, y, 6) - noise.getScale16Noise(x, y, 7)) - noise.getScale16Noise(x, y, 8));
		if (val > 0.25 && wval < 0.35)
			map.setTile(x, y, Tiles.get("water"), 0);
		else if(val > 0.125 && mval < 0.20 || nval < 0.25)
			map.setTile(x, y, Tiles.get("dirt"), 0);
		else if(noise.getScale16Noise(x, y, 9) > 0.65)
			map.setTile(x, y, Tiles.get("iron Ore"), 0);
		else if(noise.getScale32Noise(x, y, 3) > 0.8)
			map.setTile(x, y, Tiles.get("Lapis"), 0);
		else if (new Random(System.nanoTime()).nextDouble() < 0.003)
			map.setTile(x, y, Tiles.get("Stairs Down"), 0);
		else
			map.setTile(x, y, Tiles.get("rock"), 0);
	}
}
