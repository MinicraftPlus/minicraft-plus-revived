package minicraft.screen.entry;

import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;

import java.util.Set;

public class KeyInputEntry extends SelectEntry {

	private String action, mapping, buffer;

	public KeyInputEntry(String key, Set<String> duplicated) {
		super("", null);

		this.action = key.substring(0, key.indexOf(";"));
		setMapping(key.substring(key.indexOf(";") + 1), duplicated);
	}

	private void setMapping(String mapping, Set<String> duplicated) {
		this.mapping = mapping;

		StringBuilder buffer = new StringBuilder();
		for (int spaces = 0; spaces < Screen.w / Font.textWidth(" ") - action.length() - mapping.length(); spaces++)
			buffer.append(" ");

		String newMapping = "";
		for (String k : mapping.split("\\|")) {
			if (duplicated.contains(k)) k = Color.RED_CODE + k;
			k = Color.GRAY_CODE + k + Color.WHITE_CODE;
			newMapping += k + "|";
		}

		this.mapping = newMapping.substring(0, newMapping.length() - 1);
		this.buffer = buffer.toString();
	}

	@Override
	public void tick(InputHandler input) {
		if (input.getMappedKey("c").isClicked() || input.getMappedKey("enter").isClicked())
			input.changeKeyBinding(action);
		else if (input.getMappedKey("a").isClicked())
			// Add a binding, don't remove previous.
			input.addKeyBinding(action);
	}

	@Override
	public int getWidth() {
		return Screen.w;
	}

	@Override
	public String toString() {
		return Localization.getLocalized(action) + buffer + mapping;
	}
}
