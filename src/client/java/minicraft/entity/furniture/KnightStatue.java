package minicraft.entity.furniture;

import minicraft.core.Game;
import minicraft.core.io.Localization;
import minicraft.entity.Direction;
import minicraft.entity.mob.ObsidianKnight;
import minicraft.entity.mob.Player;
import minicraft.gfx.SpriteLinker;
import minicraft.item.Item;
import org.jetbrains.annotations.NotNull;

public class KnightStatue extends Furniture {
	private int touches = 0; // >= 0
	private final int bossHealth;

	public KnightStatue(int health) {
		super("KnightStatue", new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Entity, "knight_statue"), new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Item, "knight_statue"), 3, 2);
		bossHealth = health;
	}

	@Override
	public boolean interact(Player player, Item heldItem, Direction attackDir) {
		if (!ObsidianKnight.active) {
			if (touches == 0) { // Touched the first time.
				Game.notifications.add(Localization.getLocalized("minicraft.notifications.statue_tapped"));
				touches++;
			} else if (touches == 1) { // Touched the second time.
				Game.notifications.add(Localization.getLocalized("minicraft.notifications.statue_touched"));
				touches++;
			} else { // Touched the third time.
				// Awoken notifications is in Boss class
				// Summon the Obsidian Knight boss
				ObsidianKnight obk = new ObsidianKnight(bossHealth);
				level.add(obk, x, y, false);
				super.remove(); // Removing this statue.
			}

			return true;
		} else { // The boss is active.
			Game.notifications.add(Localization.getLocalized("minicraft.notification.boss_limit"));
			return false;
		}
	}

	@Override
	public void tryPush(Player player) {
	} // Nothing happens.

	@Override
	public @NotNull Furniture copy() {
		return new KnightStatue(bossHealth);
	}

	public int getBossHealth() {
		return bossHealth;
	}
}

