package minicraft.level.tile;

import minicraft.core.Game;
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
import minicraft.item.ToolType;
import minicraft.level.Level;
import minicraft.util.AdvancementElement;
import org.jetbrains.annotations.Nullable;

public class CloudTile extends Tile {
	private static SpriteAnimation sprite = new SpriteAnimation(SpriteType.Tile, "cloud")
		.setConnectionChecker((level, x, y, tile, side) -> !(tile instanceof InfiniteFallTile))
		.setSingletonWithConnective(true);

	protected CloudTile(String name) {
		super(name, sprite);
	}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return true;
	}

	@Override
	protected void handleDamage(Level level, int x, int y, Entity source, @Nullable Item item, int dmg) {
		level.add(new SmashParticle(x << 4, y << 4));
		level.add(new TextParticle("" + dmg, (x << 4) + 8, (y << 4) + 8, Color.RED));
	}

	@Override
	public boolean hurt(Level level, int x, int y, Entity source, @Nullable Item item, Direction attackDir, int damage) {
		if (Game.isMode("minicraft.settings.mode.creative")) {
			level.setTile(x, y, Tiles.get("Infinite Fall")); // Would allow you to shovel cloud, I think.
			Sound.play("monsterhurt");
			level.dropItem((x << 4) + 8, (y << 4) + 8, 1, 3, Items.get("Cloud"));
			return true;
		}

		// We don't want the tile to break when attacked with just anything
		if (source instanceof Player && item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.Shovel && ((Player) source).payStamina(5)) {
				int data = level.getData(x, y);
				level.setTile(x, y, Tiles.get("Infinite Fall")); // Would allow you to shovel cloud, I think.
				Sound.play("monsterhurt");
				level.dropItem((x << 4) + 8, (y << 4) + 8, 1, 3, Items.get("Cloud"));
				AdvancementElement.AdvancementTrigger.ItemUsedOnTileTrigger.INSTANCE.trigger(
					new AdvancementElement.AdvancementTrigger.ItemUsedOnTileTrigger.ItemUsedOnTileTriggerConditionHandler.ItemUsedOnTileTriggerConditions(
						item, this, data, x, y, level.depth));
				return true;
			}
		}

		handleDamage(level, x, y, source, item, 0);
		return true;
	}
}
