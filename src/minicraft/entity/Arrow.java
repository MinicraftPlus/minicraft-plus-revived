package minicraft.entity;

import java.util.HashMap;
import java.util.List;

import minicraft.gfx.Color;
import minicraft.gfx.Screen;

public class Arrow extends Entity {
	private int xdir;
	private int ydir;
	private int damage;
	public Mob owner;
	private int speed;
	
	// How many entities the arrow can hit before its removed.
	private int hitsLeft;
	// How much damage the arrow can do before its removed.
	private int damageLeft;
	// Chance of a critical hit in percent.
	private int criticalChance;
	// How much damage is added when the hit is critical.
	private int criticalDamage;
	// How much extra damage is done to non-player entities
	private int extraDamageToMobs;
	// Used to check if an arrow has already hit an entity. This allows
	// the arrow to pass through entities instead of just hitting it again the next tick.
	// Implemented as a HashMap so it is possible to make some arrows be able to hit the same entity twice.
	private HashMap<Entity, Integer> hitsOnEntities;
	// How many times the arrow can hit the same entity
	private int maxHitCount;
	
	public Arrow(Mob owner, int dirx, int diry, int dmg) {
		this(owner, owner.x, owner.y, dirx, diry, dmg);
	}
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
		
		// I picked some values which seems to work fine.
		// Critical damage and chance is the same as before.
		criticalDamage = 1;
		criticalChance = 20;
		hitsLeft = 2;
		extraDamageToMobs = 3;
		maxHitCount = 1;
		// At the moment this makes the arrow able to do full damage at both hits,
		// but only one of them can crit.
		damageLeft = hitsLeft * (damage + extraDamageToMobs) + criticalDamage;
		hitsOnEntities = new HashMap<>();
		
		
		/* // maybe this was a "critical arrow" system or something?
		if (flag) {
			damage = 2*damage + 1;
			col = Color.get(-1, 111, 222, 430);
		}*/
	}
	
	public String getData() {
		return owner.eid+":"+xdir+":"+ydir+":"+damage;
	}
	
	public void tick() {
		if (x < 0 || x>>4 > level.w || y < 0 || y>>4 > level.h) {
			remove(); // remove when out of bounds
			return;
		}

		x += xdir * speed;
		y += ydir * speed;
		
		// TODO I think I can just use the xr yr vars, and the normal system with touchedBy(entity) to detect collisions instead.
		List<Entity> entitylist = level.getEntitiesInRect(x, y, x, y);
		boolean doCriticalDamage = random.nextInt(101) < criticalChance;
		
		for (int i = 0; i < entitylist.size(); i++) {
			Entity hit = entitylist.get(i);
			
			if (hit != null && hit instanceof Mob && hit != owner) {
				Mob mob = (Mob) hit;
	
				// Doesn't hit the entity if it has already exceeded the allowed amount of hits
				if (hitsOnEntities.containsKey(hit)) {
					if (hitsOnEntities.get(hit) >= maxHitCount) {
						continue;
					}
				}
				
				int totalDamage = damage;
				if (doCriticalDamage) totalDamage += criticalDamage;
				if (!(hit instanceof Player)) totalDamage += extraDamageToMobs;
				
				// Caps the damage to the damage which can be done.
				if (totalDamage > damageLeft) {
					totalDamage = damageLeft;
					damageLeft = 0;
				} else {
					damageLeft -= totalDamage;
				}
				
				mob.hurt(owner, totalDamage, (xdir<0?2:(xdir>0?3:(ydir<0?1:0)))); // that should correctly convert to mob directions.
				hitsLeft--;
				
				// Updates the hit count from this arrow on the given entity.
				if (hitsOnEntities.containsKey(hit)) {
					int count = hitsOnEntities.get(hit);
					hitsOnEntities.put(hit, count + 1);
				} else {
					hitsOnEntities.put(hit, 1);
				}
				
				// Removes the arrow if it can't hit more entities or if it has dealt all its damage.
				if (hitsLeft <= 0 || damageLeft <= 0) {
					this.remove();
				}
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

	public boolean isBlockableBy(Mob mob) {
		return false;
	}

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
