package minicraft.level.tile;

import minicraft.Game;
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

public class FlowerTile extends GrassTile {
	public FlowerTile(int id) {
		super(id);
		tiles[id] = this;
		connectsToGrass = true;
		maySpawn = true;
	}

	//public int col0 = Color.get(20, 131, 444, 440);
	public int col = Color.get(10, 141, 555, 440);
	//public int col2 = Color.get(10, 30, 444, 330);
	//public int col3 = Color.get(0, 20, 333, 220);

	public void render(Screen screen, Level level, int x, int y) {
		super.render(screen, level, x, y);
		
		//if (Game.time == 0) {
			int data = level.getData(x, y);
			int shape = (data / 16) % 2;
			int flowerCol = col;

			if (shape == 0) screen.render(x * 16 + 0, y * 16 + 0, 1 + 1 * 32, flowerCol, 0);
			if (shape == 1) screen.render(x * 16 + 8, y * 16 + 0, 1 + 1 * 32, flowerCol, 0);
			if (shape == 1) screen.render(x * 16 + 0, y * 16 + 8, 1 + 1 * 32, flowerCol, 0);
			if (shape == 0) screen.render(x * 16 + 8, y * 16 + 8, 1 + 1 * 32, flowerCol, 0);
		/*}
		if (Game.time == 1) {
			int data = level.getData(x, y);
			int shape = (data / 16) % 2;
			int flowerCol = col1;

			if (shape == 0) screen.render(x * 16 + 0, y * 16 + 0, 1 + 1 * 32, flowerCol, 0);
			if (shape == 1) screen.render(x * 16 + 8, y * 16 + 0, 1 + 1 * 32, flowerCol, 0);
			if (shape == 1) screen.render(x * 16 + 0, y * 16 + 8, 1 + 1 * 32, flowerCol, 0);
			if (shape == 0) screen.render(x * 16 + 8, y * 16 + 8, 1 + 1 * 32, flowerCol, 0);
		}
		if (Game.time == 2) {
			int data = level.getData(x, y);
			int shape = (data / 16) % 2;
			int flowerCol = col2;

			if (shape == 0) screen.render(x * 16 + 0, y * 16 + 0, 1 + 1 * 32, flowerCol, 0);
			if (shape == 1) screen.render(x * 16 + 8, y * 16 + 0, 1 + 1 * 32, flowerCol, 0);
			if (shape == 1) screen.render(x * 16 + 0, y * 16 + 8, 1 + 1 * 32, flowerCol, 0);
			if (shape == 0) screen.render(x * 16 + 8, y * 16 + 8, 1 + 1 * 32, flowerCol, 0);
		}
		if (Game.time == 3) {
			int data = level.getData(x, y);
			int shape = (data / 16) % 2;
			int flowerCol = col3;

			if (shape == 0) screen.render(x * 16 + 0, y * 16 + 0, 1 + 1 * 32, flowerCol, 0);
			if (shape == 1) screen.render(x * 16 + 8, y * 16 + 0, 1 + 1 * 32, flowerCol, 0);
			if (shape == 1) screen.render(x * 16 + 0, y * 16 + 8, 1 + 1 * 32, flowerCol, 0);
			if (shape == 0) screen.render(x * 16 + 8, y * 16 + 8, 1 + 1 * 32, flowerCol, 0);
		}*/
	}

	public boolean interact(Level level, int x, int y, Player player, Item item, int attackDir) {
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.shovel) {
				if (player.payStamina(2 - tool.level)) {
					level.add(
							new ItemEntity(
									new ResourceItem(Resource.flower),
									x * 16 + random.nextInt(10) + 3,
									y * 16 + random.nextInt(10) + 3));
					level.add(
							new ItemEntity(
									new ResourceItem(Resource.rose),
									x * 16 + random.nextInt(10) + 3,
									y * 16 + random.nextInt(10) + 3));
					level.setTile(x, y, Tile.grass, 0);
					return true;
				}
			}
		}
		return false;
	}

	public void hurt(Level level, int x, int y, Mob source, int dmg, int attackDir) {
		int count = random.nextInt(2) + 1;
		for (int i = 0; i < count; i++) {
			level.add(
					new ItemEntity(
							new ResourceItem(Resource.flower),
							x * 16 + random.nextInt(10) + 3,
							y * 16 + random.nextInt(10) + 3));
		}
		count = random.nextInt(2);
		for (int i = 0; i < count; i++)
			level.add(
					new ItemEntity(
							new ResourceItem(Resource.rose),
							x * 16 + random.nextInt(10) + 3,
							y * 16 + random.nextInt(10) + 3));
		{
		}
		level.setTile(x, y, Tile.grass, 0);
	}
}
