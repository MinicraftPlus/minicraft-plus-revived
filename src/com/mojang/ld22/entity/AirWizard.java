package com.mojang.ld22.entity;

import com.mojang.ld22.Game;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Font;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.screen.OptionsMenu;
import com.mojang.ld22.sound.Sound;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class AirWizard extends Mob {
	private int xa, ya; // x & y acceleration
	private int randomWalkTime = 0; // time it takes for him to complete walking
	private int attackDelay = 0;
	private int attackTime = 0;
	private int attackType = 0;
	//public int healthstat;
	public static boolean beaten = false;
	public boolean secondform = false;
	Random r = new Random();
	String location = Game.gameDir;
	File folder;
	
	public AirWizard(boolean secondform) {
		folder = new File(location);
		
		x = random.nextInt(64 * 16); // x position is anywhere between (0 to 1023) [Tile position (0 to 64)]
		y = random.nextInt(64 * 16); // y position is anywhere between (0 to 1023) [Tile position (0 to 64)]
		
		if(secondform) {
			this.secondform = true;
			
			col = col1 = col2 = col3 = Color.get(-1, 0, 4, 46);
			col4 = Color.get(-1, 0, 1, 32);
			
			health = maxHealth = 5000;
		} else {
			col0 = col1 = Color.get(-1, 100, 500, 555);
			col2 = col3 = col4 = Color.get(-1, 0, 200, 333);
			
			health = maxHealth = 2000;
		}
	}
	
	public boolean canSwim() {
		return secondform;
	}
	
	public void tick() {
		super.tick(); // ticks the Entity.java part of this class
		
		isenemy = true;
		
		//healthstat = health;
		
		if (attackDelay > 0) {
			dir = (attackDelay - 45) / 4 % 4; // the direction of attack.
			dir = (dir * 2 % 4) + (dir / 2); // direction attack changes
			if (attackDelay < 45) {
				dir = 0; // direction is reset, if attackDelay is less than 45
			}
			attackDelay--;
			if (attackDelay == 0) {
				//attackType = 0; // attack type is set to 0, as the default.
				if (health < maxHealth / 2) attackType = 1; // if at 1000 health (50%) or lower, attackType = 1
				if (health < maxHealth / 10) attackType = 2; // if at 200 health (10%) or lower, attackType = 2
				attackTime = 60 * (secondform ? 3 : 2); //attackTime set to 120 or 180 (2 or 3 seconds, at default 60 ticks/sec)
			}
			return; // skips the rest of the code (attackDelay must have been < 0)
		}
		
		if (attackTime > 0) {
			attackTime--; // attackTime will decrease by 1.
			double dir = attackTime * 0.25 * (attackTime % 2 * 2 - 1); //assigns a local direction variable from the attack time.
			double speed = (secondform ? 1.2 : 0.7) + attackType * 0.2; // speed is dependent on the attackType. (higher attackType, faster speeds)
			level.add(new Spark(this, Math.cos(dir) * speed, Math.sin(dir) * speed)); // adds a spark entity with the cosine and sine of dir times speed.
			return; // skips the rest of the code (attackTime was > 0; ie we're attacking.)
		}

		if (level.player != null && randomWalkTime == 0) { // if there is a player around, and the randomWalkTime is equal to 0
			int xd = level.player.x - x; // the horizontal distance between the player and the air wizard.
			int yd = level.player.y - y; // the vertical distance between the player and the air wizard.
			if (xd * xd + yd * yd < 16*16 * 2*2) { // if actual dist to player is less than 32...(using pythag theorem)
				/* Move away from the player */
				xa = 0; //accelerations
				ya = 0;
				// these four statements basically just find which direction is away from the player:
				if (xd < 0) xa = +1;
				if (xd > 0) xa = -1;
				if (yd < 0) ya = +1;
				if (yd > 0) ya = -1;
			} else if (xd * xd + yd * yd > 16*16 * 15*15) {// 15 squares away
				// somehow just find way to shorten vector...
				double hypot = Math.sqrt(xd*xd + yd*yd);
				int newxd = (int)(xd * Math.sqrt(16*16 * 15*15) / hypot);
				int newyd = (int)(yd * Math.sqrt(16*16 * 15*15) / hypot);
				x = level.player.x - newxd;
				y = level.player.y - newyd;
			} else if (xd * xd + yd * yd > 16*16 * 5*5) { // if dist to player is greater than 80...
				/* Move towards the player */
				xa = 0;
				ya = 0;
				// opposite effect as above; says which way is toward the player
				if (xd < 0) xa = -1;
				if (xd > 0) xa = +1;
				if (yd < 0) ya = -1;
				if (yd > 0) ya = +1;
			}
		}

		int speed = (tickTime % (secondform ? 2 : 4)) == 0 ? 0 : 1; // if the remainder of tickTime/4 equals 0, then set speed to 0, else set it to 1.
		if (!move(xa * speed, ya * speed) || random.nextInt(100) == 0) { // If the air wizard is not moving & a random value (0 to 99) equals 0...
			randomWalkTime = 30;
			// set x and y accelerations:
			xa = (random.nextInt(3) - 1);
			ya = (random.nextInt(3) - 1);
		}
		if (randomWalkTime > 0) { // if randomWalkTime is greater than 0...
			randomWalkTime--; // subtract 1 from it
			// ...Very similar to above...
			if (level.player != null && randomWalkTime == 0) {
				int xd = level.player.x - x; // x dist to player
				int yd = level.player.y - y; // y dist to player
				if (random.nextInt(4) == 0 && xd * xd + yd * yd < 50 * 50) { // if a random number, 0-3, equals 0, and the player is less than 50 blocks away...
					if (attackDelay == 0 && attackTime == 0) { // ...and attackDelay and attackTime equal 0...
						attackDelay = 60 * 2; // ...then set attackDelay to 120 (2 seconds at default 60 ticks/sec)
					}
				}
			}
		}
	}
	
	// new method...
	protected void doHurt(int damage, int attackDir) {
		super.doHurt(damage, attackDir);
		if (attackDelay == 0 && attackTime == 0) {
			attackDelay = 60 * 2;
		}
	}
	
	/** Renders the air wizard on the screen */
	public void render(Screen screen) {
		int xt = 8; // x coordinate on the sprite sheet
		int yt = 14; // y coordinate on the sprite sheet
		
		 // animation flip values (used to mirror the sprite).
		int flip1 = (walkDist >> 3) & 1;
		int flip2 = (walkDist >> 3) & 1;

		if (dir == 1) {
			xt += 2; // moves the sprite coordinate 2 tiles to the right (16 pixels over)
		}
		if (dir > 1) {

			flip1 = 0; // flip1 becomes 0 (don't mirror)
			flip2 = ((walkDist >> 4) & 1); // changes bottom half of sprite (mirror)
			if (dir == 2) {
				flip1 = 1; // flip1 becomes 1 (mirrored) if the direction is 2.
			}
			xt += 4 + ((walkDist >> 3) & 1) * 2; // changes bottom half of sprite (in the sprite sheet)
		}

		int xo = x - 8; // the horizontal location to start drawing the sprite
		int yo = y - 11; // the vertical location to start drawing the sprite
		int col1 = secondform ? Color.get(-1, 0, 2, 46) : Color.get(-1, 100, 500, 555); // top half color
		int col2 = secondform ? Color.get(-1, 0, 2, 46) : Color.get(-1, 100, 500, 532); // bottom half color
		
		if (attackType == 1 && tickTime / 5 % 4 == 0 || attackType == 2 && tickTime / 3 % 2 == 0) {
				// change colors.
				col1 = secondform ? Color.get(-1, 2, 0, 46) : Color.get(-1, 500, 100, 555);
				col2 = secondform ? Color.get(-1, 2, 0, 46) : Color.get(-1, 500, 100, 532);
		}/* else if () { // ...and tickTime is divisible by 20...
				// ...change colors... in the same way.
				col1 = Color.get(-1, 500, 100, 555);
				col2 = Color.get(-1, 500, 100, 532);
		}*/
		
		if (hurtTime > 0) { //if the air wizards hurt time is above 0... (hurtTime value in Mob.java)
			// turn the sprite white, momentarily.
			col1 = Color.get(-1, 555, 555, 555);
			col2 = Color.get(-1, 555, 555, 555);
		}
		
		/* render call format:
		screen.render(int x position, int y-position, int sprite-location, int colors, int bits (0 = not mirrored, 1 = mirrored)) */
		screen.render(xo + 8 * flip1, yo + 0, xt + yt * 32, col1, flip1); // render top-right
		screen.render(xo + 8 - 8 * flip1, yo + 0, xt + 1 + yt * 32, col1, flip1); // top-left
		screen.render(xo + 8 * flip2, yo + 8, xt + (yt + 1) * 32, col2, flip2); // bottom-right
		screen.render(xo + 8 - 8 * flip2, yo + 8, xt + 1 + (yt + 1) * 32, col2, flip2); // bottom-left
		
		int textcol = Color.get(-1, 40, 40, 40);
		int textcol2 = Color.get(-1, 10, 10, 10);
		int percent = health / (maxHealth / 100);
		String h = percent + "%";
		
		if(percent < 1) h = "1%";
		
		if(percent < 16) {
			textcol = Color.get(-1, 400, 400, 400);
			textcol2 = Color.get(-1, 100, 100, 100);
		}
		else if(percent < 51) {
			textcol = Color.get(-1, 440, 440, 440);
			textcol2 = Color.get(-1, 110, 110, 110);
		}
		
		Font.draw(h, screen, x - h.length() / 2 + 1, y - 17, textcol2);
		Font.draw(h, screen, x - h.length() / 2, y - 18, textcol);
	}
	
	/** What happens when the player (or any entity) touches the air wizard */
	protected void touchedBy(Entity entity) {
		if (entity instanceof Player) {
			// if the entity is the Player, then deal them 1 or 2 damage points.
			entity.hurt(this, (secondform ? 2 : 1), dir);
		}
	}
	
	/** What happens when the air wizard dies */
	protected void die() {
		super.die(); // calls the die() method in Mob.java
		if (level.player != null) { // if the player is still here
			level.player.score += (secondform ? 500000 : 100000); // give the player 100K or 500K points.
		}
		
		Sound.bossdeath.play(); // play boss-death sound.
		
		if(!secondform) {
			if (!beaten) Game.notifications.add("The Dungeon is now open!");
			Game.notifications.add("Air Wizard: Defeated!");
			level.player.game.notetick = -500;
			beaten = true;
		} else {
			if (!OptionsMenu.unlockedskin) Game.notifications.add("A costume lies on the ground...");
			Game.notifications.add("Air Wizard II: Defeated!");
			level.player.game.notetick = -200;
			OptionsMenu.unlockedskin = true;
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
	}
}
