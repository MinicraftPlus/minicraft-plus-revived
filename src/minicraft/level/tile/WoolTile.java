package minicraft.level.tile;

import minicraft.entity.Entity;
import minicraft.entity.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Sprite;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;
import minicraft.sound.Sound;

public class WoolTile extends Tile {
	
	public enum WoolColor {
		RED (Color.get(400, 500, 400, 500)),
		YELLOW (Color.get(550, 661, 440, 550)),
		GREEN (Color.get(30, 40, 40, 50)),
		BLUE (Color.get(015, 115, 015, 115)),
		BLACK (Color.get(111, 111, 000, 111));
		
		public int col;
		private WoolColor(int color) {
			col = color;
		}
	}
	
	private static Sprite sprite = Sprite.repeat(17, 0, 2, 2, 0);
	
	protected static void addInstances() {
		Tiles.add(new WoolTile("Wool", null));
		for(WoolColor wc: WoolColor.values())
			Tiles.add(new WoolTile(wc.name()+" Wool", wc));
	}
	
	public WoolColor color;
	//int col;
	
	private WoolTile(String name, WoolColor color) {
		super(name, sprite);
		this.color = color;
		
		if(color == null) {
			sprite.color = Color.get(444, 333, 444, 555);
		}
		else sprite.color = color.col;
	}
	/*
	public void render(Screen screen, Level level, int x, int y) {
		screen.render(x * 16 + 0, y * 16 + 0, 17, col, 0);
		screen.render(x * 16 + 8, y * 16 + 0, 17, col, 0);
		screen.render(x * 16 + 0, y * 16 + 8, 17, col, 0);
		screen.render(x * 16 + 8, y * 16 + 8, 17, col, 0);
	}
	*/
	public boolean interact(Level level, int xt, int yt, Player player, Item item, int attackDir) {
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.Shovel) {
				if (player.payStamina(3 - tool.level)) {
					level.setTile(xt, yt, Tiles.get("hole"), 0);
					level.dropItem(xt*16, yt*16, Items.get("Wool"));
					Sound.monsterHurt.play();
					return true;
				}
			}
			/*if (tool.type == ToolType.spade) {
				if (player.payStamina(4 - tool.level)) {
					level.setTile(xt, yt, Tiles.get("hole"), 0);
					level.add(
							new ItemEntity(
									Items.get("Wool"),
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
