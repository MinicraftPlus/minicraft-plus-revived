package minicraft.entity.mob;

import minicraft.gfx.SpriteLinker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Wolf extends PassiveMob {
	private static final SpriteLinker.LinkedSprite[][] sprites = Mob.compileMobSpriteAnimations(0, 0, "wolf");
	private static final SpriteLinker.LinkedSprite[][] spritesTamed = Mob.compileMobSpriteAnimations(0, 0, "wolf_tame")

	private static final int WILD_HEALTH_FACTOR = 8;
	private static final int TAMED_HEALTH_FACTOR = 20;
	private Player owner;
//	private DyeColor color = null; TODO Coloring

	public Wolf() { this(null); }
	public Wolf(@Nullable Player owner) {
		super(sprites, owner == null ? WILD_HEALTH_FACTOR : TAMED_HEALTH_FACTOR);
		this.owner = owner;
	}

	/**
	 * Attempts to tame the wolf.
	 * @return {@code true} if taming is successful, {@code false} otherwise.
	 * {@code null} is returned if the wolf is not tamable, i.e. already tamed.
	 */
	@Nullable
	public Boolean tryTame(@NotNull Player player) {
		if (owner != null) return null; // Invalid action
		if (random.nextInt(3) == 0) { // Successful
			owner = player;
			return true;
		} else return false;
	}
}
