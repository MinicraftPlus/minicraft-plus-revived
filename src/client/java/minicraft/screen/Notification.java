package minicraft.screen;

import minicraft.core.io.Localization;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.MinicraftImage;
import minicraft.gfx.Point;
import minicraft.gfx.Rectangle;
import minicraft.gfx.Screen;
import minicraft.screen.entry.StringEntry;

import java.util.ArrayList;
import java.util.Arrays;

public class Notification {
	private static final int GAP = 10;
	private static final int ANIMATION_TIME = 20;

	private final int expireTime;
	private final Menu menu;

	private int tick = 0;
	private int animationTick = 0; // 0 to ANIMATION_TIME, ANIMATION_TIME to 0

	public Notification(String value) {
		this(value, 240, Color.WHITE);
	} // Default with 4 seconds

	public Notification(String value, int expireTime, int color) {
		this.expireTime = expireTime;
		menu = new Menu.Builder(true, 2, RelPos.RIGHT,
			StringEntry.useLines(color, false, Font.getLines(value,
				Screen.w - (GAP + MinicraftImage.boxWidth) * 2, Screen.h, 2)))
			.setPositioning(new Point(Screen.w - GAP, Screen.h - GAP), RelPos.TOP_LEFT)
			.createMenu();
	}

	public void tick() {
		if (tick == 0 && animationTick < ANIMATION_TIME) animationTick++;
		else if (tick < expireTime) tick++;
		else if (tick == expireTime) animationTick--;
	}

	public void render(Screen screen) {
		Rectangle bounds = menu.getBounds();
		int width = bounds.getWidth();
		int curX = bounds.getLeft();
		int toX = Screen.w - (width + GAP) * animationTick / ANIMATION_TIME; // Shifting with animation (sliding)
		menu.translate(toX - curX, 0);
		menu.render(screen);
	}

	public boolean isExpired() {
		return tick >= expireTime && animationTick <= 0;
	}
}
