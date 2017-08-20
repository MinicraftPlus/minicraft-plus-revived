package minicraft.screen;

import java.util.Arrays;

import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.gfx.Color;
import minicraft.gfx.SpriteSheet;
import minicraft.screen.entry.ListEntry;

/// TODO add a feature for the list to loop back to the beginning by having the top options appear as if on bottom, like World load selection.
public class ScrollingMenu extends Menu {
	
	protected ListEntry[] dispOptions;
	protected int dispSelected;
	protected int dispSize;
	
	private int padding; // the meaning of this variable is a bit difficult to explain. But basically, it determines when the menu scrolls, depending on what displayed position the cursor is at.
	
	protected int offset; // since not all elements are displayed at once, this determines which item is at the top.
	
	private boolean wrap; // whether or not to wrap around the options from top and bottom.
	
	protected ScrollingMenu(ListEntry[] options) {
		super(options);
		dispSize = Math.min(options.length, Screen.h / (Font.textHeight() + getLineSpacing()) - 1);
		
		padding = (dispSize+1)/2;
	}
	protected ScrollingMenu(ListEntry[] options, int displayLength) {
		super(options);
		dispSize = Math.max(0, Math.min(displayLength, options.length));
		
		padding = (dispSize+1)/2; // I may or may not choose to make this editable. Currently, though, this has the effect of making the menu scroll upon trying to go past the middle of the displayed list, as long as there's room to scroll.
	}
	
	protected void onSelectionChange(int prevSel, int newSel) {
		// remake the displayed array, but only if the new one is different
		int prevDispSel = dispSelected;
		
		dispSelected += newSel - prevSel; // changes index in the displayed "array" (portion)
		if(dispSelected < 0) dispSelected = 0; // error correct
		if(dispSelected > dispSize-1) dispSelected = dispSize - 1;
		
		offset = selected - dispSelected;
		
		if(dispSelected < padding && offset > 0) dispSelected = padding-1; // if the cursor is above halfway, and we have space to scroll up, then move the cursor back to the middle.
		if(dispSelected > dispSize-padding && offset+dispSize < options.length) dispSelected = dispSize-padding; // if the cursor is below halfway, and we have space to scroll down, then move the cursor back to the middle.
		
		if(offset + dispSize > options.length)
			offset = options.length - dispSize;
		if(offset < 0)
			offset = 0;
		
		if(prevDispSel != dispSelected) {
			dispOptions = Arrays.copyOfRange(options, offset, offset + dispSize - 1);
		}
		
		//super.onSelectionChange(prevSel, newSel); // place the brackets
	}
	
	public void render(Screen screen) {
		/*if(offset + dispSize > options.length)
			offset = options.length - dispSize;
		if(offset < 0)
			offset = 0;
		*/
		//super.renderAs(screen, options.subList(offset, offset + dispSize), dispSelected); // renders the list with the super classes parameters, but temp. replacing the array and index.
		ListEntry[] allOptions = options;
		options = dispOptions;
		int selSave = selected;
		selected = dispSelected;
		
		super.render(screen);
		
		selected = selSave;
		options = allOptions;
	}
}
