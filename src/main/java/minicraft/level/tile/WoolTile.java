package minicraft.level.tile;

import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.Player;
import minicraft.gfx.SpriteLinker.LinkedSpriteSheet;
import minicraft.gfx.SpriteLinker.SpriteType;
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
					Sound.play("monsterhurt");
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
		BLACK("Black Wool", new LinkedSpriteSheet(SpriteType.Tile, "black_wool")),
		BLUE("Blue Wool", new LinkedSpriteSheet(SpriteType.Tile, "blue_wool")),
		GREEN("Green Wool", new LinkedSpriteSheet(SpriteType.Tile, "green_wool")),
		NORMAL("Wool", new LinkedSpriteSheet(SpriteType.Tile, "white_wool")),
		RED("Red Wool", new LinkedSpriteSheet(SpriteType.Tile, "red_wool")),
		YELLOW("Yellow Wool", new LinkedSpriteSheet(SpriteType.Tile, "yellow_wool"));

		public final LinkedSpriteSheet sprite;
		public final String name;

		/**
		 * Create a type of wool.
		 *
		 * @param sprite The sprite for the type of wool.
		 */
		WoolType(String name, LinkedSpriteSheet sprite) {
			this.sprite = sprite;
			this.name = name;
		}
	}
}
