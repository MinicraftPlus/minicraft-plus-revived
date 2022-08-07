package minicraft.level.tile;

import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.Mob;
import minicraft.entity.mob.Player;
import minicraft.gfx.ConnectorSprite;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteLinker.LinkedSpriteSheet;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;

public class SandTile extends Tile {
	static LinkedSpriteSheet steppedOn = new LinkedSpriteSheet(SpriteType.Tile, "sand_stepped"),
	normal = new LinkedSpriteSheet(SpriteType.Tile, "sand").setSpriteDim(3, 0, 2, 2);

	private ConnectorSprite sprite = new ConnectorSprite(SandTile.class, new LinkedSpriteSheet(SpriteType.Tile, "sand").setSpriteSize(3, 3).setMirror(3), normal)
	{
		public boolean connectsTo(Tile tile, boolean isSide) {
			if(!isSide) return true;
			return tile.connectsToSand;
		}
	};

	protected SandTile(String name) {
		super(name, (ConnectorSprite)null);
		csprite = sprite;
		connectsToSand = true;
		maySpawn = true;
	}

	public void render(Screen screen, Level level, int x, int y) {
		boolean steppedOn = level.getData(x, y) > 0;

		if(steppedOn) csprite.full = SandTile.steppedOn;
		else csprite.full = SandTile.normal;

		csprite.sparse.setColor(DirtTile.dCol(level.depth));

		csprite.render(screen, level, x, y);
	}

	public boolean tick(Level level, int x, int y) {
		int damage = level.getData(x, y);
		if (damage > 0) {
			level.setData(x, y, damage - 1);
			return true;
		}
		return false;
	}

	public void steppedOn(Level level, int x, int y, Entity entity) {
		if (entity instanceof Mob) {
			level.setData(x, y, 10);
		}
	}

	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.Shovel) {
				if (player.payStamina(4 - tool.level) && tool.payDurability()) {
					level.setTile(xt, yt, Tiles.get("Hole"));
					Sound.monsterHurt.play();
					level.dropItem(xt * 16 + 8, yt * 16 + 8, Items.get("Sand"));
					return true;
				}
			}
		}
		return false;
	}
}
