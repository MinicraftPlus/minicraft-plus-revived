package com.mojang.ld22.entity;

import com.mojang.ld22.entity.particle.TextParticle;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.MobSprite;
import com.mojang.ld22.level.Level;
import com.mojang.ld22.level.tile.Tile;
import com.mojang.ld22.screen.ModeMenu;
import com.mojang.ld22.sound.Sound;

public abstract class Mob extends Entity {
	private Player player;
	
	//protected int[][] colors;
	protected MobSprite[][] sprites; // This contains all the mob's sprites, sorted first by direction (index corrosponding to the dir variable), and then by walk animation state.
	protected int walkDist = 0; // How far we've walked currently, incremented after each movement. This is used to change the sprite; "(walkDist >> 3) & 1" switches between a value of 0 and 1 every 8 increments of walkDist.
	
	protected int dir = 0; // The direction the mob is facing, used in attacking and rendering. 0 is down, 1 is up, 2 is left, 3 is right
	public int hurtTime = 0; // A delay after being hurt, that temporarily prevents further damage for a short time
	protected int xKnockback, yKnockback; // The amount of vertical/horizontal knockback that needs to be inflicted, if it's not 0, it will be moved one pixel at a time.
	public int health, maxHealth; // The amount of health we currently have, and the maximum.
	//public int swimTime = 2; // How many ticks pass between each movement whil in water, used to halve movement speed
	//public int woolTime = 2;
	protected int walkTime;
	//public int lightTimer = 0;
	public int tickTime = 0; // Incremented whenever tick() is called, is effectively the age in ticks
	// TODO take all these swimTime, woolTime, and walkTime and consolidate into a getSlowness() method, that checks and adds each possible source of slowness and returns the result.
		// or don't.
	public int speed;
	
	//public int r;
	//public int xx, yy;
	//public int lvl;
	//public int color;
	
	public Mob(MobSprite[][] sprites/*, int[][] colors*/, int health) {
		super(4, 3);
		this.sprites = sprites;
		this.health = this.maxHealth = health;
		//this.colors = colors;
		//xx = x;
		//yy = y;
		walkTime = 1;
		speed = 1;
	}
	
	public void tick() {
		tickTime++; // Increment our tick counter
		
		if (level.getTile(x >> 4, y >> 4) == Tile.lava) // If we are trying to swim in lava
			hurt(Tile.lava, x, y, 4); // Inflict 4 damage to ourselves, sourced from the lava Tile, with the direction as the opposite of ours.
		if (health <= 0) die(); // die if no health
		if (hurtTime > 0) hurtTime--; // If a timer preventing damage temporarily is set, decrement it's value
	}

	protected void die() { // Kill the mob, called when health drops to 0
		remove(); // Remove the mob, with the method inherited from Entity
	}

	public boolean move(int xa, int ya) { // Move the mob, overrides from Entity
		if(level == null) return true;
		
		if(tickTime % 2 == 0/* && !(this instanceof Player && ((Player)this).staminaRechargeDelay % 2 == 0)*/) {
			if(isSwimming() || isWooling()) return true;
		}
		if (tickTime % walkTime == 0 && walkTime > 1) return true;
		
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
		if (hurtTime > 0 && this instanceof Player == false) return true; // If we have been hurt recently and haven't yet cooled down, don't continue with the movement (so only knockback will be performed)
		
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
		return tile == Tile.wool;
	}
	
	/** checks if this Mob is currently on a light tile; if so, the mob sprite is brightened. */
	public boolean isLight() {
		if(level == null) return false;
		Tile tile = level.getTile(x >> 4, y >> 4);
		return tile == Tile.lightgrass
				|| tile == Tile.lightsand
				|| tile == Tile.lightwater
				|| tile == Tile.lightdirt
				|| tile == Tile.lightflower
				|| tile == Tile.lightstairsDown
				|| tile == Tile.lightstairsUp
				|| tile == Tile.lightplank
				|| tile == Tile.lightsbrick
				|| tile == Tile.lwdo
				|| tile == Tile.lsdo
				|| tile == Tile.lighthole
				|| tile == Tile.lightwool
				|| tile == Tile.lightrwool
				|| tile == Tile.lightbwool
				|| tile == Tile.lightgwool
				|| tile == Tile.lightywool
				|| tile == Tile.lightblwool
				|| tile == Tile.lightts
				|| tile == Tile.lightcs
				|| tile == Tile.torchgrass
				|| tile == Tile.torchsand
				|| tile == Tile.torchdirt
				|| tile == Tile.torchplank
				|| tile == Tile.torchsbrick
				|| tile == Tile.torchwool
				|| tile == Tile.torchwoolred
				|| tile == Tile.torchwoolblue
				|| tile == Tile.torchwoolgreen
				|| tile == Tile.torchwoolyellow
				|| tile == Tile.torchwoolblack;
	}

	protected boolean isSwimming() {
		if(level == null) return false;
		Tile tile = level.getTile(x >> 4, y >> 4); // Get the tile the mob is standing on (at x/16, y/16)
		return tile == Tile.water || tile == Tile.lava || tile == Tile.lightwater; // Check if the tile is liquid, and return true if so
	}
	
	// this is useless, I think. Why have both "blocks" and "isBlockableBy"?
	public boolean blocks(Entity e) { // Check if another entity would be prevented from moving through this one
		return e.isBlockableBy(this); // Call the method on the other entity to determine this, and return it
	}

	public void hurt(Tile tile, int x, int y, int damage) { // Hurt the mob, when the source of damage is a tile
		int attackDir = dir ^ 1; // Set attackDir to our own direction, inverted. XORing it with 1 flips the rightmost bit in the variable, this effectively adds one when even, and subtracts one when odd
		if(!(tile == Tile.lava && this instanceof Player && ((Player)this).potioneffects.containsKey("Lava")))
			doHurt(damage, attackDir); // Call the method that actually performs damage, and provide it with our new attackDir value
	}
	
	public void hurt(Mob mob, int damage, int attackDir) { // Hurt the mob, when the source is another mob
		if(mob instanceof Player && ModeMenu.creative && mob != this) doHurt(health, attackDir); // kill the mob instantly
		else doHurt(damage, attackDir); // Call the method that actually performs damage, and use our provided attackDir
	}
	/*public void hurt(Tnt tnt, int x, int y, int dmg) {
		doHurt(dmg, dir ^ 1);
	}*/
	
	public void heal(int heal) { // Restore health on the mob
		if (hurtTime > 0) return; // If the mob has been hurt recently and hasn't cooled down, don't continue
		
		level.add(new TextParticle("" + heal, x, y, Color.get(-1, 50, 50, 50))); // Add a text particle in our level at our position, that is green and displays the amount healed
		health += heal; // Actually add the amount to heal to our current health
		if (health > maxHealth) health = maxHealth; // If our health has exceeded our maximum, lower it back down to said maximum
	}
	
	/*
	public void hungerHeal(int hungerHeal) {
		hunger += hungerHeal;
		if (hunger > maxHunger) hunger = maxHunger;
	}*/
	
	protected void doHurt(int damage, int attackDir) { // Actually hurt the mob, based on only damage and a direction
		// this is overridden in Player.java
		if (removed || hurtTime > 0) return; // If the mob has been hurt recently and hasn't cooled down, don't continue
		
		if (level.player != null) { // If there is a player in the level
			/// play the hurt sound only if the player is less than 80 entity coordinates away; or 5 tiles away.
			int xd = level.player.x - x;
			int yd = level.player.y - y;
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
}
