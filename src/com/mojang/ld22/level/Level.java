package com.mojang.ld22.level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import com.mojang.ld22.Game;
import com.mojang.ld22.entity.AirWizard;
import com.mojang.ld22.entity.Cow;
import com.mojang.ld22.entity.Creeper;
import com.mojang.ld22.entity.Entity;
import com.mojang.ld22.entity.Giant;
import com.mojang.ld22.entity.Knight;
import com.mojang.ld22.entity.Mob;
import com.mojang.ld22.entity.Pig;
import com.mojang.ld22.entity.Player;
import com.mojang.ld22.entity.Sheep;
import com.mojang.ld22.entity.Skeleton;
import com.mojang.ld22.entity.Slime;
import com.mojang.ld22.entity.Snake;
import com.mojang.ld22.entity.Zombie;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.item.FurnitureItem;
import com.mojang.ld22.level.levelgen.LevelGen;
import com.mojang.ld22.level.tile.DirtTile;
import com.mojang.ld22.level.tile.Tile;
import com.mojang.ld22.level.tile.WoolTile;

public class Level {
	private Random random = new Random();

	public int w, h;
	public int sux, suy;

	public byte[] tiles;
	public byte[] data;
	public List<Entity>[] entitiesInTiles;

	public int grassColor = 141;
	public int dirtColor = 322;
	public int cl = 0;
	public int woolColor = 444;
	public int redwoolColor = 500;
	public int yellowwoolColor = 550;
	public int sandColor = 550;
	private int depth;
	public int monsterDensity = 8;
	public static int depthlvl;
	
	 
	public static List<String> ls=new ArrayList<String>();
	


	
	
	public List<Entity> entities = new ArrayList<Entity>();
	private Comparator<Entity> spriteSorter = new Comparator<Entity>() {
		public int compare(Entity e0, Entity e1) {
			if (e1.y < e0.y) return +1;
			if (e1.y > e0.y) return -1;
			return 0;
		}

	};
	
	public static String c(int i) {
	    return "" + i;
	}

