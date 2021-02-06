package minicraft.entity;

import java.util.List;

import minicraft.entity.mob.AirWizard;
import minicraft.entity.mob.Mob;
import minicraft.gfx.Color;
import minicraft.gfx.Rectangle;
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
		
		// Max time = 389 ticks. Min time = 360 ticks.
		lifeTime = 60 * 6 + random.nextInt(30);
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

		// if the entity is a mob, but not a Air Wizard, then hurt the mob with 1 damage.
		List<Entity> toHit = level.getEntitiesInRect(entity -> entity instanceof Mob && !(entity instanceof AirWizard), new Rectangle(x, y, 0, 0, Rectangle.CENTER_DIMS)); // gets the entities in the current position to hit.
		toHit.forEach(entity -> ((Mob) entity).hurt(owner, 1));
	}
	
	/** Can this entity block you? Nope. */
	public boolean isSolid() {
		return false;
	}

	@Override
	public void render(Screen screen) {
		/* this first part is for the blinking effect */
		if (time >= lifeTime - 6 * 20) {
			if (time / 6 % 2 == 0) return; // if time is divisible by 12, then skip the rest of the code.
		}

		int randmirror = random.nextInt(4);

		screen.render(x - 4, y - 4 - 2, 8 + 24 * 32, randmirror, 2); // renders the spark
	}
	
	/**
	 * Returns the owners id as a string.
	 * @return the owners id as a string.
	 */
	public String getData() {
		return owner.eid+"";
	}
}
