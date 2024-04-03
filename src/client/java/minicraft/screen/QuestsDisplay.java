package minicraft.screen;

import minicraft.core.CrashHandler;
import minicraft.core.Game;
import minicraft.core.Renderer;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.core.io.Settings;
import minicraft.core.io.Sound;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.MinicraftImage;
import minicraft.gfx.Point;
import minicraft.gfx.Rectangle;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteLinker;
import minicraft.saveload.Load;
import minicraft.screen.entry.BlankEntry;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.SelectEntry;
import minicraft.screen.entry.StringEntry;
import minicraft.util.AdvancementElement;
import minicraft.util.Logging;
import minicraft.util.Quest;
import minicraft.util.Quest.QuestSeries;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.function.IntPredicate;
import java.util.stream.Collectors;

public class QuestsDisplay extends Display {
	/**
	 * Unlocked but uncompleted.
	 */
	private final static HashSet<Quest> displayableQuests = new HashSet<>(); // Temp set. Refreshed anytime.
	private final static HashSet<QuestSeries> series = new HashSet<>();

	private SelectEntry[][] seriesEntries;
	private int selectedEntry = 0;
	private QuestSeries[][] entrySeries;
	private int previousSelection = 0;

	static {
		try { // TODO Data pack support.
			loadQuestFile("/resources/quests.json");
		} catch (IOException e) {
			e.printStackTrace();
			Logging.QUEST.error("Failed to load quests.");
		}
	}

	private static void loadQuestFile(@SuppressWarnings("SameParameterValue") String filename) throws IOException {
		JSONObject json = new JSONObject(String.join("", Load.loadFile(filename)));
		for (String key : json.keySet()) {
			loadSeries(key, json.getJSONObject(key));
		}
	}

	private static void loadSeries(String key, JSONObject json) {
		HashMap<String, AdvancementElement.ElementCriterion> criteria = new HashMap<>();
		JSONObject criteriaJson = json.optJSONObject("criteria"); // Optional
		if (criteriaJson != null) {
			for (String k : criteriaJson.keySet()) {
				JSONObject criterion = criteriaJson.getJSONObject(k);
				criteria.put(k, new AdvancementElement.ElementCriterion(criterion.getString("trigger"), criterion.getJSONObject("conditions")));
			}
		}

		HashSet<HashSet<String>> requirements = new HashSet<>();
		JSONArray requirementsJson = json.optJSONArray("requirements");
		if (requirementsJson != null) {
			for (int i = 0; i < requirementsJson.length(); i++) {
				HashSet<String> req = new HashSet<>();
				JSONArray reqJson = requirementsJson.getJSONArray(i);
				for (int j = 0; j < reqJson.length(); j++) {
					req.add(reqJson.getString(j));
				}
				requirements.add(req);
			}
		}

		HashMap<String, AdvancementElement.ElementCriterion> unlockingCriteria = new HashMap<>();
		JSONObject unlockingCriteriaJson = json.getJSONObject("unlocking_criteria");
		for (String k : unlockingCriteriaJson.keySet()) {
			JSONObject criterion = unlockingCriteriaJson.getJSONObject(k);
			unlockingCriteria.put(k, new AdvancementElement.ElementCriterion(criterion.getString("trigger"), criterion.getJSONObject("conditions")));
		}

		HashSet<HashSet<String>> unlockingRequirements = new HashSet<>();
		JSONArray unlockingRequirementsJson = json.optJSONArray("unlocking_requirements");
		if (requirementsJson != null) {
			for (int i = 0; i < unlockingRequirementsJson.length(); i++) {
				HashSet<String> req = new HashSet<>();
				JSONArray reqJson = unlockingRequirementsJson.getJSONArray(i);
				for (int j = 0; j < reqJson.length(); j++) {
					req.add(reqJson.getString(j));
				}
				unlockingRequirements.add(req);
			}
		}

		HashMap<String, Quest> quests = new HashMap<>();
		JSONObject questsJson = json.getJSONObject("quests");
		for (String k : questsJson.keySet()) {
			Quest quest = loadQuest(k, questsJson.getJSONObject(k));
			quests.put(quest.key, quest);
		}

		AdvancementElement.ElementRewards rewards = AdvancementElement.loadRewards(json.optJSONObject("rewards"));
		series.add(new QuestSeries(key, json.getString("description"), criteria,
			rewards, requirements, quests, unlockingCriteria, unlockingRequirements));
	}

