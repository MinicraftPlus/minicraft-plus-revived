package minicraft.screen.entry;

public class RangeEntry extends ArrayEntry<Integer> {

	private static Integer[] getIntegerArray(int min, int max) {
		Integer[] ints = new Integer[max - min + 1];

		for (int i = 0; i < ints.length; i++)
			ints[i] = min + i;

		return ints;
	}

	private int min, max;

	public RangeEntry(String label, int min, int max, int initial) {
		super(label, false, getIntegerArray(min, max));

		this.min = min;
		this.max = max;

		setValue(initial);
	}

	@Override
	public void setValue(Object o) {
		if (!(o instanceof Integer)) return;

		setSelection(((Integer) o) - min);
	}
}
