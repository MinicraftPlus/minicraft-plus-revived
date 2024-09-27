package minicraft.item;

import minicraft.gfx.SpriteLinker;
import minicraft.util.MyUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class WoolItem extends TileItem {
	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<>();

		for (DyeItem.DyeColor color : DyeItem.DyeColor.values()) {
			items.add(new WoolItem(MyUtils.capitalizeFully(color.toString().replace('_', ' ')) + " Wool", new SpriteLinker.LinkedSprite(
				SpriteLinker.SpriteType.Item, color.toString().toLowerCase() + "_wool"), color));
		}

		return items;
	}

	public final DyeItem.DyeColor color;

	protected WoolItem(String name, SpriteLinker.LinkedSprite sprite, DyeItem.DyeColor color) {
		super(name, sprite, new TileModel(name), "hole", "water");
		this.color = color;
	}

	@Override
	public @NotNull TileItem copy() {
		return new WoolItem(getName(), sprite, color);
	}
}