	private static Quest loadQuest(String key, JSONObject json) {
		HashMap<String, AdvancementElement.ElementCriterion> criteria = new HashMap<>();
		JSONObject criteriaJson = json.getJSONObject("criteria");
		if (criteriaJson.isEmpty()) throw new IndexOutOfBoundsException("criteria is empty.");
		for (String k : criteriaJson.keySet()) {
			JSONObject criterion = criteriaJson.getJSONObject(k);
			criteria.put(k, new AdvancementElement.ElementCriterion(criterion.getString("trigger"), criterion.getJSONObject("conditions")));
		}

		HashSet<HashSet<String>> requirements = new HashSet<>();
		JSONArray requirementsJson = json.optJSONArray("requirements");
		if (requirementsJson != null) {
			for (int i = 0; i < requirementsJson.length(); i++) {
				HashSet<String> req = new HashSet<>();
				JSONArray reqJson = requirementsJson.getJSONArray(i);
				for (int j = 0; j < reqJson.length(); j++) {
					req.add(reqJson.getString(j));
				}
				requirements.add(req);
			}
		}

		HashMap<String, AdvancementElement.ElementCriterion> unlockingCriteria = new HashMap<>();
		JSONObject unlockingCriteriaJson = json.optJSONObject("unlocking_criteria");
		if (unlockingCriteriaJson != null) {
			for (String k : unlockingCriteriaJson.keySet()) {
				JSONObject criterion = unlockingCriteriaJson.getJSONObject(k);
				unlockingCriteria.put(k, new AdvancementElement.ElementCriterion(criterion.getString("trigger"), criterion.getJSONObject("conditions")));
			}
		}

		HashSet<HashSet<String>> unlockingRequirements = new HashSet<>();
		JSONArray unlockingRequirementsJson = json.optJSONArray("unlocking_requirements");
		if (requirementsJson != null) {
			for (int i = 0; i < unlockingRequirementsJson.length(); i++) {
				HashSet<String> req = new HashSet<>();
				JSONArray reqJson = unlockingRequirementsJson.getJSONArray(i);
				for (int j = 0; j < reqJson.length(); j++) {
					req.add(reqJson.getString(j));
				}
				unlockingRequirements.add(req);
			}
		}

		AdvancementElement.ElementRewards rewards = AdvancementElement.loadRewards(json.optJSONObject("rewards"));
		return new Quest(key, json.getString("description"), criteria,
			rewards, requirements, json.optString("parent", null), unlockingCriteria, unlockingRequirements);
	}

	public static void refreshDisplayableQuests() {
		displayableQuests.clear();
		series.forEach(series -> {
			if (series.isDisplayableAtStatus())
				series.getSeriesQuests().values().forEach(quest -> {
					if (quest.isDisplayableAtStatus())
						displayableQuests.add(quest);
				});
		});
	}

	private void reloadEntries() {
		ArrayList<SelectEntry> completed = new ArrayList<>();
		ArrayList<SelectEntry> unlocked = new ArrayList<>();
		ArrayList<QuestSeries> completedSeries = new ArrayList<>();
		ArrayList<QuestSeries> unlockedSeries = new ArrayList<>();
		for (QuestSeries questSeries : series) {
			boolean isCompleted = questSeries.isCompleted();
			boolean isUnlocked = questSeries.isUnlocked();
			SelectEntry select = new SelectEntry(Localization.getLocalized(questSeries.key), () -> Game.setDisplay(new SeriesInformationDisplay(questSeries)), true) {
				@Override
				public int getColor(boolean isSelected) {
					return isCompleted ? Color.GREEN : isUnlocked ? Color.WHITE : Color.GRAY;
				}
			};

			if (isCompleted) {
				completed.add(select);
				completedSeries.add(questSeries);
			}

			if (isUnlocked) {
				unlocked.add(select);
				unlockedSeries.add(questSeries);
			}
		}

		seriesEntries = new SelectEntry[][] {
			unlocked.toArray(new SelectEntry[0]),
			completed.toArray(new SelectEntry[0])
		};

		entrySeries = new QuestSeries[][] {
			unlockedSeries.toArray(new QuestSeries[0]),
			completedSeries.toArray(new QuestSeries[0])
		};
	}

