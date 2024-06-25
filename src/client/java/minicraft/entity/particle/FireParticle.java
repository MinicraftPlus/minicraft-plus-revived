package minicraft.entity.particle;

import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.gfx.SpriteLinker.SpriteType;

public class FireParticle extends Particle {
	/// This is used for Spawners, when they spawn an entity.

	/**
	 * Creates a new particle at the given position. It has a lifetime of 30 ticks
	 * and a fire looking sprite.
	 * @param x X map position
	 * @param y Y map position
	 */
	public FireParticle(int x, int y) {
		super(x, y, 30, new LinkedSprite(SpriteType.Gui, "hud").setSpriteDim(4, 2, 1, 1));
	}
}
