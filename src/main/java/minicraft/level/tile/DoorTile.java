package minicraft.level.tile;

import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.Mob;
import minicraft.entity.mob.Player;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteAnimation;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.level.Level;

public class DoorTile extends Tile {
	protected Material type;
	private SpriteAnimation closedSprite;
	private SpriteAnimation openSprite;

	protected DoorTile(Material type) {
		super(type.name() + " Door", (SpriteAnimation) null);
		this.type = type;
		switch (type) {
			case Wood:
				closedSprite = new SpriteAnimation(SpriteType.Tile, "wood_door");
				openSprite = new SpriteAnimation(SpriteType.Tile, "wood_door_opened");
				break;
			case Stone:
				closedSprite = new SpriteAnimation(SpriteType.Tile, "stone_door");
				openSprite = new SpriteAnimation(SpriteType.Tile, "stone_door_opened");
				break;
			case Obsidian:
				closedSprite = new SpriteAnimation(SpriteType.Tile, "obsidian_door");
				openSprite = new SpriteAnimation(SpriteType.Tile, "obsidian_door_opened");
				break;
		}
		sprite = closedSprite;
	}

	public void render(Screen screen, Level level, int x, int y) {
		boolean closed = level.getData(x, y) == 0;
		SpriteAnimation curSprite = closed ? closedSprite : openSprite;
		curSprite.render(screen, level, x, y);
	}

	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == type.getRequiredTool()) {
				if (player.payStamina(4 - tool.level) && tool.payDurability()) {
					level.setTile(xt, yt, Tiles.get(id + 3)); // Will get the corresponding floor tile.
					Sound.play("monsterhurt");
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
