package minicraft.level.tile;

import minicraft.entity.Entity;
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

public class CloudTile extends Tile {
	public CloudTile(int id) {
		super(id);
	}

	public void render(Screen screen, Level level, int x, int y) {
		int col = Color.get(444, 444, 555, 444);
		int transitionColor = Color.get(333, 444, 555, -1);

		boolean u = level.getTile(x, y - 1) == Tile.infiniteFall;
		boolean d = level.getTile(x, y + 1) == Tile.infiniteFall;
		boolean l = level.getTile(x - 1, y) == Tile.infiniteFall;
		boolean r = level.getTile(x + 1, y) == Tile.infiniteFall;

		boolean ul = level.getTile(x - 1, y - 1) == Tile.infiniteFall;
		boolean dl = level.getTile(x - 1, y + 1) == Tile.infiniteFall;
		boolean ur = level.getTile(x + 1, y - 1) == Tile.infiniteFall;
		boolean dr = level.getTile(x + 1, y + 1) == Tile.infiniteFall;

		if (!u && !l) {
			if (!ul) screen.render(x * 16 + 0, y * 16 + 0, 19, col, 0);
			else screen.render(x * 16 + 0, y * 16 + 0, 7 + 0 * 32, transitionColor, 3);
		} else
			screen.render(x * 16 + 0, y * 16 + 0, (l ? 6 : 5) + (u ? 2 : 1) * 32, transitionColor, 3);

		if (!u && !r) {
			if (!ur) screen.render(x * 16 + 8, y * 16 + 0, 18, col, 0);
			else screen.render(x * 16 + 8, y * 16 + 0, 8 + 0 * 32, transitionColor, 3);
		} else
			screen.render(x * 16 + 8, y * 16 + 0, (r ? 4 : 5) + (u ? 2 : 1) * 32, transitionColor, 3);

		if (!d && !l) {
			if (!dl) screen.render(x * 16 + 0, y * 16 + 8, 20, col, 0);
			else screen.render(x * 16 + 0, y * 16 + 8, 7 + 1 * 32, transitionColor, 3);
		} else
			screen.render(x * 16 + 0, y * 16 + 8, (l ? 6 : 5) + (d ? 0 : 1) * 32, transitionColor, 3);
		if (!d && !r) {
			if (!dr) screen.render(x * 16 + 8, y * 16 + 8, 19, col, 0);
			else screen.render(x * 16 + 8, y * 16 + 8, 8 + 1 * 32, transitionColor, 3);
		} else
			screen.render(x * 16 + 8, y * 16 + 8, (r ? 4 : 5) + (d ? 0 : 1) * 32, transitionColor, 3);
	}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return true;
	}

	public boolean interact(Level level, int xt, int yt, Player player, Item item, int attackDir) {
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.Shovel) {
				if (player.payStamina(5)) {
					// level.setTile(xt, yt, Tile.infiniteFall, 0); // would allow you to shovel cloud, I think.
					int count = random.nextInt(2) + 1;
					for (int i = 0; i < count; i++) {
						level.add(
								new ItemEntity(
										Items.get("cloud"),
										xt * 16 + random.nextInt(10) + 3,
										yt * 16 + random.nextInt(10) + 3));
					}
					return true;
				}
			}
		}
		return false;
	}
}
