package com.mojang.ld22.entity;


import com.mojang.ld22.entity.particle.TextParticle;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.level.Level;
import com.mojang.ld22.level.tile.Tile;
import com.mojang.ld22.screen.ModeMenu;
import com.mojang.ld22.sound.Sound;

public class Mob extends Entity {
	private Player player;
	protected int walkDist = 0; // How far we've walked currently, incremented after each movement
	protected int dir = 0; // The direction the mob is facing, used in attacking and rendering. 0 is down, 1 is up, 2 is left, 3 is right
	public int hurtTime = 0; // A delay after being hurt, that temporarily prevents further damage for a short time
	protected int xKnockback, yKnockback; // The amount of vertical/horizontal knockback that needs to be inflicted, if it's not 0, it will be moved one pixel at a time.
	public int maxHealth = 10; // The maximum amount of health the mob can have
	public int health = maxHealth; // The amount of health we currently have, set to the maximum to start
	public int maxHunger = 10;
	public int hunger = maxHunger;
	public int swimTimer = 0; // How much we have moved in water currently, used to halve movement speed
	public int woolTimer = 0;
	//public int lightTimer = 0;
	public int tickTime = 0; // Incremented whenever tick() is called, is effectively the age in ticks
	public int r;
	public int xx;
	public int yy;
	public boolean isenemy = false;
	public int lvl;

	public Mob() {
		x = y = 8; // By default, set x and y coordinates to 8
		xr = 4; // Sets the x and y radius/size of the mob
		yr = 3;
		xx = x;
		yy = y;
	}

	public void tick() {
		tickTime++; // Increment our tick counter
		if (level.getTile(x >> 4, y >> 4) == Tile.lava) // If we are trying to swim in lava
			hurt(this, 4, dir ^ 1); // Inflict 4 damage to ourselves, sourced from ourselves, with the direction as the opposite of ours
		if (health <= 0) die(); // die if no health
		if (hurtTime > 0) hurtTime--; // If a timer preventing damage temporarily is set, decrement it's value
	}

	protected void die() { // Kill the mob, called when health drops to 0
		remove(); // Remove the mob, with the method inherited from Entity
	}

	public boolean move(int xa, int ya) { // Move the mob, overrides from Entity
		if (isSwimming()) { // Check if the mob is swimming, ie. in water/lava
			if (swimTimer++ % 2 == 0) return true; // Increments swimTimer, and returns every other time
		}
		if (isWooling()) { // same as above, for wool
			if (woolTimer++ % 2 == 0) return true;
		}
		
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
		if (hurtTime > 0) return true; // If we have been hurt recently and haven't yet cooled down, don't continue with the movement (so only knockback will be performed)
		
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
		Tile tile = level.getTile(x >> 0, y >> 0);
		return tile == Tile.wool;
	}
	
	/** checks if this Mob is currently on a light tile; if so, the mob sprite is brightened. */
	public boolean isLight() {
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
		Tile tile = level.getTile(x >> 4, y >> 4); // Get the tile the mob is standing on (at x/16, y/16)
		return tile == Tile.water || tile == Tile.lava || tile == Tile.lightwater; // Check if the tile is liquid, and return true if so
	}
	
	// this is useless, I think. Why have both "blocks" and "isBlockableBy"?
	public boolean blocks(Entity e) { // Check if another entity would be prevented from moving through this one
		return e.isBlockableBy(this); // Call the method on the other entity to determine this, and return it
	}

	public void hurt(Tile tile, int x, int y, int damage) { // Hurt the mob, when the source of damage is a tile
		int attackDir = dir ^ 1; // Set attackDir to our own direction, inverted. XORing it with 1 flips the rightmost bit in the variable, this effectively adds one when even, and subtracts one when odd
		doHurt(damage, attackDir); // Call the method that actually performs damage, and provide it with our new attackDir value
	}
	
	public void hurt(Mob mob, int damage, int attackDir) { // Hurt the mob, when the source is another mob
		doHurt(damage, attackDir); // Call the method that actually performs damage, and use our provided attackDir
	}
	
