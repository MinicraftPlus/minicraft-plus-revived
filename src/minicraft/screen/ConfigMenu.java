package minicraft.screen;

import minicraft.gfx.FontStyle;
import minicraft.gfx.Screen;
import minicraft.screen.entry.ConfigEntry;
import minicraft.screen.entry.ListEntry;

import java.util.HashSet;
import java.util.LinkedHashMap;

/// this class exists so that it is possible to retain a named reference to each of the options.
public class ConfigMenu extends ScrollingMenu {
	
	private LinkedHashMap<String, ConfigEntry> settings;
	//private HashSet<ConfigEntry> hiddenEntries = new HashSet<ConfigEntry>();
	
	protected ConfigMenu(LinkedHashMap<String, ConfigEntry> settings) {
		super(settings.values().toArray(new ConfigEntry[settings.size()]));
		
		this.settings = settings;
	}
	
	public ConfigEntry getEntry(String key) {
		return settings.get(key);
	}
	
	/*
	@Override
	protected void onSelectionChange(int oldSel, int newSel) {
		/// It can be taken as a given that newSel != oldSel.
		int selDir = 0;
		if(input.getKey("left").clicked)
			selDir = -1;
		else if(input.getKey("right").clicked)
			selDir = 1;
		
		if(selDir == 0) {
			System.out.println("selection dir not tracked; can't skip hidden entries in menu.");
			super.onSelectionChange(oldSel, newSel);
			return;
		}
		
		/// Menus wrap by default.
		
		while(hiddenEntries.contains(options[newSel]) && newSel != oldSel) {
			newSel += selDir;
			
			
		}
		
		do { // this could loop multiple times, due to encountering a hidden value, and each time it will perform the same cursor movement as the last.
			if (input.getKey("right").clicked && (wrap || newSel < settings.size()))
				newSel++;
			if (input.getKey("left").clicked && (wrap || newSel > 0))
				newSel--;
			
			
			if((newSel >= settings.size() - 1 || newSel <= 0) && hiddenEntries.contains(options[newSel]))
				newSel = oldSel; // if the index is at either limit, and the value is hidden, then revert back to the original value, because everything from the original to here has been hidden. 
			
		}
	}
	
	public void hideEntry(String entry) {
		hiddenEntries.add(getEntry(entry));
	}
	public void showEntry(String entry) { hiddenEntries.remove(getEntry(entry)); }
	public void showAllEntries() { hiddenEntries.clear(); }
	*/
}
