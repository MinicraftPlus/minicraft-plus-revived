package minicraft.entity.mob;

import minicraft.gfx.Color;
import org.jetbrains.annotations.Nullable;

import minicraft.core.Updater;
import minicraft.core.io.Settings;
import minicraft.entity.Direction;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;

public class Sheep extends PassiveMob {
	private static final LinkedSprite[][] sprites = Mob.compileMobSpriteAnimations(0, 0, "sheep");
	private static final LinkedSprite[][] cutSprites = Mob.compileMobSpriteAnimations(0, 2, "sheep");

	private static final int WOOL_GROW_TIME = 3 * 60 * Updater.normSpeed; // Three minutes

	public enum ColorSet {
		BLACK(0x1_1D1D21),
		RED(0x1_B02E26),
		GREEN(0x1_5E7C16),
		BROWN(0x1_835432),
		BLUE(0x1_3C44AA),
		PURPLE(0x1_8932B8),
		CYAN(0x1_169C9C),
		LIGHT_GRAY(0x1_9D9D97),
		GRAY(0x1_474F52),
		PINK(0x1_F38BAA),
		LIME(0x1_80C71F),
		YELLOW(0x1_FED83D),
		LIGHT_BLUE(0x1_3AB3DA),
		MAGENTA(0x1_C74EBD),
		ORANGE(0x1_F9801D),
		WHITE(0x1_F9FFFE),
		;

		public final int color;

		ColorSet(int color) {
			this.color = color;
		}
	}

	public boolean cut = false;
	public ColorSet color = ColorSet.WHITE;
	private int ageWhenCut = 0;

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
		if (age - ageWhenCut > WOOL_GROW_TIME) cut = false;
	}

	public boolean interact(Player player, @Nullable Item item, Direction attackDir) {
		if (cut) return false;

		if (item instanceof ToolItem) {
			if (((ToolItem) item).type == ToolType.Shears) {
				cut = true;
				dropItem(1, 3, Items.get("Wool"));
				ageWhenCut = age;
				((ToolItem) item).payDurability();
				return true;
			}
		}
		return false;
	}

	public void die() {
		int min = 0, max = 0;
		if (Settings.get("diff").equals("minicraft.settings.difficulty.easy")) {min = 1; max = 3;}
		if (Settings.get("diff").equals("minicraft.settings.difficulty.normal")) {min = 1; max = 2;}
		if (Settings.get("diff").equals("minicraft.settings.difficulty.hard")) {min = 0; max = 2;}

		if (!cut) dropItem(min, max, Items.get("wool"));
		dropItem(min, max, Items.get("Raw Beef"));

		super.die();
	}
}
