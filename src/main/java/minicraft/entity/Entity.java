package minicraft.entity;

import minicraft.core.Game;
import minicraft.core.Updater;
import minicraft.entity.mob.Player;
import minicraft.gfx.Rectangle;
import minicraft.gfx.Screen;
import minicraft.item.Item;
import minicraft.level.Level;
import minicraft.network.Network;
import minicraft.util.Logging;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class Entity implements Tickable {

	/* I guess I should explain something real quick. The coordinates between tiles and entities are different.
	 * The world coordinates for tiles is 128x128
	 * The world coordinates for entities is 2048x2048
	 * This is because each tile is 16x16 pixels big
	 * 128 x 16 = 2048.
	 * When ever you see a ">>", it means that it is a right shift operator. This means it shifts bits to the right (making them smaller)
	 * x >> 4 is the equivalent to x / (2^4). Which means it's dividing the X value by 16. (2x2x2x2 = 16)
	 * xt << 4 is the equivalent to xt * (2^4). Which means it's multiplying the X tile value by 16.
	 *
	 * These bit shift operators are used to easily get the X & Y coordinates of a tile that the entity is standing on.
	 */

	// Entity coordinates are per pixel, not per tile; each tile is 16x16 entity pixels.
	protected final Random random = new Random();
	public int x, y; // x, y entity coordinates on the map
	private int xr, yr; // x, y radius of entity
	private boolean removed; // If the entity is to be removed from the level.
	protected Level level; // The level that the entity is on.
	public int col; // Current color.

	// Numeric unique identifier for the entity.
	public int eid;

	/**
	 * Default constructor for the Entity class.
	 * Assings null/none values to the instace variables.
	 * The exception is removed which is set to true, and
	 * lastUpdate which is set to System.nanoTime().
	 * @param xr X radius of entity.
	 * @param yr Y radius of entity.
	 */
	public Entity(int xr, int yr) { // Add color to this later, in color update
		this.xr = xr;
		this.yr = yr;

		level = null;
		removed = true;
		col = 0;

		eid = -1;
	}

	public abstract void render(Screen screen); // Used to render the entity on screen.

	@Override
	public abstract void tick(); // Used to update the entity.

	/**
	 * Returns true if the entity is removed from the level, otherwise false.
	 * @return removed
	 */
	public boolean isRemoved() { return removed/* || level == null*/; }

	/**
	 * Returns the level which this entity belongs in.
	 * @return level
	 */
	public Level getLevel() { return level; }

	/** Returns a Rectangle instance using the defined bounds of the entity. */
	protected Rectangle getBounds() { return new Rectangle(x, y, xr * 2, yr * 2, Rectangle.CENTER_DIMS); }

	/** Returns true if this entity is found in the rectangle specified by given two coordinates. */
	public boolean isTouching(Rectangle area) { return area.intersects(getBounds()); }

	/** Returns if this entity stops other solid entities from moving. */
	public boolean isSolid() { return true; } // Most entities are solid

	/** Determines if the given entity should prevent this entity from moving. */
	public boolean blocks(Entity e) { return isSolid() && e.isSolid(); }

	public boolean canSwim() { return false; } // Determines if the entity can swim (extended in sub-classes)
	public boolean canWool() { return false; } // This, strangely enough, determines if the entity can walk on wool; among some other things..?
	public boolean canBurn() {
		return true;
	} // Determines if the entity can burn.
	public boolean canBeAffectedByLava() {
		return true;
	} // Determines if the entity can burn in lava.
	public int burningDuration = 0;

	public int getLightRadius() { return 0; } // Used for lanterns... and player? that might be about it, though, so idk if I want to put it here.


	/** If this entity is touched by another entity (extended by sub-classes) */
	protected void touchedBy(Entity entity) {}

	/**
	 * Interacts with the entity this method is called on
	 * @param player The player attacking
	 * @param item The item the player attacked with
	 * @param attackDir The direction to interact
	 * @return If the interaction was successful
	 */
	public boolean interact(Player player, @Nullable Item item, Direction attackDir) {
		return false;
	}

	/** Moves an entity horizontally and vertically. Returns whether entity was unimpeded in it's movement.  */
	public boolean move(int xd, int yd) {
		if (Updater.saving || (xd == 0 && yd == 0)) return true; // Pretend that it kept moving

		boolean stopped = true; // Used to check if the entity has BEEN stopped, COMPLETELY; below checks for a lack of collision.
		if (move2(xd, 0)) stopped = false; // Becomes false if horizontal movement was successful.
		if (move2(0, yd)) stopped = false; // Becomes false if vertical movement was successful.
		if (!stopped) {
			int xt = x >> 4; // The x tile coordinate that the entity is standing on.
			int yt = y >> 4; // The y tile coordinate that the entity is standing on.
			level.getTile(xt, yt).steppedOn(level, xt, yt, this); // Calls the steppedOn() method in a tile's class. (used for tiles like sand (footprints) or lava (burning))
		}

		return !stopped;
	}

	/**
	 * Moves the entity a long only one direction.
	 * If xd != 0 then ya should be 0.
	 * If xd = 0 then ya should be != 0.
	 * Will throw exception otherwise.
	 * @param xd Horizontal move.
	 * @param yd Vertical move.
	 * @return true if the move was successful, false if not.
	 */
	protected boolean move2(int xd, int yd) {
		if (xd == 0 && yd == 0) return true; // Was not stopped

		boolean interact = true;//!Game.isValidClient() || this instanceof ClientTickable;

		// Gets the tile coordinate of each direction from the sprite...
		int xto0 = ((x) - xr) >> 4; // To the left
		int yto0 = ((y) - yr) >> 4; // Above
		int xto1 = ((x) + xr) >> 4; // To the right
		int yto1 = ((y) + yr) >> 4; // Below

		// Gets same as above, but after movement.
		int xt0 = ((x + xd) - xr) >> 4;
		int yt0 = ((y + yd) - yr) >> 4;
		int xt1 = ((x + xd) + xr) >> 4;
		int yt1 = ((y + yd) + yr) >> 4;

		//boolean blocked = false; // If the next tile can block you.
		for (int yt = yt0; yt <= yt1; yt++) { // Cycles through y's of tile after movement
			for (int xt = xt0; xt <= xt1; xt++) { // Cycles through x's of tile after movement
				if (xt >= xto0 && xt <= xto1 && yt >= yto0 && yt <= yto1) continue; // Skip this position if this entity's sprite is touching it
				// Tile positions that make it here are the ones that the entity will be in, but are not in now.
				if (interact)
					level.getTile(xt, yt).bumpedInto(level, xt, yt, this); // Used in tiles like cactus
				if (!level.getTile(xt, yt).mayPass(level, xt, yt, this)) { // If the entity can't pass this tile...
					//blocked = true; // Then the entity is blocked
					return false;
				}
			}
		}

		// These lists are named as if the entity has already moved-- it hasn't, though.
		List<Entity> wasInside = level.getEntitiesInRect(getBounds()); // Gets all of the entities that are inside this entity (aka: colliding) before moving.

		int xr = this.xr, yr = this.yr;
		List<Entity> isInside = level.getEntitiesInRect(new Rectangle(x+xd, y+yd, xr*2, yr*2, Rectangle.CENTER_DIMS)); // Gets the entities that this entity will touch once moved.
		if (interact) {
			for (Entity e : isInside) {
				/// Cycles through entities about to be touched, and calls touchedBy(this) for each of them.
				if (e == this) continue; // Touching yourself doesn't count.

				if (e instanceof Player) {
					if (!(this instanceof Player))
						touchedBy(e);
				} else
					e.touchedBy(this); // Call the method. ("touch" the entity)
			}
		}

		isInside.removeAll(wasInside); // Remove all the entities that this one is already touching before moving.
		for (Entity e : isInside) {
			if (e == this) continue; // Can't interact with yourself
			if (e.blocks(this)) return false; // If the entity prevents this one from movement, don't move.
		}

		// Finally, the entity moves!
		x += xd;
		y += yd;

		return true; // the move was successful.
	}

	/** Checks if the entity is able to naturally be despawned in general conditions. Handles (despawns) if true. */
	public void handleDespawn() {}

	/** This exists as a way to signify that the entity has been removed through player action and/or world action; basically, it's actually gone, not just removed from a level because it's out of range or something. Calls to this method are used to, say, drop items. */
	public void die() { remove(); }

	/** Removes the entity from the level. */
	public void remove() {
		if (removed && !(this instanceof ItemEntity)) // Apparently this happens fairly often with item entities.
			Logging.ENTITY.debug("Note: remove() called on removed entity: " + this);

		removed = true;

		if (level == null)
			Logging.ENTITY.debug("Note: remove() called on entity with no level reference: " + getClass());
		else
			level.remove(this);
	}

	/** This should ONLY be called by the Level class. To properly remove an entity from a level, use level.remove(entity) */
	public void remove(Level level) {
		if (level != this.level) {
			Logging.ENTITY.debug("Tried to remove entity " + this + " from level it is not in: " + level + "; in level " + this.level);
		} else {
			removed = true; // Should already be set.
			this.level = null;
		}
	}

	/** This should ONLY be called by the Level class. To properly add an entity to a level, use level.add(entity) */
	public void setLevel(Level level, int x, int y) {
		if (level == null) {
			Logging.ENTITY.debug("Tried to set level of entity " + this + " to a null level; Should use remove(level)");
			return;
		}

		this.level = level;
		removed = false;
		this.x = x;
		this.y = y;

		if (eid < 0)
			eid = Network.generateUniqueEntityId();
	}

	public boolean isWithin(int tileRadius, Entity other) {
		if (level == null || other.getLevel() == null) return false;
		if (level.depth != other.getLevel().depth) return false; // Obviously, if they are on different levels, they can't be next to each other.

		double distance = Math.abs(Math.hypot(x - other.x, y - other.y)); // Calculate the distance between the two entities, in entity coordinates.

		return Math.round(distance) >> 4 <= tileRadius; // Compare the distance (converted to tile units) with the specified radius.
	}

	/**
	 * Returns the closest player to this entity.
	 * @return the closest player.
	 */
	protected Player getClosestPlayer() {
		return getClosestPlayer(true);
	}

	/**
	 * Returns the closes player to this entity.
	 * If this is called on a player it can return itself.
	 * @param returnSelf determines if the method can return itself.
	 * @return The closest player to this entity.
	 */
	protected Player getClosestPlayer(boolean returnSelf) {
		if (this instanceof Player && returnSelf)
			return (Player) this;

		if (level == null) return null;

		return level.getClosestPlayer(x, y);
	}

	public String toString() { return getClass().getSimpleName() + getDataPrints(); }
	protected List<String> getDataPrints() {
		List<String> prints = new ArrayList<>();
		prints.add("eid=" + eid);
		return prints;
	}

	@Override
	public final boolean equals(Object other) {
		return other instanceof Entity && hashCode() == other.hashCode();
	}

	@Override
	public final int hashCode() { return eid; }
}
