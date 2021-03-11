package minicraft.level.tile;

import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.Mob;
import minicraft.entity.mob.Player;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;

public class DoorTile extends Tile {
	protected Material type;
	private Sprite closedSprite;
	private Sprite openSprite;

	protected DoorTile(Material type) {
		super(type.name() + " Door", (Sprite) null);
		this.type = type;
		switch (type) {
			case Wood -> {
				closedSprite = new Sprite(5, 16, 2, 2, 1);
				openSprite = new Sprite(3, 16, 2, 2, 1);
			}
			case Stone -> {
				closedSprite = new Sprite(15, 16, 2, 2, 1);
				openSprite = new Sprite(13, 16, 2, 2, 1);
			}
			case Obsidian -> {
				closedSprite = new Sprite(25, 16, 2, 2, 1);
				openSprite = new Sprite(23, 16, 2, 2, 1);
			}
		}
		sprite = closedSprite;
	}

	public void render(Screen screen, Level level, int x, int y) {
		boolean closed = level.getData(x, y) == 0;
		Sprite curSprite = closed ? closedSprite : openSprite;
		curSprite.render(screen, x * 16, y * 16);
	}

	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == type.getRequiredTool()) {
				if (player.payStamina(4 - tool.level) && tool.payDurability()) {
					level.setTile(xt, yt, Tiles.get(id + 3)); // will get the corresponding floor tile.
					Sound.monsterHurt.play();
					level.dropItem(xt * 16 + 8, yt * 16 + 8, Items.get(type.name() + " Door"));
					return true;
				}
			}
		}
		return false;
	}

	public boolean hurt(Level level, int x, int y, Mob source, int dmg, Direction attackDir) {
		if (source instanceof Player) {
			boolean closed = level.getData(x, y) == 0;
			level.setData(x, y, closed ? 1 : 0);
		}
		return false;
	}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		boolean closed = level.getData(x, y) == 0;
		return !closed;
	}
}
