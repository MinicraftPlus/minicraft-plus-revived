package minicraft.entity.particle;

import minicraft.gfx.Color;

public class FireParticle extends Particle {
	/// This is used for Spawners, when they spawn an entity.
	
	public FireParticle(int x, int y) {
		super(x, y, 30, Color.get(-1, 520, 550, 500));
	}
}
