package minicraft.screen.entry.commands;

import minicraft.core.Game;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Sound;
import minicraft.screen.ListItemSelectDisplay;
import minicraft.screen.entry.InputEntry;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SelectableListInputEntry extends InputEntry {
	private final String prompt;
	private final List<String> list;
	private final @Nullable String placeholder;

	public SelectableListInputEntry(String prompt, Collection<String> list) {
		this(prompt, list, null);
	}

	public SelectableListInputEntry(String prompt, Collection<String> list, @Nullable String placeholder) {
		super(prompt);
		ArrayList<String> arrayList = list.stream().map(String::toUpperCase).sorted().collect(Collectors.toCollection(ArrayList::new));
		this.prompt = prompt;
		this.list = Collections.unmodifiableList(arrayList);
		this.placeholder = placeholder;
	}

	@Override
	public boolean isValid() {
		String input = getUserInput();
		if (input.isEmpty()) return placeholder != null; // Default behaviour
		else
			return list.contains(input);
	}

	@Override
	public void tick(InputHandler input) {
		if (input.getMappedKey("SELECT").isClicked()) {
			Sound.play("confirm");
			Game.setDisplay(new ListItemSelectDisplay<>(list, this::setUserInput));
			return;
		}

		super.tick(input);
	}

	@Override
	public String getUserInput() {
		return super.getUserInput().toUpperCase(); // In case list content is all upper-cased.
	}

	@Override
	public String toString() {
		return getUserInput().isEmpty() && placeholder != null ? prompt + ": " + placeholder : super.toString();
	}
}
