package minicraft.screen;

import minicraft.InputHandler;
import minicraft.gfx.Screen;
import minicraft.gfx.Font;
import minicraft.gfx.Color;
import minicraft.sound.Sound;

public abstract class SelectMenu extends Menu {
	
	protected String[] options;
	protected int selected;
	
	protected int x, y, spacing;
	protected boolean centered;
	
	private int colSelect, colOther;
	
	protected SelectMenu(String[] options, int x, int y, boolean center, int spacing, int col1, int col2) {
		this.options = options;
		this.x = x;
		this.y = y;
		this.spacing = spacing;
		centered = center;
		colSelect = col1;
		colOther = col2;
	}
	protected SelectMenu(String[] options, int x, int y, int spacing, int col1, int col2) {
		this(options, x, y, false, spacing, col1, col2);
	}
	protected SelectMenu(String[] options, int y, int spacing, int col1, int col2) {
		this(options, 0, y, true, spacing, col1, col2);
	}
	
	public void tick() {
		int prevSel = selected;
		
		if(input.getKey("up").clicked) selected--;
		if(input.getKey("down").clicked) selected++;
		
		if(selected >= options.length) selected = 0;
		if(selected < 0) selected = options.length - 1;
		
		if(prevSel != selected) Sound.craft.play();
	}
	
	public void render(Screen screen) {
		renderAs(screen, this.options, selected);
	}
	
	protected void renderAs(Screen screen, String[] options, int sel) {
		for(int i = 0; i < options.length; i++) {
			if(options[i] == null) continue;
			boolean current = sel == i;
			int col = (current ? colSelect : colOther);
			String option = (current ? "> "+options[i]+" <" : options[i]);
			int ypos = y + i*(spacing+Font.textHeight());
			if (centered) Font.drawCentered(option, screen, ypos, col);
			else {
				int x = current ? this.x-Font.textWidth("> ") : this.x;
				Font.draw(option, screen, x, ypos, col);
			}
		}
	}
}
