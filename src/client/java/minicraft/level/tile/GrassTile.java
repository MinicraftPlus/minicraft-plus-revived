package minicraft.level.tile;

import minicraft.core.Game;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
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

public class GrassTile extends Tile {
	private static final SpriteAnimation sprite = new SpriteAnimation(SpriteType.Tile, "grass")
		.setConnectionChecker((level, x, y, tile, side) -> !side || tile.connectsToGrass(level, x, y))
		.setSingletonWithConnective(true);

	protected GrassTile(String name) {
		super(name, sprite);
		maySpawn = true;
	}

	@Override
	public boolean connectsToGrass(Level level, int x, int y) {
		return true;
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

	@Override
	protected void handleDamage(Level level, int x, int y, Entity source, @Nullable Item item, int dmg) {
		level.add(new SmashParticle(x << 4, y << 4));
		level.add(new TextParticle("" + dmg, (x << 4) + 8, (y << 4) + 8, Color.RED));
	}

	@Override
	public boolean hurt(Level level, int x, int y, Entity source, @Nullable Item item, Direction attackDir, int damage) {
		if (Game.isMode("minicraft.settings.mode.creative")) {
			level.setTile(x, y, Tiles.get("Dirt"));
			Sound.play("monsterhurt");
			if (random.nextInt(5) == 0) { // 20% chance to drop Grass seeds
				level.dropItem((x << 4) + 8, (y << 4) + 8, 1, Items.get("Grass Seeds"));
			}
			return true;
		}

		if (item instanceof ToolItem && source instanceof Player) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.Shovel) {
				if (((Player) source).payStamina(4 - tool.level) && tool.payDurability()) {
					int data = level.getData(x, y);
					level.setTile(x, y, Tiles.get("Dirt"));
					Sound.play("monsterhurt");
					if (random.nextInt(5) == 0) { // 20% chance to drop Grass seeds
						level.dropItem((x << 4) + 8, (y << 4) + 8, 1, Items.get("Grass Seeds"));
					}
					AdvancementElement.AdvancementTrigger.ItemUsedOnTileTrigger.INSTANCE.trigger(
						new AdvancementElement.AdvancementTrigger.ItemUsedOnTileTrigger.ItemUsedOnTileTriggerConditionHandler.ItemUsedOnTileTriggerConditions(
							item, this, data, x, y, level.depth));
					return true;
				}
			}
			if (tool.type == ToolType.Hoe) {
				if (((Player) source).payStamina(4 - tool.level) && tool.payDurability()) {
					int data = level.getData(x, y);
					level.setTile(x, y, Tiles.get("Farmland"));
					Sound.play("monsterhurt");
					if (random.nextInt(5) != 0) { // 80% chance to drop Wheat seeds
						level.dropItem((x << 4) + 8, (y << 4) + 8, Items.get("Wheat Seeds"));
					}
					AdvancementElement.AdvancementTrigger.ItemUsedOnTileTrigger.INSTANCE.trigger(
						new AdvancementElement.AdvancementTrigger.ItemUsedOnTileTrigger.ItemUsedOnTileTriggerConditionHandler.ItemUsedOnTileTriggerConditions(
							item, this, data, x, y, level.depth));
					return true;
				}
			}
			if (tool.type == ToolType.Pickaxe) {
				if (((Player) source).payStamina(4 - tool.level) && tool.payDurability()) {
					level.setTile(x, y, Tiles.get("Path"));
					Sound.play("monsterhurt");
				}
			}
		}

		handleDamage(level, x, y, source, item, 0);
		return true;
	}
}