	public QuestsDisplay() {
		super(true, true);
		reloadEntries();

		menus = new Menu[] {
			new Menu.Builder(false, 1, RelPos.CENTER)
				.setPositioning(new Point(Screen.w / 2, Screen.h / 2 - 20), RelPos.CENTER)
				.setDisplayLength(5)
				.setSelectable(true)
				.createMenu(),
			new Menu.Builder(false, 0, RelPos.LEFT)
				.setPositioning(new Point(Screen.w / 2 - 8 * (2 + Localization.getLocalized("minicraft.displays.quests.display.header.unlocked").length()), 30), RelPos.RIGHT)
				.setEntries(new StringEntry("minicraft.displays.quests.display.header.unlocked", Color.GRAY))
				.setSelectable(false)
				.createMenu(),
			new Menu.Builder(false, 0, RelPos.LEFT)
				.setPositioning(new Point(Screen.w / 2 + 8 * 2, 30), RelPos.RIGHT)
				.setEntries(new StringEntry("minicraft.displays.quests.display.header.completed", Color.GRAY))
				.setSelectable(false)
				.createMenu(),
			new Menu.Builder(false, 0, RelPos.CENTER)
				.setPositioning(new Point(Screen.w / 2, Screen.h / 2 + 35), RelPos.CENTER)
				.setEntries(new StringEntry(Localization.getLocalized("minicraft.displays.quests.display.no_quest_desc")))
				.setSelectable(false)
				.createMenu(),
			new Menu.Builder(false, 0, RelPos.CENTER)
				.setPositioning(new Point(Screen.w / 2, 10), RelPos.CENTER)
				.setEntries(new StringEntry(Settings.getEntry("quests").toString(), Color.WHITE))
				.setSelectable(false)
				.createMenu()
		};

		updateEntries();
	}

	public static class SeriesInformationDisplay extends Display {
		public SeriesInformationDisplay(QuestSeries series) {
			super(false, true);
			ArrayList<ListEntry> entries = new ArrayList<>();

			entries.add(series.isCompleted() ? new StringEntry(Localization.getLocalized("minicraft.displays.quests.quest_info.display.status",
				Localization.getLocalized("minicraft.displays.quests.quest_info.display.status.completed")), Color.GREEN, false) :
				series.isUnlocked() ? new StringEntry(Localization.getLocalized("minicraft.displays.quests.quest_info.display.status",
					Localization.getLocalized("minicraft.displays.quests.quest_info.display.status.unlocked")), Color.WHITE, false) :
					new StringEntry(Localization.getLocalized("minicraft.displays.quests.quest_info.display.status",
						Localization.getLocalized("minicraft.displays.quests.quest_info.display.status.locked")), Color.GRAY, false) // Locked series would not been shown...?
			);

			entries.add(new StringEntry(Localization.getLocalized("minicraft.displays.quests.quest_info.display.quests_completed_count",
				series.getSeriesQuests().values().stream().filter(AdvancementElement::isCompleted).count()), Color.WHITE, false));
			entries.addAll(Arrays.asList(StringEntry.useLines(Color.WHITE, false,
				Localization.getLocalized("minicraft.displays.quests.quest_info.display.description", Localization.getLocalized(series.description)))));
			entries.add(new StringEntry(Localization.getLocalized("minicraft.displays.quests.quest_info.display.ongoing_quests",
				series.getSeriesQuests().values().stream().filter(AdvancementElement::isDisplayableAtStatus).count()), Color.WHITE, false));

			entries.add(new BlankEntry());
			entries.add(new SelectEntry("minicraft.displays.quests.quest_info.view_quests", () -> Game.setDisplay(new SeriesQuestViewerDisplay(series))));

			menus = new Menu[] {
				new Menu.Builder(true, 0, RelPos.CENTER)
					.setPositioning(new Point(Screen.w / 2, 5), RelPos.BOTTOM)
					.setEntries(new StringEntry(Localization.getLocalized(series.key)))
					.setSelectable(false)
					.createMenu(),
				new Menu.Builder(true, 2, RelPos.CENTER)
					.setPositioning(new Point(Screen.w / 2, 30), RelPos.BOTTOM)
					.setEntries(entries)
					.setSelectable(true)
					.createMenu()
			};

			selection = 1;
		}

