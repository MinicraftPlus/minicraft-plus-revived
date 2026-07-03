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
import minicraft.level.Level;
import minicraft.util.AdvancementElement;

public class MaterialTile extends Tile {
	protected Material type;

	protected MaterialTile(Material type) {
		super((type == Material.Stone ? "Stone" : type == Material.Obsidian ? "Raw Obsidian" : type == Material.LavaStone ? "Lava Stone" : type.name()), (SpriteAnimation) null);
		this.type = type;
		maySpawn = true;
		switch (type) {
			case Stone:
				sprite = new SpriteAnimation(SpriteType.Tile, "stone");
				break;
			case Obsidian:
				sprite = new SpriteAnimation(SpriteType.Tile, "obsidian");
				break;
                        case LavaStone:
                                sprite = new SpriteAnimation(SpriteType.Tile, "lava_stone");
                                break;
			default:
		}
	}

	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == type.getRequiredTool()) {
				if (player.payStamina(4 - tool.level) && tool.payDurability()) {
					int data = level.getData(xt, yt);
					if (level.depth == 1) {
						level.setTile(xt, yt, Tiles.get("Cloud"));
					} else {
						level.setTile(xt, yt, Tiles.get("Hole"));
					}
					Item drop;
					switch (type) {
						case Stone:
							drop = Items.get("Stone");
							break;
						case Obsidian:
							drop = Items.get("Raw Obsidian");
							break;
                                                case LavaStone:
                                                        drop = Items.get("Lava Stone");
                                                        break;
						default:
							throw new IllegalStateException("Unexpected value: " + type);
					}
					Sound.play("monsterhurt");
					level.dropItem((xt << 4) + 8, (yt << 4) + 8, drop);
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
		return true;
	}
}
