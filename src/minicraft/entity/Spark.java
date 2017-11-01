package minicraft.entity;

import java.util.List;

import minicraft.gfx.Color;
import minicraft.gfx.Screen;

public class Spark extends Entity {
	private int lifeTime; // how much time until the spark disappears
	private double xa, ya; // the x and y acceleration
	private double xx, yy; // the x and y positions
	private int time; // the amount of time that has passed
	private AirWizard owner; // the AirWizard that created this spark
	
	/**
	 * Creates a new spark. Owner is the AirWizard which is spawning this spark.
	 * @param owner The AirWizard spawning the spark.
	 * @param xa X velocity.
	 * @param ya Y velocity.
	 */
	public Spark(AirWizard owner, double xa, double ya) {
		super(0, 0);
		
		this.owner = owner;
		xx = owner.x;
		yy = owner.y;
		this.xa = xa;
		this.ya = ya;
		
		// Max time = 629 ticks. Min time = 600 ticks.
		lifeTime = 60 * 10 + random.nextInt(30);
	}
	
	@Override
	public void tick() {
		time++;
		if (time >= lifeTime) {
			remove(); // remove this from the world
			return;
		}
		// move the spark:
		xx += xa;
		yy += ya;
		x = (int) xx;
		y = (int) yy;
		List<Entity> toHit = level.getEntitiesInRect(x, y, x, y); // gets the entities in the current position to hit.
		for (int i = 0; i < toHit.size(); i++) {
			Entity e = toHit.get(i);
			if (e instanceof Mob && !(e instanceof AirWizard)) {
				 // if the entity is a mob, but not a Air Wizard, then hurt the mob with 1 damage.
				e.hurt(owner, 1, Mob.getAttackDir(this, e));
			}
		}
	}
	
	/**
	 * Sparks can't block any mob's movement.
	 */
	@Override
	public boolean isBlockableBy(Mob mob) {
		return false;
	}

	@Override
	public void render(Screen screen) {
		/* this first part is for the blinking effect */
		if (time >= lifeTime - 6 * 20) {
			if (time / 6 % 2 == 0) return; // if time is divisible by 12, then skip the rest of the code.
		}
		
		int xt = 8;
		int yt = 13;
		
		///shouldn't the random be stored before being used twice...? Let me try.
		int randmirror = random.nextInt(4);
		screen.render(x - 4, y - 4 - 2, xt + yt * 32, Color.WHITE, randmirror); // renders the spark
		screen.render(x - 4, y - 4 + 2, xt + yt * 32, Color.BLACK, randmirror); // renders the shadow on the ground
	}
	
	/**
	 * Returns the owners id as a string.
	 * @return the owners id as a string.
	 */
	public String getData() {
		return owner.eid+"";
	}
}
