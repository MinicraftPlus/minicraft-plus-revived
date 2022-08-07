package minicraft.level.tile;

import minicraft.core.Game;
import minicraft.core.Renderer;
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
import minicraft.gfx.SpriteSheet;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;
import minicraft.screen.AchievementsDisplay;
import minicraft.screen.QuestsDisplay;

public class TreeTile extends Tile {

	protected TreeTile(String name) {
		super(name, (ConnectorSprite)null);
		connectsToGrass = true;
	}

	public void render(Screen screen, Level level, int x, int y) {
		Tiles.get("Grass").render(screen, level, x, y);

		boolean u = level.getTile(x, y - 1) == this;
		boolean l = level.getTile(x - 1, y) == this;
		boolean r = level.getTile(x + 1, y) == this;
		boolean d = level.getTile(x, y + 1) == this;
		boolean ul = level.getTile(x - 1, y - 1) == this;
		boolean ur = level.getTile(x + 1, y - 1) == this;
		boolean dl = level.getTile(x - 1, y + 1) == this;
		boolean dr = level.getTile(x + 1, y + 1) == this;

		SpriteSheet sprite = Renderer.spriteLinker.getSpriteSheet(SpriteType.Tile, "tree");

		if (u && ul && l) {
			screen.render(x * 16 + 0, y * 16 + 0, 1, 1, 0, sprite);
		} else {
			screen.render(x * 16 + 0, y * 16 + 0, 0, 0, 0, sprite);
		}

		if (u && ur && r) {
			screen.render(x * 16 + 8, y * 16 + 0, 1, 2, 0, sprite);
		} else {
			screen.render(x * 16 + 8, y * 16 + 0, 1, 0, 0, sprite);
		}

		if (d && dl && l) {
			screen.render(x * 16 + 0, y * 16 + 8, 1, 2, 0, sprite);
		} else {
			screen.render(x * 16 + 0, y * 16 + 8, 0, 1, 0, sprite);
		}

		if (d && dr && r) {
			screen.render(x * 16 + 8, y * 16 + 8, 1, 1, 0, sprite);
		} else {
			screen.render(x * 16 + 8, y * 16 + 8, 1, 3, 0, sprite);
		}
	}

	public boolean tick(Level level, int xt, int yt) {
		int damage = level.getData(xt, yt);
		if (damage > 0) {
			level.setData(xt, yt, damage - 1);
			return true;
		}
		return false;
	}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return false;
	}

	@Override
	public boolean hurt(Level level, int x, int y, Mob source, int dmg, Direction attackDir) {
		hurt(level, x, y, dmg);
		return true;
	}

	@Override
	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		if(Game.isMode("minicraft.settings.mode.creative"))
			return false; // Go directly to hurt method
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.Axe) {
				if (player.payStamina(4 - tool.level) && tool.payDurability()) {
					hurt(level, xt, yt, tool.getDamage());
					return true;
				}
			}
		}
		return false;
	}

	public void hurt(Level level, int x, int y, int dmg) {
		if (random.nextInt(100) == 0)
			level.dropItem(x * 16 + 8, y * 16 + 8, Items.get("Apple"));

		int damage = level.getData(x, y) + dmg;
		int treeHealth = 20;
		if (Game.isMode("minicraft.settings.mode.creative")) dmg = damage = treeHealth;

		level.add(new SmashParticle(x*16, y*16));
		Sound.monsterHurt.play();

		level.add(new TextParticle("" + dmg, x * 16 + 8, y * 16 + 8, Color.RED));
		if (damage >= treeHealth) {
			level.dropItem(x * 16 + 8, y * 16 + 8, 1, 3, Items.get("Wood"));
			level.dropItem(x * 16 +  8, y * 16 + 8, 0, 2, Items.get("Acorn"));
			level.setTile(x, y, Tiles.get("Grass"));
			AchievementsDisplay.setAchievement("minicraft.achievement.woodcutter", true);
			QuestsDisplay.questCompleted("minicraft.quest.tutorial.get_started.kill_tree");
		} else {
			level.setData(x, y, damage);
		}
	}
}
