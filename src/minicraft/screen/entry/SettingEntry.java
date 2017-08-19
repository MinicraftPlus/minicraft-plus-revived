package minicraft.screen.entry;

public class SettingEntry extends ArrayEntry<String> {
	
	private String[] choices;
	
	public SettingEntry(String label, String... choices) {
		super(label, choices.length);
		this.choices = choices;
	}
	
	@Override
	public String getValue() {
		return choices[getIndex()];
	}
	
	@Override
	public void setValue(String value) {
		for(int i = 0; i < choices.length; i++) {
			if(value.equalsIgnoreCase(choices[i])) {
				setIndex(i);
				return;
			}
		}
		
		throw new java.lang.IllegalArgumentException();
	}
}
