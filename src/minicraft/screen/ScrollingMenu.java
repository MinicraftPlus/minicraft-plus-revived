package minicraft.screen;

import java.util.Arrays;

import minicraft.Game;
import minicraft.InputHandler;
import minicraft.gfx.Screen;
import minicraft.screen.entry.ListEntry;

public class ScrollingMenu extends Menu {
	
	private int dispSelection;
	private int displayLength;
	private ListEntry[] dispEntries;
	
	/**
	 * Padding determines how many entries should be left visible past the current selection, before actually scrolling.
	 * 
	 * In the constructor, it's a float between 0 and 1;
	 * 0 means the menu shouldn't scroll until the cursor reaches the top, and
	 * 1 means always keep the cursor in the middle (unless we've reached the edge of the list, of course).
	 * 
	 * This gets converted into an actual entry count in the constructor, so the field below always holds literally the number of entries, rather than a float.
	 */
	private int padding;
	//private boolean wrap;
	
	protected ScrollingMenu(MenuData data, int dispLen, /*boolean wrap, */float padding, Frame... frames) {
		this(data, false, dispLen, padding, frames);
	}
	protected ScrollingMenu(MenuData data, boolean mutable, int dispLen, /*boolean wrap, */float padding, Frame... frames) {
		super(data, mutable, frames);
		dispSelection = getSelection();
		this.displayLength = dispLen;
		
		if(padding < 0) padding = 0;
		if(padding > 1) padding = 1;
		
		// if padding = 1, this.padding = dispLen / 2
		// if padding = 0, this.padding = 0
		// if padding = 0.5, this.padding = dispLen / 4
		
		// if padding = n, this.padding = Math.round(n * dispLen / 2)
		
		// example, if dispLen = 10, and padding = 0.75, then this.padding = 0.75 * 10 / 2 = 7.5 / 2 = 3.75 -> 4
		
		this.padding = Math.round(padding * dispLen / 2);
		
		dispEntries = Arrays.copyOfRange(getEntries(), 0, Math.min(displayLength, getNumEntries()));
	}
	
	@Override
	public void tick(InputHandler input) {
		int prevSel = getSelection();
		
		super.tick(input);
		
		if(Game.getMenuType() != this)
			return; // don't continue if we aren't still the current menu
		
		updateSelection(prevSel);
	}
	
	@Override
	void updateEntries() {
		int selection = getSelection();
		super.updateEntries();
		updateSelection(selection);
	}
	
	private void updateSelection(int prevSel) {
		if(prevSel == getSelection()) return;
		
		int prevOffset = prevSel - dispSelection;
		// selection changed; update displayed entries and selection
		dispSelection += getSelection() - prevSel;
		
		if(dispSelection < 0) dispSelection = 0;
		if(dispSelection >= displayLength) dispSelection = displayLength - 1;
		
		// check if dispSelection is past padding point, and if so, bring it back in
		
		int offset = getSelection() - dispSelection;
		
		// for scrolling up
		while(dispSelection < padding && offset > 0) {
			offset--;
			dispSelection++;
		}
		
		// for scrolling down
		while(displayLength - dispSelection < padding && offset + displayLength < getNumEntries()) {
			offset++;
			dispSelection--;
		}
		
		// use offset to redefine entry list, if it has changed
		if(prevOffset != offset) {
			dispEntries = Arrays.copyOfRange(getEntries(), offset, Math.min(offset+displayLength, getNumEntries()));
		}
	}
	
	@Override
	public void render(Screen screen) {
		renderFrames(screen);
		getMenuType().render(screen);
		super.renderEntries(screen, dispSelection, dispEntries);
	}
}
