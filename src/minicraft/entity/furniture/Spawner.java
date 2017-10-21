package minicraft.entity.furniture;

import java.util.Random;

import minicraft.Game;
import minicraft.Sound;
import minicraft.entity.Direction;
import minicraft.entity.mob.EnemyMob;
import minicraft.entity.mob.MobAi;
import minicraft.entity.mob.Player;
import minicraft.entity.particle.FireParticle;
import minicraft.entity.particle.TextParticle;
import minicraft.gfx.Color;
import minicraft.gfx.Sprite;
import minicraft.item.FurnitureItem;
import minicraft.item.Item;
import minicraft.item.PotionType;
import minicraft.item.PowerGloveItem;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.tile.Tile;

public class Spawner extends Furniture {
	
	Random rnd = new Random();
	
	private static final int ACTIVE_RADIUS = 8*16;
	private static final int minSpawnInterval = 200, maxSpawnInterval = 500;
	private static final int minMobSpawnChance = 10; // 1 in minMobSpawnChance chance of calling trySpawn every interval.
	
	public MobAi mob;
	private int health, lvl, maxMobLevel;
	private int spawnTick;
	
	private void initMob(MobAi m) {
		mob = m;
		sprite.color = col = mob.col;
		
		if(m instanceof EnemyMob) {
			lvl = ((EnemyMob)mob).lvl;
			maxMobLevel = mob.getMaxLevel();
		} else {
			lvl = 1;
			maxMobLevel = 1;
		}
	}
	
	public Spawner(MobAi m) {
		super(getClassName(m.getClass()) + " Spawner", new Sprite(20, 8, 2, 2, m.col), 7, 2);
		health = 100;
		initMob(m);
		resetSpawnInterval();
	}
	
	private static String getClassName(Class c) {
		String fullName = c.getCanonicalName();
		return fullName.substring(fullName.lastIndexOf(".")+1);
	}
	
	public void tick() {
		super.tick();
		
		spawnTick--;
		if(spawnTick <= 0) {
			int chance = (int) (minMobSpawnChance * Math.pow(level.mobCount, 2) / Math.pow(level.maxMobCount, 2)); // this forms a quadratic function that determines the mob spawn chance.
			if(chance <= 0 || random.nextInt(chance) == 0)
				trySpawn();
			resetSpawnInterval();
		}
	}
	
	private void resetSpawnInterval() {
		spawnTick = rnd.nextInt(maxSpawnInterval - minSpawnInterval + 1) + minSpawnInterval;
	}
	
	private void trySpawn() {
		if(level == null) return;
		if(level.mobCount >= level.maxMobCount) return; // can't spawn more entities
		
		Player player = getClosestPlayer();
		if(player == null) return;
		int xd = player.x - x;
		int yd = player.y - y;
		
		if(xd * xd + yd * yd > ACTIVE_RADIUS * ACTIVE_RADIUS) return;
		
		MobAi newmob;
		try {
			if(mob instanceof EnemyMob)
				//noinspection JavaReflectionMemberAccess
				newmob = mob.getClass().getConstructor(int.class).newInstance(((EnemyMob)mob).lvl);
			else
				newmob = mob.getClass().newInstance();
		} catch(Exception ex) {
			System.err.println("Spawner ERROR: could not spawn mob; error initializing mob instance:");
			ex.printStackTrace();
			return;
		}
		
		int randX, randY;
		Tile tile;
		do {
			randX = (x>>4) + rnd.nextInt(2) - 1; // the rand is really just one tile in any direction
			randY = (y>>4) + rnd.nextInt(2) - 1;
			tile = level.getTile(randX, randY);
		} while(!tile.mayPass(level, randX, randY, newmob) || mob instanceof EnemyMob && tile.getLightRadius(level, randX, randY) > 0);
		
		newmob.x = randX << 4;
		newmob.y = randY << 4;
		//if (Game.debug) level.printLevelLoc("spawning new " + mob, (newmob.x>>4), (newmob.y>>4), "...");
		
		level.add(newmob);
		Sound.monsterHurt.play();
		for(int i = 0; i < 6; i++) {
			randX = rnd.nextInt(16);
			randY = rnd.nextInt(12);
			level.add(new FireParticle(x - 8 + randX, y - 6 + randY));
		}
	}
	
	@Override
	public boolean interact(Player player, Item item, Direction attackDir) {
		if(item instanceof ToolItem) {
			ToolItem tool = (ToolItem)item;
			//if(tool.type != ToolType.Pickaxe && !Game.isMode("creative")) return false;
			
			Sound.monsterHurt.play();
			
			int dmg;
			if(Game.isMode("creative"))
				dmg = health;
			else {
				dmg = tool.level + random.nextInt(2);
				
				if(tool.type == ToolType.Pickaxe)
					dmg += random.nextInt(5)+2;
				
				if (player.potioneffects.containsKey(PotionType.Haste))
					dmg *= 2;
			}
			
			health -= dmg;
			level.add(new TextParticle("" + dmg, x, y, Color.get(-1, 200, 300, 400)));
			if(health <= 0) {
				level.remove(this);
				Sound.playerDeath.play();
				player.score += 500;
			}
			
			return true;
		}
		
		if(item instanceof PowerGloveItem && Game.isMode("creative")) {
			level.remove(this);
			player.inventory.add(0, player.activeItem);
			player.activeItem = new FurnitureItem(this);
			return true;
		}
		
		return false;
	}
	
	@Override
	@SuppressWarnings("JavaReflectionMemberAccess")
	public boolean use(Player player) {
		if(Game.isMode("creative") && mob instanceof EnemyMob) {
			lvl++;
			if(lvl > maxMobLevel) lvl = 1;
			EnemyMob newmob = null;
			try {
				newmob = (EnemyMob)mob.getClass().getConstructor(int.class).newInstance(lvl);
			} catch(Exception ex) {
				ex.printStackTrace();
			}
			initMob(newmob);
			return true;
		}
		
		return false;
	}
	
	public Furniture clone() { return new Spawner(mob); }
	
	protected String getUpdateString() {
		String updates = super.getUpdateString() + ";";
		updates += "health,"+health+
		";lvl,"+lvl;
		
		return updates;
	}
	
	protected boolean updateField(String field, String val) {
		if(super.updateField(field, val)) return true;
		switch(field) {
			case "health": health = Integer.parseInt(val); return true;
			case "lvl": lvl = Integer.parseInt(val); return true;
		}
		
		return false;
	}
}
