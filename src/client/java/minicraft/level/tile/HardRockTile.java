package minicraft.level.tile;

import minicraft.core.Game;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.Mob;
import minicraft.entity.mob.Player;
import minicraft.entity.particle.SmashParticle;
import minicraft.entity.particle.TextParticle;
import minicraft.gfx.Color;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteAnimation;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;
import minicraft.util.AdvancementElement;
import org.jetbrains.annotations.Nullable;

public class HardRockTile extends Tile {
	private static final int MAX_HEALTH = 200;

	// Theoretically the full sprite should never be used, so we can use a placeholder
	private static SpriteAnimation sprite = new SpriteAnimation(SpriteType.Tile, "hardrock")
		.setConnectionChecker((level, x, y, tile, side) -> tile instanceof HardRockTile)
		.setSingletonWithConnective(true);

	protected HardRockTile(String name) {
		super(name, sprite);
	}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return false;
	}

	@Override
	public boolean hurt(Level level, int x, int y, Entity source, @Nullable Item item, Direction attackDir, int damage) {
		if (Game.isMode("minicraft.settings.mode.creative")) {
			handleDamage(level, x, y, source, item, MAX_HEALTH);
			return true;
		}

		if (item instanceof ToolItem && source instanceof Player) {
			ToolItem tool = (ToolItem) item;

			// If we are hitting with a gem pickaxe.
			if (tool.type == ToolType.Pickaxe && tool.level == 4) {
				if (((Player) source).payStamina(2) && tool.payDurability()) {
					int data = level.getData(x, y);
					handleDamage(level, x, y, source, item, tool.getDamage());
					AdvancementElement.AdvancementTrigger.ItemUsedOnTileTrigger.INSTANCE.trigger(
						new AdvancementElement.AdvancementTrigger.ItemUsedOnTileTrigger.ItemUsedOnTileTriggerConditionHandler.ItemUsedOnTileTriggerConditions(
							item, this, data, x, y, level.depth));
					return true;
				}
			} else {
				Game.notifications.add("minicraft.notification.gem_pickaxe_required");
			}
		}

		handleDamage(level, x, y, source, item, 0);
		return true;
	}

	@Override
	protected void handleDamage(Level level, int x, int y, Entity source, @Nullable Item item, int dmg) {
		int damage = level.getData(x, y) + dmg;
		level.add(new SmashParticle(x << 4, y << 4));
		Sound.play("monsterhurt");

		level.add(new TextParticle("" + dmg, (x << 4) + 8, (y << 4) + 8, Color.RED));
		if (damage >= MAX_HEALTH) {
			level.setTile(x, y, Tiles.get("dirt"));
			level.dropItem((x << 4) + 8, (y << 4) + 8, 1, 3, Items.get("Stone"));
			level.dropItem((x << 4) + 8, (y << 4) + 8, 0, 1, Items.get("Coal"));
		} else {
			level.setData(x, y, damage);
		}
	}

	@Override
	public void render(Screen screen, Level level, int x, int y) {
		Tiles.get("dirt").render(screen, level, x, y);
		super.render(screen, level, x, y);
	}

	public boolean tick(Level level, int xt, int yt) {
		int damage = level.getData(xt, yt);
		if (damage > 0) {
			level.setData(xt, yt, damage - 1);
			return true;
		}
		return false;
	}
}