	@SuppressWarnings("unchecked")
	public Level(int w, int h, int level, Level parentLevel) {
		this.depth = level;
		cl = level;
		this.w = w;
		this.h = h;
		byte[][] maps;
		int saveTile;
		if (level != 0) {
			if (DirtTile.dirtc == 0){
			dirtColor = 222;
			DirtTile.dirtc++;
			}
			if (DirtTile.dirtc == 1){
			dirtColor = 222;
			}
		}
		if (level == 0) {
			if (DirtTile.dirtc == 0){
			dirtColor = 322;
			}
			if (DirtTile.dirtc == 0){
			if (level == 0) {
			dirtColor = 322;
			}
			if (level != 0) {
			dirtColor = 222;
			}
			DirtTile.dirtc++;
			}
		}

		if (level == 1) {
			dirtColor = 444;
		}
		if (level == 0)
			maps = LevelGen.createAndValidateTopMap(w, h);
		else if (level < 0 && level > -4) {
			maps = LevelGen.createAndValidateUndergroundMap(w, h, -level);
			monsterDensity = 4; 
			
		}	else if (level == -4){
		maps = LevelGen.createAndValidateDungeon(w, h);
		
	}else {
			maps = LevelGen.createAndValidateSkyMap(w, h); // Sky level
			monsterDensity = 4;
		}

		tiles = maps[0];
		data = maps[1];
		
		

		if (parentLevel != null) {
			for (int y = 0; y < h; y++)
				for (int x = 0; x < w; x++) {
					if (parentLevel.getTile(x, y) == Tile.stairsDown) {

						setTile(x, y, Tile.stairsUp, 0); 
						if (level == -4) {
							setTile(x - 1, y, Tile.o, 0);
							setTile(x + 1, y, Tile.o, 0);
							setTile(x + 2, y, Tile.odc, 0);
							setTile(x - 2, y, Tile.odc, 0);
							setTile(x, y - 1, Tile.o, 0);
							setTile(x, y + 1, Tile.o, 0);
							setTile(x, y + 2, Tile.odc, 0);
							setTile(x, y - 2, Tile.odc, 0);
							setTile(x - 1, y - 1, Tile.o, 0);
							setTile(x - 1, y + 1, Tile.o, 0);
							setTile(x + 1, y - 1, Tile.o, 0);
							setTile(x + 1, y + 1, Tile.o, 0);
							setTile(x + 3, y, Tile.o, 0);
							setTile(x - 3, y, Tile.o, 0);
							setTile(x + 3, y - 1, Tile.o, 0);
							setTile(x - 3, y - 1, Tile.o, 0);
							setTile(x + 3, y + 1, Tile.o, 0);
							setTile(x - 3, y + 1, Tile.o, 0);
							setTile(x + 4, y, Tile.o, 0);
							setTile(x - 4, y, Tile.o, 0);
							setTile(x + 4, y - 1, Tile.o, 0);
							setTile(x - 4, y - 1, Tile.o, 0);
							setTile(x + 4, y + 1, Tile.o, 0);
							setTile(x - 4, y + 1, Tile.o, 0);
							setTile(x, y+3, Tile.o, 0);
							setTile(x, y-3, Tile.o, 0);
							setTile(x + 1, y - 3, Tile.o, 0);
							setTile(x - 1, y - 3, Tile.o, 0);
							setTile(x + 1, y + 3, Tile.o, 0);
							setTile(x - 1, y + 3, Tile.o, 0);
							setTile(x, y+4, Tile.o, 0);
							setTile(x, y-4, Tile.o, 0);
							setTile(x + 1, y - 4, Tile.o, 0);
							setTile(x - 1, y - 4, Tile.o, 0);
							setTile(x + 1, y + 4, Tile.o, 0);
							setTile(x - 1, y + 4, Tile.o, 0);
							setTile(x - 2, y - 2, Tile.ow, 0);
							setTile(x - 3, y - 2, Tile.ow, 0);
							setTile(x - 3, y + 2, Tile.ow, 0);
							setTile(x - 2, y + 1, Tile.ow, 0);
							setTile(x + 2, y - 2, Tile.ow, 0);
							setTile(x + 4, y - 2, Tile.ow, 0);
							setTile(x + 4, y + 2, Tile.ow, 0);
							setTile(x - 4, y - 2, Tile.ow, 0);
							setTile(x - 4, y + 2, Tile.ow, 0);
							setTile(x + 1, y - 2, Tile.ow, 0);
							setTile(x - 2, y + 2, Tile.ow, 0);
							setTile(x + 2, y + 3, Tile.ow, 0);
							setTile(x + 2, y + 4, Tile.ow, 0);
							setTile(x - 2, y - 3, Tile.ow, 0);
							setTile(x - 2, y - 4, Tile.ow, 0);
							setTile(x + 2, y - 3, Tile.ow, 0);
							setTile(x + 2, y - 4, Tile.ow, 0);
							setTile(x - 2, y + 3, Tile.ow, 0);
							setTile(x - 2, y + 4, Tile.ow, 0);
							setTile(x + 3, y - 2, Tile.ow, 0);
							setTile(x + 3, y + 2, Tile.ow, 0);
							setTile(x + 2, y + 2, Tile.ow, 0);
							setTile(x - 1, y + 2, Tile.ow, 0);
							setTile(x + 2, y - 1, Tile.ow, 0);
							setTile(x + 2, y + 1, Tile.ow, 0);
							setTile(x + 1, y + 2, Tile.ow, 0);
							setTile(x - 2, y - 1, Tile.ow, 0);
							setTile(x - 1, y - 2, Tile.ow, 0);
							
							
						}
						if (level == 0) {
							sux = x;
							suy = y;
							System.out.print("X = " + sux + " " + "Y = " + suy + " ");
							setTile(x - 1, y, Tile.hardRock, 0);
							setTile(x + 1, y, Tile.hardRock, 0);
							setTile(x, y - 1, Tile.hardRock, 0);
							setTile(x, y + 1, Tile.hardRock, 0);
							setTile(x - 1, y - 1, Tile.hardRock, 0);
							setTile(x - 1, y + 1, Tile.hardRock, 0);
							setTile(x + 1, y - 1, Tile.hardRock, 0);
							setTile(x + 1, y + 1, Tile.hardRock, 0);
						}
						
						if (level != 0 && level != -4) {
							setTile(x - 1, y, Tile.dirt, 0);
							setTile(x + 1, y, Tile.dirt, 0);
							setTile(x, y - 1, Tile.dirt, 0);
							setTile(x, y + 1, Tile.dirt, 0);
							setTile(x - 1, y - 1, Tile.dirt, 0);
							setTile(x - 1, y + 1, Tile.dirt, 0);
							setTile(x + 1, y - 1, Tile.dirt, 0);
							setTile(x + 1, y + 1, Tile.dirt, 0);
						}
					}

				}
		}

		entitiesInTiles = new ArrayList[w * h];
		for (int i = 0; i < w * h; i++) {
			entitiesInTiles[i] = new ArrayList<Entity>();
		}
		
		
		

  	   	
  	 	
  	 	
  	 	
		if (level==1) {
			AirWizard aw = new AirWizard();
			aw.x = w*8;
			aw.y = h*8;
			add(aw);
		}
	}

