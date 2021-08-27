package minicraft.entity.particle;

import minicraft.gfx.Sprite;

public class SmashParticle extends Particle {
	static int[][] mirrors = {{2, 3}, {0, 1}};
	
	/**
	 * Creates a smash particle at the given position. Has a lifetime of 10 ticks.
	 * Will also play a monsterhurt sound when created.
	 * 
	 * @param x X map position
	 * @param y Y map position
	 */
	public SmashParticle(int x, int y) {
		super(x, y, 10, new Sprite(3, 3, 2, 2, 3, true, mirrors));
	}
}
