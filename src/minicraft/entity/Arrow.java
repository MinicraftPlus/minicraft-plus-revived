package minicraft.entity;

import java.util.List;

import minicraft.entity.mob.Mob;
import minicraft.entity.mob.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Rectangle;
import minicraft.gfx.Screen;

public class Arrow extends Entity implements ClientTickable {
	private Direction dir;
	private int damage;
	public Mob owner;
	private int speed;
	
	public Arrow(Mob owner, Direction dir, int dmg) {
		this(owner, owner.x, owner.y, dir, dmg);
	}
	public Arrow(Mob owner, int x, int y, Direction dir, int dmg) {
		super(Math.abs(dir.getX())+1, Math.abs(dir.getY())+1);
		this.owner = owner;
		this.x = x;
		this.y = y;
		this.dir = dir;
		
		damage = dmg;
		col = Color.get(-1, 111, 222, 430);
		
		if (damage > 3) speed = 8;
		else if (damage >= 0) speed = 7;
		else speed = 6;
	}
	
	/**
	 * Generates information about the arrow.
	 * @return string representation of owner, xdir, ydir and damage.
	 */
	public String getData() {
		return owner.eid+":"+dir.ordinal()+":"+damage;
	}
	
	@Override
	public void tick() {
		if (x < 0 || x>>4 > level.w || y < 0 || y>>4 > level.h) {
			remove(); // remove when out of bounds
			return;
		}

		x += dir.getX() * speed;
		y += dir.getY() * speed;
		
		// TODO I think I can just use the xr yr vars, and the normal system with touchedBy(entity) to detect collisions instead.
		List<Entity> entitylist = level.getEntitiesInRect(new Rectangle(x, y, 0, 0, Rectangle.CENTER_DIMS));
		boolean criticalHit = random.nextInt(11) < 9;
		for (int i = 0; i < entitylist.size(); i++) {
			Entity hit = entitylist.get(i);
			
			if (hit instanceof Mob && hit != owner) {
				Mob mob = (Mob) hit;
				int extradamage = (hit instanceof Player ? 0 : 3) + (criticalHit ? 0 : 1);
				mob.hurt(owner, damage + extradamage, dir);
			}

			if (!level.getTile(x / 16, y / 16).mayPass(level, x / 16, y / 16, this)
					&& !level.getTile(x / 16, y / 16).connectsToWater
					&& level.getTile(x / 16, y / 16).id != 16) {
				this.remove();
			}
		}
	}

	public boolean isSolid() {
		return false;
	}

	@Override
	public void render(Screen screen) {
		int xt = 13;
		int yt = 5;

		if(dir == Direction.LEFT) xt = 14;
		if(dir == Direction.UP) xt = 15;
		if(dir == Direction.DOWN) xt = 16;
		
		screen.render(x - 4, y - 4, xt + yt * 32, col, 0);
	}
}
