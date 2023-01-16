package minicraft.level.tile;

import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.Player;
import minicraft.gfx.SpriteAnimation;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;

public class CloudTile extends Tile {
	private static SpriteAnimation sprite = new SpriteAnimation(SpriteType.Tile, "cloud")
		.setConnectChecker((tile, side) -> tile.getClass() != InfiniteFallTile.class)
		.setSingletonWithConnective(true);

	protected CloudTile(String name) {
		super(name, sprite);
	}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return true;
	}

	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		// We don't want the tile to break when attacked with just anything, even in creative mode
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.Shovel && player.payStamina(5)) {
				level.setTile(xt, yt, Tiles.get("Infinite Fall")); // Would allow you to shovel cloud, I think.
				Sound.play("monster_hurt");
				level.dropItem(xt * 16 + 8, yt * 16 + 8, 1, 3, Items.get("Cloud"));
				return true;
			}
		}
		return false;
	}
}
