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
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.gfx.SpriteAnimation;
import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;
import minicraft.screen.AchievementsDisplay;
import minicraft.screen.QuestsDisplay;
import minicraft.util.AdvancementElement;

public class TreeTile extends Tile {
	private static LinkedSprite treeSprite = new LinkedSprite(SpriteType.Tile, "tree");
	private static LinkedSprite treeSpriteFull = new LinkedSprite(SpriteType.Tile, "tree_full");

	protected TreeTile(String name) {
		super(name, null);
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

		Sprite sprite = treeSprite.getSprite();
		Sprite spriteFull = treeSpriteFull.getSprite();

		if (u && ul && l) {
			screen.render(x * 16 + 0, y * 16 + 0, spriteFull.spritePixels[0][1]);
		} else {
			screen.render(x * 16 + 0, y * 16 + 0, sprite.spritePixels[0][0]);
		}

		if (u && ur && r) {
			screen.render(x * 16 + 8, y * 16 + 0, spriteFull.spritePixels[0][0]);
		} else {
			screen.render(x * 16 + 8, y * 16 + 0, sprite.spritePixels[0][1]);
		}

		if (d && dl && l) {
			screen.render(x * 16 + 0, y * 16 + 8, spriteFull.spritePixels[1][1]);
		} else {
			screen.render(x * 16 + 0, y * 16 + 8, sprite.spritePixels[1][0]);
		}

		if (d && dr && r) {
			screen.render(x * 16 + 8, y * 16 + 8, spriteFull.spritePixels[1][0]);
		} else {
			screen.render(x * 16 + 8, y * 16 + 8, sprite.spritePixels[1][1]);
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
					int data = level.getData(xt, yt);
					hurt(level, xt, yt, tool.getDamage());
					AdvancementElement.AdvancementTrigger.ItemUsedOnTileTrigger.INSTANCE.trigger(
						new AdvancementElement.AdvancementTrigger.ItemUsedOnTileTrigger.ItemUsedOnTileTriggerConditionHandler.ItemUsedOnTileTriggerConditions(
							item, this, data, xt, yt, level.depth));
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
		Sound.play("monsterhurt");

		level.add(new TextParticle("" + dmg, x * 16 + 8, y * 16 + 8, Color.RED));
		if (damage >= treeHealth) {
			level.dropItem(x * 16 + 8, y * 16 + 8, 1, 3, Items.get("Wood"));
			level.dropItem(x * 16 +  8, y * 16 + 8, 0, 2, Items.get("Acorn"));
			level.setTile(x, y, Tiles.get("Grass"));
			AchievementsDisplay.setAchievement("minicraft.achievement.woodcutter", true);
		} else {
			level.setData(x, y, damage);
		}
	}
}
