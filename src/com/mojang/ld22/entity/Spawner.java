package com.mojang.ld22.entity;

import com.mojang.ld22.entity.particle.FireParticle;
import com.mojang.ld22.entity.particle.TextParticle;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.item.Item;
import com.mojang.ld22.item.ToolItem;
import com.mojang.ld22.item.ToolType;
import com.mojang.ld22.sound.Sound;
import java.util.Random;

public class Spawner extends Furniture {

	public Entity mob;
	Random rnd = new Random();
	int randx = 2;
	int randy = 2;
	int r = 90;
	int health = 100;
	public int lvl = 1;
	int tick = 0;
	boolean spawn = false;
	int dmg = 0;
	
	
	public Spawner(Entity m, int level) {
		super(m.getClass().getCanonicalName().replace("com.mojang.ld22.entity.", "") + " Spawner");
		mob = m;
		lvl = level;
		/*
			The problem with the colors is likely occuring right here; perhaps the mob's colors are not yet set at this time.
			
			(after checking Creeper.java) Ha! I was right!
		*/
		col0 = m.col1;
		col1 = m.col2;
		col2 = m.col3;
		col3 = m.col4;
		col = col2;
		sprite = 10;
		xr = 7;
		yr = 2;
	}

	public void tick() {
		super.tick();
		int xd = level.player.x - x;
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
		}

		if(tick > 180) {
			tick = 0;
			spawn = true;
		}

		if(spawn && mob != null) {
			xd = level.player.x - x;
			yd = level.player.y - y;
			if(xd * xd + yd * yd > r * r) {
				return;
			}

			Object newmob = getEntity(mob.getClass().getCanonicalName().replace("com.mojang.ld22.entity.", ""), lvl);
			if(newmob instanceof Mob) {
				newmob = (Mob)newmob;
				getEntity(newmob.getClass().getCanonicalName().replace("com.mojang.ld22.entity.", ""), lvl);
				if(col1 == Color.get(-1, 0, 4, 46)) {
					newmob = getEntity(mob.getClass().getCanonicalName().replace("com.mojang.ld22.entity.", "") + "II", lvl);
				}
			}

			if(level.getTile((x - 16 + rnd.nextInt(32)) / 16, (y - 16 + rnd.nextInt(32)) / 16).mayPass(level, (x - 16 + rnd.nextInt(32)) / 16, (y - 16 + rnd.nextInt(32)) / 16, this) && level.getTile((x - 16 + rnd.nextInt(32)) / 16, (y - 16 + rnd.nextInt(32)) / 16).getLightRadius(level, (x - 16 + rnd.nextInt(32)) / 16, (y - 16 + rnd.nextInt(32)) / 16) == 0) {
				((Entity)newmob).x = x - 16 + rnd.nextInt(32);
				((Entity)newmob).y = y - 16 + rnd.nextInt(32);
				((Entity)newmob).hasspawned = true;
				level.add((Entity)newmob);

				for(int i = 0; i < 6; i++) {
					randx = rnd.nextInt(16);
					randy = rnd.nextInt(12);
					level.add(new FireParticle(x - 4 + randx, y - 4 + randy));
				}
			}

			spawn = false;
		}

	}

	public boolean interact(Player player, Item item, int attackDir) {
		if(item instanceof ToolItem) {
			ToolItem tool = (ToolItem)item;
			if(tool.type == ToolType.pickaxe) {
				Sound.monsterHurt.play();
				if(player.haste) {
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
				if(player.haste) {
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

	public Entity getEntity(String string, int lvl) {
		switch(string) {
			case "Zombie": return (Entity)new Zombie(lvl);
			case "Slime": return (Entity)new Slime(lvl);
			case "Cow": return (Entity)new Cow(lvl);
			case "Sheep": return (Entity)new Sheep(lvl);
			case "Pig": return (Entity)new Pig(lvl);
			case "Creeper": return (Entity)new Creeper(lvl);
			case "Skeleton": return (Entity)new Skeleton(lvl);
			case "Workbench": return (Entity)new Workbench();
			case "AirWizard": return (Entity)new AirWizard(false);
			case "AirWizardII": return (Entity)new AirWizard(true);
			case "Chest": return (Entity)new Chest();
			case "DeathChest": return (Entity)new Chest(true);
			case "DungeonChest": return (Entity)new DungeonChest();
			case "Anvil": return (Entity)new Anvil();
			case "Enchanter": return (Entity)new Enchanter();
			case "Loom": return (Entity)new Loom();
			case "Furnace": return (Entity)new Furnace();
			case "Spawner": return (Entity)new Spawner(new Zombie(lvl), lvl);
			case "Oven": return (Entity)new Oven();
			case "Bed": return (Entity)new Bed();
			case "Tnt": return (Entity)new Tnt();
			case "Lantern": return (Entity)new Lantern();
			case "IronLantern": return (Entity)new IronLantern();
			case "GoldLantern": return (Entity)new GoldLantern();
			case "Knight": return (Entity)new Knight(lvl);
			case "Snake": return (Entity)new Snake(lvl);
			default: return new Zombie(lvl);
		}
	}
}
