package com.mojang.ld22.entity.particle;

import com.mojang.ld22.entity.Entity;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.sound.Sound;

public class FireParticle extends Entity {
	/// This is used for Spawners, when they spawn an entity.
	
	private int time = 0; // lifetime elapsed.

	public FireParticle(int x, int y) {
		// make a fire particle at the given coordinates
		this.x = x;
		this.y = y;
		//Sound.monsterHurt.play(); // I think this would play too many sounds.
	}

	public void tick() {
		time++;
		if(time > 30) {
			// fire particles live for 30 ticks.
			remove();
		}

	}

	public void render(Screen screen) {
		int col = Color.get(-1, 520, 550, 500); // orangish-fire color.
		screen.render(x - 8, y - 8, 425, col, 0); // render the particle.
	}
}
