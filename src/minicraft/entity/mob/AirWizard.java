package minicraft.entity.mob;

import minicraft.core.Updater;
import minicraft.core.io.Settings;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.Spark;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.MobSprite;
import minicraft.gfx.Screen;
import minicraft.saveload.Save;

public class AirWizard extends EnemyMob {
	private static MobSprite[][][] sprites;
	static {
		sprites = new MobSprite[2][4][2];
		for (int i = 0; i < 2; i++) {
			MobSprite[][] list  = MobSprite.compileMobSpriteAnimations(8, 20 + (i * 2));
			sprites[i] = list;
		}
	}
	
	public static boolean beaten = false;
	
	public boolean secondform;
	private int attackDelay = 0;
	private int attackTime = 0;
	private int attackType = 0;
	
	/**
	 * Constructor for the AirWizard. Will spawn as secondary form if lvl>1.
	 * @param lvl The AirWizard level.
	 */
	public AirWizard(int lvl) { 
		this(lvl>1);
	}
	
	/**
	 * Constructor for the AirWizard.
	 * @param secondform determines if the wizard should be level 2 or 1.
	 */
	public AirWizard(boolean secondform) {
		super(secondform?2:1, sprites, secondform?5000:2000, false, 16*8, -1, 10, 50);
		
		this.secondform = secondform;
		if(secondform) speed = 3;
		else speed = 2;
		walkTime = 2;
	}
	
	public boolean canSwim() { return secondform; }
	
	public boolean canWool() { return false; }
	
	@Override
	public void tick() {
		super.tick();
		
		if (attackDelay > 0) {
			xmov = ymov = 0;
			int dir = (attackDelay - 45) / 4 % 4; // the direction of attack.
			dir = (dir * 2 % 4) + (dir / 2); // direction attack changes
			if (attackDelay < 45)
				dir = 0; // direction is reset, if attackDelay is less than 45; prepping for attack.
			
			this.dir = Direction.getDirection(dir);
			
			attackDelay--;
			if (attackDelay == 0) {
				//attackType = 0; // attack type is set to 0, as the default.
				if (health < maxHealth / 2) attackType = 1; // if at 1000 health (50%) or lower, attackType = 1
				if (health < maxHealth / 10) attackType = 2; // if at 200 health (10%) or lower, attackType = 2
				attackTime = 60 * (secondform ? 3 : 2); //attackTime set to 120 or 180 (2 or 3 seconds, at default 60 ticks/sec)
			}
			return; // skips the rest of the code (attackDelay must have been > 0)
		}
		
		if (attackTime > 0) {
			xmov = ymov = 0;
			attackTime *= 0.92; // attackTime will decrease by 7% every time.
			double dir = attackTime * 0.25 * (attackTime % 2 * 2 - 1); //assigns a local direction variable from the attack time.
			double speed = (secondform ? 1.2 : 0.7) + attackType * 0.2; // speed is dependent on the attackType. (higher attackType, faster speeds)
			level.add(new Spark(this, Math.cos(dir) * speed, Math.sin(dir) * speed)); // adds a spark entity with the cosine and sine of dir times speed.
			return; // skips the rest of the code (attackTime was > 0; ie we're attacking.)
		}
		
		Player player = getClosestPlayer();
		
		if (player != null && randomWalkTime == 0) { // if there is a player around, and the walking is not random
			int xd = player.x - x; // the horizontal distance between the player and the air wizard.
			int yd = player.y - y; // the vertical distance between the player and the air wizard.
			if (xd * xd + yd * yd < 16*16 * 2*2) {
				/// Move away from the player if less than 2 blocks away
				
				this.xmov = 0; //accelerations
				this.ymov = 0;
				// these four statements basically just find which direction is away from the player:
				if (xd < 0) this.xmov = +1;
				if (xd > 0) this.xmov = -1;
				if (yd < 0) this.ymov = +1;
				if (yd > 0) this.ymov = -1;
			} else if (xd * xd + yd * yd > 16*16 * 15*15) {// 15 squares away
				/// drags the airwizard to the player, maintaining relative position.
				double hypot = Math.sqrt(xd*xd + yd*yd);
				int newxd = (int)(xd * Math.sqrt(16*16 * 15*15) / hypot);
				int newyd = (int)(yd * Math.sqrt(16*16 * 15*15) / hypot);
				x = player.x - newxd;
				y = player.y - newyd;
			}
		}
		
		if (player != null && randomWalkTime == 0) {
			int xd = player.x - x; // x dist to player
			int yd = player.y - y; // y dist to player
			if (random.nextInt(4) == 0 && xd * xd + yd * yd < 50 * 50 && attackDelay == 0 && attackTime == 0) { // if a random number, 0-3, equals 0, and the player is less than 50 blocks away, and attackDelay and attackTime equal 0...
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
		
		if(percent < 1) h = "1%";
		
		if(percent < 16) {
			textcol = Color.get(1, 204, 0, 0);
			textcol2 = Color.get(1, 51, 0, 0);
		}
		else if(percent < 51) {
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
			// if the entity is the Player, then deal them 1 or 2 damage points.
			((Player)entity).hurt(this, (secondform ? 2 : 1));
		}
	}
	
	/** What happens when the air wizard dies */
	public void die() {
		Player[] players = level.getPlayers();
		if (players.length > 0) { // if the player is still here
			for(Player p: players)
				p.addScore((secondform ? 500000 : 100000)); // give the player 100K or 500K points.
		}
		
		Sound.bossDeath.play(); // play boss-death sound.
		
		if(!secondform) {
			Updater.notifyAll("Air Wizard: Defeated!");
			if (!beaten) Updater.notifyAll("The Dungeon is now open!", -400);
			beaten = true;
		} else {
			Updater.notifyAll("Air Wizard II: Defeated!");
			if (!(boolean)Settings.get("unlockedskin")) Updater.notifyAll("A costume lies on the ground...", -200);
			Settings.set("unlockedskin", true);
			new Save();
		}
		
		super.die(); // calls the die() method in EnemyMob.java
	}
	
	public int getMaxLevel() { return 2; }
}
