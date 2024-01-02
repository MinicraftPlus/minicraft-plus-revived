package minicraft.level.tile;

import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.Mob;
import minicraft.entity.mob.Player;
import minicraft.entity.particle.SandParticle;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteAnimation;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;
import minicraft.util.AdvancementElement;

public class SandTile extends Tile {
	private static SpriteAnimation sprite = new SpriteAnimation(SpriteType.Tile, "sand")
		.setConnectChecker((tile, side) -> !side || tile.connectsToSand)
		.setSingletonWithConnective(true);

	protected SandTile(String name) {
		super(name, sprite);
		connectsToSand = true;
		maySpawn = true;
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
			if (random.nextInt(3) == 0) {
				int spawnX = entity.x - 8 + random.nextInt(5) - 2;
				int spawnY = entity.y - 8 + random.nextInt(5) - 2;
				for (Direction dir : Direction.values()) {
					Tile neighbour = level.getTile(x + dir.getX(), y + dir.getY());
					if (neighbour != null) {
						if (!(neighbour instanceof SandTile)) { // Particles only spawn on sand tiles.
							if (dir.getX() < 0) // Offsets
								if (entity.x % 16 < 8) spawnX += 8 - entity.x % 16;
							if (dir.getX() > 0)
								if (entity.x % 16 > 7) spawnX -= entity.x % 16 - 8;
							if (dir.getY() < 0)
								if (entity.y % 16 < 8) spawnY += 8 - entity.y % 16;
							if (dir.getY() > 0)
								if (entity.y % 16 > 7) spawnY -= entity.y % 16 - 8;
						}
					}
				}

				level.add(new SandParticle(spawnX, spawnY));
			}
		}
	}

	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.Shovel) {
				if (player.payStamina(4 - tool.level) && tool.payDurability()) {
					int data = level.getData(xt, yt);
					level.setTile(xt, yt, Tiles.get("Hole"));
					Sound.play("monsterhurt");
					level.dropItem(xt * 16 + 8, yt * 16 + 8, Items.get("Sand"));
					AdvancementElement.AdvancementTrigger.ItemUsedOnTileTrigger.INSTANCE.trigger(
						new AdvancementElement.AdvancementTrigger.ItemUsedOnTileTrigger.ItemUsedOnTileTriggerConditionHandler.ItemUsedOnTileTriggerConditions(
							item, this, data, xt, yt, level.depth));
					return true;
				}
			}
		}
		return false;
	}
}
