package minicraft.screen;

import java.util.Arrays;
import minicraft.Game;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.saveload.Save;

public class KeyInputMenu extends ScrollingMenu {
	
	private boolean listeningForBind;
	private Menu parent;
	
	private String[] actionKeys;
	
	public KeyInputMenu(Menu parent) {
		super(Arrays.asList(parent.input.getKeyPrefs()), (Game.HEIGHT-Font.textHeight()*9)/8, 0, Font.textHeight()*3, 1, Color.get(0, 555), Color.get(0, 333));
		
		this.parent = parent;
		listeningForBind = false;
		String[] keys = options.toArray(new String[0]);
		actionKeys = new String[keys.length];
		updateKeys(keys);
	}
	
	public void tick() {
		if(listeningForBind && input.keyToChange == null) {
			// the key has just been set
			//System.out.println("binding changed at " + input.ticks);
			listeningForBind = false;
			updateKeys(input.getKeyPrefs());
			return;
		}
		
		if(input.getKey("exit").clicked) {
			game.setMenu(parent);
			new Save(game);
			return;
		}
		
		super.tick();
		
		if(input.getKey("select").clicked) {
			//System.out.println("changing input binding at " + input.ticks);
			input.changeKeyBinding(actionKeys[selected]);
			listeningForBind = true;
		}
		else if(input.getKey("a").clicked) {
			// add a binding, don't remove previous.
			input.addKeyBinding(actionKeys[selected]);
			listeningForBind = true;
		}
	}
	
	private void updateKeys(String[] keys) {
		//options = new ArrayList<String>(keys.length);
		//actionKeys = new String[keys.length];
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
		
		Font.drawCentered("Controls", screen, 0, Color.get(0, 555));
		
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
		} else {
			Font.drawCentered("Press Enter to change key binding", screen, screen.h-Font.textHeight()*3, Color.get(0, 555));
			Font.drawCentered("Press A to add key binding", screen, screen.h-Font.textHeight()*2, Color.get(0, 555));
			Font.drawCentered("Esc to Return to menu", screen, screen.h-Font.textHeight()*1, Color.get(0, 555));
		}
	}
}
