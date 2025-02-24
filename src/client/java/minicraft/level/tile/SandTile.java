package minicraft.level.tile;

import minicraft.core.Game;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.Mob;
import minicraft.entity.mob.Player;
import minicraft.entity.particle.SandParticle;
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

public class SandTile extends Tile {
	private static SpriteAnimation sprite = new SpriteAnimation(SpriteType.Tile, "sand")
		.setConnectionChecker((level, x, y, tile, side) -> tile.connectsToSand(level, x, y))
		.setSingletonWithConnective(true);

	protected SandTile(String name) {
		super(name, sprite);
		maySpawn = true;
	}

	@Override
	public boolean connectsToSand(Level level, int x, int y) {
		return true;
	}

	public void render(Screen screen, Level level, int x, int y) {
		Tiles.get("dirt").render(screen, level, x, y);
		sprite.render(screen, level, x, y);
	}

	public boolean tick(Level level, int x, int y) {
		return false;
	}

	public void steppedOn(Level level, int x, int y, Entity entity) {
		if (entity instanceof Mob) {
			if (((Mob) entity).walkDist % 8 == 0) { // Mob animation changes by every 2^3 in walkDist.
				int spawnX = entity.x - 4; // Shifting is done to ensure that the center of particle is located
				int spawnY = entity.y - 4; // at the center of the mob.
				switch (((Mob) entity).dir) { // Shifting along the orthogonal axis of direction.
					case NONE: // UNKNOWN
						return;
					case UP: case DOWN: // y-axis
						spawnX += (random.nextInt(2) + 2) * (((((Mob) entity).walkDist) >> 3) % 2 == 0 ? 1 : -1);
						break;
					case LEFT: case RIGHT: // x-axis
						spawnY += (random.nextInt(2) + 2) * (((((Mob) entity).walkDist) >> 3) % 2 == 0 ? 1 : -1);
						break;
				}

				level.add(new SandParticle(spawnX, spawnY));
			}
		}
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
			level.dropItem((x << 4) + 8, (y << 4) + 8, Items.get("Sand"));
			return true;
		}

		if (item instanceof ToolItem && source instanceof Player) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.Shovel) {
				if (((Player) source).payStamina(4 - tool.level) && tool.payDurability()) {
					int data = level.getData(x, y);
					level.setTile(x, y, Tiles.get("Hole"));
					Sound.play("monsterhurt");
					level.dropItem((x << 4) + 8, (y << 4) + 8, Items.get("Sand"));
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
}
