package com.mojang.ld22.entity;

import com.mojang.ld22.Game;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.item.FurnitureItem;
import com.mojang.ld22.item.PowerGloveItem;

/** Many furniture classes are very similar; they might not even need to be there at all... */

public class Furniture extends Entity {
	private int pushTime = 0; // time for each push.
	private int pushDir = -1; // the direction to push the furniture
	public int col, col0, col1, col2, col3, sprite; // color and sprite vars.
	public String name;
	public int lightTimer = 0; //?
	private Player shouldTake; // the player that should take the furniture

	public Furniture(String name) {
		this.name = name;
		xr = 3; // x radius of the furniture
		yr = 3; // y radius of the furniture
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
		// renders each corner, which different lighting depending on the time of day.
		if (Game.time == 0) {
			screen.render(x - 8, y - 8 - 4, sprite * 2 + 8 * 32, col0, 0); // top left
			screen.render(x - 0, y - 8 - 4, sprite * 2 + 8 * 32 + 1, col0, 0); // top right
			screen.render(x - 8, y - 0 - 4, sprite * 2 + 8 * 32 + 32, col0, 0); // bottom left
			screen.render(x - 0, y - 0 - 4, sprite * 2 + 8 * 32 + 33, col0, 0); // bottom right
		}
		if (Game.time == 1) {
			screen.render(x - 8, y - 8 - 4, sprite * 2 + 8 * 32, col1, 0);
			screen.render(x - 0, y - 8 - 4, sprite * 2 + 8 * 32 + 1, col1, 0);
			screen.render(x - 8, y - 0 - 4, sprite * 2 + 8 * 32 + 32, col1, 0);
			screen.render(x - 0, y - 0 - 4, sprite * 2 + 8 * 32 + 33, col1, 0);
		}
		if (Game.time == 2) {
			screen.render(x - 8, y - 8 - 4, sprite * 2 + 8 * 32, col2, 0);
			screen.render(x - 0, y - 8 - 4, sprite * 2 + 8 * 32 + 1, col2, 0);
			screen.render(x - 8, y - 0 - 4, sprite * 2 + 8 * 32 + 32, col2, 0);
			screen.render(x - 0, y - 0 - 4, sprite * 2 + 8 * 32 + 33, col2, 0);
		}
		if (Game.time == 3) {
			screen.render(x - 8, y - 8 - 4, sprite * 2 + 8 * 32, col3, 0);
			screen.render(x - 0, y - 8 - 4, sprite * 2 + 8 * 32 + 1, col3, 0);
			screen.render(x - 8, y - 0 - 4, sprite * 2 + 8 * 32 + 32, col3, 0);
			screen.render(x - 0, y - 0 - 4, sprite * 2 + 8 * 32 + 33, col3, 0);
		}
	}
	
	/** Determines if this entity can block others */
	public boolean blocks(Entity e) {
		return true; // yes this can block your way (Needed for pushing)
	}
	
	/** What happens when this is touched by another entity */
	protected void touchedBy(Entity entity) {
		/// ADD TO THIS METHOD TO REMOVE UNNECESSARY CLASSES? maybe combine others into a "CraftingFurniture" class?
		if (entity instanceof Player && pushTime == 0) {
			if (name != "D.Chest") { // can't push death chests
				pushDir = ((Player) entity).dir; // set pushDir to the player's dir.
				pushTime = 10; // set pushTime to 10.
			}
		}
	}
	
	/** Used in PowerGloveItem.java */
	public void take(Player player) {
		if (name != "D.Chest") { //can't grab death chests
			shouldTake = player; // assigns the player that should take this
		}
	}
	
	//..?
	public boolean canWool() {
		return true;
	}
}
