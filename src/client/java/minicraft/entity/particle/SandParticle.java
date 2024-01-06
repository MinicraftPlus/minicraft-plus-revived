package minicraft.entity.particle;

import minicraft.gfx.SpriteManager.SpriteLink;
import minicraft.gfx.SpriteManager.SpriteType;

import java.util.Random;

public class SandParticle extends Particle {
	/**
	 * Creating a sand particle.
	 * @param x X map position
	 * @param y Y map position
	 */
	public SandParticle(int x, int y) {
		super(x, y, 180 + new Random().nextInt(71) - 35, null);
		sprite = new SpriteLink.SpriteLinkBuilder(SpriteType.Entity, "sand_dust").setMirror(random.nextInt(4))
			.createSpriteLink();
	}
}
