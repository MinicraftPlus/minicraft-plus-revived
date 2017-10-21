package minicraft.level.tile;

import minicraft.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.Mob;
import minicraft.entity.mob.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;

public class DoorTile extends Tile {
	private Sprite closedSprite = new Sprite(2, 24, 2, 2);
	private Sprite openSprite = new Sprite(0, 24, 2, 2);
	
	protected Material type;
	
	protected DoorTile(Material type) {
		super(type.name() + " Door", (Sprite)null);
		this.type = type;
		switch(type) {
			case Wood:
				closedSprite.color = Color.get(320, 430, 210, 430);
				openSprite.color = Color.get(320, 430, 430, 210);
				break;
			case Stone:
				closedSprite.color = Color.get(444, 333, 222, 333);
				openSprite.color = Color.get(444, 333, 333, 222);
				break;
			case Obsidian:
				closedSprite.color = Color.get(203, 102, 203, 102);
				openSprite.color = Color.get(203, 102);
				break;
		}
		sprite = closedSprite;
	}
	
	public void render(Screen screen, Level level, int x, int y) {
		boolean closed = level.getData(x, y) == 0;
		Sprite curSprite = closed?closedSprite:openSprite;
		curSprite.render(screen, x*16, y*16);
	}
	
	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.Pickaxe) {
				if (player.payStamina(4 - tool.level)) {
					level.setTile(xt, yt, Tiles.get(id+3)); // will get the corresponding floor tile.
					level.dropItem(xt*16+8, yt*16+8, Items.get(type.name() + " Door"));
					Sound.monsterHurt.play();
					return true;
				}
			}
		}
		return false;
	}

	public void hurt(Level level, int x, int y, Mob source, int dmg, Direction attackDir) {
		if(source instanceof Player) {
			boolean closed = level.getData(x, y) == 0;
			level.setData(x, y, closed?1:0);
		}
	}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		boolean closed = level.getData(x, y) == 0;
		return !closed;
	}
}
