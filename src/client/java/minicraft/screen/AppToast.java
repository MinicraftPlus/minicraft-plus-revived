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

/**
 * Application Toasts are always located in the top-right corner with some margins.
 * These toasts are supposed to be globally accessed application-wide.
 * They contain only text messages.
 */
public class AppToast extends Toast {
	private static final int TOP_PADDING = 8;
	private static final int MAX_WIDTH = Screen.w * 3 / 8;
	private static final int MAX_HEIGHT = Screen.w * 3 / 8;

	private final Dimension size;
	private final List<String> text;
	private final ToastType type;
	private final int color;

	public enum ToastType {
		GENERAL(new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Gui, "toasts")
			.setSpriteDim(0, 0, 3, 3), new Insets(4)),
		URGENT(new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Gui, "toasts")
			.setSpriteDim(3, 0, 3, 3), new Insets(4)),
		WINDOW(new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Gui, "toasts")
			.setSpriteDim(0, 3, 3, 3), new Insets(6, 4, 2, 8)),
		BRICK(new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Gui, "toasts")
			.setSpriteDim(3, 3, 3, 3), new Insets(6, 6, 2, 6));

		private final SpriteLinker.LinkedSprite sprite;
		private final Insets paddings;

		ToastType(SpriteLinker.LinkedSprite sprite, Insets paddings) {
			this.sprite = sprite;
			this.paddings = paddings;
		}
	}

	public AppToast(ToastType type, String message) {
		this(type, message, 180, Color.WHITE); // Default with 3 seconds
	}

	public AppToast(ToastType type, String message, int expireTime, int color) { // Align left
		super(expireTime);
		this.type = type;
		this.color = color;
		String[] lines = Font.getLines(message, MAX_WIDTH - type.paddings.left - type.paddings.right,
			MAX_HEIGHT - type.paddings.top - type.paddings.bottom, SPACING);
		size = new Dimension((Font.textWidth(lines) + type.paddings.left + type.paddings.right + 7) / 8 * 8,
			(Math.max(0, lines.length * Font.textHeight() +
				SPACING * (lines.length - 1) + type.paddings.top + type.paddings.bottom) + 7) / 8 * 8);
		// There is little a chance that the resultant height may still exceed the max height, but the problem is negligible.
		text = Arrays.asList(lines);
	}

	@Override
	public void render(Screen screen) {
		int x = Screen.w - size.width * animationTick / ANIMATION_TIME; // Shifting with animation (sliding)
		int y = TOP_PADDING;
		renderFrame(screen, x, y, size.width / 8, size.height / 8, type.sprite.getSprite());
		Font.drawParagraph(text, screen, new FontStyle(color)
			.setAnchor(x + type.paddings.left, y + type.paddings.top)
			.setRelTextPos(RelPos.BOTTOM_RIGHT, true), SPACING);
	}
}
