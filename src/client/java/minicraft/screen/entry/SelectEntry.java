package minicraft.screen.entry;

import minicraft.core.Action;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.core.io.Sound;
import minicraft.gfx.Font;

public class SelectEntry extends ListEntry {

	private Action onSelect;
	private String text;
	private boolean localize;

	/**
	 * Creates a new entry which acts as a button.
	 * Can do an action when it is selected.
	 * Localizes the provided string.
	 *
	 * @param text     Text displayed on this entry
	 * @param onSelect Action which happens when the entry is selected
	 */
	public SelectEntry(String text, Action onSelect) {
		this(text, onSelect, true);
	}

	public SelectEntry(String text, Action onSelect, boolean localize) {
		this.onSelect = onSelect;
		this.text = text;
		this.localize = localize;
	}

	/**
	 * Changes the text of the entry.
	 *
	 * @param text new text
	 */
	void setText(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

	@Override
	public void tick(InputHandler input) {
		if (input.inputPressed("select") && onSelect != null) {
			Sound.play("confirm");
			onSelect.act();
		}
	}

	@Override
	public int getWidth() {
		return Font.textWidth(toString());
	}

	@Override
	public String toString() {
		return localize ? Localization.getLocalized(text) : text;
	}
}
