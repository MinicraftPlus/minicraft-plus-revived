package minicraft.entity.mob;

import minicraft.core.Game;
import minicraft.core.Updater;
import minicraft.core.World;
import minicraft.core.io.Localization;
import minicraft.core.io.Sound;
import minicraft.entity.Arrow;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.FireSpark;
import minicraft.entity.furniture.KnightStatue;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteLinker;
import minicraft.item.Items;
import minicraft.screen.AchievementsDisplay;
import org.jetbrains.annotations.Range;

public class ObsidianKnight extends EnemyMob {
	private static final SpriteLinker.LinkedSprite[][][] armored = new SpriteLinker.LinkedSprite[][][]{
		Mob.compileMobSpriteAnimations(0, 0, "obsidian_knight_armored"),
		Mob.compileMobSpriteAnimations(0, 2, "obsidian_knight_armored"),
		Mob.compileMobSpriteAnimations(0, 4, "obsidian_knight_armored"),
		Mob.compileMobSpriteAnimations(0, 6, "obsidian_knight_armored")
	};
	private static final SpriteLinker.LinkedSprite[][][] broken = new SpriteLinker.LinkedSprite[][][]{
		Mob.compileMobSpriteAnimations(0, 0, "obsidian_knight_broken"),
		Mob.compileMobSpriteAnimations(0, 2, "obsidian_knight_broken"),
		Mob.compileMobSpriteAnimations(0, 4, "obsidian_knight_broken"),
		Mob.compileMobSpriteAnimations(0, 6, "obsidian_knight_broken")
	};
	public static ObsidianKnight entity = null;

	public static final int MaxHealth = 5000;
	public static boolean beaten = false; // If the boss was beaten
	public static boolean active = false; // If the boss is active

	@Range(from = 0, to = 1)
	private int phase = 0; // The phase of the boss. {0, 1}
	private int attackPhaseCooldown = 0; // Cooldown between attacks

	private AttackPhase attackPhase = AttackPhase.Attacking;

	private enum AttackPhase {Attacking, Dashing, Walking;} // Using fire sparks in attacking.

	private static final AttackPhase[] ATTACK_PHASES = AttackPhase.values();

	private int dashTime = 0;
	private int dashCooldown = 1000;

	private int attackDelay = 0;
	private int attackTime = 0;
	private int attackLevel = 0; // Attack level is set to 0, as the default.

	/**
	 * Constructor for the ObsidianKnight.
	 */
	public ObsidianKnight(int health) {
		super(1, armored, MaxHealth, false, 16 * 8, -1, 10, 50);

		Updater.notifyAll(Localization.getLocalized("minicraft.notification.obsidian_knight_awoken")); // On spawn tell player.

		active = true;
		speed = 1;
		walkTime = 3;
		this.health = health;
		entity = this;

		World.levels[World.lvlIdx(-4)].regenerateBossRoom();
	}

