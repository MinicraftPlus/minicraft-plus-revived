package minicraft.screen;

import minicraft.core.Action;
import minicraft.core.Game;
import minicraft.core.Updater;
import minicraft.core.World;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Settings;
import minicraft.core.io.Sound;
import minicraft.entity.Entity;
import minicraft.entity.mob.Cow;
import minicraft.entity.mob.Creeper;
import minicraft.entity.mob.Knight;
import minicraft.entity.mob.Pig;
import minicraft.entity.mob.Player;
import minicraft.entity.mob.Sheep;
import minicraft.entity.mob.Skeleton;
import minicraft.entity.mob.Slime;
import minicraft.entity.mob.Snake;
import minicraft.entity.mob.Zombie;
import minicraft.gfx.Color;
import minicraft.gfx.Point;
import minicraft.item.Inventory;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.PotionItem;
import minicraft.item.PotionType;
import minicraft.item.StackableItem;
import minicraft.item.UnknownItem;
import minicraft.level.Level;
import minicraft.level.tile.HoleTile;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;
import minicraft.screen.entry.ArrayEntry;
import minicraft.screen.entry.BooleanEntry;
import minicraft.screen.entry.ChangeListener;
import minicraft.screen.entry.InputEntry;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.SelectEntry;
import minicraft.screen.entry.UserMutable;
import minicraft.screen.entry.commands.LevelCoordinatesOption;
import minicraft.screen.entry.commands.LevelSelectionOption;
import minicraft.screen.entry.commands.SelectableListInputEntry;
import minicraft.screen.entry.commands.TargetSelectorEntry;
import minicraft.screen.entry.commands.UnionEntry;
import minicraft.util.Logging;
import minicraft.util.MyUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class DebugPanelDisplay extends Display {
	// This would only handle keyboard inputs.
	// No localization would be applied.
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
			Level curLevel = Game.player.getLevel();
			LevelSelectionOption levelOption = new LevelSelectionOption(curLevel.depth);
			LevelCoordinatesOption coordinatesOption =
				new LevelCoordinatesOption(curLevel.w, curLevel.h, Game.player.x, Game.player.y, false, false);
			CommandOptionEntry optionEntry = new CommandOptionEntry(levelOption);
			CommandOptionEntry optionEntry1 = new CommandOptionEntry(coordinatesOption);
			Game.setDisplay(new CommandPopupDisplay(null, () -> {
				int distLevel;
				int distX;
				int distY;
				try {
					distLevel = levelOption.getValue();
					distX = coordinatesOption.getXValue();
					distY = coordinatesOption.getYValue();
				} catch (IllegalArgumentException e) {
					Logging.WORLDNAMED.error(e, "Invalid arguments in options of command `Teleport to ...`");
					return true; // No action.
				}

				int prevLevel = curLevel.depth;
				int prevX = Game.player.x;
				int prevY = Game.player.y;
				Game.player.x = distX;
				Game.player.y = distY;
				if (prevLevel != distLevel) {
					Game.exitDisplay(2);
					Game.setDisplay(new LevelTransitionDisplay(distLevel - curLevel.depth));
					Logging.WORLDNAMED.info("Teleported player from ({}, {}, {}) to ({}, {}, {}).",
						curLevel.depth, prevX, prevY, distLevel, Game.player.x, Game.player.y);
					return false;
				}

				Logging.WORLDNAMED.info("Teleported player from ({}, {}, {}) to ({}, {}, {}).",
					curLevel.depth, prevX, prevY, distLevel, Game.player.x, Game.player.y);
				return true;
			}, display -> levelOption.isValid() && coordinatesOption.isAllInputValid(),
				Arrays.asList(optionEntry, optionEntry1)));
		}, false));
		entries.add(new SelectEntry("Time set ...", () -> {
			//noinspection Convert2Diamond Ambious type infer
			ArrayEntry<Updater.Time> timeArrayEntry = new ArrayEntry<Updater.Time>("Time", false, false,
				Updater.Time.Morning, Updater.Time.Day, Updater.Time.Evening, Updater.Time.Night);
			InputEntry timeOption = new InputEntry("Time", InputEntry.regexNumber, 0) {
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
			timeTypeEntry.setChangeListener(v -> {
				unionEntry.setSelection((boolean) v ? 1 : 0);
				optionEntry.callCheckUpdateListener();
			}); // TODO Change ChangeListener into ChangeListener<T>
			ChangeListener listener = v -> optionEntry.callCheckUpdateListener();
			timeOption.setChangeListener(listener);
			timeArrayEntry.setChangeListener(listener);
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
			InputEntry durEntry = new InputEntry("Duration", InputEntry.regexNumber, 0) {
				private boolean specific = false;

				@Override
				public void tick(InputHandler input) {
					if (input.getMappedKey("CURSOR-LEFT").isClicked() ||
						input.getMappedKey("CURSOR-RIGHT").isClicked()) {
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
				public String toString() {
					return specific ? super.toString() : "Duration: Default";
				}
			};

			CommandOptionEntry optionEntry = new CommandOptionEntry(actionEntry);
			CommandOptionEntry optionEntry1 = new CommandOptionEntry(effectEntry);
			CommandOptionEntry optionEntry2 = new CommandOptionEntry(durEntry);
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
		entries.add(new SelectEntry("Inventory Clear ...", () -> {
			SelectableListInputEntry itemSelEntry = new SelectableListInputEntry("Item", Items.getRegisteredItemKeys(), "All");
			InputEntry countEntry = new InputEntry("Max Count", InputEntry.regexNumber, 0) {
				@Override
				public boolean isValid() {
					String input = getUserInput();
					if (input.isEmpty()) return true; // Infinite
					try {
						int value = Integer.parseInt(input);
						return value >= 0;
					} catch (NumberFormatException e) {
						return false;
					}
				}

				@Override
				public String toString() {
					return getUserInput().isEmpty() ? "Max Count: Infinite" : super.toString();
				}
			};

			countEntry.setVisible(false);
			CommandOptionEntry optionEntry = new CommandOptionEntry(itemSelEntry, new CommandOptionEntry(countEntry));
			itemSelEntry.setChangeListener(v -> {
				countEntry.setVisible(!itemSelEntry.getUserInput().isEmpty());
				optionEntry.callCheckUpdateListener();
			});
			Game.setDisplay(new CommandPopupDisplay(null, () -> {
				int count = 0;
				if (itemSelEntry.getUserInput().isEmpty()) {
					Inventory inventory = Game.player.getInventory();
					count = inventory.getItems().stream().reduce(0, (a, b) -> a + Items.getCount(b), Integer::sum);
					inventory.clearInv();
				} else if (!itemSelEntry.isValid()) {
					Logging.WORLDNAMED.error("Item specified is invalid: {}.", itemSelEntry.getUserInput());
					return true;
				} else {
					Inventory inventory = Game.player.getInventory();
					Item item = Items.get(itemSelEntry.getUserInput());
					if (item instanceof UnknownItem) {
						Logging.WORLDNAMED.error("Item specified is unknown: {}.", itemSelEntry.getUserInput());
						return true;
					} else {
						if (countEntry.getUserInput().isEmpty()) {
							for (int i = 0; i < inventory.invSize(); i++) {
								Item stack;
								if (item.equals(stack = inventory.get(i))) {
									count += Items.getCount(stack); // May add case 0 handle
									inventory.remove(i);
									i--; // To iterate correctly
								}
							}
						} else {
							int value;
							try {
								value = Integer.parseInt(countEntry.getUserInput());
								if (value < 0)
									throw new IllegalArgumentException("count negative");
							} catch (IllegalArgumentException e) {
								Logging.WORLDNAMED.error(e, "Invalid count: {}", countEntry.getUserInput());
								return true;
							}

							if (value == 0) {
								Logging.WORLDNAMED.info("Item of `{}` in inventory count: {}.", item.getName(), inventory.count(item));
								return true;
							} else {
								for (int i = 0; i < inventory.invSize(); i++) {
									Item stack;
									if (item.equals(stack = inventory.get(i))) {
										if (stack instanceof StackableItem) {
											int toRemove = Math.min(value - count, ((StackableItem) stack).count);
											((StackableItem) stack).count -= toRemove;
											if (((StackableItem) stack).count == 0) inventory.remove(i);
											count += toRemove;
										} else {
											inventory.remove(i);
											count++;
										}

										if (count == value) break;
										i--; // To iterate correctly
									}
								}
							}
						}
					}
				}

				Logging.WORLDNAMED.info("Removed {} items from the inventory.", count);
				return true;
			}, display -> itemSelEntry.getUserInput().isEmpty() || itemSelEntry.isValid() && countEntry.isValid(),
				Collections.singletonList(optionEntry)));
		}, false));
		entries.add(new SelectEntry("Set tile ...", () -> {
			Level curLevel = Game.player.getLevel();
			LevelSelectionOption levelOption = new LevelSelectionOption(curLevel.depth);
			LevelCoordinatesOption coordinatesOption =
				new LevelCoordinatesOption(curLevel.w, curLevel.h, Game.player.x, Game.player.y, false, true);
			SelectableListInputEntry tileSelEntry = new SelectableListInputEntry("Tile", Tiles.getRegisteredTileKeys());
			InputEntry dataEntry = new InputEntry("Data", InputEntry.regexNumber, 0, "0") {
				@Override
				public boolean isValid() {
					try {
						Short.parseShort(getUserInput());
						return true;
					} catch (NumberFormatException e) {
						return false;
					}
				}

				@Override
				public void setChangeListener(ChangeListener l) {
					super.setChangeListener(v -> {
						String input = getUserInput();
						if (input.startsWith("0") && input.length() > 1)
							setUserInput(input.substring(1)); // Trimming leading zero
						if (input.isEmpty()) setUserInput("0"); // "zero" placeholder (default value)
						l.onChange(v);
					});
				}
			};
			final String METHOD_KEEP = "KEEP";
			final String METHOD_REPLACE = "REPLACE";
			//noinspection Convert2Diamond Ambious type infer
			ArrayEntry<String> methodEntry = new ArrayEntry<String>("Method", true, false, METHOD_KEEP, METHOD_REPLACE);
			methodEntry.setValue(METHOD_REPLACE);

			CommandOptionEntry optionEntry = new CommandOptionEntry(levelOption);
			CommandOptionEntry optionEntry1 = new CommandOptionEntry(coordinatesOption);
			CommandOptionEntry optionEntry2 = new CommandOptionEntry(tileSelEntry);
			CommandOptionEntry optionEntry3 = new CommandOptionEntry(dataEntry);
			CommandOptionEntry optionEntry4 = new CommandOptionEntry(methodEntry);
			Game.setDisplay(new CommandPopupDisplay(null, () -> {
				int distLevel;
				int distX;
				int distY;
				String tile;
				short data;
				try {
					distLevel = levelOption.getValue();
					distX = coordinatesOption.getXValue();
					distY = coordinatesOption.getYValue();
					if (!tileSelEntry.isValid())
						throw new IllegalArgumentException("tile inputted is invalid");
					tile = tileSelEntry.getUserInput();
					data = Short.parseShort(dataEntry.getUserInput());
				} catch (IllegalArgumentException e) {
					Logging.WORLDNAMED.error(e, "Invalid arguments in options of command `Set tile ...`");
					return true; // No action.
				}

				Level level = World.levels[World.lvlIdx(distLevel)];
				switch (methodEntry.getValue()) {
					case METHOD_KEEP:
						if (!(level.getTile(distX, distY) instanceof HoleTile)) {
							Logging.WORLDNAMED.info("No tile is placed.");
							return true;
						}
					case METHOD_REPLACE: break; // Valid and expected; skip check
					default:
						Logging.WORLDNAMED.error("Invalid (unexpected) method is inputted.");
						return true;
				}

				level.setTile(distX, distY, Tiles.get(tile), data);
				Logging.WORLDNAMED.info("Placed tile {} at ({}, {}, {}) with data {}.", tile, distLevel, distX, distY, data);
				return true;
			}, display -> levelOption.isValid() && coordinatesOption.isAllInputValid() && tileSelEntry.isValid() && dataEntry.isValid(),
				Arrays.asList(optionEntry, optionEntry1, optionEntry2, optionEntry3, optionEntry4)));
		}, false));
		entries.add(new SelectEntry("Fill ...", () -> {
			Level curLevel = Game.player.getLevel();
			LevelSelectionOption levelOption = new LevelSelectionOption(curLevel.depth);
			LevelCoordinatesOption fromOption =
				new LevelCoordinatesOption("From", curLevel.w, curLevel.h, Game.player.x, Game.player.y, false, true);
			LevelCoordinatesOption toOption =
				new LevelCoordinatesOption("To", curLevel.w, curLevel.h, Game.player.x, Game.player.y, false, true);
			SelectableListInputEntry tileSelEntry = new SelectableListInputEntry("Tile", Tiles.getRegisteredTileKeys());
			InputEntry dataEntry = new InputEntry("Data", InputEntry.regexNumber, 0, "0") {
				@Override
				public boolean isValid() {
					try {
						Short.parseShort(getUserInput());
						return true;
					} catch (NumberFormatException e) {
						return false;
					}
				}

				@Override
				public void setChangeListener(ChangeListener l) {
					super.setChangeListener(v -> {
						String input = getUserInput();
						if (input.startsWith("0") && input.length() > 1)
							setUserInput(input.substring(1)); // Trimming leading zero
						if (input.isEmpty()) setUserInput("0"); // "zero" placeholder (default value)
						l.onChange(v);
					});
				}
			};
			final String METHOD_HOLLOW = "HOLLOW";
			final String METHOD_KEEP = "KEEP";
			final String METHOD_OUTLINE = "OUTLINE";
			final String METHOD_REPLACE = "REPLACE";
			//noinspection Convert2Diamond Ambious type infer
			ArrayEntry<String> methodEntry = new ArrayEntry<String>("Method", true, false,
				METHOD_HOLLOW, METHOD_KEEP, METHOD_OUTLINE, METHOD_REPLACE);
			methodEntry.setValue(METHOD_REPLACE);

			CommandOptionEntry optionEntry = new CommandOptionEntry(levelOption);
			CommandOptionEntry optionEntry1 = new CommandOptionEntry(fromOption);
			CommandOptionEntry optionEntry2 = new CommandOptionEntry(toOption);
			CommandOptionEntry optionEntry3 = new CommandOptionEntry(tileSelEntry);
			CommandOptionEntry optionEntry4 = new CommandOptionEntry(dataEntry);
			CommandOptionEntry optionEntry5 = new CommandOptionEntry(methodEntry);
			Game.setDisplay(new CommandPopupDisplay(null, () -> {
				int distLevel;
				int distX1;
				int distY1;
				int distX2;
				int distY2;
				String tile;
				short data;
				try {
					distLevel = levelOption.getValue();
					distX1 = fromOption.getXValue();
					distY1 = fromOption.getYValue();
					distX2 = toOption.getXValue();
					distY2 = toOption.getYValue();
					if (!tileSelEntry.isValid())
						throw new IllegalArgumentException("tile inputted is invalid");
					tile = tileSelEntry.getUserInput();
					data = Short.parseShort(dataEntry.getUserInput());
				} catch (IllegalArgumentException e) {
					Logging.WORLDNAMED.error(e, "Invalid arguments in options of command `Fill ...`");
					return true; // No action.
				}

				int minX = Math.min(distX1, distX2);
				int minY = Math.min(distY1, distY2);
				int maxX = Math.max(distX1, distX2);
				int maxY = Math.max(distY1, distY2);
				int count = 0;
				Level level = World.levels[World.lvlIdx(distLevel)];
				Tile tileInstance = Tiles.get(tile);
				switch (methodEntry.getValue()) {
					case METHOD_HOLLOW:
						Tile hole = Tiles.get("hole");
						for (int x = minX; x <= maxX; x++) {
							for (int y = minY; y <= maxY; y++) {
								if (x == minX || x == maxX || y == minY || y == maxY) {
									level.setTile(x, y, tileInstance, data);
								} else {
									level.setTile(x, y, hole);
								}
								count++;
							}
						}
						break;
					case METHOD_KEEP:
						for (int x = minX; x <= maxX; x++) {
							for (int y = minY; y <= maxY; y++) {
								if (!(level.getTile(x, y) instanceof HoleTile)) {
									level.setTile(x, y, tileInstance, data);
									count++;
								}
							}
						}
						break;
					case METHOD_OUTLINE:
						for (int x = minX; x <= maxX; x++) {
							for (int y = minY; y <= maxY; y++) {
								if (x == minX || x == maxX || y == minY || y == maxY) {
									level.setTile(x, y, tileInstance, data);
									count++;
								}
							}
						}
						break;
					case METHOD_REPLACE:
						for (int x = minX; x <= maxX; x++) {
							for (int y = minY; y <= maxY; y++) {
								level.setTile(x, y, tileInstance, data);
								count++;
							}
						}
						break;
					default:
						Logging.WORLDNAMED.error("Invalid (unexpected) method is inputted.");
						return true;
				}

				Logging.WORLDNAMED.info("Placed {}.", count, MyUtils.plural(count, "tile"));
				return true;
			}, display -> levelOption.isValid() && fromOption.isAllInputValid() && toOption.isAllInputValid() && tileSelEntry.isValid() && dataEntry.isValid(),
				Arrays.asList(optionEntry, optionEntry1, optionEntry2, optionEntry3, optionEntry4, optionEntry5)));
		}, false));
		entries.add(new SelectEntry("Kill ...", () -> {
			TargetSelectorEntry selectorEntry = new TargetSelectorEntry(Game.player);
			CommandOptionEntry optionEntry = new CommandOptionEntry(selectorEntry);
			Game.setDisplay(new CommandPopupDisplay(null, () -> {
				if (!selectorEntry.isValid()) {
					Logging.WORLDNAMED.error("Invalid arguments in options of command `Kill ...`");
					return true; // No action.
				}

				Collection<Entity> entities = selectorEntry.collectTargets();
				int count = entities.size();
				entities.forEach(Entity::die);
				Logging.WORLDNAMED.info("Eliminated {}.", count, MyUtils.plural(count, "entity"));
				return true;
			}, display -> selectorEntry.isValid(), Collections.singletonList(optionEntry)));
		}, false));
		// Item attributes cannot be modified or customized because of the design and nature (static and non-dynamic) of the game.
		entries.add(new SelectEntry("Give ...", () -> {
			SelectableListInputEntry itemSelEntry = new SelectableListInputEntry("Item", Items.getRegisteredItemKeys());
			InputEntry countEntry = new InputEntry("Count", InputEntry.regexNumber, 0) {
				@Override
				public boolean isValid() {
					String input = getUserInput();
					if (input.isEmpty()) return true; // One
					try {
						int value = Integer.parseInt(input);
						return value >= 1;
					} catch (NumberFormatException e) {
						return false;
					}
				}

				@Override
				public String toString() {
					return getUserInput().isEmpty() ? "Count: One" : super.toString();
				}
			};
			CommandOptionEntry optionEntry = new CommandOptionEntry(itemSelEntry);
			CommandOptionEntry optionEntry1 = new CommandOptionEntry(countEntry);
			Game.setDisplay(new CommandPopupDisplay(null, () -> {
				int count;
				try {
					String input = countEntry.getUserInput();;
					if (input.isEmpty()) count = 1;
					else {
						count = Integer.parseInt(input);
						if (count < 1)
							throw new IllegalArgumentException("count invalid");
					}
				} catch (IllegalArgumentException e) {
					Logging.WORLDNAMED.error(e, "Invalid arguments in options of command `Fill ...`");
					return true; // No action.
				}

				String itemName;
				if (!itemSelEntry.isValid()) {
					Logging.WORLDNAMED.error("Item specified is invalid: {}.", itemSelEntry.getUserInput());
					return true;
				} else {
					Inventory inventory = Game.player.getInventory();
					Item item = Items.get(itemSelEntry.getUserInput());
					if (item instanceof UnknownItem) {
						Logging.WORLDNAMED.error("Item specified is unknown: {}.", itemSelEntry.getUserInput());
						return true;
					} else {
						itemName = item.getName();
						if (item instanceof StackableItem) {
							((StackableItem) item).count = count;
							Game.player.tryAddToInvOrDrop(item);
						} else {
							for (int i = 0; i < count; ++i)
								Game.player.tryAddToInvOrDrop(item);
						}
					}
				}

				Logging.WORLDNAMED.info("Gave {} * {}.", count, itemName);
				return true;
			}, display -> itemSelEntry.isValid() && countEntry.isValid(),
				Arrays.asList(optionEntry, optionEntry1)));
		}, false));
		entries.add(new SelectEntry("Daytime Lock ...", () -> {
			BooleanEntry booleanEntry = new BooleanEntry("Do Daylight Cycle", Updater.timeFlow);
			CommandOptionEntry optionEntry = new CommandOptionEntry(booleanEntry);
			Game.setDisplay(new CommandPopupDisplay(null, () -> {
				Logging.WORLDNAMED.info("Daytime flow is now {}.", Updater.timeFlow = booleanEntry.getValue());
				return true;
			}, null, Collections.singletonList(optionEntry)));
		}, false));
		// Because of the hit box, summoning with a number is not implemented.
		// If entity attributes are going to be implemented, this would be massive and unideal
		// due to the nature (static and non-dynamic) of the system.
		// As the whole list of entities is too long to be included here and
		// as mentioned above, some entities require attributes to work properly, only mobs are listed here.
		// The original plan with bosses is to enable summoning if forced, but it is redundant to be implemented,
		// bosses are not included instead.
		HashMap<String, Function<Integer, Entity>> entitySelectionList = new HashMap<>();
		entitySelectionList.put("COW", lvl -> new Cow());
		entitySelectionList.put("CREEPER", Creeper::new);
		entitySelectionList.put("KNIGHT", Knight::new);
		entitySelectionList.put("PIG", lvl -> new Pig());
		entitySelectionList.put("SHEEP", lvl -> new Sheep());
		entitySelectionList.put("SKELETON", Skeleton::new);
		entitySelectionList.put("SLIME", Slime::new);
		entitySelectionList.put("SNAKE", Snake::new);
		entitySelectionList.put("ZOMBIE", Zombie::new);
		entries.add(new SelectEntry("Summon ...", () -> {
			SelectableListInputEntry entitySelEntry = new SelectableListInputEntry("Entity", entitySelectionList.keySet());
			Level curLevel = Game.player.getLevel();
			LevelSelectionOption levelOption = new LevelSelectionOption(curLevel.depth);
			LevelCoordinatesOption coordinatesOption =
				new LevelCoordinatesOption(curLevel.w, curLevel.h, Game.player.x, Game.player.y, false, false);
			CommandOptionEntry optionEntry = new CommandOptionEntry(entitySelEntry);
			CommandOptionEntry optionEntry1 = new CommandOptionEntry(levelOption);
			CommandOptionEntry optionEntry2 = new CommandOptionEntry(coordinatesOption);
			Game.setDisplay(new CommandPopupDisplay(null, () -> {
				int distLevel;
				int distX;
				int distY;
				try {
					distLevel = levelOption.getValue();
					distX = coordinatesOption.getXValue();
					distY = coordinatesOption.getYValue();
				} catch (IllegalArgumentException e) {
					Logging.WORLDNAMED.error(e, "Invalid arguments in options of command `Teleport to ...`");
					return true; // No action.
				}

				String selectedEntity = entitySelEntry.getUserInput();
				if (!entitySelectionList.containsKey(selectedEntity)) {
					Logging.WORLDNAMED.error("Invalid entity inputted.");
					return true; // No action.
				}

				Entity entity = entitySelectionList.get(selectedEntity).apply(MyUtils.clamp(-distLevel, 0, 3));
				World.levels[World.lvlIdx(distLevel)].add(entity, distX, distY);
				Logging.WORLDNAMED.info("Summoned a {}.", selectedEntity.toUpperCase());
				return true;
			}, display -> entitySelEntry.isValid() && levelOption.isValid() && coordinatesOption.isAllInputValid(),
				Arrays.asList(optionEntry, optionEntry1, optionEntry2)));
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
			builder = new Menu.Builder(true, 2, RelPos.CENTER)
				.setTitle("minicraft.display.debug_panel.command_popup");
			optionEntries.forEach(e -> entries.addAll(e.getEntries()));
			entries.add(actionEntry);
			Action checkUpdateListener = () -> {
				actionEntry.setSelectable(allowCheck == null || allowCheck.test(this));
				builder.setEntries(entries.stream().filter(ListEntry::isVisible).collect(Collectors.toList()));
			};
			optionEntries.forEach(e -> e.setCheckUpdateListener(checkUpdateListener));
			checkUpdateListener.act(); // First call
			menus = new Menu[] { builder.createMenu() };
			this.onCancel = onCancel;
		}

		@Override
		public void tick(InputHandler input) {
			if (input.getMappedKey("EXIT").isClicked()) {
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
			if (delegateEntry instanceof UserMutable) {
				((UserMutable) delegateEntry).setChangeListener(v -> callCheckUpdateListener());
			}
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
}
