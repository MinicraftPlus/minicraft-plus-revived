package minicraft.level.tile;

import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.Player;
import minicraft.gfx.SpriteAnimation;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;
import minicraft.util.AdvancementElement;
import org.jetbrains.annotations.Nullable;

public class WoolTile extends Tile {

	public WoolTile(WoolType woolType) {
		super(woolType.name, woolType.sprite);
	}

	@Override
	public boolean hurt(Level level, int x, int y, Entity source, @Nullable Item item, Direction attackDir, int damage) {
		if (item instanceof ToolItem && source instanceof Player) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.Shears) {
				if (((Player) source).payStamina(3 - tool.level) && tool.payDurability()) {
					int data = level.getData(x, y);
					level.setTile(x, y, Tiles.get("Hole"));
					Sound.play("monsterhurt");
					level.dropItem((x << 4) + 8, (y << 4) + 8, Items.get(name));
					AdvancementElement.AdvancementTrigger.ItemUsedOnTileTrigger.INSTANCE.trigger(
						new AdvancementElement.AdvancementTrigger.ItemUsedOnTileTrigger.ItemUsedOnTileTriggerConditionHandler.ItemUsedOnTileTriggerConditions(
							item, this, data, x, y, level.depth));
					return true;
				}
			}
		}
		return false;
	}

	@Override
	protected void handleDamage(Level level, int x, int y, Entity source, @Nullable Item item, int dmg) {

	}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return e.canWool();
	}

	public enum WoolType {
		BLACK("Black Wool", new SpriteAnimation(SpriteType.Tile, "black_wool")),
		BLUE("Blue Wool", new SpriteAnimation(SpriteType.Tile, "blue_wool")),
		GREEN("Green Wool", new SpriteAnimation(SpriteType.Tile, "green_wool")),
		NORMAL("Wool", new SpriteAnimation(SpriteType.Tile, "white_wool")),
		RED("Red Wool", new SpriteAnimation(SpriteType.Tile, "red_wool")),
		YELLOW("Yellow Wool", new SpriteAnimation(SpriteType.Tile, "yellow_wool"));

		public final SpriteAnimation sprite;
		public final String name;

		/**
		 * Create a type of wool.
		 * @param sprite The sprite for the type of wool.
		 */
		WoolType(String name, SpriteAnimation sprite) {
			this.sprite = sprite;
			this.name = name;
		}
	}
}
