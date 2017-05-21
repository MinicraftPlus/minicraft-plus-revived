package minicraft.level.tile;

import minicraft.Game;
import minicraft.entity.ItemEntity;
import minicraft.entity.Mob;
import minicraft.entity.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.gfx.ConnectorSprite;
import minicraft.item.Item;
import minicraft.item.StackableItem;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.item.Items;
import minicraft.level.Level;

public class FlowerTile extends GrassTile {
	private static Sprite sprite = new Sprite(1, 1, Color.get(10, 141, 555, 440));
	
	protected static void addInstances() {
		Tiles.add(new FlowerTile("Flower"));
	}
	
	private FlowerTile(String name) {
		super(name, sprite);
		//tiles[id] = this;
		connectsToGrass = true;
		maySpawn = true;
	}
	
	public void render(Screen screen, Level level, int x, int y) {
		super.render(screen, level, x, y);
		
		int data = level.getData(x, y);
		int shape = (data / 16) % 2;
		
		x = x << 4;
		y = y << 4;
		
		sprite.render(screen, x + 8*shape, y);
		sprite.render(screen, x + 8*(shape==0?1:0), y + 8);
		/*
		if (shape == 0) screen.render(x * 16 + 0, y * 16 + 0, 1 + 1 * 32, flowerCol, 0);
		if (shape == 1) screen.render(x * 16 + 8, y * 16 + 0, 1 + 1 * 32, flowerCol, 0);
		if (shape == 0) screen.render(x * 16 + 8, y * 16 + 8, 1 + 1 * 32, flowerCol, 0);
		if (shape == 1) screen.render(x * 16 + 0, y * 16 + 8, 1 + 1 * 32, flowerCol, 0);
		*/
	}

	public boolean interact(Level level, int x, int y, Player player, Item item, int attackDir) {
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.Shovel) {
				if (player.payStamina(2 - tool.level)) {
					level.dropItem(x*16, y*16, Items.get("Flower"));
					level.dropItem(x*16, y*16, Items.get("Rose"));
					level.setTile(x, y, Tiles.get("grass"), 0);
					return true;
				}
			}
		}
		return false;
	}

	public void hurt(Level level, int x, int y, Mob source, int dmg, int attackDir) {
		level.dropItem(x*16, y*16, 1, 2, Items.get("Flower"));
		level.dropItem(x*16, y*16, 0, 1, Items.get("Rose"));
		level.setTile(x, y, Tiles.get("grass"), 0);
	}
}
