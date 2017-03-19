package com.mojang.ld22.entity.particle;

import com.mojang.ld22.entity.Entity;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Font;
import com.mojang.ld22.gfx.Screen;

public class TextParticle extends Entity {
	private String msg; // Message of the text particle
	private int col; // Color of the text particle
	private int time = 0; // time (in ticks) that the particle has been alive
	public double xa, ya, za; // x,y,z acceleration
	public double xx, yy, zz; // x,y,z coordinates

	public TextParticle(String msg, int x, int y, int col) {
		this.msg = msg;
		this.x = x;
		this.y = y;
		this.col = col;
		xx = x; //assigns x pos
		yy = y; //assigns y pos
		zz = 2; //assigns z pos to be 2
		
		//assigns x,y,z acceleration:
		xa = random.nextGaussian() * 0.3;
		ya = random.nextGaussian() * 0.2;
		za = random.nextFloat() * 0.7 + 2;
	}

	public void tick() {
		time++;
		if (time > 60) {
			//remove text particle after 60 ticks.
			remove();
		}
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
		//		Font.draw(msg, screen, x - msg.length() * 4, y, Color.get(-1, 0, 0, 0));
		Font.draw(msg, screen, x - msg.length() * 4 + 1, y - (int) (zz) + 1, Color.get(-1, 0, 0, 0)); //renders the backdrop
		Font.draw(msg, screen, x - msg.length() * 4, y - (int) (zz), col); // renders the text
	}
}
