package minicraft.screen.entry;

public class SettingEntry<T> extends ArrayEntry<T> {
	
	private T[] choices;
	
	@SafeVarargs // idk what this does, actually...
	public SettingEntry(String label, T... choices) {
		super(label, choices.length);
		this.choices = choices;
	}
	
	@Override
	public T getValue() {
		return choices[getIndex()];
	}
	
	@Override
	public void setValue(T value) {
		for(int i = 0; i < choices.length; i++) {
			if(value.equals(choices[i])) {
				setIndex(i);
				return;
			}
		}
		
		throw new java.lang.IllegalArgumentException();
	}
}