	public void heal(int heal) { // Restore health on the mob
		if (hurtTime > 0) return; // If the mob has been hurt recently and hasn't cooled down, don't continue
		
		level.add(new TextParticle("" + heal, x, y, Color.get(-1, 50, 50, 50))); // Add a text particle in our level at our position, that is green and displays the amount healed
		health += heal; // Actually add the amount to heal to our current health
		if (health > maxHealth) health = maxHealth; // If our health has exceeded our maximum, lower it back down to said maximum
	}

	public void hungerHeal(int hungerHeal) {

		hunger += hungerHeal;
		if (hunger > maxHunger) hunger = maxHunger;
	}

	protected void doHurt(int damage, int attackDir) { // Actually hurt the mob, based on only damage and a direction
		// this is overridden in Player.java
		if (hurtTime > 0) return; // If the mob has been hurt recently and hasn't cooled down, don't continue
		
		if (level.player != null) { // If there is a player in the level
			/// play the hurt sound only if the player is less than 80 entity coordinates away; or 5 tiles away.
			int xd = level.player.x - x;
			int yd = level.player.y - y;
			if (xd * xd + yd * yd < 80 * 80) {
				Sound.monsterHurt.play();
			}
		}
		level.add(new TextParticle("" + damage, x, y, Color.get(-1, 500, 500, 500))); // Make a text particle at this position in this level, bright red and displaying the damage inflicted
		health -= damage; // Actually change the health
		// add the knockback in the correct direction
		if (attackDir == 0) yKnockback = +6;
		if (attackDir == 1) yKnockback = -6;
		if (attackDir == 2) xKnockback = -6;
		if (attackDir == 3) xKnockback = +6;
		hurtTime = 10; // Set a delay before we can be hurt again
	}

