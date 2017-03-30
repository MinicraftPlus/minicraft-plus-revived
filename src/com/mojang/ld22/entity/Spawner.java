package com.mojang.ld22.entity;

import com.mojang.ld22.entity.particle.FireParticle;
import com.mojang.ld22.entity.particle.TextParticle;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.item.Item;
import com.mojang.ld22.item.ToolItem;
import com.mojang.ld22.item.ToolType;
import com.mojang.ld22.level.tile.Tile;
import com.mojang.ld22.sound.Sound;
import java.util.Random;

public class Spawner extends Furniture {
	
	Random rnd = new Random();
	//public Entity mob; // I think it would be better if this were just a string.
	public String mob;
	public int lvl;
	//int randx = 2;
	//int randy = 2;
	int r;
	int health;
	int tick;
	//boolean spawn;
	int dmg;
	
	public Spawner(String m, int level) {
		super(m + " Spawner");
		lvl = level;
		
		health = 100;
		r = 8*16; // activate from up to 8 blocks away
		tick = 0;
		dmg = 0;
		//spawn = false;
		
		setMob(m);
		sprite = 10;
		xr = 7;
		yr = 2;
	}
	
	public void tick() {
		super.tick();
		// what's all this for (the if/else below)? It seems pretty useless... I mean, why change color?
		/*int xd = level.player.x - x;
		int yd = level.player.y - y;
		if(xd * xd + yd * yd < r * r) {
			col0 = mob.col1;
			col1 = mob.col1;
			col2 = mob.col1;
			col3 = mob.col1;
			tick++;
		} else {
			col0 = mob.col4;
			col1 = mob.col4;
			col2 = mob.col4;
			col3 = mob.col4;
		}*/
		tick++;
		if(tick > 180) {
			tick = 0;
			trySpawn();
		}
	}
	
	private void trySpawn() {
		int xd = level.player.x - x;
		int yd = level.player.y - y;
		if(xd * xd + yd * yd <= r * r) return;
		
		Mob newmob = getMob(mob, lvl);
		
		/*if(newmob instanceof Mob) {
			newmob = (Mob)newmob;
			//getEntity(getEntityName(newmob)), lvl);
			/*if(col1 == Color.get(-1, 0, 4, 46)) { // unnecessary if my improvements are successful
				newmob = getEntity(getEntityName(mob) + "II", lvl);
			}*/
		//}*/
		
		int randX = (x/16 - 1 + rnd.nextInt(2)); // the rand is really just one tile in any direction
		int randY = (y/16 - 1 + rnd.nextInt(2));
		Tile tile = level.getTile(randX, randY);
		if(tile.mayPass(level, randX, randY, newmob) && tile.getLightRadius(level, randX, randY) == 0) {
			(newmob).x = randX * 16;
			(newmob).y = randY * 16;
			(newmob).hasspawned = true;
			if (com.mojang.ld22.Game.debug) System.out.println("spawning new " + mob + " on level "+lvl+": x=" + (newmob.x/16)+" y="+(newmob.y/16) + "...");
			level.add(newmob);
			
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
			if(tool.type == ToolType.pickaxe) {
				Sound.monsterHurt.play();
				if(player.potioneffects.containsKey("Haste")) {
					dmg = tool.level + 1 + random.nextInt(5);
				} else {
					dmg = tool.level + 1 + random.nextInt(3);
				}

				health -= dmg;
				level.add(new TextParticle("" + dmg, x, y, Color.get(-1, 200, 300, 400)));
				if(health < 1) {
					level.remove(this);
					Sound.playerDeath.play();
					Player.score += 500;
				}

				return true;
			}

			if(tool.type == ToolType.pick) {
				Sound.monsterHurt.play();
				if(player.potioneffects.containsKey("Haste")) {
					dmg = tool.level + 1 + random.nextInt(4);
				} else {
					dmg = tool.level + 1 + random.nextInt(2);
				}
				
				health -= dmg;
				level.add(new TextParticle("" + dmg, x, y, Color.get(-1, 200, 300, 400)));
				if(health < 1) {
					level.remove(this);
					Sound.playerDeath.play();
					Player.score += 500;
				}

				return true;
			}
		}

		return false;
	}
	
	protected void touchedBy(Entity entity) {}
	
	public boolean use(Player player, int attackDir) {
		return false;
	}
	
	public void setMob(String newmob) {
		this.mob = newmob;
		
		Mob model = getMob(newmob, lvl);
		
		col0 = model.col1;
		col1 = model.col2;
		col2 = model.col3;
		col3 = model.col4;
		col = col2;
	}
	
	public Mob getMob(String string, int lvl) {
		switch(string) {
			case "Zombie": return (Mob)new Zombie(lvl);
			case "Slime": return (Mob)new Slime(lvl);
			case "Cow": return (Mob)new Cow(lvl);
			case "Sheep": return (Mob)new Sheep(lvl);
			case "Pig": return (Mob)new Pig(lvl);
			case "Creeper": return (Mob)new Creeper(lvl);
			case "Skeleton": return (Mob)new Skeleton(lvl);
			//case "Workbench": return (Entity)new Workbench();
			case "AirWizard": return (Mob)new AirWizard(false);
			case "AirWizardII": return (Mob)new AirWizard(true);
			// these are not mobs; only mobs should be gotten here
			/*case "Chest": return (Entity)new Chest();
			case "DeathChest": return (Entity)new Chest(true);
			case "DungeonChest": return (Entity)new DungeonChest();
			case "Anvil": return (Entity)new Anvil();
			case "Enchanter": return (Entity)new Enchanter();
			case "Loom": return (Entity)new Loom();
			case "Furnace": return (Entity)new Furnace();
			case "Oven": return (Entity)new Oven();
			case "Bed": return (Entity)new Bed();
			case "Tnt": return (Entity)new Tnt();
			case "Lantern": return (Entity)new Lantern();
			case "IronLantern": return (Entity)new IronLantern();
			case "GoldLantern": return (Entity)new GoldLantern();
			*/case "Knight": return (Mob)new Knight(lvl);
			case "Snake": return (Mob)new Snake(lvl);
			default:
				/* spawners are NOT entities; should not pass a spawner type
				if (string.contains(" Spawner") {
					String type = string.split(" ")[0];
					if(type.length() > 0)
						return (Entity)new Spawner(type, lvl);
				}*/
				return (Mob)new Zombie(lvl); // fix: make a missing texture entity!
		}
	}
}
