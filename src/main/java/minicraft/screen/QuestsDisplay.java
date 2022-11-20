package minicraft.screen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

import minicraft.core.Game;
import minicraft.core.World;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.core.io.Settings;
import minicraft.entity.furniture.Chest;
import minicraft.gfx.Color;
import minicraft.gfx.Point;
import minicraft.gfx.Screen;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.Recipe;
import minicraft.saveload.Load;
import minicraft.screen.entry.BlankEntry;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.SelectEntry;
import minicraft.screen.entry.StringEntry;
import minicraft.util.Logging;
import minicraft.util.Quest;
import minicraft.util.Quest.QuestReward;
import minicraft.util.Quest.QuestSeries;

public class QuestsDisplay extends Display {
	private final static HashMap<String, Quest> quests = new HashMap<>();
	private final static ArrayList<Quest> unlockedQuests = new ArrayList<>();
	private final static ArrayList<Quest> completedQuest = new ArrayList<>();
	private final static JSONObject questStatus = new JSONObject();
	private final static HashMap<String, QuestSeries> series = new HashMap<>();
	private final static ArrayList<QuestSeries> unlockedSeries = new ArrayList<>();
	private final static ArrayList<QuestSeries> completedSeries = new ArrayList<>();
	private final static ArrayList<String> initiallyUnlocked = new ArrayList<>();

	private SelectEntry[][] seriesEntries;
	private int selectedEntry = 0;
	private QuestSeries[][] entrySeries;
	private int previousSelection = 0;


	static {
		try {
			loadQuestFile("/resources/quests.json", false);
			loadQuestFile("/resources/tutorials.json", true);
		} catch (IOException e) {
			e.printStackTrace();
			Logging.QUEST.error("Failed to load quests.");
		}

		// TODO Localize this class
		// TODO Setting callback messages for some tutorials
	}

	private static void loadQuestFile(String filename, boolean tutorial) throws IOException {
		JSONObject json = new JSONObject(String.join("", Load.loadFile(filename)));
		for (String id : json.keySet()) {
			loadSeries(id, json.getJSONObject(id), tutorial);
		}
	}

	private static void loadSeries(String id, JSONObject json, boolean tutorial) {
		boolean unlocked = json.optBoolean("unlocked", false); // Is unlocked initially
		JSONArray unlocksJson = json.optJSONArray("leads_to");
		JSONArray questsJson = json.getJSONArray("quests");

		String[] unlocks = new String[0];
		if (unlocksJson != null) {
			unlocks = new String[unlocksJson.length()];
			for (int i = 0; i < unlocksJson.length(); i++) {
				unlocks[i] = unlocksJson.getString(i);
			}
		}

		if (unlocked)
			initiallyUnlocked.add(id);

		ArrayList<Quest> seriesQuests = new ArrayList<>();
		for (int i = 0; i < questsJson.length(); i++) {
			Quest quest = loadQuest(questsJson.getJSONObject(i));
			seriesQuests.add(quest);
			quests.put(quest.id, quest);
		}

		series.put(id, new QuestSeries(id, json.getString("desc"), seriesQuests, loadReward(json.optJSONObject("reward")), unlocked, tutorial, unlocks));
	}

	private static QuestReward loadReward(JSONObject json) {
		ArrayList<Item> items = new ArrayList<>();
		ArrayList<Recipe> recipes = new ArrayList<>();
		if (json != null) {
			JSONArray itemsJson = json.optJSONArray("items");
			if (itemsJson != null) {
				for (int i = 0; i < itemsJson.length(); i++) {
					items.add(Items.get(itemsJson.getString(i)));
				}
			}

			JSONArray recipesJson = json.optJSONArray("recipes");
			if (recipesJson != null) {
				for (String product : json.keySet()) {
					JSONArray costsJson = json.getJSONArray(product);
					String[] costs = new String[costsJson.length()];
					for (int j = 0; j < costsJson.length(); j++) {
						costs[j] = costsJson.getString(j);
					}

					recipes.add(new Recipe(product, costs));
				}
			}
		}

		return new QuestReward(items, recipes);
	}