		private static class SeriesQuestViewerDisplay extends Display {
			private static final int entryPadding = 3;
			private static final int entryGap = 5; // 2x for y-axis only when between entries.
			private final Menu[] menus;
			private final Quest[][] questsTree;
			private final Rectangle[][] treeDimensions;
			private final HashMap<Quest, Quest> parents = new HashMap<>();
			private final int rasterWidth;
			private final int rasterHeight;
			private final int rasterX;
			private final int rasterY;
			private final MinicraftImage image;
			private final int[] rasterPixels;
			private final Screen simulatedRasterScreen = new Screen(new BufferedImage(Screen.w, Screen.h, BufferedImage.TYPE_INT_RGB)) {
				@Override
				public void render(int xp, int yp, int xt, int yt, int bits, MinicraftImage sheet, int whiteTint, boolean fullbright, int color) {
					if (sheet == null) return; // Verifying that sheet is not null.
					// Ignoring mirror.
					// Validation check
					if (xt * 8 + yt * 8 * sheet.width + 7 + 7 * sheet.width >= sheet.pixels.length) {
						sheet = Renderer.spriteLinker.missingSheet(SpriteLinker.SpriteType.Item);
						xt = 0;
						yt = 0;
					}

					int xTile = xt; // Gets x position of the spritesheet "tile"
					int yTile = yt; // Gets y position
					int toffs = xTile * 8 + yTile * 8 * sheet.width; // Gets the offset of the sprite into the spritesheet pixel array, the 8's represent the size of the box. (8 by 8 pixel sprite boxes)

					// THIS LOOPS FOR EVERY PIXEL
					for (int y = 0; y < 8; y++) { // Loops 8 times (because of the height of the tile)
						for (int x = 0; x < 8; x++) { // Loops 8 times (because of the width of the tile)
							int col = sheet.pixels[toffs + x + y * sheet.width]; // Gets the color of the current pixel from the value stored in the sheet.
							boolean isTransparent = (col >> 24 == 0);
							if (!isTransparent) {
								if (whiteTint != -1 && col == 0x1FFFFFF) {
									// If this is white, write the whiteTint over it
									renderRasterPixel(x + xp, y + yp, whiteTint & 0xFFFFFF);
								} else {
									// Inserts the colors into the image
									if (fullbright) {
										renderRasterPixel(x + xp, y + yp, Color.WHITE);
									} else {
										if (color != 0) {
											renderRasterPixel(x + xp, y + yp, color);
										} else {
											renderRasterPixel(x + xp, y + yp, col & 0xFFFFFF);
										}
									}
								}
							}
						}
					}
				}
			};

			private int cursorX = 0;
			private int cursorY = 0;
			private int xScroll = 0;
			private int yScroll = 0;
			private boolean inBrowsing = false; // Simple check;

