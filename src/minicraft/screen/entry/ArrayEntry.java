package minicraft.screen.entry;

import minicraft.InputHandler;
import minicraft.Sound;
import minicraft.gfx.Font;
import minicraft.screen.Menu;

public class ArrayEntry<T> implements ListEntry {
	
	/// TO-DO the locking mechanism will be implemented later, when I've verified that the rest works. They will still be selectable, but they'll just be a different color..? Idk about that, actually. Well, maybe they will just not be saved if they end on an invalid value.
	
	//private static final int LOCKED_COLOR = Color.get(200);
	
	private String label;
	private T[] options;
	
	private int selection;
	private boolean wrap;
	
	private int maxWidth;
	//private ArrayList<Integer> lockedOptions = new ArrayList<>();
	
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
	}
	
	public void setSelection(int idx) {
		if(idx >= 0 && idx < options.length)
			selection = idx;
	}
	
	public void setValue(Object value) {
		boolean areStrings = value instanceof String && options instanceof String[];
		for(int i = 0; i < options.length; i++) {
			if(areStrings && ((String)value).equalsIgnoreCase((String)options[i]) || options[i].equals(value)) {
				setSelection(i);
				break;
			}
		}
	}
	
	public String getLabel() { return label; }
	
	public int getSelection() { return selection; }
	public T getValue() { return options[selection]; }
	
	public boolean hasValue(Object value) {
		if(value instanceof String && options instanceof String[])
			return ((String)value).equalsIgnoreCase((String)getValue());
		else
			return getValue().equals(value);
	}
	
	@Override
	public void tick(InputHandler input, Menu menu) {
		int prevSel = selection;
		int selection = this.selection;
		
		if(input.getKey("left").clicked) selection--;
		if(input.getKey("right").clicked) selection++;
		
		if(prevSel != selection) {
			Sound.select.play();
			
			//int diff = selected - prevSel;
			
			if(wrap) {
				selection = selection % options.length;
				while(selection < 0) selection += options.length;
			} else {
				selection = Math.min(selection, options.length-1);
				selection = Math.max(0, selection);
			}
			
			// --stuff for locking mechanism, and skipping locked entries--
		}
		
		this.selection = selection;
	}
	
	@Override
	public int getWidth() {
		return Font.textWidth(label+" ") + maxWidth;
	}
	
	@Override
	public String toString() {
		return label + ": " + options[selection];
	}
}
