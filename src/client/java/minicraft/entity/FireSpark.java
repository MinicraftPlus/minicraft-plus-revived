package minicraft.entity;

import minicraft.core.Game;
import minicraft.entity.mob.ObsidianKnight;
import minicraft.entity.mob.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteLinker;

public class FireSpark extends Entity {
	private static final SpriteLinker.LinkedSprite sprite = new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Entity, "spark");

	private final int lifeTime; // How much time until the spark disappears
	private final double xa, ya; // The x and y acceleration
	private double xx, yy; // The x and y positions
	private int time; // The amount of time that has passed
	private int stoppedTime;
	private boolean stopped;
	private final ObsidianKnight owner; // The Obsidian Knight that created this spark

	/**
	 * Creates a new spark. Owner is the Obsidian Knight which is spawning this spark.
	 *
	 * @param owner The Obsidian Knight spawning the spark.
	 * @param xa    X velocity.
	 * @param ya    Y velocity.
	 */
	public FireSpark(ObsidianKnight owner, double xa, double ya) {
		super(0, 0);

		this.owner = owner;
		xx = owner.x;
		yy = owner.y;
		this.xa = xa;
		this.ya = ya;

		// Max time = 199 ticks. Min time = 180 ticks.
		lifeTime = 60 * 3 + random.nextInt(20);
	}

	@Override
	public void tick() {
		time++;
		if (time >= lifeTime) {
			remove(); // Remove this from the world
			return;
		}

		if (stopped) {
			stoppedTime++;
			if (stoppedTime >= 50) { // Despawn if the spark has been stopped for 50 ticks.
				remove();
				return;
			}
		} else {
			// Move the spark:
			int x0 = (int) xx; // Original position
			int y0 = (int) yy;
			xx += xa; // Final position
			yy += ya;
			boolean stopped = true;
			//noinspection RedundantIfStatement
			if (moveX(((int) xx) - x0))
				stopped = false; // This kind of difference is handled due to errors by flooring.
			if (moveY(((int) yy) - y0)) stopped = false;
			if (stopped) {
				this.stopped = true;
			}
		}

		Player player = getClosestPlayer();
		if (player != null) { // Failsafe if player dies in a fire spark.
			if (player.isWithin(0, this)) {
				player.burn(5); // Burn the player for 5 seconds
			}
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

		int xt = 8;
		int yt = 13;

		screen.render(x - 4, y - 4 + 2, sprite.getSprite(), randmirror, false, Color.BLACK); // renders the shadow on the ground
		screen.render(x - 4, y - 4 - 2, sprite.getSprite(), randmirror, false, Color.RED); // Renders the spark
	}

	/**
	 * Returns the owners id as a string.
	 *
	 * @return the owners id as a string.
	 */
	public String getData() {
		return owner.eid + "";
	}
}
