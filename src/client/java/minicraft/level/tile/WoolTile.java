package minicraft.level.tile;

import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.Player;
import minicraft.gfx.SpriteAnimation;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.item.DyeItem;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;
import minicraft.util.AdvancementElement;

import java.util.HashMap;

public class WoolTile extends Tile {
	private static final HashMap<DyeItem.DyeColor, SpriteAnimation> sprites = new HashMap<>();

	static {
		for (DyeItem.DyeColor color : DyeItem.DyeColor.values()) {
			sprites.put(color, new SpriteAnimation(SpriteType.Tile, color.toString().toLowerCase() + "_wool"));
		}
	}

	public WoolTile(DyeItem.DyeColor color) {
		super(color.toString().replace('_', ' ') + " Wool", sprites.get(color));
	}

	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.Shears) {
				if (player.payStamina(3 - tool.level) && tool.payDurability()) {
					int data = level.getData(xt, yt);
					level.setTile(xt, yt, Tiles.get("Hole"));
					Sound.play("monsterhurt");
					level.dropItem((xt << 4) + 8, (yt << 4) + 8, Items.get(name));
					AdvancementElement.AdvancementTrigger.ItemUsedOnTileTrigger.INSTANCE.trigger(
						new AdvancementElement.AdvancementTrigger.ItemUsedOnTileTrigger.ItemUsedOnTileTriggerConditionHandler.ItemUsedOnTileTriggerConditions(
							item, this, data, xt, yt, level.depth));
					return true;
				}
			}
		}
		return false;
	}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return e.canWool();
	}
}
