package minicraft.entity;

import minicraft.Settings;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import minicraft.Game;
import minicraft.Sound;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.MobSprite;
import minicraft.gfx.Screen;

public class AirWizard extends EnemyMob {
	private static MobSprite[][] sprites = MobSprite.compileMobSpriteAnimations(8, 14);
	
	public static boolean beaten = false;
	
	public boolean secondform;
	private int attackDelay = 0;
	private int attackTime = 0;
	private int attackType = 0;
	String location = Game.gameDir;
	File folder;
	
	public AirWizard(int lvl) { this(lvl>1); }
	public AirWizard(boolean secondform) {
		super(secondform?2:1, sprites, (new int[2]), secondform?5000:2000, false, 16*8, 10, 50);
		
		this.secondform = secondform;
		if(secondform) speed = 3;
		else speed = 2;
		walkTime = 2;
		
		folder = new File(location);
		
		lvlcols[0] = secondform ? Color.get(-1, 0, 2, 46) : Color.get(-1, 100, 500, 555); // top half color
		lvlcols[1] = secondform ? Color.get(-1, 0, 2, 46) : Color.get(-1, 100, 500, 532); // bottom half color
		col = lvlcols[lvl-1];
	}
	
	public boolean canSwim() {
		return secondform;
	}
	
	public boolean canWool() {
		return false;
	}
	
	public void tick() {
		super.tick();
		//if(secondform) super.tick(); // double speed for lvl 2
		
		if (attackDelay > 0) {
			xa = ya = 0;
			dir = (attackDelay - 45) / 4 % 4; // the direction of attack.
			dir = (dir * 2 % 4) + (dir / 2); // direction attack changes
			if (attackDelay < 45) {
				dir = 0; // direction is reset, if attackDelay is less than 45; preping for attack.
			}
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
			xa = ya = 0;
			attackTime--; // attackTime will decrease by 1.
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
				
				xa = 0; //accelerations
				ya = 0;
				// these four statements basically just find which direction is away from the player:
				if (xd < 0) xa = +1;
				if (xd > 0) xa = -1;
				if (yd < 0) ya = +1;
				if (yd > 0) ya = -1;
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
	
	public void doHurt(int damage, int attackDir) {
		super.doHurt(damage, attackDir);
		if (attackDelay == 0 && attackTime == 0) {
			attackDelay = 60 * 2;
		}
	}
	
	/** Renders the air wizard on the screen */
	public void render(Screen screen) {
		int xo = x - 8; // the horizontal location to start drawing the sprite
		int yo = y - 11; // the vertical location to start drawing the sprite
		
		int col1 = secondform ? Color.get(-1, 0, 2, 46) : Color.get(-1, 100, 500, 555); // top half color
		int col2 = secondform ? Color.get(-1, 0, 2, 46) : Color.get(-1, 100, 500, 532); // bottom half color
		
		if (attackType == 1 && tickTime / 5 % 4 == 0 || attackType == 2 && tickTime / 3 % 2 == 0) {
				// change colors.
				col1 = secondform ? Color.get(-1, 2, 0, 46) : Color.get(-1, 500, 100, 555);
				col2 = secondform ? Color.get(-1, 2, 0, 46) : Color.get(-1, 500, 100, 532);
		}
		
		if (hurtTime > 0) { //if the air wizards hurt time is above 0... (hurtTime value in Mob.java)
			// turn the sprite white, momentarily.
			col1 = Color.WHITE;
			col2 = Color.WHITE;
		}
		
		MobSprite curSprite = sprites[dir][(walkDist >> 3) & 1];
		curSprite.renderRow(0, screen, xo, yo, col1);
		curSprite.renderRow(1, screen, xo, yo+8, col2);
		
		int textcol = Color.get(-1, 40);
		int textcol2 = Color.get(-1, 10);
		int percent = health / (maxHealth / 100);
		String h = percent + "%";
		
		if(percent < 1) h = "1%";
		
		if(percent < 16) {
			textcol = Color.get(-1, 400);
			textcol2 = Color.get(-1, 100);
		}
		else if(percent < 51) {
			textcol = Color.get(-1, 440);
			textcol2 = Color.get(-1, 110);
		}
		int textwidth = Font.textWidth(h);
		Font.draw(h, screen, (x - textwidth/2) + 1, y - 17, textcol2);
		Font.draw(h, screen, (x - textwidth/2), y - 18, textcol);
	}
	
	/** What happens when the player (or any entity) touches the air wizard */
	protected void touchedBy(Entity entity) {
		if (entity instanceof Player) {
			// if the entity is the Player, then deal them 1 or 2 damage points.
			entity.hurt(this, (secondform ? 2 : 1), Mob.getAttackDir(this, entity));
		}
	}
	
	/** What happens when the air wizard dies */
	protected void die() {
		Player[] players = level.getPlayers();
		if (players.length > 0) { // if the player is still here
			for(Player p: players)
				p.score += (secondform ? 500000 : 100000); // give the player 100K or 500K points.
		}
		
		Sound.bossDeath.play(); // play boss-death sound.
		
		if(!secondform) {
			Game.notifyAll("Air Wizard: Defeated!");
			if (!beaten) Game.notifyAll("The Dungeon is now open!", -400);
			beaten = true;
		} else {
			Game.notifyAll("Air Wizard II: Defeated!");
			if (!(boolean)Settings.get("wear suit")) Game.notifyAll("A costume lies on the ground...", -200);
			Settings.set("wear suit", true);
			BufferedWriter bufferedWriter = null;
			
			try {
				bufferedWriter = new BufferedWriter(new FileWriter(location + "/unlocks.miniplussave", true));
				bufferedWriter.write("AirSkin");
			} catch (IOException ex) {
				ex.printStackTrace();
			} finally {
				try {
					if(bufferedWriter != null) {
						bufferedWriter.flush();
						bufferedWriter.close();
					}
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
		
		super.die(); // calls the die() method in EnemyMob.java
	}
	
	public int getMaxLevel() {
		return 2;
	}
}