	@Override
	public void tick() {
		super.tick();
		Player player = getClosestPlayer();
		if (player == null || player.isRemoved()) {
			active = false;
			KnightStatue ks = new KnightStatue(health);
			level.add(ks, x, y, false);
			this.remove();
			return;
		}

		// Achieve phase 2
		if (health <= 2500 && phase == 0) { // Assume that phase would not turn back to phase 1
			phase = 1;
			lvlSprites = broken; // Refreshing phased sprites
		}

		if (Game.isMode("minicraft.settings.mode.creative")) return; // Should not attack if player is in creative

		if (attackPhaseCooldown == 0) {
			AttackPhase newPhase;
			do {
				newPhase = ATTACK_PHASES[random.nextInt(ATTACK_PHASES.length)];
			} while (newPhase == attackPhase);
			attackPhase = newPhase;
			attackPhaseCooldown = 500;
		} else {
			attackPhaseCooldown--;
		}

		if (attackPhase == AttackPhase.Attacking) {
			if (attackDelay > 0) {
				xmov = ymov = 0;
				int dir = (attackDelay - 35) / 4 % 4; // The direction of attack.
				dir = (dir * 2 % 4) + (dir / 2); // Direction attack changes
				if (attackDelay < 35)
					dir = 0; // Direction is reset, if attackDelay is less than 45; prepping for attack.

				this.dir = Direction.getDirection(dir);
				attackDelay--;
				if (attackDelay == 0) {
					if (health < maxHealth / 2)
						attackLevel = 1; // If at 1000 health (50%) or lower, attackLevel = 1
					if (health < maxHealth / 10)
						attackLevel = 2; // If at 200 health (10%) or lower, attackLevel = 2
					attackTime = 120; // attackTime set to 120 (2 seconds, at default 60 ticks/sec)
				}

				return; // Skips the rest of the code (attackDelay must have been > 0)
			}

			// Send out sparks
			if (attackTime > 0) {
				xmov = ymov = 0;
				attackTime--;
				int attackDir; // The degree of attack. {0, 45, 90, 135, 180, -45, -90, -135}
				double atan2 = Math.toDegrees(Math.atan2(player.y - y, player.x - x));
				if (atan2 > 157.5 || atan2 < -157.5) attackDir = 270;
				else if (atan2 > 112.5) attackDir = 135;
				else if (atan2 > 67.5) attackDir = 90;
				else if (atan2 > 22.5) attackDir = 45;
				else if (atan2 < -112.5) attackDir = -135;
				else if (atan2 < -67.5) attackDir = -90;
				else if (atan2 < -22.5) attackDir = -45;
				else attackDir = 0;
				double speed = 1 + attackLevel * 0.2 + attackTime / 10D * 0.01; // speed is dependent on the attackType. (higher attackType, faster speeds)
				// The range of attack is 90 degrees. With little random factor.
				int phi = attackDir - 36 + (attackTime % 5) * 18 + random.nextInt(7) - 3;
				level.add(new FireSpark(this, Math.cos(Math.toRadians(phi)) * speed, Math.sin(Math.toRadians(phi)) * speed)); // Adds a spark entity with the cosine and sine of dir times speed.
				return; // Skips the rest of the code (attackTime was > 0; ie we're attacking.)
			}

			if (player != null && randomWalkTime == 0) { // If there is a player around, and the walking is not random
				int xd = player.x - x; // The horizontal distance between the player and the Obsidian Knight.
				int yd = player.y - y; // The vertical distance between the player and the Obsidian Knight.
				if (xd * xd + yd * yd < 16 * 16 * 2 * 2) {
					/// Move away from the player if less than 2 blocks away

					this.xmov = 0; // Velocity
					this.ymov = 0;

					// These four statements basically just find which direction is away from the player:
					if (xd < 0) this.xmov = +1;
					if (xd > 0) this.xmov = -1;
					if (yd < 0) this.ymov = +1;
					if (yd > 0) this.ymov = -1;

				} else if (xd * xd + yd * yd > 16 * 16 * 15 * 15) {// 15 squares away
					/// Drags the Obsidian Knight to the player, maintaining relative position.
					double hypot = Math.sqrt(xd * xd + yd * yd);
					int newxd = (int) (xd * Math.sqrt(16 * 16 * 15 * 15) / hypot);
					int newyd = (int) (yd * Math.sqrt(16 * 16 * 15 * 15) / hypot);
					x = player.x - newxd;
					y = player.y - newyd;
				}

				xd = player.x - x; // Recalculate these two
				yd = player.y - y;
				// If a random number, 0-3, equals 0, and the player is less than 50 blocks away, and attackDelay and attackTime equal 0...
				if (random.nextInt(4) == 0 && xd * xd + yd * yd < 50 * 50 && attackDelay == 0 && attackTime == 0) {
					attackDelay = 60 * 2; // ...then set attackDelay to 120 (2 seconds at default 60 ticks/sec)
				}
			}
			// AttackPhase.Walking is handled by Mob.java like normal mob.
		} else if (phase == 1) { // AttackPhase.Dashing is handled here only in second phase. Otherwise, it is handled as same as AttackPhase.Walking.
			if (attackPhase == AttackPhase.Dashing) {
				if (dashCooldown < 1) {
					dashTime = 40;
					dashCooldown = 250;
				} else {
					dashCooldown--;
				}
				if (dashTime == 0) {
					this.speed = 1;
					dashCooldown--; // We want cooldown
				}

				if (dashTime > 0) {
					dashTime--;
					this.speed = 2;
					level.add(new FireSpark(this, 0, 0)); // Fiery trail
				}
			}
		}
	}

	@Override
	public void doHurt(int damage, Direction attackDir) {
		super.doHurt(damage, attackDir);
		if (attackDelay == 0 && attackTime == 0) {
			attackDelay = 60 * 2;
		}
	}

	@Override
	public void render(Screen screen) {
		super.render(screen);

		int textcol = Color.get(1, 0, 204, 0);
		int textcol2 = Color.get(1, 0, 51, 0);
		int percent = health / (maxHealth / 100);
		String h = percent + "%";

		if (percent < 1) h = "1%";

		if (percent < 16) {
			textcol = Color.get(1, 204, 0, 0);
			textcol2 = Color.get(1, 51, 0, 0);
		} else if (percent < 51) {
			textcol = Color.get(1, 204, 204, 9);
			textcol2 = Color.get(1, 51, 51, 0);
		}
		int textwidth = Font.textWidth(h);
		Font.draw(h, screen, (x - textwidth / 2) + 1, y - 17, textcol2);
		Font.draw(h, screen, (x - textwidth / 2), y - 18, textcol);
	}

	@Override
	protected void touchedBy(Entity entity) {
		if (entity instanceof Player) {
			// If the entity is the Player, then deal them 2 damage points.
			((Player) entity).hurt(this, 2);
			if (attackPhase == AttackPhase.Dashing) {
				dashTime = Math.max(dashTime - 10, 0);
			}
		}
	}

	/**
	 * What happens when the obsidian knight dies
	 */
	@Override
	public void die() {
		Player[] players = level.getPlayers();
		if (players.length > 0) { // If the player is still here
			for (Player p : players) {
				p.addScore(300000); // Give the player 300K points.
				dropItem(15, 25, Items.get("shard"));
				dropItem(1, 1, Items.get("Obsidian Heart")); // Drop it's precious item.
			}
		}

		Sound.play("bossdeath");

		//Analytics.AirWizardDeath.ping();
		Updater.notifyAll(Localization.getLocalized("minicraft.notification.obsidian_knight_defeated"));


		// If this is the first time we beat the obsidian knight.
		if (!beaten) {
			AchievementsDisplay.setAchievement("minicraft.achievement.obsidianknight", true);

			//Analytics.FirstAirWizardDeath.ping();
		}

		beaten = true;
		active = false;
		entity = null;

		super.die(); // Calls the die() method in EnemyMob.java
	}

	@Override
	public int calculateEntityDamage(Entity attacker, int damage) {
		if (attacker instanceof Arrow && phase == 0) {
			attacker.remove();
			return 0;
		}

		return super.calculateEntityDamage(attacker, damage);
	}
}
