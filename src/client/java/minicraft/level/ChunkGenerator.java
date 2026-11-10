package minicraft.level;

import minicraft.core.Game;
import minicraft.core.World;
import minicraft.entity.mob.Player;
import minicraft.gfx.Point;
import minicraft.level.biome.Biome;
import minicraft.level.biome.Biomes;
import minicraft.level.noise.LevelNoise;
import minicraft.level.structure.Structure;
import minicraft.saveload.Load;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Collections;
import java.util.List;

public class ChunkGenerator implements Runnable {
	private final Level target;
	private final List<ChunkPoint> chunksToLoad = Collections.synchronizedList(new ArrayList<>());

	public ChunkGenerator(Level target) {
		this.target = target;
	}

	public void run() {
		while(true) {
			if(Game.levels[Game.currentLevel] != target) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ignored) {}
				continue;
			}

			if(!chunksToLoad.isEmpty()) {
				ChunkPoint c = chunksToLoad.get(0);
				synchronized(chunksToLoad) {
					c = chunksToLoad.remove(0);
				}
				int stage = target.chunkManager.getChunkStage(c.x, c.y);
				try {
					if(Load.chunkFileExists(target, c.x, c.y)
						&& Load.chunkFileStage(target, c.x, c.y) > stage) {
						Load.loadChunkFile(target, c.x, c.y);
					}
				else
					advanceChunk(c.x, c.y);
				} catch(Exception e) {
					e.printStackTrace();
				}
			} else {
				try {
					Thread.sleep(200);
				} catch (InterruptedException ignored) {}
			}
		}
	}

	// Queue all chunks up to 3 chunks away from the given location to be loaded
	public void queueChunksAround(int tileX, int tileY) {
		int cX = Math.floorDiv(tileX, ChunkManager.CHUNK_SIZE), cY = Math.floorDiv(tileY, ChunkManager.CHUNK_SIZE);
		synchronized(chunksToLoad) {
			chunksToLoad.clear();
			for (int x = cX - 3; x <= cX + 3; x++)
				for (int y = cY - 3; y <= cY + 3; y++) {
					ChunkPoint c = new ChunkPoint(x,y,target.depth);
					if (target.chunkManager.getChunkStage(x, y) != ChunkManager.CHUNK_STAGE_DONE)
						chunksToLoad.add(c);
				}

			// Sort so that chunks closer to
			chunksToLoad.sort((a, b) -> {
				int distA = (a.x-cX)*(a.x-cX)+(a.y-cY)*(a.y-cY);
				int distB = (b.x-cX)*(b.x-cX)+(b.y-cY)*(b.y-cY);
				return distA - distB;
			});
		}
	}

	public void generateChunksAround(int tileX, int tileY) {
		int S = ChunkManager.CHUNK_SIZE;
		int chunkX = Math.floorDiv(tileX, S);
		int chunkY = Math.floorDiv(tileY, S);


		// Generate a 3x3 radius of chunks
		for(int x = chunkX-1; x <= chunkX+1; x++)
			for(int y = chunkY-1; y <= chunkX+1; y++)
				// Max 64 iterations on chunk generation
				for(int k = 0; k < 64; k++)
					if(target.chunkManager.getChunkStage(x, y) < ChunkManager.CHUNK_STAGE_DONE)
						advanceChunk(x, y);
	}

	public void unloadChunks() {
		final int S = ChunkManager.CHUNK_SIZE;
		HashSet<Point> keep = new HashSet<>();
		for(Player p : target.getPlayers()) {
			int chunkX = Math.floorDiv(p.x, S*16);
			int chunkY = Math.floorDiv(p.y, S*16);
			for(int x = chunkX - 1; x <= chunkX + 1; x++)
				for(int y = chunkY - 1; y <= chunkY + 1; y++)
					keep.add(new Point(x, y));
		}

		synchronized(chunksToLoad) {
			chunksToLoad.clear();
			for(Point c : target.chunkManager.getAllChunks())
				if(!keep.contains(c))
					target.chunkManager.unloadChunk(c.x, c.y);
		}
	}

	private void primeNoise(int x, int y) {
		final int S = ChunkManager.CHUNK_SIZE;
		target.chunkManager.setChunkNoise(x, y, new LevelNoise(target.getSeed(), S * x, S * y, 0, S, S));
	}

	private void assignBiomes(int x, int y) {
		final int S = ChunkManager.CHUNK_SIZE;
		LevelNoise noise = target.chunkManager.getChunkNoise(x, y);
		for(int tileX = 0; tileX < S; tileX++)
			for(int tileY = 0; tileY < S; tileY++) {
				Biome biome = Biomes.getLayerBiomes(target.depth).getClosestBiome(noise, tileX, tileY);
				target.chunkManager.setBiome(tileX + S * x, tileY + S * y, biome);
			}
	}

	private void generateTiles(int x, int y) {
		final int S = ChunkManager.CHUNK_SIZE;
		for(int tileX = 0; tileX < S; tileX++)
			for(int tileY = 0; tileY < S; tileY++) {
				Biome biome = target.chunkManager.getBiome(tileX + S * x, tileY + S * y);
				biome.generate(target.chunkManager, tileX + S * x, tileY + S * y);
			}
	}

	private boolean generateStructures(int x, int y) {
		HashMap<Structure, HashSet<Point>> loaded = target.chunkManager.getChunkLoadedStructures(x, y);
		for(Biome b : target.chunkManager.getChunkBiomes(x, y))
			for(Structure s : b.getStructures()) {
				if(!loaded.containsKey(s))
					loaded.put(s, new HashSet<Point>());
				for(Point p : s.occurencesInChunk(target, x, y)) {
					if(loaded.get(s).contains(p))
						continue;
					if(target.chunkManager.getBiome(p.x, p.y) != b)
						continue;
					for(ChunkPoint c : s.affectedChunks(target, p.x, p.y)) {
						Level affectedLevel = Game.levels[World.lvlIdx(c.level)];
						ChunkManager affectedCM = affectedLevel.chunkManager;
						if(affectedCM.getChunkStage(c.x, c.y) < ChunkManager.CHUNK_STAGE_ASSIGNED_TILES) {
							affectedLevel.getChunkGenerator().advanceChunk(c.x, c.y);
							return false;
						}
					}

					s.draw(target, p.x, p.y);
					loaded.get(s).add(p);
				}
			}
		return true;
	}

	protected void advanceChunk(int x, int y) {
		int stage = target.chunkManager.getChunkStage(x, y);
		switch(stage) {
			case ChunkManager.CHUNK_STAGE_NONE:
				primeNoise(x, y);
				target.chunkManager.setChunkStage(x, y, ChunkManager.CHUNK_STAGE_PRIMED_NOISE);
				break;
			case ChunkManager.CHUNK_STAGE_PRIMED_NOISE:
				assignBiomes(x, y);
				target.chunkManager.setChunkStage(x, y, ChunkManager.CHUNK_STAGE_ASSIGNED_BIOMES);
				break;
			case ChunkManager.CHUNK_STAGE_ASSIGNED_BIOMES:
				generateTiles(x, y);
				target.chunkManager.setChunkStage(x, y, ChunkManager.CHUNK_STAGE_ASSIGNED_TILES);
				break;
			case ChunkManager.CHUNK_STAGE_ASSIGNED_TILES:
				if(generateStructures(x, y))
					target.chunkManager.setChunkStage(x, y, ChunkManager.CHUNK_STAGE_DONE);
				break;
		}
	}
}
