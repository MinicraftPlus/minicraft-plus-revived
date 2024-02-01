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

public class DecorTile extends Tile {
	private static SpriteAnimation stoneSprite = new SpriteAnimation(SpriteType.Tile, "ornate_stone");
	private static SpriteAnimation obsidianSprite = new SpriteAnimation(SpriteType.Tile, "ornate_obsidian");

	protected Material type;

	protected DecorTile(Material type) {
		super((type == Material.Obsidian ? "Ornate Obsidian" : type == Material.Stone ? "Ornate Stone" : "Decorated " + type.name()),
			type == Material.Stone ? stoneSprite : obsidianSprite);
		this.type = type;
		maySpawn = true;
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
							drop = Items.get("Ornate Stone");
							break;
						case Obsidian:
							drop = Items.get("Ornate Obsidian");
							break;
						default:
							throw new IllegalStateException("Unexpected value: " + type);
					}
					Sound.play("monsterhurt");
					level.dropItem(xt * 16 + 8, yt * 16 + 8, drop);
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