	public void renderBackground(Screen screen, int xScroll, int yScroll) {
		int xo = xScroll >> 4;
		int yo = yScroll >> 4;
		int w = (screen.w + 15) >> 4;
		int h = (screen.h + 15) >> 4;
		screen.setOffset(xScroll, yScroll);
		for (int y = yo; y <= h + yo; y++) {
			for (int x = xo; x <= w + xo; x++) {
				getTile(x, y).render(screen, this, x, y);
			}
		}
		screen.setOffset(0, 0);
	}

	private List<Entity> rowSprites = new ArrayList<Entity>();

	public Player player;

	public void renderSprites(Screen screen, int xScroll, int yScroll) {
		int xo = xScroll >> 4;
		int yo = yScroll >> 4;
		int w = (screen.w + 15) >> 4;
		int h = (screen.h + 15) >> 4;

		screen.setOffset(xScroll, yScroll);
		for (int y = yo; y <= h + yo; y++) {
			for (int x = xo; x <= w + xo; x++) {
				if (x < 0 || y < 0 || x >= this.w || y >= this.h) continue;
				rowSprites.addAll(entitiesInTiles[x + y * this.w]);
			}
			if (rowSprites.size() > 0) {
				sortAndRender(screen, rowSprites);
			}
			rowSprites.clear();
		}
		screen.setOffset(0, 0);
	}

	public void renderLight(Screen screen, int xScroll, int yScroll) {
		int xo = xScroll >> 4;
		int yo = yScroll >> 4;
		int w = (screen.w + 15) >> 4;
		int h = (screen.h + 15) >> 4;

		screen.setOffset(xScroll, yScroll);
		int r = 4;
		for (int y = yo - r; y <= h + yo + r; y++) {
			for (int x = xo - r; x <= w + xo + r; x++) {
				if (x < 0 || y < 0 || x >= this.w || y >= this.h) continue;
				List<Entity> entities = entitiesInTiles[x + y * this.w];
				for (int i = 0; i < entities.size(); i++) {
					Entity e = entities.get(i);
					// e.render(screen);
					int lr = e.getLightRadius();
					if (lr > 0) screen.renderLight(e.x - 1, e.y - 4, lr * 8);
				}
				int lr = getTile(x, y).getLightRadius(this, x, y);
				if (lr > 0) screen.renderLight(x * 16 + 8, y * 16 + 8, lr * 8);
			}
		}
		screen.setOffset(0, 0);
	}

	// private void renderLight(Screen screen, int x, int y, int r) {
	// screen.renderLight(x, y, r);
	// }


	
	private void sortAndRender(Screen screen, List<Entity> list) {
		Collections.sort(list, spriteSorter);
		for (int i = 0; i < list.size(); i++) {
			list.get(i).render(screen);
		}
	}

	public Tile getTile(int x, int y) {
		if (x < 0 || y < 0 || x >= w || y >= h) return Tile.rock;
		return Tile.tiles[tiles[x + y * w]];
	}

	public void setTile(int x, int y, Tile t, int dataVal) {
		if (x < 0 || y < 0 || x >= w || y >= h) return;
		tiles[x + y * w] = t.id;
		data[x + y * w] = (byte) dataVal;
	}

	public int getData(int x, int y) {
		if (x < 0 || y < 0 || x >= w || y >= h) return 0;
		return data[x + y * w] & 0xff;
	}

	public void setData(int x, int y, int val) {
		if (x < 0 || y < 0 || x >= w || y >= h) return;
		data[x + y * w] = (byte) val;
	}

	public void add(Entity entity) {
		if (entity instanceof Player) {
			player = (Player) entity;
		}
		entity.removed = false;
		entities.add(entity);
		entity.init(this);

		insertEntity(entity.x >> 4, entity.y >> 4, entity);
	}
	
	public void adds(Entity entity, int xs, int ys) {
		if (entity instanceof Player) {
			player = (Player) entity;
		}
		entity.removed = false;
		entities.add(entity);
		entity.init(this);

		insertEntity(entity.x >> xs, entity.x >> ys, entity);
	}
	

