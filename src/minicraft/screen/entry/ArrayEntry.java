package minicraft.screen.entry;

import java.util.Arrays;

import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.core.io.Sound;
import minicraft.gfx.Font;

public class ArrayEntry<T> extends ListEntry {
	
	private String label;
	private T[] options;
	private boolean[] optionVis;
	
	private int selection;
	private boolean wrap;
	private boolean localize;
	
	private int maxWidth;
	
	private ChangeListener changeAction;
	
	@SafeVarargs
	public ArrayEntry(String label, T... options) { this(label, true, true, options); }
	
	@SafeVarargs
	public ArrayEntry(String label, boolean wrap, T... options) { this(label, wrap, true, options); }
	
	@SafeVarargs
	public ArrayEntry(String label, boolean wrap, boolean localize, T... options) {
		this.label = label;
		this.options = options;
		this.wrap = wrap;
		this.localize = localize;
		
		maxWidth = 0;
		for(T option: options)
			maxWidth = Math.max(maxWidth, Font.textWidth(
				localize ? Localization.getLocalized(option.toString()) : option.toString()
			));
		
		optionVis = new boolean[options.length];
		Arrays.fill(optionVis, true);
	}
	
	public void setSelection(int idx) {
		boolean diff = idx != selection;
		if(idx >= 0 && idx < options.length) {
			selection = idx;
			if(diff && changeAction != null)
				changeAction.onChange(getValue());
		}
	}
	
	public void setValue(Object value) {
		setSelection(getIndex(value)); // if it is -1, setSelection simply won't set the value.
	}
	
	protected String getLabel() { return label; }
	
	public int getSelection() { return selection; }
	public T getValue() { return options[selection]; }
	
	public boolean valueIs(Object value) {
		if(value instanceof String && options instanceof String[])
			return ((String)value).equalsIgnoreCase((String)getValue());
		else
			return getValue().equals(value);
	}
	
	
	private int getIndex(Object value) {
		boolean areStrings = value instanceof String && options instanceof String[];
		for(int i = 0; i < options.length; i++) {
			if(areStrings && ((String)value).equalsIgnoreCase((String)options[i]) || options[i].equals(value)) {
				return i;
			}
		}
		
		return -1;
	}
	
	
	public void setValueVisibility(Object value, boolean visible) {
		int idx = getIndex(value);
		if(idx >= 0) {
			optionVis[idx] = visible;
			if(idx == selection && !visible)
				moveSelection(1);
		}
	}
	
	public boolean getValueVisibility(Object value) {
		int idx = getIndex(value);
		if(idx < 0) return false;
		return optionVis[idx];
	}
	
	
	@Override
	public void tick(InputHandler input) {
		int prevSel = selection;
		int selection = this.selection;
		
		if(input.getKey("cursor-left").clicked) selection--;
		if(input.getKey("cursor-right").clicked) selection++;
		
		if(prevSel != selection) {
			Sound.select.play();
			moveSelection(selection - prevSel);
		}
	}
	
	private void moveSelection(int dir) {
		// stuff for changing the selection, including skipping locked entries
		int prevSel = selection;
		int selection = this.selection;
		do {
			selection += dir;
			
			if(wrap) {
				selection = selection % options.length;
				if(selection < 0) selection = options.length - 1;
			} else {
				selection = Math.min(selection, options.length-1);
				selection = Math.max(0, selection);
			}
		} while(!optionVis[selection] && selection != prevSel);
		
		setSelection(selection);
	}
	
	@Override
	public int getWidth() {
		return Font.textWidth(Localization.getLocalized(label)+": ") + maxWidth;
	}
	
	@Override
	public String toString() {
		String str = Localization.getLocalized(label) + ": ";
		String option = options[selection].toString();
		
		str += localize ? Localization.getLocalized(option) : option;
		
		return str;
	}
	
	public void setChangeAction(ChangeListener l) {
		this.changeAction = l;
		if(l != null)
			l.onChange(getValue());
	}
}
