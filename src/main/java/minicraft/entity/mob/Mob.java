package minicraft.entity.mob;

import java.util.List;

import minicraft.core.Game;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.furniture.Tnt;
import minicraft.entity.particle.TextParticle;
import minicraft.gfx.Color;
import minicraft.gfx.MobSprite;
import minicraft.item.PotionType;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;

public abstract class Mob extends Entity {
	
	protected MobSprite[][] sprites; // This contains all the mob's sprites, sorted first by direction (index corresponding to the dir variable), and then by walk animation state.
	public int walkDist = 0; // How far we've walked currently, incremented after each movement. This is used to change the sprite; "(walkDist >> 3) & 1" switches between a value of 0 and 1 every 8 increments of walkDist.
	
	public Direction dir = Direction.DOWN; // The direction the mob is facing, used in attacking and rendering. 0 is down, 1 is up, 2 is left, 3 is right
	int hurtTime = 0; // A delay after being hurt, that temporarily prevents further damage for a short time
	private int xKnockback, yKnockback; // The amount of vertical/horizontal knockback that needs to be inflicted, if it's not 0, it will be moved one pixel at a time.
	public int health;
	final int maxHealth; // The amount of health we currently have, and the maximum.
	int walkTime;
	public int speed;
	int tickTime = 0; // Incremented whenever tick() is called, is effectively the age in ticks
	
	/**
	 * Default constructor for a mob.
	 * Default x radius is 4, and y radius is 3.
	 * @param sprites All of this mob's sprites.
	 * @param health The mob's max health.
	 */
	public Mob(MobSprite[][] sprites, int health) {
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
		
		if (level != null && level.getTile(x >> 4, y >> 4) == Tiles.get("lava")) // If we are trying to swim in lava
			hurt(Tiles.get("lava"), x, y, 4); // Inflict 4 damage to ourselves, sourced from the lava Tile, with the direction as the opposite of ours.
		if (health <= 0) die(); // Die if no health
		if (hurtTime > 0) hurtTime--; // If a timer preventing damage temporarily is set, decrement it's value
		

		/// The code below checks the direction of the knockback, moves the Mob accordingly, and brings the knockback closer to 0.
		int xd = 0, yd = 0;
		if (xKnockback != 0) {
			xd = (int)Math.ceil(xKnockback/2);
			xKnockback -= xKnockback/Math.abs(xKnockback);
		}
		if (yKnockback != 0) {
			yd = (int)Math.ceil(yKnockback/2);
			yKnockback -= yKnockback/Math.abs(yKnockback);
		}
		
		// if the player moved via knockback, update the server
		if ((xd != 0 || yd != 0) && Game.isConnectedClient() && this == Game.player)
			Game.client.move((Player)this, x+xd, y+yd);

		move(xd, yd, false);
	}
	
	@Override
	public boolean move(int xd, int yd) { return move(xd, yd, true); } // Move the mob, overrides from Entity
	private boolean move(int xd, int yd, boolean changeDir) { // Knockback shouldn't change mob direction
		if (level == null) return false; // Stopped b/c there's no level to move in!
		
		int oldxt = x >> 4;
		int oldyt = y >> 4;
		
		if (!(Game.isValidServer() && this instanceof RemotePlayer)) { // this will be the case when the client has sent a move packet to the server. In this case, we DO want to always move.
			// These should return true b/c the mob is still technically moving; these are just to make it move *slower*.
			if (tickTime % 2 == 0 && (isSwimming() || (!(this instanceof Player) && isWooling())))
				return true;
			if (tickTime % walkTime == 0 && walkTime > 1)
				return true;
		}
		
		boolean moved = true;
		
		if (hurtTime == 0 || this instanceof Player) { // If a mobAi has been hurt recently and hasn't yet cooled down, it won't perform the movement (by not calling super)
			if (xd != 0 || yd != 0) {
				if (changeDir)
					dir = Direction.getDirection(xd, yd); // Set the mob's direction; NEVER set it to NONE
				walkDist++;
			}
			
			// This part makes it so you can't move in a direction that you are currently being knocked back from.
			if (xKnockback != 0)
				xd = Math.copySign(xd, xKnockback)*-1 != xd ? xd : 0; // If xKnockback and xd have different signs, do nothing, otherwise, set xd to 0.
			if (yKnockback != 0)
				yd = Math.copySign(yd, yKnockback)*-1 != yd ? yd : 0; // Same as above.
			
			moved = super.move(xd, yd); // Call the move method from Entity
		}
		
		if (Game.isValidServer() && (xd != 0 || yd != 0))
			updatePlayers(oldxt, oldyt);
		
		return moved;
	}
	
