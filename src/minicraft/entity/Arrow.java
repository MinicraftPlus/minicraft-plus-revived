package minicraft.entity;

import java.util.List;

import minicraft.gfx.Color;
import minicraft.gfx.Screen;

public class Arrow extends Entity {
	private int xdir;
	private int ydir;
	private int damage;
	public Mob owner;
	private int speed;
	
	/**
	 * Constructs an arrow at the owner's (mob who shoots arrow) position. 
	 * @param owner Mob shooting the arrow.
	 * @param dirx Horizontal direction.
	 * @param diry Vertical direction.
	 * @param dmg How much damage the arrow does.
	 */
	public Arrow(Mob owner, int dirx, int diry, int dmg) {
		this(owner, owner.x, owner.y, dirx, diry, dmg);
	}
	
	/**
	 * Constructs an arrow.
	 * @param owner Mob shooting the arrow.
	 * @param x Starting X map position.
	 * @param y Starting Y map position.
	 * @param dirx Horizontal direction.
	 * @param diry Vertical direction.
	 * @param dmg How much damage the arrow does.
	 */
	public Arrow(Mob owner, int x, int y, int dirx, int diry, int dmg) {
		super(Math.abs(dirx)+1, Math.abs(diry)+1);
		this.owner = owner;
		this.x = x;
		this.y = y;
		xdir = dirx;
		ydir = diry;
		
		damage = dmg;
		col = Color.get(-1, 111, 222, 430);
		
		if (damage > 3) speed = 3;
		else if (damage >= 0) speed = 2;
		else speed = 1;
		
		/* // maybe this was a "critical arrow" system or something?
		if (flag) {
			damage = 2*damage + 1;
			col = Color.get(-1, 111, 222, 430);
		}*/
	}
	
	/**
	 * Generates information about the arrow.
	 * @return string representation of owner, xdir, ydir and damage.
	 */
	public String getData() {
		return owner.eid+":"+xdir+":"+ydir+":"+damage;
	}
	
	@Override
	public void tick() {
		if (x < 0 || x>>4 > level.w || y < 0 || y>>4 > level.h) {
			remove(); // remove when out of bounds
			return;
		}

		x += xdir * speed;
		y += ydir * speed;
		
		// TODO I think I can just use the xr yr vars, and the normal system with touchedBy(entity) to detect collisions instead.
		List<Entity> entitylist = level.getEntitiesInRect(x, y, x, y);
		boolean criticalHit = random.nextInt(11) < 9;
		for (int i = 0; i < entitylist.size(); i++) {
			Entity hit = entitylist.get(i);
			
			if (hit != null && hit instanceof Mob && hit != owner) {
				Mob mob = (Mob) hit;
				int extradamage = (hit instanceof Player ? 0 : 3) + (criticalHit ? 0 : 1);
				mob.hurt(owner, damage + extradamage, (xdir<0?2:(xdir>0?3:(ydir<0?1:0)))); // that should correctly convert to mob directions.
			}
			
			/*if(owner instanceof Player && minicraft.screen.Game.isMode("creative") && minicraft.Game.debug) {
				/// debug fun!
				level.getTile(x/16, y/16).hurt(level, x/16, y/16, 500); // KO all tiles
			} else { /// normal behavior
			*/	if (!level.getTile(x / 16, y / 16).mayPass(level, x / 16, y / 16, this)
						&& !level.getTile(x / 16, y / 16).connectsToWater
						&& level.getTile(x / 16, y / 16).id != 16) {
					this.remove();
				}
			//}
		}
	}

	@Override
	public boolean isBlockableBy(Mob mob) {
		return false;
	}

	@Override
	public void render(Screen screen) {
		/* // probably makes a blinking effect.
		if (time >= lifeTime - 3 * 20) {
			if (time / 6 % 2 == 0) return;
		}*/
		
		byte xt = 0;
		byte yt = 5;
		
		if (xdir == 0 && ydir == -1) xt = 15;
		else if (xdir == 1 && ydir == 0) xt = 14;
		else if (xdir == -1 && ydir == 0) xt = 13;
		else if (xdir == 0 && ydir == 1) xt = 16;
		
		screen.render(x - 4, y - 4, xt + yt * 32, col, 1);
	}
}