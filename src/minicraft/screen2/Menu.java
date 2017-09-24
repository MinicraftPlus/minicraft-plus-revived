package minicraft.screen2;

import java.awt.Point;

import minicraft.Game;
import minicraft.InputHandler;
import minicraft.Sound;
import minicraft.gfx.Screen;
import minicraft.screen2.entry.ListEntry;

public class Menu {
	
	private static final String exitKey = "exit"; // this key control will always exit the menu
	
	// this may replace the styling fetches from the MenuData instance later
	//private FontStyle style; // the styling of the text (color, centering, etc)
	
	private Menu parent; // the menu that led to this display
	final MenuData menuData;
	private ListEntry[] entries; // fetched from menuData
	
	private int selection;
	
	// menus should not be instantiated directly; instead, an instance of MenuData will instantiate it
	protected Menu(MenuData data) {
		this.menuData = data;
		this.parent = Game.getMenu();
		entries = data.getEntries();
		if(entries.length == 0)
			selection = -1;
		else
			selection = 0;
	}
	
	public Menu getParent() { return parent; }
	int getSelection() { return selection; }
	ListEntry[] getEntries() { return entries; }
	int getNumEntries() { return entries.length; }
	
	public void tick(InputHandler input) {
		if(input.getKey(exitKey).clicked) {
			Game.setMenu(parent);
			return;
		}
		
		if(entries.length > 0) {
			int prevSel = selection;
			
			if (input.getKey("up").clicked) selection--;
			if (input.getKey("down").clicked) selection++;
			
			selection = selection % entries.length;
			
			if (prevSel != selection) {
				Sound.select.play();
				//later: menuData.onSelectionChange(entries[selection]);
			}
		}
		
		menuData.tick(input); // allows handling of own inputs
	}
	
	void updateEntries() {
		entries = menuData.getEntries();
		
		if(entries.length == 0)
			selection = -1;
		else if(selection >= entries.length)
			selection = entries.length - 1;
		else if(selection < 0)
			selection = 0;
	}
	
	
	public void render(Screen screen) {
		menuData.render(screen); // draws frame, any background stuff; timing can be checked in other ways
		
		renderEntries(screen, selection, entries);
	}
	
	void renderEntries(Screen screen, int selection, ListEntry[] entries) {
		Point anchor = menuData.getAnchor();
		boolean centered = menuData.centerEntries();
		int spacing = menuData.getSpacing();
		
		int y = anchor.y;
		
		for(int i = 0; i < entries.length; i++) {
			int x = anchor.x;
			if(centered)
				x -= entries[i].getWidth() / 2;
			
			entries[i].render(screen, x, y, i == selection);
			
			y += entries[i].getHeight() + spacing;
		}
	}
}
