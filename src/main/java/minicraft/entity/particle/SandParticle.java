package minicraft.entity.particle;

import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.gfx.SpriteLinker.SpriteType;

import java.util.Random;

public class SandParticle extends Particle {
	/**
	 * Creating a sand particle.
	 * @param x X map position
	 * @param y Y map position
	 */
	public SandParticle(int x, int y) {
		super(x, y, 180 + new Random().nextInt(71) - 35, new LinkedSprite(SpriteType.Entity, "sand_dust"));
		this.sprite.setMirror(random.nextInt(4));
	}
}
