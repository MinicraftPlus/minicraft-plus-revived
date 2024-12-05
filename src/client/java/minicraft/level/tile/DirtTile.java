package minicraft.level.tile;

import minicraft.core.Game;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.Player;
import minicraft.entity.particle.SmashParticle;
import minicraft.entity.particle.TextParticle;
import minicraft.gfx.Color;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteAnimation;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;
import minicraft.util.AdvancementElement;
import org.jetbrains.annotations.Nullable;

public class DirtTile extends Tile {
	private static SpriteAnimation[] levelSprite = new SpriteAnimation[] {
		new SpriteAnimation(SpriteType.Tile, "dirt"),
		new SpriteAnimation(SpriteType.Tile, "gray_dirt"),
		new SpriteAnimation(SpriteType.Tile, "purple_dirt")
	};

	protected DirtTile(String name) {
		super(name, levelSprite[0]);
		maySpawn = true;
	}

	protected static int dCol(int depth) {
		switch (depth) {
			case 1:
				return Color.get(1, 194, 194, 194); // Sky.
			case 0:
				return Color.get(1, 129, 105, 83); // Surface.
			case -4:
				return Color.get(1, 76, 30, 100); // Dungeons.
			default:
				return Color.get(1, 102); // Caves.
		}
	}

	protected static int dIdx(int depth) {
		switch (depth) {
			case 0:
				return 0; // Surface
			case -4:
				return 2; // Dungeons
			default:
				return 1; // Caves
		}
	}

	public void render(Screen screen, Level level, int x, int y) {
		levelSprite[dIdx(level.depth)].render(screen, level, x, y);
	}

	@Override
	protected void handleDamage(Level level, int x, int y, Entity source, @Nullable Item item, int dmg) {
		level.add(new SmashParticle(x << 4, y << 4));
		level.add(new TextParticle("" + dmg, (x << 4) + 8, (y << 4) + 8, Color.RED));
	}

	@Override
	public boolean hurt(Level level, int x, int y, Entity source, @Nullable Item item, Direction attackDir, int damage) {
		if (Game.isMode("minicraft.settings.mode.creative")) {
			level.setTile(x, y, Tiles.get("Hole"));
			Sound.play("monsterhurt");
			level.dropItem((x << 4) + 8, (y << 4) + 8, Items.get("Dirt"));
			return true;
		}

		if (item instanceof ToolItem && source instanceof Player) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.Shovel) {
				if (((Player) source).payStamina(4 - tool.level) && tool.payDurability()) {
					int data = level.getData(x, y);
					level.setTile(x, y, Tiles.get("Hole"));
					Sound.play("monsterhurt");
					level.dropItem((x << 4) + 8, (y << 4) + 8, Items.get("Dirt"));
					AdvancementElement.AdvancementTrigger.ItemUsedOnTileTrigger.INSTANCE.trigger(
						new AdvancementElement.AdvancementTrigger.ItemUsedOnTileTrigger.ItemUsedOnTileTriggerConditionHandler.ItemUsedOnTileTriggerConditions(
							item, this, data, x, y, level.depth));
					return true;
				}
			}
			if (tool.type == ToolType.Hoe) {
				if (((Player) source).payStamina(4 - tool.level) && tool.payDurability()) {
					int data = level.getData(x, y);
					level.setTile(x, y, Tiles.get("Farmland"));
					Sound.play("monsterhurt");
					AdvancementElement.AdvancementTrigger.ItemUsedOnTileTrigger.INSTANCE.trigger(
						new AdvancementElement.AdvancementTrigger.ItemUsedOnTileTrigger.ItemUsedOnTileTriggerConditionHandler.ItemUsedOnTileTriggerConditions(
							item, this, data, x, y, level.depth));
					return true;
				}
			}
		}

		handleDamage(level, x, y, source, item, 0);
		return true;
	}
}
