package minicraft.level.tile;

import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.Player;
import minicraft.gfx.Sprite;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;

public class Wool extends Tile {

	public Wool(String name, WoolType woolType) {
		super(name, woolType.sprite);
	}

	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.Shear) {
				if (player.payStamina(3 - tool.level) && tool.payDurability()) {
					level.setTile(xt, yt, Tiles.get("hole"));
					Sound.monsterHurt.play();
					level.dropItem(xt * 16 + 8, yt * 16 + 8, Items.get(name));
					return true;
				}
			}
		}
		return false;
	}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return e.canWool();
	}

	public enum WoolType {
		BLACK(new Sprite(10, 4, 2, 2, 1)),
		BLUE(new Sprite(8, 2, 2, 2, 1)),
		GREEN(new Sprite(10, 2, 2, 2, 1)),
		NORMAL(new Sprite(8, 0, 2, 2, 1)),
		RED(new Sprite(10, 0, 2, 2, 1)),
		YELLOW(new Sprite(8, 4, 2, 2, 1));

		public final Sprite sprite;

		/**
		 * Create a type of wool.
		 *
		 * @param sprite The sprite for the type of wool.
		 */
		WoolType(Sprite sprite) {
			this.sprite = sprite;
		}
	}
}
