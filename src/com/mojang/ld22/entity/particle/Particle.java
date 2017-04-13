package com.mojang.ld22.entity.particle;

import com.mojang.ld22.entity.Entity;
import com.mojang.ld22.gfx.Screen;

public class Particle extends Entity {
	private int time = 0; // lifetime elapsed.
	protected int lifetime;
	protected int color;
	
	public Particle(int x, int y, int lifetime, int color) {
		// make a particle at the given coordinates
		this.x = x;
		this.y = y;
		this.lifetime = lifetime;
		this.color = color;
	}

	public void tick() {
		time++;
		if(time > lifetime) {
			remove();
		}
	}

	public void render(Screen screen) {
		screen.render(x - 8, y - 8, 425, color, 0); // render the particle.
	}
}
