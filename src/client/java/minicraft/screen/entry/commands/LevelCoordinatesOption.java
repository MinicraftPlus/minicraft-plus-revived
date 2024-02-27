package minicraft.screen.entry.commands;

import minicraft.core.io.InputHandler;
import minicraft.core.io.Sound;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.MinicraftImage;
import minicraft.gfx.Screen;
import minicraft.screen.entry.ChangeListener;
import minicraft.screen.entry.InputEntry;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.UserMutable;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class LevelCoordinatesOption extends ListEntry implements UserMutable {
	private static final int INPUT_ENTRY_COUNT = 2; // x, y

	private final @Nullable String prompt;
	private final List<CoordinateInputEntry> inputs;

	private int selection = 0;

	public LevelCoordinatesOption(int w, int h, boolean isTile) {
		this(null, w, h, isTile);
	}

	public LevelCoordinatesOption(int w, int h, int x, int y, boolean isInputTile, boolean isTile) {
		this(null, w, h, x, y, isInputTile, isTile);
	}

	public LevelCoordinatesOption(@Nullable String prompt, int w, int h, boolean isTile) {
		this.prompt = prompt;
		inputs = Collections.unmodifiableList(isTile ? Arrays.asList(
			new TileCoordinateInputEntry("X", w),
			new TileCoordinateInputEntry("Y", h)
		) : Arrays.asList(
			new EntityCoordinateInputEntry("X", w),
			new EntityCoordinateInputEntry("Y", h)
		));
	}

	public LevelCoordinatesOption(@Nullable String prompt, int w, int h, int x, int y, boolean isInputTile, boolean isTile) {
		this.prompt = prompt;
		inputs = Collections.unmodifiableList(isTile ? Arrays.asList(
			new TileCoordinateInputEntry("X", w, x, isInputTile),
			new TileCoordinateInputEntry("Y", h, y, isInputTile)
		) : Arrays.asList(
			new EntityCoordinateInputEntry("X", w, x, isInputTile),
			new EntityCoordinateInputEntry("Y", h, y, isInputTile)
		));
	}

	private static abstract class CoordinateInputEntry extends InputEntry {
		public CoordinateInputEntry(String prompt, String regex, String initValue) {
			super(prompt, regex, 0, initValue);
		}

		public boolean isAllValid() {
			return isValid();
		}

		public abstract int getValue() throws IllegalArgumentException;
	}

	// Based on entity coordinate system
	private static class EntityCoordinateInputEntry extends CoordinateInputEntry {
		private final int bound;
		private final InputEntry minorInput;

		private boolean specified;
		private boolean minor;

		public EntityCoordinateInputEntry(String prompt, int bound, int initValue, boolean isTile) {
			this(prompt, bound, String.valueOf(isTile ? initValue : initValue / 16), !isTile, isTile ? "" : String.valueOf(initValue % 16));
		}

		/**
		 * Construct an entry with no default input.
		 */
		public EntityCoordinateInputEntry(String prompt, int bound) {
			this(prompt, bound, "", false, "");
		}

		private EntityCoordinateInputEntry(String prompt, int bound, String initValue, boolean specified, String minorDefault) {
			super(prompt, regexNumber, initValue);
			this.bound = bound;
			this.specified = specified;
			minorInput = new InputEntry("", regexNumber, 0, minorDefault) {
				@Override
				public boolean isValid() {
					try {
						int value = Integer.parseInt(getUserInput());
						return value >= 0 && value < 16;
					} catch (NumberFormatException e) {
						return false;
					}
				}

				@Override
				public void setChangeListener(ChangeListener l) {
					super.setChangeListener(v -> {
						String input = getUserInput();
						if (input.startsWith("0") && input.length() > 1)
							setUserInput(input.substring(1)); // Trimming leading zero
						if (input.isEmpty()) setUserInput("0"); // "zero" placeholder (default value)
						l.onChange(v);
					});
				}
			};
		}

		@Override
		public boolean isValid() {
			try {
				int value = Integer.parseInt(getUserInput());
				return value >= 0 && value < bound;
			} catch (NumberFormatException e) {
				return false;
			}
		}

		@Override
		public boolean isAllValid() {
			return super.isAllValid() && minorInput.isValid();
		}

		@Override
		public void tick(InputHandler input) {
			if (input.getMappedKey("MINUS").isClicked()) {
				if (!specified) specified = true;
				minor = !minor;
			} else if (!minor) {
				super.tick(input);
			} else {
				minorInput.tick(input);
			}
		}

		@Override
		public void render(Screen screen, int x, int y, boolean isSelected) {
			String text = super.toString();
			String input = getUserInput();
			int padding = text.length() - input.length();
			Font.draw(text.substring(0, padding), screen, x, y, isSelected ? COL_SLCT : COL_UNSLCT);
			Font.draw(input, screen, x + padding * MinicraftImage.boxWidth, y,
				isValid() ? isSelected ? minor ? COL_SLCT : Color.GREEN : COL_UNSLCT : isSelected ? Color.RED : DARK_RED);
			String minorText = minorInput.getUserInput();
			if (specified) {
				Font.draw("-", screen, x += Font.textWidth(text), y, isSelected ? COL_SLCT : COL_UNSLCT);
				if (minorText.isEmpty()) minorText = "0";
				Font.draw(minorText, screen, x + MinicraftImage.boxWidth, y,
					minorInput.isValid() ? isSelected ? minor ? Color.GREEN : COL_SLCT : COL_UNSLCT : isSelected ? Color.RED : DARK_RED);
			}
		}

		@Override
		public void setChangeListener(ChangeListener l) {
			minorInput.setChangeListener(l);
			super.setChangeListener(l);
		}

		public int getValue() throws IllegalArgumentException {
			String input = getUserInput();
			if (input.isEmpty()) throw new IllegalArgumentException("input is empty");
			if (!isValid() || !minorInput.isValid()) throw new IllegalArgumentException("invalid input");
			try {
				return Integer.parseInt(input) * 16 + Integer.parseInt(minorInput.getUserInput());
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException(e);
			}
		}

		@Override
		public String toString() {
			return super.toString() + (specified ? "-" + minorInput.getUserInput() : "");
		}
	}

	// Based on tile coordinate system
	private static class TileCoordinateInputEntry extends CoordinateInputEntry {
		private final int bound;

		public TileCoordinateInputEntry(String prompt, int bound) {
			this(prompt, bound, "");
		}

		public TileCoordinateInputEntry(String prompt, int bound, int initValue, boolean isTile) {
			this(prompt, bound, String.valueOf(isTile ? initValue : initValue / 16));
		}

		private TileCoordinateInputEntry(String prompt, int bound, String initValue) {
			super(prompt, regexNumber, initValue);
			this.bound = bound;
		}

		@Override
		public boolean isValid() {
			try {
				int value = Integer.parseInt(getUserInput());
				return value >= 0 && value < bound;
			} catch (NumberFormatException e) {
				return false;
			}
		}

		public int getValue() throws IllegalArgumentException {
			String input = getUserInput();
			if (input.isEmpty()) throw new IllegalArgumentException("input is empty");
			if (!isValid()) throw new IllegalArgumentException("invalid input");
			try {
				return Integer.parseInt(input);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException(e);
			}
		}
	}

	@Override
	public void tick(InputHandler input) {
		if (input.getMappedKey("CURSOR-LEFT").isClicked()) {
			if (selection > 0) selection--;
			Sound.play("select");
		} else if (input.getMappedKey("CURSOR-RIGHT").isClicked()) {
			if (selection < INPUT_ENTRY_COUNT - 1) selection++;
			Sound.play("select");
		} else {
			inputs.get(selection).tick(input);
		}
	}

	@Override
	public void render(Screen screen, int x, int y, boolean isSelected) {
		if (isVisible()) {
			for (int i = 0; i < INPUT_ENTRY_COUNT; i++) {
				inputs.get(i).render(screen, x, y, isSelected && i == selection);
				x += Font.textWidth(inputs.get(i).toString()) + Font.textWidth("; ");
			}
		}
	}

	public String getXUserInput() {
		return inputs.get(0).getUserInput();
	}

	public String getYUserInput() {
		return inputs.get(1).getUserInput();
	}

	public int getXValue() throws IllegalArgumentException {
		return inputs.get(0).getValue();
	}

	public int getYValue() throws IllegalArgumentException {
		return inputs.get(1).getValue();
	}

	public boolean isAllInputValid() {
		return inputs.stream().allMatch(CoordinateInputEntry::isAllValid);
	}

	@Override
	public void setChangeListener(ChangeListener listener) {
		inputs.forEach(i -> i.setChangeListener(listener));
	}

	@Override
	public String toString() {
		return (prompt == null ? "" : prompt + ": ") + inputs.stream().map(InputEntry::toString).collect(Collectors.joining("; "));
	}
}
