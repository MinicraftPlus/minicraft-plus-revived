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
	public boolean removed; // Determines if the entity is removed from it's level; checked in Level.java
	public Level level; // the level that the entity is on
	// TODO might replace the below with a simple array of colors.
	public int col; // day/night color variations, plus current color.
	
	public Entity(int xr, int yr) { // add color to this later, in color update
		this.xr = xr;
		this.yr = yr;
		
		level = null;
		removed = true;
		col = 0;
	}
	
	public abstract void render(Screen screen); /// used to render the entity on screen.
	public abstract void tick(); /// used to update the entity.
	
	/** Removes the entity from the level. */
	public void remove() {
		removed = true;
	}
	
	public void setLevel(Level level, int x, int y) {
		this.level = level;
		if(level != null)
			removed = false;
		this.x = x;
		this.y = y;
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
		
	/** Moves an entity horizontally and vertically. */
	public boolean move(int xa, int ya) {
		if (!Game.saving && (xa != 0 || ya != 0)) { // if not saving, and the entity is actually going to move...
			boolean stopped = true; // used to check if the entity has BEEN stopped, COMPLETELY; this checks for a lack of collision.
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
		return true; // reaches this if no movement was requested / game was saving.
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
		List<Entity> wasInside = level.getEntities(x - xr, y - yr, x + xr, y + yr); // gets all of the entities that are inside this entity (aka: colliding) before moving.
		List<Entity> isInside = level.getEntities(x + xa - xr, y + ya - yr, x + xa + xr, y + ya + yr); // gets the entities that this entity will touch once moved.
		for (int i = 0; i < isInside.size(); i++) {
			/// cycles through entites about to be touched, and calls touchedBy(this) for each of them.
			Entity e = isInside.get(i);
			if (e == this) continue; // touching yourself doesn't count.
			
			e.touchedBy(this); // call the method. ("touch" the entity)
		}
		isInside.removeAll(wasInside); // remove all the entites that this one is already touching before move.
		for (int i = 0; i < isInside.size(); i++) {
			Entity e = isInside.get(i);
			if (e == this) continue;

			if (e.blocks(this)) return false; // if the entity can block this entity, then this can't move.
		}
		
		// finally, the entity moves!
		x += xa;
		y += ya;
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
	
	protected Player getClosestPlayer() { return getClosestPlayer(true); }
	protected Player getClosestPlayer(boolean returnSelf) {
		if (this instanceof Player && returnSelf)
			return (Player) this;
		
		if (level == null) return null;
		
		return level.getClosestPlayer(x, y);
	}
}
