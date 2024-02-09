package minicraft.screen;

import minicraft.gfx.Color;
import minicraft.gfx.Dimension;
import minicraft.gfx.Font;
import minicraft.gfx.FontStyle;
import minicraft.gfx.Insets;
import minicraft.gfx.MinicraftImage;
import minicraft.gfx.Point;
import minicraft.gfx.Rectangle;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.gfx.SpriteLinker;
import minicraft.screen.entry.StringEntry;
import org.intellij.lang.annotations.MagicConstant;

import java.util.Arrays;
import java.util.List;

public class AppToast {
	private static final int TOP_PADDING = 30;
	private static final int BOTTOM_PADDING = 30;
	private static final int MAX_WIDTH = Screen.w * 3 / 8;
	private static final int MAX_HEIGHT = Screen.w * 3 / 8;
	private static final int SPACING = 2;
	private static final int ANIMATION_TIME = 12;

	@MagicConstant(intValues = {0, 1})
	private static int positioning = 0;

	public static void setPositionTop() {
		positioning = 0;
	}
	public static void setPositionBottom() {
		positioning = 1;
	}

	private final int expireTime;
	private final Dimension size;
	private final List<String> text;
	private final AppToastFrame frame;
	private final int color;

	private int tick = 0;
	private int animationTick = 0; // 0 to ANIMATION_TIME, ANIMATION_TIME to 0

	public static abstract class AppToastFrame {
		public static final AppToastFrame FRAME_GENERAL = new GeneralAppToastFrame();
		public static final AppToastFrame FRAME_URGENT = new UrgentAppToastFrame();
		public static final AppToastFrame FRAME_WINDOW = new WindowAppToastFrame();
		public static final AppToastFrame FRAME_BRICK = new BrickAppToastFrame();

		protected final Insets paddings;

		protected AppToastFrame(Insets paddings) {
			this.paddings = paddings;
		}

		public abstract void render(Screen screen, int x, int y, int w, int h); // w, h in units of cells

		protected void render(Screen screen, int x, int y, int w, int h, Sprite sprite) {
			for (int i = 0; i < w; ++i) {
				for (int j = 0; j < h; ++j) {
					Sprite.Px[][] pxs = sprite.spritePixels;
					if (i == 0 && j == 0)
						screen.render(x, y, pxs[0][0]);
					else if (i == 0 && j == h - 1)
						screen.render(x, y + j * 8, pxs[2][0]);
					else if (i == w - 1 && j == 0)
						screen.render(x + i * 8, y, pxs[0][2]);
					else if (i == w - 1 && j == h - 1)
						screen.render(x + i * 8, y + j * 8, pxs[2][2]);
					else if (i == 0)
						screen.render(x, y + j * 8, pxs[1][0]);
					else if (j == 0)
						screen.render(x + i * 8, y, pxs[0][1]);
					else if (i == w - 1)
						screen.render(x + i * 8, y + j * 8, pxs[1][2]);
					else if (j == h - 1)
						screen.render(x + i * 8, y + j * 8, pxs[2][1]);
					else
						screen.render(x + i * 8, y + j * 8, pxs[1][1]);
				}
			}
		}

		private static class GeneralAppToastFrame extends AppToastFrame {
			private static final SpriteLinker.LinkedSprite sprite =
				new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Gui, "toasts")
					.setSpriteDim(0, 0, 3, 3);

			private GeneralAppToastFrame() {
				super(new Insets(4));
			}

			@Override
			public void render(Screen screen, int x, int y, int w, int h) {
				render(screen, x, y, w, h, sprite.getSprite());
			}
		}

		private static class UrgentAppToastFrame extends AppToastFrame {
			private static final SpriteLinker.LinkedSprite sprite =
				new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Gui, "toasts")
					.setSpriteDim(3, 0, 3, 3);

			private UrgentAppToastFrame() {
				super(new Insets(4));
			}

			@Override
			public void render(Screen screen, int x, int y, int w, int h) {
				render(screen, x, y, w, h, sprite.getSprite());
			}
		}

		private static class WindowAppToastFrame extends AppToastFrame {
			private static final SpriteLinker.LinkedSprite sprite =
				new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Gui, "toasts")
					.setSpriteDim(0, 3, 3, 3);

			private WindowAppToastFrame() {
				super(new Insets(6, 4, 2, 8));
			}

			@Override
			public void render(Screen screen, int x, int y, int w, int h) {
				render(screen, x, y, w, h, sprite.getSprite());
			}
		}

		private static class BrickAppToastFrame extends AppToastFrame {
			private static final SpriteLinker.LinkedSprite sprite =
				new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Gui, "toasts")
					.setSpriteDim(3, 3, 3, 3);

			private BrickAppToastFrame() {
				super(new Insets(6, 6, 2, 6));
			}

			@Override
			public void render(Screen screen, int x, int y, int w, int h) {
				render(screen, x, y, w, h, sprite.getSprite());
			}
		}
	}

	public AppToast(AppToastFrame frame, String message) {
		this(frame, message, 180, Color.WHITE); // Default with 3 seconds
	}

	public AppToast(AppToastFrame frame, String message, int expireTime, int color) { // Align left
		this.expireTime = expireTime;
		this.frame = frame;
		this.color = color;
		String[] lines = Font.getLines(message, MAX_WIDTH - frame.paddings.left - frame.paddings.right,
			MAX_HEIGHT - frame.paddings.top - frame.paddings.bottom, SPACING);
		size = new Dimension((Font.textWidth(lines) + frame.paddings.left + frame.paddings.right + 7) / 8 * 8,
			(Math.max(0, lines.length * Font.textHeight() +
				SPACING * (lines.length - 1) + frame.paddings.top + frame.paddings.bottom) + 7) / 8 * 8);
		// There is little a chance that the resultant height may still exceed the max height, but the problem is negligible.
		text = Arrays.asList(lines);
	}

	public void tick() {
		if (tick == 0 && animationTick < ANIMATION_TIME) animationTick++;
		else if (tick < expireTime) tick++;
		else if (tick == expireTime) animationTick--;
	}

	public void render(Screen screen) {
		int x = Screen.w - size.width * animationTick / ANIMATION_TIME; // Shifting with animation (sliding)
		int y = positioning == 0 ? TOP_PADDING : Screen.h - BOTTOM_PADDING - size.height;
		frame.render(screen, x, y, size.width / 8, size.height / 8);
		Font.drawParagraph(text, screen, new FontStyle(color)
			.setAnchor(x + frame.paddings.left, y + frame.paddings.top)
			.setRelTextPos(RelPos.BOTTOM_RIGHT, true), SPACING);
	}

	public boolean isExpired() {
		return tick >= expireTime && animationTick <= 0;
	}
}
