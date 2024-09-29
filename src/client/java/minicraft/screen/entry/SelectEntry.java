package minicraft.screen.entry;

import minicraft.core.Action;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.core.io.Sound;
import minicraft.gfx.Font;
import minicraft.util.DisplayString;

public class SelectEntry extends ListEntry {

	private Action onSelect;
	private DisplayString text;

	/**
	 * Creates a new entry which acts as a button.
	 * Can do an action when it is selected.
	 * Localizes the provided string.
	 * @param text Text displayed on this entry
	 * @param onSelect Action which happens when the entry is selected
	 */
	public SelectEntry(DisplayString text, Action onSelect) {
		this.onSelect = onSelect;
		this.text = text;
	}

	/**
	 * Changes the text of the entry.
	 * @param text new text
	 */
	void setText(DisplayString text) {
		this.text = text;
	}

	public DisplayString getText() {
		return text;
	}

	@Override
	public void tick(InputHandler input) {
		if (input.inputPressed("select") && onSelect != null && isSelectable()) {
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
		return text.toString();
	}
}
