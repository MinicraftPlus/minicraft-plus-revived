package minicraft.screen;

import minicraft.core.io.InputHandler;
import minicraft.gfx.Dimension;
import minicraft.gfx.MinicraftImage;
import minicraft.gfx.Point;
import minicraft.gfx.Screen;
import minicraft.level.Level;
import minicraft.screen.entry.StringEntry;
import minicraft.util.Logging;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SignDisplayMenu extends Menu {
	private final int levelDepth, x, y;

	public SignDisplayMenu(Level level, int x, int y) {
		super(new Menu.Builder(true, 0, RelPos.CENTER)
			.setPositioning(new Point(Screen.w / 2, 6), RelPos.BOTTOM)
			.setMenuSize(new Dimension(MinicraftImage.boxWidth * (SignDisplay.MAX_TEXT_LENGTH + 2), MinicraftImage.boxWidth * (SignDisplay.MAX_ROW_COUNT + 2)))
			.setDisplayLength(4)
			.setSelectable(false)
			.createMenu());
		this.levelDepth = level.depth;
		this.x = x;
		this.y = y;
		List<String> lines;
		if ((lines = SignDisplay.getSign(levelDepth, x, y)) == null) {
			lines = Collections.emptyList();
			Logging.WORLDNAMED.warn("Sign at ({}, {}) does not exist or has not initialized, but a display menu is invoked.", x, y);
		}
		setEntries(lines.stream().map(r -> new StringEntry(r, false)).collect(Collectors.toList()));
	}

	/** Checks if this sign's coordinates differ from the given ones. */
	public boolean differsFrom(int levelDepth, int x, int y) {
		return this.levelDepth != levelDepth || this.x != x || this.y != y;
	}

	/** Checks if this sign's coordinates match the given ones. */
	public boolean matches(int levelDepth, int x, int y) {
		return this.levelDepth == levelDepth && this.x == x && this.y == y;
	}

	@Deprecated
	@Override
	public void tick(InputHandler input) {} // Render only
}
