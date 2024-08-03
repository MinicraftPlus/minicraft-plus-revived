package minicraft.level.tile;

import minicraft.core.Game;
import minicraft.core.io.Localization;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.mob.ObsidianKnight;
import minicraft.entity.mob.Player;
import minicraft.gfx.SpriteAnimation;
import minicraft.gfx.SpriteLinker;
import minicraft.item.Item;
import minicraft.item.ToolItem;
import minicraft.level.Level;

public class BossWallTile extends WallTile {
	private static SpriteAnimation obsidian = new SpriteAnimation(SpriteLinker.SpriteType.Tile, "obsidian_wall")
		.setConnectionChecker((level, x, y, tile, side) -> tile instanceof WallTile);

	private static final String wallMsg = "minicraft.notification.defeat_obsidian_knight_first";

	protected BossWallTile() {
		super(Material.Obsidian, "Boss Wall");
		sprite = obsidian; // Renewing the connectivity.
	}

	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		if ((!ObsidianKnight.beaten || ObsidianKnight.active) && !Game.isMode("minicraft.settings.mode.creative")) {
			if (item instanceof ToolItem) {
				ToolItem tool = (ToolItem) item;
				if (tool.type == type.getRequiredTool()) {
					if (player.payStamina(1)) {
						Game.notifications.add(Localization.getLocalized(wallMsg));
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
