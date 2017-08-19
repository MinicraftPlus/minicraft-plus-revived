package minicraft.screen;

import minicraft.gfx.Color;
import minicraft.gfx.Rectangle;
import minicraft.gfx.Screen;
import minicraft.gfx.Font;
import minicraft.gfx.FontStyle;
import minicraft.gfx.SpriteSheet;

public abstract class MessageDisplay extends Display {
	
	private String[] message;
	
	protected FontStyle style = new FontStyle(Color.get(-1, 444));
	private int spacing = Font.textHeight()/2;
	
	private Menu parent;
	
	protected MessageDisplay(Menu parent) {
		this.parent = parent;
	}
	
	protected void setMessage(String[] message) { this.message = message; }
	protected void setSpacing(int spacing) { this.spacing = spacing; }
	
	public void tick() {
		if(input.getKey("select").clicked || input.getKey("exit").clicked)
			game.setMenu(parent);
	}
	
	public void render(Screen screen) {
		super.render(screen); // draws the frame, if any
		
		Font.drawParagraph(message, screen, style, spacing);
	}
}
