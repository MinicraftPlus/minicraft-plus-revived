package minicraft.level.tile;

import minicraft.core.Game;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.ObsidianKnight;
import minicraft.entity.mob.Player;
import minicraft.gfx.Sprite;
import minicraft.gfx.SpriteAnimation;
import minicraft.gfx.SpriteLinker;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.level.Level;

public class BossFloorTile extends FloorTile {
	private static final String floorMsg = "The Obsidian Knight must be defeated first.";

	protected BossFloorTile() {
		super(Material.Obsidian, "Boss Floor");
	}

	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		if (ObsidianKnight.beaten || !ObsidianKnight.active) {
			if (item instanceof ToolItem) {
				ToolItem tool = (ToolItem) item;
				if (tool.type == type.getRequiredTool()) {
					if (player.payStamina(1)) {
						Game.notifications.add(floorMsg);
						Sound.play("monsterhurt");
						return true;
					}
				}
			}

			return false;
		}

		return super.interact(level, xt, yt, player, item, attackDir);
	}
}
