package minicraft.level.tile;

import minicraft.core.Game;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.Mob;
import minicraft.entity.mob.Player;
import minicraft.entity.particle.SmashParticle;
import minicraft.entity.particle.TextParticle;
import minicraft.gfx.Color;
import minicraft.gfx.ConnectorSprite;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.gfx.SpriteLinker.LinkedSpriteSheet;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;

public class HardRockTile extends Tile {
	// Theoretically the full sprite should never be used, so we can use a placeholder
	private static ConnectorSprite sprite = new ConnectorSprite(HardRockTile.class, new LinkedSpriteSheet(SpriteType.Tile, "hardrock")
		.setSpriteSize(3, 3).setMirror(3), new LinkedSpriteSheet(SpriteType.Tile, "hardrock").setSpriteDim(3, 0, 2, 2).setMirror(3), Sprite.missingTexture(SpriteType.Tile));

	protected HardRockTile(String name) {
		super(name, sprite);
	}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return false;
	}

	public boolean hurt(Level level, int x, int y, Mob source, int dmg, Direction attackDir) {
		hurt(level, x, y, 0);
		return true;
	}

	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		if(Game.isMode("minicraft.settings.mode.creative"))
			return false; // Go directly to hurt method
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;

			// If we are hitting with a gem pickaxe.
			if (tool.type == ToolType.Pickaxe && tool.level == 4) {
				if (player.payStamina(2) && tool.payDurability()) {
					hurt(level, xt, yt, tool.getDamage());
					return true;
				}
			} else {
				Game.notifications.add("minicraft.notification.gem_pickaxe_required");
			}
		}
		return false;
	}

	public void hurt(Level level, int x, int y, int dmg) {
		int damage = level.getData(x, y) + dmg;
		int hrHealth = 200;
		if (Game.isMode("minicraft.settings.mode.creative")) dmg = damage = hrHealth;
		level.add(new SmashParticle(x * 16, y * 16));
		Sound.play("monsterhurt");

		level.add(new TextParticle("" + dmg, x * 16 + 8, y * 16 + 8, Color.RED));
		if (damage >= hrHealth) {
			level.setTile(x, y, Tiles.get("dirt"));
			level.dropItem(x * 16 + 8, y * 16 + 8, 1, 3, Items.get("Stone"));
			level.dropItem(x * 16 + 8, y * 16 + 8, 0, 1, Items.get("Coal"));
		} else {
			level.setData(x, y, damage);
		}
	}

	@Override
	public void render(Screen screen, Level level, int x, int y) {
		sprite.sparse.setColor(DirtTile.dCol(level.depth));
		super.render(screen, level, x, y);
	}

	public boolean tick(Level level, int xt, int yt) {
		int damage = level.getData(xt, yt);
		if (damage > 0) {
			level.setData(xt, yt, damage - 1);
			return true;
		}
		return false;
	}
}
