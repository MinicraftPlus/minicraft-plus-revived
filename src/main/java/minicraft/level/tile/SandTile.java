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
			level.add(new SandParticle(entity.x - 8 + random.nextInt(5)-2, entity.y - 8 + random.nextInt(5)-2));
		}
	}

	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.Shovel) {
				if (player.payStamina(4 - tool.level) && tool.payDurability()) {
					level.setTile(xt, yt, Tiles.get("Hole"));
					Sound.play("monsterhurt");
					level.dropItem(xt * 16 + 8, yt * 16 + 8, Items.get("Sand"));
					return true;
				}
			}
		}
		return false;
	}
}
