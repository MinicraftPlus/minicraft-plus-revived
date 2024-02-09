package minicraft.screen.entry;

import minicraft.core.io.InputHandler;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class ControlSettingEntry extends SelectEntry {

	private final String action;
	private String mapping, buffer;

	public ControlSettingEntry(String key, Set<String> duplicated) {
		super(null, null);

		this.action = key.substring(0, key.indexOf(";"));
		setMapping(key.substring(key.indexOf(";") + 1), duplicated);
	}

	private void setMapping(String mapping, Set<String> duplicated) {
		this.mapping = mapping;

		StringBuilder buffer = new StringBuilder();
		for (int spaces = 0; spaces < Screen.w / Font.textWidth(" ") - action.length() - mapping.length(); spaces++)
			buffer.append(" ");

		StringBuilder newMapping = new StringBuilder();
		for (String k : mapping.split("\\|")) {
			if (duplicated.contains(k)) k = Color.RED_CODE + k + Color.RESET_CODE;
			newMapping.append(k).append("|");
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
	public void render(Screen screen, Screen.@Nullable RenderingLimitingModel bounds, int x, int y, boolean isSelected) {
		if (isVisible()) {
			String text = toString();
			Font.drawColor(bounds, isSelected ? text : text.replaceAll(Color.RED_CODE, Color.DIMMED_RED_CODE),
				screen, x, y, getColor(isSelected));
		}
	}

	@Override
	public int getWidth() {
		return Screen.w;
	}

	@Override
	public String toString() {
		return action + buffer + mapping;
	}
}
