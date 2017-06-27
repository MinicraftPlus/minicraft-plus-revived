package minicraft.entity;

import minicraft.entity.particle.TextParticle;
import minicraft.gfx.Color;
import minicraft.gfx.MobSprite;
import minicraft.item.PotionType;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;
import minicraft.screen.ModeMenu;
import minicraft.Sound;

public abstract class Mob extends Entity {
	
	protected MobSprite[][] sprites; // This contains all the mob's sprites, sorted first by direction (index corrosponding to the dir variable), and then by walk animation state.
	public int walkDist = 0; // How far we've walked currently, incremented after each movement. This is used to change the sprite; "(walkDist >> 3) & 1" switches between a value of 0 and 1 every 8 increments of walkDist.
	
	public int dir = 0; // The direction the mob is facing, used in attacking and rendering. 0 is down, 1 is up, 2 is left, 3 is right
	public int hurtTime = 0; // A delay after being hurt, that temporarily prevents further damage for a short time
	protected int xKnockback, yKnockback; // The amount of vertical/horizontal knockback that needs to be inflicted, if it's not 0, it will be moved one pixel at a time.
	public int health, maxHealth; // The amount of health we currently have, and the maximum.
	protected int walkTime;
	public int speed;
	public int tickTime = 0; // Incremented whenever tick() is called, is effectively the age in ticks
	// TODO take all these swimTime, woolTime, and walkTime and consolidate into a getSlowness() method, that checks and adds each possible source of slowness and returns the result.
		// or don't.
	
	public Mob(MobSprite[][] sprites, int health) {
		super(4, 3);
		this.sprites = sprites;
		this.health = this.maxHealth = health;
		walkTime = 1;
		speed = 1;
	}
	
	public void tick() {
		tickTime++; // Increment our tick counter
		
		if (level != null && level.getTile(x >> 4, y >> 4) == Tiles.get("lava")) // If we are trying to swim in lava
			hurt(Tiles.get("lava"), x, y, 4); // Inflict 4 damage to ourselves, sourced from the lava Tile, with the direction as the opposite of ours.
		if (health <= 0) die(); // die if no health
		if (hurtTime > 0) hurtTime--; // If a timer preventing damage temporarily is set, decrement it's value
	}

	protected void die() { // Kill the mob, called when health drops to 0
		remove(); // Remove the mob, with the method inherited from Entity
	}

	public boolean move(int xa, int ya) { // Move the mob, overrides from Entity
		if(level == null) return false; // stopped b/c there's no level to move in!
		
		// these should return true b/c the mob is still technically moving; these are just to make it move *slower*.
		if (!(this instanceof Player) && tickTime % 2 == 0 && isSwimming() || isWooling())
			return true;
		if (tickTime % walkTime == 0 && walkTime > 1)
			return true;
		
		/// These 4 following conditionals check the direction of the knockback, move the Mob accordingly, and bring knockback closer to 0.
		if (xKnockback < 0) { // If we have negative horizontal knockback (to the left)
			move2(-1, 0); // Move to the left 1 pixel
			xKnockback++; // And increase the knockback by 1 so it is gradually closer to 0, and this will stop being called
		}
		if (xKnockback > 0) { // knocked to the right
			move2(1, 0);
			xKnockback--;
		}
		if (yKnockback < 0) { // knocked upwards
			move2(0, -1);
			yKnockback++;
		}
		if (yKnockback > 0) { // knocked downwards
			move2(0, 1);
			yKnockback--;
		}
		if (hurtTime > 0 && this instanceof Player == false) return true; // If a mob has been hurt recently and hasn't yet cooled down, don't continue with the movement (so only knockback will be performed)
		
		if (xa != 0 || ya != 0) { // Only if horizontal or vertical movement is actually happening
			walkDist++; // Increment our walking/movement counter
			if (xa < 0) dir = 2; // Set the mob's direction based on movement: left
			if (xa > 0) dir = 3; // right
			if (ya < 0) dir = 1; // up
			if (ya > 0) dir = 0; // down
		}
		
		return super.move(xa, ya); // Call the move method from Entity
	}

