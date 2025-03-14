package minicraft.screen.entry.commands;

import minicraft.core.Game;
import minicraft.core.World;
import minicraft.core.io.InputHandler;
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
import minicraft.gfx.Point;
import minicraft.gfx.Rectangle;
import minicraft.gfx.Screen;
import minicraft.level.Level;
import minicraft.screen.Display;
import minicraft.screen.ListItemSelectDisplay;
import minicraft.screen.Menu;
import minicraft.screen.RelPos;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractList;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TargetSelectorEntry extends ArrayEntry<TargetSelectorEntry.TargetScope> {
	// UPV == UnParsable Value
	private static final String UPV_STRING = Color.RED_CODE + "UPV" + Color.WHITE_CODE + Color.GRAY_CODE;
	private static final int DARK_RED = Color.tint(Color.RED, -1, true);
	private static final int DARK_GREEN = Color.tint(Color.GREEN, -1, true);
	private static final int DARK_BLUE = Color.tint(Color.BLUE, -1, true);
	private static final Predicate<Entity> NOOP_FILTER = e -> true;

	public enum TargetScope {
		Player(true, e -> e instanceof minicraft.entity.mob.Player), // There is only Game#player available as of now, so this specifies the player itself.
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

		public void setChangeListener(ChangeListener listener) {
			positionArgument.setChangeListener(listener);
			filterArgument.setChangeListener(listener);
			collectionArgument.setChangeListener(listener);
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
				case Nearest:
					comparator = Comparator.comparingDouble(e -> Math.hypot(localX - e.x, localY - e.y));
					break;
				case Furthest:
					comparator = Comparator.<Entity>comparingDouble(e -> Math.hypot(localX - e.x, localY - e.y)).reversed();
					break;
				case Random: // Reference: https://stackoverflow.com/a/40380283
					final Map<Object, UUID> uniqueIds = new IdentityHashMap<>();
					comparator = Comparator.comparing(e -> uniqueIds.computeIfAbsent(e, k -> UUID.randomUUID()));
					break;
				default: // Reference: https://stackoverflow.com/a/70007141
				case Arbitrary:
					comparator = (a, b) -> 0; // NO-OP; Keeps it as the original order as added with LinkedHashSet.
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
		private final PositionArgument.PositionSelectDisplay display;
		private final SelectEntry entry;
		private boolean depthValid = true, xValid = true, yValid = true;
		private @Nullable Integer depth, x, y;
		private boolean specify = false;
		private ChangeListener listener = null;

		public PositionArgument(Level level, int x, int y) {
			this.defaultDepth = level.depth;
			this.defaultX = x;
			this.defaultY = y;
			display = new PositionArgument.PositionSelectDisplay(level);
			entry = new SelectEntry("Position", () -> Game.setDisplay(display), false) {

				@Override
				public String toString() {
					return String.format("Position: (%s, %s, %s)",
						depthValid ? depth == null ? defaultDepth :
							Color.CYAN_CODE + depth + Color.WHITE_CODE + Color.GRAY_CODE : UPV_STRING,
						xValid ? PositionArgument.this.x == null ? formatPosition(defaultX) :
							Color.CYAN_CODE + formatPosition(PositionArgument.this.x) + Color.WHITE_CODE + Color.GRAY_CODE : UPV_STRING,
						yValid ? PositionArgument.this.y == null ? formatPosition(defaultY) :
							Color.CYAN_CODE + formatPosition(PositionArgument.this.y) + Color.WHITE_CODE + Color.GRAY_CODE : UPV_STRING
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
				if (input.getMappedKey("EXIT").isClicked()) {
					if (specifyOption.getValue()) {
						specify = true;
						if (levelOption.getUserInput().isEmpty()) {
							depth = null; // Default value
							depthValid = true;
						} else try {
							depth = levelOption.getValue();
							depthValid = true;
						} catch (IllegalArgumentException e) {
							depth = null;
							depthValid = false;
						}

						if (positionOption.getXUserInput().isEmpty()) {
							x = null; // Default value
							xValid = true;
						} else try {
							x = positionOption.getXValue();
							xValid = true;
						} catch (IllegalArgumentException e) {
							x = null;
							xValid = false;
						}

						if (positionOption.getYUserInput().isEmpty()) {
							y = null; // Default value
							yValid = true;
						} else try {
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

					if (listener != null) listener.onChange(null);
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

		public void setChangeListener(ChangeListener listener) {
			this.listener = listener;
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
		private final FilterArgument.FilterSettingDisplay display;
		private final SelectEntry entry;
		private final LinkedHashMap<FilterArgument.FilterOption, FilterArgument.FilterOption.FilterOptionEntry> argumentOptions = new LinkedHashMap<>();
		private ChangeListener listener = null;

		public FilterArgument() {
			display = new FilterArgument.FilterSettingDisplay();
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
			private final HashMap<ListEntry, FilterOption> entryMap = new HashMap<>(); // A temporary list storing entry relations.

			public FilterSettingDisplay() {
				actionEntry = new SelectEntry("Add Filter",
					() -> Game.setDisplay(new ListItemSelectDisplay<>(FilterArgument.FilterOption.values(),
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
				entryMap.clear();
				if (argumentOptions.isEmpty())
					entries.add(placeholderEntry);
				else
					argumentOptions.forEach((k, v) -> {
						ListEntry entry1 = v.getEntry();
						entries.add(entry1);
						entryMap.put(entry1, k);
					});
				entries.add(new BlankEntry());
				actionEntry.setSelectable(Arrays.stream(FilterArgument.FilterOption.values()).anyMatch(o -> !argumentOptions.containsKey(o)));
				entries.add(actionEntry);
				menus[0] = builder.setEntries(entries).createMenu();
				if (listener != null) listener.onChange(null);
			}

			@Override
			public void tick(InputHandler input) {
				if (input.getMappedKey("D").isClicked()) {
					int index = menus[0].getSelection();
					if (index < argumentOptions.size()) {
						argumentOptions.remove(entryMap.get(menus[0].getCurEntry()));
						update();
					}
				} else
					super.tick(input);
			}

			private void addFilter(FilterArgument.FilterOption option) {
				FilterArgument.FilterOption.FilterOptionEntry entry = option.getEntry();
				argumentOptions.put(option, entry);
				entry.setChangeListener(v -> update());
				update();
			}
		}

		private enum FilterOption {
			Distance("Distance") { // Value based on the smallest unit in entity coordinate system.

				@Override
				public @NotNull FilterArgument.FilterOption.FilterOptionEntry getEntry() {
					return new DistanceOptionEntry();
				}

				class DistanceOptionEntry extends InputEntry implements FilterArgument.FilterOption.FilterOptionEntry {
					private final InputEntry rangedInput = new InputEntry("", regexNumber, 0);
					private int selection = 0; // Maybe this should be boolean?
					private boolean ranged = false;

					public DistanceOptionEntry() {
						super("Distance", regexNumber, 0);
					}

					@Override
					public void tick(InputHandler input) {
						if (input.getMappedKey("ENTER").isClicked()) {
							ranged = !ranged;
							if (selection == 1) selection = 0;
						} else if (input.getMappedKey("CURSOR-LEFT").isClicked()) {
							if (selection == 1) selection = 0;
							Sound.play("select");
						} else if (input.getMappedKey("CURSOR-RIGHT").isClicked()) {
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
							}
							else if (input.isEmpty()) try {
								return Integer.parseInt(range) >= 0;
							} catch (NumberFormatException e) {
								return false;
							}
							else {
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
				public @NotNull FilterArgument.FilterOption.FilterOptionEntry getEntry() {
					return new RangeOptionEntry();
				}

				class RangeOptionEntry extends Vector2ValueOption implements FilterArgument.FilterOption.FilterOptionEntry {
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
				public @NotNull FilterArgument.FilterOption.FilterOptionEntry getEntry() {
					return new RotationOptionEntry();
				}

				class RotationOptionEntry extends ArrayEntry<Direction> implements FilterArgument.FilterOption.FilterOptionEntry {
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
				public @NotNull FilterArgument.FilterOption.FilterOptionEntry getEntry() {
					return new NameOptionEntry();
				}

				class NameOptionEntry extends InputEntry implements FilterArgument.FilterOption.FilterOptionEntry {
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
				public @NotNull FilterArgument.FilterOption.FilterOptionEntry getEntry() {
					return new TypeOptionEntry();
				}

				class TypeOptionEntry extends ListEntry implements FilterArgument.FilterOption.FilterOptionEntry {
					private final EnumMap<FilterArgument.FilterOption.EntityType, Boolean> typeList = new EnumMap<>(FilterArgument.FilterOption.EntityType.class);
					private final Display display;

					private ChangeListener listener;

					public TypeOptionEntry() {
						for (FilterArgument.FilterOption.EntityType type : FilterArgument.FilterOption.EntityType.values()) {
							typeList.put(type, null);
						}

						HashMap<Boolean, Boolean> boolMap = new HashMap<>();
						boolMap.put(null, true);
						boolMap.put(true, false);
						boolMap.put(false, null);
						display = new Display(new Menu.Builder(true, 2, RelPos.CENTER,
							typeList.keySet().stream().map(t -> new ListEntry() {
								@Override
								public void tick(InputHandler input) {
									if (input.getMappedKey("ENTER").isClicked()) {
										typeList.compute(t, (ty, v) -> // null -> true -> false -> null
											boolMap.get(v));
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
								if (input.getMappedKey("EXIT").isClicked()) {
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
						if (input.getMappedKey("SELECT").isClicked()) {
							Sound.play("select");
							Game.setDisplay(display);
						}
					}

					@Override
					public String toString() {
						if (typeList.values().stream().allMatch(Objects::isNull)) {
							return "Type: All";
						} else {
							List<Map.Entry<FilterArgument.FilterOption.EntityType, Boolean>> s;
							if ((s = typeList.entrySet().stream().filter(e -> e.getValue() != null)
								// An efficient collection for only the first element from the stream
								.collect(Collectors.toCollection(() -> new AbstractList<Map.Entry<EntityType, Boolean>>() {
									private int size = 0; // Only check for first element
									private @Nullable Set<Map.Entry<EntityType, Boolean>> singleton = null;

									@Override
									public boolean add(Map.Entry<EntityType, Boolean> entityTypeBooleanEntry) {
										if (singleton == null) {
											singleton = Collections.singleton(entityTypeBooleanEntry);
											size = 1;
											return true;
										} else {
											size = 2;
											return false;
										}
									}

									@Override
									public Map.Entry<EntityType, Boolean> get(int index) {
										return singleton == null ? null : singleton.iterator().next();
									}

									@Override
									public Iterator<Map.Entry<EntityType, Boolean>> iterator() {
										return singleton == null ? Collections.emptyIterator() : singleton.iterator();
									}

									@Override
									public int size() {
										return size;
									}
								}))).size() == 1) {
								Map.Entry<FilterArgument.FilterOption.EntityType, Boolean> e;
								return "Type: " + ((e = s.get(0)).getValue() ? "" : "Not ") + e.getKey().name;
							} else { // At least 1 of them > 0
								// WL == WhiteList, BL == BlackList
								long wl = typeList.values().stream().filter(e -> e != null && e).count();
								long bl = typeList.values().stream().filter(e -> e != null && !e).count();
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

			/**
			 * Constructs a new entry instance for this argument option.
			 */
			@NotNull
			public abstract FilterArgument.FilterOption.FilterOptionEntry getEntry();

			public interface FilterOptionEntry extends UserMutable {
				@NotNull Predicate<Entity> getFilter(PositionArgument positionArgument);

				/**
				 * The entry representing this option.
				 */
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

		public void setChangeListener(ChangeListener listener) {
			this.listener = listener;
		}

		public boolean isAllValid() {
			return argumentOptions.values().stream().allMatch(FilterArgument.FilterOption.FilterOptionEntry::isFilterValid);
		}
	}

	// Order and Bound
	private static class CollectionArgument {
		private final InputEntry limitEntry;
		private final ArrayEntry<CollectionArgument.SortType> sortEntry;
		private final BooleanEntry limitEnabled;
		private final BooleanEntry sortEnabled;
		private final SelectEntry entry;
		private final Display display;
		private ChangeListener listener = null;

		public enum SortType {
			Nearest, Furthest, Random, Arbitrary
		}

		public CollectionArgument() {
			limitEnabled = new BooleanEntry("Enable Limit", false);
			sortEnabled = new BooleanEntry("Enable Sort", false);
			limitEntry = new InputEntry("Limit", InputEntry.regexNumber, 0) {
				@Override
				public boolean isValid() {
					try {
						return Integer.parseInt(getUserInput()) >= 1;
					} catch (NumberFormatException e) {
						return false;
					}
				}
			};
			sortEntry = new ArrayEntry<>("Sort", true, false, CollectionArgument.SortType.values());
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
					if (listener != null) listener.onChange(null);
				}
			};
			entry = new SelectEntry("Order and Bound", () -> Game.setDisplay(display), false) {
				@Override
				public String toString() {
					return super.toString() + (isAllValid() ? "" : " (Bound " + UPV_STRING + ")");
				}
			};
		}

		public boolean isAllValid() {
			return !limitEnabled.getValue() || limitEntry.isValid();
		}

		public SelectEntry getEntry() {
			return entry;
		}

		public void setChangeListener(ChangeListener listener) {
			this.listener = listener;
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

		public CollectionArgument.SortType getSort() {
			return sortEnabled.getValue() ? sortEntry.getValue() : CollectionArgument.SortType.Arbitrary;
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
	public void setChangeListener(ChangeListener l) {
		configDisplay.setChangeListener(l);
		super.setChangeListener(l);
	}

	@Override
	public void tick(InputHandler input) {
		if (input.getMappedKey("SELECT").isClicked()) {
			Sound.play("confirm");
			Game.setDisplay(configDisplay);
		} else {
			super.tick(input);
		}
	}
}
