package minicraft.level.tile;

import minicraft.entity.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Sprite;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;
import minicraft.sound.Sound;

public class DirtTile extends Tile {
	private static Sprite sprite = Sprite.dots(getColor(0));
	
	protected DirtTile(String name) {
		super(name, sprite);
		maySpawn = true;
	}

	protected static int dCol(int depth) {
		switch(depth) {
			case 1: return 444; // no dirt in sky anyway.
			case 0: return 321; // surface.
			case -4: return 222; // no dirt in dungeons anyway.
			default: return 222; // caves.
		}
	}
	
	private static int getColor(int depth) {
		int dcol = dCol(depth);
		return Color.get(dcol, dcol, dcol-111, dcol-111);
	}
	
	public boolean interact(Level level, int xt, int yt, Player player, Item item, int attackDir) {
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.Shovel) {
				if (player.payStamina(4 - tool.level)) {
					level.setTile(xt, yt, Tiles.get("hole"));
					level.dropItem(xt*16, yt*16, Items.get("dirt"));
					Sound.monsterHurt.play();
					return true;
				}
			}
			if (tool.type == ToolType.Hoe) {
				if (player.payStamina(4 - tool.level)) {
					level.setTile(xt, yt, Tiles.get("farmland"));
					Sound.monsterHurt.play();
					return true;
				}
			}
		}
		return false;
	}
}
