package minicraft.level.tile;

import minicraft.entity.Entity;
import minicraft.entity.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;
import minicraft.sound.Sound;

public class WoolTile extends Tile {
	
	public enum WoolColor {
		NONE (Color.get(444, 333, 444, 555)),
		RED (Color.get(400, 500, 400, 500)),
		YELLOW (Color.get(550, 661, 440, 550)),
		GREEN (Color.get(30, 40, 40, 50)),
		BLUE (Color.get(015, 115, 015, 115)),
		BLACK (Color.get(111, 111, 000, 111));
		
		public int col;
		private WoolColor(int color) {
			col = color;
		}
		public static final WoolColor[] values = values();
	}
	
	private static Sprite sprite = Sprite.repeat(17, 0, 2, 2, 0);
	
	//public WoolColor color;
	//int col;
	
	protected WoolTile() {
		super(/*(color==WoolColor.NONE?"Wool":color.name() + " Wool")*/"Wool", sprite);
		//this.color = color;
		
		//sprite.color = color.col;
	}
	
	
	public void render(Screen screen, Level level, int x, int y) {
		int data = level.getData(x, y);
		int color = WoolColor.values[data].col;
		sprite.render(screen, x*16, y*16, color);
	}
	
	public boolean interact(Level level, int xt, int yt, Player player, Item item, int attackDir) {
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.Shovel) {
				if (player.payStamina(3 - tool.level)) {
					level.setTile(xt, yt, Tiles.get("hole"));
					level.dropItem(xt*16, yt*16, Items.get("Wool"));
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
	
	public boolean matches(int thisData, String otherTile) {
		if(!otherTile.contains("_"))
			return name.equals(otherTile);
		else {
			String[] parts = otherTile.split("_");
			String tname = parts[0];
			int tdata = Integer.parseInt(parts[1]);
			return name.equals(tname) && thisData == tdata;
		}
	}
}