			public SeriesQuestViewerDisplay(QuestSeries series) {
				super(false, true);
				menus = new Menu[] {
					new Menu.Builder(true, 0, RelPos.CENTER, StringEntry.useLines("minicraft.displays.quests", series.key))
						.setPositioning(new Point(Screen.w / 2, 6), RelPos.BOTTOM)
						.createMenu(),
					new Menu.Builder(true, 0, RelPos.CENTER)
						.setPositioning(new Point(Screen.w / 2, 40), RelPos.BOTTOM)
						.setSize(Screen.w - 16, Screen.h - 60)
						.createMenu()
				};

				Map<String, Quest> quests = series.getSeriesQuests()
					.entrySet().stream().filter(entry -> entry.getValue().isUnlocked()) // Showing unlocked only.
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
				if (!quests.isEmpty()) {
					ArrayList<ArrayList<Quest>> questRowsList = new ArrayList<>();
					ArrayList<Quest> topRow = quests.values().stream().filter(quest -> quest.getParent() == null).collect(Collectors.toCollection(ArrayList::new));
					questRowsList.add(topRow);
					HashMap<Quest, HashSet<Quest>> childQuests = new HashMap<>();
					quests.values().forEach(quest -> {
						Quest parent = quest.getParent();
						if (parent != null) {
							parents.put(quest, parent);
							if (childQuests.containsKey(parent))
								childQuests.get(parent).add(quest);
							else
								childQuests.put(parent, new HashSet<>(Collections.singletonList(quest)));
						}
					});

					while (childQuests.size() > 0) {
						for (Iterator<Map.Entry<Quest, HashSet<Quest>>> it = childQuests.entrySet().iterator(); it.hasNext(); ) {
							Map.Entry<Quest, HashSet<Quest>> entry = it.next();
							Quest parent = entry.getKey();
							ArrayList<Quest> questRow = questRowsList.stream().filter(quests1 -> quests1.contains(parent)).findAny().orElse(null);
							if (questRow != null) {
								if (questRowsList.indexOf(questRow) < questRowsList.size() - 1) {
									questRowsList.get(questRowsList.indexOf(questRow) + 1).addAll(entry.getValue());
								} else { // The row is the last element.
									questRowsList.add(new ArrayList<>(entry.getValue()));
								}
								it.remove();
							}
						}
					}

					questsTree = questRowsList.stream().map(quests1 -> quests1.toArray(new Quest[0])).toArray(Quest[][]::new);
					treeDimensions = new Rectangle[questsTree.length][];
					for (int i = 0; i < questsTree.length; i++) { // y-axis
						treeDimensions[i] = new Rectangle[questsTree[i].length];
						int height = Font.textHeight();
						for (int j = 0; j < questsTree[i].length; j++) { // x-axis
							int width = Font.textWidth(Localization.getLocalized(questsTree[i][j].key));
							treeDimensions[i][j] = new Rectangle(entryGap, entryGap, entryPadding * 2 + width, entryPadding * 2 + height, 0);
							if (j > 0)
								treeDimensions[i][j].translate(treeDimensions[i][j - 1].getRight() + entryGap, 0);
							if (i > 0)
								treeDimensions[i][j].translate(0, treeDimensions[i - 1][0].getBottom() + entryGap * 2);
						}
					}
				} else {
					questsTree = new Quest[0][];
					treeDimensions = new Rectangle[0][];
				}

				Rectangle menuBounds = menus[1].getBounds();
				rasterWidth = menuBounds.getWidth() - MinicraftImage.boxWidth * 2;
				rasterHeight = menuBounds.getHeight() - MinicraftImage.boxWidth * 2;
				image = new MinicraftImage(rasterWidth, rasterHeight);
				rasterPixels = image.pixels;
				rasterX = menuBounds.getLeft() + MinicraftImage.boxWidth;
				rasterY = menuBounds.getTop() + MinicraftImage.boxWidth;
			}

