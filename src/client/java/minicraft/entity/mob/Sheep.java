package minicraft.entity.mob;

import minicraft.item.DyeItem;
import org.jetbrains.annotations.Nullable;

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

import java.util.HashMap;

public class Sheep extends PassiveMob {
	private static final HashMap<DyeItem.DyeColor, LinkedSprite[][]> sprites = new HashMap<>();
	private static final HashMap<DyeItem.DyeColor, LinkedSprite[][]> cutSprites = new HashMap<>();

	static {
		for (DyeItem.DyeColor color : DyeItem.DyeColor.values()) {
			LinkedSprite[][] mobSprites = Mob.compileMobSpriteAnimations(0, 0, "sheep");
			for (LinkedSprite[] mobSprite : mobSprites) {
				for (LinkedSprite linkedSprite : mobSprite) {
					linkedSprite.setColor(color.color);
				}
			}
			sprites.put(color, mobSprites);
			mobSprites = Mob.compileMobSpriteAnimations(0, 2, "sheep");
			for (LinkedSprite[] mobSprite : mobSprites) {
				for (LinkedSprite linkedSprite : mobSprite) {
					linkedSprite.setColor(color.color);
				}
			}
			cutSprites.put(color, mobSprites);
		}
	}

	public boolean cut = false;
	public DyeItem.DyeColor color;

	/**
	 * Creates a sheep entity.
	 */
	public Sheep() { this(DyeItem.DyeColor.WHITE); }
	public Sheep(DyeItem.DyeColor color) {
		super(null);
		this.color = color;
	}

	@Override
	public void render(Screen screen) {
		int xo = x - 8;
		int yo = y - 11;

		LinkedSprite[][] curAnim = cut ? cutSprites.get(color) : sprites.get(color);

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
		if (item instanceof ToolItem) {
			if (!cut && ((ToolItem) item).type == ToolType.Shears) {
				cut = true;
				dropItem(1, 3, Items.get(color.toString().replace('_', ' ') + " Wool"));
				((ToolItem) item).payDurability();
				return true;
			}
		} else if (item instanceof DyeItem) {
			color = ((DyeItem) item).color;
			((DyeItem) item).count--;
			return true;
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

		if (!cut) dropItem(min, max, Items.get(color.toString().replace('_', ' ') + " Wool"));
		dropItem(min, max, Items.get("Raw Beef"));

		super.die();
	}
}
