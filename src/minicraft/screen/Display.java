package minicraft.screen;

import minicraft.Game;
import minicraft.InputHandler;
import minicraft.gfx.Screen;

public abstract class Display {
	protected Game game;
	protected InputHandler input;
	protected Display parent;
	
	public void init(Game game, InputHandler input, Display parent) {
		this.input = input;
		this.game = game;
	}
	
	private Frame[] frames = null;
	
	protected Display() {
		//setFrameColors(Color.get(-1, 1, 5, 445), Color.get(005, 005), Color.get(5, 5, 5, 550)); // this will probably be the case very frequently, so it's the defualt.
	}
	
	protected Display setFrames(Frame frame) { this.frames = (new Frame[] {frame}); return this; }
	protected Display setFrames(Frame[] frames) { this.frames = frames; return this; }
	
	/** Sets the color scheme of all the frames at once. */
	protected Display setFrameColors(int titleCol, int midCol, int sideCol) {
		if(frames == null) return this;
		
		for(Frame frame: frames)
			frame.setColors(titleCol, midCol, sideCol);
		
		return this;
	}
	
	/** The behavior method. At this level, nothing is known of the behavior of the display, so it is abstract. */
	public abstract void tick();
	
	public void render(Screen screen) {
		renderFrames(screen);
		
		//if(title != null && frame == null) // draw the title centered at the top of the screen, if there's no frame.
			//Font.drawCentered(title, screen, SpriteSheet.boxWidth, titleColor);
		
		/*if(text != null) {
			if(style == null) style = new FontStyle(Color.get(-1, 555));
			for(int i = 0; i < text.length; i++)
				renderLine(screen, style, i);
		}*/
	}
	
	/** This renders the blue frame you see when you open up the crafting/inventory menus.
	 *  The width & height are based on 4 points (Staring x & y positions (0), and Ending x & y positions (1)). */
	protected void renderFrames(Screen screen) {
		if(frames == null) return;
		
		for(Frame frame: frames)
			frame.render(screen);
	}
}
