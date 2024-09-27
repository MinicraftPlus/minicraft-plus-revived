package minicraft.level.tile;

import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
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

	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.Shovel) {
				if (player.payStamina(4 - tool.level) && tool.payDurability()) {
					int data = level.getData(xt, yt);
					level.setTile(xt, yt, Tiles.get("Hole"));
					Sound.play("monsterhurt");
					level.dropItem((xt << 4) + 8, (yt << 4) + 8, Items.get("Dirt"));
					AdvancementElement.AdvancementTrigger.ItemUsedOnTileTrigger.INSTANCE.trigger(
						new AdvancementElement.AdvancementTrigger.ItemUsedOnTileTrigger.ItemUsedOnTileTriggerConditionHandler.ItemUsedOnTileTriggerConditions(
							item, this, data, xt, yt, level.depth));
					return true;
				}
			}
			if (tool.type == ToolType.Hoe) {
				if (player.payStamina(4 - tool.level) && tool.payDurability()) {
					int data = level.getData(xt, yt);
					level.setTile(xt, yt, Tiles.get("Farmland"));
					Sound.play("monsterhurt");
					AdvancementElement.AdvancementTrigger.ItemUsedOnTileTrigger.INSTANCE.trigger(
						new AdvancementElement.AdvancementTrigger.ItemUsedOnTileTrigger.ItemUsedOnTileTriggerConditionHandler.ItemUsedOnTileTriggerConditions(
							item, this, data, xt, yt, level.depth));
					return true;
				}
			}
		}
		return false;
	}
}
