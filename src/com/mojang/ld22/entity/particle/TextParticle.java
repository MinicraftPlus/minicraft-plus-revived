package com.mojang.ld22.entity.particle;

import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Font;
import com.mojang.ld22.gfx.Screen;

public class TextParticle extends Particle {
	private String msg; // Message of the text particle
	public double xa, ya, za; // x,y,z acceleration
	public double xx, yy, zz; // x,y,z coordinates

	public TextParticle(String msg, int x, int y, int col) {
		super(x, y, 60, col);
		
		this.msg = msg;
		xx = x; //assigns x pos
		yy = y; //assigns y pos
		zz = 2; //assigns z pos to be 2
		
		//assigns x,y,z acceleration:
		xa = random.nextGaussian() * 0.3;
		ya = random.nextGaussian() * 0.2;
		za = random.nextFloat() * 0.7 + 2;
	}

	public void tick() {
		super.tick();
		
		//move the particle according to the acceleration
		xx += xa;
		yy += ya;
		zz += za;
		if (zz < 0) {
			//if z pos if less than 0, alter accelerations...
			zz = 0;
			za *= -0.5;
			xa *= 0.6;
			ya *= 0.6;
		}
		za -= 0.15;  // za decreases by 0.15 every tick.
		//truncate x and y coordinates to integers:
		x = (int) xx;
		y = (int) yy;
	}

	public void render(Screen screen) {
		if(!msg.contains("Thanks")) {
			Font.draw(msg, screen, x - msg.length() * 4 + 1, y - (int)zz + 1, Color.get(-1, 0, 0, 0)); //renders the shadow
			Font.draw(msg, screen, x - msg.length() * 4, y - (int)zz, color);
		} else { // special, for "Thanks for Playing!" message?
			String msg1 = msg.substring(0, 19);
			String msg2 = msg.substring(19, msg.length());
			Font.draw(msg1, screen, x - msg.length() * 4 + 1, y - (int)zz - 7, Color.get(-1, 0, 0, 0));
			Font.draw(msg1, screen, x - msg.length() * 4, y - (int)zz - 8, color);
			Font.draw(msg2, screen, x - msg.length() * 4 + 1, y - (int)zz + 1, Color.get(-1, 0, 0, 0));
			Font.draw(msg2, screen, x - msg.length() * 4, y - (int)zz, color);
		}
	}
}
