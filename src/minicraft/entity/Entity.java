package minicraft.entity;

import java.util.List;
import java.util.Random;

import minicraft.Game;
import minicraft.gfx.Screen;
import minicraft.item.Item;
import minicraft.level.Level;
import minicraft.level.tile.Tile;

public abstract class Entity {
	/// entity coordinates are per pixel, not per tile; each tile is 16x16 entity pixels.
	protected final Random random = new Random();
	public int x, y; // x, y entity coordinates on the map
	public int xr, yr; // x, y radius of entity
	private boolean removed; // Determines if the entity is removed from it's level; checked in Level.java
	protected Level level; // the level that the entity is on
	public int col; // current color.
	
	public int eid; /// this is intended for multiplayer, but I think it could be helpful in single player, too. certainly won't harm anything, I think... as long as finding a valid id doesn't take long...
	private String prevUpdates = ""; /// holds the last value returned from getUpdateString(), for comparison with the next call.
	private String curDeltas = ""; /// holds the updates returned from the last time getUpdates() was called.
	private boolean accessedUpdates = false;
	public long lastUpdate;
	
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
	
	/**
	 * Draws the entity to a screen.
	 * @param screen The screen which the entity should be drawn to.
	 */
	public abstract void render(Screen screen);
	
	/**
	 * Updates the entity.
	 */
	public abstract void tick();
	
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
	
	/**
	 * Removes the entity from the level.
	 */
	public void remove() {
		if(removed && !(this instanceof ItemEntity)) // apparently this happens fairly often with item entities.
			System.out.println("Note: remove() called on removed entity: " + this);
		
		removed = true;
		
		if(level == null)
			System.out.println("Note: remove() called on entity with no level reference: " + getClass());
		else
			level.remove(this);
	}
	
	/**
	 * Called when the level removes an entity from itself.
	 * @param level The level removing this entity.
	 */
	public void remove(Level level) {
		if(level != this.level && Game.debug)
			System.out.println("tried to remove entity "+this+" from level it is not in: " + level + "; in level " + this.level);
		else {
			removed = true; // should already be set.
			this.level = null;
			//if (Game.debug && !(this instanceof Particle)) System.out.println(Game.onlinePrefix()+"set level reference of entity " + this + " to null.");
		}
	}
	
	/**
	 * This should ONLY be called by the Level class. To properly add an entity to a level, use level.add(entity) 
	 * @param level New level for entity.
	 * @param x Entity x position.
	 * @param y Entity y position.
	 */
	public void setLevel(Level level, int x, int y) {
		if(level == null) {
			System.out.println("tried to set level of entity " + this + " to a null level; should use remove(level)");
			return;
		} else if(level != this.level && Game.isValidServer()) {
			Game.server.broadcastEntityRemoval(this);
		}
		
		this.level = level;
		removed = false;
		this.x = x;
		this.y = y;
		
		if(eid < 0)
			eid = Game.generateUniqueEntityId();
	}
	
	/**
	 * Checks if this entity is inside a rectangle given by two coordinates.
	 * @param x0 First x coordinate.
	 * @param y0 First y coordinate.
	 * @param x1 Second x coordinate.
	 * @param y1 Second y coordinate.
	 * @return true if the entity is inside the rectangle, false otherwise.
	 */
	public boolean intersects(int x0, int y0, int x1, int y1) {
		// x0,y0 = upper-left corner of rect; x1,y1 = bottom-right corner of rect
		return !(x + xr < x0 || y + yr < y0 || x - xr > x1 || y - yr > y1);
	}
	
	/**
	 * Determines if this entity should prevent another entity from moving inside it.
	 * Extended in Mob.java & Furniture.java
	 * @param e The other entity.
	 * @return false
	 */
	public boolean blocks(Entity e) {
		return false;
	}
		
