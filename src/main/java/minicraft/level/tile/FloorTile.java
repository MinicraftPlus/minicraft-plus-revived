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

public class FloorTile extends Tile {
	protected Material type;

	protected FloorTile(Material type) {
		super((type == Material.Wood ? "Wood Planks" : type == Material.Obsidian ? "Obsidian" : type.name() + " Bricks"), (LinkedSprite) null);
		this.type = type;
		maySpawn = true;
		switch (type) {
			case Wood: sprite = new LinkedSprite(SpriteType.Tile, "wood_floor"); break;
			case Stone: sprite = new LinkedSprite(SpriteType.Tile, "stone_floor"); break;
			case Obsidian: sprite = new LinkedSprite(SpriteType.Tile, "obsidian_floor"); break;
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
						case Wood: drop = Items.get("Plank"); break;
						default: drop = Items.get(type.name() + " Brick"); break;
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
