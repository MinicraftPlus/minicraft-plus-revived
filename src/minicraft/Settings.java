package minicraft;

import java.util.HashMap;

import minicraft.screen2.entry.ArrayEntry;
import minicraft.screen2.entry.BooleanEntry;

public class Settings {
	
	private static HashMap<String, ArrayEntry> options = new HashMap<>();
	
	static {
		options.put("diff", new ArrayEntry<>("Difficulty", "Easy", "Normal", "Hard"));
		options.put("mode", new ArrayEntry<>("Game Mode", "Survival", "Creative", "Hardcore", "Score"));
		options.put("scoretime", new ArrayEntry<>("Time (Score Mode)", /*10, */20, 40, 60/*, 120*/));
		
		options.put("sound", new BooleanEntry("Sound", true));
		options.put("autosave", new BooleanEntry("Autosave", true));
		
		options.put("size", new ArrayEntry<>("World Size", 128, 256, 512));
		options.put("theme", new ArrayEntry<>("World Theme", "Normal", "Forest", "Desert", "Plain", "Hell"));
		options.put("type", new ArrayEntry<>("Terrain Type", "Island", "Box", "Mountain", "Irregular"));
		
		options.put("unlockedskin", new BooleanEntry("Wear Suit", false));
		options.put("skinon", new BooleanEntry("Wear Suit", false));
	}
	
	// returns the value of the specified option
	public static Object get(String option) { return options.get(option.toLowerCase()).getValue(); }
	
	// returns the index of the value in the list of values for the specified option
	public static int getIdx(String option) { return options.get(option.toLowerCase()).getSelection(); }
	
	// return the ArrayEntry object associated with the given option name.
	public static ArrayEntry getEntry(String option) { return options.get(option.toLowerCase()); }
	
	// checks if the given option is set to the specified value
	public static boolean hasValue(String option, Object value) {
		return options.get(option.toLowerCase()).hasValue(value);
	}
	
	// sets the value of the given option name, to the given value, provided it is a valid value for that option.
	public static void set(String option, Object value) {
		options.get(option.toLowerCase()).setValue(value);
	}
	
	// sets the index of the value of the given option, provided it is a valid index
	public static void setIdx(String option, int idx) {
		options.get(option.toLowerCase()).setSelection(idx);
	}
}
