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

public class FloorTile extends Tile {
	private Sprite sprite;
	
	protected Material type;
	
	protected FloorTile(Material type) {
		super((type == Material.Wood ? "Wood Planks" : type == Material.Obsidian ? "Obsidian" : type.name()+" Bricks"), (Sprite)null);
		this.type = type;
		maySpawn = true;
		switch(type) {
			case Wood:
				sprite = new Sprite(5, 14, 2, 2, 1, 0);
				break;
			case Stone:
				sprite = new Sprite(15, 14, 2, 2, 1, 0);
				break;
			case Obsidian:
				sprite = new Sprite(25, 14, 2, 2, 1, 0);
				break;
		}
		super.sprite = sprite;
	}
	
	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.Pickaxe) {
				if (player.payStamina(4 - tool.level) && tool.payDurability()) {
					if (level.depth == 1) {
						level.setTile(xt, yt, Tiles.get("Cloud"));
					} else {
						level.setTile(xt, yt, Tiles.get("hole"));
					}
					Item drop;
					switch(type) {
						case Wood: drop = Items.get("Plank"); break;
						default: drop = Items.get(type.name()+" Brick"); break;
					}
					Sound.monsterHurt.play();
					level.dropItem(xt*16+8, yt*16+8, drop);
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
