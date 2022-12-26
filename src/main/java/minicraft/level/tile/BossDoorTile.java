package minicraft.level.tile;

import minicraft.core.Game;
import minicraft.core.io.Localization;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.mob.Mob;
import minicraft.entity.mob.ObsidianKnight;
import minicraft.entity.mob.Player;
import minicraft.item.Item;
import minicraft.item.ToolItem;
import minicraft.level.Level;

public class BossDoorTile extends DoorTile {
	private static final String doorMsg = "minicraft.notification.defeat_obsidian_knight_first";

	protected BossDoorTile() {
		super(Material.Obsidian, "Boss Door");
	}

	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		if ((!ObsidianKnight.beaten || ObsidianKnight.active) && !Game.isMode("minicraft.settings.mode.creative")) {
			if (item instanceof ToolItem) {
				ToolItem tool = (ToolItem) item;
				if (tool.type == type.getRequiredTool()) {
					if (player.payStamina(1)) {
						Game.notifications.add(Localization.getLocalized(doorMsg));
						Sound.play("monsterhurt");
						return true;
					}
				}
			}

			return false;
		}

		return super.interact(level, xt, yt, player, item, attackDir);
	}

	@Override
	public boolean hurt(Level level, int x, int y, Mob source, int dmg, Direction attackDir) {
		if (source instanceof Player) {
			if (ObsidianKnight.active && !Game.isMode("minicraft.settings.mode.creative")) {
				Game.notifications.add(doorMsg);
				return true;
			}
		}

		return super.hurt(level, x, y, source, dmg, attackDir);
	}
}
