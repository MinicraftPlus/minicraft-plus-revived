package minicraft.level.tile;

import minicraft.core.Game;
import minicraft.core.io.Localization;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.Mob;
import minicraft.entity.mob.ObsidianKnight;
import minicraft.entity.mob.Player;
import minicraft.item.Item;
import minicraft.item.ToolItem;
import minicraft.level.Level;
import org.jetbrains.annotations.Nullable;

public class BossDoorTile extends DoorTile {
	private static final String doorMsg = "minicraft.notification.defeat_obsidian_knight_first";

	protected BossDoorTile() {
		super(Material.Obsidian, "Boss Door");
	}

	@Override
	public boolean hurt(Level level, int x, int y, Entity source, @Nullable Item item, Direction attackDir, int damage) {
		if ((!ObsidianKnight.beaten || ObsidianKnight.active) && !Game.isMode("minicraft.settings.mode.creative") && source instanceof Player) {
			if (item instanceof ToolItem) {
				ToolItem tool = (ToolItem) item;
				if (tool.type == type.getRequiredTool()) {
					if (((Player) source).payStamina(1)) {
						Game.notifications.add(Localization.getLocalized(doorMsg));
						Sound.play("monsterhurt");
						return true;
					}
				}
			}

			return false;
		}

		return super.hurt(level, x, y, source, item, attackDir, damage);
	}

	@Override
	public boolean use(Level level, int xt, int yt, Player player, @Nullable Item item, Direction attackDir) {
		if (ObsidianKnight.active && !Game.isMode("minicraft.settings.mode.creative")) {
			Game.notifications.add(doorMsg);
			return true;
		}

		return super.use(level, xt, yt, player, item, attackDir);
	}
}