	/**
	 * Moves an entity horizontally and vertically. Returns whether entity was unimpeded in it's movement.
	 * @param xa Horizontal velocity.
	 * @param ya Vertical velocity.
	 * @return only returns true if the game is saving and at least one direction has zero velocity, or 
	 * if the entity was able to move unobstructed in both directions.
	 */
	public boolean move(int xa, int ya) {
		if (!Game.saving && (xa != 0 || ya != 0)) { // if not saving, and the entity is actually going to move...
			boolean stopped = true; // used to check if the entity has BEEN stopped, COMPLETELY; below checks for a lack of collision.
			if (xa != 0 && move2(xa, 0)) stopped = false; // horizontal movement was successful.
			if (ya != 0 && move2(0, ya)) stopped = false; // vertical movement was successful.
			if (!stopped) {
				
				/* I guess I should explain something real quick. The coordinates between tiles and entities are different.
				 * The world coordinates for tiles is 128x128
				 * The world coordinates for entities is 2048x2048
				 * This is because each tile is 16x16 pixels big
				 * 128 x 16 = 2048.
				 * When ever you see a ">>", it means that it is a right shift operator. This means it shifts bits to the right (making them smaller)
				 * x >> 4 is the equivalent to x / (2^4). Which means it's dividing the X value by 16. (2x2x2x2 = 16)
				 * xt << 4 is the equivalent to xt * (2^4). Which means it's multiplying the X tile value by 16.
				 *
				 * These bit shift operators are used to easily get the X & Y coordinates of a tile that the entity is standing on. */
				
				int xt = x >> 4; // the x tile coordinate that the entity is standing on.
				int yt = y >> 4; // the y tile coordinate that the entity is standing on.
				level.getTile(xt, yt).steppedOn(level, xt, yt, this); // Calls the steppedOn() method in a tile's class. (used for tiles like sand (footprints) or lava (burning))
			}
			return !stopped;
		}
		return true; // reaches this if no movement was requested / game was saving. return true, becuase MobAis should still do the moving phase thing, just paused, not obstructed.
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
		if (xa != 0 && ya != 0)
			throw new IllegalArgumentException("Move2 can only move along one axis at a time!");
		
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
				level.getTile(xt, yt).bumpedInto(level, xt, yt, this); // Used in tiles like cactus
				if (!level.getTile(xt, yt).mayPass(level, xt, yt, this)) { // if the entity can't pass this tile...
					//blocked = true; // then the entity is blocked
					return false;
				}
			}
		}
		
		// these lists are named as if the entity has already moved-- it hasn't, though.
		List<Entity> wasInside = level.getEntitiesInRect(x - xr, y - yr, x + xr, y + yr); // gets all of the entities that are inside this entity (aka: colliding) before moving.
		
		int xr = this.xr, yr = this.yr;
		if(Game.isValidClient() && this instanceof Player) {
			xr++;
			yr++;
		}
		List<Entity> isInside = level.getEntitiesInRect(x + xa - xr, y + ya - yr, x + xa + xr, y + ya + yr); // gets the entities that this entity will touch once moved.
		for (int i = 0; i < isInside.size(); i++) {
			/// cycles through entities about to be touched, and calls touchedBy(this) for each of them.
			Entity e = isInside.get(i);
			if (e == this) continue; // touching yourself doesn't count.
			
			e.touchedBy(this); // call the method. ("touch" the entity)
			
			//if(Game.debug && e != this && (e instanceof Player || this instanceof Player)) System.out.println("entity " + this.toClassString() + " is moving inside furniture " + e.toClassString());
		}
		
		/*if(Game.debug) {
			for(Entity e: wasInside) {
				if(e != this && (e instanceof Player || this instanceof Player))
					System.out.println(Game.onlinePrefix()+"entity " + this.toClassString() + " is moving from inside " + e.toClassString());
			}
		}*/
		
		isInside.removeAll(wasInside); // remove all the entites that this one is already touching before moving.
		for (int i = 0; i < isInside.size(); i++) {
			Entity e = isInside.get(i);
			
			//if(Game.debug && e != this && (e instanceof Player || this instanceof Player)) System.out.println(Game.onlinePrefix()+"entity " + this.toClassString() + " is moving to be inside " + e.toClassString());
			
			if (e == this) continue;
			
			if (e.blocks(this)) {
				//if (Game.debug && (e instanceof Player || this instanceof Player)) System.out.println(Game.onlinePrefix()+"entity " + this.toClassString() + " was blocked by entity " + e.toClassString());
				return false; // if the entity can block this entity, then this can't move.
			}
		}
		
		// finally, the entity moves!
		x += xa;
		y += ya;
		
		//if (Game.debug && !(this instanceof MobAi)) System.out.println(Game.onlinePrefix()+"entity " + this.toClassString() + " allowed to move by ("+xa+","+ya+"), to: ("+x+","+y+")");
		
		return true; // the move was successful.
	}
	
	/**
	 * This is called whenever this entity is touched by another entity.
	 * @param entity The entity touching this entity.
	 */
	protected void touchedBy(Entity entity) {}
	
	/**
	 * Checks if the mob can block this entity's movement.
	 * @param mob The mob.
	 * @return true if the mob blocks movement, false if not.
	 */
	public boolean isBlockableBy(Mob mob) {
		return true; // yes, mobs generally block other entities.
	}
	
	/**
	 * This is called when a entity touches an ItemEntity.
	 * Used by the Player class to make the player pick up an item.
	 * @param itemEntity The ItemEntity touching this entity.
	 */
	public void touchItem(ItemEntity itemEntity) {}
	
	/**
	 * Determines if the entity can swim.
	 * @return false by default, true if the entity can swim.
	 */
	public boolean canSwim() {
		return false;
	}
	
	/**
	 * Strange behavior ahead! This determines if this entity can walk on wool.
	 * And also does some other things? Consider changing this methods name.
	 * @return false by default.
	 */
	public boolean canWool() { return false; }
	
	/**
	 * Marked as deprecated because its only used by the player and its not really 
	 * possible to tell what it does. The player always returns true.
	 * 
	 * Determines if this entity can light up?
	 * @return false by default.
	 */
	@Deprecated
	public boolean canLight() {
		return false;
	}
	
	/**
	 * Only used by the Player class to interact with this entity.
	 * It's called when the player uses an item on this entity.
	 * @param player The player using an item.
	 * @param item The item the player is using.
	 * @param attackDir Which direction the player is attacking from.
	 * @return the result of the item.interact() method.
	 */
	public boolean interact(Player player, Item item, int attackDir) {
		return item.interact(player, this, attackDir);
	}
	
	/**
	 * Called when the player interacts with this entity.
	 * Used by some of the furniture classes.
	 * @param player The player trying to use this entity.
	 * @param attackDir Which direction the player is using it from.
	 * @return false by default, but true if this entity accepts the player's interaction.
	 */
	public boolean use(Player player, int attackDir) {
		// this may not be necessary for all entities.
		return false;
	}
	
	/**
	 * Returns how far the entity can see underground.
	 * @return integer saying how far (in tiles) the entity can see.
	 */
	public int getLightRadius() {
		return 0;
	}
	
	/**
	 * Damages this entity when a mob is the damage source.
	 * @param mob Mob which is damaging this entity.
	 * @param dmg How much damage is done.
	 * @param attackDir From which direction.
	 */
	public void hurt(Mob mob, int dmg, int attackDir) {}
	
	/**
	 * Damages this entity when tnt is damage the source. 
	 * @param tnt The tnt doing the damage.
	 * @param dmg How much damage is done.
	 * @param attackDir From which direction.
	 */
	public void hurt(Tnt tnt, int dmg, int attackDir) {}
	
	/**
	 * Damages this entity when a tile is the damage source.
	 * @param tile The tile doing damage.
	 * @param x Not sure if this is even used?
	 * @param y Not sure if this is even used?
	 * @param dmg How much damage is done.
	 */
	public void hurt(Tile tile, int x, int y, int dmg) {}
	
	/**
	 * Determines if another entity is within a given radius of this entity.
	 * @param tileRadius How far apart the other entity can be.
	 * @param other The other entity.
	 * @return true if the entities are within the given radius, false if not.
	 */
	public boolean isWithin(int tileRadius, Entity other) {
		if(level == null || other.getLevel() == null) return false;
		if(level.depth != other.getLevel().depth) return false; // obviously, if they are on different levels, they can't be next to each other.}
		
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
				Level newLvl = Game.levels[Integer.parseInt(val)];
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
		+"level,"+(level==null?"null":Game.lvlIdx(level.depth));
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
		
		/// if we did have updates last time, then save them as an array, before overriting the update field for next time.
		String[] curUpdates = updates.split(";");
		String[] prevUpdates = this.prevUpdates.split(";");
		this.prevUpdates = updates;
		
		/// now, we have the current values, and the previous values, as arrays of key-value pairs sep. by commas. Now, the goal is to seperate which are actually *updates*, meaning they are different from last time.
		
		StringBuilder deltas = new StringBuilder();
		for(int i = 0; i < curUpdates.length; i++) { // b/c the string always contains the same number of pairs (and the same keys, in the same order), the indexes of cur and prev updates will be the same.
			/// loop though each of the updates this call. If it is differnt from the last one, then add it to the list.
			if(!curUpdates[i].equals(prevUpdates[i])) {
				deltas.append(curUpdates[i]).append(";");
				//if(Game.debug) System.out.println("found delta for "+this+"; old:\""+prevUpdates[i]+"\" -- new:\""+curUpdates[i]+"\"");
			}
		}
		
		curDeltas = deltas.toString();
		
		if(curDeltas.length() > 0) curDeltas = curDeltas.substring(0, curDeltas.length()-1); // cuts off extra ";"
		
		return curDeltas;
	}
	
	/**
	 * Marks the entity so a new state will be fetched from the server.
	 */
	public void flushUpdates() {
		accessedUpdates = false;
	}
	
	/**
	 * Returns a string representation of the entity. (Which can't be sent to a server/client)
	 */
	@Override
	public String toString() {
		//String superName = super.toString();
		//superName = superName.substring(superName.lastIndexOf(".")+1);
		return toClassString() + "(eid="+eid+")";
	}
	
	/**
	 * Returns the name of the class which this entity belongs to.
	 * @return Name of the entity's class.
	 */
	public String toClassString() {
		String clazz = getClass().getName();
		return clazz.substring(clazz.lastIndexOf(".")+1);
	}
}
