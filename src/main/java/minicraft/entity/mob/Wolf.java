package minicraft.entity.mob;

import minicraft.entity.Direction;
import minicraft.gfx.SpriteLinker;
import minicraft.item.FoodItem;
import minicraft.item.Item;
import minicraft.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Wolf extends PassiveMob {
	private static final SpriteLinker.LinkedSprite[][] sprites = Mob.compileMobSpriteAnimations(0, 0, "wolf");
	private static final SpriteLinker.LinkedSprite[][] spritesTamed = Mob.compileMobSpriteAnimations(0, 0, "wolf_tame");

	private static final int WILD_HEALTH_FACTOR = 8;
	private static final int TAMED_HEALTH_FACTOR = 20;
	private Player owner;
//	private DyeColor color = null; TODO Coloring

	public Wolf() { this(null); }
	public Wolf(@Nullable Player owner) {
		super(sprites, owner == null ? WILD_HEALTH_FACTOR : TAMED_HEALTH_FACTOR);
		this.owner = owner;
	}

	@Override
	public boolean interact(Player player, @Nullable Item item, Direction attackDir) {
		if (item != null) {
			if (AirWizard.beaten) { // No curse
				if (item.getName().equals("Bone") && owner == null) {
					if (random.nextInt(3) == 0) { // Successful
						owner = player;
						return true;
					}
				} else if (owner != null) {
					if (item instanceof FoodItem && health < getMaxHealth() &&
						(item.getName().equals("Raw Pork")||item.getName().equals("Cooked Pork")||
							item.getName().equals("Raw Beef")||item.getName().equals("Steak"))) {
						((FoodItem) item).count--;
						health = Math.min(health + 2, getMaxHealth());
						return true;
					}
				}
			}
		}

		return false;
	}

	@Override
	public void handleDespawn() {
		if (owner == null) super.handleDespawn(); // Despawns only when not tamed.
	}
}
