package minicraft.gfx;

import java.util.Arrays;

import minicraft.core.Game;
import minicraft.core.Network;
import minicraft.screen.RelPos;

public class FontStyle {
	/*
		draw needs some parameters...
		CENTERED or no - and can be screen, or given bounds
			-centering bounds: nothing, or two ints
		
		SHADOWED or no - and can be single default, full outline, or custom
			-shadow color: int
			-shadow type: nothing, boolean, or string
		
		ALL COMBOS:
		--centering possibilities--
			++centering types++
				not centered
				auto centered to screen
				centered in given bounds
			++ with each var:
				x
				y
		-- repeated for each shadow type:
			no shadow
			shadow true (outline)
			shadow false (norm shadow)
			shadow custom positions
		
		total possibilities: 6561...
		
		All draw methods have the parameters:
		-String msg
		-Screen screen
		-int color
	*/
	/*
		the method:
		have a font builder, with the various options:
		
		.addCenterX( | int min, int max) - nothing means screen width, or can specify bounds
		.addCenterY( | int min, int max) - nothing means screen height, or can specify bounds
		.addShadow( | boolean fullOutline | String outlineType, int color) - specifies shadow outline
		.draw() - draws the text to the screen with the given options.
	*/
	
	/// this specifies the x and y offsets for each binary value in the "shadow location byte", and is what causes each value to progress in a circle.
	private static int[] shadowPosMap = {	 0,  1,  1,  1,  0, -1, -1, -1,
										-1, -1,  0,  1,  1,  1,  0, -1};
	/**
		The shadowing technique uses binary strings to specify where to outline a string of text. It was going to be a straight byte, since there are 8 positions, but since it's going to be a string anyway, I decided to make it accept a string.
		Each position is specified fairly simply: it goes clockwise, starting from the top. Then it goes top right, right, bottom right, bottom, etc. up to top left. It's kind of like a compass, with a position for N, NE, E, etc.
		For an example, for the default shadow, the string is "00010000". though, becuase of the way it's designed, the trailing zeros may be dropped, so it could just be "0001". This doesn't quite read like binary, but it doesn't have to, so whatever. :P
	*/
	
	private int mainColor;
	
	private int shadowColor;
	private String shadowType;
	//private int anchorMinX, anchorMaxX, anchorMinY, anchorMaxY; // specifies the anchor pos as the center of the two bounds, while also specifying paragraph dimensions.
	private Point anchor, lineAnchor;
	//private int xPosition = -1, yPosition = -1;
	private RelPos relTextPos = RelPos.CENTER; // aligns the complete block of text with the anchor. 
	private RelPos relLinePos = RelPos.CENTER; // when setup for a paragraph with multiple lines, this determines the alignment of each line within the bounds of the paragraph.
	//private RelPos relParaPos = RelPos.CENTER; // aligns each line of text within the main paragraph block.
	
	private String[] configuredPara;
	private Rectangle paraBounds;
	private int padX = 0, padY = 0;
	
	public FontStyle() { this(Color.WHITE); }
	public FontStyle(int mainColor) {
		this.mainColor = mainColor;
		shadowColor = Color.get(-1, -1);
		shadowType = "";
		anchor = new Point(Screen.w/2, Screen.h/2);
		// anchorMinX = 0;
		// anchorMinY = 0;
		// anchorMaxX = Screen.w;
		// anchorMaxY = Screen.h;
		
		/// by default, the styling is set so as to center the text in the middle of the screen, with no shadow.
	}
	
	
	// TODO make a constructor that takes another FontStyle and just copies all the protected fields.
	
	
	/// actually draws the text.
	public void draw(String msg, Screen screen) {
		/// the field variables should NOT be modified! modify local vars instead.
		
		/// for centering
		/*
		int cxMin = anchorMinX;
		int cyMin = anchorMinY;
		int cxMax = anchorMaxX == -1 ? Screen.w : anchorMaxX;
		int cyMax = anchorMaxY == -1 ? Screen.h : anchorMaxY;
		
		int xPos = xPosition == -1 ? Font.centerX(msg, cxMin, cxMax) : xPosition;
		int yPos = yPosition == -1 ? Font.centerY(msg, cyMin, cyMax) : yPosition;
		*/
		
		Dimension size = new Dimension(Font.textWidth(msg), Font.textHeight());
		
		Rectangle textBounds = relTextPos.positionRect(size, lineAnchor, new Rectangle());
		
		if(padX != 0 || padY != 0) {
			size.width += padX;
			size.height += padY;
			Rectangle textBox = relTextPos.positionRect(size, lineAnchor, new Rectangle());
			
			relLinePos.positionRect(textBounds.getSize(), textBox, textBounds);
		}
		
		// account for anchor
		//Point textPos = relTextPos.positionRect(textSize, textBox);
		// a paragraph in a line 
		/*
			- position the line of text around the anchor.
			- 
		 */
		int xPos = textBounds.getLeft();
		int yPos = textBounds.getTop();
		
		/// for the shadow
		char[] sides = shadowType.toCharArray();
		for(int i = 0; i < 8 && i < sides.length; i++)
			if(sides[i] == '1')
				Font.draw(msg, screen, xPos + shadowPosMap[i], yPos + shadowPosMap[i+8], shadowColor);
	    
		/*// a little feature to control what color you paint with.
		int mainCol = mainColor;
		if(msg.startsWith("~")) {
			msg = msg.substring(1);
			mainCol = shadowColor;
		}*/
		
		/// the main drawing of the text:
		Font.draw(msg, screen, xPos, yPos, mainColor);
	}
	