			@Override
			public void tick(InputHandler input) {
				super.tick(input);

				if (questsTree.length > 0) {
					if (input.getMappedKey("shift").isDown()) { // Browsing mode.
						inBrowsing = true;
						if (input.getMappedKey("shift+cursor-down").isClicked())
							yScroll += 3;
						else if (input.getMappedKey("shift+cursor-up").isClicked())
							yScroll -= 3;
						else if (input.getMappedKey("shift+cursor-right").isClicked())
							xScroll += 3;
						else if (input.getMappedKey("shift+cursor-left").isClicked())
							xScroll -= 3;
					} else {
						if (inBrowsing) {
							scrollIfNeeded();
							inBrowsing = false;
						}
						if (input.getMappedKey("cursor-down").isClicked()) {
							if (cursorY < questsTree.length - 1) {
								cursorY++;
								if (cursorX >= questsTree[cursorY].length)
									cursorX = questsTree[cursorY].length - 1;
								Sound.play("select");
								scrollIfNeeded();
							}
						} else if (input.getMappedKey("cursor-up").isClicked()) {
							if (cursorY > 0) {
								cursorY--;
								if (cursorX >= questsTree[cursorY].length)
									cursorX = questsTree[cursorY].length - 1;
								Sound.play("select");
								scrollIfNeeded();
							}
						} else if (input.getMappedKey("cursor-right").isClicked()) {
							if (cursorX < questsTree[cursorY].length - 1) {
								cursorX++;
								Sound.play("select");
								scrollIfNeeded();
							}
						} else if (input.getMappedKey("cursor-left").isClicked()) {
							if (cursorX > 0) {
								cursorX--;
								Sound.play("select");
								scrollIfNeeded();
							}
						} else if (input.getMappedKey("select").isClicked()) {
							Sound.play("confirm");
							Game.setDisplay(new QuestInformationDisplay(questsTree[cursorY][cursorX]));
						}
					}
				}
			}

			private void scrollIfNeeded() {
				if (xScroll > treeDimensions[cursorY][cursorX].getLeft() - entryGap)
					xScroll = treeDimensions[cursorY][cursorX].getLeft() - entryGap;
				else if (xScroll + rasterWidth < treeDimensions[cursorY][cursorX].getRight() + entryGap)
					xScroll = treeDimensions[cursorY][cursorX].getRight() + entryGap - rasterWidth;

				if (yScroll > treeDimensions[cursorY][cursorX].getTop() - entryGap)
					yScroll = treeDimensions[cursorY][cursorX].getTop() - entryGap;
				else if (yScroll + rasterHeight < treeDimensions[cursorY][cursorX].getBottom() + entryGap)
					yScroll = treeDimensions[cursorY][cursorX].getBottom() + entryGap - rasterHeight;
			}

			@Override
			public void render(Screen screen) {
				super.render(screen);
				for (Menu menu : menus)
					menu.render(screen);
				Arrays.fill(rasterPixels, Color.BLACK);
				renderRaster();
				// Border
				screen.drawRect(rasterX - 1, rasterY - 1, rasterWidth + 2, rasterHeight + 2, Color.WHITE);
				screen.render(rasterX, rasterY, 0, 0, rasterWidth, rasterHeight, image);
			}

