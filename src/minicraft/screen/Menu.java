package minicraft.screen;

import java.util.List;
import minicraft.Game;
import minicraft.InputHandler;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.FontStyle;
import minicraft.gfx.Rectangle;
import minicraft.gfx.Screen;
import minicraft.Sound;

/*** The Menu class is now basically the SelectMenu class... but better. ;)  On another note, perhaps... I *could* make this class not abstract, but I don't really want random menus being generated on the fly, so I'll stick with a class per menu type. **/
public abstract class Menu extends Display {
	
	private int selected;
	private int highlightColor, offColor;
	private List<String> options;
	
	//private String[] oldOptions;
	
	protected Menu() {}
	protected Menu(List<String> options, Rectangle frame, int colOn, int colOff) {
		super().setFrameBounds(frame);
		this.options = options;
		super.setText(options);
		super.setStyle(new FontStyle(Color.get(-1, 333)).setShadowType(Color.get(-1, 555), "0"));
		selected = 0;
		highlightColor = colOn;
		offColor = colOff;
	}
	
	public void tick() {
		int size = getNumLines();
		int prevSel = selected;
		
		if(input.getKey("up").clicked) selected--;
		if(input.getKey("down").clicked) selected++;
		
		if(selected >= size) selected = 0;
		if(selected < 0) selected = size - 1;
		
		if(prevSel != selected)
			onSelectionChange(prevSel, selected);
	}
	
	public void render(Screen screen) {
		// the super should render the options as well.
		
		/*
		// FIXME THIS isn't going to work! I can't make a random line of text a different color with the current setup... but maybe... I can just draw them all, but then overwrite the selected one? Yeah.... that could work...
		String[] newOptions = options.toArray(new String[options.size()]);
		if(newOptions.length > 0) {
			try {
				newOptions.set(selected, "> "+newOptions.get(selected)+" <");
			} catch(IndexOutOfBoundsException ex) {
				selected = Math.max(0, Math.min(selected, options.size()-1));
			}
		}
		
		if(!Arrays.deepEquals(newOptions, oldOptions)) {
			oldOptions = newOptions;
			super.setText(newOptions);
		}*/
		
		
		//String oldSel = options.get(selected);
		//options.set(selected, "> "+options.get(selected)+" <");
		
		super.render(screen);
		
		//options.set(selected, oldSel);
		
		// now, render the selected option differently... the issue is likely going to be the positioning... maybe... oh! I've got it! How about I render everything as white, including the selected one being edited (will have the effect of popping out that one), and then I replace the selected one with an empty string, and render it again as dark! It may not be all *that* efficient... but it might work for now...
		
		/// ...of course, if I find an easy way to get the screen position of the selected option, then I'm ditching the above. It's cool, but inefficient, I feel.
		/// hmmm... what about the opposite, replacing the others with empty strings...? idk...
		// idk if I can just add a method to Display, b/c the positioning is back in the Font class...
		
		//String prevoptions
	}
	
	/** This was made with expansion in mind, mostly. It's sort of like an API / event method. It gets called whenever the selected option changes. I can see it being helpful for scrolling menus. */
	protected void onSelectionChange(int oldSelection, int newSelection) {
		Sound.craft.play();
		try {
			String prev = options.get(oldSelection);
			options.set(oldSelection, prev.substring(3, prev.length()-2));
		} catch(IndexOutOfBoundsException ex) {}
		options.set(selected, "~> "+options.get(selected)+" <");
		setText(options);
	}
	
	//protected abstract void updateOptions();
	
	public void renderItemList(Screen screen, int xo, int yo, int x1, int y1,
	  List<? extends ListItem> listItems, int selected) {
		boolean renderCursor = true;
		if (selected < 0) { // this is set to negative purposely for the express reason of disabling the cursor.
			selected = -selected - 1;
			renderCursor = false;
		}
		int w = x1 - xo;
		int h = y1 - yo - 1;
		int i0 = 0;
		int i1 = listItems.size();
		if (i1 > h) i1 = h;
		int io = selected - h / 2;
		if (io > listItems.size() - h) io = listItems.size() - h; /// this is what stops the inventory from scrolling when you reach the bottom.
		if (io < 0) io = 0;
		
		for (int i = i0; i < i1; i++) {
			listItems.get(i + io).renderInventory(screen, (xo+1) * 8, (yo+1 + i) * 8);
		}
		
		if (renderCursor) {
			int yy = selected + 1 - io + yo;
			Font.draw(">", screen, (xo + 0) * 8, yy * 8, Color.get(-1, 555));
			Font.draw("<", screen, (xo + w) * 8, yy * 8, Color.get(-1, 555));
		}
	}
}
