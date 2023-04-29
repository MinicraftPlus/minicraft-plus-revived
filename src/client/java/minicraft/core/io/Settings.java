package minicraft.core.io;

import minicraft.core.Initializer;
import minicraft.screen.entry.ArrayEntry;
import minicraft.screen.entry.BooleanEntry;

import java.awt.DisplayMode;
import java.util.ArrayList;
import java.util.HashMap;

public class Settings {

	private static final HashMap<String, ArrayEntry<?>> options = new HashMap<>();

	static {
		options.put("fps", new FPSEntry("minicraft.settings.fps")); // Has to check if the game is running in a headless mode. If it doesn't set the fps to 60
		options.put("screenshot", new ArrayEntry<>("minicraft.settings.screenshot_scale", 1, 2, 5, 10, 15, 20)); // The magnification of screenshot. I would want to see ultimate sized.
		options.put("diff", new ArrayEntry<>("minicraft.settings.difficulty", "minicraft.settings.difficulty.easy", "minicraft.settings.difficulty.normal", "minicraft.settings.difficulty.hard"));
		options.get("diff").setSelection(1);
		options.put("mode", new ArrayEntry<>("minicraft.settings.mode", "minicraft.settings.mode.survival", "minicraft.settings.mode.creative", "minicraft.settings.mode.hardcore", "minicraft.settings.mode.score"));

		options.put("scoretime", new ArrayEntry<>("minicraft.settings.scoretime", 10, 20, 40, 60, 120));
		options.get("scoretime").setValueVisibility(10, false);
		options.get("scoretime").setValueVisibility(120, false);

		options.put("sound", new BooleanEntry("minicraft.settings.sound", true));
		options.put("autosave", new BooleanEntry("minicraft.settings.autosave", true));

		options.put("size", new ArrayEntry<>("minicraft.settings.size", 128, 256, 512));
		options.put("theme", new ArrayEntry<>("minicraft.settings.theme", "minicraft.settings.theme.normal", "minicraft.settings.theme.forest", "minicraft.settings.theme.desert", "minicraft.settings.theme.plain", "minicraft.settings.theme.hell"));
		options.put("type", new ArrayEntry<>("minicraft.settings.type", "minicraft.settings.type.island", "minicraft.settings.type.box", "minicraft.settings.type.mountain", "minicraft.settings.type.irregular"));

		// TODO localize these labels
		options.put("tutorials", new BooleanEntry("Tutorials", false));
		options.put("quests", new BooleanEntry("Quests", false));
		options.put("showquests", new BooleanEntry("Quests Panel", true));

		options.get("mode").setChangeAction(value ->
			options.get("scoretime").setVisible("minicraft.settings.mode.score".equals(value))
		);
	}

	// Returns the value of the specified option
	public static Object get(String option) { return options.get(option.toLowerCase()).getValue(); }

	// Returns the index of the value in the list of values for the specified option
	public static int getIdx(String option) { return options.get(option.toLowerCase()).getSelection(); }

	// Return the ArrayEntry object associated with the given option name.
	public static ArrayEntry<?> getEntry(String option) { return options.get(option.toLowerCase()); }

	// Sets the value of the given option name, to the given value, provided it is a valid value for that option.
	public static void set(String option, Object value) {
		options.get(option.toLowerCase()).setValue(value);
	}

	// Sets the index of the value of the given option, provided it is a valid index
	public static void setIdx(String option, int idx) {
		options.get(option.toLowerCase()).setSelection(idx);
	}

	public static final int FPS_UNLIMITED = -1;

	/**
	 * Getting the set value for maximum framerate.
	 * @return The set or device framerate value, or {@link #FPS_UNLIMITED} if unlimited.
	 */
	public static int getFPS() {
		return ((FPSEntry) getEntry("fps")).getFPSValue();
	}

	private static class FPSEntry extends ArrayEntry<FPSEntry.FPSValue> {
		public static class FPSValue {
			private int fps = DisplayMode.REFRESH_RATE_UNKNOWN;
			public enum ValueType { Normal, VSync, Unlimited; }
			private final ValueType type;

			public FPSValue(ValueType type) {
				this.type = type == ValueType.Normal ? ValueType.Unlimited : type;
			}
			public FPSValue(int fps) {
				type = ValueType.Normal;
				if (fps > 300) fps = 300;
				if (fps < 10) fps = 10;
				this.fps = fps;
			}

			public int getFPS() {
				if (type == ValueType.VSync) {
					// There is currently no actual way to do VSync by only Swing/AWT.
					// Instead, the device refresh rate is used.
					int rate = Initializer.getFrame().getGraphicsConfiguration().getDevice().getDisplayMode().getRefreshRate();
					return rate == DisplayMode.REFRESH_RATE_UNKNOWN ? FPS_UNLIMITED : rate;
				} else if (type == ValueType.Unlimited) {
					return FPS_UNLIMITED;
				} else {
					return fps;
				}
			}

			@Override
			public String toString() {
				return type == ValueType.Normal ? String.valueOf(fps) : type.toString();
			}

			@Override
			public boolean equals(Object obj) {
				if (obj == null) return false;
				if (this == obj) return true;
				if (obj instanceof FPSValue)
					return type == ((FPSValue) obj).type &&
						fps == ((FPSValue) obj).fps;
				return false;
			}

			@Override
			public int hashCode() {
				return type.ordinal() * fps + fps;
			}
		}

		private static FPSValue[] getArray() {
			ArrayList<FPSValue> list = new ArrayList<>();
			for (int i = 10; i <= 300; i += 10) {
				list.add(new FPSValue(i));
			}

			list.add(new FPSValue(FPSValue.ValueType.VSync));
			list.add(new FPSValue(FPSValue.ValueType.Unlimited));

			return list.toArray(new FPSValue[0]);
		}

		public FPSEntry(String label) {
			super(label, false, getArray());
			int rate = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getRefreshRate();
			setValue(rate == DisplayMode.REFRESH_RATE_UNKNOWN ? 60 : rate % 10 == 0 ? rate : rate - rate % 10);
		}

		/**
		 * Getting the set value for maximum framerate.
		 * @return The set or device framerate value, or {@link #FPS_UNLIMITED} if unlimited.
		 */
		public int getFPSValue() {
			return getValue().getFPS();
		}

		@Override
		public void setValue(Object value) {
			if (value instanceof FPSValue) {
				super.setValue(value);
			} else {
				if (!(value instanceof FPSValue.ValueType)) try { // First tests if it is an integral value.
					int v;
					if (value instanceof Integer) v = (int) value;
					else v = Integer.parseInt(value.toString());
					if (v < 10) v = 10;
					if (v > 300) v = 300;
					v -= v % 10;
					for (FPSValue val : options) {
						if (val.fps == v) {
							super.setValue(val);
							return;
						}
					}
				} catch (NumberFormatException ignored) {}

				try { // Then falls back to enums.
					FPSValue.ValueType type;
					if (value instanceof FPSValue.ValueType) type = (FPSValue.ValueType) value;
					else type = FPSValue.ValueType.valueOf(value.toString());
					if (type != FPSValue.ValueType.Normal) for (FPSValue val : options) {
						if (val.type == type) {
							super.setValue(val);
							return;
						}
					}
				} catch (IllegalArgumentException ignored) {}

				// Finally VSync is applied.
				super.setValue(FPSValue.ValueType.VSync);
			}
		}
	}
}
