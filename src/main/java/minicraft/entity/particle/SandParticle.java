package minicraft.entity.particle;

import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.gfx.SpriteLinker.SpriteType;

public class SandParticle extends Particle {
	/**
	 * Creating a sand particle.
	 * @param x X map position
	 * @param y Y map position
	 */
	public SandParticle(int x, int y) {
		super(x, y, 180, new LinkedSprite(SpriteType.Entity, "sand_dust"));
	}
}
