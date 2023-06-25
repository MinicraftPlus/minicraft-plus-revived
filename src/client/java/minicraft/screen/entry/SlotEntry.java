package minicraft.screen.entry;

import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.core.io.Sound;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteLinker;
import minicraft.item.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SlotEntry extends ListEntry {
	private final SlotInteractionListener onSelect;
	private final SlotEntryPlaceholder placeholder;

	private @Nullable Item item; // null when empty

	public SlotEntry(@NotNull SlotEntryPlaceholder placeholder, @Nullable SlotInteractionListener onSelect) {
		this.onSelect = onSelect;
		this.placeholder = placeholder;
	}

	// Item depletion is not handled here by itself.

	public void setItem(@Nullable Item item) {
		this.item = item;
	}

	public @Nullable Item getItem() {
		return item;
	}

	@Override
	public void tick(InputHandler input) {
		if (input.inputPressed("select") && onSelect != null) {
			Sound.play("confirm");
			onSelect.onInteract(this, input);
		}
	}

	@Override
	public void render(Screen screen, int x, int y, boolean isSelected) {
		super.render(screen, x, y, isSelected);
		SpriteLinker.LinkedSprite sprite;
		if (item == null && (sprite = placeholder.getSprite()) != null)
			screen.render(x, y, sprite);
		else
			screen.render(x, y, item.sprite);
	}

	@Override
	public int getColor(boolean isSelected) {
		return item == null ? placeholder.getDisplayColor(isSelected) : super.getColor(isSelected);
	}

	@Override
	public String toString() {
		return item == null ? placeholder.getDisplayString() : item.getDisplayName();
	}

	@FunctionalInterface
	public interface SlotInteractionListener {
		void onInteract(SlotEntry slot, InputHandler input);
	}

	public static class SlotEntryPlaceholder {
		private static final int LIGHT_GRAY = Color.tint(Color.GRAY, 1, true);

		private final String text;
		private final @Nullable SpriteLinker.LinkedSprite sprite;

		public SlotEntryPlaceholder(String text) { this(text, null); }
		public SlotEntryPlaceholder(String text, @Nullable SpriteLinker.LinkedSprite sprite) {
			this.text = text;
			this.sprite = sprite;
		}

		public String getDisplayString() { return text; }
		public int getDisplayColor(boolean isSelected) { return isSelected ? LIGHT_GRAY : Color.DARK_GRAY; }
		/** @return must be an 8*8 (1*1 block) LinkedSprite */
		public @Nullable SpriteLinker.LinkedSprite getSprite() { return sprite; }
	}

	public static class SingletonItemSlotEntryPlaceholder extends SlotEntryPlaceholder {
		public SingletonItemSlotEntryPlaceholder(@NotNull Item item) {
			super(Localization.getLocalized(item.getName()), item.sprite);
		}
	}

	@SuppressWarnings("unused") // Reserved for future use
	public static class ReadonlySlotEntry extends SlotEntry {
		public ReadonlySlotEntry(@NotNull SlotEntryPlaceholder placeholder) {
			super(placeholder, null);
			setSelectable(false);
		}
	}

	/** Non-selectable when no item is in this slot */
	public static class TemporarySlotEntry extends SlotEntry {
		public TemporarySlotEntry(@NotNull SlotEntryPlaceholder placeholder, @Nullable SlotInteractionListener onSelect) {
			super(placeholder, onSelect);
			setSelectable(false);
		}

		@Override
		public void setItem(@Nullable Item item) {
			super.setItem(item);
			setSelectable(item != null);
		}
	}
}
