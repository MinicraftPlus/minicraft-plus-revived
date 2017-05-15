package minicraft.level.tile;

import minicraft.Game;
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
	
	public WoolColor color;
	int col;
	
	public WoolTile(int id, WoolColor color) {
		super(id);
		this.color = color;
		
		if(color == null) {
			col = Color.get(444, 333, 444, 555);
		}
	}

	public void render(Screen screen, Level level, int x, int y) {
		screen.render(x * 16 + 0, y * 16 + 0, 17, col, 0);
		screen.render(x * 16 + 8, y * 16 + 0, 17, col, 0);
		screen.render(x * 16 + 0, y * 16 + 8, 17, col, 0);
		screen.render(x * 16 + 8, y * 16 + 8, 17, col, 0);
	}

	public boolean interact(Level level, int xt, int yt, Player player, Item item, int attackDir) {
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.Shovel) {
				if (player.payStamina(3 - tool.level)) {
					level.setTile(xt, yt, Tile.hole, 0);
					level.add(
							new ItemEntity(
									Items.get("Wool"),
									xt * 16 + random.nextInt(10) + 3,
									yt * 16 + random.nextInt(10) + 3));
					Sound.monsterHurt.play();
					return true;
				}
			}
			/*if (tool.type == ToolType.spade) {
				if (player.payStamina(4 - tool.level)) {
					level.setTile(xt, yt, Tile.hole, 0);
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
