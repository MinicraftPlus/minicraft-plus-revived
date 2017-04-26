package minicraft.entity;

import minicraft.gfx.Color;
import minicraft.gfx.Screen;
import java.util.List;

public class Arrow extends Entity {
	private int lifeTime;
	private int xdir;
	private int ydir;
	private int time;
	private int damage;
	public Mob owner;
	private int speed;
	
	public Arrow(Mob owner, int dirx, int diry, int dmg, boolean flag) {
		super(Math.abs(dirx)+1, Math.abs(diry)+1);
		this.owner = owner;
		xdir = dirx;
		ydir = diry;
		
		damage = dmg;
		col = Color.get(-1, 111, 222, 430);
		
		if (damage > 3) speed = 3;
		else if (damage >= 0) speed = 2;
		else speed = 1;
		
		if (flag) {
			damage = 2*damage + 1;
			col = Color.get(-1, 111, 222, 430);
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

		x += xdir * speed;
		y += ydir * speed;
		
		// TODO I think I can just use the xr yr vars, and the normal system with touchedBy(entity) to detect collisions instead.
		List<Entity> entitylist = level.getEntities(x, y, x, y);
		boolean criticalHit = random.nextInt(11) < 9;
		for (int i = 0; i < entitylist.size(); i++) {
			Entity hit = entitylist.get(i);
			
			if (hit != null && hit instanceof Mob && hit != owner) {
				Mob mob = (Mob) hit;
				int extradamage = (hit instanceof Player ? 0 : 3) + (criticalHit ? 0 : 1);
				mob.hurt(owner, damage + extradamage, mob.dir);
			}
			
			if (!level.getTile(x / 16, y / 16).mayPass(level, x / 16, y / 16, this)
					&& !level.getTile(x / 16, y / 16).connectsToWater
					&& level.getTile(x / 16, y / 16).id != 16) {
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
		byte xt;
		byte yt;
		//if(minicraft.Game.debug) System.out.println(xdir + " " + ydir + " ");
		if (xdir == 0 && ydir == -1) {
			xt = 15;
			yt = 5;

			screen.render(x - 4, y - 4, xt + yt * 32, col, 1);

		} else if (xdir == 1 && ydir == 0) {
			xt = 14;
			yt = 5;
			screen.render(x - 4, y - 4, xt + yt * 32, col, 1);
		} else if (xdir == -1 && ydir == 0) {
			xt = 13;
			yt = 5;

			screen.render(x - 4, y - 4, xt + yt * 32, col, 1);
		} else if (xdir == 0 && ydir == 1) {
			xt = 16;
			yt = 5;

			screen.render(x - 4, y - 4, xt + yt * 32, col, 1);
		}
	}
}
