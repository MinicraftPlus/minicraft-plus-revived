package minicraft.entity.furniture;

import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.SpriteLinker;
import minicraft.item.ItemStack;
import minicraft.item.Items;
import org.jetbrains.annotations.Nullable;

public class Composter extends Furniture {
	private static final SpriteLinker.LinkedSprite sprite = new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Entity, "composter");
	private static final SpriteLinker.LinkedSprite spriteFilled = new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Entity, "composter_filled");
	private static final SpriteLinker.LinkedSprite spriteFull = new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Entity, "composter_full");
	private static final SpriteLinker.LinkedSprite itemSprite = new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Item, "composter");

	private static final int MAX_COMPOST = 7;
	private int compost = 0;

	public Composter() {
		super("Composter", sprite, itemSprite);
	}

	@Override
	public boolean interact(Player player, @Nullable ItemStack item, Direction attackDir) {
		if (compost == MAX_COMPOST) {
			compost = 0;
			player.getLevel().dropItem(x, y, Items.getStackOf("Fertilizer"));
			refreshStatus();
			return true;
		}

		if (item.isStackable()) {
			// 100% compost as they are heavy food
			if (item.getName().equalsIgnoreCase("Baked Potato") || item.getName().equalsIgnoreCase("Bread")) {
				compost++;
				item.decrement(1);
				refreshStatus();
				return true;

				// 75% compost as they are crop food
			} else if (item.getName().equalsIgnoreCase("Wheat") || item.getName().equalsIgnoreCase("Rose") ||
				item.getName().equalsIgnoreCase("Flower") || item.getName().equalsIgnoreCase("Apple") ||
				item.getName().equalsIgnoreCase("Potato") || item.getName().equalsIgnoreCase("Carrot")) {
				if (random.nextInt(4) != 0) { // 75%
					compost++;
					item.decrement(1);
					refreshStatus();
					return true;
				}

				// 66.7& compost as they are seeds
			} else if (item.getName().equalsIgnoreCase("Acorn") || item.getName().equalsIgnoreCase("Cactus") ||
				item.getName().equalsIgnoreCase("Wheat Seeds") || item.getName().equalsIgnoreCase("Grass Seeds")) {
				if (random.nextInt(3) != 0) { // 66.7%
					compost++;
					item.decrement(1);
					refreshStatus();
					return true;
				}
			}
		}

		return false;
	}

	private void refreshStatus() {
		if (compost == 0)
			super.sprite = sprite;
		else if (compost < MAX_COMPOST)
			super.sprite = spriteFilled;
		else
			super.sprite = spriteFull;
	}
}
