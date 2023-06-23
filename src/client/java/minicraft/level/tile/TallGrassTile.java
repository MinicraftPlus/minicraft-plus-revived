package minicraft.level.tile;

import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.mob.Mob;
import minicraft.entity.mob.Player;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteAnimation;
import minicraft.gfx.SpriteLinker;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.level.Level;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TallGrassTile extends Tile {
	public enum TallGrassType {
		GRASS(new SpriteAnimation(SpriteLinker.SpriteType.Tile, "tall_grass")),
		TALL_GRASS(new SpriteAnimation(SpriteLinker.SpriteType.Tile, "double_tall_grass"));

		private final SpriteAnimation sprite;

		TallGrassType(SpriteAnimation sprite) {
			this.sprite = sprite.setConnectChecker((tile, side) -> !side || tile.connectsToGrass)
				.setSingletonWithConnective(true);
		}
	}

	public static final List<Short> grassIDs = Collections.unmodifiableList(Arrays.asList(
		(short) 52, (short) 53));

	protected TallGrassTile(String name, TallGrassType type) {
		super(name, type.sprite);
		connectsToGrass = true;
	}

	@Override
	public void render(Screen screen, Level level, int x, int y) {
		Tiles.get("grass").render(screen, level, x, y);
		sprite.render(screen, level, x, y);
	}

	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		level.setTile(xt, yt, Tiles.get("Grass"));
		Sound.play("monsterhurt");
		level.dropItem(xt * 16 + 8, yt * 16 + 8, 0, 1, Items.get("Wheat Seeds"));
		return true;
	}

	@Override
	public boolean hurt(Level level, int x, int y, Mob source, int dmg, Direction attackDir) {
		level.setTile(x, y, Tiles.get("Grass"));
		Sound.play("monsterhurt");
		level.dropItem(x * 16 + 8, y * 16 + 8, 0, 1, Items.get("Wheat Seeds"));
		return true;
	}
}
