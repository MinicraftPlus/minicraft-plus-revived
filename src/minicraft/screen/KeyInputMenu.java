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
	
	private static Frame inputFrame = new Frame("", new Rectangle(4, 4, Screen.w/SpriteSheet.boxWidth-8, Screen.h/SpriteSheet.boxWidth-8));
	
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
		if(listeningForBind) {
			if(input.keyToChange == null) {
				// the key has just been set
				listeningForBind = false;
				menu.updateEntries();
			}
		}
	}
	
	@Override
	public void onExit() {
		new Save();
	}
	
	public void render(Screen screen) {
		screen.clear(0);
		
		Font.drawCentered("Controls", screen, 0, Color.WHITE);
		
		/*if(listeningForBind) {
			
		} else if (confirmReset) {
			
		} */if(!listeningForBind && !confirmReset) {
			String[] lines = {
				"Press C/Enter to change key binding",
				"Press A to add key binding",
				"Shift-D to reset all keys to default",
				Game.input.getMapping("exit")+" to Return to menu"
			};
			for(int i = 0; i < lines.length; i++)
				Font.drawCentered(lines[i], screen, Screen.h-Font.textHeight()*(4-i), Color.WHITE);
		}
	}
	
	@Override
	public void renderPopup(Screen screen) {
		if(listeningForBind) {
			inputFrame.setTitle("");
			inputFrame.render(screen);
			Font.drawCentered("Press the desired", screen, (Screen.h-Font.textHeight()) / 2 - 4, Color.get(-1, 450));
			Font.drawCentered("key sequence", screen, (Screen.h-Font.textHeight()) / 2 + 4, Color.get(-1, 450));
		}
		else if(confirmReset) {
			inputFrame.setTitle("Confirm Action");
			inputFrame.render(screen);
			
			FontStyle style = new FontStyle(Color.get(-1, 511));
			Font.drawParagraph("Are you sure you want to reset all key bindings to the default keys?", screen, 8*4, true, 8*4, true, style, 4);
			style.setColor(Color.get(-1, 533));
			Font.drawParagraph(Game.input.getMapping("select")+" to confirm\n"+Game.input.getMapping("exit")+" to cancel", screen, 8, true, (Screen.h-Font.textHeight()) / 2 + 8*3, false, style, 4);
		}
	}
	
	@Override
	public Centering getCentering() { return Centering.make(new Point(Game.WIDTH/2, Font.textHeight()), RelPos.BOTTOM, RelPos.CENTER); }
	
	@Override
	public int getSpacing() {
		return 0;
	}
	
	
	class KeyInputEntry extends SelectEntry {
		
		private String action, mapping, buffer;
		
		public KeyInputEntry(String key) {
			super("", null);
			
			this.action = key.substring(0, key.indexOf(";"));
			setMapping(key.substring(key.indexOf(";")+1));
		}
		
		private void setMapping(String mapping) {
			this.mapping = mapping;
			
			StringBuilder buffer = new StringBuilder();
			for(int spaces = 0; spaces < Screen.w/Font.textWidth(" ") - action.length() - mapping.length(); spaces++) {
				buffer.append(" ");
			}
			
			this.buffer = buffer.toString();
		}
		
		@Override
		public void tick(InputHandler input) {
			//if(input.getKey("select").clicked)
			if(confirmReset) {
				if(input.getKey("exit").clicked) {
					confirmReset = false;
				}
				else if(input.getKey("select").clicked) {
					confirmReset = false;
					input.resetKeyBindings();
					menu.updateEntries();
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
		public int getWidth() {
			return Game.WIDTH;
		}
		
		@Override
		public String toString() {
			return action + buffer + mapping;
		}
	} 
}
