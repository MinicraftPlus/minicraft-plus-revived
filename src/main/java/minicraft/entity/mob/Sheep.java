package minicraft.entity.mob;

import org.jetbrains.annotations.Nullable;

import minicraft.core.Updater;
import minicraft.core.io.Settings;
import minicraft.entity.Direction;
import minicraft.gfx.MobSprite;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;

public class Sheep extends PassiveMob {
	private static final LinkedSprite sprites = new LinkedSprite(SpriteType.Entity, "sheep");
	private static final LinkedSprite cutSprites = new LinkedSprite(SpriteType.Entity, "sheep").setSpritePos(0, 2);

	private static final int WOOL_GROW_TIME = 3 * 60 * Updater.normSpeed; // Three minutes

	public boolean cut = false;
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

		MobSprite[][] curAnim = cut ? cutSprites.getMobSprites() : sprites.getMobSprites();

		MobSprite curSprite = curAnim[dir.getDir()][(walkDist >> 3) % curAnim[dir.getDir()].length];
		if (hurtTime > 0) {
			curSprite.render(screen, xo, yo, true);
		} else {
			curSprite.render(screen, xo, yo);
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
