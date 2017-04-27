package minicraft.screen;

import minicraft.InputHandler;
import minicraft.gfx.Screen;
import minicraft.gfx.Font;
import minicraft.gfx.Color;
import minicraft.sound.Sound;

public abstract class SelectMenu extends Menu {
	
	protected String[] choices;
	protected int selected;
	
	protected int x, y, spacing;
	protected boolean centered;
	
	public SelectMenu(String[] choices, int x, int y, int spacing) {
		this.choices = choices;
		this.x = x;
		this.y = y;
		this.spacing = spacing;
		centered = false;
	}
	public SelectMenu(String[] choices, int y, int spacing) {
		this.choices = choices;
		x = 0; // this doesn't matter, it won't be used.
		this.y = y;
		this.spacing = spacing;
		centered = true;
	}
	
	public void tick() {
		/*if(input.getKey("escape").clicked && parent != this) {
			game.setMenu(parent);
			return;
		}*/
		
		int prevSel = selected;
		
		if(input.getKey("up").clicked) selected--;
		if(input.getKey("down").clicked) selected++;
		
		if(selected >= choices.length) selected = 0;
		if(selected < 0) selected = choices.length - 1;
		
		if(prevSel != selected) Sound.craft.play();
	}
	
	public void render(Screen screen) {
		for(int i = 0; i < choices.length; i++) {
			boolean current = selected == i;
			int col = (current ? Color.get(-1, 555) : Color.get(-1, 111));
			String option = (current ? "> "+choices[i]+" <" : choices[i]);
			if (centered) Font.drawCentered(option, screen, y + i*spacing, col);
			else {
				int x = current ? this.x-Font.textWidth("> ") : this.x;
				Font.draw(option, screen, x, y + i*spacing, col);
			}
		}
	}
}
