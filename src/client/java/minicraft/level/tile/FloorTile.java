package minicraft.level.tile;

import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.Player;
import minicraft.entity.particle.SmashParticle;
import minicraft.entity.particle.TextParticle;
import minicraft.gfx.Color;
import minicraft.gfx.SpriteAnimation;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.level.Level;
import minicraft.util.AdvancementElement;
import org.jetbrains.annotations.Nullable;

public class FloorTile extends Tile {
	protected Material type;

	protected FloorTile(Material type) {
		this(type, null);
	}

	protected FloorTile(Material type, String name) {
		super((type == Material.Wood ? "Wood Planks" : type == Material.Obsidian ? "Obsidian" + (name == null ? "" : " " + name) : type.name() + " " + (name == null ? "Bricks" : name)), null);
		this.type = type;
		maySpawn = true;
		switch (type) {
			case Wood:
				sprite = new SpriteAnimation(SpriteType.Tile, "wood_floor");
				break;
			case Stone:
				sprite = new SpriteAnimation(SpriteType.Tile, "stone_floor");
				break;
			case Obsidian:
				sprite = new SpriteAnimation(SpriteType.Tile, "obsidian_floor");
				break;
		}
	}

	@Override
	public boolean hurt(Level level, int x, int y, Entity source, @Nullable Item item, Direction attackDir, int damage) {
		if (item instanceof ToolItem && source instanceof Player) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == type.getRequiredTool()) {
				if (((Player) source).payStamina(4 - tool.level) && tool.payDurability()) {
					int data = level.getData(x, y);
					if (level.depth == 1) {
						level.setTile(x, y, Tiles.get("Cloud"));
					} else {
						level.setTile(x, y, Tiles.get("Hole"));
					}
					Item drop;
					switch (type) {
						case Wood:
							drop = Items.get("Plank");
							break;
						default:
							drop = Items.get(type.name() + " Brick");
							break;
					}
					Sound.play("monsterhurt");
					level.dropItem((x << 4) + 8, (y << 4) + 8, drop);
					AdvancementElement.AdvancementTrigger.ItemUsedOnTileTrigger.INSTANCE.trigger(
						new AdvancementElement.AdvancementTrigger.ItemUsedOnTileTrigger.ItemUsedOnTileTriggerConditionHandler.ItemUsedOnTileTriggerConditions(
							item, this, data, x, y, level.depth));
					return true;
				}
			}
		}

		handleDamage(level, x, y, source, item, 0);
		return true;
	}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return true;
	}

	@Override
	protected void handleDamage(Level level, int x, int y, Entity source, @Nullable Item item, int dmg) {
		level.add(new SmashParticle(x << 4, y << 4));
		level.add(new TextParticle("" + dmg, (x << 4) + 8, (y << 4) + 8, Color.RED));
	}
}
