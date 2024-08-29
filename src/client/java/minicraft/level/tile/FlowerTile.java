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
	public enum FlowerVariant {
		OXEYE_DAISY("Oxeye Daisy", new SpriteAnimation(SpriteType.Tile, "oxeye_daisy")),
		ROSE("Rose", new SpriteAnimation(SpriteType.Tile, "rose")),
		SUNFLOWER("Sunflower", new SpriteAnimation(SpriteType.Tile, "sunflower")),
		ALLIUM("Allium", new SpriteAnimation(SpriteType.Tile, "allium")),
		BLUE_ORCHID("Blue Orchid", new SpriteAnimation(SpriteType.Tile, "blue_orchid")),
		CORNFLOWER("Cornflower", new SpriteAnimation(SpriteType.Tile, "cornflower")),
		DANDELION("Dandelion", new SpriteAnimation(SpriteType.Tile, "dandelion")),
		HYDRANGEA("Hydrangea", new SpriteAnimation(SpriteType.Tile, "hydrangea")),
		IRIS("Iris", new SpriteAnimation(SpriteType.Tile, "iris")),
		ORANGE_TULIP("Orange Tulip", new SpriteAnimation(SpriteType.Tile, "orange_tulip")),
		PINK_TULIP("Pink Tulip", new SpriteAnimation(SpriteType.Tile, "pink_tulip")),
		RED_TULIP("Red Tulip", new SpriteAnimation(SpriteType.Tile, "red_tulip")),
		WHITE_TULIP("White Tulip", new SpriteAnimation(SpriteType.Tile, "white_tulip")),
		PEONY("Peony", new SpriteAnimation(SpriteType.Tile, "peony")),
		PERIWINKLE("Periwinkle", new SpriteAnimation(SpriteType.Tile, "periwinkle")),
		PINK_LILY("Pink Lily", new SpriteAnimation(SpriteType.Tile, "pink_lily")),
		WHITE_LILY("White Lily", new SpriteAnimation(SpriteType.Tile, "white_lily")),
		POPPY("Poppy", new SpriteAnimation(SpriteType.Tile, "poppy")),
		VIOLET("Violet", new SpriteAnimation(SpriteType.Tile, "violet"));

		private final String name;
		private final SpriteAnimation sprite;

		FlowerVariant(String name, SpriteAnimation sprite) {
			this.name = name;
			this.sprite = sprite;
		}

		public String getName() {
			return name;
		}
	}

	protected FlowerTile(String name) {
		super(name, null);
		maySpawn = true;
	}

	@Override
	public boolean connectsToGrass(Level level, int x, int y) {
		return true;
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
		FlowerVariant.values()[level.getData(x, y)].sprite.render(screen, level, x, y);
	}

	public boolean interact(Level level, int x, int y, Player player, Item item, Direction attackDir) {
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.Shovel) {
				if (player.payStamina(2 - tool.level) && tool.payDurability()) {
					int data = level.getData(x, y);
					level.setTile(x, y, Tiles.get("Grass"));
					Sound.play("monsterhurt");
					level.dropItem((x << 4) + 8, (y << 4) + 8, Items.get(FlowerVariant.values()[level.getData(x, y)].name));
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
		level.dropItem((x << 4) + 8, (y << 4) + 8, Items.get(FlowerVariant.values()[level.getData(x, y)].name));
		level.setTile(x, y, Tiles.get("Grass"));
		return true;
	}
}
