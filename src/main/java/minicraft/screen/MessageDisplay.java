package minicraft.screen;

import minicraft.screen.entry.StringEntry;

public class MessageDisplay extends Display {
	public MessageDisplay(String[] msg) {
		menus = new Menu[] {
			new Menu.Builder(true, 5, RelPos.CENTER).setEntries(StringEntry.useLines(msg)).createMenu()
		};
	}
}