	public void configureForParagraph(String[] para, int spacing) {
		configuredPara = para; // save the passed in paragraph for later comparison
		
		// at this point, the main anchor is meant for the whole paragraph block.
		// when drawing a line, there's an anchor, and then a position around that anchor.
		// in a paragraph, it could be the left side, or right, or top... it depends.
		// either way, the draw method needs to use a different position.
		
		Dimension size = new Dimension(Font.textWidth(para), para.length*(Font.textHeight()+spacing));
		paraBounds = relTextPos.positionRect(size, anchor, new Rectangle());
		
		/*
		if(yPosition == -1) { // yPosition is auto-centered
			int centerYDouble = anchorMinY + anchorMaxY;
			int height = para.length * (Font.textHeight() + spacing);
			paraMinY = (centerYDouble - height) / 2; // by doubles to maybe avoid possible rounding errors.
		} else
			paraMinY = yPosition; // save the y position.*/
	}
	
	public void setupParagraphLine(String[] para, int line, int spacing) {
		if(para == null || line < 0 || line >= para.length) {
			System.err.print("FontStyle.java: ");
			if(para == null) System.err.print("paragraph is null");
			else System.err.print("index "+line+" is invalid");
			System.err.println("; can't draw line.");
			return;
		}
		
		if(configuredPara == null || !Arrays.equals(para, configuredPara))
			configureForParagraph(para, spacing);
		
		//setYPos(paraMinY + line*Font.textHeight() + line*spacing);
		Rectangle textArea = new Rectangle(paraBounds);
		textArea.setSize(textArea.getWidth(), Font.textHeight()+spacing, RelPos.TOP_LEFT);
		textArea.translate(0, line*textArea.getHeight());
		//Rectangle textArea = new Rectangle(paraBounds.getLeft(), paraBounds.getTop() + line*(Font.textHeight()+spacing), paraBounds.getWidth(), Font.textHeight()+spacing, Rectangle.CORNER_DIMS);
		
		lineAnchor = textArea.getPosition(relTextPos);
		
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
	
	/** Sets the color of the text itself. */
	public FontStyle setColor(int col) {
		mainColor = col;
		return this;
	}
	
	/** sets the x position of the text anchor. This causes the text to be left-justified, if alignment is reset. */
	public FontStyle setXPos(int pos) { return setXPos(pos, true); }
	public FontStyle setXPos(int pos, boolean resetAlignment) {
		anchor.x = pos;
		lineAnchor = new Point(anchor);
		if(resetAlignment) {
			relTextPos = RelPos.getPos(RelPos.RIGHT.xIndex, relTextPos.yIndex);
			relLinePos = RelPos.getPos(RelPos.LEFT.xIndex, relLinePos.yIndex);
		}
		return this;
	}
	/** sets the y position of the text anchor. This sets the y pos to be the top of the block, if alignment is reset. */
	public FontStyle setYPos(int pos) { return setYPos(pos, true); }
	public FontStyle setYPos(int pos, boolean resetAlignment) {
		anchor.y = pos;
		lineAnchor = new Point(anchor);
		if(resetAlignment) {
			relTextPos = RelPos.getPos(relTextPos.xIndex, RelPos.BOTTOM.yIndex);
			// relLinePos = RelPos.getPos(relLinePos.xIndex, RelPos.BOTTOM.yIndex);
		}
		return this;
	}
	
	/** Sets the position of the text box relative to the anchor. */
	public FontStyle setRelTextPos(RelPos relPos) { return setRelTextPos(relPos, true); }
	public FontStyle setRelTextPos(RelPos relPos, boolean setBoth) {
		this.relTextPos = relPos;
		if(setBoth) relLinePos = relTextPos.getOpposite();
		return this;
	}
	
	/** Sets the position of a paragraph of text relative to the anchor. */
	public FontStyle setRelLinePos(RelPos relPos) {
		relLinePos = relPos;
		return this;
	}
	
	/*
	*//** sets the two anchors to center the text between horizontally. This enables horizontal centering. *//*
	public FontStyle xCenterBounds(int min, int max) {
		anchorMinX = min;
		anchorMaxX = max;
		xPosition = -1;
		return this;
	}
	
	*//** same as above, but vertically. *//*
	public FontStyle yCenterBounds(int min, int max) {
		anchorMinY = min;
		anchorMaxY = max;
		yPosition = -1;
		return this;
	}
	*/
	
	/** This enables text shadowing, and sets the shadow color and type. It is a convenience method that offers a preset for text outlines, and a single shadow in a standard direction. */
	public FontStyle setShadowType(int color, boolean full) {
		String type = full ? "10101010" : "00010000";
		setShadowType(color, type);
		return this;
	}
	
	/** This is what acutally sets the values described above. It also allows custom shadows. */
	public FontStyle setShadowType(int color, String type) {
		shadowColor = color;
		shadowType = type;
		return this;
	}
	
	/** getters. */
	
	public int getColor() {return mainColor;}
	// public int getXPos() { return xPosition < 0 ? anchorMinX : xPosition; }
	// public int getYPos() { return yPosition < 0 ? anchorMinY : yPosition; }
}
