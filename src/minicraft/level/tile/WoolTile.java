package minicraft.level.tile;

import minicraft.Game;
import minicraft.entity.Entity;
import minicraft.entity.ItemEntity;
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

public class WoolTile extends Tile {
	
	public enum WoolColor {
		RED, YELLOW, GREEN, BLUE, BLACK
	}
	
	public WoolColor color;
	//int col0, col1, col2, col3, col4;
	int col;
	
	public WoolTile(int id, WoolColor color) {
		super(id);
		this.color = color;
		
		if(color != null) switch(color) {
			case RED:
			col = Color.get(400, 500, 400, 500);
			/*	col0 = Color.get(300, 400, 300, 400);
				col2 = Color.get(300, 400, 300, 400);
				col3 = Color.get(200, 300, 200, 300);
				col4 = Color.get(500, 400, 500, 400);*/
				break;
			case YELLOW:
			col = Color.get(550, 661, 440, 550);
				/*col0 = Color.get(440, 500, 330, 440);
				col2 = Color.get(440, 500, 330, 440);
				col3 = Color.get(330, 439, 220, 330);
				col4 = Color.get(550, 661, 440, 550);*/
				break;
			case GREEN:
			col = Color.get(30, 40, 40, 50);
				/*col0 = Color.get(20, 30, 30, 40);
				col2 = Color.get(20, 30, 30, 40);
				col3 = Color.get(10, 20, 20, 30);
				col4 = Color.get(30, 40, 40, 50);*/
				break;
			case BLUE:
			col = Color.get(015, 115, 015, 115);
				/*col0 = Color.get(015, 125, 015, 125);
				col2 = Color.get(014, 015, 014, 015);
				col3 = Color.get(000, 015, 000, 015);
				col4 = Color.get(015, 115, 015, 115);*/
				break;
			case BLACK:
			col = Color.get(111, 111, 000, 111);
				/*col0 = Color.get(111, 111, 000, 111);
				col2 = Color.get(111, 111, 000, 111);
				col3 = Color.get(111, 000, 111, 000);
				col4 = Color.get(111, 111, 000, 111);*/
				break;
			default: color = null;
		}
		
		if(color == null) {
			//col0 = Color.get(333, 222, 333, 444);
			col = Color.get(444, 333, 444, 555);
			/*col2 = Color.get(333, 222, 333, 444);
			col3 = Color.get(222, 111, 222, 333);
			col4 = Color.get(444, 333, 444, 555);*/
		}
	}

	public void render(Screen screen, Level level, int x, int y) {
		/*int col = 0;
		
		if (level.dirtColor == 322) {
			if (Game.time == 0) col = col0;
			if (Game.time == 1) col = col1;
			if (Game.time == 2) col = col2;
			if (Game.time == 3) col = col3;
		} else if (level.dirtColor == 222) {
			col = col4;
		}
		*/
		screen.render(x * 16 + 0, y * 16 + 0, 17, col, 0);
		screen.render(x * 16 + 8, y * 16 + 0, 17, col, 0);
		screen.render(x * 16 + 0, y * 16 + 8, 17, col, 0);
		screen.render(x * 16 + 8, y * 16 + 8, 17, col, 0);
	}

	public boolean interact(Level level, int xt, int yt, Player player, Item item, int attackDir) {
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.shovel) {
				if (player.payStamina(3 - tool.level)) {
					level.setTile(xt, yt, Tile.hole, 0);
					level.add(
							new ItemEntity(
									new ResourceItem(Resource.wool),
									xt * 16 + random.nextInt(10) + 3,
									yt * 16 + random.nextInt(10) + 3));
					Sound.monsterHurt.play();
					return true;
				}
			}
			if (tool.type == ToolType.spade) {
				if (player.payStamina(4 - tool.level)) {
					level.setTile(xt, yt, Tile.hole, 0);
					level.add(
							new ItemEntity(
									new ResourceItem(Resource.wool),
									xt * 16 + random.nextInt(10) + 3,
									yt * 16 + random.nextInt(10) + 3));
					Sound.monsterHurt.play();
					return true;
				}
			}
		}
		return false;
	}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return e.canWool();
	}
}
