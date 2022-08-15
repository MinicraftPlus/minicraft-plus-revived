package minicraft.entity.furniture;

import minicraft.core.Game;
import minicraft.core.io.Localization;
import minicraft.entity.Direction;
import minicraft.entity.mob.ObsidianKnight;
import minicraft.entity.mob.Player;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.item.Item;

public class KnightStatue extends Furniture {

	private boolean touched = false;
	public int obkHealth;
	public static boolean active;

	public KnightStatue(int health) {
		super("KnightStatue", new Sprite(8, 16, 2, 2, 2), 3, 2);
		ObsidianKnight.active = false;
		ObsidianKnight.failed = false;
		this.obkHealth = health;
		active = true;
	}

	@Override
	public void tick() {
		super.tick();

		if (!ObsidianKnight.active) {
			if (touched) {
				// Summon the Obsidian Knight boss
				ObsidianKnight ob = new ObsidianKnight(obkHealth);
				level.add(ob, x, y, false);
				super.remove();
			}
		}
		else {
			Game.notifications.add(Localization.getLocalized("minicraft.notification.boss_limit"));
		}
	}

	@Override
	public void render(Screen screen) {
		super.render(screen);
	}

	@Override
	public boolean interact(Player player, Item heldItem, Direction attackDir) {
		if (!touched) {
			touched = true;
			return true;
		}
		return false;
	}
}

