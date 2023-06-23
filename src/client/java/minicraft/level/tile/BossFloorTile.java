package minicraft.level.tile;

import minicraft.core.Game;
import minicraft.core.io.Localization;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.mob.ObsidianKnight;
import minicraft.entity.mob.Player;
import minicraft.item.Item;
import minicraft.item.ToolItem;
import minicraft.level.Level;

public class BossFloorTile extends FloorTile {
	private static final String floorMsg = "minicraft.notification.defeat_obsidian_knight_first";

	protected BossFloorTile() {
		super(Material.Obsidian, "Boss Floor");
	}

	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		if ((!ObsidianKnight.beaten || ObsidianKnight.active) && !Game.isMode("minicraft.settings.mode.creative")) {
			if (item instanceof ToolItem) {
				ToolItem tool = (ToolItem) item;
				if (tool.type == type.getRequiredTool()) {
					if (player.payStamina(1)) {
						Game.notifications.add(Localization.getLocalized(floorMsg));
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
