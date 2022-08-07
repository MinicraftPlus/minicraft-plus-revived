package minicraft.level.tile;

import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.Mob;
import minicraft.entity.mob.Player;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.gfx.SpriteLinker.LinkedSpriteSheet;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.level.Level;

public class DoorTile extends Tile {
	protected Material type;
	private LinkedSpriteSheet closedSprite;
	private LinkedSpriteSheet openSprite;

	protected DoorTile(Material type) {
		super(type.name() + " Door", (LinkedSpriteSheet) null);
		this.type = type;
		switch (type) {
			case Wood:
				closedSprite = new LinkedSpriteSheet(SpriteType.Tile, "wood_door").setSpriteDim(2, 0, 2, 2);
				openSprite = new LinkedSpriteSheet(SpriteType.Tile, "wood_door").setSpriteSize(2, 2);
				break;
			case Stone:
				closedSprite = new LinkedSpriteSheet(SpriteType.Tile, "stone_door").setSpriteDim(2, 0, 2, 2);
				openSprite = new LinkedSpriteSheet(SpriteType.Tile, "stone_door").setSpriteSize(2, 2);
				break;
			case Obsidian:
				closedSprite = new LinkedSpriteSheet(SpriteType.Tile, "obsidian_door").setSpriteDim(2, 0, 2, 2);
				openSprite = new LinkedSpriteSheet(SpriteType.Tile, "obsidian_door").setSpriteSize(2, 2);
				break;
		}
		sprite = closedSprite;
	}

	public void render(Screen screen, Level level, int x, int y) {
		boolean closed = level.getData(x, y) == 0;
		Sprite curSprite = (closed ? closedSprite : openSprite).getSpriteOrMissing(SpriteType.Tile);
		curSprite.render(screen, x * 16, y * 16);
	}

	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == type.getRequiredTool()) {
				if (player.payStamina(4 - tool.level) && tool.payDurability()) {
					level.setTile(xt, yt, Tiles.get(id + 3)); // Will get the corresponding floor tile.
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
