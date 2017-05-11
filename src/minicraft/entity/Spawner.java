package minicraft.entity;

import java.util.Random;
import minicraft.Game;
import minicraft.entity.particle.FireParticle;
import minicraft.entity.particle.TextParticle;
import minicraft.gfx.Color;
import minicraft.item.Item;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.tile.Tile;
import minicraft.sound.Sound;

public class Spawner extends Furniture {
	
	Random rnd = new Random();
	
	private static final int SPAWN_INTERVAL = 360;
	private static final int ACTIVE_RADIUS = 8*16;
	
	public MobAi mob;
	private int health, tick, lvl, maxMobLevel;
	
	private final void initMob(MobAi m, int lvl) {
		mob = m;
		col = mob.col;
		this.lvl = lvl;
	}
	
	private Spawner(MobAi m, int lvl) {
		super(getClassName(m.getClass()) + " Spawner", 0, 10, 7, 2);
		health = 100;
		tick = 0;
		initMob(m, lvl);
	}
	public Spawner(PassiveMob m) {
		this(m, 1);
		maxMobLevel = 1;
	}
	public Spawner(EnemyMob m) {
		this(m, m.lvl);
		maxMobLevel = m.getMaxLevel();
	}
	
	private String getClassName(Class c) {
		String fullName = c.getCanonicalName();
		return fullName.substring(fullName.lastIndexOf(".")+1);
	}
	
	public void tick() {
		super.tick();
		
		tick++;
		if(tick >= SPAWN_INTERVAL) {
			tick = 0;
			trySpawn();
		}
	}
	
	private void trySpawn() {
		int xd = level.player.x - x;
		int yd = level.player.y - y;
		
		if(xd * xd + yd * yd > ACTIVE_RADIUS * ACTIVE_RADIUS) return;
		
		
		int randX = (x/16 - 1 + rnd.nextInt(2)); // the rand is really just one tile in any direction
		int randY = (y/16 - 1 + rnd.nextInt(2));
		Tile tile = level.getTile(randX, randY);
		
		MobAi newmob;
		if(mob instanceof EnemyMob)
			newmob = mob.getClass().getConstructor(int.class).newInstance(((EnemyMob)mob).lvl);
		else
			newmob = mob.getClass().newInstance();
		
		//if (Game.debug) System.out.println("attempting " + mob + " spawn on tile with id: " + tile.id);
		if(tile.mayPass(level, randX, randY, newmob) && tile.getLightRadius(level, randX, randY) == 0) {
			newmob.x = randX << 4;
			newmob.y = randY << 4;
			//if (Game.debug) System.out.println("spawning new " + mob + " on level "+lvl+": x=" + (newmob.x>>4)+" y="+(newmob.y>>4) + "...");
			level.add(newmob);
			Sound.monsterHurt.play();
			for(int i = 0; i < 6; i++) {
				randX = rnd.nextInt(16);
				randY = rnd.nextInt(12);
				level.add(new FireParticle(x - 4 + randX, y - 4 + randY));
			}
		}
	}

	public boolean interact(Player player, Item item, int attackDir) {
		if(item instanceof ToolItem) {
			ToolItem tool = (ToolItem)item;
			if(tool.type != ToolType.Pickaxe) return false;
			
			int dmg;
			Sound.monsterHurt.play();
			if(player.potioneffects.containsKey("Haste"))
				dmg = tool.level + 1 + random.nextInt(5);
			else
				dmg = tool.level + 1 + random.nextInt(3);
			
			health -= dmg;
			level.add(new TextParticle("" + dmg, x, y, Color.get(-1, 200, 300, 400)));
			if(health <= 0) {
				level.remove(this);
				Sound.playerDeath.play();
				Player.score += 500;
			}
			
			return true;
		}
		return false;
	}
	
	public void hurt(Mob attacker, int dmg, int attackDir) {
		if(attacker instanceof Player && ModeMenu.creative && mob instanceof EnemyMob) {
			lvl++;
			if(lvl > maxMobLevel) lvl = 1;
			EnemyMob newmob = mob.getClass().getConstructor(int.class).newInstance(lvl);
			initMob(newmob, lvl);
		}
	}
	
	/*protected void touchedBy(Entity entity) {}
	
	public boolean use(Player player, int attackDir) {
		return false;
	}*/
	/*
	public void setMob(String newmob) {
		this.mob = newmob;
		
		MobAi model = getMob(newmob, lvl);
		
		//color = model.col;
		//if(model instanceof EnemyMob)
			//color = Color.tint(color, -1, true);
		col = color;
	}*/
	/*
	public MobAi getMob(MobAi m, int lvl) {
		String string = getClassName(m.getClass());
		switch(string) {
			case "Zombie": return (MobAi)new Zombie(lvl);
			case "Slime": return (MobAi)new Slime(lvl);
			case "Cow": return (MobAi)new Cow();
			case "Sheep": return (MobAi)new Sheep();
			case "Pig": return (MobAi)new Pig();
			case "Creeper": return (MobAi)new Creeper(lvl);
			case "Skeleton": return (MobAi)new Skeleton(lvl);
			case "AirWizard": return (MobAi)new AirWizard(lvl>1);
			case "Knight": return (MobAi)new Knight(lvl);
			case "Snake": return (MobAi)new Snake(lvl);
			default:
				System.out.println("Attempted to spawn invalid mob: " + string);
				return null; // fix: make a missing texture entity! maybe...
		}
	}*/
	
	public Furniture copy() {
		return (Furniture) new Spawner(mob);
	}
}
