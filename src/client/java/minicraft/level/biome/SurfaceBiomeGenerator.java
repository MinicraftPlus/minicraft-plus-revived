package minicraft.level.biome;

import minicraft.level.noise.LevelNoise;
import minicraft.level.ChunkManager;

public interface SurfaceBiomeGenerator {
	public void generate(ChunkManager map, int x, int y, LevelNoise scale16, LevelNoise scale32);
}
