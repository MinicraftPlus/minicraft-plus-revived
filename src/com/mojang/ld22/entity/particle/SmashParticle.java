package com.mojang.ld22.entity.particle;

import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.sound.Sound;

public class SmashParticle extends Particle {
	
	public SmashParticle(int x, int y) {
		super(x, y, 10, Color.get(-1, 555, 555, 555));
		Sound.monsterHurt.play(); // plays the sound of a monster getting hit.
	}
	
	public void render(Screen screen) {
		// renders each corner from the same sprite.
		screen.render(x - 8, y - 8, 5 + 12 * 32, color, 2); //top-left
		screen.render(x - 0, y - 8, 5 + 12 * 32, color, 3); //top-right
		screen.render(x - 8, y - 0, 5 + 12 * 32, color, 0); //bottom-left
		screen.render(x - 0, y - 0, 5 + 12 * 32, color, 1); //bottom-right
	}
}
