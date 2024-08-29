package minicraft.item;

import minicraft.gfx.SpriteLinker;
import minicraft.util.MyUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class DyeItem extends StackableItem {

	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<>();

		for (DyeColor color : DyeColor.values()) {
			items.add(new DyeItem(MyUtils.capitalizeFully(color.toString().replace('_', ' ')) + " Dye", new SpriteLinker.LinkedSprite(
				SpriteLinker.SpriteType.Item, color.toString().toLowerCase() + "_dye"), color));
		}

		return items;
	}

	public final DyeColor color;

	protected DyeItem(String name, SpriteLinker.LinkedSprite sprite, DyeColor color) {
		super(name, sprite);
		this.color = color;
	}

	public enum DyeColor {
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
		WHITE(0x1_F9FFFE);

		public final int color;

		DyeColor(int color) {
			this.color = color;
		}
	}

	@Override
	public @NotNull DyeItem copy() {
		return new DyeItem(getName(), sprite, color);
	}
}
