package minicraft.screen.entry;

import minicraft.InputHandler;
import minicraft.Sound;
import minicraft.gfx.FontStyle;
import minicraft.gfx.Screen;

/**
 * Sort of like a slider; it denotes a range of number values.
 */
public class RangeEntry extends ArrayEntry<Integer> {
	
	private int minVal;
	
	public RangeEntry(String label, int minVal, int maxVal, int startVal) {
		super(label, maxVal-minVal, startVal);
		this.minVal = minVal;
	}
	
	@Override
	public Integer getValue() {
		return getIndex() + minVal;
	}
	
	@Override
	public void setValue(Integer value) {
		setIndex(clamp(value, minVal, minVal + getNumChoices() - 1));
	}
	
	public static int clamp(int val, int min, int max) {
		if(min > max) throw new java.lang.ArithmeticException();
		
		if(val < min) val = min;
		if(val > max) val = max;
		
		return val;
	}
}
