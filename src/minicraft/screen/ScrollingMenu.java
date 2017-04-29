package minicraft.screen;

import minicraft.InputHandler;
import minicraft.gfx.Screen;
import minicraft.gfx.Font;
import minicraft.gfx.Color;
import java.util.Arrays;

//run it!
public class ScrollingMenu extends SelectMenu {
	
	//protected String[] allOptions; // all the possible items, not just the displayed ones.
	
	protected int dispSelected;
	protected int dispSize;
	
	private int padding; // how many items are left before reaching the end of the displayed list, under which the menu will scroll (in the opposite direction). If this is -1, the menu will always scroll, effectively keeping the selected item in the middle. Note, the menu will normally stop scrolling if it runs out of items, so that it never displays whitespace in place of an item. If this is -1, then it will display whitespace, favoring keeping the selection in the middle over not displaying whitespace.
	
	// note...to myself... padding does NOT affect ANYTHING except the offset. it doesn't affect the selected index whatsoever.
	
	protected int offset; // since not all elements are displayed at once, this determines which item is at the top.
	/// rule I've decided on: offset + super.selected MUST ALWAYS EQUAL the index of the selected item in allOptions.
	
	// since this does not call the parent tick method, the inherited "selected" will refer to an index in allOptions.
	
	private ScrollingMenu(String[] options, int displayLength/*, int scrollPadding*/, int x, int y, boolean centered, int spacing, int colSel, int colNoSel) {
		super(options, x, y, centered, spacing, colSel, colNoSel);
		//allOptions = options;
		//offset = 0;
		dispSize = Math.min(displayLength, options.length);
		//padding = scrollPadding < 0 ? (this.options.length+1) / 2 : scrollPadding;
		padding = (dispSize+1)/2;// == 5
	}
	public ScrollingMenu(String[] options, int displayLength/*, int scrollPadding*/, int x, int y, int spacing, int colSel, int colNoSel) {
		this(options, displayLength, x, y, false, spacing, colSel, colNoSel);
	}
	public ScrollingMenu(String[] options, int displayLength/*, int scrollPadding*/, int y, int spacing, int colSel, int colNoSel) {
		this(options, displayLength, 0, y, true, spacing, colSel, colNoSel);
	}
	
	public void tick() {
		int prevSel = selected;
		super.tick(); // ticks parent, which changes index in entire array.
		
		// changes index in the displayed "array" (portion)
		dispSelected += selected - prevSel;
		if(dispSelected < 0) dispSelected = 0;
		if(dispSelected > dispSize-1) dispSelected = dispSize - 1;
		offset = selected - dispSelected;
		if(dispSelected < padding && offset > 0) dispSelected = padding-1; // if the cursor is above halfway, and we have space to scroll up, then move the cursor back to the middle.
		if(dispSelected > dispSize-padding && offset+dispSize < options.length) dispSelected = dispSize-padding; // if the cursor is below halfway, and we have space to scroll down, then move the cursor back to the middle.
		
		// when there are no more items to scroll, offset+dispSize == options.length
	}
	
	/*
	public void tick() {
		super.tick();
		int prevSel = selected; // what was selected before this tick.
		
		if(input.getKey("up").clicked) selected--;
		if(input.getKey("down").clicked) selected++;
		
		int dispPos = selected - offset;
		//int realPos = selected + offset;
		if(prevSel != selected) { // if there was a change in selection...
			if(dispPos/* + padding*/// >= options.length) { // if the new selection is past the last displayed element...
			/*	if(realPos < allOptions.length - 1)
					offset++;
				else {
					offset = 0;
					selected = 0;
				}
			}
			
			if(padding == 0 && selected == 0 && prevSel == options.length-1/* && offset+prevSel < allOptions.length-1*//*) {
				selected = prevSel;
				offset++;
			}
			else if(selected == options.length-1 && prevSel == 0)
				selected = prevSel;
			if(selected > options.length-1 - padding)
				offset++;
			if(offset + options.length >= allOptions.length)
		}
		
		
		if(prevSel != selected) Sound.craft.play();
	}*/
	
	public void render(Screen screen) {
		//System.out.println("render offset: " + offset);
		//offset = selected - dispSelected;// + padding; // gets offset of displayed portion of the entire list
		if(offset + dispSize > options.length)
			offset = options.length - dispSize;
		if(offset < 0)
			offset = 0;
		///	sets up for calling parent to display.
		//int prevSel = selected;
		//options = Arrays.copyOfRange(allOptions, offset, offset + dispSize); // sets parent options.
		//selected = dispSelected; // sets selection index.
		super.renderAs(screen, Arrays.copyOfRange(options, offset, offset + dispSize), dispSelected); // calls parent render
		
		//selected = prevSel; // restores full array selection index
	}
}
