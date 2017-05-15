package minicraft.level.tile;

import minicraft.Game;
import minicraft.entity.Entity;
import minicraft.entity.ItemEntity;
import minicraft.entity.Mob;
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

public class StoneDoorOpenTile extends Tile {
	public StoneDoorOpenTile(int id) {
		super(id);
	}

	public void render(Screen screen, Level level, int x, int y) {
		int col = Color.get(444, 333, 333, 222);
		
		screen.render(x * 16 + 0, y * 16 + 0, 0 + 24 * 32, col, 0);
		screen.render(x * 16 + 8, y * 16 + 0, 1 + 24 * 32, col, 0);
		screen.render(x * 16 + 0, y * 16 + 8, 0 + 25 * 32, col, 0);
		screen.render(x * 16 + 8, y * 16 + 8, 1 + 25 * 32, col, 0);
	}

	public void hurt(Level level, int x, int y, Mob source, int dmg, int attackDir) {
		level.setTile(x, y, Tile.sdc, 0);
	}

	public boolean interact(Level level, int xt, int yt, Player player, Item item, int attackDir) {
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			int hd = 3;
			if (tool.type == ToolType.Pickaxe) {
				if (player.payStamina(4 - tool.level)) {
					if (hd == 0) {
						level.setTile(xt, yt, Tile.sbrick, 0);
						level.add(
								new ItemEntity(
										Items.get("Stone Door"),
										xt * 16 + random.nextInt(10) + 3,
										yt * 16 + random.nextInt(10) + 3));
						Sound.monsterHurt.play();
						return true;
					}
					if (hd != 0) {
						hd--;
					}
				}
			}
			/*if (tool.type == ToolType.pick) {
				if (player.payStamina(4 - tool.level)) {
					level.setTile(xt, yt, Tile.sbrick, 0);
					level.add(
							new ItemEntity(
									Items.get("Stone Door"),
									xt * 16 + random.nextInt(10) + 3,
									yt * 16 + random.nextInt(10) + 3));
					Sound.monsterHurt.play();
					return true;
				}
			}*/
		}
		return false;
	}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return e.canWool();
	}
}
