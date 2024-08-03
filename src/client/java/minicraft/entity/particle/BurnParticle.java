package minicraft.entity.particle;

import minicraft.gfx.SpriteLinker;

public class BurnParticle extends Particle {
	/// This is used for Spawners, when they spawn an entity.
	private static final SpriteLinker.LinkedSprite sprite = new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Gui, "hud")
		.setSpriteDim(6, 2, 1, 1)
		.setMirror(3);

	/**
	 * Creates a new particle at the given position. It has a lifetime of 30 ticks
	 * and a fire looking sprite.
	 * @param x X map position
	 * @param y Y map position
	 */
	public BurnParticle(int x, int y) {
		super(x, y, 30, sprite);
	}
}
