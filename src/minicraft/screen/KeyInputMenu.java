package minicraft.screen;

import java.awt.Point;

import minicraft.Game;
import minicraft.InputHandler;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.FontStyle;
import minicraft.gfx.Rectangle;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteSheet;
import minicraft.saveload.Save;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.SelectEntry;

public class KeyInputMenu implements MenuData {
	
	private boolean listeningForBind, confirmReset;
	
	//private String[] actionKeys;
	
	//private static final FontStyle style = new FontStyle().setYPos(Font.textHeight()*2).setXPos(0);
	
	private static Frame inputFrame = new Frame("", new Rectangle(4, 4, Screen.w/SpriteSheet.boxWidth-4, Screen.h/SpriteSheet.boxWidth-4));
	
	private ScrollingMenu menu;
	
	public KeyInputMenu() {
		//setLineSpacing(1);
		//setTextStyle(style);
		
		//this.parent = parent;
		listeningForBind = false;
		confirmReset = false;
		//actionKeys = new String[prefs.length];
		//updateKeys(prefs);
	}
	
	@Override
	public Menu getMenu() {
		if(this.menu == null)
			this.menu = new ScrollingMenu(this, (Screen.h-Font.textHeight()*9)/8, 0, inputFrame);
		
		return this.menu;
	}
	
	@Override
	public ListEntry[] getEntries() {
		String[] prefs = Game.input.getKeyPrefs();
		KeyInputEntry[] entries = new KeyInputEntry[prefs.length];
		
		for (int i = 0; i < entries.length; i++)
			entries[i] = new KeyInputEntry(prefs[i]);
		
		return entries;
	}
	
	@Override
	public void tick(InputHandler input) {
		
		//super.tick();
		
		if(listeningForBind) {
			if(input.keyToChange == null) {
				// the key has just been set
				//System.out.println("binding changed at " + input.ticks);
				listeningForBind = false;
			}
			return;
		}
		
		if(input.getKey("exit").clicked && !confirmReset) {
			new Save();
			//return;
		}
		
		//super.tick(input);
	}
	
	/*private void updateKeys(String[] keys) {
		for(int i = 0; i < keys.length; i++) {
			String key = keys[i];
			
			actionKeys[i] = action;
			
		}
		updateEntries();
	}*/
	
	public void render(Screen screen) {
		screen.clear(0);
		
		Font.drawCentered("Controls", screen, 0, Color.get(-1, 555));
		
		/*if(Game.debug) {
			System.out.println("current status:");
			System.out.println("selected: " + selected + " of " + options.size());
			System.out.println("disp sel: " + dispSelected + " of " + dispSize);
			System.out.println("offset: " + offset);
			System.out.println("CONTENTS:");
			for(String str: options)
				System.out.println(str);
		}*/
		
		//super.render(screen);
		
		if(listeningForBind) {
			inputFrame.setTitle("");
			inputFrame.render(screen);
			Font.drawCentered("Press the desired", screen, (Screen.h-Font.textHeight()) / 2 - 4, Color.get(-1, 450));
			Font.drawCentered("key sequence", screen, (Screen.h-Font.textHeight()) / 2 + 4, Color.get(-1, 450));
		} else if (confirmReset) {
			inputFrame.setTitle("Confirm Action");
			inputFrame.render(screen);
			FontStyle style = new FontStyle(Color.get(-1, 511));
			Font.drawParagraph("Are you sure you want to reset all key bindings to the default keys?", screen, 8*4, true, 8*4, true, style, 4);
			style.setColor(Color.get(-1, 533));
			Font.drawParagraph(Game.input.getMapping("select")+" to confirm\n"+Game.input.getMapping("exit")+" to cancel", screen, 8, true, (Screen.h-Font.textHeight()) / 2 + 8*3, false, style, 4);
		} else {
			String[] lines = {
				"Press C/Enter to change key binding",
				"Press A to add key binding",
				"Shift-D to reset all keys to default",
				Game.input.getMapping("exit")+" to Return to menu"
			};
			for(int i = 0; i < lines.length; i++)
				Font.drawCentered(lines[i], screen, Screen.h-Font.textHeight()*(4-i), Color.get(-1, 555));
		}
	}
	
	@Override
	public boolean centerEntries() {
		return false;
	}
	
	@Override
	public int getSpacing() {
		return 1;
	}
	
	@Override
	public Point getAnchor() {
		return new Point();
	}
	
	class KeyInputEntry extends SelectEntry {
		
		private String action, mapping, buffer;
		
		public KeyInputEntry(String key) {
			super("", null);
			
			this.action = key.substring(0, key.indexOf(";"));
			this.mapping = key.substring(key.indexOf(";")+1);
			
			StringBuilder buffer = new StringBuilder();
			for(int spaces = 0; spaces < Screen.w/Font.textWidth(" ") - action.length() - mapping.length(); spaces++) {
				buffer.append(" ");
			}
			
			this.buffer = buffer.toString();
		}
		
		@Override
		public void tick(InputHandler input, Menu menu) {
			//if(input.getKey("select").clicked)
			if(confirmReset) {
				if(input.getKey("exit").clicked) {
					confirmReset = false;
				}
				else if(input.getKey("select").clicked) {
					confirmReset = false;
					input.resetKeyBindings();
				}
			}
			else if(input.getKey("c").clicked || input.getKey("enter").clicked) {
				//System.out.println("changing input binding at " + input.ticks);
				input.changeKeyBinding(action);
				listeningForBind = true;
			}
			else if(input.getKey("a").clicked) {
				// add a binding, don't remove previous.
				input.addKeyBinding(action);
				listeningForBind = true;
			}
			else if(input.getKey("shift-d").clicked && !confirmReset) {
				confirmReset = true;
			}
		}
		
		@Override
		public String toString() {
			return action + buffer + mapping;
		}
	} 
}
