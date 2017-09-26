package minicraft.screen2.entry;

import minicraft.InputHandler;
import minicraft.Sound;
import minicraft.gfx.Font;
import minicraft.screen2.Menu;

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
	
	void setSelection(int idx) {
		if(idx >= 0 && idx < options.length)
			selection = idx;
	}
	
	public T getSelected() { return options[selection]; }
	
	@Override
	public void tick(InputHandler input, Menu menu) {
		int prevSel = selection;
		
		if(input.getKey("left").clicked) selection--;
		if(input.getKey("right").clicked) selection++;
		
		if(prevSel != selection) {
			Sound.select.play();
			
			//int diff = selected - prevSel;
			
			if(wrap)
				selection = selection % options.length;
			else {
				selection = Math.min(selection, options.length-1);
				selection = Math.max(0, selection);
			}
			
			// --stuff for locking mechanism, and skipping locked entries--
		}
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
