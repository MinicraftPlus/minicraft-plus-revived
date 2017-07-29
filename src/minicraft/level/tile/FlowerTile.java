package minicraft.level.tile;

import minicraft.entity.Mob;
import minicraft.entity.Player;
import minicraft.gfx.Color;
import minicraft.gfx.ConnectorSprite;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;

public class FlowerTile extends Tile {
	private static Sprite flowersprite = new Sprite(1, 1, Color.get(10, 141, 555, 440));
	
	protected FlowerTile(String name) {
		super(name, (ConnectorSprite)null);
		connectsToGrass = true;
		maySpawn = true;
	}
	
	public void render(Screen screen, Level level, int x, int y) {
		Tiles.get("grass").render(screen, level, x, y);
		
		int data = level.getData(x, y);
		int shape = (data / 16) % 2;
		
		x = x << 4;
		y = y << 4;
		
		flowersprite.render(screen, x + 8*shape, y);
		flowersprite.render(screen, x + 8*(shape==0?1:0), y + 8);
	}

	public boolean interact(Level level, int x, int y, Player player, Item item, int attackDir) {
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.Shovel) {
				if (player.payStamina(2 - tool.level)) {
					level.dropItem(x*16+8, y*16+8, Items.get("Flower"));
					level.dropItem(x*16+8, y*16+8, Items.get("Rose"));
					level.setTile(x, y, Tiles.get("grass"));
					return true;
				}
			}
		}
		return false;
	}

	public void hurt(Level level, int x, int y, Mob source, int dmg, int attackDir) {
		level.dropItem(x*16+8, y*16+8, 1, 2, Items.get("Flower"));
		level.dropItem(x*16+8, y*16+8, 0, 1, Items.get("Rose"));
		level.setTile(x, y, Tiles.get("grass"));
	}
}
