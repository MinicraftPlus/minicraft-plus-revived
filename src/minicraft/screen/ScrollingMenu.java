package minicraft.screen;

import java.util.List;
import minicraft.gfx.Screen;

public class ScrollingMenu extends SelectMenu {
	
	protected int dispSelected;
	protected int dispSize;
	
	private int padding;
	
	protected int offset; // since not all elements are displayed at once, this determines which item is at the top.
	
	private ScrollingMenu(List<String> options, int displayLength, int x, int y, boolean centered, int spacing, int colSel, int colNoSel) {
		super(options, x, y, centered, spacing, colSel, colNoSel);
		dispSize = Math.min(displayLength, options.size());
		
		padding = (dispSize+1)/2; // I may or may not choose to make this editable.
	}
	public ScrollingMenu(List<String> options, int displayLength, int x, int y, int spacing, int colSel, int colNoSel) {
		this(options, displayLength, x, y, false, spacing, colSel, colNoSel);
	}
	public ScrollingMenu(List<String> options, int displayLength, int y, int spacing, int colSel, int colNoSel) {
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
		if(dispSelected > dispSize-padding && offset+dispSize < options.size()) dispSelected = dispSize-padding; // if the cursor is below halfway, and we have space to scroll down, then move the cursor back to the middle.
		
		// when there are no more items to scroll, offset+dispSize == options.size()
	}
	
	public void render(Screen screen) {
		if(options.size() == 0) return;
		
		if(offset + dispSize > options.size())
			offset = Math.max(0, options.size() - dispSize);
		if(offset < 0)
			offset = 0;
		
		super.renderAs(screen, options.subList(offset, Math.min(options.size(), offset + dispSize)), dispSelected); // renders the list with the super classes parameters, but temp. replacing the array and index.
	}
}
