package minicraft.level.tile;

import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.Player;
import minicraft.gfx.Sprite;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.level.Level;

public class DecorTile extends Tile {
	protected Material type;
	private Sprite sprite;

	protected DecorTile(Material type) {
		super((type == Material.Obsidian ? "Ornate Obsidian" : type == Material.Stone ? "Ornate Stone" : "Decorated " + type.name()), (Sprite) null);
		this.type = type;
		maySpawn = true;
		switch (type) {
			case Stone: sprite = new Sprite(17,16,2,2,1,0); break;
			case Obsidian: sprite = new Sprite(27,16,2,2,1,0); break;
			default: break;
		}
		super.sprite = sprite;
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
						case Stone: drop = Items.get("Ornate Stone"); break;
						case Obsidian: drop = Items.get("Ornate Obsidian"); break;
						default: throw new IllegalStateException("Unexpected value: " + type);
					}
					Sound.monsterHurt.play();
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
