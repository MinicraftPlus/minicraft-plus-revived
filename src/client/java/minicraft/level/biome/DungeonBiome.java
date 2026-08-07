package minicraft.level.biome;

import java.util.Random; // TODO: Make single-instance tiles (stairs) into structures

import minicraft.level.ChunkManager;
import minicraft.level.Structure;
import minicraft.level.noise.LevelNoise;
import minicraft.level.tile.Tiles;

public class DungeonBiome extends Biome {
	public DungeonBiome() {
		super(0, 0, 0);
	}

	public void generate(ChunkManager map, int x, int y) {
		LevelNoise noise = map.getTileNoise(x, y);
		double val = Math.abs(noise.getScale16Noise(x, y, 0) - noise.getScale16Noise(x, y, 1));

		Random r = new Random(System.nanoTime());

		boolean generateDecor = r.nextDouble() < 0.001;

		if(val < 0.15)
			map.setTile(x, y, Tiles.get("Obsidian Wall"), 0);
		else if (val < 0.18)
			map.setTile(x, y, Tiles.get("Lava"), 0);
		else {
			generateDecor = false;
			int selection = (int)Math.abs(noise.getScale16Noise(x, y, 2) * 9) % 3;
			if (r.nextDouble() < 0.0001)
				map.setTile(x, y, Tiles.get("Stairs Down"), 0);
			else if(selection == 0)
				map.setTile(x, y, Tiles.get("Obsidian"), 0);
			else if(selection == 1)
				map.setTile(x, y, Tiles.get("Raw Obsidian"), 0);
			else
				map.setTile(x, y, Tiles.get("dirt"), 0);
		}

		if(generateDecor)
			Structure.ornateLavaPool.draw(map, x, y);
	}
}