			private void renderRaster() {
				if (questsTree.length == 0) {
					String text = Localization.getLocalized("minicraft.displays.quests.display.no_quest");
					Font.draw(text, simulatedRasterScreen, xScroll + rasterWidth / 2 - Font.textWidth(text) / 2,
						yScroll + rasterHeight / 2 - Font.textHeight() / 2, Color.GRAY);
					return;
				}

				// Tree relations.
				for (int r = 0; r < questsTree.length; r++) {
					for (int c = 0; c < questsTree[r].length; c++) {
						Quest quest = questsTree[r][c];
						Quest parent = parents.get(quest);
						if (parent == null) continue;
						Rectangle rec = treeDimensions[r][c];
						Rectangle parentRec = null;
						int parentX = 0;
						int parentY = 0;
						for (int i = 0; i < treeDimensions.length; i++) {
							for (int j = 0; j < treeDimensions[i].length; j++) {
								if (questsTree[i][j] == parent) {
									parentRec = treeDimensions[i][j];
									parentX = j;
									parentY = i;
									break;
								}
							}
						}

						if (parentRec == null) { // Unexpected.
							Logging.QUEST.warn("Unexpected situation: rectangle of parent quest not found.");
							continue;
						}

						// Parent is always higher than this.
						Point p0 = parentRec.getCenter();
						Point p1 = rec.getCenter();
						int x0 = p0.x;
						int x1 = p1.x;
						int y0 = p0.y;
						int y1 = p1.y;
						boolean selected = c == cursorX && r == cursorY;
						boolean parentSelected = parentX == cursorX && parentY == cursorY;
						int color = selected ? Color.CYAN : parentSelected ? Color.GREEN : Color.tint(Color.GRAY, -1, true);

						// Bresenham's line algorithm
						Rectangle finalParentRec = parentRec;
						plotLine(x0, y0, x1, y1, yy -> yy >= finalParentRec.getBottom() && yy <= rec.getTop(), color);
					}
				}

				// Tree elements.
				for (int r = 0; r < questsTree.length; r++) {
					for (int c = 0; c < questsTree[r].length; c++) {
						Quest quest = questsTree[r][c];
						Rectangle rec = treeDimensions[r][c];
						int x = rec.getLeft();
						int y = rec.getTop();
						boolean selected = c == cursorX && r == cursorY;
						Font.draw(Localization.getLocalized(quest.key), simulatedRasterScreen, x + entryPadding, y + entryPadding,
							selected ? (quest.isCompleted() ? Color.tint(Color.GREEN, 1, true) :
								Color.WHITE) : Color.tint(Color.GRAY, 1, true));
						for (int i = 0; i < rec.getWidth(); i++) { // Border.
							for (int j = 0; j < rec.getHeight(); j++) {
								if (i == 0 || i == rec.getWidth() - 1 || j == 0 || j == rec.getHeight() - 1)
									renderRasterPixel(x + i, y + j,
										selected ? (quest.isCompleted() ? Color.tint(Color.GREEN, -1, true) :
											Color.tint(Color.GRAY, 2, true)) : Color.GRAY);
							}
						}
					}
				}
			}

			private void renderRasterPixel(int x, int y, int color) {
				x -= xScroll;
				y -= yScroll;
				if (x < 0 || x >= rasterWidth || y < 0 || y >= rasterHeight) return; // Out of bounds.
				rasterPixels[x + y * rasterWidth] = color;
			}

			// Parts of Bresenham's line algorithm
			void plotLineLow(int x0, int y0, int x1, int y1, IntPredicate yRange, int color) {
				int dx = x1 - x0;
				int dy = y1 - y0;
				int yi = 1;
				if (dy < 0) {
					yi = -1;
					dy = -dy;
				}
				int D = (2 * dy) - dx;
				int y = y0;

				for (int x = x0; x <= x1; x++) {
					if (yRange.test(y)) renderRasterPixel(x, y, color);
					if (D > 0) {
						y = y + yi;
						D = D + (2 * (dy - dx));
					} else
						D = D + 2 * dy;
				}
			}

			void plotLineHigh(int x0, int y0, int x1, int y1, IntPredicate yRange, int color) {
				int dx = x1 - x0;
				int dy = y1 - y0;
				int xi = 1;
				if (dx < 0) {
					xi = -1;
					dx = -dx;
				}
				int D = (2 * dx) - dy;
				int x = x0;

				for (int y = y0; y <= y1; y++) {
					if (yRange.test(y)) renderRasterPixel(x, y, color);
					if (D > 0) {
						x = x + xi;
						D = D + (2 * (dx - dy));
					} else
						D = D + 2 * dx;
				}
			}

			void plotLine(int x0, int y0, int x1, int y1, IntPredicate yRange, int color) {
				if (Math.abs(y1 - y0) < Math.abs(x1 - x0)) {
					if (x0 > x1)
						plotLineLow(x1, y1, x0, y0, yRange, color);
					else
						plotLineLow(x0, y0, x1, y1, yRange, color);
				} else {
					if (y0 > y1)
						plotLineHigh(x1, y1, x0, y0, yRange, color);
					else
						plotLineHigh(x0, y0, x1, y1, yRange, color);
				}
			}