	protected boolean isWooling() { // supposed to walk at half speed on wool
		if(level == null) return false;
		Tile tile = level.getTile(x >> 4, y >> 4);
		return tile == Tiles.get("wool");
	}
	
	/** checks if this Mob is currently on a light tile; if so, the mob sprite is brightened. */
	public boolean isLight() {
		if(level == null) return false;
		//Tile tile = level.getTile(x >> 4, y >> 4);
		return level.isLight(x>>4, y>>4);
	}

	protected boolean isSwimming() {
		if(level == null) return false;
		Tile tile = level.getTile(x >> 4, y >> 4); // Get the tile the mob is standing on (at x/16, y/16)
		return tile == Tiles.get("water") || tile == Tiles.get("lava"); // Check if the tile is liquid, and return true if so
	}
	
	// this is useless, I think. Why have both "blocks" and "isBlockableBy"?
	public boolean blocks(Entity e) { // Check if another entity would be prevented from moving through this one
		return e.isBlockableBy(this); // Call the method on the other entity to determine this, and return it
	}

	public void hurt(Tile tile, int x, int y, int damage) { // Hurt the mob, when the source of damage is a tile
		int attackDir = dir ^ 1; // Set attackDir to our own direction, inverted. XORing it with 1 flips the rightmost bit in the variable, this effectively adds one when even, and subtracts one when odd
		if(!(tile == Tiles.get("lava") && this instanceof Player && ((Player)this).potioneffects.containsKey(PotionType.Lava)))
			doHurt(damage, attackDir); // Call the method that actually performs damage, and provide it with our new attackDir value
	}
	
	public void hurt(Mob mob, int damage, int attackDir) { // Hurt the mob, when the source is another mob
		if(mob instanceof Player && ModeMenu.creative && mob != this) doHurt(health, attackDir); // kill the mob instantly
		else doHurt(damage, attackDir); // Call the method that actually performs damage, and use our provided attackDir
	}
	
	public void heal(int heal) { // Restore health on the mob
		if (hurtTime > 0) return; // If the mob has been hurt recently and hasn't cooled down, don't continue
		
		level.add(new TextParticle("" + heal, x, y, Color.get(-1, 50))); // Add a text particle in our level at our position, that is green and displays the amount healed
		health += heal; // Actually add the amount to heal to our current health
		if (health > maxHealth) health = maxHealth; // If our health has exceeded our maximum, lower it back down to said maximum
	}
	
	protected void doHurt(int damage, int attackDir) { // Actually hurt the mob, based on only damage and a direction
		// this is overridden in Player.java
		if (removed || hurtTime > 0) return; // If the mob has been hurt recently and hasn't cooled down, don't continue
		
		Player player = getClosestPlayer();
		if (player != null) { // If there is a player in the level
			/// play the hurt sound only if the player is less than 80 entity coordinates away; or 5 tiles away.
			int xd = player.x - x;
			int yd = player.y - y;
			if (xd * xd + yd * yd < 80 * 80) {
				Sound.monsterHurt.play();
			}
		}
		level.add(new TextParticle("" + damage, x, y, Color.get(-1, 500))); // Make a text particle at this position in this level, bright red and displaying the damage inflicted
		health -= damage; // Actually change the health
		// add the knockback in the correct direction
		if (attackDir == 0) yKnockback = +6;
		if (attackDir == 1) yKnockback = -6;
		if (attackDir == 2) xKnockback = -6;
		if (attackDir == 3) xKnockback = +6;
		hurtTime = 10; // Set a delay before we can be hurt again
	}
	
	public String getUpdates() {
		String updates = super.getUpdates() + ";";
		updates += "dir,"+dir+
		";health,"+health+
		";hurtTime,"+hurtTime+
		";walkDist,"+walkDist;
		
		return updates;
	}
	
	protected boolean updateField(String field, String val) {
		if(super.updateField(field, val)) return true;
		switch(field) {
			case "dir": dir = Integer.parseInt(val); return true;
			case "health": health = Integer.parseInt(val); return true;
			case "hurtTime": hurtTime = Integer.parseInt(val); return true;
			case "walkDist": walkDist = Integer.parseInt(val); return true;
		}
		
		return false;
	}
}
