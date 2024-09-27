package minicraft.level.tile;

import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.entity.particle.Particle;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteAnimation;
import minicraft.gfx.SpriteLinker;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.StackableItem;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;
import minicraft.util.AdvancementElement;

import java.util.Random;

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
		if (random.nextInt(35) != 0) return false;

		if (random.nextInt(20) == 0) { // "Generate" tall grass
			level.setTile(xt, yt, Tiles.get("Tall Grass"), TallGrassTile.getRandomData(random));
		} else { // "Extend" grass
			//noinspection DuplicatedCode
			int xn = xt;
			int yn = yt;

			if (random.nextBoolean()) xn += random.nextInt(2) * 2 - 1;
			else yn += random.nextInt(2) * 2 - 1;

			if (level.getTile(xn, yn) == Tiles.get("Dirt")) {
				level.setTile(xn, yn, this);
				return true;
			}
		}

		return false;
	}

	@Override
	public void render(Screen screen, Level level, int x, int y) {
		Tiles.get("dirt").render(screen, level, x, y);
		sprite.render(screen, level, x, y);
	}

	private static final SpriteLinker.LinkedSprite particleSprite = new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Entity, "glint");

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
					if (random.nextInt(8) < 5) { // 62.5% chance to drop Wheat seeds (some is covered by tall grass)
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
		} else if (item instanceof StackableItem && item.getName().equalsIgnoreCase("Fertilizer")) {
			//noinspection DuplicatedCode
			((StackableItem) item).count--;
			Random random = new Random();
			for (int i = 0; i < 2; ++i) {
				double x = (double) xt * 16 + 8 + (random.nextGaussian() * 0.5) * 8;
				double y = (double) yt * 16 + 8 + (random.nextGaussian() * 0.5) * 8;
				level.add(new Particle((int) x, (int) y, 120 + random.nextInt(21) - 40, particleSprite));
			}

			// Change to forcedly generate tall grass
			if (random.nextInt(4) == 0) // 25%
				level.setTile(xt, yt, Tiles.get("Tall Grass"), TallGrassTile.getRandomData(random));
			return true;
		}
		return false;
	}
}
