package minicraft.screen.entry.commands;

import minicraft.core.io.InputHandler;
import minicraft.core.io.Sound;
import minicraft.gfx.Font;
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

// Integer 2-value vectors within a 2D Euclidean plane
public class Vector2ValueOption extends ListEntry implements UserMutable {
	private static final int INPUT_ENTRY_COUNT = 2; // x, y

	private final @Nullable String prompt;
	private final List<CoordinateInputEntry> inputs;

	private int selection = 0;

	public Vector2ValueOption(boolean isNeg, @Nullable Integer x, @Nullable Integer y) {
		this(null, isNeg, x, y);
	}

	public Vector2ValueOption(@Nullable String prompt, boolean isNeg, @Nullable Integer x, @Nullable Integer y) {
		this.prompt = prompt;
		inputs = Collections.unmodifiableList(Arrays.asList(
			new CoordinateInputEntry("X", isNeg, x == null ? "" : String.valueOf(x)),
			new CoordinateInputEntry("Y", isNeg, y == null ? "" : String.valueOf(y))
		));
	}

	private static class CoordinateInputEntry extends TargetedInputEntry<Integer> {
		public CoordinateInputEntry(String prompt, boolean isNeg, String initValue) {
			super(prompt, isNeg ? regexNegNumber : regexNumber, new TargetedValidator<>(null, input -> {
				try {
					return Integer.parseInt(input);
				} catch (NumberFormatException e) {
					return null;
				}
			}), initValue);
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

	public int getXValue() throws IllegalArgumentException {
		return inputs.get(0).getValue();
	}

	public int getYValue() throws IllegalArgumentException {
		return inputs.get(1).getValue();
	}

	public boolean isAllInputValid() {
		return inputs.stream().allMatch(CoordinateInputEntry::isValid);
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
