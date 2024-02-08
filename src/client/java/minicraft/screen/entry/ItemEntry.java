package minicraft.screen.entry;

import minicraft.core.io.InputHandler;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemEntry extends ListEntry {

	public static ItemEntry[] useItems(List<Item> items) {
		ItemEntry[] entries = new ItemEntry[items.size()];
		for (int i = 0; i < items.size(); i++)
			entries[i] = new ItemEntry(items.get(i));
		return entries;
	}

	private Item item;

	public ItemEntry(Item i) {
		this.item = i;
	}

	public Item getItem() {
		return item;
	}

	@Override
	public void tick(InputHandler input) {
	}

	@Override
	public void render(Screen screen, @Nullable Screen.RenderingLimitingModel limitingModel, int x, int y, boolean isSelected) {
		super.render(screen, limitingModel, x + 16, y, true);
		screen.render(null, x, y, item.sprite);
	}

	@Override
	public void render(Screen screen, int x, int y, boolean isSelected, @Nullable IntRange bounds, String contain, int containColor) {
		if (!isVisible()) {
			return;
		}

		render(screen, x, y, isSelected, bounds);
		if (contain == null || contain.isEmpty()) {
			return;
		}

		String string = toString();

		Font.drawColor(string.replace(contain, String.format("%s%s%s", Color.toStringCode(isSelected ? containColor :
				Color.tint(containColor, -1, true)), contain, Color.WHITE_CODE)), screen, x + 16, y);
	}

	@Override
	public int getWidth() {
		return super.getWidth() + 16;
	}

	// If you add to the length of the string, and therefore the width of the entry, then it will actually move the entry RIGHT in the inventory, instead of the intended left, because it is auto-positioned to the left side.
	@Override
	public String toString() {
		return item.getDisplayName();
	}
}
