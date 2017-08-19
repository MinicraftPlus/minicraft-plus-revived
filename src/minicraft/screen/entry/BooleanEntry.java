package minicraft.screen.entry;

import minicraft.gfx.FontStyle;
import minicraft.gfx.Screen;

/// This represents an option that just holds a boolean value. Index 0 is false, and 1 is true.
/// "== true" and "== false" are used for clarity.
public class BooleanEntry extends ArrayEntry<Boolean> {
	
	public BooleanEntry(String label) {
		this(label, false);
	}
	public BooleanEntry(String label, boolean defaultValue) {
		super(label, 2, (defaultValue == false ? 0 : 1));
	}
	
	@Override
	public void render(Screen screen, FontStyle style) {
		style.draw(getLabel() + ": " + (getValue() == true ? "On" : "Off"), screen);
	}
	
	public Boolean getValue() { return getIndex() == 0 ? false: true; }
	
	@Override
	public void setValue(Boolean value) {
		setIndex(value == false ? 0 : 1);
	}
}
