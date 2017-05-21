package minicraft.level.tile;

import minicraft.Game;
import minicraft.entity.Entity;
import minicraft.entity.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.gfx.ConnectorSprite;
import minicraft.item.Item;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;
import minicraft.sound.Sound;

public class FloorTile extends Tile {
	private Sprite sprite = new Sprite(19, 2, 2, 2, 0, 0, true); //(Color.get(102, 102, 203, 203));//obsidian
	
	protected static void addInstances() {
		for(Material mat: Material.values())
			Tiles.add(new FloorTile(mat));
	}
	
	protected Material type;
	
	private FloorTile(Material type) {
		super((type == Material.Wood ? "Wood Planks" : type == Material.Obsidian ? "Obsidian" : type.name()+" Bricks"), (Sprite)null);
		maySpawn = true;
		switch(type) {
			case Wood: sprite.color = Color.get(210, 210, 430, 320);
			break;
			case Stone: sprite.color = Color.get(333, 333, 444, 444);
			break;
			case Obsidian: sprite.color = Color.get(102, 102, 203, 203);
			break;
		}
		super.sprite = sprite;
	}
	/*
	public void render(Screen screen, Level level, int x, int y) {
		int col = Color.get(102, 102, 203, 203);
		screen.render(x * 16 + 0, y * 16 + 0, 19 + 2 * 32, col, 0);
		screen.render(x * 16 + 8, y * 16 + 0, 19 + 2 * 32, col, 0);
		screen.render(x * 16 + 0, y * 16 + 8, 19 + 2 * 32, col, 0);
		screen.render(x * 16 + 8, y * 16 + 8, 19 + 2 * 32, col, 0);
	}
	*/
	public boolean interact(Level level, int xt, int yt, Player player, Item item, int attackDir) {
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.Pickaxe) {
				if (player.payStamina(4 - tool.level)) {
					level.setTile(xt, yt, Tiles.get("hole"), 0);
					Sound.monsterHurt.play();
					return true;
				}
			}
			/*if (tool.type == ToolType.pick) {
				if (player.payStamina(4 - tool.level)) {
					level.setTile(xt, yt, Tiles.get("hole"), 0);
					Sound.monsterHurt.play();
					return true;
				}
			}*/
		}
		return false;
	}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return true;
	}
}