			private static class QuestInformationDisplay extends Display {
				public QuestInformationDisplay(Quest quest) {
					super(false, true);
					String state = quest.isCompleted() ? "minicraft.displays.quests.quest_info.display.status.completed" :
						quest.isUnlocked() ? "minicraft.displays.quests.quest_info.display.status.unlocked" :
							"minicraft.displays.quests.quest_info.display.status.locked";
					int color = quest.isCompleted() ? Color.GREEN : quest.isUnlocked() ? Color.WHITE : Color.GRAY;
					menus = new Menu[] {
						new Menu.Builder(true, 1, RelPos.CENTER)
							.setPositioning(new Point(Screen.w / 2, 5), RelPos.BOTTOM)
							.setEntries(new StringEntry(quest.getSeries().key),
								new StringEntry(Localization.getLocalized(quest.key) + ": " + Localization.getLocalized(state), color, false),
								new StringEntry(quest.shouldAllCriteriaBeCompleted() ?
									Localization.getLocalized("minicraft.displays.quests.quest_info.display.progress",
										quest.getNumCriteriaCompleted(), quest.getTotalNumCriteria()) :
									Localization.getLocalized("minicraft.displays.quests.quest_info.display.progress_uncompleted"), Color.WHITE, false))
							.setSelectable(false)
							.createMenu(),
						new Menu.Builder(true, 2, RelPos.CENTER,
							StringEntry.useLines(quest.description))
							.setPositioning(new Point(Screen.w / 2, 52), RelPos.BOTTOM)
							.setSelectable(false)
							.createMenu()
					};
				}
			}
		}
	}

	public static HashSet<Quest> getDisplayableQuests() {
		return new HashSet<>(displayableQuests);
	}

	public static void resetGameQuests() {
		resetGameQuests(true);
	}

	private static void resetGameQuests(boolean update) {
		series.forEach(AdvancementElement::reset);
		if (update) refreshDisplayableQuests();
	}

	public static void load(JSONObject json) {
		resetGameQuests(false);
		for (String k : json.keySet()) {
			series.forEach(series1 -> {
				if (series1.key.equals(k)) {
					series1.load(json.getJSONObject(k));
				} else {
					series1.getSeriesQuests().forEach((key, value) -> {
						if (key.equals(k))
							value.load(json.getJSONObject(k));
					});
				}
			});
		}
		refreshDisplayableQuests();
	}

	/**
	 * Saving and writing all data into the given JSONObject.
	 */
	public static void save(JSONObject json) {
		series.forEach(series1 -> {
			series1.save(json);
			series1.getSeriesQuests().values().forEach(quest -> quest.save(json));
		});
	}

	@Override
	public void tick(InputHandler input) {
		super.tick(input);

		if (input.inputPressed("cursor-left")) if (selectedEntry > 0) {
			selectedEntry--;
			updateEntries();
		}

		if (input.inputPressed("cursor-right")) if (selectedEntry < 1) {
			selectedEntry++;
			updateEntries();
		}

		if (menus[0].getCurEntry() != null) {
			menus[3].setEntries(StringEntry.useLines(entrySeries[selectedEntry][menus[0].getSelection()].description));
		} else {
			menus[3].setEntries(StringEntry.useLines("minicraft.displays.quests.display.no_quest_desc"));
		}
	}

	private void updateEntries() {
		menus[0].setEntries(seriesEntries[selectedEntry]);

		String[] entryNames = new String[] {
			"minicraft.displays.quests.display.header.unlocked", "minicraft.displays.quests.display.header.completed"
		};

		for (int i = 0; i < 2; i++) {
			menus[i + 1].updateEntry(0, new StringEntry(entryNames[i], (i == selectedEntry) ? Color.WHITE : Color.GRAY));
		}

		int select = previousSelection;
		previousSelection = menus[0].getSelection();
		menus[0].setSelection(select);
	}
}
