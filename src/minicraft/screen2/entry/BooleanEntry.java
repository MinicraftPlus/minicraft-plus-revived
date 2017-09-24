package minicraft.screen2.entry;

public class BooleanEntry extends ArrayEntry<Boolean> {
	
	public BooleanEntry(String label, boolean initial) {
		super(label, new Boolean[] {true, false}, true);
		
		setSelection(initial ? 0 : 1);
	}
}
