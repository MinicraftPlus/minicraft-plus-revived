package minicraft.level.tile;

import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteAnimation;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;
import minicraft.util.AdvancementElement;

public class GrassTile extends Tile {
	private static final SpriteAnimation sprite = new SpriteAnimation(SpriteType.Tile, "grass")
		.setConnectionChecker((level, x, y, tile, side) -> tile.connectsToGrass(level, x, y))
		.setSingletonWithConnective(true);

	protected GrassTile(String name) {
		super(name, sprite);
		maySpawn = true;
	}

	@Override
	public boolean connectsToGrass(Level level, int x, int y) {
		return true;
	}

	public boolean tick(Level level, int xt, int yt) {
		// TODO revise this method.
		if (random.nextInt(40) != 0) return false;

		int xn = xt;
		int yn = yt;

		if (random.nextBoolean()) xn += random.nextInt(2) * 2 - 1;
		else yn += random.nextInt(2) * 2 - 1;

		if (level.getTile(xn, yn) == Tiles.get("Dirt")) {
			level.setTile(xn, yn, this);
		}
		return false;
	}

	@Override
	public void render(Screen screen, Level level, int x, int y) {
		Tiles.get("dirt").render(screen, level, x, y);
		sprite.render(screen, level, x, y);
	}

	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.Shovel) {
				if (player.payStamina(4 - tool.level) && tool.payDurability()) {
					int data = level.getData(xt, yt);
					level.setTile(xt, yt, Tiles.get("Dirt"));
					Sound.play("monsterhurt");
					if (random.nextInt(5) == 0) { // 20% chance to drop Grass seeds
						level.dropItem((xt << 4) + 8, (yt << 4) + 8, 1, Items.get("Grass Seeds"));
					}
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
					if (random.nextInt(5) != 0) { // 80% chance to drop Wheat seeds
						level.dropItem((xt << 4) + 8, (yt << 4) + 8, Items.get("Wheat Seeds"));
					}
					AdvancementElement.AdvancementTrigger.ItemUsedOnTileTrigger.INSTANCE.trigger(
						new AdvancementElement.AdvancementTrigger.ItemUsedOnTileTrigger.ItemUsedOnTileTriggerConditionHandler.ItemUsedOnTileTriggerConditions(
							item, this, data, xt, yt, level.depth));
					return true;
				}
			}
			if (tool.type == ToolType.Pickaxe) {
				if (player.payStamina(4 - tool.level) && tool.payDurability()) {
					level.setTile(xt, yt, Tiles.get("Path"));
					Sound.play("monsterhurt");
				}
			}
		}
		return false;
	}
}
