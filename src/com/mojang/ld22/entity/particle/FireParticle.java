package com.mojang.ld22.entity.particle;

import com.mojang.ld22.entity.particle.Particle;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.sound.Sound;

public class FireParticle extends Particle {
	/// This is used for Spawners, when they spawn an entity.
	
	public FireParticle(int x, int y) {
		super(x, y, 30, Color.get(-1, 520, 550, 500));
	}
}
