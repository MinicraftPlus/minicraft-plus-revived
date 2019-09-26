package minicraft.level.tile;

import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;

public class WoolTile extends Tile {
	
	public enum WoolColor {
		NONE (72),
		RED (80),
		BLUE (4168),
		GREEN (4176),
		YELLOW (8264),
		BLACK (8272);
		
		public int location;
		WoolColor(int loc) {
			location = loc;
		}
		public static final WoolColor[] values = values();
	}
	
	private static Sprite sprite = Sprite.repeat(17, 0, 2, 2);
	
	protected WoolTile() {
		super("Wool", sprite);
	}
	
	
	public void render(Screen screen, Level level, int x, int y) {
		int data = level.getData(x, y);
		sprite.render(screen, x*16, y*16);
	}
	
	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.Shovel) {
				if (player.payStamina(3 - tool.level) && tool.payDurability()) {
					level.setTile(xt, yt, Tiles.get("hole"));
					Sound.monsterHurt.play();
					level.dropItem(xt*16+8, yt*16+8, Items.get("Wool"));
					return true;
				}
			}
		}
		return false;
	}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return e.canWool();
	}
	
	public int getData(String data) {
		return Enum.valueOf(WoolColor.class, data.toUpperCase()).ordinal();
	}
	
	public boolean matches(String woolColor, String otherTile) {
		return matches(getData(woolColor), otherTile);
	}
	@Override
	public boolean matches(int thisData, String tileInfo) {
		if(!tileInfo.contains("_"))
			return name.equals(tileInfo);
		else {
			String[] parts = tileInfo.split("_");
			String tname = parts[0];
			int tdata = Integer.parseInt(parts[1]);
			return name.equals(tname) && thisData == tdata;
		}
	}
	
	public String getName(String woolColor) {
		//woolColor is treated as data value
		return Enum.valueOf(WoolColor.class, woolColor.toUpperCase()) + " " + name;
	}
}
