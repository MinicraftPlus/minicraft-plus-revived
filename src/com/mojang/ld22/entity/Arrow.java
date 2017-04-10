//new class, no comments
package com.mojang.ld22.entity;

import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Screen;
import java.util.List;

public class Arrow extends Entity {
	private int lifeTime;
	private int xdir;
	private int ydir;
	private final int speed = 2;
	private int time;
	private int damage;
	private Mob owner;
	private Player player;
	private int color;
	private int speeddmg;
	private boolean edmg = false;

	public Arrow(Mob owner, int dirx, int diry, int dmg, boolean flag) {
		this.owner = owner;
		xdir = dirx;
		ydir = diry;

		damage = dmg;
		color = Color.get(-1, 111, 222, 430);

		if (damage > -1) {
			speeddmg = 2;
		}
		if (damage > 3) {
			speeddmg = 3;
		}

		if (flag) {
			damage *= 2 + 1;
			color = Color.get(-1, 111, 222, 430);
		}

		x = owner.x;
		y = owner.y;

		lifeTime = 100 * (damage + 2);
	}

	public void tick() {
		time++;
		if (time >= lifeTime) {
			remove();
			return;
		}

		x += xdir * speeddmg;
		y += ydir * speeddmg;
		List<Entity> entitylist = level.getEntities(x, y, x, y);
		int count = random.nextInt(11);
		for (int i = 0; i < entitylist.size(); i++) {
			Entity hit = entitylist.get(i);
			
			if (count < 9) {
				if (hit != null) {
					if (hit instanceof Mob && hit != owner && owner.isenemy == false) {
						hit.hurt(owner, damage + 3, ((Mob) hit).dir);
					}
					if (hit instanceof Player && hit != owner && owner.isenemy == true) {
						hit.hurt(owner, damage, ((Player) hit).dir);
					}
				}
			} else if (count > 8) {
				if (hit != null) {
					if (hit instanceof Mob && hit != owner && owner.isenemy == false) {
						hit.hurt(owner, damage + 4, ((Mob) hit).dir);
					}
					if (hit instanceof Player && hit != owner && owner.isenemy == true) {
						hit.hurt(owner, damage + 1, ((Player) hit).dir);
					}
				}
			}
			
			if (level.getTile(x / 16, y / 16).mayPass(level, x / 16, y / 16, this)
					|| level.getTile(x / 16, y / 16).connectsToWater
					|| level.getTile(x / 16, y / 16).id == 16) {
			} else {
				this.remove();
			}
		}
	}

	public boolean isBlockableBy(Mob mob) {
		return false;
	}

	public void render(Screen screen) {
		if (time >= lifeTime - 3 * 20) {
			if (time / 6 % 2 == 0) return;
		}
		int xt;
		int yt;
		//if(com.mojang.ld22.Game.debug) System.out.println(xdir + " " + ydir + " ");
		if (xdir == 0 && ydir == -1) {
			xt = 15;
			yt = 5;

			screen.render(x - 4, y - 4, xt + yt * 32, color, 1);

		} else if (xdir == 1 && ydir == 0) {
			xt = 14;
			yt = 5;
			screen.render(x - 4, y - 4, xt + yt * 32, color, 1);
		} else if (xdir == -1 && ydir == 0) {
			xt = 13;
			yt = 5;

			screen.render(x - 4, y - 4, xt + yt * 32, color, 1);
		} else if (xdir == 0 && ydir == 1) {
			xt = 16;
			yt = 5;

			screen.render(x - 4, y - 4, xt + yt * 32, color, 1);
		}
	}
}
