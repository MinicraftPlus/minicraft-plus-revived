package minicraft.entity;

import java.util.Random;
import minicraft.entity.particle.FireParticle;
import minicraft.entity.particle.TextParticle;
import minicraft.gfx.Color;
import minicraft.gfx.Sprite;
import minicraft.item.Item;
import minicraft.item.FurnitureItem;
import minicraft.item.PotionType;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.item.PowerGloveItem;
import minicraft.level.tile.Tile;
import minicraft.screen.ModeMenu;
import minicraft.Sound;

public class Spawner extends Furniture {
	
	Random rnd = new Random();
	
	private static final int ACTIVE_RADIUS = 8*16;
	private static final int minSpawnInterval = 200, maxSpawnInterval = 500;
	
	public MobAi mob;
	private int health, lvl, maxMobLevel;
	private int spawnTick;
	
	private final void initMob(MobAi m) {
		mob = m;
		sprite.color = col = mob.col;
		
		if(m instanceof EnemyMob) {
			lvl = ((EnemyMob)mob).lvl;
			maxMobLevel = ((EnemyMob)mob).getMaxLevel();
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
		
		MobAi newmob = null;
		try {
			if(mob instanceof EnemyMob)
				newmob = mob.getClass().getConstructor(int.class).newInstance(((EnemyMob)mob).lvl);
			else
				newmob = mob.getClass().newInstance();
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		
		int randX, randY;
		Tile tile;
		do {
			randX = (x>>4 - 1 + rnd.nextInt(2)); // the rand is really just one tile in any direction
			randY = (y>>4 - 1 + rnd.nextInt(2));
			tile = level.getTile(randX, randY);
		} while(!tile.mayPass(level, randX, randY, newmob) || mob instanceof EnemyMob && tile.getLightRadius(level, randX, randY) > 0);
		//if (Game.debug) System.out.println("attempting " + mob + " spawn on tile with id: " + tile.id);
		newmob.x = randX << 4;
		newmob.y = randY << 4;
		//if (Game.debug) System.out.println("spawning new " + mob + " on level "+lvl+": x=" + (newmob.x>>4)+" y="+(newmob.y>>4) + "...");
		level.add(newmob);
		Sound.monsterHurt.play();
		for(int i = 0; i < 6; i++) {
			randX = rnd.nextInt(16);
			randY = rnd.nextInt(12);
			level.add(new FireParticle(x - 8 + randX, y - 6 + randY));
		}
	}

	public boolean interact(Player player, Item item, int attackDir) {
		if(item instanceof ToolItem) {
			ToolItem tool = (ToolItem)item;
			if(tool.type != ToolType.Pickaxe) return false;
			
			int dmg;
			Sound.monsterHurt.play();
			if(player.potioneffects.containsKey(PotionType.Haste))
				dmg = tool.level + 1 + random.nextInt(5);
			else
				dmg = tool.level + 1 + random.nextInt(3);
			
			health -= dmg;
			level.add(new TextParticle("" + dmg, x, y, Color.get(-1, 200, 300, 400)));
			if(health <= 0) {
				level.remove(this);
				Sound.playerDeath.play();
				player.score += 500;
			}
			
			return true;
		}
		
		if(item instanceof PowerGloveItem && ModeMenu.creative) {
			level.remove(this);
			player.inventory.add(0, player.activeItem);
			player.activeItem = new FurnitureItem(this);
			return true;
		}
		
		return false;
	}
	
	public void hurt(Mob attacker, int dmg, int attackDir) {
		if(attacker instanceof Player && ModeMenu.creative && mob instanceof EnemyMob) {
			lvl++;
			if(lvl > maxMobLevel) lvl = 1;
			EnemyMob newmob = null;
			try {
				newmob = (EnemyMob)mob.getClass().getConstructor(int.class).newInstance(lvl);
			} catch(Exception ex) {
				ex.printStackTrace();
			}
			initMob(newmob);
		}
	}
	
	public Furniture clone() {
		return (Furniture) new Spawner(mob);
	}
	
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
