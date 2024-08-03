package minicraft.entity.particle;

import minicraft.gfx.SpriteManager.SpriteLink;
import minicraft.gfx.SpriteManager.SpriteType;

public class SmashParticle extends Particle {
	/**
	 * Creates a smash particle at the given position. Has a lifetime of 10 ticks.
	 * Will also play a monsterhurt sound when created.
	 * @param x X map position
	 * @param y Y map position
	 */
	public SmashParticle(int x, int y) {
		super(x, y, 10, new SpriteLink.SpriteLinkBuilder(SpriteType.Entity, "smash").createSpriteLink());
	}
}
