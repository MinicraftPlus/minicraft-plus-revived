package minicraft.screen.entry;

import minicraft.core.Action;
import minicraft.core.io.ClipboardHandler;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.screen.RelPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class InputEntry extends ListEntry {

	private final @Nullable Localization.LocalizationString prompt;
	private final String regex;
	private final int maxLength;
	private RelPos entryPos;

	private String userInput;

	private ChangeListener listener;

	private final ClipboardHandler clipboardHandler = new ClipboardHandler();

	public InputEntry(@Nullable Localization.LocalizationString prompt) {
		this(prompt, null, 0);
	}

	public InputEntry(@Nullable Localization.LocalizationString prompt, String regex, int maxLen) {
		this(prompt, regex, maxLen, "");
	}

	public InputEntry(@Nullable Localization.LocalizationString prompt, String regex, int maxLen, String initValue) {
		this.prompt = prompt;
		this.regex = regex;
		this.maxLength = maxLen;

		userInput = initValue;
	}

	@Override
	public void tick(InputHandler input) {
		String prev = userInput;
		userInput = input.addKeyTyped(userInput, regex);
		if (!prev.equals(userInput)) {
			if (hook != null)
				hook.act();
			if (listener != null)
				listener.onChange(userInput);
		}
		hook = null; // Once per tick

		if (maxLength > 0 && userInput.length() > maxLength)
			userInput = userInput.substring(0, maxLength); // truncates extra
		if (input.getMappedKey("CTRL-V").isClicked()) {
			userInput += clipboardHandler.getClipboardContents();
		}
		if (!userInput.isEmpty()) {
			if (input.getMappedKey("CTRL-C").isClicked()) {
				clipboardHandler.setClipboardContents(userInput);
			}
			if (input.getMappedKey("CTRL-X").isClicked()) {
				clipboardHandler.setClipboardContents(userInput);
				userInput = "";
			}
		}
	}

	private @Nullable Action hook = null;

	@Override
	public void hook(@NotNull Action callback) {
		this.hook = callback;
	}

	public String getUserInput() {
		return userInput;
	}

	public String toString() {
		return prompt == null ? userInput : Localization.getLocalized("minicraft.display.entry", prompt, userInput);
	}

	@Override
	public void render(Screen screen, @Nullable Screen.RenderingLimitingModel limitingModel, int x, int y, boolean isSelected) {
		Font.draw(limitingModel, toString(), screen, x, y, isValid() ? isSelected ? Color.WHITE : COL_UNSLCT : Color.RED);
	}

	public boolean isValid() {
		return userInput.matches(regex);
	}

	public void setChangeListener(ChangeListener l) {
		listener = l;
	}

	public void addChangeListener(ChangeListener l) {
		if (listener == null) listener = l;
		else {
			ChangeListener orig = listener;
			listener = o -> {
				orig.onChange(o);
				l.onChange(o);
			};
		}
	}
}
