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
	// TODO might replace the below with a simple array of colors.
	public int col; // day/night color variations, plus current color.
	
	public int eid; /// this is intended for multiplayer, but I think it could be helpful in single player, too. certainly won't harm anything, I think... as long as finding a valid id doesn't take long...
	private String prevUpdates = ""; /// holds the last value returned from getUpdateString(), for comparison with the next call.
	private String curDeltas = ""; /// holds the updates returned from the last time getUpdates() was called.
	private boolean accessedUpdates = false;
	
	public Entity(int xr, int yr) { // add color to this later, in color update
		this.xr = xr;
		this.yr = yr;
		
		level = null;
		removed = true;
		col = 0;
		
		eid = -1;
	}
	
	public abstract void render(Screen screen); /// used to render the entity on screen.
	public abstract void tick(); /// used to update the entity.
	
	public boolean isRemoved() { return removed; }
	public Level getLevel() { return level; }
	
	/** Removes the entity from the level. */
	public void remove() {
		if(removed && !(this instanceof ItemEntity)) // apparently this happens fairly often with item entities.
			System.out.println("Note: remove() called on removed entity: " + getClass());
		
		removed = true;
		
		if(level == null)
			System.out.println("Note: remove() called on entity with no level reference: " + getClass());
		else
			level.remove(this);
		//if(Game.isValidClient() && !Game.isValidServer() && !(this instanceof minicraft.entity.particle.Particle))
			//System.out.println("WARNING: client game is removing "+getClass().getName().replace("minicraft.entity.","")+" entity from level " + (level==null?"null":level.depth));
	}
	public void remove(Level level) {
		if(level != this.level && Game.debug)
			System.out.println("tried to remove entity "+this+" from level it is not in: " + level + "; in level " + this.level);
		else {
			removed = true;
			level = null;
			//if (Game.debug && !(this instanceof Particle)) System.out.println(Game.onlinePrefix()+"set level reference of entity " + this + " to null.");
		}
	}
	
	/** This should ONLY be called by the Level class. To properly add an entity to a level, use level.add(entity) */
	public void setLevel(Level level, int x, int y) {
		if(level == null) {
			System.out.println("tried to set level of entity " + this + " to a null level; should use remove(level)");
			return;
		}
		this.level = level;
		removed = false;
		this.x = x;
		this.y = y;
		
		if(eid < 0)
			eid = Game.generateUniqueEntityId();
	}
	
	// TODO Inplement this! it's a really good idea!
	//public abstract void getColor();
	
	/** returns true if this entity is found in the rectangle specified by given two coordinates. */
	public boolean intersects(int x0, int y0, int x1, int y1) {
		// x0,y0 = upper-left corner of rect; x1,y1 = bottom-right corner of rect
		return !(x + xr < x0 || y + yr < y0 || x - xr > x1 || y - yr > y1);
	}
	
	/** Extended in Mob.java & Furniture.java */
	public boolean blocks(Entity e) {
		return false;
	}
		
	/** Moves an entity horizontally and vertically. Returns whether entity was unimpeded in it's movement.  */
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
	
	/** Second part to the move method (moves in one direction at a time) */
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
			/// cycles through entites about to be touched, and calls touchedBy(this) for each of them.
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
	
	/** if this entity is touched by another entity (extended by sub-classes) */
	protected void touchedBy(Entity entity) {}
	
	/** returns if mobs can block this entity (aka: can't pass through them) */
	public boolean isBlockableBy(Mob mob) {
		return true; // yes, mobs generally block other entities.
	}
	
	/** Used in ItenEntity.java, extended with Player.java */
	public void touchItem(ItemEntity itemEntity) {}
	
	/** Determines if the entity can swim (extended in sub-classes) */
	public boolean canSwim() {
		return false;
	}
	
	/** This, strangely enough, determines if the entity can walk on wool; among some other things..? */
	public boolean canWool() {
		// TODO I personally think this should be removed, and replaced in the wool mayPass mothod. Speaking of the Wool classes, I see no reason why they are more than one class...
		return false;
	}
	
	/** If the entity can light up..? */
	public boolean canLight() {
		return false;
	}
	
	/** Item interact, used in player.java */
	public boolean interact(Player player, Item item, int attackDir) {
		return item.interact(player, this, attackDir);
	}
	
	/** sees if the player has used an item in a direction (extended in player.java) */
	public boolean use(Player player, int attackDir) {
		// this may not be necessary for all entities.
		return false;
	}
	
	public int getLightRadius() {
		return 0;
	}
	
	/** Extended in Mob.java */
	public void hurt(Mob mob, int dmg, int attackDir) {}
	public void hurt(Tnt tnt, int dmg, int attackDir) {}
	public void hurt(Tile tile, int x, int y, int dmg) {}
	
	public boolean isWithin(int tileRadius, Entity other) {
		if(level == null || other.getLevel() == null) return false;
		if(level.depth != other.getLevel().depth) return false; // obviously, if they are on different levels, they can't be next to each other.
		
		double distance = Math.abs(Math.hypot(x - other.x, y - other.y)); // calculate the distance between the two entities, in entity coordinates.
		
		return Math.round(distance) >> 4 <= tileRadius; // compare the distance (converted to tile units) with the specified radius.
	}
	
	protected Player getClosestPlayer() { return getClosestPlayer(true); }
	protected Player getClosestPlayer(boolean returnSelf) {
		if (this instanceof Player && returnSelf)
			return (Player) this;
		
		if (level == null) return null;
		
		return level.getClosestPlayer(x, y);
	}
	
	public final void update(String deltas) {
		for(String field: deltas.split(";")) {
			String fieldName = field.substring(0, field.indexOf(","));
			String val = field.substring(field.indexOf(",")+1);
			updateField(fieldName, val);
		}
	}
	
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
					if(level != null) level.remove(this);
					if(newLvl != null) newLvl.add(this);
				}
				return true;
		}
		return false;
	}
	
	/// I think I'll make these "getUpdates()" methods be an established thing, that returns all the things that can change that you need to account for when updating entities across a server.
	/// by extension, the update() method should always account for all the variables specified here.
	protected String getUpdateString() {
		return "x,"+x+";"
		+"y,"+y+";"
		+"level,"+(level==null?"null":Game.lvlIdx(level.depth));
	}
	
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
		
		String deltas = "";
		for(int i = 0; i < curUpdates.length; i++) { // b/c the string always contains the same number of pairs (and the same keys, in the same order), the indexes of cur and prev updates will be the same.
			/// loop though each of the updates this call. If it is differnt from the last one, then add it to the list.
			if(curUpdates[i].equals(prevUpdates[i]) == false) {
				deltas += curUpdates[i] + ";";
				//if(Game.debug) System.out.println("found delta for "+this+"; old:\""+prevUpdates[i]+"\" -- new:\""+curUpdates[i]+"\"");
			}
		}
		
		if(deltas.length() > 0) deltas = deltas.substring(0, deltas.length()-1); // cuts off extra ";"
		
		curDeltas = deltas;
		return deltas;
	}
	
	/// this marks the entity as having a new state to fetch.
	public void flushUpdates() {
		accessedUpdates = false;
	}
	
	public String toString() {
		//String superName = super.toString();
		//superName = superName.substring(superName.lastIndexOf(".")+1);
		return toClassString() + "(eid="+eid+")";
	}
	
	public String toClassString() {
		String clazz = getClass().getName();
		return clazz.substring(clazz.lastIndexOf(".")+1);
	}
}
