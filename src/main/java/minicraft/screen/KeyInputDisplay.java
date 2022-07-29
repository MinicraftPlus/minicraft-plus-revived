package minicraft.screen;

import minicraft.core.Game;
import minicraft.core.io.InputHandler;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Point;
import minicraft.gfx.Screen;
import minicraft.screen.entry.KeyInputEntry;
import minicraft.screen.entry.StringEntry;

public class KeyInputDisplay extends Display {

	private boolean listeningForBind, confirmReset;

	private static Menu.Builder builder;

	private static KeyInputEntry[] getEntries() {
		String[] prefs = Game.input.getKeyPrefs();
		KeyInputEntry[] entries = new KeyInputEntry[prefs.length];

		for (int i = 0; i < entries.length; i++)
			entries[i] = new KeyInputEntry(prefs[i]);

		return entries;
	}

	public KeyInputDisplay() {
		super(true);
		builder = new Menu.Builder(false, 0, RelPos.CENTER, getEntries())
			.setTitle("minicraft.display.key_input.title_controls")
			.setPositioning(new Point(Screen.w/2, Screen.h - Font.textHeight()*5), RelPos.TOP);

		Menu.Builder popupBuilder = new Menu.Builder(true, 4, RelPos.CENTER)
			.setShouldRender(false)
			.setSelectable(false);

		menus = new Menu[] {
			builder.createMenu(),

			popupBuilder
				.setEntries(StringEntry.useLines(Color.YELLOW, "minicraft.display.key_input.popup.press_key_sequence"))
				.createMenu(),

			popupBuilder
				.setEntries(StringEntry.useLines(Color.RED, "minicraft.display.key_input.confirm_popup", "minicraft.display.popup.enter_confirm", "minicraft.display.popup.escape_cancel"))
				.setTitle("minicraft.display.popup.title_confirm")
				.createMenu()
		};

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
					.createMenu();
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

	public void render(Screen screen) {
		if(selection == 0) // not necessary to put in if statement now, but it's probably more efficient anyway
			screen.clear(0);

		super.render(screen);

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
	}
}
