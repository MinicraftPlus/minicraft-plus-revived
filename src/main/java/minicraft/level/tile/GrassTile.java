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

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map;

public class GrassTile extends Tile implements BoostablePlant {
	private static final SpriteAnimation sprite = new SpriteAnimation(SpriteType.Tile, "grass")
		.setConnectChecker((tile, side) -> !side || tile.connectsToGrass)
		.setSingletonWithConnective(true);

	protected GrassTile(String name) {
		super(name, sprite);
		connectsToGrass = true;
		maySpawn = true;
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
						level.dropItem(xt * 16 + 8, yt * 16 + 8, 1, Items.get("Grass Seeds"));
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
					if (random.nextInt(2) != 0) { // 50% chance to drop Wheat seeds
						level.dropItem(xt * 16 + 8, yt * 16 + 8, Items.get("Wheat Seeds"));
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

	@Override
	public boolean isValidBoostablePlantTarget(Level level, int x, int y) {
		return true;
	}

	@Override
	public boolean isPlantBoostSuccess(Level level, int x, int y) {
		return true;
	}

	@Override
	public void performPlantBoost(Level level, int x, int y) {
		label:
		for (int i = 0; i < 128; i++) {
			int xx = x;
			int yy = y;

			for(int j = 0; j < i / 16; ++j) {
				xx += x + random.nextInt(3) - 1;
				yy += y + random.nextInt(3) - 1;
				if (!(level.getTile(xx, yy) == this)) {
					continue label;
				}
			}

			if (level.getTile(xx, yy) == this && random.nextInt(10) == 0) {
				performPlantBoost(level, xx, yy);
			}

			if (level.getTile(xx, yy) != this) continue; // Further confirming the tile is still grass tile.
			Map.Entry<Short, Short> plant = boostPerformingPlants.get(random.nextInt(boostPerformingPlants.size()));
			level.setTile(xx, yy, Tiles.get(plant.getKey()), plant.getValue());
		}
	}

	private static final ArrayList<Map.Entry<Short, Short>> boostPerformingPlants = new ArrayList<>();
	static { // The left-hand-sided data is tile id; the right-hand-sided data is tile data.
		boostPerformingPlants.add(new AbstractMap.SimpleEntry<>((short) 2, (short) 0));
		boostPerformingPlants.add(new AbstractMap.SimpleEntry<>((short) 2, (short) 1));
		boostPerformingPlants.add(new AbstractMap.SimpleEntry<>((short) 52, (short) 0));
		boostPerformingPlants.add(new AbstractMap.SimpleEntry<>((short) 53, (short) 0));
		boostPerformingPlants.add(new AbstractMap.SimpleEntry<>((short) 54, (short) 0));
		boostPerformingPlants.add(new AbstractMap.SimpleEntry<>((short) 55, (short) 0));
	}
}
