package minicraft.entity.mob;

import minicraft.core.Game;
import minicraft.core.Renderer;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.furniture.Tnt;
import minicraft.entity.particle.BurnParticle;
import minicraft.entity.particle.TextParticle;
import minicraft.gfx.Color;
import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.item.PotionType;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;

public abstract class Mob extends Entity {

	protected LinkedSprite[][] sprites; // This contains all the mob's sprites, sorted first by direction (index corresponding to the dir variable), and then by walk animation state.
	public int walkDist = 0; // How far we've walked currently, incremented after each movement. This is used to change the sprite; "(walkDist >> 3) & 1" switches between a value of 0 and 1 every 8 increments of walkDist.

	public Direction dir = Direction.DOWN; // The direction the mob is facing, used in attacking and rendering. 0 is down, 1 is up, 2 is left, 3 is right
	int hurtTime = 0; // A delay after being hurt, that temporarily prevents further damage for a short time
	private int xKnockback, yKnockback; // The amount of vertical/horizontal knockback that needs to be inflicted, if it's not 0, it will be moved one pixel at a time.
	public int health;
	public final int maxHealth; // The amount of health we currently have, and the maximum.
	int walkTime;
	public int speed;
	int tickTime = 0; // Incremented whenever tick() is called, is effectively the age in ticks
	int noActionTime = 0;

	/**
	 * Default constructor for a mob.
	 * Default x radius is 4, and y radius is 3.
	 *
	 * @param sprites All of this mob's sprites.
	 * @param health  The mob's max health.
	 */
	public Mob(LinkedSprite[][] sprites, int health) {
		super(4, 3);
		this.sprites = sprites;
		this.health = this.maxHealth = health;
		walkTime = 1;
		speed = 1;
	}

	/**
	 * Updates the mob.
	 */
	@Override
	public void tick() {
		tickTime++; // Increment our tick counter

		if (isRemoved()) return;
		noActionTime++;

		if (level != null && level.getTile(x >> 4, y >> 4) == Tiles.get("lava")) // If we are trying to swim in lava
			hurt(Tiles.get("lava"), x, y, 4); // Inflict 4 damage to ourselves, sourced from the lava Tile, with the direction as the opposite of ours.

		if (canBurn()) {
			if (this.burningDuration > 0) {
				if (level.getTile(x / 16, y / 16) == Tiles.get("water")) this.burningDuration = 0;
				if (this.burningDuration % 10 == 0)
					level.add(new BurnParticle(x - 8 + (random.nextInt(8) - 4), y - 8 + (random.nextInt(8) - 4)));
				this.burningDuration--;
				if (this instanceof Player) {
					if (this.burningDuration % 70 == 0 && !Renderer.player.potioneffects.containsKey(PotionType.Lava))
						hurt(this, 1, Direction.NONE); //burning damage
				} else {
					if (this.burningDuration % 70 == 0)
						hurt(this, 2, Direction.NONE); //burning damage
				}
			}
		}

		if (health <= 0) die(); // Die if no health
		if (hurtTime > 0) hurtTime--; // If a timer preventing damage temporarily is set, decrement it's value


		/// The code below checks the direction of the knockback, moves the Mob accordingly, and brings the knockback closer to 0.
		int xd = 0, yd = 0;
		if (xKnockback != 0) {
			xd = (int) Math.ceil(xKnockback / 2);
			xKnockback -= xKnockback / Math.abs(xKnockback);
		}
		if (yKnockback != 0) {
			yd = (int) Math.ceil(yKnockback / 2);
			yKnockback -= yKnockback / Math.abs(yKnockback);
		}

		move(xd, yd, false);
	}

	@Override
	public boolean move(int xd, int yd) {
		return move(xd, yd, true);
	} // Move the mob, overrides from Entity