	public boolean findStartPos(Level level) { // Find a place to spawn the mob
		// Pick a random x and y coordinate, inside the level's bounds
		int x = random.nextInt(level.w);
		int y = random.nextInt(level.h);
		int xx = x * 16 + 8; // Get actual pixel (entity) coordinates from this tile coord
		int yy = y * 16 + 8;

		if (level.player != null) {
			/// don't spawn a mob less than 5 blocks away from the player
			int xd = level.player.x - xx;
			int yd = level.player.y - yy;
			// prevents any mobs from spawning too close to the player.
			if (xd * xd + yd * yd < 60 * 60) return false;
		}

		int r = level.monsterDensity * 13; // Get the allowed density of mobs in the level, convert it from a tile to a real coordinate
		if (level.getEntities(xx - r, yy - r, xx + r, yy + r).size() > 0) return false; // Get a list of mobs in the level, within a box centered on our attempted coordinates, with dimensions of r times 2; and, if there are any close to us, return false.
		
		// TODO yeah... this DEFINATELY needs revision... will be part of the Tile system revision.
		if (level.getTile(x, y).mayPass(level, x, y, this)) { // Make sure the tile we're trying to spawn on is solid to us
			if (level.getTile(x, y) != Tile.sdo) {
				if (level.getTile(x, y) != Tile.wdo) {
					if (level.getTile(x, y) != Tile.wheat) {
						if (level.getTile(x, y) != Tile.farmland) {
							if (level.getTile(x, y) != Tile.lightsbrick) {
								if (level.getTile(x, y) != Tile.lightplank) {
									if (level.getTile(x, y) != Tile.lightwool) {
										if (level.getTile(x, y) != Tile.lightrwool) {
											if (level.getTile(x, y) != Tile.lightbwool) {
												if (level.getTile(x, y) != Tile.lightgwool) {
													if (level.getTile(x, y) != Tile.lightywool) {
														if (level.getTile(x, y) != Tile.lightblwool) {
															if (level.getTile(x, y) != Tile.lightgrass) {
																if (level.getTile(x, y) != Tile.lightsand) {
																	if (level.getTile(x, y) != Tile.lightdirt) {
																		if (level.getTile(x, y) != Tile.lightflower) {
																			if (level.getTile(x, y) != Tile.torchgrass) {
																				if (level.getTile(x, y) != Tile.torchsand) {
																					if (level.getTile(x, y) != Tile.torchdirt) {
																						if (level.getTile(x, y) != Tile.torchplank) {
																							if (level.getTile(x, y) != Tile.torchsbrick) {
																								if (level.getTile(x, y) != Tile.torchwool) {
																									if (level.getTile(x, y) != Tile.torchwoolred) {
																										if (level.getTile(x, y) != Tile.torchwoolblue) {
																											if (level.getTile(x, y)
																													!= Tile.torchwoolgreen) {
																												if (level.getTile(x, y)
																														!= Tile.torchwoolyellow) {
																													if (level.getTile(x, y)
																															!= Tile.torchwoolblack) {

																														this.x = xx; // Set our coordinates to the attempted ones
																														this.y = yy;
																														return true;
																													}
																												}
																											}
																										}
																									}
																								}
																							}
																						}
																					}
																				}
																			}
																		}
																	}
																}
															}
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}

		return false;
	}
	
	
	/** Start pos is a bit different for dungeons. */
	public boolean findStartPosDungeon(Level level) {
		int x = random.nextInt(level.w);
		int y = random.nextInt(level.h);
		int xx = x * 16 + 8;
		int yy = y * 16 + 8;

		if (level.player != null) {
			// don't spawn if the player is less than 3.75 blocks away
			int xd = level.player.x - xx;
			int yd = level.player.y - yy;

			if (xd * xd + yd * yd < 60 * 60) return false;
		}
		
		// Get the allowed density of mobs in the level, convert it from a tile to a real coordinate (?) higher in score mode.
		if (!ModeMenu.score) r = level.monsterDensity * 15;
		else r = level.monsterDensity * 22;
		
		if (level.getEntities(xx - r, yy - r, xx + r, yy + r).size() > 0) return false; // don't spawn on top of, or too close to, another entity.

		if (level.getTile(x, y).mayPass(level, x, y, this)) {
			if (level.getTile(x, y) == Tile.o) {
				// can only spawn on obsidian bricks
				this.x = xx;
				this.y = yy;
				return true;
			}
		}

		return false;
	}
	
	/** same as above two, but different allowed density and spawn tiles. */
	public boolean findStartPosCow(Level level) {
		int x = random.nextInt(level.w);
		int y = random.nextInt(level.h);
		int xx = x * 16 + 8;
		int yy = y * 16 + 8;

		if (level.player != null) {

			int xd = level.player.x - xx;
			int yd = level.player.y - yy;

			if (xd * xd + yd * yd < 80 * 80) return false;
		}

		if (!ModeMenu.score) {
			r = level.monsterDensity * 20;
		} else {
			r = level.monsterDensity * 27;
		}
		if (level.getEntities(xx - r, yy - r, xx + r, yy + r).size() > 0) return false;

		//makes it so that cows only spawn on grass or flowers.
		if (level.getTile(x, y).mayPass(level, x, y, this)) {
			Tile tile = level.getTile(x, y);
			if (tile == Tile.grass || tile == Tile.lightgrass || tile == Tile.flower || tile == Tile.lightflower) {
				this.x = xx;
				this.y = yy;
				return true;
			}
		}

		return false;
	}
	
	// very similar to above; but the mob density is different.
	public boolean findStartPosCowLight(Level level) {
		int x = random.nextInt(level.w);
		int y = random.nextInt(level.h);
		int xx = x * 16 + 8;
		int yy = y * 16 + 8;

		if (level.player != null) {

			int xd = level.player.x - xx;
			int yd = level.player.y - yy;

			if (xd * xd + yd * yd < 80 * 80) return false;
		}

		if (!ModeMenu.score) {
			r = level.monsterDensity * 15;
		} else {
			r = level.monsterDensity * 22;
		}
		if (level.getEntities(xx - r, yy - r, xx + r, yy + r).size() > 0) return false;

		//makes it so that cows only spawn on grass or flowers.
		if (level.getTile(x, y).mayPass(level, x, y, this)) {
			if (level.getTile(x, y) == Tile.grass) {
				this.x = xx;
				this.y = yy;
				return true;
			} else if (level.getTile(x, y) == Tile.flower) {
				this.x = xx;
				this.y = yy;
				return true;
			}
			if (level.getTile(x, y) == Tile.lightgrass) {
				this.x = xx;
				this.y = yy;
				return true;
			} else if (level.getTile(x, y) == Tile.lightflower) {
				this.x = xx;
				this.y = yy;
				return true;
			}
		}

		return false;
	}
}
