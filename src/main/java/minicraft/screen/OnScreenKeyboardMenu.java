package minicraft.screen;

import com.studiohartman.jamepad.ControllerButton;
import minicraft.core.Game;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Sound;
import minicraft.gfx.*;
import minicraft.gfx.Dimension;
import minicraft.gfx.Font;
import minicraft.gfx.Point;
import minicraft.gfx.Rectangle;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class OnScreenKeyboardMenu extends Menu {
	private static final Builder builder = getBuilder();

	private static Builder getBuilder() {
		return new Builder(false, 0, RelPos.CENTER)
			.setPositioning(new Point(0, Screen.h), RelPos.TOP_RIGHT)
			.setMenuSize(new Dimension(Screen.w, 59));
	}

	public OnScreenKeyboardMenu() {
		super(builder.createMenu());
		initKeyboard();
	}

	/**
	 * This checks if there is any controller connected. If true, create the instance. No otherwise.
	 * @return The created menu instance. `null` if there is no controller connected.
	 */
	@Nullable
	public static OnScreenKeyboardMenu checkAndCreateMenu() {
		if (Game.input.anyControllerConnected()) {
			return new OnScreenKeyboardMenu();
		}

		return null;
	}

	public class VirtualKey {
		public final InputHandler.Key key;
		public final String output;

		public VirtualKey(String out, InputHandler.Key key) {
			this.key = key;
			this.output = out;
		}

		public void press() {
			key.toggle(true);
			Game.input.keyTyped(new KeyEvent(new Label(), 0, 0, 0, 0, output.charAt(0)));
		}
	}

	public class StickyVirtualKey extends VirtualKey {
		private boolean sticky = false;
		private final Consumer<Boolean> stickyListener;

		public StickyVirtualKey(Consumer<Boolean> stickyListener) {
			super(null, null);
			this.stickyListener = stickyListener;
		}

		@Override
		public void press() {
			stickyListener.accept(sticky = !sticky);
		}
	}

	private VirtualKey[][] keysF; // Forge keyboard (alphabets)
	private VirtualKey[][] keysB; // Back keyboard (symbols)

	private VirtualKey spaceBar; // The space bar key.
	private VirtualKey backspace; // The backspace key.
	private StickyVirtualKey shiftKey; // The sticky shift key.

	private boolean shiftPressed = false;

	private int x = 0;
	private int y = 0;

	private int keyPressed = 0; // Controls whether to render the selected key as pressed.

	private void initKeyboard() {
		InputHandler input = Game.input;

		spaceBar = new VirtualKey(" ", input.getKey(" "));
		backspace = new VirtualKey("<", input.getKey("backspace"));
		shiftKey = new StickyVirtualKey(this::setShiftPressed);

		keysF = new VirtualKey[4][];
		keysF[0] = new VirtualKey[10];
		keysF[1] = new VirtualKey[10];
		keysF[2] = new VirtualKey[9];
		keysF[3] = new VirtualKey[5];
		keysF[0][0] = new VirtualKey("Q", input.getKey("Q"));
		keysF[0][1] = new VirtualKey("W", input.getKey("W"));
		keysF[0][2] = new VirtualKey("E", input.getKey("E"));
		keysF[0][3] = new VirtualKey("R", input.getKey("R"));
		keysF[0][4] = new VirtualKey("T", input.getKey("T"));
		keysF[0][5] = new VirtualKey("Y", input.getKey("Y"));
		keysF[0][6] = new VirtualKey("U", input.getKey("U"));
		keysF[0][7] = new VirtualKey("I", input.getKey("I"));
		keysF[0][8] = new VirtualKey("O", input.getKey("O"));
		keysF[0][9] = new VirtualKey("P", input.getKey("P"));
		keysF[1][0] = new VirtualKey("A", input.getKey("A"));
		keysF[1][1] = new VirtualKey("S", input.getKey("S"));
		keysF[1][2] = new VirtualKey("D", input.getKey("D"));
		keysF[1][3] = new VirtualKey("F", input.getKey("F"));
		keysF[1][4] = new VirtualKey("G", input.getKey("G"));
		keysF[1][5] = new VirtualKey("H", input.getKey("H"));
		keysF[1][6] = new VirtualKey("J", input.getKey("J"));
		keysF[1][7] = new VirtualKey("K", input.getKey("K"));
		keysF[1][8] = new VirtualKey("L", input.getKey("L"));
		keysF[1][9] = backspace;
		keysF[2][0] = shiftKey;
		keysF[2][1] = new VirtualKey("Z", input.getKey("Z"));
		keysF[2][2] = new VirtualKey("X", input.getKey("X"));
		keysF[2][3] = new VirtualKey("C", input.getKey("C"));
		keysF[2][4] = new VirtualKey("V", input.getKey("V"));
		keysF[2][5] = new VirtualKey("B", input.getKey("B"));
		keysF[2][6] = new VirtualKey("N", input.getKey("N"));
		keysF[2][7] = new VirtualKey("M", input.getKey("M"));
		keysF[2][8] = shiftKey;
		keysF[3][0] = new VirtualKey("/", input.getKey("/"));
		keysF[3][1] = new VirtualKey("?", input.getKey("?"));
//		keysF[3][2] = new VirtualKey("`", input.getKey("`"));
//		keysF[3][3] = new VirtualKey("~", input.getKey("~"));
		keysF[3][2] = spaceBar;
		keysF[3][3] = spaceBar;
		keysF[3][4] = spaceBar;

		keysB = new VirtualKey[4][10];
		keysB[0][0] = new VirtualKey("1", input.getKey("1"));
		keysB[0][1] = new VirtualKey("2", input.getKey("2"));
		keysB[0][2] = new VirtualKey("3", input.getKey("3"));
		keysB[0][3] = new VirtualKey("4", input.getKey("4"));
		keysB[0][4] = new VirtualKey("5", input.getKey("5"));
		keysB[0][5] = new VirtualKey("6", input.getKey("6"));
		keysB[0][6] = new VirtualKey("7", input.getKey("7"));
		keysB[0][7] = new VirtualKey("8", input.getKey("8"));
		keysB[0][8] = new VirtualKey("9", input.getKey("9"));
		keysB[0][9] = new VirtualKey("0", input.getKey("0"));
		keysB[1][0] = new VirtualKey("!", input.getKey("!"));
		keysB[1][1] = new VirtualKey("@", input.getKey("@"));
		keysB[1][2] = new VirtualKey("#", input.getKey("#"));
		keysB[1][3] = new VirtualKey("$", input.getKey("$"));
		keysB[1][4] = new VirtualKey("%", input.getKey("%"));
		keysB[1][5] = new VirtualKey("^", input.getKey("^"));
		keysB[1][6] = new VirtualKey("&", input.getKey("&"));
		keysB[1][7] = new VirtualKey("*", input.getKey("*"));
		keysB[1][8] = new VirtualKey("(", input.getKey("("));
		keysB[1][9] = new VirtualKey(")", input.getKey(")"));
		keysB[2][0] = new VirtualKey("-", input.getKey("-"));
		keysB[2][1] = new VirtualKey("=", input.getKey("="));
		keysB[2][2] = new VirtualKey("_", input.getKey("_"));
		keysB[2][3] = new VirtualKey("+", input.getKey("+"));
		keysB[2][4] = new VirtualKey("[", input.getKey("["));
		keysB[2][5] = new VirtualKey("]", input.getKey("]"));
		keysB[2][6] = new VirtualKey("{", input.getKey("{"));
		keysB[2][7] = new VirtualKey("}", input.getKey("}"));
		keysB[2][8] = new VirtualKey("\\", input.getKey("\\"));
		keysB[2][9] = new VirtualKey("|", input.getKey("|"));
		keysB[3][0] = shiftKey;
		keysB[3][1] = new VirtualKey(";", input.getKey(";"));
		keysB[3][2] = new VirtualKey(":", input.getKey(":"));
		keysB[3][3] = new VirtualKey("'", input.getKey("'"));
		keysB[3][4] = new VirtualKey("\"", input.getKey("\""));
		keysB[3][5] = new VirtualKey(",", input.getKey(","));
		keysB[3][6] = new VirtualKey("<", input.getKey("<"));
		keysB[3][7] = new VirtualKey(".", input.getKey("."));
		keysB[3][8] = new VirtualKey(">", input.getKey(">"));
		keysB[3][9] = spaceBar;
	}

	private void setShiftPressed(boolean pressed) {
		this.shiftPressed = pressed;
		x = 0;
		if (pressed) {
			y = 2;
		} else {
			y = 3;
		}
	}

	@Override
	public void tick(InputHandler input) throws OnScreenKeyboardMenuTickActionCompleted, OnScreenKeyboardMenuBackspaceButtonActed {
		if (keyPressed > 0) keyPressed--; // Resetting rendered pressing status.

		// This is only controllable by controller.
		if (visible) {
			VirtualKey[][] keys = shiftPressed? keysB: keysF;
			if (input.buttonPressed(ControllerButton.A)) { // Select
				keys[y][x].press();
				Sound.play("select"); // Lack of sounds.
				keyPressed = 5;
			} else if (input.buttonPressed(ControllerButton.B)) { // Backspace
				backspace.press();
				Sound.play("confirm");
				throw new OnScreenKeyboardMenuBackspaceButtonActed();
			} else if (input.buttonPressed(ControllerButton.DPAD_UP)) {
				if (y > 0) {
					y--;
					if (x >= keys[y].length) {
						x = keys[y].length - 1;
					}

					Sound.play("select");
				}

				throw new OnScreenKeyboardMenuTickActionCompleted();
			} else if (input.buttonPressed(ControllerButton.DPAD_DOWN)) {
				if (y < keys.length - 1) {
					y++;
					if (x >= keys[y].length) {
						x = keys[y].length - 1;
					}

					Sound.play("select");
				}

				throw new OnScreenKeyboardMenuTickActionCompleted();
			} else if (input.buttonPressed(ControllerButton.DPAD_LEFT)) {
				if (x > 0) {
					x--;
					Sound.play("select");
				}

				throw new OnScreenKeyboardMenuTickActionCompleted();
			} else if (input.buttonPressed(ControllerButton.DPAD_RIGHT)) {
				if (x < keys[y].length - 1) {
					x++;
					Sound.play("select");
				}

				throw new OnScreenKeyboardMenuTickActionCompleted();
			}
		}
	}

	private boolean visible = true;

	public void setVisible(boolean visible) {
		if (this.visible != visible) {
			Rectangle rec = getBounds();
			translate(0, visible? -rec.getHeight(): rec.getHeight());
			this.visible = visible;
		}
	}
	public boolean isVisible() {
		return visible;
	}

	@Override
	public void render(Screen screen) {
		super.render(screen);

		BiConsumer<Integer, Integer> colorPixel = (pos, color) -> {
			if (pos < screen.pixels.length && pos > 0)
				screen.pixels[pos] = color;
		};

		Rectangle bounds = getBounds();
		int width = bounds.getWidth();
		int height = bounds.getHeight();
		int renderingTop = bounds.getTop();
		for (int x = 0; x < width; x++) { // Rendering background.
			for (int y = 0; y < height; y++) {
				colorPixel.accept(x + (y + renderingTop) * Screen.w, 0x1CFCFCF);
			}
		}

		for (int x = 0; x < width; x++) { // Rendering upper edge.
			for (int y = 0; y < 2; y++) {
				colorPixel.accept(x + (y + renderingTop) * Screen.w, 0x1EFEFEF);
			}
		}

		final int keyWidth = 16;
		final int keyHeight = 14;
		VirtualKey[][] keys = shiftPressed? keysB: keysF;
		for (int r = 0; r < keys.length; r++) {
			int xOffset = (Screen.w - (keys[r].length * keyWidth)) / 2;
			int y = renderingTop + 2 + r * keyHeight;
			for (int c = 0; c < keys[r].length; c++) {
				VirtualKey key = keys[r][c];
				int x = xOffset + c * keyWidth;
				if (key != spaceBar) {
					int color = keyPressed > 0 && r == this.y && c == this.x? 0x1EFEFF0: 0x1FDFDFD;
					if (key == backspace) { // Rendering the left arrow.
						for (int i = 1; i < 9; i++) {
							colorPixel.accept(x + keyWidth/2 + i - 4 + (y + keyHeight/2) * Screen.w, color);
						}

						colorPixel.accept(x + keyWidth/2 - 2 + (y + keyHeight/2 - 1) * Screen.w, color);
						colorPixel.accept(x + keyWidth/2 - 2 + (y + keyHeight/2 + 1) * Screen.w, color);
						colorPixel.accept(x + keyWidth/2 - 1 + (y + keyHeight/2 - 2) * Screen.w, color);
						colorPixel.accept(x + keyWidth/2 - 1 + (y + keyHeight/2 + 2) * Screen.w, color);
						colorPixel.accept(x + keyWidth/2 + (y + keyHeight/2 - 3) * Screen.w, color);
						colorPixel.accept(x + keyWidth/2 + (y + keyHeight/2 + 3) * Screen.w, color);
					} else if (key == shiftKey) { // Rendering the up arrow
						for (int i = 1; i < 9; i++) {
							colorPixel.accept(x + keyWidth/2 + (y + keyHeight/2 + i - 4) * Screen.w, color);
						}

						colorPixel.accept(x + keyWidth/2 - 1 + (y + keyHeight/2 - 2) * Screen.w, color);
						colorPixel.accept(x + keyWidth/2 + 1 + (y + keyHeight/2 - 2) * Screen.w, color);
						colorPixel.accept(x + keyWidth/2 - 2 + (y + keyHeight/2 - 1) * Screen.w, color);
						colorPixel.accept(x + keyWidth/2 + 2 + (y + keyHeight/2 - 1) * Screen.w, color);
						colorPixel.accept(x + keyWidth/2 - 3 + (y + keyHeight/2) * Screen.w, color);
						colorPixel.accept(x + keyWidth/2 + 3 + (y + keyHeight/2) * Screen.w, color);
					} else
						Font.draw(key.output, screen, x + keyWidth/2 - 3, y + keyHeight/2 - 3, color);
				}

				for (int i = 0; i <= keyHeight; i++) { // Rendering left and right border.
					colorPixel.accept(x + (y + i) * Screen.w, 0x1BCBCBC);
					colorPixel.accept(x + keyWidth + (y + i) * Screen.w, 0x1BCBCBC);
				} for (int i = 0; i <= keyWidth; i++) { // Rendering top and bottom border.
					colorPixel.accept(x + i + y * Screen.w, 0x1BCBCBC);
					colorPixel.accept(x + i + (y + keyHeight) * Screen.w, 0x1BCBCBC);
				}
			}
		}

		{
			int xOffset = (Screen.w - (keys[y].length * keyWidth)) / 2;
			int yy = renderingTop + 2 + y * keyHeight;
			int xx = xOffset + x * keyWidth;
			int color = keyPressed > 0? 0x1EFEFF0: 0x1DFDFE0;
			for (int i = 0; i <= keyHeight; i++) { // Rendering left and right border.
				colorPixel.accept(xx + (yy + i) * Screen.w, color);
				colorPixel.accept(xx + keyWidth + (yy + i) * Screen.w, color);
				colorPixel.accept(xx + 1 + (yy + i) * Screen.w, color);
				colorPixel.accept(xx - 1 + keyWidth + (yy + i) * Screen.w, color);
			} for (int i = 0; i <= keyWidth; i++) { // Rendering top and bottom border.
				colorPixel.accept(xx + i + yy * Screen.w, color);
				colorPixel.accept(xx + i + (yy + keyHeight) * Screen.w, color);
				colorPixel.accept(xx + i + (yy + 1) * Screen.w, color);
				colorPixel.accept(xx + i + (yy - 1 + keyHeight) * Screen.w, color);
			}
		}
	}

	public static class OnScreenKeyboardMenuTickActionCompleted extends RuntimeException {}
	public static class OnScreenKeyboardMenuBackspaceButtonActed extends RuntimeException {}
}
