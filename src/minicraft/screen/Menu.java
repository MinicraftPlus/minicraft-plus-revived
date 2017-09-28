package minicraft.screen;

import java.awt.Point;

import minicraft.Game;
import minicraft.InputHandler;
import minicraft.Sound;
import minicraft.gfx.Screen;
import minicraft.screen.entry.ListEntry;

public class Menu {
	
	private static final String exitKey = "exit"; // this key control will always exit the menu
	
	// this may replace the styling fetches from the MenuData instance later
	//private FontStyle style; // the styling of the text (color, centering, etc)
	
	private Menu parent; // the menu that led to this display
	private final MenuData menuData;
	private ListEntry[] entries; // fetched from menuData
	private final boolean mutable;
	
	private Frame[] frames;
	
	private int selection;
	
	private int ticks;
	
	// menus should not be instantiated directly; instead, an instance of MenuData will instantiate it
	protected Menu(MenuData data, Frame... frames) {
		this(data, false, frames);
	}
	protected Menu(MenuData data, boolean entriesAreMutable, Frame... frames) {
		this.menuData = data;
		this.mutable = entriesAreMutable;
		if(data instanceof TitleMenu)
			this.parent = this;
		else
			this.parent = Game.getMenu();
		entries = data.getEntries();
		if(entries.length == 0)
			selection = -1;
		else
			selection = 0;
		
		if(frames == null)
			this.frames = new Frame[0];
		else
			this.frames = frames;
	}
	
	Menu setFrameColors(int titleCol, int midCol, int sideCol) {
		if(frames == null) return this;
		for(Frame frame: frames)
			frame.setColors(titleCol, midCol, sideCol);
		
		return this;
	}
	
	public Menu getParent() { return parent; }
	public MenuData getMenuType() { return menuData; }
	
	int getSelection() { return selection; }
	ListEntry[] getEntries() { return entries; }
	int getNumEntries() { return entries.length; }
	
	void setSelectedEntry(ListEntry entry) {
		if(mutable && selectionExists())
			entries[selection] = entry;
	}
	
	private boolean selectionExists() {
		return entries != null && selection >= 0 && getNumEntries() > selection;
	}
	
	public void tick(InputHandler input) {
		ticks++;
		if(parent != this) {
			boolean auto = menuData.autoExitDelay() > 0;
			if (!auto && input.getKey(exitKey).clicked || auto && ticks > menuData.autoExitDelay()) {
				Game.setMenu(parent);
				return;
			}
		}
		
		if(entries.length > 0) {
			int prevSel = selection;
			
			if (input.getKey("up").clicked) selection--;
			if (input.getKey("down").clicked) selection++;
			
			selection = selection % entries.length;
			
			while(selection < 0) selection += entries.length;
			
			if (prevSel != selection) {
				Sound.select.play();
				//later: menuData.onSelectionChange(entries[selection]);
			}
		}
		
		menuData.tick(input); // allows handling of own inputs
		
		if(selectionExists())
			entries[selection].tick(input, this);
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
		if(menuData.clearScreen())
			screen.clear(0);
		
		menuData.render(screen); // draws frame, any background stuff; timing can be checked in other ways
		
		renderEntries(screen, selection, entries);
	}
	
	void renderEntries(Screen screen, int selection, ListEntry[] entries) {
		for(Frame frame: frames)
			frame.render(screen);
		
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
