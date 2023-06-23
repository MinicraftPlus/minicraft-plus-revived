package minicraft.screen.entry;

import minicraft.util.MyUtils;

public class RangeEntry extends ArrayEntry<Integer> {

	private static Integer[] getIntegerArray(int min, int max, int interval) {
		Integer[] ints = new Integer[(max - min) / interval + 1];

		for (int i = 0; i < ints.length; i++)
			ints[i] = min + i*interval;

		return ints;
	}

	private final int min, max;
	private final int interval;

	@SuppressWarnings("unused") // Reserved for future use
	public RangeEntry(String label, int min, int max, int initial) { this(label, min, max, initial, 1); }
	public RangeEntry(String label, int min, int max, int initial, int interval) {
		super(label, false, getIntegerArray(min, max, interval));

		this.min = min;
		this.max = max;

		this.interval = interval;

		setValue(initial);
	}

	@Override
	public void setValue(Object o) {
		if (!(o instanceof Integer)) return;

		setSelection(MyUtils.clamp((((Integer)o)-min) / interval, min, max));
	}
}