	private static Quest loadQuest(JSONObject json) {
		JSONArray unlocksJson = json.optJSONArray("leads_to");
		String[] unlocks = new String[0];
		if (unlocksJson != null) {
			unlocks = new String[unlocksJson.length()];
			for (int i = 0; i < unlocksJson.length(); i++) {
				unlocks[i] = unlocksJson.getString(i);
			}
		}

		return new Quest(json.getString("id"), json.getString("desc"), loadReward(json.optJSONObject("reward")), false, unlocks);
	}


	private void reloadEntries() {
		ArrayList<SelectEntry> completed = new ArrayList<>();
		ArrayList<SelectEntry> unlocked = new ArrayList<>();
		ArrayList<QuestSeries> completedSeries = new ArrayList<>();
		ArrayList<QuestSeries> unlockedSeries = new ArrayList<>();
		for (QuestSeries questSeries : series.values()) {
			boolean isCompleted = completedSeries.contains(questSeries);
			boolean isUnlocked = questSeries.unlocked;
			SelectEntry select = new SelectEntry(Localization.getLocalized(questSeries.id), () -> Game.setDisplay(new SeriesInfomationDisplay(this, questSeries)), true) {
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
				.setPositioning(new Point(Screen.w / 2 - 8 * 11, 30), RelPos.RIGHT)
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
				.setEntries(new StringEntry(Settings.getEntry("quests") + "; " + Settings.getEntry("tutorials"), Color.WHITE))
				.setSelectable(false)
				.createMenu()
		};

		updateEntries();
	}

	public static class SeriesInfomationDisplay extends Display {
		public SeriesInfomationDisplay(QuestsDisplay display, QuestSeries series) {
			super(false, true);
			ArrayList<ListEntry> entries = new ArrayList<>();

			int tQuestNum = series.getSeriesQuests().size();
			int completedQuestNum = getSeriesQuestsCompleted(series);
			entries.add(completedSeries.contains(series)? new StringEntry("Status: Completed (" + completedQuestNum + "/" + tQuestNum + ")", Color.GREEN):
				series.unlocked? new StringEntry("Status: Unlocked (" + completedQuestNum + "/" + tQuestNum + ")", Color.WHITE):
				new StringEntry("Status: Locked (" + completedQuestNum + "/" + tQuestNum + ")", Color.GRAY) // Locked series would not been shown...?
			);

			entries.add(new StringEntry("Tutorial: " + (series.tutorial ? "Yes" : "No"), series.tutorial ? Color.CYAN : Color.WHITE));
			entries.addAll(Arrays.asList(StringEntry.useLines("Description: " + Localization.getLocalized(series.description))));

			ArrayList<Quest> ongoingQuests = getOngoingSeriesQuests(series);
			entries.addAll(Arrays.asList(StringEntry.useLines(ongoingQuests.size() + " ongoing quests" + (ongoingQuests.size() > 0 ? ": " : "") + String.join(", ", ongoingQuests.stream().map(q -> Localization.getLocalized(q.id)).collect(Collectors.toList())))));

			entries.add(new BlankEntry());
			entries.add(new SelectEntry("View all quests of this series", () -> Game.setDisplay(new QuestListDisplay(series.getSeriesQuests()))));

			if (series.tutorial) {
				entries.add(new SelectEntry("Skip this tutorial series", () -> Game.setDisplay(new Display(false, true, new Menu.Builder(true, 1, RelPos.CENTER)
					.setSelectable(false)
					.setEntries(StringEntry.useLines(Color.RED, "minicraft.display.tutorial_skip.confirm_popup", "minicraft.display.popup.enter_confirm", "minicraft.display.popup.escape_cancel"))
					.createMenu()) {
						@Override
						public void tick(InputHandler input) {
							super.tick(input);
							if (input.getKey("select").clicked) {
								skipSeries(series);
								display.reloadEntries();
								if (display.menus[0].getSelection() > display.seriesEntries[display.selectedEntry].length) {
									display.menus[0].setSelection(display.seriesEntries[display.selectedEntry].length - 1);
								}

								if (display.previousSelection > display.seriesEntries[display.selectedEntry ^ 1].length) {
									display.previousSelection = display.seriesEntries[display.selectedEntry ^ 1].length - 1;
								}

								Game.exitDisplay();
							}
						}
				})));
			}

			menus = new Menu[] {
				new Menu.Builder(true, 0, RelPos.CENTER)
					.setPositioning(new Point(Screen.w / 2, 5), RelPos.BOTTOM)
					.setEntries(new StringEntry(Localization.getLocalized(series.id)))
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

		private static class QuestListDisplay extends Display {
			public QuestListDisplay(ArrayList<Quest> quests) {
				super(false, true,
					new Menu.Builder(true, 0, RelPos.CENTER, quests.stream().map(q -> new SelectEntry(q.id, () -> Game.setDisplay(new QuestInformationDisplay(q)), true) {
						@Override
						public int getColor(boolean isSelected) {
							return completedQuest.contains(q) ? Color.GREEN : q.unlocked ? Color.WHITE : Color.GRAY;
						}
					}).toArray(SelectEntry[]::new))
						.createMenu());
			}

			private static class QuestInformationDisplay extends Display {
				public QuestInformationDisplay(Quest quest) {
					super(false, true);
					ArrayList<ListEntry> entries = new ArrayList<>();

					QuestSeries series = quest.getSeries();
					int tQuestNum = series.getSeriesQuests().size();
					int completedQuestNum = getSeriesQuestsCompleted(series);

					boolean isCompleted = completedQuest.contains(quest);
					String state = isCompleted ? "Completed" : quest.unlocked ? "Unlocked" : "Locked";
					int color = isCompleted ? Color.GREEN : quest.unlocked ? Color.WHITE : Color.GRAY;

					entries.add(new StringEntry("Status: " + (questStatus.get(quest.id) == null ? "None" : questStatus.get(quest.id).toString())));
					entries.add(new BlankEntry());
					entries.addAll(Arrays.asList(StringEntry.useLines(Localization.getLocalized(quest.description))));

					menus = new Menu[] {
						new Menu.Builder(true, 1, RelPos.CENTER)
							.setPositioning(new Point(Screen.w / 2, 5), RelPos.BOTTOM)
							.setEntries(new StringEntry(Localization.getLocalized(series.id) + " (" + completedQuestNum + "/" + tQuestNum + ")"),
								new StringEntry(Localization.getLocalized(quest.id) + ": " + state, color))
							.setSelectable(false)
							.createMenu(),
						new Menu.Builder(true, 2, RelPos.CENTER)
							.setPositioning(new Point(Screen.w / 2, 40), RelPos.BOTTOM)
							.setEntries(entries)
							.setSelectable(false)
							.createMenu()
					};
				}
			}
		}
	}

	public static Quest getQuest(String id) {
		return quests.get(id);
	}

	public static ArrayList<Quest> getQuests() {
		return new ArrayList<>(quests.values());
	}
	public static ArrayList<Quest> getUnlockedQuests() {
		return new ArrayList<>(unlockedQuests);
	}
	public static ArrayList<Quest> getCompletedQuest() {
		return new ArrayList<>(completedQuest);
	}
	public static JSONObject getStatusQuests() {
		return new JSONObject(questStatus.toMap());
	}

	public static boolean isQuestDone(String id) {
		return completedQuest.contains(getQuest(id));
	}

	public static Object getQuestData(String id) {
		return questStatus.get(id);
	}
	public static void setQuestData(String id, Object val) {
		questStatus.put(id, val);
	}

	public static int getSeriesQuestsCompleted(String id) { return getSeriesQuestsCompleted(series.get(id)); }
	public static int getSeriesQuestsCompleted(QuestSeries series) {
		int num = 0;
		for (Quest quest : series.getSeriesQuests()) {
			if (completedQuest.contains(quest)) num++;
		}

		return num;
	}
	public static ArrayList<Quest> getOngoingSeriesQuests(QuestSeries series) {
		return new ArrayList<>(series.getSeriesQuests().stream().filter(q -> !completedQuest.contains(q) && q.unlocked).collect(Collectors.toList()));
	}

	public static void unlockQuest(String id) { unlockQuest(quests.get(id)); }
	public static void unlockQuest(Quest quest) {
		if (quest == null) return;
		if (unlockedQuests.contains(quest)) return;

		quest.unlocked = true;
		unlockedQuests.add(quest);
		Game.notifications.add(Localization.getLocalized("minicraft.notification.quest_unlocked") + ": " + Localization.getLocalized(quest.id));
	}
	public static void refreshQuestLocks() {
		for (Quest quest : quests.values()) {
			if (unlockedQuests.contains(quest) && !quest.unlocked) {
				unlockedQuests.remove(quest);
			} else if (!unlockedQuests.contains(quest) && quest.unlocked) {
				unlockedQuests.add(quest);
			}
		}
	}
	public static boolean questCompleted(String id) { return questCompleted(id, true); }
	public static boolean questCompleted(String id, boolean mustUnlocked) {
		Quest quest = quests.get(id);
		if (quest == null) return false;
		if (completedQuest.contains(quest)) return false;
		if (mustUnlocked && !quest.unlocked) return false;

		completedQuest.add(quest);
		Game.notifications.add(Localization.getLocalized("minicraft.notification.quest_completed") + ": " + Localization.getLocalized(id));
		sendReward(quest.reward);
		if (quest.getSeries().getSeriesQuests().stream().allMatch(q -> completedQuest.contains(q))) { // Checks if all the quests of the series are completed
			seriesCompleted(quest.getSeries());
		}

		for (String q : quest.getUnlocks()) unlockQuest(q);

		if (quest.callback != null) quest.callback.act();
		return true;
	}
	public static void unlockSeries(QuestSeries series) {
		if (series == null) return;
		if (unlockedSeries.contains(series)) return;

		series.unlocked = true;
		unlockedSeries.add(series);
		Game.notifications.add(Localization.getLocalized("minicraft.notification.quest_series_unlocked") + ": " + Localization.getLocalized(series.id));
		unlockQuest(series.getSeriesQuests().get(0));
	}
	public static void seriesCompleted(QuestSeries questSeries) {
		if (questSeries == null) return;
		if (completedSeries.contains(questSeries)) return;

		completedSeries.add(questSeries);
		Game.notifications.add(Localization.getLocalized("minicraft.notification.quest_series_completed") + ": " + Localization.getLocalized(questSeries.id));
		sendReward(questSeries.reward);

		for (String un : questSeries.getUnlocks()) {
			unlockSeries(series.get(un));
		}

		if ((boolean) Settings.get("tutorials") &&
			series.values().stream().filter(s -> s.tutorial && !completedSeries.contains(s)).count() == 0) { // Tutorial completed
			Logging.QUEST.debug("Tutorial completed.");
			tutorialOff(); // Turns off tutorial

			Game.notifications.add(Localization.getLocalized("minicraft.notification.tutorial_completed"));
		}

		if (questSeries.callback != null) questSeries.callback.act();
	}

	private static void skipSeries(QuestSeries series) {
		if (series == null) return;
		if (completedSeries.contains(series)) {
			seriesCompleted(series);
			return;
		}

		series.unlocked = false;
		unlockedSeries.remove(series);
		sendReward(series.reward, true);
		series.getSeriesQuests().stream().filter(q -> !completedQuest.contains(q)).map(q -> {
			sendReward(q.reward, true);
			return q;
		}).filter(q -> q.unlocked).forEach(q -> {
			q.unlocked = false;
			unlockedQuests.remove(q);
		});

		for (String un : series.getUnlocks()) {
			unlockSeries(QuestsDisplay.series.get(un));
		}

		if ((boolean) Settings.get("tutorials") &&
			QuestsDisplay.series.values().stream().filter(s -> s.tutorial && !completedSeries.contains(s)).count() == 0) { // Tutorial completed
			Logging.QUEST.debug("Tutorial completed.");
			tutorialOff(); // Turns off tutorial

			Game.notifications.add(Localization.getLocalized("minicraft.notification.tutorial_completed"));
		}
	}

	private static void sendReward(QuestReward reward) { sendReward(reward, false); }
	private static void sendReward(QuestReward reward, boolean recipeOnly) {
		if (reward == null) return;

		if (!recipeOnly) {
			ArrayList<Item> items = reward.getItems();
			if (items.size() > 0) {
				Chest chest = new Chest("Quest Series Reward");
				chest.x = World.player.x;
				chest.y = World.player.y;
				for (Item item : items) chest.getInventory().add(item);
				World.levels[World.currentLevel].add(chest);
			}
		}

		ArrayList<Recipe> recipes = reward.getRecipe();
		if (recipes.size() > 0) {
			recipes.forEach(recipe -> CraftingDisplay.unlockRecipe(recipe));
		}
	}

	/** Call only when the tutorial is completed or turned off. */
	public static void tutorialOff() {
		Settings.set("tutorials", false);

		// Locks all uncompleted tutorial series and quests.
		for (Quest quest : unlockedQuests.stream().filter(q -> !completedQuest.contains(q) && q.getSeries().tutorial).collect(Collectors.toList())) {
			quest.unlocked = false;
			unlockedQuests.remove(quest);
			quest.getSeries().unlocked = false;
			unlockedSeries.remove(quest.getSeries());
		}

		CraftingDisplay.unlockLeft();
		if ((boolean) Settings.get("quests")) { // Unlock initial quests
			for (QuestSeries qSeries : series.values()) {
				if (initiallyUnlocked.contains(qSeries.id) && !unlockedSeries.contains(qSeries)) {
					unlockSeries(qSeries);
				}
			}
		}
	}

	public static void resetGameQuests() {
		unlockedQuests.clear();
		completedQuest.clear();
		unlockedQuests.clear();
		completedQuest.clear();
		questStatus.clear();

		quests.values().forEach(q -> q.unlocked = false);
		if ((boolean) Settings.get("quests") || (boolean) Settings.get("tutorials")) {
			for (QuestSeries questSeries : series.values()) {
				if (initiallyUnlocked.contains(questSeries.id) && ((boolean) Settings.get("tutorials") && questSeries.tutorial || (boolean) Settings.get("quests") && !questSeries.tutorial)) {
					unlockSeries(questSeries);
				} else {
					questSeries.unlocked = false;
				}
			}
		}
	}

	public static void loadGameQuests(ArrayList<String> unlocked, ArrayList<String> completed) { loadGameQuests(unlocked, completed, new JSONObject()); }
	public static void loadGameQuests(ArrayList<String> unlocked, ArrayList<String> completed, JSONObject data) {
		resetGameQuests();

		for (String n : unlocked) {
			Quest q = quests.get(n);
			if (q != null && !unlockedQuests.contains(q))
				unlockedQuests.add(q);

			QuestSeries s = series.get(n);
			if (s != null && !unlockedSeries.contains(s))
				unlockedSeries.add(s);
		}

		for (String n : completed) {
			Quest q = quests.get(n);
			if (q != null)
				completedQuest.add(q);

			QuestSeries s = series.get(n);
			if (s != null)
				completedSeries.add(s);
		}

		for (String k : data.keySet()) {
			questStatus.put(k, data.get(k));
		}
	}

	@Override
	public void tick(InputHandler input) {
		super.tick(input);

		if (input.getKey("cursor-left").clicked) if (selectedEntry > 0) {
			selectedEntry--;
			updateEntries();
		}

		if (input.getKey("cursor-right").clicked) if (selectedEntry < 1) {
			selectedEntry++;
			updateEntries();
		}

		if (menus[0].getCurEntry() != null) {
			menus[3].setEntries(StringEntry.useLines(Localization.getLocalized(entrySeries[selectedEntry][menus[0].getSelection()].description)));
		} else {
			menus[3].setEntries(StringEntry.useLines(Localization.getLocalized("minicraft.displays.quests.display.no_quest_desc")));
		}
	}

	private void updateEntries() {
		menus[0].setEntries(seriesEntries[selectedEntry]);

		String[] entryNames = new String[] {
			"Unlocked", "Completed"
		};

		for (int i = 0; i < 2; i++) {
			menus[i+1].updateEntry(0, new StringEntry(entryNames[i], (i == selectedEntry) ? Color.WHITE : Color.GRAY));
		}

		int select = previousSelection;
		previousSelection = menus[0].getSelection();
		menus[0].setSelection(select);
	}
}
