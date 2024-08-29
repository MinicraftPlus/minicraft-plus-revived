package minicraft.entity.furniture;

import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.SpriteManager;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.StackableItem;
import org.jetbrains.annotations.Nullable;

public class Composter extends Furniture {
	private static final SpriteManager.SpriteLink sprite =
		new SpriteManager.SpriteLink.SpriteLinkBuilder(SpriteManager.SpriteType.Entity, "composter").createSpriteLink();
	private static final SpriteManager.SpriteLink spriteFilled =
		new SpriteManager.SpriteLink.SpriteLinkBuilder(SpriteManager.SpriteType.Entity, "composter_filled").createSpriteLink();
	private static final SpriteManager.SpriteLink spriteFull =
		new SpriteManager.SpriteLink.SpriteLinkBuilder(SpriteManager.SpriteType.Entity, "composter_full").createSpriteLink();
	private static final SpriteManager.SpriteLink itemSprite =
		new SpriteManager.SpriteLink.SpriteLinkBuilder(SpriteManager.SpriteType.Item, "composter").createSpriteLink();

	private static final int MAX_COMPOST = 7;
	private int compost = 0;

	public Composter() {
		super("Composter", sprite, itemSprite);
	}

	@Override
	public boolean interact(Player player, @Nullable Item item, Direction attackDir) {
		if (compost == MAX_COMPOST) {
			compost = 0;
			StackableItem i = (StackableItem) Items.get("Fertilizer").copy();
			i.count = 1;
			player.getLevel().dropItem(x, y, i);
			refreshStatus();
			return true;
		}

		if (item instanceof StackableItem) {
			// 100% compost as they are heavy food
			if (item.getName().equalsIgnoreCase("Baked Potato") || item.getName().equalsIgnoreCase("Bread")) {
				compost++;
				((StackableItem) item).count--;
				refreshStatus();
				return true;

				// 75% compost as they are crop food
			} else if (item.getName().equalsIgnoreCase("Wheat") || item.getName().equalsIgnoreCase("Rose") ||
				item.getName().equalsIgnoreCase("Flower") || item.getName().equalsIgnoreCase("Apple") ||
				item.getName().equalsIgnoreCase("Potato") || item.getName().equalsIgnoreCase("Carrot")) {
				if (random.nextInt(4) != 0) { // 75%
					compost++;
					((StackableItem) item).count--;
					refreshStatus();
					return true;
				}

				// 66.7& compost as they are seeds
			} else if (item.getName().equalsIgnoreCase("Acorn") || item.getName().equalsIgnoreCase("Cactus") ||
				item.getName().equalsIgnoreCase("Wheat Seeds") || item.getName().equalsIgnoreCase("Grass Seeds")) {
				if (random.nextInt(3) != 0) { // 66.7%
					compost++;
					((StackableItem) item).count--;
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
