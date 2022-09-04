package minicraft.entity.mob;

import minicraft.core.Game;
import minicraft.core.Updater;
import minicraft.core.io.Localization;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.FireSpark;
import minicraft.entity.furniture.KnightStatue;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.MobSprite;
import minicraft.gfx.Screen;
import minicraft.item.Items;
import minicraft.network.Analytics;
import minicraft.screen.AchievementsDisplay;

public class ObsidianKnight extends EnemyMob {
	private static final MobSprite[][][] armored;
	private static final MobSprite[][][] broken;
	public static boolean beaten = false;
	public static boolean active = false;
	public static boolean failed = false;
	private static boolean phase1 = true;
	private int dashTime = 0;
	private int dashCooldown = 1000;

	static {
		armored = new MobSprite[2][4][2];
		broken = new MobSprite[2][4][2];
		for (int i = 0; i < 2; i++) {
			armored[i] = MobSprite.compileMobSpriteAnimations(0, 16 + (i * 2));
			broken[i] = MobSprite.compileMobSpriteAnimations(0, 18 + (i * 2));
		}
	}


	private int attackDelay = 0;
	private int attackTime = 0;
	private int attackType = 0;
	int ydir=90;
	public static int length;

	/**
	 * Constructor for the ObsidianKnight.
	 */
	public ObsidianKnight(int savHealth) {
		super(1, armored, 5000, false, 16 * 8, -1, 10, 50);

		Updater.notifyAll(Localization.getLocalized("minicraft.notification.obsidian_knight_awoken")); // On spawn tell player.

		failed = false;
		phase1 = true;
		active = true;
		speed = 1;
		walkTime = 3;
		health = savHealth;
	}

	@Override
	public void tick() {
		super.tick();
		if (getClosestPlayer().isRemoved()) {
			failed = true;
			KnightStatue ks = new KnightStatue(health);
			level.add(ks, x, y, false);
			this.remove();
		}

		length = health / (maxHealth / 100);

		//Achieve phase2
		if (health <= 2500) {
			phase1 = false;
		}
		if (!phase1){
			lvlSprites = broken;
		}

		if (Game.isMode("minicraft.settings.mode.creative")) return; // Should not attack if player is in creative
		
		if (phase1) {
			Player player = getClosestPlayer();
			if (attackDelay > 0) {
				xmov = ymov = 0;
				int dir = (attackDelay - 35) / 4 % 4; // The direction of attack.
				dir = (dir * 2 % 4) + (dir / 2); // Direction attack changes
				if (attackDelay < 35) {
					ydir = (player.y < y ? 180 : y-16<player.y && player.y<y+16 ? 0 : 90);
					dir = 0; // Direction is reset, if attackDelay is less than 45; prepping for attack.
				}

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

			if(attackTime==0)ydir=(player.y < y ? 180 : 90);
			// Send out sparks
			if (attackTime > 0) {
				xmov = ymov = 0;
				attackTime *= 0.95; // attackTime will decrease by 4% every time.
				double dir = attackTime * 0.25 * (attackTime % 2 * 2 - 1); // Assigns a local direction variable from the attack time.
				double speed = 1 + attackType * 0.2; // speed is dependent on the attackType. (higher attackType, faster speeds)
				int xdir=45+(random.nextInt(32)-16);
				//xdir+(y-16<player.y && player.y<y+16 ? (player.x<x-8 ? -90 : 0) : (player.x>x+8 ? 90 : 0))
				if(y-16<player.y && player.y<y+16)
					level.add(new FireSpark(this, Math.cos(player.x<x ? 90 : 270) * speed, Math.sin(ydir+(random.nextInt(32)-16)) * speed)); // Adds a spark entity with the cosine and sine of dir times speed.
				else
				level.add(new FireSpark(this, Math.cos(xdir) * speed, Math.sin(ydir) * speed)); // Adds a spark entity with the cosine and sine of dir times speed.
				return; // Skips the rest of the code (attackTime was > 0; ie we're attacking.)
			}


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
		}else{
			if(dashCooldown<1){
				dashTime=120;
				dashCooldown=1000;
			}else{
				dashCooldown--;
			}
			if(dashTime==0){
				this.speed=1;
				dashCooldown--; //we want cooldown
			}

			if(dashTime>0){
				dashTime--;
				this.speed=3;
				level.add(new FireSpark(this, 0, 0)); //fiery trail
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
		}
		else if (percent < 51) {
			textcol = Color.get(1, 204, 204, 9);
			textcol2 = Color.get(1, 51, 51, 0);
		}
		int textwidth = Font.textWidth(h);
		Font.draw(h, screen, (x - textwidth/2) + 1, y - 17, textcol2);
		Font.draw(h, screen, (x - textwidth/2), y - 18, textcol);
	}

	@Override
	protected void touchedBy(Entity entity) {
		if (entity instanceof Player) {
			// If the entity is the Player, then deal them 1 damage points.
			((Player)entity).hurt(this, 3);
		}
	}

	/** What happens when the obsidian knight dies */
	@Override
	public void die() {
		Player[] players = level.getPlayers();
		if (players.length > 0) { // If the player is still here
			for (Player p: players) {
				p.addScore(300000); // Give the player 300K points.
				dropItem(15, 25, Items.get("shard"));
				dropItem(1, 1, Items.get("Obsidian Heart")); // Drop it's precious item.
			}
		}

		Sound.bossDeath.play();

		//Analytics.AirWizardDeath.ping();
		Updater.notifyAll(Localization.getLocalized("minicraft.notification.obsidian_knight_defeated"));


		// If this is the first time we beat the obsidian knight.
		if (!beaten) {
			AchievementsDisplay.setAchievement("minicraft.achievement.obsidianknight", true);

			//Analytics.FirstAirWizardDeath.ping();
		}

		beaten = true;
		active = false;
		KnightStatue.active = false;

		super.die(); // Calls the die() method in EnemyMob.java
	}
}
