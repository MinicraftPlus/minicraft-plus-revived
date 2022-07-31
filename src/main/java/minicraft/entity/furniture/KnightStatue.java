package minicraft.entity.furniture;

import minicraft.entity.Direction;
import minicraft.entity.mob.ObsidianKnight;
import minicraft.entity.mob.Player;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.item.Item;

public class KnightStatue extends Furniture {

	private boolean touched = false;

	public KnightStatue() {
		super("KnightStatue", new Sprite(8, 16, 2, 2, 2), 3, 2);
	}

	@Override
	public void tick() {
		super.tick();

		if (touched) {
			// Summon the Obsidian Knight boss
			ObsidianKnight ob = new ObsidianKnight();
			level.add(ob, x, y, false);
			super.remove();
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

