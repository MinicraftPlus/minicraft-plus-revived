package minicraft.screen;

import minicraft.InputHandler;
import minicraft.gfx.*;
import minicraft.saveload.Save;
import minicraft.screen.entry.StringEntry;

public class KeyInputMenu extends ScrollingMenu {
	
	private boolean listeningForBind, confirmReset;
	
	private String[] actionKeys;
	
	private static final FontStyle style = new FontStyle().setYPos(Font.textHeight()*2).setXPos(0);
	
	private static Frame inputFrame = new Frame("", new Rectangle(4, 4, Screen.w/SpriteSheet.boxWidth-4, Screen.h/SpriteSheet.boxWidth-4));
	
	public KeyInputMenu(String[] prefs) {
		super(StringEntry.useStringArray(prefs), (Screen.h-Font.textHeight()*9)/8);
		setLineSpacing(1);
		setTextStyle(style);
		
		//this.parent = parent;
		listeningForBind = false;
		confirmReset = false;;
		actionKeys = new String[prefs.length];
		updateKeys(prefs);
	}
	
	public void tick() {
		if(listeningForBind) {
			if(input.keyToChange == null) {
				// the key has just been set
				//System.out.println("binding changed at " + input.ticks);
				listeningForBind = false;
				updateKeys(input.getKeyPrefs());
			}
			return;
		}
		
		if(input.getKey("exit").clicked && !confirmReset) {
			game.setMenu(parent);
			new Save(game);
			return;
		}
		
		super.tick();
		
		if(confirmReset) {
			if(input.getKey("exit").clicked) {
				confirmReset = false;
			}
			else if(input.getKey("select").clicked) {
				confirmReset = false;
				input.resetKeyBindings();
				updateKeys(input.getKeyPrefs());
			}
		}
		else if(input.getKey("c").clicked || input.getKey("enter").clicked) {
			//System.out.println("changing input binding at " + input.ticks);
			input.changeKeyBinding(actionKeys[selected]);
			listeningForBind = true;
		}
		else if(input.getKey("a").clicked) {
			// add a binding, don't remove previous.
			input.addKeyBinding(actionKeys[selected]);
			listeningForBind = true;
		}
		else if(input.getKey("shift-d").clicked && !confirmReset) {
			confirmReset = true;
		}
	}
	
	private void updateKeys(String[] keys) {
		for(int i = 0; i < keys.length; i++) {
			String key = keys[i];
			
			String action = key.substring(0, key.indexOf(";"));
			String mapping = key.substring(key.indexOf(";")+1);
			
			StringBuilder buffer = new StringBuilder();
			for(int spaces = 0; spaces < Screen.w/Font.textWidth(" ") - action.length() - mapping.length(); spaces++) {
				buffer.append(" ");
			}
			
			actionKeys[i] = action;
			options[i] = new StringEntry(action + buffer.toString() + mapping);
		}
	}
	
	public void render(Screen screen) {
		screen.clear(0);
		
		Font.drawCentered("Controls", screen, 0, Color.get(-1, 555));
		
		/*if(Game.debug&&false) {
			System.out.println("current status:");
			System.out.println("selected: " + selected + " of " + options.size());
			System.out.println("disp sel: " + dispSelected + " of " + dispSize);
			System.out.println("offset: " + offset);
			System.out.println("CONTENTS:");
			for(String str: options)
				System.out.println(str);
		}*/
		
		super.render(screen);
		
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
			Font.drawParagraph(input.getMapping("select")+" to confirm\n"+input.getMapping("exit")+" to cancel", screen, 8, true, (Screen.h-Font.textHeight()) / 2 + 8*3, false, style, 4);
		} else {
			String[] lines = {
				"Press C/Enter to change key binding",
				"Press A to add key binding",
				"Shift-D to reset all keys to default",
				input.getMapping("exit")+" to Return to menu"
			};
			for(int i = 0; i < lines.length; i++)
				Font.drawCentered(lines[i], screen, Screen.h-Font.textHeight()*(4-i), Color.get(-1, 555));
		}
	}
}
