package minicraft.screen.entry;

import minicraft.InputHandler;
import minicraft.Sound;
import minicraft.gfx.FontStyle;
import minicraft.gfx.Screen;

import java.util.HashSet;

/**
 * This is similar to RangeEntry, but instead of a range of numbers, this contains a list of Strings to choose from, each denoting a configurable setting, usually.
 * 
 * TODO add a feature that allows me to hide certain values. 
 */
public abstract class ArrayEntry<T> extends ConfigEntry<T> {
	
	private String label;
	private int idx;
	private int numChoices;
	private boolean wrap; // this determines whether the list goes back to the beginning when it is done.
	
	private HashSet<T> hiddenValues = new HashSet<T>();
	
	private ArrayEntry() {} // to disable having a constructor with no parameters
	
	protected ArrayEntry(String label, int numChoices) {
		this(label, numChoices, 0, true);
	}
	protected ArrayEntry(String label, int numChoices, int startIdx, boolean wrap) {
		this.label = label;
		this.numChoices = numChoices;
		if(numChoices <= 0) throw new java.lang.NegativeArraySizeException();
		
		this.wrap = wrap;
		idx = startIdx;
	}
	
	@Override
	public void tick(InputHandler input) {
		super.tick(input); // this generally won't be used, though.
		
		int prevIdx = idx; // records the index before changing it.
		
		do { // this could loop multiple times, due to encountering a hidden value, and each time it will perform the same cursor movement as the last.
			if (input.getKey("right").clicked && (wrap || idx < numChoices))
				idx++;
			if (input.getKey("left").clicked && (wrap || idx > 0))
				idx--;
			
			if(wrap) { // should effectively wrap around the index depending on how off it is, no matter how far.
				if(idx < 0) idx = numChoices - (idx % numChoices);
				else idx = idx % numChoices;
			}
			else if((idx >= numChoices - 1 || idx <= 0) && hiddenValues.contains(getValue()))
				idx = prevIdx; // if the index is at either limit, and the value is hidden, then revert back to the original value, because everything from the original to here has been hidden. 
			
		} while(hiddenValues.contains(getValue()) && idx != prevIdx); // this loop will stop either when a non-hidden value is reached, or the index ends up back where it started, which could happen in a number of ways.
		
		
		if(prevIdx != idx)
			Sound.craft.play();
	}
	
	public void render(Screen screen, FontStyle style) {
		style.draw(getLabel() + ": " + getValue(), screen);
	}
	
	@Override
	public abstract void setValue(T value);
	
	@Override
	public abstract T getValue(); // stores whatever sort of value is meant to be stored in any given instance.
	
	public T getValueAtIndex(int idx) {
		int idxSave = this.idx;
		try {
			setIndex(idx);
		} catch(IndexOutOfBoundsException ex) {
			return null;
		}
		
		T value = getValue();
		
		this.idx = idxSave;
		return value;
	}
	
	protected String getLabel() { return label; }	
	protected int getNumChoices() { return numChoices; }
	public int getIndex() { return idx; }
	
	public void setIndex(int idx) {
		if(idx < getNumChoices() && idx >= 0) {
			this.idx = idx;
		} else
			throw new IndexOutOfBoundsException();
	}
	
	public void hideValue(T value) { hiddenValues.add(value); }
	public void showValue(T value) { hiddenValues.remove(value); }
	public void showAllValues() { hiddenValues.clear(); }
}