	public void remove(Entity e) {
		entities.remove(e);
		int xto = e.x >> 4;
		int yto = e.y >> 4;
		removeEntity(xto, yto, e);
	}
	
	public void insertEntity(int x, int y, Entity e) {
		if (x < 0 || y < 0 || x >= w || y >= h) return;
		entitiesInTiles[x + y * w].add(e);
	}

	private void removeEntity(int x, int y, Entity e) {
		if (x < 0 || y < 0 || x >= w || y >= h) return;
		entitiesInTiles[x + y * w].remove(e);
	}

	public void trySpawn(int count) {
		for (int i = 0; i < count; i++) {
			Mob mob;

			int minLevel = 1;
			int maxLevel = 1;
			if (depth < 0) {
				maxLevel = (-depth) + 1;
			}
			if (depth > 0) {
				minLevel = maxLevel = 4;
			}

			int lvl = random.nextInt(maxLevel - minLevel + 1) + minLevel;
			int levels = depth;
			int rnd = random.nextInt(100);
			int tim = Game.Time;

			if (levels == 0){
			if (tim > 2){
				if (rnd <= 40)
					mob = new Slime(lvl);
				else if(rnd <= 75)
					mob = new Zombie(lvl);
				else if(rnd >= 85)
					mob = new Skeleton(lvl);
				else 
					mob = new Creeper(lvl);
			
			
			if (mob.findStartPos(this)) {
				this.add(mob);
			}
			}
			}
			
			if (levels == 0){
				if (tim != 3){
				if (rnd <= 22)
				mob = new Cow(lvl);
				else if(rnd >= 68)
				mob = new Pig(lvl);
				else
					mob = new Sheep(lvl);
				
				if (mob.findStartPosCow(this)) {
					this.add(mob);
				}
				}
				
				if (tim == 3){
				if (rnd <= 33)
				mob = new Cow(lvl);
				else if(rnd >= 68)
					mob = new Pig(lvl);
				else
					mob = new Sheep(lvl);
				
				if (mob.findStartPosCowLight(this)) {
					this.add(mob);
				}
				}
			}
			
			
			else if (levels != 0 && levels != -4){
				
				if (rnd <= 40)
					mob = new Slime(lvl);
				else if(rnd <= 75)
					mob = new Zombie(lvl);
				else if(rnd >= 85)
					mob = new Skeleton(lvl);
				else 
					mob = new Creeper(lvl);
			
				if (mob.findStartPos(this)) {
					this.add(mob);
			}
		}
			else if (levels == -4){
				
				if (rnd <= 40)
					mob = new Snake(lvl);
				else if(rnd <= 75)
					mob = new Knight(lvl);
				else if(rnd >= 85)
					mob = new Snake(lvl);
				else 
					mob = new Knight(lvl);
			
				if (mob.findStartPosDungeon(this)) {
					this.add(mob);
			}
		}
		}
	}

	public void tick() {
		trySpawn(1);

		depthlvl = depth;
		
		for (int i = 0; i < w * h / 50; i++) {
			int xt = random.nextInt(w);
			int yt = random.nextInt(w);
			getTile(xt, yt).tick(this, xt, yt);
		}
		for (int i = 0; i < entities.size(); i++) {
			Entity e = entities.get(i);
			int xto = e.x >> 4;
			int yto = e.y >> 4;

			e.tick();

			if (e.removed) {
				entities.remove(i--);
				removeEntity(xto, yto, e);
			} else {
				int xt = e.x >> 4;
				int yt = e.y >> 4;

				if (xto != xt || yto != yt) {
					removeEntity(xto, yto, e);
					insertEntity(xt, yt, e);
				}
			}
		}
	}

	public List<Entity> getEntities(int x0, int y0, int x1, int y1) {
		List<Entity> result = new ArrayList<Entity>();
		int xt0 = (x0 >> 4) - 1;
		int yt0 = (y0 >> 4) - 1;
		int xt1 = (x1 >> 4) + 1;
		int yt1 = (y1 >> 4) + 1;
		for (int y = yt0; y <= yt1; y++) {
			for (int x = xt0; x <= xt1; x++) {
				if (x < 0 || y < 0 || x >= w || y >= h) continue;
				List<Entity> entities = entitiesInTiles[x + y * this.w];
				for (int i = 0; i < entities.size(); i++) {
					Entity e = entities.get(i);
					if (e.intersects(x0, y0, x1, y1)) result.add(e);
				}
			}
		}
		return result;
	}

}