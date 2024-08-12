package minicraft.entity.mob;

import minicraft.core.Renderer;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.furniture.Tnt;
import minicraft.entity.particle.BurnParticle;
import minicraft.entity.particle.TextParticle;
import minicraft.gfx.Color;
import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.item.Item;
import minicraft.item.PotionType;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;
import minicraft.util.DamageSource;
import org.jetbrains.annotations.Nullable;

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
	 * @param sprites All of this mob's sprites.
	 * @param health The mob's max health.
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
			// Inflict 4 damage to ourselves, sourced from the lava Tile, with the direction as the opposite of ours.
			hurt(new DamageSource(DamageSource.DamageType.LAVA, level, x, y, Tiles.get("lava")), Direction.NONE, 4);

		if (canBurn()) {
			if (this.burningDuration > 0) {
				if (level.getTile(x >> 4, y >> 4) == Tiles.get("water")) this.burningDuration = 0;
				if (this.burningDuration % 10 == 0)
					level.add(new BurnParticle(x - 8 + (random.nextInt(8) - 4), y - 8 + (random.nextInt(8) - 4)));
				this.burningDuration--;
				// TODO different damage?
				hurt(new DamageSource(DamageSource.DamageType.ON_FIRE, level, x, y, null), // TODO last attacker
					Direction.NONE, this instanceof Player ? 1 : 2); //burning damage
			}
		}

		if (health <= 0) die(); // Die if no health
		if (hurtTime > 0) hurtTime--; // If a timer preventing damage temporarily is set, decrement it's value


		/// The code below checks the direction of the knockback, moves the Mob accordingly, and brings the knockback closer to 0.
		int xd = 0, yd = 0;
		if (xKnockback != 0) {
			xd = (int) Math.ceil(xKnockback / 2F);
			xKnockback -= xKnockback / Math.abs(xKnockback);
		}
		if (yKnockback != 0) {
			yd = (int) Math.ceil(yKnockback / 2F);
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
	 * @return true if the mob is on a light tile, false if not.
	 */
	public boolean isLight() {
		if (level == null) return false;
		return level.isLight(x >> 4, y >> 4);
	}

	/**
	 * Checks if the mob is swimming (standing on a liquid tile).
	 * @return true if the mob is swimming, false if not.
	 */
	public boolean isSwimming() {
		if (level == null) return false;
		Tile tile = level.getTile(x >> 4, y >> 4); // Get the tile the mob is standing on (at x/16, y/16)
		return tile == Tiles.get("water") || tile == Tiles.get("lava"); // Check if the tile is liquid, and return true if so
	}

	/**
	 * Attacks an entity
	 * @param entity The entity to attack
	 * @return If the interaction was successful
	 */
	public boolean attack(Entity entity) {
		return false;
	}

	/**
	 * @param sec duration in seconds
	 */
	public void burn(int sec) {
		this.burningDuration = sec * 60;
	}

	/**
	 * Executed when a TNT bomb explodes near this mob.
	 * @param tnt The TNT exploding.
	 * @param dmg The amount of damage the explosion does.
	 */
	public void onExploded(Tnt tnt, int dmg) {
		hurt(new DamageSource(DamageSource.DamageType.EXPLOSION, tnt, null),
			getInteractionDir(tnt, this), dmg);
	}

	@Override
	public boolean hurt(DamageSource source, Direction attackDir, int damage) {
		handleDamage(source, attackDir, damage);
		return true;
	}

	@Override
	protected void handleDamage(DamageSource source, Direction attackDir, int damage) {
		if (isRemoved() || hurtTime > 0)
			return; // If the mob has been hurt recently and hasn't cooled down, don't continue

		Player player = getClosestPlayer();
		if (player != null) { // If there is a player in the level

			/// Play the hurt sound only if the player is less than 80 entity coordinates away; or 5 tiles away.
			int xd = player.x - x;
			int yd = player.y - y;
			if (xd * xd + yd * yd < 80 * 80) {
				Sound.play("monsterhurt");
			}
		}
		level.add(new TextParticle("" + damage, x, y, Color.RED)); // Make a text particle at this position in this level, bright red and displaying the damage inflicted

		health -= damage; // Actually change the health

		// Add the knockback in the correct direction
		xKnockback = attackDir.getX() * 6;
		yKnockback = attackDir.getY() * 6;
		hurtTime = 10; // Set a delay before we can be hurt again
	}

	/**
	 * Restores health to this mob.
	 * @param heal How much health is restored.
	 */
	public void heal(int heal) { // Restore health on the mob
		if (hurtTime > 0) return; // If the mob has been hurt recently and hasn't cooled down, don't continue

		level.add(new TextParticle("" + heal, x, y, Color.GREEN)); // Add a text particle in our level at our position, that is green and displays the amount healed
		health += heal; // Actually add the amount to heal to our current health
		if (health > (Player.baseHealth + Player.extraHealth))
			health = (Player.baseHealth + Player.extraHealth); // If our health has exceeded our maximum, lower it back down to said maximum
	}
}
