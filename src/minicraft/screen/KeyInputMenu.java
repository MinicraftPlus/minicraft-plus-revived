package minicraft.screen;

import java.util.Arrays;
import minicraft.Game;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.saveload.Save;

public class KeyInputMenu extends ScrollingMenu {
	
	private boolean listeningForBind, confirmReset;
	private Menu parent;
	
	private String[] actionKeys;
	
	public KeyInputMenu(Menu parent) {
		super(Arrays.asList(parent.input.getKeyPrefs()), (Game.HEIGHT-Font.textHeight()*9)/8, 0, Font.textHeight()*2, 1, Color.get(-1, 555), Color.get(-1, 333));
		
		this.parent = parent;
		listeningForBind = false;
		confirmReset = false;
		String[] keys = options.toArray(new String[0]);
		actionKeys = new String[keys.length];
		updateKeys(keys);
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
		
		if(input.getKey("exit").clicked) {
			game.setMenu(parent);
			new Save(game);
			return;
		}
		
		super.tick();
		
		if(input.getKey("c").clicked || input.getKey("enter").clicked) {
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
		else if(input.getKey("select").clicked && confirmReset) {
			confirmReset = false;
			input.resetKeyBindings();
			updateKeys(input.getKeyPrefs());
		}
	}
	
	private void updateKeys(String[] keys) {
		for(int i = 0; i < keys.length; i++) {
			String key = keys[i];
			
			String action = key.substring(0, key.indexOf(";"));
			String mapping = key.substring(key.indexOf(";")+1);
			
			String buffer = "";
			for(int spaces = 0; spaces < Game.WIDTH/8 - action.length() - mapping.length(); spaces++) {
				buffer += " ";
			}
			
			actionKeys[i] = action;
			options.set(i, action+buffer+mapping);
		}
	}
	
	public void render(Screen screen) {
		screen.clear(0);
		
		Font.drawCentered("Controls", screen, 0, Color.get(-1, 555));
		
		if(Game.debug&&false) {
			System.out.println("current status:");
			System.out.println("selected: " + selected + " of " + options.size());
			System.out.println("disp sel: " + dispSelected + " of " + dispSize);
			System.out.println("offset: " + offset);
			System.out.println("CONTENTS:");
			for(String str: options)
				System.out.println(str);
		}
		
		super.render(screen);
		
		if(listeningForBind) {
			renderFrame(screen, "", 4, 4, screen.w/8-4, screen.h/8-4);
			Font.drawCentered("Press the desired", screen, (screen.h-Font.textHeight()) / 2 - 4, Color.get(-1, 450));
			Font.drawCentered("key sequence", screen, (screen.h-Font.textHeight()) / 2 + 4, Color.get(-1, 450));
		} else if (confirmReset) {
			renderFrame(screen, "Confirm Action", 4, 4, screen.w/8-4, screen.h/8-4);
			Font.drawParagraph("Are you sure you want to reset all key bindings to the default keys?", screen, 8*4, screen.h/2 - 8*4, true, 4, Color.get(-1, 511));
			Font.drawParagraph(input.getMapping("select")+" to confirm\n"+input.getMapping("exit")+" to cancel", screen, 8, screen.h/2 + 24, true, 4, Color.get(-1, 533));
		} else {
			String[] lines = {
				"Press C/Enter to change key binding",
				"Press A to add key binding",
				"Shift-D to reset all keys to default",
				input.getMapping("exit")+" to Return to menu"
			};
			for(int i = 0; i < lines.length; i++)
				Font.drawCentered(lines[i], screen, screen.h-Font.textHeight()*(4-i), Color.get(-1, 555));
		}
	}
}
