package minicraft.entity.mob;

import minicraft.gfx.SpriteLinker;

public class Chicken extends PassiveMob {
	private static final SpriteLinker.LinkedSprite[][] sprites = Mob.compileMobSpriteAnimations(0, 0, "chicken");

	public Chicken() {
		super(sprites);
	}
}
