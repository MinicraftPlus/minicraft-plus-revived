package minicraft.screen.entry;

import minicraft.InputHandler;
import minicraft.Sound;
import minicraft.gfx.FontStyle;
import minicraft.gfx.Screen;

/**
 * This is similar to RangeEntry, but instead of a range of numbers, this contains a list of Strings to choose from, each denoting a configurable setting, usually.
 */
public abstract class ArrayEntry<T> extends ListEntry {
	
	private String label;
	private int idx;
	private int numChoices;
	
	private ArrayEntry() {} // to disable having a constructor with no parameters
	
	protected ArrayEntry(String label, int numChoices) {
		this(label, numChoices, 0);
	}
	protected ArrayEntry(String label, int numChoices, int startIdx) {
		this.label = label;
		this.numChoices = numChoices;
		if(numChoices <= 0) throw new java.lang.NegativeArraySizeException();
		idx = startIdx;
	}
	
	@Override
	public void tick(InputHandler input) {
		super.tick(input); // this generally won't be used, though.
		
		int prevIdx = idx;
		
		if(input.getKey("right").clicked && idx < numChoices)
			idx++;
		if(input.getKey("left").clicked && idx > 0)
			idx--;
		
		if(prevIdx != idx)
			Sound.craft.play();
	}
	
	public void render(Screen screen, FontStyle style) {
		style.draw(getLabel() + ": " + getValue(), screen);
	}
	
	public abstract T getValue(); // stores whatever sort of value is meant to be stored in any given instance.
	
	protected String getLabel() { return label; }	
	public int getIndex() { return idx; }
	protected int getNumChoices() { return numChoices; }
	
	public void setIndex(int idx) {
		if(idx < getNumChoices() && idx >= 0)
			this.idx = idx;
		else
			throw new IndexOutOfBoundsException();
	}
	
	public abstract void setValue(T value);
}
