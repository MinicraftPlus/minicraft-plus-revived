package minicraft.screen;

import minicraft.gfx.Color;
import minicraft.gfx.Dimension;
import minicraft.gfx.Font;
import minicraft.gfx.FontStyle;
import minicraft.gfx.Insets;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.gfx.SpriteLinker;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class AppToast extends Toast {
	private static final int TOP_PADDING = 30;
	private static final int BOTTOM_PADDING = 30;
	private static final int MAX_WIDTH = Screen.w * 3 / 8;
	private static final int MAX_HEIGHT = Screen.w * 3 / 8;

	@MagicConstant(intValues = {0, 1})
	private static int positioning = 0;

	public static void setPositionTop() {
		positioning = 0;
	}
	public static void setPositionBottom() {
		positioning = 1;
	}

	private final Dimension size;
	private final List<String> text;
	private final AppToastFrame frame;
	private final int color;

	public static abstract class AppToastFrame extends ToastFrame {
		public static final AppToastFrame FRAME_GENERAL = new GeneralAppToastFrame();
		public static final AppToastFrame FRAME_URGENT = new UrgentAppToastFrame();
		public static final AppToastFrame FRAME_WINDOW = new WindowAppToastFrame();
		public static final AppToastFrame FRAME_BRICK = new BrickAppToastFrame();

		protected final @Nullable SpriteLinker.LinkedSprite sprite;

		protected AppToastFrame(@Nullable SpriteLinker.LinkedSprite sprite, Insets paddings) {
			super(paddings);
			this.sprite = sprite;
		}

		@Override
		public void render(Screen screen, int x, int y, int w, int h) {
			assert sprite != null; // If sprite == null, this implementation should be overridden.
			render(screen, x, y, w, h, sprite.getSprite());
		}

		private static class GeneralAppToastFrame extends AppToastFrame {
			private GeneralAppToastFrame() {
				super(new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Gui, "toasts")
					.setSpriteDim(0, 0, 3, 3), new Insets(4));
			}
		}

		private static class UrgentAppToastFrame extends AppToastFrame {
			private UrgentAppToastFrame() {
				super(new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Gui, "toasts")
					.setSpriteDim(3, 0, 3, 3), new Insets(4));
			}
		}

		private static class WindowAppToastFrame extends AppToastFrame {
			private WindowAppToastFrame() {
				super(new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Gui, "toasts")
					.setSpriteDim(0, 3, 3, 3), new Insets(6, 4, 2, 8));
			}
		}

		private static class BrickAppToastFrame extends AppToastFrame {
			private BrickAppToastFrame() {
				super(new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Gui, "toasts")
					.setSpriteDim(3, 3, 3, 3), new Insets(6, 6, 2, 6));
			}
		}
	}

	public AppToast(AppToastFrame frame, String message) {
		this(frame, message, 180, Color.WHITE); // Default with 3 seconds
	}

	public AppToast(AppToastFrame frame, String message, int expireTime, int color) { // Align left
		super(expireTime);
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

	@Override
	public void render(Screen screen) {
		int x = Screen.w - size.width * animationTick / ANIMATION_TIME; // Shifting with animation (sliding)
		int y = positioning == 0 ? TOP_PADDING : Screen.h - BOTTOM_PADDING - size.height;
		frame.render(screen, x, y, size.width / 8, size.height / 8);
		Font.drawParagraph(text, screen, new FontStyle(color)
			.setAnchor(x + frame.paddings.left, y + frame.paddings.top)
			.setRelTextPos(RelPos.BOTTOM_RIGHT, true), SPACING);
	}
}
