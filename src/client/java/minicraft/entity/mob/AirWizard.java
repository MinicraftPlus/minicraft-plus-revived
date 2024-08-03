package minicraft.entity.mob;

import minicraft.core.Game;
import minicraft.core.Updater;
import minicraft.core.io.Localization;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.Spark;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.item.Items;
import minicraft.network.Analytics;
import minicraft.screen.AchievementsDisplay;

public class AirWizard extends EnemyMob {
	private static final LinkedSprite[][][] sprites = new LinkedSprite[][][] {
		Mob.compileMobSpriteAnimations(0, 0, "air_wizard"),
		Mob.compileMobSpriteAnimations(0, 2, "air_wizard")
	};

	public static boolean beaten = false;
	public static boolean active = false;
	public static AirWizard entity = null;

	private int attackDelay = 0;
	private int attackTime = 0;
	private int attackType = 0;

	/**
	 * This is used by the spawner to spawn air wizards. {@code lvl} is unused.
	 */
	public AirWizard(int lvl) {
		this();
	}

	/**
	 * Constructor for the AirWizard.
	 */
	public AirWizard() {
		super(1, sprites, 2000, false, 16 * 8, -1, 10, 50);

		active = true;
		speed = 2;
		walkTime = 2;
		entity = this;
	}

	@Override
	public void tick() {
		super.tick();

		if (Game.isMode("minicraft.settings.mode.creative")) return; // Should not attack if player is in creative

		if (attackDelay > 0) {
			xmov = ymov = 0;
			int dir = (attackDelay - 45) / 4 % 4; // The direction of attack.
			dir = (dir * 2 % 4) + (dir / 2); // Direction attack changes
			if (attackDelay < 45)
				dir = 0; // Direction is reset, if attackDelay is less than 45; prepping for attack.

			this.dir = Direction.getDirection(dir);

			attackDelay--;
			if (attackDelay == 0) {
				//attackType = 0; // Attack type is set to 0, as the default.
				if (health < maxHealth / 2) attackType = 1; // If at 1000 health (50%) or lower, attackType = 1
				if (health < maxHealth / 10) attackType = 2; // If at 200 health (10%) or lower, attackType = 2
				attackTime = 120; // attackTime set to 120 (2 seconds, at default 60 ticks/sec)
			}
			return; // Skips the rest of the code (attackDelay must have been > 0)
		}

		// Send out sparks
		if (attackTime > 0) {
			xmov = ymov = 0;
			attackTime *= 0.92; // attackTime will decrease by 7% every time.
			double dir = attackTime * 0.25 * (attackTime % 2 * 2 - 1); // Assigns a local direction variable from the attack time.
			double speed = 0.7 + attackType * 0.2; // speed is dependent on the attackType. (higher attackType, faster speeds)
			level.add(new Spark(this, Math.cos(dir) * speed, Math.sin(dir) * speed)); // Adds a spark entity with the cosine and sine of dir times speed.
			return; // Skips the rest of the code (attackTime was > 0; ie we're attacking.)
		}

		Player player = getClosestPlayer();
		if (player != null && randomWalkTime == 0) { // If there is a player around, and the walking is not random
			int xd = player.x - x; // The horizontal distance between the player and the air wizard.
			int yd = player.y - y; // The vertical distance between the player and the air wizard.
			if (xd * xd + yd * yd < 16 * 16 * 2 * 2) {
				/// Move away from the player if less than 2 blocks away

				this.xmov = 0; // Accelerations
				this.ymov = 0;

				// These four statements basically just find which direction is away from the player:
				if (xd < 0) this.xmov = +1;
				if (xd > 0) this.xmov = -1;
				if (yd < 0) this.ymov = +1;
				if (yd > 0) this.ymov = -1;
			} else if (xd * xd + yd * yd > 16 * 16 * 15 * 15) {// 15 squares away

				/// Drags the airwizard to the player, maintaining relative position.
				double hypot = Math.sqrt(xd * xd + yd * yd);
				int newxd = (int) (xd * Math.sqrt(16 * 16 * 15 * 15) / hypot);
				int newyd = (int) (yd * Math.sqrt(16 * 16 * 15 * 15) / hypot);
				x = player.x - newxd;
				y = player.y - newyd;
			}

			xd = player.x - x; // Recalculate these two
			yd = player.y - y;
			if (random.nextInt(4) == 0 && xd * xd + yd * yd < 50 * 50 && attackDelay == 0 && attackTime == 0) { // If a random number, 0-3, equals 0, and the player is less than 50 blocks away, and attackDelay and attackTime equal 0...
				attackDelay = 60 * 2; // ...then set attackDelay to 120 (2 seconds at default 60 ticks/sec)
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
			// If the entity is the Player, then deal them 1 damage points.
			((Player) entity).hurt(this, 1);
		}
	}

	/**
	 * What happens when the air wizard dies
	 */
	@Override
	public void die() {
		Player[] players = level.getPlayers();
		if (players.length > 0) { // If the player is still here
			for (Player p : players) {
				p.addScore(100000); // Give the player 100K points.
				dropItem(5, 10, Items.get("cloud ore")); // Drop cloud ore to guarantee respawn.
			}
		}

		Sound.play("bossdeath");

		Analytics.AirWizardDeath.ping();
		Updater.notifyAll(Localization.getLocalized("minicraft.notification.air_wizard_defeated"));


		// If this is the first time we beat the air wizard.
		if (!beaten) {
			AchievementsDisplay.setAchievement("minicraft.achievement.airwizard", true);

			Analytics.FirstAirWizardDeath.ping();
			Updater.notifyAll("minicraft.notification.dungeon_opened", -400);
		}

		beaten = true;
		active = false;
		entity = null;

		super.die(); // Calls the die() method in EnemyMob.java
	}

	@Override
	public int getMaxLevel() {
		return 2;
	}
}
