package minicraft.screen;

import minicraft.core.io.InputHandler;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.StringEntry;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/** Light-weighted exitable display with single menu. */
public class PopupDisplay extends Display {
	// TODO Turn all popups into PopupDisplay, including weird BookDisplay uses.
	// Using Color codes for coloring in title and plain text messages.

	private final ArrayList<PopupActionCallback> callbacks;

	public PopupDisplay(@Nullable PopupConfig config, String... messages) { this(config, false, messages); }
	public PopupDisplay(@Nullable PopupConfig config, boolean clearScreen, String... messages) { this(config, clearScreen, true, messages); }
	public PopupDisplay(@Nullable PopupConfig config, boolean clearScreen, boolean menuFrame, String... messages) { this(config, clearScreen, StringEntry.useLines(messages)); }
	public PopupDisplay(@Nullable PopupConfig config, ListEntry... entries) { this(config, false, entries); }
	public PopupDisplay(@Nullable PopupConfig config, boolean clearScreen, ListEntry... entries) { this(config, false, true, entries); }
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

		menus = new Menu[] {builder.createMenu()};
	}

	@Override
	public void tick(InputHandler input) {
		if (callbacks != null) {
			for (PopupActionCallback callback : callbacks) {
				if (callback.key == null || input.getKey(callback.key).clicked) {
					if (callback.callback != null && callback.callback.acts(menus[0])) {
						// This overrides the original #tick check.
						return;
					}
				}
			}
		}

		// Continues with this if no callback returns true.
		super.tick(input);
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
		 * will be done. It continues if {@code false}.
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
