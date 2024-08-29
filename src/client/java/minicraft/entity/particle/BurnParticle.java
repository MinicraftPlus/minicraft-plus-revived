package minicraft.entity.particle;

import minicraft.gfx.SpriteManager;

public class BurnParticle extends Particle {
	/// This is used for Spawners, when they spawn an entity.
	private static final SpriteManager.SpriteLink sprite =
		new SpriteManager.SpriteLink.SpriteLinkBuilder(SpriteManager.SpriteType.Gui, "hud")
			.setSpriteDim(6, 2, 1, 1)
			.setMirror(3).createSpriteLink();

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
