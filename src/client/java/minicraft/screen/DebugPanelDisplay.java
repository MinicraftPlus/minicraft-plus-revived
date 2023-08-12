package minicraft.screen;

import minicraft.core.Action;
import minicraft.core.Game;
import minicraft.core.Updater;
import minicraft.core.World;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Settings;
import minicraft.core.io.Sound;
import minicraft.entity.Arrow;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.FireSpark;
import minicraft.entity.ItemEntity;
import minicraft.entity.Spark;
import minicraft.entity.furniture.DeathChest;
import minicraft.entity.furniture.Furniture;
import minicraft.entity.furniture.KnightStatue;
import minicraft.entity.mob.AirWizard;
import minicraft.entity.mob.Cow;
import minicraft.entity.mob.Creeper;
import minicraft.entity.mob.EnemyMob;
import minicraft.entity.mob.Knight;
import minicraft.entity.mob.Mob;
import minicraft.entity.mob.ObsidianKnight;
import minicraft.entity.mob.PassiveMob;
import minicraft.entity.mob.Pig;
import minicraft.entity.mob.Player;
import minicraft.entity.mob.Sheep;
import minicraft.entity.mob.Skeleton;
import minicraft.entity.mob.Slime;
import minicraft.entity.mob.Snake;
import minicraft.entity.mob.Zombie;
import minicraft.entity.particle.BurnParticle;
import minicraft.entity.particle.FireParticle;
import minicraft.entity.particle.SandParticle;
import minicraft.entity.particle.SmashParticle;
import minicraft.entity.particle.TextParticle;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.MinicraftImage;
import minicraft.gfx.Point;
import minicraft.gfx.Rectangle;
import minicraft.gfx.Screen;
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
import minicraft.screen.entry.BlankEntry;
import minicraft.screen.entry.BooleanEntry;
import minicraft.screen.entry.ChangeListener;
import minicraft.screen.entry.InputEntry;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.SelectEntry;
import minicraft.screen.entry.StringEntry;
import minicraft.screen.entry.UserMutable;
import minicraft.util.Logging;
import minicraft.util.MyUtils;
import org.intellij.lang.annotations.RegExp;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DebugPanelDisplay extends Display {
	@RegExp
	private static final String regexNumber = "[0-9]+";
	@RegExp
	private static final String regexNegNumber = "[0-9-]+";

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
			InputEntry durEntry = new InputEntry("Duration", regexNumber, 0) {
				private boolean specific = false;

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
			InputEntry countEntry = new InputEntry("Max Count", regexNumber, 0) {
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
			InputEntry dataEntry = new InputEntry("Data", regexNumber, 0, "0") {
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
			InputEntry dataEntry = new InputEntry("Data", regexNumber, 0, "0") {
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
				if (selectorEntry.isValid()) {
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
			InputEntry countEntry = new InputEntry("Count", regexNumber, 0) {
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

				int given;
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
						given = inventory.add(item, count);
						itemName = item.getName();
					}
				}

				Logging.WORLDNAMED.info("Gave {} * {}.", given, itemName);
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
		entitySelectionList.put("Cow", lvl -> new Cow());
		entitySelectionList.put("Creeper", Creeper::new);
		entitySelectionList.put("Knight", Knight::new);
		entitySelectionList.put("Pig", lvl -> new Pig());
		entitySelectionList.put("Sheep", lvl -> new Sheep());
		entitySelectionList.put("Skeleton", Skeleton::new);
		entitySelectionList.put("Slime", Slime::new);
		entitySelectionList.put("Snake", Snake::new);
		entitySelectionList.put("Zombie", Zombie::new);
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

	private static class LevelCoordinatesOption extends ListEntry implements UserMutable {
		private static final int INPUT_ENTRY_COUNT = 2; // x, y

		private final @Nullable String prompt;
		private final List<CoordinateInputEntry> inputs;

		private int selection = 0;

		public LevelCoordinatesOption(int w, int h, boolean isTile) { this(null, w, h, isTile); }
		public LevelCoordinatesOption(int w, int h, int x, int y, boolean isInputTile, boolean isTile) {
			this(null, w, h, x, y, isInputTile, isTile);
		}
		public LevelCoordinatesOption(@Nullable String prompt, int w, int h, boolean isTile) {
			this.prompt = prompt;
			inputs = Collections.unmodifiableList(isTile ? Arrays.asList(
				new TileCoordinateInputEntry("X", w),
				new TileCoordinateInputEntry("Y", h)
			) : Arrays.asList(
				new EntityCoordinateInputEntry("X", w),
				new EntityCoordinateInputEntry("Y", h)
			));
		}
		public LevelCoordinatesOption(@Nullable String prompt, int w, int h, int x, int y, boolean isInputTile, boolean isTile) {
			this.prompt = prompt;
			inputs = Collections.unmodifiableList(isTile ? Arrays.asList(
				new TileCoordinateInputEntry("X", w, x, isInputTile),
				new TileCoordinateInputEntry("Y", h, y, isInputTile)
			) : Arrays.asList(
				new EntityCoordinateInputEntry("X", w, x, isInputTile),
				new EntityCoordinateInputEntry("Y", h, y, isInputTile)
			));
		}

		private static abstract class CoordinateInputEntry extends InputEntry {
			public CoordinateInputEntry(String prompt, String regex, String initValue) {
				super(prompt, regex, 0, initValue);
			}

			public boolean isAllValid() {
				return isValid();
			}

			public abstract int getValue() throws IllegalArgumentException;
		}

		// Based on entity coordinate system
		private static class EntityCoordinateInputEntry extends CoordinateInputEntry {
			private final int bound;
			private final InputEntry minorInput;

			private boolean specified;
			private boolean minor;

			public EntityCoordinateInputEntry(String prompt, int bound, int initValue, boolean isTile) {
				this(prompt, bound, String.valueOf(isTile ? initValue : initValue / 16), !isTile, isTile ? "" : String.valueOf(initValue % 16));
			}
			/** Construct an entry with no default input. */
			public EntityCoordinateInputEntry(String prompt, int bound) {
				this(prompt, bound, "", false, "");
			}
			private EntityCoordinateInputEntry(String prompt, int bound, String initValue, boolean specified, String minorDefault) {
				super(prompt, regexNumber, initValue);
				this.bound = bound;
				this.specified = specified;
				minorInput = new InputEntry("", regexNumber, 0, minorDefault) {
					@Override
					public boolean isValid() {
						try {
							int value = Integer.parseInt(getUserInput());
							return value >= 0 && value < 16;
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
			}

			@Override
			public boolean isValid() {
				try {
					int value = Integer.parseInt(getUserInput());
					return value >= 0 && value < bound;
				} catch (NumberFormatException e) {
					return false;
				}
			}

			@Override
			public boolean isAllValid() {
				return super.isAllValid() && minorInput.isValid();
			}

			@Override
			public void tick(InputHandler input) {
				if (input.getKey("MINUS").clicked) {
					if (!specified) specified = true;
					minor = !minor;
				} else if (!minor) {
					super.tick(input);
				} else {
					minorInput.tick(input);
				}
			}

			@Override
			public void render(Screen screen, int x, int y, boolean isSelected) {
				String text = super.toString();
				String input = getUserInput();
				int padding = text.length() - input.length();
				Font.draw(text.substring(0, padding), screen, x, y, isSelected ? COL_SLCT : COL_UNSLCT);
				Font.draw(input, screen, x + padding * MinicraftImage.boxWidth, y,
					isValid() ? isSelected ? minor ? COL_SLCT : Color.GREEN : COL_UNSLCT : isSelected ? Color.RED : DARK_RED);
				String minorText = minorInput.getUserInput();
				if (specified) {
					Font.draw("-", screen, x += Font.textWidth(text), y, isSelected ? COL_SLCT : COL_UNSLCT);
					if (minorText.isEmpty()) minorText = "0";
					Font.draw(minorText, screen, x + MinicraftImage.boxWidth, y,
						minorInput.isValid() ? isSelected ? minor ? Color.GREEN : COL_SLCT : COL_UNSLCT : isSelected ? Color.RED : DARK_RED);
				}
			}

			@Override
			public void setChangeListener(ChangeListener l) {
				minorInput.setChangeListener(l);
				super.setChangeListener(l);
			}

			public int getValue() throws IllegalArgumentException {
				String input = getUserInput();
				if (input.isEmpty()) throw new IllegalArgumentException("input is empty");
				if (!isValid() || !minorInput.isValid()) throw new IllegalArgumentException("invalid input");
				try {
					return Integer.parseInt(input) * 16 + Integer.parseInt(minorInput.getUserInput());
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException(e);
				}
			}

			@Override
			public String toString() {
				return super.toString() + (specified ? "-" + minorInput.getUserInput() : "");
			}
		}

		// Based on tile coordinate system
		private static class TileCoordinateInputEntry extends CoordinateInputEntry {
			private final int bound;

			public TileCoordinateInputEntry(String prompt, int bound) {
				this(prompt, bound, "");
			}
			public TileCoordinateInputEntry(String prompt, int bound, int initValue, boolean isTile) {
				this(prompt, bound, String.valueOf(isTile ? initValue : initValue / 16));
			}
			private TileCoordinateInputEntry(String prompt, int bound, String initValue) {
				super(prompt, regexNumber, initValue);
				this.bound = bound;
			}

			@Override
			public boolean isValid() {
				try {
					int value = Integer.parseInt(getUserInput());
					return value >= 0 && value < bound;
				} catch (NumberFormatException e) {
					return false;
				}
			}

			public int getValue() throws IllegalArgumentException {
				String input = getUserInput();
				if (input.isEmpty()) throw new IllegalArgumentException("input is empty");
				if (!isValid()) throw new IllegalArgumentException("invalid input");
				try {
					return Integer.parseInt(input);
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException(e);
				}
			}
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

		public int getXValue() throws IllegalArgumentException {
			return inputs.get(0).getValue();
		}
		public int getYValue() throws IllegalArgumentException {
			return inputs.get(1).getValue();
		}

		public boolean isAllInputValid() {
			return inputs.stream().allMatch(CoordinateInputEntry::isAllValid);
		}

		@Override
		public void setChangeListener(ChangeListener listener) {
			inputs.forEach(i -> i.setChangeListener(listener));
		}

		@Override
		public String toString() {
			return (prompt == null ? "" : prompt + ": ") + inputs.stream().map(InputEntry::toString).collect(Collectors.joining("; "));
		}
	}

	// Integer 2-value vectors within a 2D Euclidean plane
	private static class Vector2ValueOption extends ListEntry implements UserMutable {
		private static final int INPUT_ENTRY_COUNT = 2; // x, y

		private final @Nullable String prompt;
		private final List<CoordinateInputEntry> inputs;

		private int selection = 0;

		public Vector2ValueOption(boolean isNeg, @Nullable Integer x, @Nullable Integer y) {
			this(null, isNeg, x, y);
		}
		public Vector2ValueOption(@Nullable String prompt, boolean isNeg, @Nullable Integer x, @Nullable Integer y) {
			this.prompt = prompt;
			inputs = Collections.unmodifiableList(Arrays.asList(
				new CoordinateInputEntry("X", isNeg, x == null ? "" : String.valueOf(x)),
				new CoordinateInputEntry("Y", isNeg, y == null ? "" : String.valueOf(y))
			));
		}

		private static class CoordinateInputEntry extends TargetedInputEntry<Integer> {
			public CoordinateInputEntry(String prompt, boolean isNeg, String initValue) {
				super(prompt, isNeg ? regexNegNumber : regexNumber, new TargetedValidator<>(null, input -> {
					try {
						return Integer.parseInt(input);
					} catch (NumberFormatException e) {
						return null;
					}
				}), initValue);
			}
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

		public int getXValue() throws IllegalArgumentException {
			return inputs.get(0).getValue();
		}
		public int getYValue() throws IllegalArgumentException {
			return inputs.get(1).getValue();
		}

		public boolean isAllInputValid() {
			return inputs.stream().allMatch(CoordinateInputEntry::isValid);
		}

		@Override
		public void setChangeListener(ChangeListener listener) {
			inputs.forEach(i -> i.setChangeListener(listener));
		}

		@Override
		public String toString() {
			return (prompt == null ? "" : prompt + ": ") + inputs.stream().map(InputEntry::toString).collect(Collectors.joining("; "));
		}
	}

	/** A type of input entry that focuses on returning a deserved type of value with the user input. */
	private static class TargetedInputEntry<T> extends InputEntry {
		protected static TargetedValidator<?> NOOP = new TargetedValidator<>(null, i -> null);

		@SuppressWarnings("unchecked")
		protected static <T> TargetedValidator<T> noOpValidator() {
			return (TargetedValidator<T>) NOOP;
		}

		public static class TargetedValidator<T> {
			public final @Nullable Predicate<String> validator;
			public final @NotNull Function<String, T> parser;

			/**
			 * In order to function precisely, the {@code validator} function must be compatible with the {@code parser} function;
			 * for all {@code string}, the following must hold:
			 * <pre>{@code
			 *     (parser.apply(string) != null) == validator.test(string)
			 * }</pre>
			 * @param validator A validator to validate user input with the desired input.
			 *                  If {@code null}, it checks whether the given {@code parser} returns {@code null} instead.
			 * @param parser A parser to parse and may validate the input.
			 */
			public TargetedValidator(@Nullable Predicate<String> validator, @NotNull Function<String, T> parser) {
				this.validator = validator;
				this.parser = parser;
			}
		}

		private final TargetedValidator<T> validator;

		public TargetedInputEntry(String prompt, @NotNull TargetedValidator<T> validator) { this(prompt, null, validator); }
		public TargetedInputEntry(String prompt, @RegExp String regex, @NotNull TargetedValidator<T> validator) {
			this(prompt, regex, validator, "");
		}
		public TargetedInputEntry(String prompt, @RegExp String regex, @NotNull TargetedValidator<T> validator, @NotNull String initValue) {
			super(prompt, regex, 0, initValue); // maxLen is not important
			this.validator = validator;
		}

		protected boolean hasValidator() {
			checkForValidator();
			return validator.validator != null;
		}

		protected boolean validate(String input) {
			checkForValidator();
			return validator.validator == null ? parse(input) != null : validator.validator.test(input);
		}

		protected @Nullable T parse(String input) {
			checkForValidator();
			return validator.parser.apply(input);
		}

		@Override
		public boolean isValid() {
			return super.isValid() && validate(getUserInput());
		}

		private void checkForValidator() {
			if (validator == NOOP)
				throw new SecurityException("NOOP is used without method overrides");
		}

		public T getValue() throws IllegalArgumentException {
			String input = getUserInput();
			boolean valid = false;
			if (!hasValidator() || (valid = validate(input))) {
				T value = parse(input);
				if (value != null) return value;
				else if (valid)
					throw new SecurityException("corrupted validator parser");
			}

			throw new IllegalArgumentException("value is invalid");
		}
	}

	private static class LevelSelectionOption extends TargetedInputEntry<Integer> {
		private static final int[] levelDepths;
		static {
			levelDepths = World.idxToDepth.clone();
			Arrays.sort(levelDepths);
		}

		private final int defaultLevel;

		private boolean typing = false; // Typing or selecting
		private int selection;

		public LevelSelectionOption() { this(null); }
		public LevelSelectionOption(@Nullable Integer level) {
			super("Level", regexNegNumber, noOpValidator(), level == null ? "" : String.valueOf(level));
			this.defaultLevel = level == null ? 0 : level;
			selection = Arrays.binarySearch(levelDepths, defaultLevel);
		}

		@Override
		public void tick(InputHandler input) {
			if (input.getKey("ENTER").clicked) {
				if (typing) {
					if (isValid()) { // Only to change when the input is valid.
						int level = getValue();
						selection = Arrays.binarySearch(levelDepths, level);
						typing = false;
					}
				} else // !typing
					typing = true;
			} else if (!typing) {
				if (input.getKey("CURSOR-LEFT").clicked) {
					selection = Math.max(selection - 1, 0);
					Sound.play("select");
					setUserInput(String.valueOf(levelDepths[selection]));
				} else if (input.getKey("CURSOR-RIGHT").clicked) {
					selection = Math.min(selection + 1, levelDepths.length - 1);
					Sound.play("select");
					setUserInput(String.valueOf(levelDepths[selection]));
				}
			} else
				super.tick(input);
		}

		@Override
		protected boolean hasValidator() {
			return true;
		}

		@Override
		protected boolean validate(String input) {
			try {
				if (input.isEmpty()) return true; // Default level
				return Arrays.binarySearch(levelDepths, Integer.parseInt(input)) >= 0;
			} catch (NumberFormatException e) {
				return false;
			}
		}

		@Override
		protected @Nullable Integer parse(String input) {
			try {
				if (input.isEmpty()) return defaultLevel; // Default level
				int val = Integer.parseInt(input);
				return Arrays.binarySearch(levelDepths, val) < 0 ? null : val; // Non-null if input is valid
			} catch (NumberFormatException e) {
				return null;
			}
		}

		@Override
		public void render(Screen screen, int x, int y, boolean isSelected) {
			if (!isSelected) typing = false; // TODO There should be #tick(..., isSelected) to replace this line.
			Font.draw(isSelected && typing ? super.toString() : toString(), screen, x, y,
				isValid() ? isSelected ? Color.GREEN : COL_UNSLCT : isSelected ? Color.RED : DARK_RED);
		}

		@Override
		public String toString() {
			try {
				int depth = getValue();
				return "Level: " + Level.getDepthString(depth) + " (" + Level.getLevelName(depth) + ")";
			} catch (IllegalArgumentException e) {
				return super.toString();
			}
		}
	}

	private static class UnionEntry<T extends ListEntry> extends ListEntry {
		private final List<T> entries;

		private int selection = 0;

		@SafeVarargs
		public UnionEntry(T... entries) {
			this.entries = Arrays.asList(entries);
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

	private static class SelectableListInputEntry extends InputEntry {
		private final String prompt;
		private final List<String> list;
		private final @Nullable String placeholder;

		public SelectableListInputEntry(String prompt, Collection<String> list) { this(prompt, list, null); }
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
			if (input.getKey("SELECT").clicked) {
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

	private static class ListItemSelectDisplay<T> extends Display {
		private final Consumer<T> callback;

		public static class ListItemHandler<T> {
			public final @NotNull Function<T, String> stringifier;
			public final @Nullable Predicate<T> itemFilter;
			public final boolean removeFiltered;

			public ListItemHandler() { this(Objects::toString); }
			public ListItemHandler(@NotNull Function<T, String> stringifier) { this(stringifier, null); }
			public ListItemHandler(@NotNull Function<T, String> stringifier, @Nullable Predicate<T> itemFilter) {
				this(stringifier, itemFilter, true);
			}
			public ListItemHandler(@NotNull Function<T, String> stringifier, @Nullable Predicate<T> itemFilter, boolean removeFiltered) {
				this.stringifier = stringifier;
				this.itemFilter = itemFilter;
				this.removeFiltered = removeFiltered;
			}
		}

		public ListItemSelectDisplay(T[] list, Consumer<T> callback) { this(Arrays.asList(list), new ListItemHandler<>(), callback); }
		public ListItemSelectDisplay(List<T> list, Consumer<T> callback) { this(list, new ListItemHandler<>(), callback); }
		public ListItemSelectDisplay(T[] list, @NotNull ListItemHandler<T> itemHandler, Consumer<T> callback) {
			this(Arrays.asList(list), itemHandler, callback);
		}
		public ListItemSelectDisplay(List<T> list, @NotNull ListItemHandler<T> itemHandler, Consumer<T> callback) {
			this.callback = callback;
			menus = new Menu[] {
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

	private static class TargetSelectorEntry extends ArrayEntry<TargetSelectorEntry.TargetScope> {
		// UPV == UnParsable Value
		private static final String UPV_STRING = Color.RED_CODE + "UPV" + Color.WHITE_CODE + Color.GRAY_CODE;
		private static final int DARK_RED = Color.tint(Color.RED, -1, true);
		private static final int DARK_GREEN = Color.tint(Color.GREEN, -1, true);
		private static final int DARK_BLUE = Color.tint(Color.BLUE, -1, true);
		private static final Predicate<Entity> NOOP_FILTER = e -> true;

		private enum TargetScope {
			Player(true, e -> e instanceof Player), // There is only Game#player available as of now, so this specifies the player itself.
			Entity(false, e -> true); // All entities

			public final boolean single;
			public final Predicate<Entity> filter;

			TargetScope(boolean single, Predicate<Entity> filter) {
				this.single = single;
				this.filter = filter;
			}
		}

		private static String formatPosition(int position) {
			return (position / 16) + "-" + (position % 16);
		}

		private final TargetSelectorConfigDisplay configDisplay;

		public TargetSelectorEntry(Player player) {
			super("Target", true, false, TargetScope.Player, TargetScope.Entity);
			configDisplay = new TargetSelectorConfigDisplay(player);
		}

		private class TargetSelectorConfigDisplay extends Display {
			private final Menu.Builder builder;
			private final PositionArgument positionArgument;
			private final FilterArgument filterArgument;
			private final CollectionArgument collectionArgument;

			public TargetSelectorConfigDisplay(Player player) {
				positionArgument = new PositionArgument(player.getLevel(), player.x, player.y);
				filterArgument = new FilterArgument();
				collectionArgument = new CollectionArgument();
				builder = new Menu.Builder(true, 2, RelPos.CENTER,
					positionArgument.getEntry(), filterArgument.getEntry(), collectionArgument.getEntry())
					.setPositioning(new Point(Screen.w / 2, Screen.h / 2), RelPos.CENTER);
				menus = new Menu[1];
				update();
			}

			private void update() {
				int oldSel = menus[0] == null ? 0 : menus[0].getSelection();
				menus[0] = builder.createMenu();
				menus[0].setSelection(oldSel);
			}

			public boolean isValid() {
				return positionArgument.isAllValid() && filterArgument.isAllValid() && collectionArgument.isAllValid() &&
					(!getValue().single || !filterArgument.isSet()); // Single target cannot apply filter
			}

			public Collection<Entity> collectTargets() {
				assert isValid(); // This method should be called after checked of isValid.
				if (getValue() == TargetScope.Player) return Collections.singleton(Game.player);
				// TargetScope#Entity
				LinkedHashSet<Entity> entities = new LinkedHashSet<>();
				if (positionArgument.isSpecified())
					entities.addAll(Arrays.asList(World.levels[World.lvlIdx(positionArgument.getLevelValue())].getEntityArray()));
				else {
					for (Level level : World.levels) {
						entities.addAll(Arrays.asList(level.getEntityArray()));
					}
				}

				// The effect of level depths on distance is undefined, so this is not taken into account here.
				int localX = positionArgument.getXValue();
				int localY = positionArgument.getYValue();
				Comparator<Entity> comparator;
				switch (collectionArgument.getSort()) {
					case Nearest: comparator = Comparator.comparingDouble(e -> Math.hypot(localX - e.x, localY - e.y)); break;
					case Furthest: comparator = Comparator.<Entity>comparingDouble(e -> Math.hypot(localX - e.x, localY - e.y)).reversed(); break;
					case Random: // Reference: https://stackoverflow.com/a/40380283
						final Map<Object, UUID> uniqueIds = new IdentityHashMap<>();
						comparator = Comparator.comparing(e -> uniqueIds.computeIfAbsent(e, k -> UUID.randomUUID()));
						break;
					default: // Reference: https://stackoverflow.com/a/70007141
					case Arbitrary: comparator = (a, b) -> 0; // NO-OP; Keeps it as the original order as added with LinkedHashSet.
				}
				Stream<Entity> stream = entities.stream().filter(filterArgument.getFilter(positionArgument))
					.sorted(comparator);
				if (collectionArgument.isLimitEnabled()) try {
					stream = stream.limit(collectionArgument.getLimit());
				} catch (IllegalArgumentException e) {
					Logging.WORLDNAMED.error(e, "Invalid (unchecked and unexpected) limit value");
					return Collections.emptySet();
				}

				return stream.collect(Collectors.toList());
			}
		}

		private static class PositionArgument {

			private final int defaultDepth, defaultX, defaultY;
			private final PositionSelectDisplay display;
			private final SelectEntry entry;
			private boolean depthValid = true, xValid = true, yValid = true;
			private @Nullable Integer depth, x, y;
			private boolean specify = false;

			public PositionArgument(Level level, int x, int y) {
				this.defaultDepth = level.depth;
				this.defaultX = x;
				this.defaultY = y;
				display = new PositionSelectDisplay(level);
				entry = new SelectEntry("Position", () -> Game.setDisplay(display), false) {

					@Override
					public String toString() {
						return String.format("Position: (%s, %s, %s)",
							depthValid ? depth == null ? formatPosition(defaultDepth) :
								Color.BLUE_CODE + formatPosition(depth) + Color.WHITE_CODE + Color.GRAY_CODE : UPV_STRING,
							xValid ? PositionArgument.this.x == null ? formatPosition(defaultX) :
								Color.BLUE_CODE + formatPosition(PositionArgument.this.x) + Color.WHITE_CODE + Color.GRAY_CODE : UPV_STRING,
							yValid ? PositionArgument.this.y == null ? formatPosition(defaultY) :
								Color.BLUE_CODE + formatPosition(PositionArgument.this.y) + Color.WHITE_CODE + Color.GRAY_CODE : UPV_STRING
						);
					}
				};
			}

			private class PositionSelectDisplay extends Display {
				private final Menu.Builder builder;
				private final BooleanEntry specifyOption; // If unspecified, all entities are selected unless there is any range argument used.
				private final LevelSelectionOption levelOption; // If level is not specified, all entities in the world are selected.
				private final LevelCoordinatesOption positionOption; // If level is specified, the position is also used.

				public PositionSelectDisplay(Level level) {
					specifyOption = new BooleanEntry("Specify", false);
					levelOption = new LevelSelectionOption();
					positionOption = new LevelCoordinatesOption(level.w, level.h, false);
					builder = new Menu.Builder(true, 2, RelPos.CENTER, levelOption, positionOption)
						.setPositioning(new Point(Screen.w / 2, Screen.h / 2), RelPos.CENTER)
						.setTitle("Position");
					menus = new Menu[1];
					ChangeListener l = v -> onUpdate();
					specifyOption.setChangeListener(l);
					levelOption.setChangeListener(l);
					positionOption.setChangeListener(l);
					onUpdate();
				}

				private void onUpdate() {
					ArrayList<ListEntry> entries = new ArrayList<>();
					entries.add(specifyOption);
					if (specifyOption.getValue()) {
						entries.add(levelOption);
						entries.add(positionOption);
					}
					int oldSel = menus[0] == null ? 0 : menus[0].getSelection();
					menus[0] = builder.setEntries(entries).createMenu();
					menus[0].setSelection(oldSel);
				}

				@Override
				public void tick(InputHandler input) {
					if (input.getKey("EXIT").clicked) {
						if (specifyOption.getValue()) {
							specify = true;
							try {
								depth = levelOption.getValue();
								depthValid = true;
							} catch (IllegalArgumentException e) {
								depth = null;
								depthValid = false;
							}

							try {
								x = positionOption.getXValue();
								xValid = true;
							} catch (IllegalArgumentException e) {
								x = null;
								xValid = false;
							}

							try {
								y = positionOption.getYValue();
								yValid = true;
							} catch (IllegalArgumentException e) {
								y = null;
								yValid = false;
							}
						} else {
							specify = false;
							depth = null;
							x = null;
							y = null;
							depthValid = true;
							xValid = true;
							yValid = true;
						}

						Game.exitDisplay();
						return;
					}

					super.tick(input);
				}

				@Override
				public void init(@Nullable Display parent) {
					super.init(parent);
					menus[0].setSelection(0); // Reset selection
				}
			}

			public boolean isAllValid() {
				return depthValid && xValid && yValid;
			}

			public SelectEntry getEntry() {
				return entry;
			}

			public boolean isSpecified() {
				return specify;
			}

			public int getLevelValue() {
				return depth == null ? defaultDepth : depth;
			}
			public int getXValue() {
				return x == null ? defaultX : x;
			}
			public int getYValue() {
				return y == null ? defaultY : y;
			}
		}

		private static class FilterArgument {
			private final FilterSettingDisplay display;
			private final SelectEntry entry;
			private final LinkedHashMap<FilterOption, FilterOption.FilterOptionEntry> argumentOptions = new LinkedHashMap<>();

			public FilterArgument() {
				display = new FilterSettingDisplay();
				entry = new SelectEntry("Filter", () -> Game.setDisplay(display), false) {
					@Override
					public String toString() {
						long v;
						return argumentOptions.isEmpty() ? "Filter(s) ..." : String.format("Filter(s) (%s)", argumentOptions.size() +
							((v = argumentOptions.values().stream().filter(e -> !e.isFilterValid()).count()) > 0 ?
								";" + Color.RED_CODE + v + " " + Color.WHITE_CODE + Color.GRAY_CODE + UPV_STRING : ""));
					}
				};
			}

			private class FilterSettingDisplay extends Display {
				private final StringEntry placeholderEntry = new StringEntry("No Filter Selected", Color.GRAY, false);
				private final SelectEntry actionEntry;
				private final Menu.Builder builder;

				public FilterSettingDisplay() {
					actionEntry = new SelectEntry("Add Filter",
						() -> Game.setDisplay(new ListItemSelectDisplay<>(FilterOption.values(),
							new ListItemSelectDisplay.ListItemHandler<>(o -> o.name, o -> !argumentOptions.containsKey(o)), this::addFilter))) {
						@Override
						public int getColor(boolean isSelected) {
							return isSelectable() ? super.getColor(isSelected) : Color.DARK_GRAY;
						}
					};
					builder = new Menu.Builder(true, 2, RelPos.CENTER)
						.setPositioning(new Point(Screen.w / 2, Screen.h / 2), RelPos.CENTER);
					menus = new Menu[1];
					update();
				}

				private void update() {
					ArrayList<ListEntry> entries = new ArrayList<>();
					if (argumentOptions.isEmpty())
						entries.add(placeholderEntry);
					else
						argumentOptions.values().forEach(e -> entries.add(e.getEntry()));
					entries.add(new BlankEntry());
					actionEntry.setSelectable(Arrays.stream(FilterOption.values()).anyMatch(o -> !argumentOptions.containsKey(o)));
					entries.add(actionEntry);
					menus[0] = builder.setEntries(entries).createMenu();
				}

				private void addFilter(FilterOption option) {
					FilterOption.FilterOptionEntry entry = option.getEntry();;
					argumentOptions.put(option, entry);
					entry.setChangeListener(v -> update());
					update();
				}
			}

			private enum FilterOption {
				Distance("Distance") { // Value based on the smallest unit in entity coordinate system.
					@Override
					public @NotNull FilterOptionEntry getEntry() {
						return new DistanceOptionEntry();
					}

					class DistanceOptionEntry extends InputEntry implements FilterOptionEntry {
						private final InputEntry rangedInput = new InputEntry("", regexNumber, 0);
						private int selection = 0; // Maybe this should be boolean?
						private boolean ranged = false;

						public DistanceOptionEntry() {
							super("Distance", regexNumber, 0);
						}

						@Override
						public void tick(InputHandler input) {
							if (input.getKey("ENTER").clicked) {
								ranged = !ranged;
								if (selection == 1) selection = 0;
							} else if (input.getKey("CURSOR-LEFT").clicked) {
								if (selection == 1) selection = 0;
								Sound.play("select");
							} else if (input.getKey("CURSOR-RIGHT").clicked) {
								if (ranged) {
									if (selection == 0) selection = 1;
									Sound.play("select");
								}
							} else {
								if (selection == 1) rangedInput.tick(input);
								else super.tick(input);
							}
						}

						@Override
						public boolean isValid() {
							String input = getUserInput();
							if (!ranged) {
								if (input.isEmpty()) return false;
								try {
									return Integer.parseInt(input) >= 0;
								} catch (NumberFormatException e) {
									return false;
								}
							} else {
								String range = rangedInput.getUserInput();
								if (range.isEmpty() && input.isEmpty()) return false;
								if (range.isEmpty()) try {
									return Integer.parseInt(input) >= 0;
								} catch (NumberFormatException e) {
									return false;
								} else if (input.isEmpty()) try {
									return Integer.parseInt(range) >= 0;
								} catch (NumberFormatException e) {
									return false;
								} else {
									try {
										int a = Integer.parseInt(input);
										int b = Integer.parseInt(input);
										return a >= 0 && b >= a;
									} catch (NumberFormatException e) {
										return false;
									}
								}
							}
						}

						@Override
						public @NotNull Predicate<Entity> getFilter(PositionArgument positionArgument) {
							if (!isValid()) return NOOP_FILTER;
							if (!ranged) {
								int value;
								try {
									value = Integer.parseInt(getUserInput());
								} catch (NumberFormatException e) { // #isValid should have already handled this.
									return NOOP_FILTER;
								}

								return e -> positionArgument.getLevelValue() == e.getLevel().depth && // Same level
									Math.hypot(positionArgument.getXValue() - e.x, positionArgument.getYValue()) == value;
							} else {
								String input = getUserInput();
								String rInput = rangedInput.getUserInput();
								int value, range;
								// This should have checked that there should be at least one of them non-empty.
								assert !(input.isEmpty() && rInput.isEmpty()); // by #isValid
								if (input.isEmpty()) { // ..range
									try {
										range = Integer.parseInt(rInput);
									} catch (NumberFormatException e) { // #isValid should have already handled this.
										return NOOP_FILTER;
									}

									return e -> positionArgument.getLevelValue() == e.getLevel().depth && // Same level
										Math.hypot(positionArgument.getXValue() - e.x, positionArgument.getYValue()) <= range;
								} else if (rInput.isEmpty()) { // input..
									try {
										value = Integer.parseInt(input);
									} catch (NumberFormatException e) { // #isValid should have already handled this.
										return NOOP_FILTER;
									}

									return e -> positionArgument.getLevelValue() == e.getLevel().depth && // Same level
										Math.hypot(positionArgument.getXValue() - e.x, positionArgument.getYValue()) >= value;
								} else {
									try {
										range = Integer.parseInt(rInput);
									} catch (NumberFormatException e) { // #isValid should have already handled this.
										return NOOP_FILTER;
									}

									try {
										value = Integer.parseInt(input);
									} catch (NumberFormatException e) { // #isValid should have already handled this.
										return NOOP_FILTER;
									}

									return e -> {
										double d;
										return positionArgument.getLevelValue() == e.getLevel().depth && // Same level
											(d = Math.hypot(positionArgument.getXValue() - e.x, positionArgument.getYValue())) >= value &&
											d <= range;
									};
								}
							}
						}

						@Override
						public @NotNull ListEntry getEntry() {
							return this;
						}

						@Override
						public boolean isFilterValid() {
							return isValid();
						}
					}
				},
				// Entity hit boxes are rectangular, and this argument filters entities that their hit boxes are "touched" by the ranges.
				DimensionRange("Dimension Range") { // Value based on the smallest unit in entity coordinate system.
					@Override
					public @NotNull FilterOptionEntry getEntry() {
						return new RangeOptionEntry();
					}

					class RangeOptionEntry extends Vector2ValueOption implements FilterOptionEntry {
						public RangeOptionEntry() {
							super("Dimension Range", true, null, null);
						}

						@Override
						public @NotNull Predicate<Entity> getFilter(PositionArgument positionArgument) {
							Integer x, y;
							try {
								x = getXValue();
							} catch (IllegalArgumentException e) {
								x = null;
							}

							try {
								y = getYValue();
							} catch (IllegalArgumentException e) {
								y = null;
							}

							if (x == null && y == null) return NOOP_FILTER;
							else {
								if (x == null) x = 0;
								if (y == null) y = 0;
								int ax = positionArgument.getXValue();
								int bx = ax + x;
								int minX = Math.min(ax, bx);
								int maxX = Math.max(ax, bx);
								int ay = positionArgument.getYValue();
								int by = ay + y;
								int minY = Math.min(ay, by);
								int maxY = Math.max(ay, by);
								return e -> positionArgument.getLevelValue() == e.getLevel().depth && // Same level
									e.isTouching(new Rectangle(minX, minY, maxX, maxY, Rectangle.CORNERS));
							}
						}

						@Override
						public @NotNull ListEntry getEntry() {
							return this;
						}

						@Override
						public boolean isFilterValid() {
							return isAllInputValid();
						}
					}
				},
				Rotation("Rotation") { // This ambiguously specifies mobs as only mobs have this property.
					@Override
					public @NotNull FilterOptionEntry getEntry() {
						return new RotationOptionEntry();
					}

					class RotationOptionEntry extends ArrayEntry<Direction> implements FilterOptionEntry {
						public RotationOptionEntry() {
							super("Rotation", true, false,
								Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT);
						}

						@Override
						public @NotNull Predicate<Entity> getFilter(PositionArgument positionArgument) {
							return e -> e instanceof Mob && ((Mob) e).dir == getValue();
						}

						@Override
						public @NotNull ListEntry getEntry() {
							return this;
						}

						@Override
						public boolean isFilterValid() {
							return true;
						}
					}
				},
				Name("Name") {
					@Override
					public @NotNull FilterOptionEntry getEntry() {
						return new NameOptionEntry();
					}

					class NameOptionEntry extends InputEntry implements FilterOptionEntry {
						public NameOptionEntry() {
							super("Name");
						}

						@Override
						public @NotNull Predicate<Entity> getFilter(PositionArgument positionArgument) {
							return e -> getUserInput().equalsIgnoreCase(e instanceof Furniture ? ((Furniture) e).name :
								entityNames.get(e.getClass()));
						}

						@Override
						public boolean isValid() {
							return super.isValid() && !getUserInput().isEmpty();
						}

						@Override
						public @NotNull ListEntry getEntry() {
							return this;
						}

						@Override
						public boolean isFilterValid() {
							return isValid();
						}
					}
				},
				Type("Type") {
					@Override
					public @NotNull FilterOptionEntry getEntry() {
						return new TypeOptionEntry();
					}

					class TypeOptionEntry extends ListEntry implements FilterOptionEntry {
						private final EnumMap<EntityType, Boolean> typeList = new EnumMap<>(EntityType.class);
						private final Display display;

						private ChangeListener listener;

						public TypeOptionEntry() {
							for (EntityType type : EntityType.values()) {
								typeList.put(type, null);
							}

							display = new Display(new Menu.Builder(true, 2, RelPos.CENTER,
								typeList.keySet().stream().map(t -> new ListEntry() {
									@Override
									public void tick(InputHandler input) {
										if (input.getKey("ENTER").clicked) {
											typeList.compute(t, (ty, v) -> // null -> true -> false -> null
												v == null ? true : v ? false : null);
										}
									}

									@Override
									public int getColor(boolean isSelected) {
										Boolean s = typeList.get(t);
										if (s == null) return super.getColor(isSelected);
										return s ? isSelected ? Color.GREEN : DARK_GREEN :
											isSelected ? Color.BLUE : DARK_BLUE;
									}

									@Override
									public String toString() {
										return t.name;
									}
								}).collect(Collectors.toList()))
								.setPositioning(new Point(Screen.w / 2, Screen.h / 2), RelPos.CENTER)
								.createMenu()) {
								@Override
								public void tick(InputHandler input) {
									if (input.getKey("SELECT").clicked) {
										listener.onChange(null);
										Game.exitDisplay();
										return;
									}

									super.tick(input);
								}
							};
						}

						@Override
						public @NotNull Predicate<Entity> getFilter(PositionArgument positionArgument) {
							return typeList.entrySet().stream().reduce(e -> true,
								(a, b) -> b.getValue() == null ? a : a.and(b.getValue() ? b.getKey().filter : b.getKey().filter.negate()), Predicate::and);
						}

						@Override
						public @NotNull ListEntry getEntry() {
							return this;
						}

						@Override
						public boolean isFilterValid() {
							return true;
						}

						@Override
						public void tick(InputHandler input) {
							if (input.getKey("SELECT").clicked) {
								Sound.play("select");
								Game.setDisplay(display);
							}
						}

						@Override
						public String toString() {
							if (typeList.values().stream().allMatch(Objects::isNull)) {
								return "Type: All";
							} else {
								Stream<Map.Entry<EntityType, Boolean>> s;
								if ((s = typeList.entrySet().stream().filter(Objects::nonNull)).count() == 1) {
									Map.Entry<EntityType, Boolean> e;
									return "Type: " + ((e = s.findAny().get()).getValue() ? "" : "Not ") + e.getKey().name;
								} else { // At least 1 of them > 0
									// WL == WhiteList, BL == BlackList
									Stream<Boolean> stream = typeList.values().stream();
									long wl = stream.filter(e -> e != null && e).count();
									long bl = stream.filter(e -> e != null && !e).count();
									return String.format("Type: %s",
										wl == 0 ? bl + "BL" : bl == 0 ? wl + "WL" : String.format("%sWL;%sBL", wl, bl));
								}
							}
						}

						@Override
						public void setChangeListener(ChangeListener listener) {
							this.listener = listener;
						}
					}
				};

				private static final HashMap<Class<? extends Entity>, String> entityNames = new HashMap<>();

				static {
					// Reference: https://stackoverflow.com/a/56087201
					UnaryOperator<String> nameSeparator = str -> {
						StringBuilder sb = new StringBuilder();
						sb.append(str.charAt(0)); // Assume str#length() > 0
						for (int i = 1; i < str.length(); i++) {
							char c = str.charAt(i);
							if (Character.isUpperCase(c)) sb.append(" ");
							sb.append(c);
						}
						return sb.toString();
					};

					List<Class<? extends Entity>> classes = Arrays.asList(
						// Furniture not in FurnitureItem
						DeathChest.class,
						KnightStatue.class,
						// Mob
						AirWizard.class,
						Cow.class,
						Creeper.class,
						Knight.class,
						ObsidianKnight.class,
						Pig.class,
						Player.class,
						Sheep.class,
						Skeleton.class,
						Slime.class,
						Snake.class,
						Zombie.class,
						// Particle
						BurnParticle.class,
						FireParticle.class,
						SandParticle.class,
						SmashParticle.class,
						TextParticle.class,
						// Other entities
						Arrow.class,
						FireSpark.class,
						Spark.class
					);
					classes.forEach(c -> entityNames.put(c, nameSeparator.apply(c.getSimpleName())));
				}

				private enum EntityType {
					Furniture("Furniture", e -> e instanceof Furniture),
					Chest("Chest", e -> e instanceof minicraft.entity.furniture.Chest),
					Lantern("Lantern", e -> e instanceof minicraft.entity.furniture.Lantern),
					Spawner("Spawner", e -> e instanceof minicraft.entity.furniture.Spawner),
					Boss("Boss", e -> e instanceof AirWizard || e instanceof ObsidianKnight),
					Enemy("Enemy", e -> e instanceof EnemyMob), // Or hostile
					Passive("Passive", e -> e instanceof PassiveMob),
					Mob("Mob", e -> e instanceof Mob),
					MobAi("Mob with AI", e -> e instanceof minicraft.entity.mob.MobAi),
					Player("Player", e -> e instanceof Player),
					Particle("Particle", e -> e instanceof minicraft.entity.particle.Particle),
					Item("Item", e -> e instanceof ItemEntity),
					Spark("Spark", e -> e instanceof Spark || e instanceof FireSpark),
					Arrow("Arrow", e -> e instanceof Arrow),
					Projectile("Projectile", e -> e instanceof Arrow || e instanceof Spark || e instanceof FireSpark);

					public final String name;
					public final Predicate<Entity> filter;

					EntityType(String name, Predicate<Entity> filter) {
						this.name = name;
						this.filter = filter;
					}
				}

				public final String name;

				FilterOption(@NotNull String name) {
					this.name = name;
				}

				/** Constructs a new entry instance for this argument option. */
				@NotNull
				public abstract FilterOptionEntry getEntry();

				public interface FilterOptionEntry extends UserMutable {
					@NotNull Predicate<Entity> getFilter(PositionArgument positionArgument);

					/** The entry representing this option. */
					@NotNull ListEntry getEntry();

					boolean isFilterValid();
				}
			}

			public Predicate<Entity> getFilter(PositionArgument positionArgument) {
				return argumentOptions.values().stream().reduce(NOOP_FILTER, (a, b) -> a.and(b.getFilter(positionArgument)), Predicate::and);
			}

			public boolean isSet() {
				return argumentOptions.size() > 0;
			}

			public SelectEntry getEntry() {
				return entry;
			}

			public boolean isAllValid() {
				return argumentOptions.values().stream().allMatch(FilterOption.FilterOptionEntry::isFilterValid);
			}
		}

		// Order and Bound
		private static class CollectionArgument {
			private final InputEntry limitEntry;
			private final ArrayEntry<SortType> sortEntry;
			private final BooleanEntry limitEnabled;
			private final BooleanEntry sortEnabled;
			private final SelectEntry entry;
			private final Display display;

			public enum SortType {
				Nearest, Furthest, Random, Arbitrary
			}

			public CollectionArgument() {
				limitEnabled = new BooleanEntry("Enable Limit", false);
				sortEnabled = new BooleanEntry("Enable Sort", false);
				limitEntry = new InputEntry("Limit", regexNumber, 0) {
					@Override
					public boolean isValid() {
						try {
							return Integer.parseInt(getUserInput()) >= 1;
						} catch (NumberFormatException e) {
							return false;
						}
					}
				};
				sortEntry = new ArrayEntry<>("Sort", true, false, SortType.values());
				Menu.Builder builder = new Menu.Builder(true, 2, RelPos.CENTER)
					.setPositioning(new Point(Screen.w / 2, Screen.h / 2), RelPos.CENTER);
				display = new Display() {
					{
						menus = new Menu[1];
						ChangeListener listener = v -> update();
						limitEntry.setChangeListener(listener);
						sortEntry.setChangeListener(listener);
						limitEnabled.setChangeListener(listener);
						sortEnabled.setChangeListener(listener);
						update();
					}

					private void update() {
						ArrayList<ListEntry> entries = new ArrayList<>();
						entries.add(limitEnabled);
						entries.add(sortEnabled);
						if (limitEnabled.getValue()) entries.add(limitEntry);
						if (sortEnabled.getValue()) entries.add(sortEntry);
						int oldSel = menus[0] == null ? 0 : menus[0].getSelection();
						menus[0] = builder.setEntries(entries).createMenu();
						menus[0].setSelection(oldSel);
					}
				};
				entry = new SelectEntry("Order and Bound", () -> Game.setDisplay(display), false) {
					@Override
					public String toString() {
						return super.toString() + (limitEntry.isValid() ? "" : " (Order " + UPV_STRING + ")");
					}
				};
			}

			public boolean isAllValid() {
				return !limitEnabled.getValue() || limitEntry.isValid();
			}

			public SelectEntry getEntry() {
				return entry;
			}

			public boolean isSortEnabled() {
				return sortEnabled.getValue();
			}
			public boolean isLimitEnabled() {
				return limitEnabled.getValue();
			}

			public int getLimit() throws IllegalArgumentException {
				try {
					int value = Integer.parseInt(limitEntry.getUserInput());
					if (value < 1) throw new IllegalArgumentException("value invalid");
					return value;
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException(e);
				}
			}
			public SortType getSort() {
				return sortEnabled.getValue() ? sortEntry.getValue() : SortType.Arbitrary;
			}
		}

		public boolean isValid() {
			return configDisplay.isValid();
		}

		public Collection<Entity> collectTargets() {
			return configDisplay.collectTargets();
		}

		@Override
		public int getColor(boolean isSelected) {
			return isValid() ? super.getColor(isSelected) : isSelected ? Color.RED : DARK_RED;
		}

		@Override
		public void tick(InputHandler input) {
			if (input.getKey("SELECT").clicked) {
				Sound.play("confirm");
				Game.setDisplay(configDisplay);
			} else {
				super.tick(input);
			}
		}
	}
}
