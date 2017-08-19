package minicraft.gfx;

import java.util.Arrays;

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
	
	protected int mainColor;
	
	protected int shadowColor;
	protected String shadowType;
	protected int centerMinX, centerMaxX, centerMinY, centerMaxY;
	protected int xPosition = -1, yPosition = -1;
	
	private String[] configuredPara;
	private int paraMinY;
	
	public FontStyle() { this(Color.get(-1, 555)); }
	public FontStyle(int mainColor) {
		this.mainColor = mainColor;
		shadowColor = Color.get(-1, -1);
		shadowType = "";
		centerMinX = 0;
		centerMinY = 0;
		centerMaxX = Screen.w;
		centerMaxY = Screen.h;
		
		/// by default, the styling is set so as to center the text in the middle of the screen, with no shadow.
	}
	
	
	// TODO make a constructor that takes another FontStyle and just copies all the protected fields.
	
	
	/// actually draws the text.
	public void draw(String msg, Screen screen) {
		/// the field variables should NOT be modified! modify local vars instead.
		
		/// for centering
		int cxMin = centerMinX, cyMin = centerMinY;
		int cxMax = centerMaxX == -1 ? Screen.w : centerMaxX;
		int cyMax = centerMaxY == -1 ? Screen.h : centerMaxY;
		
		int xPos = xPosition == -1 ? Font.centerX(msg, cxMin, cxMax) : xPosition;
		int yPos = yPosition == -1 ? Font.centerY(msg, cyMin, cyMax) : yPosition;
		
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
		
		if(yPosition == -1) { // yPosition is auto-centered
			int centerYDouble = centerMinY + centerMaxY;
			int height = para.length * (Font.textHeight() + spacing);
			paraMinY = (centerYDouble - height) / 2; // by doubles to maybe avoid possible rounding errors.
		} else
			paraMinY = yPosition; // save the y position.
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
		
		setYPos(paraMinY + line*Font.textHeight() + line*spacing);
	}
	
	public void drawParagraphLine(String[] para, int line, int spacing, Screen screen) {
		setupParagraphLine(para, line, spacing);
		draw(para[line], screen);
	}
	
	/** All the font modifier methods are below. They all return the current FontStyle instance for chaining. */
	
	/** Sets the color of the text itself. */
	public FontStyle setColor(int col) {
		mainColor = col;
		return this;
	}
	
	/** (assuming pos is >= 0) sets the absolute left x position of the text. This causes the text to be left-justified. */
	public FontStyle setXPos(int pos) {
		xPosition = pos;
		return this;
	}
	/** (assuming pos is >= 0) sets the absolute top y position of the text. This removes vertical centering. */
	public FontStyle setYPos(int pos) {
		yPosition = pos;
		return this;
	}
	
	/** sets the two anchors to center the text between horizontally. This enables horizontal centering. */
	public FontStyle xCenterBounds(int min, int max) {
		centerMinX = min;
		centerMaxX = max;
		xPosition = -1;
		return this;
	}
	
	/** same as above, but vertically. */
	public FontStyle yCenterBounds(int min, int max) {
		centerMinY = min;
		centerMaxY = max;
		yPosition = -1;
		return this;
	}
	
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
	public int getXPos() { return xPosition < 0 ? centerMinX : xPosition; }
	public int getYPos() { return yPosition < 0 ? centerMinY : yPosition; }
}
