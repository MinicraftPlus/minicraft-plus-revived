package minicraft.entity.particle;

import minicraft.gfx.Color;
import minicraft.gfx.Sprite;

public class FireParticle extends Particle {
	/// This is used for Spawners, when they spawn an entity.
	
	public FireParticle(int x, int y) {
		super(x, y, 30, new Sprite(9, 19, Color.get(-1, 520, 550, 500)));
	}
}
