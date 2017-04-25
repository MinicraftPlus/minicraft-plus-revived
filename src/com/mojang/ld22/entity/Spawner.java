package com.mojang.ld22.entity;

import com.mojang.ld22.Game;
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
	public String mob;
	public int lvl;
	int r, health, tick, dmg;
	
	public Spawner(String m, int level) {
		super(m + " Spawner");
		lvl = level;
		
		health = 100;
		r = 8*16; // activate from up to 8 blocks away
		tick = 0;
		dmg = 0;
		
		setMob(m);
		sprite = 10;
		xr = 7;
		yr = 2;
	}
	
	public void tick() {
		super.tick();
		
		tick++;
		if(tick > 360) {
			tick = 0;
			trySpawn();
		}
	}
	
	private void trySpawn() {
		int xd = level.player.x - x;
		int yd = level.player.y - y;
		//if (Game.debug) System.out.println("spawn attempt trigged for " + mob + "; player dist: " + Math.sqrt(xd * xd + yd * yd) + "; max dist: " + r);
		if(xd * xd + yd * yd > r * r) return;
		
		Mob newmob = getMob(mob, lvl);
		
		int randX = (x/16 - 1 + rnd.nextInt(2)); // the rand is really just one tile in any direction
		int randY = (y/16 - 1 + rnd.nextInt(2));
		Tile tile = level.getTile(randX, randY);
		//if (Game.debug) System.out.println("attempting " + mob + " spawn on tile with id: " + tile.id);
		if(tile.mayPass(level, randX, randY, newmob) && tile.getLightRadius(level, randX, randY) == 0) {
			newmob.x = randX << 4;
			newmob.y = randY << 4;
			//(newmob).hasspawned = true;
			if (Game.debug) System.out.println("spawning new " + mob + " on level "+lvl+": x=" + (newmob.x>>4)+" y="+(newmob.y>>4) + "...");
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
			case "Cow": return (Mob)new Cow();
			case "Sheep": return (Mob)new Sheep();
			case "Pig": return (Mob)new Pig();
			case "Creeper": return (Mob)new Creeper(lvl);
			case "Skeleton": return (Mob)new Skeleton(lvl);
			case "AirWizard": return (Mob)new AirWizard(lvl>1);
			//case "AirWizardII": return (Mob)new AirWizard(true);
			case "Knight": return (Mob)new Knight(lvl);
			case "Snake": return (Mob)new Snake(lvl);
			default:
				return null; // fix: make a missing texture entity!
		}
	}
}
