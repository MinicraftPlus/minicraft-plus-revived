package minicraft.entity;

import minicraft.entity.mob.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Screen;
import minicraft.item.Item;

import java.util.List;

public class ItemEntity extends Entity implements ClientTickable {
	private int lifeTime; // The life time of this entity in the level
	private double xa, ya, za; // The x, y, and z accelerations.
	private double xx, yy, zz; // The x, y, and z coordinates; in double precision.
	public Item item; // The item that this entity is based off of.
	private int time = 0; // Time it has lasted in the level

	// Solely for multiplayer use.
	private boolean pickedUp = false;
	private long pickupTimestamp;

	/**
	 * Creates an item entity of the item item at position (x,y) with size 2*2.
	 * @param item Item to add as item entity
	 * @param x position on map
	 * @param y position on map
	 */
	public ItemEntity(Item item, int x, int y) {
		super(2, 2);

		this.item = item.copy();
		this.x = x;
		this.y = y;
		xx = x;
		yy = y;

		zz = 2;
		// Random direction for each acceleration
		xa = random.nextGaussian() * 0.3;
		ya = random.nextGaussian() * 0.2;
		za = random.nextFloat() * 0.7 + 1;

		lifeTime = 60 * 10 + random.nextInt(70); // Sets the lifetime of the item. min = 600 ticks, max = 669 ticks.
		// The idea was to have it last 10-11 seconds, I think.
	}

	/**
	 * Creates an item entity of the item item at position (x,y) with size 2*2.
	 * @param item Item to add as item entity.
	 * @param x position on map
	 * @param y position on map
	 * @param zz z position?
	 * @param lifetime lifetime (in ticks) of the entity.
	 * @param time starting time (in ticks) of the entity.
	 * @param xa x velocity
	 * @param ya y velocity
	 * @param za z velocity?
	 */
	public ItemEntity(Item item, int x, int y, double zz, int lifetime, int time, double xa, double ya, double za) {
		this(item, x, y);
		this.lifeTime = lifetime;
		this.time = time;
		this.zz = zz;
		this.xa = xa;
		this.ya = ya;
		this.za = za;
	}

	/**
	 * Returns a string representation of the itementity
	 * @return string representation of this entity
	 */
	public String getData() {
		return String.join(":", (new String[] { item.getData(), zz + "", lifeTime + "", time + "", xa + "", ya + "", za + "" }));
	}

	@Override
	public void tick() {
		time++;
		if (time >= lifeTime) { // If the time is larger or equal to lifeTime then...
			remove(); // Remove from the world
			return; // Skip the rest of the code
		}
		// Moves each coordinate by the its acceleration
		xx += xa;
		yy += ya;
		zz += za;
		if (zz < 0) { // If z pos is smaller than 0 (which probably marks hitting the ground)
			zz = 0; // Set it to zero
			// Multiply the accelerations by an amount:
			za *= -0.5;
			xa *= 0.6;
			ya *= 0.6;
		}
		za -= 0.15; // Decrease z acceleration by 0.15

		// Storage of x and y positions before move
		int ox = x;
		int oy = y;

		// Integer conversion of the double x and y postions (which have already been updated):
		int nx = (int) xx;
		int ny = (int) yy;

		// The difference between the double->int new positions, and the inherited x and y positions:
		int expectedx = nx - x; // Expected movement distance
		int expectedy = ny - y;

		/// THIS is where x and y are changed.
		move(expectedx, expectedy); // Move the ItemEntity.

		// Finds the difference between the inherited before and after positions
		int gotx = x - ox;
		int goty = y - oy;

		// Basically, this accounts for any error in the whole double-to-int position conversion thing:
		xx += gotx - expectedx;
		yy += goty - expectedy;
	}

	public boolean isSolid() {
		return false; // Mobs cannot block this
	}

	@Override
	public void render(Screen screen) {
		/* This first part is for the blinking effect */
		if (time >= lifeTime - 6 * 20) {
			if (time / 6 % 2 == 0) return;
		}

		screen.render(x - 4, y - 4, item.sprite.getSprite(), 0, false, Color.get(0, 31)); // Item shadow
		screen.render(x - 4, y - 4 - (int) zz, item.sprite); // Item
	}

	@Override
	protected void touchedBy(Entity entity) {
		if (!(entity instanceof Player)) return; // For the time being, we only care when a player touches an item.

		if (time > 30) { // Conditional prevents this from being collected immediately.
			if (!pickedUp) {// Don't register if we are online and a player touches it; the client will register that.
				pickedUp = true;
				((Player) entity).pickupItem(this);
				pickedUp = isRemoved();
			}
		}
	}

	@Override
	protected List<String> getDataPrints() {
		List<String> prints = super.getDataPrints();
		prints.add(0, item.toString());
		return prints;
	}
}
