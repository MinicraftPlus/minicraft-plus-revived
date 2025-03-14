package minicraft.screen.entry;

import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.core.io.Sound;
import minicraft.gfx.Color;
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
		if (item == null) {
			SpriteLinker.LinkedSprite sprite = placeholder.getSprite();
			if (sprite != null) screen.render(x, y, sprite);
		} else
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

	public static abstract class SynchronizedSlotEntry extends ListEntry { // Used when the slot is protected.
		private final SlotEntryPlaceholder placeholder;

		public SynchronizedSlotEntry(@NotNull SlotEntryPlaceholder placeholder) {
			this.placeholder = placeholder;
		}

		/** This is invoked when a select action is inputted on the slot in a display. */
		protected abstract void onSelect(InputHandler input);

		/**
		 * Safely withdraw the item inside the slot. This is a modification actor.
		 * @param whole {@code true} if willing to withdraw the whole stack until reaching the maximum stack size
		 * @param maxStackSize the maximum stack size of the returned item object
		 * @return the withdrawn item object from the slot
		 */
		public @Nullable Item withdrawSlot(boolean whole, int maxStackSize) {
			return withdrawSlot(whole ? maxStackSize : 1);
		}
		/**
		 * Safely withdraw the item inside the slot. This is a modification actor.
		 * @param count the amount of items willing to withdraw
		 * @return the withdrawn item object from the slot
		 */
		public abstract @Nullable Item withdrawSlot(int count);

		/**
		 * Safely deposit the provided item into the slot. This is a modification actor.
		 * @param item the item object to be deposited into the slot
		 * @return {@code true} if the provided {@code item} is not depleted
		 */
		public abstract boolean depositSlot(Item item);

		/**
		 * Getting the current item object in the current state of the slot.
		 * No modification to the slot should be made.
		 * @return a clone of the current slot (for safety); {@code null} if the current slot is empty
		 * @see #isEmpty()
		 */
		public abstract @Nullable Item examineSlot();

		@Override
		public void tick(InputHandler input) {
			if (input.inputPressed("select")) {
				Sound.play("confirm");
				onSelect(input);
			}
		}

		@Override
		public void render(Screen screen, int x, int y, boolean isSelected) {
			super.render(screen, x, y, isSelected);
			if (isEmpty()) {
				SpriteLinker.LinkedSprite sprite = placeholder.getSprite();
				if (sprite != null) screen.render(x, y, sprite);
			} else
				renderItemSprite(screen, x, y);
		}

		/**
		 * Quick examine to the existence of the slot to not create a new clone or object.
		 * This is associated with {@link #getColor(boolean)} and {@link #toString()} by default.
		 * @return {@code true} if the current slot is empty
		 * @see #renderItemSprite(Screen, int, int)
		 * @see #getItemDisplayString()
		 */
		public abstract boolean isEmpty();

		/**
		 * Rendering the current item sprite of the slot.
		 * This is called by {@link #render(Screen, int, int, boolean)} only when the slot is not empty by default.
		 * @see #isEmpty()
		 */
		protected abstract void renderItemSprite(Screen screen, int x, int y);

		@Override
		public int getColor(boolean isSelected) {
			return isEmpty() ? placeholder.getDisplayColor(isSelected) : super.getColor(isSelected);
		}

		/**
		 * Getting the display string of the current item in the slot.
		 * This is called by {@link #toString()} only when the slot is not empty by default.
		 * @return the display string associated with the item in the slot; {@code null} is expected when the slot is empty
		 * @see #isEmpty()
		 */
		protected abstract @Nullable String getItemDisplayString();

		@Override
		public String toString() {
			String itemDisplayName;
			return isEmpty() || (itemDisplayName = getItemDisplayString()) == null ? placeholder.getDisplayString() : itemDisplayName;
		}
	}

	public static class SingletonItemSlotEntryPlaceholder extends SlotEntryPlaceholder {
		public SingletonItemSlotEntryPlaceholder(@NotNull Item item) {
			super(" " + Localization.getLocalized(item.getName()), item.sprite);
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
