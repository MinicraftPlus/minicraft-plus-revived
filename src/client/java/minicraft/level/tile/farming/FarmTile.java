package minicraft.level.tile.farming;

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
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;
import minicraft.level.tile.WaterTile;
import minicraft.util.AdvancementElement;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class FarmTile extends Tile {
	private static final SpriteAnimation sprite = new SpriteAnimation(SpriteType.Tile, "farmland");
	private static final SpriteAnimation spriteMoist = new SpriteAnimation(SpriteType.Tile, "farmland_moist");

	public FarmTile(String name) {
		super(name, sprite);
	}

	protected FarmTile(String name, SpriteAnimation sprite) {
		super(name, sprite);
	}

	@Override
	protected void handleDamage(Level level, int x, int y, Entity source, @Nullable Item item, int dmg) {
		level.add(new SmashParticle(x << 4, y << 4));
		level.add(new TextParticle("" + dmg, (x << 4) + 8, (y << 4) + 8, Color.RED));
	}

	@Override
	public boolean hurt(Level level, int x, int y, Entity source, @Nullable Item item, Direction attackDir, int damage) {
		if (Game.isMode("minicraft.settings.mode.creative")) {
			level.setTile(x, y, Tiles.get("Hole"));
			Sound.play("monsterhurt");
			level.dropItem((x << 4) + 8, (y << 4) + 8, Items.get("Dirt"));
			return true;
		}
		
		if (item instanceof ToolItem && source instanceof Player) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.Shovel) {
				if (((Player) source).payStamina(4 - tool.level) && tool.payDurability()) {
					int data = level.getData(x, y);
					level.setTile(x, y, Tiles.get("Dirt"));
					Sound.play("monsterhurt");
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

	@Override
	public boolean tick(Level level, int xt, int yt) {
		int moisture = level.getData(xt, yt) & 0b111;
		if (Arrays.stream(level.getAreaTiles(xt, yt, 4)).anyMatch(t -> t instanceof WaterTile)) { // Contains water.
			if (moisture < 7 && random.nextInt(10) == 0) { // hydrating
				level.setData(xt, yt, moisture + 1);
				return true;
			}
		} else if (moisture > 0 && random.nextInt(10) == 0) { // drying
			level.setData(xt, yt, moisture - 1);
			return true;
		} else if (moisture == 0 && random.nextInt(10) == 0) {
			level.setTile(xt, yt, Tiles.get("dirt"));
			return true;
		}

		return false;
	}

	@Override
	public void render(Screen screen, Level level, int x, int y) {
		if ((level.getData(x, y) & 0b111) > 0)
			spriteMoist.render(screen, level, x, y);
		else
			sprite.render(screen, level, x, y);
	}
}
