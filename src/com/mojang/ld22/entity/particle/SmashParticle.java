package com.mojang.ld22.entity.particle;

import com.mojang.ld22.entity.Entity;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.sound.Sound;

public class SmashParticle extends Entity {
	private int time = 0; // the time that the particle is on screen

	public SmashParticle(int x, int y) {
		this.x = x; // assigns the x position of the particle
		this.y = y; // assigns the y position of the particle
		Sound.monsterHurt.play(); // plays the sound of a monster getting hit.
	}
	
	/// Essentially, removes the particle after 10 ticks.
	public void tick() {
		time++;
		if (time > 10) {
			remove();
		}
	}

	public void render(Screen screen) {
		int col = Color.get(-1, 555, 555, 555); // color of the particle (white)
		
		// renders each corner...
		screen.render(x - 8, y - 8, 5 + 12 * 32, col, 2); //top-left
		screen.render(x - 0, y - 8, 5 + 12 * 32, col, 3); //top-right
		screen.render(x - 8, y - 0, 5 + 12 * 32, col, 0); //bottom-left
		screen.render(x - 0, y - 0, 5 + 12 * 32, col, 1); //bottom-right
	}
}
