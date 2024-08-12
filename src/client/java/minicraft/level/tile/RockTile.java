package minicraft.level.tile;

import minicraft.core.Game;
import minicraft.core.io.Settings;
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

// This is the normal stone you see underground and on the surface, that drops coal and stone.

public class RockTile extends Tile {
	private static final int MAX_HEALTH = 50;

	private static SpriteAnimation sprite = new SpriteAnimation(SpriteType.Tile, "rock")
		.setConnectionChecker((level, x, y, tile, side) -> tile instanceof RockTile)
		.setSingletonWithConnective(true);

	private boolean dropCoal = false;

	private int damage;

	protected RockTile(String name) {
		super(name, sprite);
	}

	public void render(Screen screen, Level level, int x, int y) {
		Tiles.get("dirt").render(screen, level, x, y);
		sprite.render(screen, level, x, y);
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

		// Can only be reached when player hits w/o pickaxe, so remove ability to get coal
		if (item instanceof ToolItem && source instanceof Player) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.Pickaxe && ((Player) source).payStamina(5 - tool.level) && tool.payDurability()) {
				int data = level.getData(x, y);
				// Drop coal since we use a pickaxe.
				handleDamage(level, x, y, source, item, tool.getDamage());
				AdvancementElement.AdvancementTrigger.ItemUsedOnTileTrigger.INSTANCE.trigger(
					new AdvancementElement.AdvancementTrigger.ItemUsedOnTileTrigger.ItemUsedOnTileTriggerConditionHandler.ItemUsedOnTileTriggerConditions(
						item, this, data, x, y, level.depth));
				return true;
			}
		}

		return false;
	}

	@Override
	protected void handleDamage(Level level, int x, int y, Entity source, @Nullable Item item, int dmg) {
		damage = level.getData(x, y) + dmg;

		level.add(new SmashParticle(x << 4, y << 4));
		Sound.play("monsterhurt");

		level.add(new TextParticle("" + dmg, (x << 4) + 8, (y << 4) + 8, Color.RED));
		if (damage >= MAX_HEALTH) {
			int stone = 1;
// 			if (dropCoal) {
				stone += random.nextInt(3) + 1;

				int coal = 1;
				if (!Settings.get("diff").equals("minicraft.settings.difficulty.hard")) {
					coal += 1;
				}

				level.dropItem((x << 4) + 8, (y << 4) + 8, 0, coal, Items.get("Coal"));
// 			}

			level.dropItem((x << 4) + 8, (y << 4) + 8, stone, Items.get("Stone"));
			level.setTile(x, y, Tiles.get("Dirt"));
		} else {
			level.setData(x, y, damage);
		}
	}

	public boolean tick(Level level, int xt, int yt) {
		damage = level.getData(xt, yt);
		if (damage > 0) {
			level.setData(xt, yt, damage - 1);
			return true;
		}
		return false;
	}
}
