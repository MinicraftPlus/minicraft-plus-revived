package minicraft.entity;

import minicraft.Game;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.item.FurnitureItem;
import minicraft.item.PowerGloveItem;

/** Many furniture classes are very similar; they might not even need to be there at all... */

public class Furniture extends Entity {
	
	protected int pushTime = 0; // time for each push.
	protected int pushDir = -1; // the direction to push the furniture
	public Sprite sprite;
	public String name;
	protected Player shouldTake; // the player that should take the furniture
	
	public Furniture(String name, Sprite sprite) { this(name, sprite, 3, 3); }
	public Furniture(String name, Sprite sprite, int xr, int yr) {
		// all of these are 2x2 on the spritesheet; radius is for collisions only.
		super(xr, yr);
		this.name = name;
		this.sprite = sprite;
		col = sprite.color;
		//xr = 3; // x radius of the furniture
		//yr = 3; // y radius of the furniture
	}
	
	public Furniture clone() {
		try {
			return getClass().newInstance();//new Furniture(name, color, sprite, xr, yr);
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public void tick() {
		if (shouldTake != null) { // if the player that should take this exists...
			if (shouldTake.activeItem instanceof PowerGloveItem) { // ...and the player's holding a power glove...
				remove(); // remove this from the world
				shouldTake.inventory.add(0, shouldTake.activeItem); // put the power glove into the player's inventory
				shouldTake.activeItem = new FurnitureItem(this); // make this the player's current item.
			}
			shouldTake = null; // the player is now dereferenced.
		}
		// moves the furniture in the correct direction.
		if (pushDir == 0) move(0, +1);
		if (pushDir == 1) move(0, -1);
		if (pushDir == 2) move(-1, 0);
		if (pushDir == 3) move(+1, 0);
		pushDir = -1; // makes pushDir -1 so it won't repeat itself.
		if (pushTime > 0) pushTime--; // update pushTime by subtracting 1.
	}
	
	/** Draws the furniture on the screen. */
	public void render(Screen screen) {
		sprite.render(screen, x-8, y-8);
		// renders each corner, which different lighting depending on the time of day.
		/*col = col4;
		if (Game.time == 0) col = col0;
		if (Game.time == 1) col = col1;
		if (Game.time == 2) col = col2;
		if (Game.time == 3) col = col3;
		*/
		/*screen.render(x - 8, y - 8 - 4, sprite * 2 + 8 * 32, col, 0);
		screen.render(x - 0, y - 8 - 4, sprite * 2 + 8 * 32 + 1, col, 0);
		screen.render(x - 8, y - 0 - 4, sprite * 2 + 8 * 32 + 32, col, 0);
		screen.render(x - 0, y - 0 - 4, sprite * 2 + 8 * 32 + 33, col, 0);
		*/
	}
	
	/** Determines if this entity can block others */
	public boolean blocks(Entity e) {
		return true; // yes this can block your way (Needed for pushing)
	}
	
	/** What happens when this is touched by another entity */
	protected void touchedBy(Entity entity) {
		/// ADD TO THIS METHOD TO REMOVE UNNECESSARY CLASSES? maybe combine others into a "CraftingFurniture" class?
		if (entity instanceof Player && pushTime == 0) {
			pushDir = ((Player) entity).dir; // set pushDir to the player's dir.
			pushTime = 10; // set pushTime to 10.
		}
	}
	
	/** Used in PowerGloveItem.java */
	public void take(Player player) {
		shouldTake = player; // assigns the player that should take this
	}
	
	public boolean canWool() {
		return true;
	}
}