	public void updatePlayers(int oldxt, int oldyt) {
		if (!Game.isValidServer()) return;
		
		List<RemotePlayer> prevPlayers = Game.server.getPlayersInRange(level, oldxt, oldyt, true);
		
		List<RemotePlayer> activePlayers = Game.server.getPlayersInRange(this, true);
		for (int i = 0; i < prevPlayers.size(); i++) {
			if (activePlayers.contains(prevPlayers.get(i))) {
				activePlayers.remove(prevPlayers.remove(i));
				i--;
			}
		}
		for (int i = 0; i < activePlayers.size(); i++) {
			if (prevPlayers.contains(activePlayers.get(i))) {
				prevPlayers.remove(activePlayers.remove(i));
				i--;
			}
		}
		// The lists should now only contain players that are now out of range, and players that are just now in range.
		for (RemotePlayer rp: prevPlayers)
			 Game.server.getAssociatedThread(rp).sendEntityRemoval(this.eid);
		for (RemotePlayer rp: activePlayers)
			 Game.server.getAssociatedThread(rp).sendEntityAddition(this);
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
		return level.isLight(x>>4, y>>4);
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
	 * Do damage to the mob this method is called on.
	 * @param tile The tile that hurt the player
	 * @param x The x position of the mob
	 * @param y The x position of the mob
	 * @param damage The amount of damage to hurt the mob with
	 */
	public void hurt(Tile tile, int x, int y, int damage) { // Hurt the mob, when the source of damage is a tile
		Direction attackDir = Direction.getDirection(dir.getDir() ^ 1); // Set attackDir to our own direction, inverted. XORing it with 1 flips the rightmost bit in the variable, this effectively adds one when even, and subtracts one when odd.
		if (!(tile == Tiles.get("lava") && this instanceof Player && ((Player)this).potioneffects.containsKey(PotionType.Lava)))
			doHurt(damage, tile.mayPass(level, x, y, this) ? Direction.NONE : attackDir); // Call the method that actually performs damage, and set it to no particular direction
	}

	/**
	 * Do damage to the mob this method is called on.
	 * @param mob The mob that hurt this mob
	 * @param damage The amount of damage to hurt the mob with
	 */
	public void hurt(Mob mob, int damage) { hurt(mob, damage, getAttackDir(mob, this)); }

	/**
	 * Do damage to the mob this method is called on.
	 * @param mob The mob that hurt this mob
	 * @param damage The amount of damage to hurt the mob with
	 * @param attackDir The direction this mob was attacked from
	 */
	public void hurt(Mob mob, int damage, Direction attackDir) { // Hurt the mob, when the source is another mob
		if (mob instanceof Player && Game.isMode("creative") && mob != this) doHurt(health, attackDir); // Kill the mob instantly
		else doHurt(damage, attackDir); // Call the method that actually performs damage, and use our provided attackDir
	}

	/**
	 * Executed when a TNT bomb explodes near this mob.
	 * @param tnt The TNT exploding.
	 * @param dmg The amount of damage the explosion does.
	 */
	public void onExploded(Tnt tnt, int dmg) { doHurt(dmg, getAttackDir(tnt, this)); }

	/**
	 * Hurt the mob, based on only damage and a direction
	 * This is overridden in Player.java
	 * @param damage The amount of damage to hurt the mob with
	 * @param attackDir The direction this mob was attacked from
	 */
	protected void doHurt(int damage, Direction attackDir) {
		if (isRemoved() || hurtTime > 0) return; // If the mob has been hurt recently and hasn't cooled down, don't continue
		
		health -= damage; // Actually change the health
		
		// Add the knockback in the correct direction
		xKnockback = attackDir.getX()*6;
		yKnockback = attackDir.getY()*6;
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
		if (health > maxHealth) health = maxHealth; // If our health has exceeded our maximum, lower it back down to said maximum
	}

	protected static Direction getAttackDir(Entity attacker, Entity hurt) {
		return Direction.getDirection(hurt.x - attacker.x, hurt.y - attacker.y);
	}
	
	@Override
	protected String getUpdateString() {
		String updates = super.getUpdateString() + ";";
		updates += "dir," + dir.ordinal() +
		";health," + health +
		";hurtTime," + hurtTime;
		
		return updates;
	}
	
	@Override
	protected boolean updateField(String field, String val) {
		if (field.equals("x") || field.equals("y")) walkDist++;
		if (super.updateField(field, val)) return true;
		switch (field) {
			case "dir": dir = Direction.values[Integer.parseInt(val)]; return true;
			case "health": health = Integer.parseInt(val); return true;
			case "hurtTime": hurtTime = Integer.parseInt(val); return true;
		}
		
		return false;
	}
}
