package minicraft.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import minicraft.core.Game;
import minicraft.core.Network;
import minicraft.core.Updater;
import minicraft.core.World;
import minicraft.entity.mob.MobAi;
import minicraft.entity.mob.Player;
import minicraft.gfx.Rectangle;
import minicraft.gfx.Screen;
import minicraft.item.Item;
import minicraft.level.Level;

import org.jetbrains.annotations.Nullable;

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
	
	// entity coordinates are per pixel, not per tile; each tile is 16x16 entity pixels.
	protected final Random random = new Random();
	public int x, y; // x, y entity coordinates on the map
	private int xr, yr; // x, y radius of entity
	private boolean removed; // Determines if the entity is removed from it's level; checked in Level.java
	protected Level level; // the level that the entity is on
	public int col; // current color.
	
	public int eid; // this is intended for multiplayer, but I think it could be helpful in single player, too. certainly won't harm anything, I think... as long as finding a valid id doesn't take long...
	private String prevUpdates = ""; // holds the last value returned from getUpdateString(), for comparison with the next call.
	private String curDeltas = ""; // holds the updates returned from the last time getUpdates() was called.
	private boolean accessedUpdates = false;
	private long lastUpdate;
	
	/**
	 * Default constructor for the Entity class.
	 * Assings null/none values to the instace variables.
	 * The exception is removed which is set to true, and
	 * lastUpdate which is set to System.nanoTime().
	 * @param xr X radius of entity.
	 * @param yr Y radius of entity.
	 */
	public Entity(int xr, int yr) { // add color to this later, in color update
		this.xr = xr;
		this.yr = yr;
		
		level = null;
		removed = true;
		col = 0;
		
		eid = -1;
		lastUpdate = System.nanoTime();
	}
	
	public abstract void render(Screen screen); /// used to render the entity on screen.
	@Override public abstract void tick(); /// used to update the entity.
	
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
	protected Rectangle getBounds() { return new Rectangle(x, y, xr*2, yr*2, Rectangle.CENTER_DIMS); }
	/** returns true if this entity is found in the rectangle specified by given two coordinates. */
	public boolean isTouching(Rectangle area) { return area.intersects(getBounds()); }
	/** returns if this entity stops other solid entities from moving. */
	public boolean isSolid() { return true; } // most entities are solid
	/** Determines if the given entity should prevent this entity from moving. */
	public boolean blocks(Entity e) { return isSolid() && e.isSolid(); }
	
	public boolean canSwim() { return false; } // Determines if the entity can swim (extended in sub-classes)
	public boolean canWool() { return false; } // This, strangely enough, determines if the entity can walk on wool; among some other things..?
	
	public int getLightRadius() { return 0; } // used for lanterns... and player? that might be about it, though, so idk if I want to put it here.
	
	
	/** if this entity is touched by another entity (extended by sub-classes) */
	protected void touchedBy(Entity entity) {}
	
	/** Item interact */
	public boolean interact(Player player, @Nullable Item item, Direction attackDir) {
		if(item != null)
			return item.interact(player, this, attackDir);
		return false;
	}
	
	/** Moves an entity horizontally and vertically. Returns whether entity was unimpeded in it's movement.  */
	public boolean move(int xa, int ya) {
		if(Updater.saving || (xa == 0 && ya == 0)) return true; // pretend that it kept moving
		
		boolean stopped = true; // used to check if the entity has BEEN stopped, COMPLETELY; below checks for a lack of collision.
		if(move2(xa, 0)) stopped = false; // becomes false if horizontal movement was successful.
		if(move2(0, ya)) stopped = false; // becomes false if vertical movement was successful.
		if (!stopped) {
			int xt = x >> 4; // the x tile coordinate that the entity is standing on.
			int yt = y >> 4; // the y tile coordinate that the entity is standing on.
			level.getTile(xt, yt).steppedOn(level, xt, yt, this); // Calls the steppedOn() method in a tile's class. (used for tiles like sand (footprints) or lava (burning))
		}
		
		return !stopped;
	}
	
	/**
	 * Moves the entity a long only one direction.
	 * If xa != 0 then ya should be 0.
	 * If xa = 0 then ya should be != 0.
	 * Will throw exception otherwise.
	 * @param xa Horizontal velocity.
	 * @param ya Vertical velocity.
	 * @return true if the move was successful, false if not.
	 */
	protected boolean move2(int xa, int ya) {
		if(xa == 0 && ya == 0) return true; // was not stopped
		
		boolean interact = true;//!Game.isValidClient() || this instanceof ClientTickable;
		
		// gets the tile coordinate of each direction from the sprite...
		int xto0 = ((x) - xr) >> 4; // to the left
		int yto0 = ((y) - yr) >> 4; // above
		int xto1 = ((x) + xr) >> 4; // to the right
		int yto1 = ((y) + yr) >> 4; // below
		
		// gets same as above, but after movement.
		int xt0 = ((x + xa) - xr) >> 4;
		int yt0 = ((y + ya) - yr) >> 4;
		int xt1 = ((x + xa) + xr) >> 4;
		int yt1 = ((y + ya) + yr) >> 4;
		
		//boolean blocked = false; // if the next tile can block you.
		for (int yt = yt0; yt <= yt1; yt++) { // cycles through y's of tile after movement
			for (int xt = xt0; xt <= xt1; xt++) { // cycles through x's of tile after movement
				if (xt >= xto0 && xt <= xto1 && yt >= yto0 && yt <= yto1) continue; // skip this position if this entity's sprite is touching it
				// tile positions that make it here are the ones that the entity will be in, but are not in now.
				if(interact)
					level.getTile(xt, yt).bumpedInto(level, xt, yt, this); // Used in tiles like cactus
				if (!level.getTile(xt, yt).mayPass(level, xt, yt, this)) { // if the entity can't pass this tile...
					//blocked = true; // then the entity is blocked
					return false;
				}
			}
		}
		
		// these lists are named as if the entity has already moved-- it hasn't, though.
		List<Entity> wasInside = level.getEntitiesInRect(getBounds()); // gets all of the entities that are inside this entity (aka: colliding) before moving.
		
		int xr = this.xr, yr = this.yr;
		if(Game.isValidClient() && this instanceof Player) {
			xr++;
			yr++;
		}
		List<Entity> isInside = level.getEntitiesInRect(new Rectangle(x+xa, y+ya, xr*2, yr*2, Rectangle.CENTER_DIMS)); // gets the entities that this entity will touch once moved.
		for (int i = 0; interact && i < isInside.size(); i++) {
			/// cycles through entities about to be touched, and calls touchedBy(this) for each of them.
			Entity e = isInside.get(i);
			if (e == this) continue; // touching yourself doesn't count.
			
			if(e instanceof Player) {
				if(!(this instanceof Player))
					touchedBy(e);
			}
			else
				e.touchedBy(this); // call the method. ("touch" the entity)
		}
		
		isInside.removeAll(wasInside); // remove all the entities that this one is already touching before moving.
		for (int i = 0; i < isInside.size(); i++) {
			Entity e = isInside.get(i);
			
			if (e == this) continue; // can't interact with yourself
			
			if (e.blocks(this)) return false; // if the entity prevents this one from movement, don't move.
		}
		
		// finally, the entity moves!
		x += xa;
		y += ya;
		
		return true; // the move was successful.
	}

	/** This exists as a way to signify that the entity has been removed through player action and/or world action; basically, it's actually gone, not just removed from a level because it's out of range or something. Calls to this method are used to, say, drop items. */
	public void die() { remove(); }
	
	/** Removes the entity from the level. */
	public void remove() {
		if(removed && !(this instanceof ItemEntity)) // apparently this happens fairly often with item entities.
			System.out.println("Note: remove() called on removed entity: " + this);
		
		removed = true;
		
		if(level == null)
			System.out.println("Note: remove() called on entity with no level reference: " + getClass());
		else
			level.remove(this);
	}
	
	/** This should ONLY be called by the Level class. To properly remove an entity from a level, use level.remove(entity) */
	public void remove(Level level) {
		if(level != this.level) {
			if(Game.debug) System.out.println("Tried to remove entity " + this + " from level it is not in: " + level + "; in level " + this.level);
		} else {
			removed = true; // should already be set.
			this.level = null;
		}
	}
	
	/** This should ONLY be called by the Level class. To properly add an entity to a level, use level.add(entity) */
	public void setLevel(Level level, int x, int y) {
		if(level == null) {
			System.out.println("Tried to set level of entity " + this + " to a null level; Should use remove(level)");
			return;
		} else if(level != this.level && Game.isValidServer() && this.level != null) {
			Game.server.broadcastEntityRemoval(this, this.level, !(this instanceof Player));
		}
		
		this.level = level;
		removed = false;
		this.x = x;
		this.y = y;
		
		if(eid < 0)
			eid = Network.generateUniqueEntityId();
	}
	
	public boolean isWithin(int tileRadius, Entity other) {
		if(level == null || other.getLevel() == null) return false;
		if(level.depth != other.getLevel().depth) return false; // obviously, if they are on different levels, they can't be next to each other.
		
		double distance = Math.abs(Math.hypot(x - other.x, y - other.y)); // calculate the distance between the two entities, in entity coordinates.
		
		return Math.round(distance) >> 4 <= tileRadius; // compare the distance (converted to tile units) with the specified radius.
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
	
	/**
	 * I think this is used to update a entity over a network.
	 * The server will send a correction of this entity's state
	 * which will then be updated.
	 * @param deltas A string representation of the new entity state.
	 */
	public final void update(String deltas) {
		for(String field: deltas.split(";")) {
			String fieldName = field.substring(0, field.indexOf(","));
			String val = field.substring(field.indexOf(",")+1);
			updateField(fieldName, val);
		}
		
		if(Game.isValidClient() && this instanceof MobAi) {
			lastUpdate = System.nanoTime();
		}
	}
	
	/**
	 * Updates one of the entity's fields based on a string pair.
	 * Used to parse data from a server.
	 * @param fieldName Which variable is being updated.
	 * @param val The new value.
	 * @return true if a variable was updated, false if not.
	 */
	protected boolean updateField(String fieldName, String val) {
		switch(fieldName) {
			case "eid": eid = Integer.parseInt(val); return true;
			case "x": x = Integer.parseInt(val); return true;
			case "y": y = Integer.parseInt(val); return true;
			case "level":
				if(val.equals("null")) return true; // this means no level.
				Level newLvl = World.levels[Integer.parseInt(val)];
				if(newLvl != null && level != null) {
					if(newLvl.depth == level.depth) return true;
					level.remove(this);
					newLvl.add(this);
				}
				return true;
		}
		return false;
	}
	
	/// I think I'll make these "getUpdates()" methods be an established thing, that returns all the things that can change that you need to account for when updating entities across a server.
	/// by extension, the update() method should always account for all the variables specified here.
	/**
	 * Converts this entity to a string representation which can be sent to
	 * a server or client.
	 * @return Networking string representation of this entity.
	 */
	protected String getUpdateString() {
		return "x,"+x+";"
		+"y,"+y+";"
		+"level,"+(level==null?"null":World.lvlIdx(level.depth));
	}
	
	/**
	 * Returns a string representation of this entity.
	 * @param fetchAll true if all variables should be returned, false if only the ones who have changed should be returned.
	 * @return Networking string representation of this entity.
	 */
	public final String getUpdates(boolean fetchAll) {
		if(accessedUpdates) {
			if(fetchAll) return prevUpdates;
			else return curDeltas;
		}
		else {
			if(fetchAll) return getUpdateString();
			else return getUpdates();
		}
	}
	
	/**
	 * Determines what has been updated and only return that.
	 * @return String representation of all the variables which has changed since last time.
	 */
	public final String getUpdates() {
		// if the updates have already been fetched and written, but not flushed, then just return those.
		if(accessedUpdates) return curDeltas;
		else accessedUpdates = true; // after this they count as accessed.
		
		/// first, get the current string of values, which includes any subclasses.
		String updates = getUpdateString();
		
		if(prevUpdates.length() == 0) {
			// if there were no values saved last call, our job is easy. But this is only the case the first time this is run.
			prevUpdates = curDeltas = updates; // set the update field for next time
			return updates; // and we're done!
		}
		
		/// if we did have updates last time, then save them as an array, before overwriting the update field for next time.
		String[] curUpdates = updates.split(";");
		String[] prevUpdates = this.prevUpdates.split(";");
		this.prevUpdates = updates;
		
		/// now, we have the current values, and the previous values, as arrays of key-value pairs sep. by commas. Now, the goal is to separate which are actually *updates*, meaning they are different from last time.
		
		StringBuilder deltas = new StringBuilder();
		for(int i = 0; i < curUpdates.length; i++) { // b/c the string always contains the same number of pairs (and the same keys, in the same order), the indexes of cur and prev updates will be the same.
			/// loop though each of the updates this call. If it is different from the last one, then add it to the list.
			if(!curUpdates[i].equals(prevUpdates[i])) {
				deltas.append(curUpdates[i]).append(";");
			}
		}
		
		curDeltas = deltas.toString();
		
		if(curDeltas.length() > 0) curDeltas = curDeltas.substring(0, curDeltas.length()-1); // cuts off extra ";"
		
		return curDeltas;
	}
	
	/// this marks the entity as having a new state to fetch.
	public void flushUpdates() { accessedUpdates = false; }
	
	public String toString() { return getClass().getSimpleName() + getDataPrints(); }
	protected List<String> getDataPrints() {
		List<String> prints = new ArrayList<>();
		prints.add("eid="+eid);
		return prints;
	}
	
	@Override
	public final boolean equals(Object other) {
		return other instanceof Entity && hashCode() == other.hashCode();
	}
	
	@Override
	public final int hashCode() { return eid; }
}
