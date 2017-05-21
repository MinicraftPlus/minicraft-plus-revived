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

public class FarmTile extends Tile {
	private static Sprite sprite;
	static {
		Sprite.Px[][] pixels = new Sprite.Px[2][2];
		pixels[0][0] = new Sprite.Px(2, 1, 1);
		pixels[0][1] = new Sprite.Px(2, 1, 0);
		pixels[1][0] = new Sprite.Px(2, 1, 0);
		pixels[1][1] = new Sprite.Px(2, 1, 1);
		sprite = new Sprite(pixels, Color.get(301, 411, 422, 533));
	}
	
	protected static void addInstances() {
		Tiles.add(new FarmTile("Farmland"));
	}
	
	private FarmTile(String name) {
		super(name, sprite);
	}
	/*
	public void render(Screen screen, Level level, int x, int y) {
		int col = Color.get(301, 411, 422, 533);
		screen.render(x * 16 + 0, y * 16 + 0, 2 + 32, col, 1);
		screen.render(x * 16 + 8, y * 16 + 0, 2 + 32, col, 0);
		screen.render(x * 16 + 0, y * 16 + 8, 2 + 32, col, 0);
		screen.render(x * 16 + 8, y * 16 + 8, 2 + 32, col, 1);
	}
	*/
	public boolean interact(Level level, int xt, int yt, Player player, Item item, int attackDir) {
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.Shovel) {
				if (player.payStamina(4 - tool.level)) {
					level.setTile(xt, yt, Tiles.get("dirt"), 0);
					return true;
				}
			}
		}
		return false;
	}

	public void tick(Level level, int xt, int yt) {
		int age = level.getData(xt, yt);
		if (age < 5) level.setData(xt, yt, age + 1);
	}

	public void steppedOn(Level level, int xt, int yt, Entity entity) {
		if (random.nextInt(60) != 0) return;
		if (level.getData(xt, yt) < 5) return;
		level.setTile(xt, yt, Tiles.get("dirt"), 0);
	}
}
