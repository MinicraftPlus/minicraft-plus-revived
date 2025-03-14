package minicraft.screen.entry.commands;

import minicraft.core.World;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Sound;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class LevelSelectionOption extends TargetedInputEntry<Integer> {
	private static final int[] levelDepths;

	static {
		levelDepths = World.idxToDepth.clone();
		Arrays.sort(levelDepths);
	}

	private final int defaultLevel;

	private boolean typing; // Typing or selecting
	private int selection;

	public LevelSelectionOption() {
		this(null);
	}

	public LevelSelectionOption(@Nullable Integer level) {
		super("Level", level == null ? regexNegNumberOpt : regexNegNumber, noOpValidator(),
			level == null ? "" : String.valueOf(level));
		this.defaultLevel = level == null ? 0 : level;
		typing = level == null;
		selection = Arrays.binarySearch(levelDepths, defaultLevel);
	}

	@Override
	public void tick(InputHandler input) {
		if (input.getMappedKey("ENTER").isClicked()) {
			if (typing) {
				if (isValid()) { // Only to change when the input is valid.
					int level = getValue();
					selection = Arrays.binarySearch(levelDepths, level);
					typing = false;
				}
			} else // !typing
				typing = true;
		} else if (!typing) {
			if (input.getMappedKey("CURSOR-LEFT").isClicked()) {
				selection = Math.max(selection - 1, 0);
				Sound.play("select");
				setUserInput(String.valueOf(levelDepths[selection]));
			} else if (input.getMappedKey("CURSOR-RIGHT").isClicked()) {
				selection = Math.min(selection + 1, levelDepths.length - 1);
				Sound.play("select");
				setUserInput(String.valueOf(levelDepths[selection]));
			}
		} else
			super.tick(input);
	}

	@Override
	protected boolean hasValidator() {
		return true;
	}

	@Override
	protected boolean validate(String input) {
		try {
			if (input.isEmpty()) return true; // Default level
			return Arrays.binarySearch(levelDepths, Integer.parseInt(input)) >= 0;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	@Override
	protected @Nullable Integer parse(String input) {
		try {
			if (input.isEmpty()) return defaultLevel; // Default level
			int val = Integer.parseInt(input);
			return Arrays.binarySearch(levelDepths, val) < 0 ? null : val; // Non-null if input is valid
		} catch (NumberFormatException e) {
			return null;
		}
	}

	@Override
	public void render(Screen screen, int x, int y, boolean isSelected) {
		if (!isSelected) typing = false; // TODO There should be #tick(..., isSelected) to replace this line.
		Font.draw(isSelected && typing ? super.toString() : toString(), screen, x, y,
			isValid() ? isSelected ? Color.GREEN : COL_UNSLCT : isSelected ? Color.RED : DARK_RED);
	}

	@Override
	public String toString() {
		try {
			int depth = getValue();
			return "Level: " + Level.getDepthString(depth) + " (" + Level.getLevelName(depth) + ")";
		} catch (IllegalArgumentException e) {
			return super.toString();
		}
	}
}
