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

public class WoolTile extends Tile {

	public WoolTile(WoolType woolType) {
		super(woolType.name, woolType.sprite);
	}

	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.Shears) {
				if (player.payStamina(3 - tool.level) && tool.payDurability()) {
					level.setTile(xt, yt, Tiles.get("Hole"));
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
		BLACK("Black Wool", new Sprite(10, 4, 2, 2, 1)),
		BLUE("Blue Wool", new Sprite(8, 2, 2, 2, 1)),
		GREEN("Green Wool", new Sprite(10, 2, 2, 2, 1)),
		NORMAL("Wool", new Sprite(8, 0, 2, 2, 1)),
		RED("Red Wool", new Sprite(10, 0, 2, 2, 1)),
		YELLOW("Yellow Wool", new Sprite(8, 4, 2, 2, 1));

		public final Sprite sprite;
		public final String name;

		/**
		 * Create a type of wool.
		 *
		 * @param sprite The sprite for the type of wool.
		 */
		WoolType(String name, Sprite sprite) {
			this.sprite = sprite;
			this.name = name;
		}
	}
}
