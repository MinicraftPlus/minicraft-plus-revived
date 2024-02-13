package minicraft.entity.mob;

import minicraft.core.io.Settings;
import minicraft.entity.Direction;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.tile.GrassTile;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;
import org.jetbrains.annotations.Nullable;

public class Sheep extends PassiveMob {
	private static final LinkedSprite[][] sprites = Mob.compileMobSpriteAnimations(0, 0, "sheep");
	private static final LinkedSprite[][] cutSprites = Mob.compileMobSpriteAnimations(0, 2, "sheep");

	public boolean cut = false;

	/**
	 * Creates a sheep entity.
	 */
	public Sheep() {
		super(sprites);
	}

	@Override
	public void render(Screen screen) {
		int xo = x - 8;
		int yo = y - 11;

		LinkedSprite[][] curAnim = cut ? cutSprites : sprites;

		LinkedSprite curSprite = curAnim[dir.getDir()][(walkDist >> 3) % curAnim[dir.getDir()].length];
		if (hurtTime > 0) {
			screen.render(xo, yo, curSprite.getSprite(), true);
		} else {
			screen.render(xo, yo, curSprite);
		}
	}

	@Override
	public void tick() {
		super.tick();
		Tile tile = level.getTile(x >> 4, y >> 4);
		if (tile instanceof GrassTile && random.nextInt(1000) == 0) { // Grazing
			level.setTile(x >> 4, y >> 4, Tiles.get("dirt"));
			cut = false;
		}
	}

	public boolean interact(Player player, @Nullable Item item, Direction attackDir) {
		if (cut) return false;

		if (item instanceof ToolItem) {
			if (((ToolItem) item).type == ToolType.Shears) {
				cut = true;
				dropItem(1, 3, Items.get("Wool"));
				((ToolItem) item).payDurability();
				return true;
			}
		}
		return false;
	}

	public void die() {
		int min = 0, max = 0;
		if (Settings.get("diff").equals("minicraft.settings.difficulty.easy")) {
			min = 1;
			max = 3;
		}
		if (Settings.get("diff").equals("minicraft.settings.difficulty.normal")) {
			min = 1;
			max = 2;
		}
		if (Settings.get("diff").equals("minicraft.settings.difficulty.hard")) {
			min = 0;
			max = 2;
		}

		if (!cut) dropItem(min, max, Items.get("wool"));
		dropItem(min, max, Items.get("Raw Beef"));

		super.die();
	}
}
