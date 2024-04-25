package minicraft.entity;

import minicraft.core.Game;
import minicraft.entity.mob.AirWizard;
import minicraft.entity.mob.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.gfx.SpriteLinker.SpriteType;

public class Spark extends Entity {
	private final int lifeTime; // How much time until the spark disappears
	private final double xa;
	private final double ya; // The x and y acceleration
	private double xx, yy; // The x and y positions
	private int time; // The amount of time that has passed
	private final AirWizard owner; // The AirWizard that created this spark
	private LinkedSprite sprite = new LinkedSprite(SpriteType.Entity, "spark");

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
			remove(); // Remove this from the world
			return;
		}
		// Move the spark:
		xx += xa;
		yy += ya;
		x = (int) xx;
		y = (int) yy;

		Player player = getClosestPlayer();
		if (player != null && player.isWithin(0, this)) {
			player.hurt(owner, 1);
		}
	}

	/**
	 * Can this entity block you? Nope.
	 */
	public boolean isSolid() {
		return false;
	}

	@Override
	public void render(Screen screen) {
		int randmirror = 0;

		// If we are in a menu, or we are on a server.
		if (Game.getDisplay() == null) {
			// The blinking effect.
			if (time >= lifeTime - 6 * 20) {
				if (time / 6 % 2 == 0) return; // If time is divisible by 12, then skip the rest of the code.
			}


			randmirror = random.nextInt(4);
		}

		sprite.setMirror(randmirror);
		screen.render(x - 4, y - 4 + 2, sprite.getSprite(), 0, false, Color.BLACK); // renders the shadow on the ground
		screen.render(x - 4, y - 4 - 2, sprite); // Renders the spark
	}

	/**
	 * Returns the owners id as a string.
	 * @return the owners id as a string.
	 */
	public String getData() {
		return owner.eid + "";
	}
}