	private boolean move(int xd, int yd, boolean changeDir) { // Knockback shouldn't change mob direction
		if (level == null) return false; // Stopped b/c there's no level to move in!

		@SuppressWarnings("unused")
		int oldxt = x >> 4;
		@SuppressWarnings("unused")
		int oldyt = y >> 4;

		// These should return true b/c the mob is still technically moving; these are just to make it move *slower*.
		if (tickTime % 2 == 0 && (isSwimming() || (!(this instanceof Player) && isWooling())))
			return true;
		if (tickTime % walkTime == 0 && walkTime > 1)
			return true;

		boolean moved = true;

		if (hurtTime == 0 || this instanceof Player) { // If a mobAi has been hurt recently and hasn't yet cooled down, it won't perform the movement (by not calling super)
			if (xd != 0 || yd != 0) {
				if (changeDir)
					dir = Direction.getDirection(xd, yd); // Set the mob's direction; NEVER set it to NONE
				walkDist++;
			}

			// This part makes it so you can't move in a direction that you are currently being knocked back from.
			if (xKnockback != 0)
				xd = Math.copySign(xd, xKnockback) * -1 != xd ? xd : 0; // If xKnockback and xd have different signs, do nothing, otherwise, set xd to 0.
			if (yKnockback != 0)
				yd = Math.copySign(yd, yKnockback) * -1 != yd ? yd : 0; // Same as above.

			moved = super.move(xd, yd); // Call the move method from Entity
		}

		return moved;
	}

	/**
	 * The mob immediately despawns if the distance of the closest player is greater than the return value of this.
	 */
	protected int getDespawnDistance() {
		return 1280;
	}

	/**
	 * The mob randomly despawns if the distance of the closest player is greater than the return value of this.
	 */
	protected int getNoDespawnDistance() {
		return 640;
	}

	/**
	 * @see #handleDespawn()
	 */
	protected boolean removeWhenFarAway(@SuppressWarnings("unused") double distance) {
		return true;
	}

	/**
	 * This is an easy way to make a list of sprites that are all part of the same "Sprite", so they have similar parameters, but they're just at different locations on the spreadsheet.
	 */
	public static LinkedSprite[] compileSpriteList(int sheetX, int sheetY, int width, int height, int mirror, int number, String key) {
		LinkedSprite[] sprites = new LinkedSprite[number];
		for (int i = 0; i < sprites.length; i++)
			sprites[i] = new LinkedSprite(SpriteType.Entity, key).setSpriteDim(sheetX + width * i, sheetY, width, height)
				.setMirror(mirror).setFlip(mirror);

		return sprites;
	}

	public static LinkedSprite[][] compileMobSpriteAnimations(int sheetX, int sheetY, String key) {
		LinkedSprite[][] sprites = new LinkedSprite[4][2];
		// dir numbers: 0=down, 1=up, 2=left, 3=right.
		/// On the spritesheet, most mobs have 4 sprites there, first facing down, then up, then right 1, then right 2. The first two get flipped to animate them, but the last two get flipped to change direction.

		// Contents: down 1, up 1, right 1, right 2
		LinkedSprite[] set1 = compileSpriteList(sheetX, sheetY, 2, 2, 0, 4, key);

		// Contents: down 2, up 2, left 1, left 2
		LinkedSprite[] set2 = compileSpriteList(sheetX, sheetY, 2, 2, 1, 4, key);

		// Down
		sprites[0][0] = set1[0];
		sprites[0][1] = set2[0];

		// Up
		sprites[1][0] = set1[1];
		sprites[1][1] = set2[1];

		// Left
		sprites[2][0] = set2[2];
		sprites[2][1] = set2[3];

		// Right
		sprites[3][0] = set1[2];
		sprites[3][1] = set1[3];

		return sprites;
	}

	private boolean isWooling() { // supposed to walk at half speed on wool
		if (level == null) return false;
		Tile tile = level.getTile(x >> 4, y >> 4);
		return tile == Tiles.get("wool");
	}

	/**
	 * Checks if this Mob is currently on a light tile; if so, the mob sprite is brightened.
	 *
	 * @return true if the mob is on a light tile, false if not.
	 */
	public boolean isLight() {
		if (level == null) return false;
		return level.isLight(x >> 4, y >> 4);
	}

	/**
	 * Checks if the mob is swimming (standing on a liquid tile).
	 *
	 * @return true if the mob is swimming, false if not.
	 */
	public boolean isSwimming() {
		if (level == null) return false;
		Tile tile = level.getTile(x >> 4, y >> 4); // Get the tile the mob is standing on (at x/16, y/16)
		return tile == Tiles.get("water") || tile == Tiles.get("lava"); // Check if the tile is liquid, and return true if so
	}

