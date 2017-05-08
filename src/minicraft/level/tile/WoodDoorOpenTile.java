package minicraft.level.tile;

import minicraft.Game;
import minicraft.entity.Entity;
import minicraft.entity.ItemEntity;
import minicraft.entity.Mob;
import minicraft.entity.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Screen;
import minicraft.item.Item;
import minicraft.item.ResourceItem;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.item.resource.Resource;
import minicraft.level.Level;
import minicraft.sound.Sound;

public class WoodDoorOpenTile extends Tile {
	public WoodDoorOpenTile(int id) {
		super(id);
	}

	public void render(Screen screen, Level level, int x, int y) {
		int col = Color.get(320, 430, 430, 210);
		
		screen.render(x * 16 + 0, y * 16 + 0, 0 + 22 * 32, col, 0);
		screen.render(x * 16 + 8, y * 16 + 0, 1 + 22 * 32, col, 0);
		screen.render(x * 16 + 0, y * 16 + 8, 0 + 23 * 32, col, 0);
		screen.render(x * 16 + 8, y * 16 + 8, 1 + 23 * 32, col, 0);
	}

	public boolean interact(Level level, int xt, int yt, Player player, Item item, int attackDir) {
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.axe) {
				if (player.payStamina(4 - tool.level)) {
					level.setTile(xt, yt, Tile.plank, 0);
					level.add(
							new ItemEntity(
									new ResourceItem(Resource.wdoor),
									xt * 16 + random.nextInt(10) + 3,
									yt * 16 + random.nextInt(10) + 3));
					Sound.monsterHurt.play();
					return true;
				}
			}
			if (tool.type == ToolType.hatchet) {
				if (player.payStamina(4 - tool.level)) {
					level.setTile(xt, yt, Tile.plank, 0);
					level.add(
							new ItemEntity(
									new ResourceItem(Resource.wdoor),
									xt * 16 + random.nextInt(10) + 3,
									yt * 16 + random.nextInt(10) + 3));
					Sound.monsterHurt.play();
					return true;
				}
			}
		}
		return false;
	}

	public void hurt(Level level, int x, int y, Mob source, int dmg, int attackDir) {
		level.setTile(x, y, Tile.wdc, 0);
	}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return e.canWool();
	}
}
