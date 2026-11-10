package minicraft.level.biome;

import minicraft.level.ChunkManager;
import minicraft.level.structure.Structures;
import minicraft.level.noise.LevelNoise;
import minicraft.level.tile.Tiles;

public class DungeonBiome extends Biome {
	public DungeonBiome() {
		super(0, 0, 0);
		structures.add(Structures.ornateLavaPool);
		structures.add(Structures.dungeonGarden);
		structures.add(Structures.dungeonChest);
		structures.add(Structures.dungeonBossRoom);
		structures.add(Structures.stairsUp);
	}

	public void generate(ChunkManager map, int x, int y) {
		LevelNoise noise = map.getTileNoise(x, y);
		double val = Math.abs(noise.getScale16Noise(x, y, 0) - noise.getScale16Noise(x, y, 1));

		if(val < 0.15)
			map.setTile(x, y, Tiles.get("Obsidian Wall"), 0);
		else if (val < 0.18)
			map.setTile(x, y, Tiles.get("Lava"), 0);
		else {
			int selection = (int)Math.abs(noise.getScale16Noise(x, y, 2) * 9) % 3;
			if(selection == 0)
				map.setTile(x, y, Tiles.get("Obsidian"), 0);
			else if(selection == 1)
				map.setTile(x, y, Tiles.get("Raw Obsidian"), 0);
			else
				map.setTile(x, y, Tiles.get("dirt"), 0);
		}
	}
}
