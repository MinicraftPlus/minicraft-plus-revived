package minicraft.level.tile;

import minicraft.Game;
import minicraft.entity.ItemEntity;
import minicraft.entity.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Screen;
import minicraft.item.Item;
import minicraft.item.StackableItem;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.item.Items;
import minicraft.level.Level;
import minicraft.sound.Sound;

public class DirtTile extends Tile {
	public DirtTile(int id) {
		super(id);
		maySpawn = true;
	}

	protected static int dCol(int depth) {
		switch(depth) {
			case 1: return 444; // no dirt in sky anyway.
			case 0: return 321;
			case -4: return 222; // no dirt in dungeons anyway.
			default: return 222; // caves.
		}
	}
	
	private static int getColor(int depth) {
		int dcol = dCol(depth);
		return Color.get(dcol, dcol, dcol-111, dcol-111);
	}
	
	public void render(Screen screen, Level level, int x, int y) {
		int col = getColor(level.depth);//level.dirtColor;
		
		screen.render(x * 16 + 0, y * 16 + 0, 0, col, 0);
		screen.render(x * 16 + 8, y * 16 + 0, 1, col, 0);
		screen.render(x * 16 + 0, y * 16 + 8, 2, col, 0);
		screen.render(x * 16 + 8, y * 16 + 8, 3, col, 0);
	}

	public boolean interact(Level level, int xt, int yt, Player player, Item item, int attackDir) {
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.Shovel) {
				if (player.payStamina(4 - tool.level)) {
					level.setTile(xt, yt, Tile.hole, 0);
					level.add(
							new ItemEntity(
									Items.get("dirt"),
									xt * 16 + random.nextInt(10) + 3,
									yt * 16 + random.nextInt(10) + 3));
					Sound.monsterHurt.play();
					return true;
				}
			}
			/*if (tool.type == ToolType.spade) {
				if (player.payStamina(5 - tool.level)) {
					level.setTile(xt, yt, Tile.hole, 0);
					level.add(
							new ItemEntity(
									Items.get("dirt"),
									xt * 16 + random.nextInt(10) + 3,
									yt * 16 + random.nextInt(10) + 3));
					Sound.monsterHurt.play();
					return true;
				}
			}*/
			if (tool.type == ToolType.Hoe) {
				if (player.payStamina(4 - tool.level)) {
					level.setTile(xt, yt, Tile.farmland, 0);
					Sound.monsterHurt.play();
					return true;
				}
			}
		}
		return false;
	}
}
