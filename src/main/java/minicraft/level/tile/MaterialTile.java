package minicraft.level.tile;

import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.Player;
import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.level.Level;

public class MaterialTile extends Tile {
	protected Material type;

	protected MaterialTile(Material type) {
		super((type == Material.Stone ? "Stone" : type == Material.Obsidian ? "Raw Obsidian" :type.name()), (LinkedSprite) null);
		this.type = type;
		maySpawn = true;
		switch (type) {
			case Stone: sprite = new LinkedSprite(SpriteType.Tile, "stone"); break;
			case Obsidian: sprite = new LinkedSprite(SpriteType.Tile, "obsidian"); break;
			default:
		}
	}

	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == type.getRequiredTool()) {
				if (player.payStamina(4 - tool.level) && tool.payDurability()) {
					if (level.depth == 1) {
						level.setTile(xt, yt, Tiles.get("Cloud"));
					} else {
						level.setTile(xt, yt, Tiles.get("Hole"));
					}
					Item drop;
					switch (type) {
						case Stone: drop = Items.get("Stone"); break;
						case Obsidian: drop = Items.get("Raw Obsidian"); break;
						default: throw new IllegalStateException("Unexpected value: " + type);
					}
					Sound.play("monsterhurt");
					level.dropItem(xt * 16 + 8, yt * 16 + 8, drop);
					return true;
				}
			}
		}
		return false;
	}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return true;
	}
}
