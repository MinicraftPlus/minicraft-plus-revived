package minicraft.level.tile;

import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.mob.Mob;
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

public class FlowerTile extends Tile {
	private static final SpriteAnimation flowerSprite0 = new SpriteAnimation(SpriteType.Tile, "flower_shape0");
	private static final SpriteAnimation flowerSprite1 = new SpriteAnimation(SpriteType.Tile, "flower_shape1");

	protected FlowerTile(String name) {
		super(name, null);
		connectsToGrass = true;
		maySpawn = true;
	}

	public boolean tick(Level level, int xt, int yt) {
		// TODO revise this method.
		if (random.nextInt(30) != 0) return false; // Skips every 31 tick.

		int xn = xt;
		int yn = yt;

		if (random.nextBoolean()) xn += random.nextInt(2) * 2 - 1;
		else yn += random.nextInt(2) * 2 - 1;

		if (level.getTile(xn, yn) == Tiles.get("Dirt")) {
			level.setTile(xn, yn, Tiles.get("Grass"));
		}
		return false;
	}

	public void render(Screen screen, Level level, int x, int y) {
		Tiles.get("Grass").render(screen, level, x, y);
		int data = level.getData(x, y);
		int shape = (data / 16) % 2;
		(shape == 0 ? flowerSprite0 : flowerSprite1).render(screen, level, x, y);
	}

	public boolean interact(Level level, int x, int y, Player player, Item item, Direction attackDir) {
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.Shovel) {
				if (player.payStamina(2 - tool.level) && tool.payDurability()) {
					int data = level.getData(x, y);
					level.setTile(x, y, Tiles.get("Grass"));
					Sound.play("monsterhurt");
					level.dropItem(x * 16 + 8, y * 16 + 8, Items.get("Flower"));
					level.dropItem(x * 16 + 8, y * 16 + 8, Items.get("Rose"));
					AdvancementElement.AdvancementTrigger.ItemUsedOnTileTrigger.INSTANCE.trigger(
						new AdvancementElement.AdvancementTrigger.ItemUsedOnTileTrigger.ItemUsedOnTileTriggerConditionHandler.ItemUsedOnTileTriggerConditions(
							item, this, data, x, y, level.depth));
					return true;
				}
			}
		}
		return false;
	}

	public boolean hurt(Level level, int x, int y, Mob source, int dmg, Direction attackDir) {
		level.dropItem(x * 16 + 8, y * 16 + 8, 0, 1, Items.get("Flower"));
		level.dropItem(x * 16 + 8, y * 16 + 8, 0, 1, Items.get("Rose"));
		level.setTile(x, y, Tiles.get("Grass"));
		return true;
	}
}
