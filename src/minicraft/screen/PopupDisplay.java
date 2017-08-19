package minicraft.screen;

import minicraft.gfx.*;

public class PopupDisplay extends MessageDisplay {
	
	private int inputDelay = 10;
	
	public PopupDisplay(String msg) {
		this(msg, Screen.w);
	}
	public PopupDisplay(String msg, int maxWidth) { // max width is in screen coordinates
		super(null);
		
		style.setColor(Color.get(-1, 444));
		
		if(maxWidth > Font.textHeight() * 2)
			maxWidth -= Font.textHeight() * 2;
		
		String[] message = Font.getLines(msg, maxWidth, Font.textHeight()/2);
		setMessage(message);
		
		int frameWidth = message.length > 1 ? maxWidth : Font.textWidth(message);
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
		
		super.tick();
	}
}
