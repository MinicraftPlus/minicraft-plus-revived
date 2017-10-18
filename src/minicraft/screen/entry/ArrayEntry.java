package minicraft.screen.entry;

import java.util.Arrays;

import minicraft.InputHandler;
import minicraft.Sound;
import minicraft.gfx.Font;

public class ArrayEntry<T> extends ListEntry {
	
	/// TO-DO the locking mechanism will be implemented later, when I've verified that the rest works. They will still be selectable, but they'll just be a different color..? Idk about that, actually. Well, maybe they will just not be saved if they end on an invalid value.
	
	//private static final int LOCKED_COLOR = Color.get(200);
	
	private String label;
	private T[] options;
	private boolean[] optionVis;
	
	private int selection;
	private boolean wrap;
	
	private int maxWidth;
	//private ArrayList<Integer> lockedOptions = new ArrayList<>();
	
	private ChangeListener changeAction;
	
	@SafeVarargs
	public ArrayEntry(String label, T... options) {
		this(label, true, options);
	}
	
	@SafeVarargs
	public ArrayEntry(String label, boolean wrap, T... options) {
		this.label = label;
		this.options = options;
		this.wrap = wrap;
		
		maxWidth = 0;
		for(T option: options)
			maxWidth = Math.max(maxWidth, Font.textWidth(option.toString()));
		
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
	
	public String getLabel() { return label; }
	
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
	
	
	@Override
	public void tick(InputHandler input) {
		int prevSel = selection;
		int selection = this.selection;
		
		if(input.getKey("left").clicked) selection--;
		if(input.getKey("right").clicked) selection++;
		
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
		return Font.textWidth(label+": ") + maxWidth;
	}
	
	@Override
	public String toString() {
		return label + ": " + options[selection];
	}
	
	public void setChangeAction(ChangeListener l) {
		this.changeAction = l;
		if(l != null)
			l.onChange(getValue());
	}
}
