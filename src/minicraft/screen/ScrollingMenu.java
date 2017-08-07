package minicraft.screen;

import java.util.List;
import minicraft.gfx.Screen;

/// TODO add a feature for the list to loop back to the beginning by having the top options appear as if on bottom, like World load selection.
public class ScrollingMenu extends Menu {
	
	protected String[] dispOptions;
	protected int dispSelected;
	protected int dispSize;
	
	private int padding; // the meaning of this variable is a bit difficult to explain. But basically, it determines when the menu scrolls, depending on what displayed position the cursor is at.
	
	protected int offset; // since not all elements are displayed at once, this determines which item is at the top.
	
	private boolean wrap; // whether or not to wrap around the options from top and bottom.
	
	protected ScrollingMenu(String[] options, int displayLength) {
		this(options, displayLength, Color.get(-1, 555), Color.get(-1, 333));
	}
	protected ScrollingMenu(String[] options, int displayLength, int colSel, int colNoSel) {
		super(options, colSel, colNoSel);
		dispSize = Math.min(displayLength, options.length);
		
		padding = (dispSize+1)/2; // I may or may not choose to make this editable. Currently, though, this has the effect of making the menu scroll upon trying to go past the middle of the displayed list, as long as there's room to scroll.
	}
	
	/*public void tick() {
		super.tick(); // ticks parent, which possibly changes index in the entire array and calls onSelectionChange.
		
		
		
		
		// when there are no more items to scroll, offset+dispSize == options.size()
	}*/
	
	protected void onSelectionChange(int prevSel, int newSel) {
		// remake the displayed array, but only if the new one is different
		int prevDispSel = dispSelected;
		
		dispSelected += newSel - prevSel; // changes index in the displayed "array" (portion)
		if(dispSelected < 0) dispSelected = 0; // error correct
		if(dispSelected > dispSize-1) dispSelected = dispSize - 1;
		offset = selected - dispSelected;
		if(dispSelected < padding && offset > 0) dispSelected = padding-1; // if the cursor is above halfway, and we have space to scroll up, then move the cursor back to the middle.
		if(dispSelected > dispSize-padding && offset+dispSize < text.length) dispSelected = dispSize-padding; // if the cursor is below halfway, and we have space to scroll down, then move the cursor back to the middle.
		
		super.onSelectionChange(prevSel, newSel); // place the brackets
	}
	
	public void render(Screen screen) {
		if(offset + dispSize > text.length)
			offset = text.length - dispSize;
		if(offset < 0)
			offset = 0;
		
		//super.renderAs(screen, options.subList(offset, offset + dispSize), dispSelected); // renders the list with the super classes parameters, but temp. replacing the array and index.
		String[] allOptions = text;
		text = Arrays.copyOfRange(text, offset, offset + dispSize);
		int selSave = selected;
		selected = dispSelected;
		
		super.render(screen);
		
		selected = selSave;
		text = allOptions;
	}
}
