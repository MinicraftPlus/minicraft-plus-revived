package minicraft.screen;

import minicraft.core.Action;
import minicraft.core.Game;
import minicraft.core.Updater;
import minicraft.core.World;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Settings;
import minicraft.core.io.Sound;
import minicraft.entity.mob.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Point;
import minicraft.gfx.Screen;
import minicraft.item.PotionItem;
import minicraft.item.PotionType;
import minicraft.level.Level;
import minicraft.level.tile.Tiles;
import minicraft.screen.entry.ArrayEntry;
import minicraft.screen.entry.BooleanEntry;
import minicraft.screen.entry.ChangeListener;
import minicraft.screen.entry.InputEntry;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.SelectEntry;
import minicraft.util.Logging;
import org.intellij.lang.annotations.RegExp;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class DebugPanelDisplay extends Display {
	@RegExp
	private static final String regexNumber = "[0-9]+";
	@RegExp
	private static final String regexNegNumber = "[0-9-]+";

	// This would only handle keyboard inputs.
	public DebugPanelDisplay() {
		super(new Menu.Builder(true, 0, RelPos.LEFT, getEntries())
			.setPositioning(new Point(9, 9), RelPos.BOTTOM_RIGHT)
			.setDisplayLength(6)
			.setSelectable(true)
			.setScrollPolicies(1, false)
			.setSearcherBar(true)
			.setTitle("minicraft.display.debug_panel")
			.createMenu());
	}

	private static List<ListEntry> getEntries() {
		ArrayList<ListEntry> entries = new ArrayList<>();

		entries.add(new SelectEntry("Print all players", () -> {
			// Print all players on all levels, and their coordinates.
			Logging.WORLD.info("Printing players on all levels.");
			for (Level value : Game.levels) {
				if (value == null) continue;
				value.printEntityLocs(Player.class);
			}
			Game.exitDisplay();
		}, false));
		entries.add(new SelectEntry("Teleport to...",() -> {
			LevelTileCoordinatesOption coordinatesOption =
				new LevelTileCoordinatesOption(Game.player.getLevel(), Game.player.x >> 4, Game.player.y >> 4);
			CommandOptionEntry optionEntry = new CommandOptionEntry(coordinatesOption);
			coordinatesOption.init(optionEntry);
			Game.setDisplay(new CommandPopupDisplay(null, () -> {
				Level distLevel;
				int distX;
				int distY;
				try {
					distLevel = coordinatesOption.getLevelValue();
					distX = coordinatesOption.getXValue();
					distY = coordinatesOption.getYValue();
				} catch (IllegalArgumentException e) {
					Logging.WORLDNAMED.error(e, "Invalid arguments in options of command `Teleport to ...`");
					return true; // No action.
				}

				Level prevLevel = Game.player.getLevel();
				int prevX = Game.player.x;
				int prevY = Game.player.y;
				Game.player.x = distX * 16 + 8;
				Game.player.y = distY * 16 + 8;
				if (prevLevel != distLevel) {
					Game.exitDisplay(2);
					Game.setDisplay(new LevelTransitionDisplay(distLevel.depth - prevLevel.depth));
					return false;
				}
				Logging.WORLDNAMED.info("Teleported player from ({}, {}, {}) to ({}, {}, {}).",
					prevLevel.depth, prevX, prevY, distLevel.depth, Game.player.x, Game.player.y);
				return true;
			}, display -> coordinatesOption.isAllInputValid(), Collections.singletonList(optionEntry)));
		}, false));
		entries.add(new SelectEntry("Time set ...", () -> {
			//noinspection Convert2Diamond Ambious type infer
			ArrayEntry<Updater.Time> timeArrayEntry = new ArrayEntry<Updater.Time>("Time", false, false,
				Updater.Time.Morning, Updater.Time.Day, Updater.Time.Evening, Updater.Time.Night);
			InputEntry timeOption = new InputEntry("Time", regexNumber, 0) {
				@Override
				public boolean isValid() {
					try {
						int value = Integer.parseInt(getUserInput());
						return value >= 0 && value <= Updater.dayLength;
					} catch (NumberFormatException e) {
						return false;
					}
				}
			};
			UnionEntry<ListEntry> unionEntry = new UnionEntry<>(timeArrayEntry, timeOption);
			BooleanEntry timeTypeEntry = new BooleanEntry("Specific", false);
			CommandOptionEntry optionEntry = new CommandOptionEntry(timeTypeEntry, new CommandOptionEntry(unionEntry));
			timeTypeEntry.setChangeAction(v -> {
				unionEntry.setSelection((boolean) v ? 1 : 0);
				optionEntry.callCheckUpdateListener();
			}); // TODO Change ChangeListener into ChangeListener<T>
			ChangeListener listener = v -> optionEntry.callCheckUpdateListener();
			timeOption.setChangeListener(listener);
			timeArrayEntry.setChangeAction(listener);
			Game.setDisplay(new CommandPopupDisplay(null, () -> {
				ListEntry selectedEntry = unionEntry.getSelectedEntry();
				int distTime;
				if (selectedEntry == timeArrayEntry) {
					distTime = timeArrayEntry.getValue().tickTime;
				} else if (selectedEntry == timeOption) {
					try {
						distTime = Integer.parseInt(timeOption.getUserInput());
						if (distTime < 0 || distTime > Updater.dayLength)
							throw new IllegalArgumentException("time out of range");
					} catch (IllegalArgumentException e) {
						Logging.WORLDNAMED.error(e, "Invalid arguments in options of command `Time set ...`");
						return true; // No action.
					}
				} else {
					Logging.WORLDNAMED.error("Invalid (unexpected) union entry selection in options of command `Time set ...`");
					return true; // No action.
				}

				Updater.setTime(distTime);
				Logging.WORLDNAMED.info("Time set to {}.", distTime);
				return true;
			}, display -> unionEntry.getSelectedEntry() != timeOption || timeOption.isValid(),
				Collections.singletonList(optionEntry)));
		}, false));
		entries.add(new SelectEntry("Gamemode ...", () -> {
			ArrayEntry<String> modeEntry = new ArrayEntry<>("minicraft.settings.mode",
				"minicraft.settings.mode.survival", "minicraft.settings.mode.creative",
				"minicraft.settings.mode.hardcore", "minicraft.settings.mode.score");
			modeEntry.setValue(Settings.get("mode"));
			CommandOptionEntry optionEntry = new CommandOptionEntry(modeEntry);
			modeEntry.setChangeAction(v -> optionEntry.callCheckUpdateListener());
			Game.setDisplay(new CommandPopupDisplay(null, () -> {
				Object prevMode = Settings.get("mode");
				String distMode = modeEntry.getValue();
				Settings.set("mode", distMode);
				Logging.WORLDNAMED.info("Game mode changed from {} into {}.", prevMode, distMode);
				return true;
			}, null, Collections.singletonList(optionEntry)));
		}, false));
		entries.add(new SelectEntry("Reset score time as 5 seconds", () -> {
			if (Game.isMode("minicraft.settings.mode.score")) {
				Updater.scoreTime = Updater.normSpeed * 5; // 5 seconds
			}
			Game.exitDisplay();
		}, false));
		entries.add(new SelectEntry("Reset tick speed (TPS)", () -> {
			float prevSpeed = Updater.gamespeed;
			Updater.gamespeed = 1;
			Logging.WORLDNAMED.trace("Tick speed reset from {} into 1.", prevSpeed);
			Game.exitDisplay();
		}, false));
		entries.add(new SelectEntry("Increase tick speed (TPS)", () -> {
			float prevSpeed = Updater.gamespeed;
			if (Updater.gamespeed < 1) Updater.gamespeed *= 2;
			else if (Updater.normSpeed * Updater.gamespeed < 2000) Updater.gamespeed++;
			Logging.WORLDNAMED.trace("Tick speed increased from {} into {}.", prevSpeed, Updater.gamespeed);
			Game.exitDisplay();
		}, false));
		entries.add(new SelectEntry("Decrease tick speed (TPS)", () -> {
			float prevSpeed = Updater.gamespeed;
			if (Updater.gamespeed > 1) Updater.gamespeed--;
			else if (Updater.normSpeed * Updater.gamespeed > 5) Updater.gamespeed /= 2;
			Logging.WORLDNAMED.trace("Tick speed decreased from {} into {}.", prevSpeed, Updater.gamespeed);
			Game.exitDisplay();
		}, false));
		entries.add(new SelectEntry("Reduce health point", () -> {
			Game.player.health--;
			Game.exitDisplay();
		}, false));
		entries.add(new SelectEntry("Reduce hunger point", () -> {
			Game.player.hunger--;
			Game.exitDisplay();
		}, false));
		entries.add(new SelectEntry("Reset moving speed", () -> {
			Game.player.moveSpeed = 1;
			Game.exitDisplay();
		}, false));
		entries.add(new SelectEntry("Increase moving speed", () -> {
			Game.player.moveSpeed++;
			Game.exitDisplay();
		}, false));
		entries.add(new SelectEntry("Decrease moving speed", () -> {
			if (Game.player.moveSpeed > 1) Game.player.moveSpeed--; // -= 0.5D;
			Game.exitDisplay();
		}, false));
		entries.add(new SelectEntry("Set tile stairs up", () -> {
			Game.levels[Game.currentLevel].setTile(Game.player.x>>4, Game.player.y>>4, Tiles.get("Stairs Up"));
			Game.exitDisplay();
		}, false));
		entries.add(new SelectEntry("Set tile stairs down", () -> {
			Game.levels[Game.currentLevel].setTile(Game.player.x>>4, Game.player.y>>4, Tiles.get("Stairs Down"));
			Game.exitDisplay();
		}, false));
		entries.add(new SelectEntry("Change level down (instant)", () -> {
			Game.exitDisplay();
			Game.setDisplay(new LevelTransitionDisplay(-1));
		}, false));
		entries.add(new SelectEntry("Change level up (instant)", () -> {
			Game.exitDisplay();
			Game.setDisplay(new LevelTransitionDisplay(1));
		}, false));
		entries.add(new SelectEntry("Change level up", () -> {
			World.scheduleLevelChange(1);
			Game.exitDisplay();
		}, false));
		entries.add(new SelectEntry("Change level down", () -> {
			World.scheduleLevelChange(-1);
			Game.exitDisplay();
		}, false));
		entries.add(new SelectEntry("Effect ...", () -> {
			BooleanEntry actionEntry = new BooleanEntry("Clear", false);
			ArrayEntry<PotionType> effectEntry = new ArrayEntry<>("Effect", true, false, PotionType.values());
			InputEntry durEntry = new InputEntry("Duration", regexNumber, 0) {
				private boolean specific = false;
				private ChangeListener listener = null;

				@Override
				public void tick(InputHandler input) {
					if (input.getKey("CURSOR-LEFT").clicked || input.getKey("CURSOR-RIGHT").clicked) {
						specific = !specific;
						Sound.play("select");
						if (listener != null) listener.onChange(specific);
					} else if (specific) {
						super.tick(input);
					}
				}

				@Override
				public boolean isValid() {
					String input = getUserInput();
					if (input != null) {
						try {
							int value = Integer.parseInt(input);
							return value >= 0;
						} catch (NumberFormatException e) {
							return false;
						}
					}

					return true;
				}

				@Override
				public String getUserInput() {
					return specific ? super.getUserInput() : null;
				}

				@Override
				public void setChangeListener(ChangeListener l) {
					listener = l;
					super.setChangeListener(l);
				}

				@Override
				public String toString() {
					return specific ? super.toString() : "Duration: Default";
				}
			};

			CommandOptionEntry optionEntry = new CommandOptionEntry(actionEntry);
			actionEntry.setChangeAction(v -> optionEntry.callCheckUpdateListener());
			CommandOptionEntry optionEntry1 = new CommandOptionEntry(effectEntry);
			effectEntry.setChangeAction(v -> optionEntry1.callCheckUpdateListener());
			CommandOptionEntry optionEntry2 = new CommandOptionEntry(durEntry);
			durEntry.setChangeListener(v -> optionEntry2.callCheckUpdateListener());
			Game.setDisplay(new CommandPopupDisplay(null, () -> {
				if (actionEntry.getValue()) for (PotionType potionType : Game.player.potioneffects.keySet()) {
					PotionItem.applyPotion(Game.player, potionType, false);
				}

				PotionType effect = effectEntry.getValue();
				String durString = durEntry.getUserInput();
				if (durString != null) {
					try {
						int value = Integer.parseInt(durString);
						if (value < 0)
							throw new IllegalArgumentException("duration negative");
						if (PotionItem.applyPotion(Game.player, effect, value))
							Logging.WORLDNAMED.info("Effect {} applied with specified duration {}.", effect, value);
						else
							Logging.WORLDNAMED.info("Effect {} failed applying with specified duration: {}.", effect, value);
					} catch (IllegalArgumentException e) {
						Logging.WORLDNAMED.error(e, "Effect {} failed applying with specified duration: {}", effect, durString);
					}
				} else {
					if (PotionItem.applyPotion(Game.player, effect, true))
						Logging.WORLDNAMED.info("Effect {} applied with default duration.", effect);
					else
						Logging.WORLDNAMED.info("Effect {} failed applying with default duration.", effect);
				}
				return true;
			}, display -> durEntry.isValid(), Arrays.asList(optionEntry, optionEntry1, optionEntry2)));
		}, false));

		return entries;
	}

	private static class CommandPopupDisplay extends Display {
		private final @Nullable Action onCancel;
		private final Menu.Builder builder;

		public CommandPopupDisplay(@Nullable Action onCancel, @NotNull Supplier<Boolean> onAction,
								   @Nullable Predicate<CommandPopupDisplay> allowCheck,
								   @NotNull List<CommandOptionEntry> optionEntries) {
			ArrayList<ListEntry> entries = new ArrayList<>();
			SelectEntry actionEntry = new SelectEntry("Execute", () -> {
				if (onAction.get()) // When necessary
					Game.exitDisplay(2); // Exits both the current display and the debug panel.
			}, false) {
				@Override
				public int getColor(boolean isSelected) {
					return isSelectable() ? super.getColor(isSelected) : Color.DARK_GRAY;
				}
			};
			Action checkUpdateListener = () -> actionEntry.setSelectable(allowCheck == null || allowCheck.test(this));
			optionEntries.forEach(e -> {
				entries.addAll(e.getEntries());
				e.setCheckUpdateListener(checkUpdateListener);
			});
			entries.add(actionEntry);
			checkUpdateListener.act(); // First call
			builder = new Menu.Builder(true, 2, RelPos.CENTER, entries)
				.setTitle("minicraft.display.debug_panel.command_popup");
			menus = new Menu[] { builder.createMenu() };
			this.onCancel = onCancel;
		}

		@Override
		public void tick(InputHandler input) {
			if (input.getKey("EXIT").clicked) {
				if (onCancel != null) onCancel.act();
				Game.exitDisplay(); // Exits the current display and the debug panel
				return;
			}

			super.tick(input);

			// For insurance
			int prevSel = menus[0].getSelection();
			menus[0] = builder.createMenu(); // Re-calculating frame
			menus[0].setSelection(prevSel);
		}
	}

	private static class CommandOptionEntry {
		private final @NotNull ListEntry delegateEntry;
		private final @Nullable CommandOptionEntry subEntry;

		private Action checkUpdateListener = null;

		public CommandOptionEntry(@NotNull ListEntry delegateEntry) { this(delegateEntry, null); }
		public CommandOptionEntry(@NotNull ListEntry delegateEntry, @Nullable CommandOptionEntry subEntry) {
			this.delegateEntry = delegateEntry;
			this.subEntry = subEntry;
		}

		public void setVisible(boolean visible) {
			delegateEntry.setVisible(visible);
			if (!visible) setSubEntryVisible(false);
		}

		public void setSubEntryVisible(boolean visible) {
			if (subEntry != null) subEntry.setVisible(visible);
		}

		public List<ListEntry> getEntries() {
			ArrayList<ListEntry> entries = new ArrayList<>();
			entries.add(delegateEntry);
			if (subEntry != null) entries.addAll(subEntry.getEntries());
			return entries;
		}

		public void callCheckUpdateListener() {
			if (checkUpdateListener != null) checkUpdateListener.act();
		}

		public void setCheckUpdateListener(Action checkUpdateListener) {
			this.checkUpdateListener = checkUpdateListener;
		}
	}

	private static class LevelTileCoordinatesOption extends ListEntry {
		private static final int DARK_RED = Color.tint(Color.RED, -1, true);
		private static final int INPUT_ENTRY_COUNT = 3; // Level depth, x, y

		private final List<InputEntry> inputs;

		private int selection = 0;

		public LevelTileCoordinatesOption(Level level, int x, int y) {
			inputs = Collections.unmodifiableList(Arrays.asList(
				new InputEntry("Level", regexNegNumber, 0, String.valueOf(level.depth)) {
					@Override
					public boolean isValid() {
						try {
							int value = Integer.parseInt(getUserInput());
							return value >= -4 && value <= 1;
						} catch (NumberFormatException e) {
							return false;
						}
					}

					@Override
					public void render(Screen screen, int x, int y, boolean isSelected) {
						Font.draw(toString(), screen, x, y, isValid() ?
							(isSelected ? Color.GREEN : COL_UNSLCT) :
							isSelected ? Color.RED : DARK_RED);
					}
				},
				new InputEntry("X", regexNumber, 0, String.valueOf(x)) {
					@Override
					public boolean isValid() {
						try {
							int value = Integer.parseInt(getUserInput());
							return value >= 0 && value < level.w;
						} catch (NumberFormatException e) {
							return false;
						}
					}

					@Override
					public void render(Screen screen, int x, int y, boolean isSelected) {
						Font.draw(toString(), screen, x, y, isValid() ?
							(isSelected ? Color.GREEN : COL_UNSLCT) :
							isSelected ? Color.RED : DARK_RED);
					}
				},
				new InputEntry("Y", regexNumber, 0, String.valueOf(y)) {
					@Override
					public boolean isValid() {
						try {
							int value = Integer.parseInt(getUserInput());
							return value >= 0 && value < level.h;
						} catch (NumberFormatException e) {
							return false;
						}
					}

					@Override
					public void render(Screen screen, int x, int y, boolean isSelected) {
						Font.draw(toString(), screen, x, y, isValid() ?
							(isSelected ? Color.GREEN : COL_UNSLCT) :
							isSelected ? Color.RED : DARK_RED);
					}
				}
			));
		}

		public void init(CommandOptionEntry optionEntry) {
			inputs.forEach(inputEntry -> inputEntry.setChangeListener(v -> optionEntry.callCheckUpdateListener()));
		}

		@Override
		public void tick(InputHandler input) {
			if (input.getKey("CURSOR-LEFT").clicked) {
				if (selection > 0) selection--;
				Sound.play("select");
			} else if (input.getKey("CURSOR-RIGHT").clicked) {
				if (selection < INPUT_ENTRY_COUNT - 1) selection++;
				Sound.play("select");
			} else {
				inputs.get(selection).tick(input);
			}
		}

		@Override
		public void render(Screen screen, int x, int y, boolean isSelected) {
			if (isVisible()) {
				for (int i = 0; i < INPUT_ENTRY_COUNT; i++) {
					inputs.get(i).render(screen, x, y, isSelected && i == selection);
					x += Font.textWidth(inputs.get(i).toString()) + Font.textWidth("; ");
				}
			}
		}

		public Level getLevelValue() throws IllegalArgumentException {
			if (!inputs.get(0).isValid()) throw new IllegalArgumentException();
			try {
				return World.levels[World.lvlIdx(Integer.parseInt(inputs.get(0).getUserInput()))];
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException(e);
			}
		}
		public int getXValue() throws IllegalArgumentException {
			if (!inputs.get(0).isValid()) throw new IllegalArgumentException();
			try {
				return Integer.parseInt(inputs.get(1).getUserInput());
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException(e);
			}
		}
		public int getYValue() throws IllegalArgumentException {
			if (!inputs.get(0).isValid()) throw new IllegalArgumentException();
			try {
				return Integer.parseInt(inputs.get(2).getUserInput());
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException(e);
			}
		}

		public boolean isAllInputValid() {
			return inputs.stream().allMatch(InputEntry::isValid);
		}

		@Override
		public String toString() {
			return inputs.stream().map(InputEntry::toString).collect(Collectors.joining("; "));
		}
	}

	private static class UnionEntry<T extends ListEntry> extends ListEntry {
		private final List<T> entries;

		private int selection = 0;

		@SafeVarargs
		public UnionEntry(T... entries) {
			this.entries = Arrays.asList(entries);
		}

		public T getEntry(int index) {
			return entries.get(index);
		}

		public void setEntry(int index, T entry) {
			entries.set(index, entry);
		}

		public void setSelection(int selection) {
			this.selection = selection;
		}

		public int getSelection() {
			return selection;
		}

		public T getSelectedEntry() {
			return entries.get(selection);
		}

		public int size() {
			return entries.size();
		}

		@Override
		public void tick(InputHandler input) {
			entries.get(selection).tick(input);
		}

		@Override
		public void render(Screen screen, int x, int y, boolean isSelected, String contain, int containColor) {
			entries.get(selection).render(screen, x, y, isSelected, contain, containColor);
		}

		@Override
		public void render(Screen screen, int x, int y, boolean isSelected) {
			entries.get(selection).render(screen, x, y, isSelected);
		}

		@Override
		public int getColor(boolean isSelected) {
			return entries.get(selection).getColor(isSelected);
		}

		@Override
		public int getWidth() {
			return entries.get(selection).getWidth();
		}

		@Override
		public String toString() {
			return entries.get(selection).toString();
		}
	}
}
