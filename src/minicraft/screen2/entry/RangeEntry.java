package minicraft.screen2.entry;

public class RangeEntry extends ArrayEntry<Integer> {
	
	private static Integer[] getIntegerList(int min, int max) {
		Integer[] ints = new Integer[max-min+1];
		
		for(int i = 0; i < ints.length; i++)
			ints[i] = min+i;
		
		return ints;
	}
	
	public RangeEntry(String label, int min, int max, int initial) {
		super(label, false, getIntegerList(min, max));
		
		setSelection(initial-min);
	}
}
