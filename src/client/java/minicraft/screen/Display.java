package minicraft.screen;

import minicraft.core.Game;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Sound;
import minicraft.gfx.Screen;
import minicraft.screen.entry.ArrayEntry;
import org.jetbrains.annotations.Nullable;

public class Display {

	private Display parent = null;

	protected Menu[] menus;
	int selection;

	private final boolean canExit, clearScreen;

	public Display() {
		this(new Menu[0]);
	}

	public Display(Menu... menus) {
		this(false, true, menus);
	}

	public Display(boolean clearScreen) {
		this(clearScreen, true, new Menu[0]);
	}

	public Display(boolean clearScreen, Menu... menus) {
		this(clearScreen, true, menus);
	}

	public Display(boolean clearScreen, boolean canExit) {
		this(clearScreen, canExit, new Menu[0]);
	}

	public Display(boolean clearScreen, boolean canExit, Menu... menus) {
		this.menus = menus;
		this.canExit = canExit;
		this.clearScreen = clearScreen;
		selection = 0;
	}

	private boolean setParent = false;

	/**
	 * Called during {@link Game#setDisplay}
	 */
	public void init(@Nullable Display parent) {
		if (!setParent) {
			setParent = true;
			this.parent = parent;
		}
	}

	public void onExit() {
	}

	public Display getParent() {
		return parent;
	}

	public void tick(InputHandler input) {

		if (canExit && input.inputPressed("exit")) {
			Game.exitDisplay();
			return;
		}

		if (menus.length == 0) return;

		boolean changedSelection = false;

		if (menus.length > 1 && menus[selection].isSelectable()) { // If menu set is unselectable, it must have been intentional, so prevent the user from setting it back.
			int prevSel = selection;

			String shift = menus[selection].getCurEntry() instanceof ArrayEntry ? "shift-" : "";
			if (input.getMappedKey(shift + "left").isClicked() || input.leftTriggerPressed()) selection--;
			if (input.getMappedKey(shift + "right").isClicked() || input.rightTriggerPressed()) selection++;

			if (prevSel != selection) {
				Sound.play("select");

				int delta = selection - prevSel;
				selection = prevSel;
				do {
					selection += delta;
					if (selection < 0) selection = menus.length - 1;
					selection = selection % menus.length;
				} while (!menus[selection].isSelectable() && selection != prevSel);

				changedSelection = prevSel != selection;
			}

			if (changedSelection)
				onSelectionChange(prevSel, selection);
		}

		if (!changedSelection)
			menus[selection].tick(input);
	}

	protected void onSelectionChange(int oldSel, int newSel) {
		selection = newSel;
	}

	// Sub-classes can do extra rendering here; this renders each menu that should be rendered, in the order of the array, such that the currently selected menu is rendered last, so it appears on top (if they even overlap in the first place).
	public void render(Screen screen) {
		if (clearScreen)
			screen.clear(0);
		else if (setParent && parent != null) {
			parent.render(screen); // Renders the parent display as background.
		}

		if (menus.length == 0)
			return;

		int idx = selection;
		do {
			idx++;
			idx = idx % menus.length;
			if (menus[idx].shouldRender())
				menus[idx].render(screen);
		} while (idx != selection);
	}
}
