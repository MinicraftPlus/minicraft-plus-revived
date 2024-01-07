package minicraft.gfx;

import minicraft.screen.RelPos;
import org.tinylog.Logger;

import java.util.Arrays;

public class FontStyle {

	/// this specifies the x and y offsets for each binary value in the "shadow location byte", and is what causes each value to progress in a circle.
	private static int[] shadowPosMap = {0, 1, 1, 1, 0, -1, -1, -1,
		-1, -1, 0, 1, 1, 1, 0, -1};
	/**
	 * The shadowing technique uses binary strings to specify where to outline a string of text. It was going to be a straight byte, since there are 8 positions, but since it's going to be a string anyway, I decided to make it accept a string.
	 * Each position is specified fairly simply: it goes clockwise, starting from the top. Then it goes top right, right, bottom right, bottom, etc. up to top left. It's kind of like a compass, with a position for N, NE, E, etc.
	 * For an example, for the default shadow, the string is "00010000". though, becuase of the way it's designed, the trailing zeros may be dropped, so it could just be "0001". This doesn't quite read like binary, but it doesn't have to, so whatever. :P
	 */

	private int mainColor;

	private int shadowColor;
	private String shadowType;
	private Point anchor;
	private RelPos relTextPos = RelPos.CENTER; // Aligns the complete block of text with the anchor.

	// Used only when drawing paragraphs:
	private RelPos relLinePos = RelPos.CENTER; // When setup for a paragraph with multiple lines, this determines the alignment of each line within the bounds of the paragraph.
	private String[] configuredPara;
	private Rectangle paraBounds;
	private int padX = 0, padY = 0;

	public FontStyle() {
		this(Color.WHITE);
	}

	public FontStyle(int mainColor) {
		this.mainColor = mainColor;
		shadowColor = Color.get(-1, -1);
		shadowType = "";
		anchor = new Point(Screen.w / 2, Screen.h / 2);

		/// By default, the styling is set so as to center the text in the middle of the screen, with no shadow.
	}


	// TODO make a constructor that takes another FontStyle and just copies all the protected fields.


	/// Actually draws the text.
	public void draw(String msg, Screen screen) {
		/// The field variables should NOT be modified! modify local vars instead.

		/// For centering
		Dimension size = new Dimension(Font.textWidth(msg), Font.textHeight());

		Rectangle textBounds = relTextPos.positionRect(size, anchor, new Rectangle());

		if (padX != 0 || padY != 0) {
			size.width += padX;
			size.height += padY;
			Rectangle textBox = relTextPos.positionRect(size, anchor, new Rectangle());

			relLinePos.positionRect(textBounds.getSize(), textBox, textBounds);
		}

		int xPos = textBounds.getLeft();
		int yPos = textBounds.getTop();

		/// For the shadow
		char[] sides = shadowType.toCharArray();
		for (int i = 0; i < 8 && i < sides.length; i++)
			if (sides[i] == '1')
				Font.draw(msg, screen, xPos + shadowPosMap[i], yPos + shadowPosMap[i + 8], shadowColor);

		/// The main drawing of the text:
		Font.draw(msg, screen, xPos, yPos, mainColor);
	}

	public void configureForParagraph(String[] para, int spacing) {
		configuredPara = para; // Save the passed in paragraph for later comparison

		// At this point, the main anchor is meant for the whole paragraph block.
		// When drawing a line, there's an anchor, and then a position around that anchor.
		// In a paragraph, it could be the left side, or right, or top... it depends.
		// Either way, the draw method needs to use a different position.

		Dimension size = new Dimension(Font.textWidth(para), para.length * (Font.textHeight() + spacing));
		paraBounds = relTextPos.positionRect(size, anchor, new Rectangle());
	}

	public void setupParagraphLine(String[] para, int line, int spacing) {
		if (para == null || line < 0 || line >= para.length) {
			Logger.tag("FontStyle").error("FontStyle.java: " +
				(para == null ? "paragraph is null" : "index " + line + " is invalid") + "; can't draw line.");
			return;
		}

		if (configuredPara == null || !Arrays.equals(para, configuredPara))
			configureForParagraph(para, spacing);

		Rectangle textArea = new Rectangle(paraBounds);
		textArea.setSize(textArea.getWidth(), Font.textHeight() + spacing, RelPos.TOP_LEFT);
		textArea.translate(0, line * textArea.getHeight());

		anchor = textArea.getPosition(relTextPos.getOpposite()); // For the relpos to put the rect in the correct pos, the anchor should be fetched using to opposite relpos.

		padX = paraBounds.getWidth() - Font.textWidth(para[line]);
		padY = spacing;
	}

	public void drawParagraphLine(String[] para, int line, int spacing, Screen screen) {
		setupParagraphLine(para, line, spacing);
		draw(para[line], screen);
		padX = 0;
		padY = 0;
	}

	/* -- All the font modifier methods are below. They all return the current FontStyle instance for chaining. -- */

	/**
	 * Sets the color of the text itself.
	 */
	public FontStyle setColor(int col) {
		mainColor = col;
		return this;
	}

	/**
	 * sets the x position of the text anchor. This causes the text to be left-justified, if alignment is reset.
	 */
	public FontStyle setXPos(int pos) {
		return setXPos(pos, true);
	}

	public FontStyle setXPos(int pos, boolean resetAlignment) {
		anchor.x = pos;
		if (resetAlignment) {
			relTextPos = RelPos.getPos(RelPos.RIGHT.xIndex, relTextPos.yIndex);
			relLinePos = RelPos.getPos(RelPos.LEFT.xIndex, relLinePos.yIndex);
		}
		return this;
	}

	/**
	 * sets the y position of the text anchor. This sets the y pos to be the top of the block, if alignment is reset.
	 */
	public FontStyle setYPos(int pos) {
		return setYPos(pos, true);
	}

	public FontStyle setYPos(int pos, boolean resetAlignment) {
		anchor.y = pos;
		if (resetAlignment) {
			relTextPos = RelPos.getPos(relTextPos.xIndex, RelPos.BOTTOM.yIndex);
			relLinePos = RelPos.getPos(relLinePos.xIndex, RelPos.TOP.yIndex);
		}
		return this;
	}

	public FontStyle setAnchor(int x, int y) {
		anchor = new Point(x, y);
		return this;
	}

	/**
	 * Sets the position of the text box relative to the anchor.
	 */
	public FontStyle setRelTextPos(RelPos relPos) {
		return setRelTextPos(relPos, true);
	}

	public FontStyle setRelTextPos(RelPos relPos, boolean setBoth) {
		this.relTextPos = relPos;
		if (setBoth) relLinePos = relTextPos.getOpposite();
		return this;
	}

	/**
	 * Sets the position of a paragraph of text relative to the anchor.
	 */
	public FontStyle setRelLinePos(RelPos relPos) {
		relLinePos = relPos;
		return this;
	}

	/**
	 * This enables text shadowing, and sets the shadow color and type. It is a convenience method that offers a preset for text outlines, and a single shadow in a standard direction.
	 */
	public FontStyle setShadowType(int color, boolean full) {
		String type = full ? "10101010" : "00010000";
		setShadowType(color, type);
		return this;
	}

	/**
	 * This is what acutally sets the values described above. It also allows custom shadows.
	 */
	public FontStyle setShadowType(int color, String type) {
		shadowColor = color;
		shadowType = type;
		return this;
	}

	public int getColor() {
		return mainColor;
	}
}
