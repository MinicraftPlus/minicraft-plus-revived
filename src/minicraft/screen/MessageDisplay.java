package minicraft.screen;

import minicraft.gfx.Color;
import minicraft.gfx.Rectangle;
import minicraft.gfx.Screen;
import minicraft.gfx.Font;
import minicraft.gfx.FontStyle;
import minicraft.gfx.SpriteSheet;

public class MessageDisplay extends Display {
	
	private String[] message;
	private int inputDelay;
	
	private static FontStyle style = new FontStyle(Color.get(-1, 444));
	
	public MessageDisplay(String msg, int maxWidth) { // max width is in screen coordinates
		super();
		
		setTextStyle(style);
		setLineSpacing(Font.textHeight()/2);
		
		inputDelay = 10;
		
		if(maxWidth > Font.textHeight() * 2)
			maxWidth -= Font.textHeight() * 2;
		
		message = Font.getLines(msg, maxWidth, Font.textHeight()/2);
		
		int frameWidth = message.length > 1 ? maxWidth : Font.textWidth(message[0]);
		frameWidth += Font.textHeight() * 2;
		
		int frameHeight = message.length * Font.textHeight() + (message.length - 1) * Font.textHeight()/2;
		frameHeight += Font.textHeight() * 2;
		
		setFrames(new Frame("", new Rectangle(Screen.w/2, Screen.h/2, frameWidth, frameHeight, Rectangle.CENTER)));
	}
	
	public void tick() {
		if(inputDelay > 0) {
			inputDelay--;
			return;
		}
		
		if(input.getKey("select").clicked || input.getKey("exit").clicked)
			game.setMenu(null);
	}
}
