package minicraft.screen;

import com.studiohartman.jamepad.ControllerButton;
import minicraft.core.Game;
import minicraft.core.io.InputHandler;
import minicraft.gfx.Screen;
import minicraft.screen.entry.InputEntry;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.StringEntry;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.stream.Stream;

/**
 * Light-weighted exitable display with single menu.
 */
public class PopupDisplay extends Display {
	// Using Color codes for coloring in title and plain text messages.

	private final ArrayList<PopupActionCallback> callbacks;

	public PopupDisplay(@Nullable PopupConfig config, String... messages) {
		this(config, false, messages);
	}

	public PopupDisplay(@Nullable PopupConfig config, boolean clearScreen, String... messages) {
		this(config, clearScreen, true, messages);
	}

	public PopupDisplay(@Nullable PopupConfig config, boolean clearScreen, boolean menuFrame, String... messages) {
		this(config, clearScreen, menuFrame, StringEntry.useLines(messages));
	}

	public PopupDisplay(@Nullable PopupConfig config, ListEntry... entries) {
		this(config, false, entries);
	}

	public PopupDisplay(@Nullable PopupConfig config, boolean clearScreen, ListEntry... entries) {
		this(config, clearScreen, true, entries);
	}

	public PopupDisplay(@Nullable PopupConfig config, boolean clearScreen, boolean menuFrame, ListEntry... entries) {
		super(clearScreen, true);

		Menu.Builder builder = new Menu.Builder(menuFrame, 0, RelPos.CENTER, entries);

		if (config != null) {
			if (config.title != null)
				builder.setTitle(config.title);

			this.callbacks = config.callbacks;
		} else {
			this.callbacks = null;
		}

		if (Stream.of(entries).anyMatch(e -> e instanceof InputEntry))
			onScreenKeyboardMenu = OnScreenKeyboardMenu.checkAndCreateMenu();
		if (onScreenKeyboardMenu == null)
			menus = new Menu[] { builder.createMenu() };
		else
			menus = new Menu[] { onScreenKeyboardMenu, builder.createMenu() };
	}

	OnScreenKeyboardMenu onScreenKeyboardMenu;

	@Override
	public void render(Screen screen) {
		super.render(screen);
		if (onScreenKeyboardMenu != null)
			onScreenKeyboardMenu.render(screen);
	}

	@Override
	public void tick(InputHandler input) {
		boolean acted = false; // Checks if typing action is needed to be handled.
		boolean takeExitHandle = true;
		boolean handleMenu = false;
		if (onScreenKeyboardMenu == null) {
			if (!tickCallbacks(input))
				super.tick(input); // Continues with this if no callback returns true.
		} else {
			try {
				onScreenKeyboardMenu.tick(input);
			} catch (OnScreenKeyboardMenu.OnScreenKeyboardMenuTickActionCompleted e) {
				acted = true;
			} catch (OnScreenKeyboardMenu.OnScreenKeyboardMenuBackspaceButtonActed e) {
				takeExitHandle = false;
				acted = true;
			}

			if (takeExitHandle && input.inputPressed("exit")) {
				Game.exitDisplay();
				return;
			}

			if (menus[1].getCurEntry() instanceof InputEntry) {
				if (input.buttonPressed(ControllerButton.X)) { // Hide the keyboard.
					onScreenKeyboardMenu.setVisible(!onScreenKeyboardMenu.isVisible());
					if (!onScreenKeyboardMenu.isVisible())
						selection = 1;
					else
						selection = 0;
				}

				if (!acted)
					handleMenu = true;
			} else if (selection == 0) {
				onScreenKeyboardMenu.setVisible(false);
				selection = 1;
				handleMenu = true;
			} else {
				handleMenu = true;
			}

			if (handleMenu)
				if (!tickCallbacks(input))
					menus[1].tick(input); // Continues with this if no callback returns true.
		}
	}

	private boolean tickCallbacks(InputHandler input) {
		if (callbacks != null) {
			for (PopupActionCallback callback : callbacks) {
				if (callback.key == null || input.getMappedKey(callback.key).isClicked()) {
					if (callback.callback != null && callback.callback.acts(menus[0])) {
						// This overrides the original #tick check.
						return true;
					}
				}
			}
		}

		return false;
	}

	public static class PopupActionCallback {
		public final String key;
		public final ActionCallback callback;

		@FunctionalInterface
		public static interface ActionCallback {
			public boolean acts(Menu popupMenu);
		}

		/**
		 * The callback acts when the key clicked.
		 * @param key The key of the callback trigger.
		 * @param callback The callback, when the return value is {@code true}, no more input check
		 * 	will be done. It continues if {@code false}.
		 */
		public PopupActionCallback(String key, ActionCallback callback) {
			this.key = key;
			this.callback = callback;
		}
	}

	public static class PopupConfig {
		public String title;
		public ArrayList<PopupActionCallback> callbacks;
		public int entrySpacing;

		public PopupConfig(@Nullable String title, @Nullable ArrayList<PopupActionCallback> callbacks, int entrySpacing) {
			this.title = title;
			this.callbacks = callbacks;
			this.entrySpacing = entrySpacing;
		}
	}
}
