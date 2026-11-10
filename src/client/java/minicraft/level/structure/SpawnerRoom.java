package minicraft.level.structure;

import minicraft.entity.furniture.Furniture;
import minicraft.entity.furniture.Spawner;
import minicraft.entity.furniture.Chest;
import minicraft.entity.furniture.DungeonChest;
import minicraft.entity.mob.MobAi;
import minicraft.entity.mob.Knight;
import minicraft.entity.mob.Skeleton;
import minicraft.entity.mob.Slime;
import minicraft.entity.mob.Snake;
import minicraft.entity.mob.Zombie;
import minicraft.gfx.Point;
import minicraft.gfx.Rectangle;
import minicraft.level.ChunkManager;
import minicraft.level.ChunkPoint;
import minicraft.level.Level;
import minicraft.util.Hash2;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Random;
import java.util.function.Consumer;

public class SpawnerRoom extends Structure {

	public SpawnerRoom() {}

	private Structure baseAtLevel(Level level) {
		if(level.depth == -4)
			return Structures.dungeonSpawner;
		else return Structures.mobDungeonCenter;

	}

	public Set<Point> occurencesInChunk(Level level, int cx, int cy) {
		return Collections.EMPTY_SET;
	}

	public Set<ChunkPoint> affectedChunks(Level level, int xt, int yt) {
		return baseAtLevel(level).affectedChunks(level, xt, yt);
	}

	public Set<Rectangle> getBounds(Level level, int x, int y) {
		return baseAtLevel(level).getBounds(level, x, y);
	}

	public void draw(Level level, int xt, int yt) {
		Structure base = baseAtLevel(level);
		base.draw(level, xt, yt);

		Random r = new Random(Hash2.hash2(level.getSeed()^base.hashCode(), xt, yt));

		MobAi mob;

		if(level.depth == -4) {
			if(r.nextBoolean())
				mob = new Knight(2);
			else
				mob = new Snake(2);
		} else {
			int choice = r.nextInt(5);
			if(choice < 2)
				mob = new Slime(-level.depth);
			else if(choice < 3)
				mob = new Skeleton(-level.depth);
			else
				mob = new Zombie(-level.depth);
		}

		Spawner spawner = new Spawner(mob);
		spawner.x = (xt << 4)+8;
		spawner.y = (yt << 4)+8;
		level.add(spawner);
		int chests = r.nextInt(2)+1;
		float angleOffset = r.nextFloat()*2.f*3.14159265f;
		for(int i = 0; i < chests; i++) {
			// Generate chests evenly around spawner
			float angle = 2.f*3.14159265f*((float)i / (float)chests);
			angle += angleOffset;
			int chestX = spawner.x + (int)(24.f*Math.cos(angle));
			int chestY = spawner.y + (int)(24.f*Math.sin(angle));

			if(level.depth == -4) {
				DungeonChest chest = new DungeonChest(r);
				level.add(chest, chestX, chestY);
			} else {
				Chest chest = new Chest();
				chest.populateInvRandom(r, "minidungeon", -level.depth);
				level.add(chest, chestX, chestY);
			}
		}
	}

}