	/**
	 * Do damage to the mob this method is called on.
	 *
	 * @param tile   The tile that hurt the player
	 * @param x      The x position of the mob
	 * @param y      The x position of the mob
	 * @param damage The amount of damage to hurt the mob with
	 */
	public void hurt(Tile tile, int x, int y, int damage) { // Hurt the mob, when the source of damage is a tile
		Direction attackDir = Direction.getDirection(dir.getDir() ^ 1); // Set attackDir to our own direction, inverted. XORing it with 1 flips the rightmost bit in the variable, this effectively adds one when even, and subtracts one when odd.
		if (!(tile == Tiles.get("lava") && this instanceof Player && ((Player) this).potioneffects.containsKey(PotionType.Lava)))
			doHurt(damage, tile.mayPass(level, x, y, this) ? Direction.NONE : attackDir); // Call the method that actually performs damage, and set it to no particular direction
	}

	/**
	 * Do damage to the mob this method is called on.
	 *
	 * @param mob    The mob that hurt this mob
	 * @param damage The amount of damage to hurt the mob with
	 */
	public void hurt(Mob mob, int damage) {
		hurt(mob, damage, getAttackDir(mob, this));
	}

	/**
	 * Do damage to the mob this method is called on.
	 *
	 * @param mob       The mob that hurt this mob
	 * @param damage    The amount of damage to hurt the mob with
	 * @param attackDir The direction this mob was attacked from
	 */
	public void hurt(Mob mob, int damage, Direction attackDir) { // Hurt the mob, when the source is another mob
		if (mob instanceof Player && Game.isMode("minicraft.settings.mode.creative") && mob != this)
			doHurt(health, attackDir); // Kill the mob instantly
		else doHurt(damage, attackDir); // Call the method that actually performs damage, and use our provided attackDir
	}

	/**
	 * @param sec duration in seconds
	 */
	public void burn(int sec) {
		this.burningDuration = sec * 60;
	}

	/**
	 * Executed when a TNT bomb explodes near this mob.
	 *
	 * @param tnt The TNT exploding.
	 * @param dmg The amount of damage the explosion does.
	 */
	public void onExploded(Tnt tnt, int dmg) {
		doHurt(dmg, getAttackDir(tnt, this));
	}

	/**
	 * Hurt the mob, based on only damage and a direction
	 * This is overridden in Player.java
	 *
	 * @param damage    The amount of damage to hurt the mob with
	 * @param attackDir The direction this mob was attacked from
	 */
	protected void doHurt(int damage, Direction attackDir) {
		if (isRemoved() || hurtTime > 0)
			return; // If the mob has been hurt recently and hasn't cooled down, don't continue

		health -= damage; // Actually change the health

		// Add the knockback in the correct direction
		xKnockback = attackDir.getX() * 6;
		yKnockback = attackDir.getY() * 6;
		hurtTime = 10; // Set a delay before we can be hurt again
	}

	/**
	 * Restores health to this mob.
	 *
	 * @param heal How much health is restored.
	 */
	public void heal(int heal) { // Restore health on the mob
		if (hurtTime > 0) return; // If the mob has been hurt recently and hasn't cooled down, don't continue

		level.add(new TextParticle("" + heal, x, y, Color.GREEN)); // Add a text particle in our level at our position, that is green and displays the amount healed
		health += heal; // Actually add the amount to heal to our current health
		if (health > (Player.baseHealth + Player.extraHealth))
			health = (Player.baseHealth + Player.extraHealth); // If our health has exceeded our maximum, lower it back down to said maximum
	}

	protected static Direction getAttackDir(Entity attacker, Entity hurt) {
		return Direction.getDirection(hurt.x - attacker.x, hurt.y - attacker.y);
	}

	/**
	 * This checks how the {@code attacker} can damage this mob.
	 *
	 * @param attacker The attacker entity.
	 * @return The calculated damage.
	 */
	public int calculateEntityDamage(Entity attacker, int damage) {
		return damage;
	}
}
