package minicraft.screen;

import minicraft.Game;
import minicraft.InputHandler;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Point;
import minicraft.gfx.Screen;
import minicraft.saveload.Save;
import minicraft.screen.entry.KeyInputEntry;
import minicraft.screen.entry.StringEntry;

public class KeyInputMenu extends Display {
	
	private boolean listeningForBind, confirmReset;
	
	//private static final FontStyle style = new FontStyle().setYPos(Font.textHeight()*2).setXPos(0);
	
	//private static Frame inputFrame = new Frame("", new Rectangle(4, 4, Screen.w/SpriteSheet.boxWidth-8,Screen.h/SpriteSheet.boxWidth-8));
	
	private static Menu.Builder builder;
	
	private static KeyInputEntry[] getEntries() {
		String[] prefs = Game.input.getKeyPrefs();
		KeyInputEntry[] entries = new KeyInputEntry[prefs.length];
		if(Game.debug) System.out.println("making entries...");
		for (int i = 0; i < entries.length; i++)
			entries[i] = new KeyInputEntry(prefs[i]);
		if(Game.debug) System.out.println("made entries");
		return entries;
	}
	
	public KeyInputMenu() {
		builder = new Menu.Builder(false, 0, RelPos.CENTER, getEntries())
			//.setSize(Screen.w, Screen.h)
			.setTitle("Controls")
			.setScrollPolicies(0.2f, false)
			.setPositioning(new Point(Game.WIDTH/2, Screen.h - Font.textHeight()*5), RelPos.TOP);
		
		Menu.Builder popupBuilder = new Menu.Builder(true, 4, RelPos.CENTER)
			.setShouldRender(false)
			.setSelectable(false);
		
		if(Game.debug) System.out.println("making menus");
		
		menus = new Menu[] {
			builder.createMenu(),
			
			popupBuilder
				.setEntries(StringEntry.useLines(Color.YELLOW, "Press the desired", "key sequence"))
				.createMenu(),
			
			popupBuilder
				.setEntries(StringEntry.useLines(Color.RED, "Are you sure you want to reset all key bindings to the default keys?", "enter to confirm", "escape to cancel"))
				.setTitle("Confirm Action")
				.createMenu()
		};
		
		if(Game.debug) System.out.println("menus made");
		
		listeningForBind = false;
		confirmReset = false;
	}
	
	@Override
	public void tick(InputHandler input) {
		if(listeningForBind) {
			if(input.keyToChange == null) {
				// the key has just been set
				listeningForBind = false;
				menus[1].shouldRender = false;
				menus[0].updateSelectedEntry(new KeyInputEntry(input.getChangedKey()));
				selection = 0;
			}
			
			return;
		}
		
		if(confirmReset) {
			if(input.getKey("exit").clicked) {
				confirmReset = false;
				menus[2].shouldRender = false;
				selection = 0;
			}
			else if(input.getKey("select").clicked) {
				confirmReset = false;
				input.resetKeyBindings();
				menus[2].shouldRender = false;
				menus[0] = builder.setEntries(getEntries())
					.setSelection(menus[0].getSelection(), menus[0].getDispSelection())
					.createMenu()
				;
				selection = 0;
			}
			
			return;
		}
		
		super.tick(input); // ticks menu
		
		if(input.keyToChange != null) {
			listeningForBind = true;
			selection = 1;
			menus[selection].shouldRender = true;
		} else if(input.getKey("shift-d").clicked && !confirmReset) {
			confirmReset = true;
			selection = 2;
			menus[selection].shouldRender = true;
		}
	}
	
	@Override
	public void onExit() {
		if(Game.debug) System.out.println("saving keys...");
		new Save();
	}
	
	public void render(Screen screen) {
		if(selection == 0) // not necessary to put in if statement now, but it's probably more efficient anyway
			screen.clear(0);
		
		super.render(screen);
		
		//Font.drawCentered("Controls", screen, 0, Color.WHITE);
		
		if(!listeningForBind && !confirmReset) {
			String[] lines = {
				"Press C/Enter to change key binding",
				"Press A to add key binding",
				"Shift-D to reset all keys to default",
				Game.input.getMapping("exit")+" to Return to menu"
			};
			for(int i = 0; i < lines.length; i++)
				Font.drawCentered(lines[i], screen, Screen.h-Font.textHeight()*(4-i), Color.WHITE);
		}
		
		/*if(listeningForBind) {
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
			Font.drawParagraph("enter to confirm\nescape to cancel", screen, 8, true, (Screen.h-Font.textHeight()) / 2 + 8*3, false, style, 4);
		}*/
	}
}
