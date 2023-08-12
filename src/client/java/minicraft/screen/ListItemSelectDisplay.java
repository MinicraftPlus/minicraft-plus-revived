package minicraft.screen;

import minicraft.core.Game;
import minicraft.gfx.Point;
import minicraft.gfx.Screen;
import minicraft.screen.entry.SelectEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ListItemSelectDisplay<T> extends Display {
	private final Consumer<T> callback;

	public static class ListItemHandler<T> {
		public final @NotNull Function<T, String> stringifier;
		public final @Nullable Predicate<T> itemFilter;
		public final boolean removeFiltered;

		public ListItemHandler() {
			this(Objects::toString);
		}

		public ListItemHandler(@NotNull Function<T, String> stringifier) {
			this(stringifier, null);
		}

		public ListItemHandler(@NotNull Function<T, String> stringifier, @Nullable Predicate<T> itemFilter) {
			this(stringifier, itemFilter, true);
		}

		public ListItemHandler(@NotNull Function<T, String> stringifier, @Nullable Predicate<T> itemFilter, boolean removeFiltered) {
			this.stringifier = stringifier;
			this.itemFilter = itemFilter;
			this.removeFiltered = removeFiltered;
		}
	}

	public ListItemSelectDisplay(T[] list, Consumer<T> callback) {
		this(Arrays.asList(list), new ListItemHandler<>(), callback);
	}

	public ListItemSelectDisplay(List<T> list, Consumer<T> callback) {
		this(list, new ListItemHandler<>(), callback);
	}

	public ListItemSelectDisplay(T[] list, @NotNull ListItemHandler<T> itemHandler, Consumer<T> callback) {
		this(Arrays.asList(list), itemHandler, callback);
	}

	public ListItemSelectDisplay(List<T> list, @NotNull ListItemHandler<T> itemHandler, Consumer<T> callback) {
		this.callback = callback;
		menus = new Menu[]{
			new Menu.Builder(true, 1, RelPos.CENTER)
				.setPositioning(new Point(Screen.w / 2, Screen.h / 2), RelPos.CENTER)
				.setEntries((itemHandler.itemFilter != null && itemHandler.removeFiltered ?
					list.stream().filter(itemHandler.itemFilter) : list.stream()).map(e -> {
					SelectEntry entry = new SelectEntry(itemHandler.stringifier.apply(e), () -> onSelect(e), false);
					if (itemHandler.itemFilter != null && !itemHandler.removeFiltered && !itemHandler.itemFilter.test(e))
						entry.setSelectable(false);
					return entry;
				}).collect(Collectors.toList()))
				.setTitle("Select")
				.setDisplayLength(Math.min(list.size(), 10))
				.setSearcherBar(true)
				.createMenu()
		};
	}

	private void onSelect(T item) {
		callback.accept(item);
		Game.exitDisplay();
	}
}
