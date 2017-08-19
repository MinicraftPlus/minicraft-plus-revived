package minicraft.screen.entry;

/**
 * Sort of like a slider; it denotes a range of number values.
 */
public class RangeEntry extends ArrayEntry<Integer> {
	
	private int minVal, increment;
	
	public RangeEntry(String label, int minVal, int maxVal, int startVal) {
		this(label, minVal, maxVal, startVal, 1);
	}
	public RangeEntry(String label, int minVal, int maxVal, int startVal, int inc) {
		super(label, maxVal-minVal - ((maxVal-minVal)%inc), startVal);
		this.minVal = minVal;
		this.increment = inc;
	}
	
	@Override
	public Integer getValue() {
		return increment * getIndex() + minVal;
	}
	
	@Override
	public void setValue(Integer value) {
		setIndex(clamp(value - ((value-minVal) % increment), minVal, minVal + increment * (getNumChoices() - 1)));
	}
	
	public static int clamp(int val, int min, int max) {
		if(min > max) throw new java.lang.ArithmeticException();
		
		if(val < min) val = min;
		if(val > max) val = max;
		
		return val;
	}
}
