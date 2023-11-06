package minicraft.screen;

import minicraft.screen.entry.StringEntry;

public class MessageDisplay extends PopupDisplay {
	public MessageDisplay(String... msg) { this(false, msg); }
	public MessageDisplay(boolean clearScreen, String... msg) {
		super(null, clearScreen, msg);
	}
}
