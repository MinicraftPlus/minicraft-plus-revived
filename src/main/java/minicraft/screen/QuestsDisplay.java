package minicraft.screen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONObject;

import minicraft.core.Game;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.gfx.Color;
import minicraft.gfx.Point;
import minicraft.gfx.Screen;
import minicraft.saveload.Load;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.SelectEntry;
import minicraft.screen.entry.StringEntry;
import minicraft.util.Quest;

public class QuestsDisplay extends Display {
	private static HashMap<String, Quest> quests = new HashMap<>();
	private static ArrayList<Quest> unlockedQuests = new ArrayList<>();
	private static ArrayList<Quest> doneQuests = new ArrayList<>();
	private static HashMap<String, QuestStatus> questStatus = new HashMap<>();
	private static ArrayList<String> initiallyUnlocked = new ArrayList<>();
	private boolean atCorner;
	private Menu.Builder builder;
	private SelectEntry[][] questEntries;
	private int selectedEntry;
	private HashMap<String, Quest> questWithLocalized;
	private boolean entrySelected = false;

	static {
		try {
			JSONArray json = new JSONArray(String.join("", Load.loadFile("/resources/quests.json")));
			for (int i = 0; i < json.length(); i++) {
				JSONObject obj = json.getJSONObject(i);
				String id = obj.getString("id");
				// Is unlocked initially
				boolean unlocked = obj.optBoolean("unlocked", false);
				JSONArray unlocksJson = obj.getJSONArray("unlocks");
				String[] unlocks = new String[unlocksJson.length()];
				for (int j = 0; j < unlocksJson.length(); j++) unlocks[j] = unlocksJson.getString(j);
				if (unlocked) initiallyUnlocked.add(id);
				quests.put(id, unlocked? new Quest(id, obj.getString("desc"), unlocked, unlocks): new Quest(id, obj.getString("desc"), unlocks));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public QuestsDisplay() { this(false); }
	public QuestsDisplay(boolean atCornerDisplay) {
		super(!atCornerDisplay, !atCornerDisplay);
		atCorner = atCornerDisplay;
		if (atCornerDisplay) {
			builder = new Menu.Builder(true, 0, RelPos.RIGHT)
				.setPositioning(new Point(Screen.w-9, 9), RelPos.BOTTOM_LEFT)
				.setDisplayLength(3)
				.setTitle("Quests")
				.setSelectable(false);
			menus = new Menu[] {
				builder.createMenu()
			};
		} else {
			ArrayList<SelectEntry> undone = new ArrayList<>();
			ArrayList<SelectEntry> done = new ArrayList<>();
			ArrayList<SelectEntry> locked = new ArrayList<>();
			ArrayList<SelectEntry> unlocked = new ArrayList<>();
			ArrayList<SelectEntry> all = new ArrayList<>();
			questWithLocalized = new HashMap<>();
			for (Quest quest : quests.values()) {
				boolean isUnlocked = quest.getUnlocked();
				boolean isDone = doneQuests.contains(quest);
				SelectEntry select = new SelectEntry(Localization.getLocalized(quest.id), () -> entrySelected(quest), true) {
					@Override
					public int getColor(boolean isSelected) {
						if (isDone) return Color.GREEN;
						else if (isUnlocked) return Color.WHITE;
						else return Color.GRAY;
					}
				};
				questWithLocalized.put(select.getText(), quest);
				all.add(select);
				if (isDone) done.add(select);
				else undone.add(select);
				if (isUnlocked) unlocked.add(select);
				else locked.add(select);
			}
			questEntries = new SelectEntry[][] {
				all.toArray(new SelectEntry[0]),
				locked.toArray(new SelectEntry[0]),
				unlocked.toArray(new SelectEntry[0]),
				undone.toArray(new SelectEntry[0]),
				done.toArray(new SelectEntry[0])
			};
			builder = new Menu.Builder(true, 0, RelPos.CENTER)
				.setPositioning(new Point(Screen.w/2, Screen.h/2), RelPos.CENTER)
				.setSelectable(false)
				.setShouldRender(false);
			menus = new Menu[] {
				new Menu.Builder(false, 0, RelPos.CENTER)
					.setPositioning(new Point(Screen.w/2, Screen.h/2-20), RelPos.CENTER)
					.setDisplayLength(5)
					.setSelectable(true)
					.createMenu(),
				builder.createMenu(),
				new Menu.Builder(false, 0, RelPos.LEFT)
					.setPositioning(new Point(16, 30), RelPos.RIGHT)
					.setEntries(new StringEntry("All", Color.GRAY))
					.setSelectable(false)
					.createMenu(),
				new Menu.Builder(false, 0, RelPos.LEFT)
					.setPositioning(new Point(16+8*4, 30), RelPos.RIGHT)
					.setEntries(new StringEntry("Locked", Color.GRAY))
					.setSelectable(false)
					.createMenu(),
				new Menu.Builder(false, 0, RelPos.LEFT)
					.setPositioning(new Point(16+8*11, 30), RelPos.RIGHT)
					.setEntries(new StringEntry("Unlocked", Color.GRAY))
					.setSelectable(false)
					.createMenu(),
				new Menu.Builder(false, 0, RelPos.LEFT)
					.setPositioning(new Point(16+8*20, 30), RelPos.RIGHT)
					.setEntries(new StringEntry("Undone", Color.GRAY))
					.setSelectable(false)
					.createMenu(),
				new Menu.Builder(false, 0, RelPos.LEFT)
					.setPositioning(new Point(16+8*27, 30), RelPos.RIGHT)
					.setEntries(new StringEntry("Done", Color.GRAY))
					.setSelectable(false)
					.createMenu(),
				new Menu.Builder(false, 0, RelPos.CENTER)
					.setPositioning(new Point(Screen.w/2, Screen.h/2+35), RelPos.CENTER)
					.setEntries(new StringEntry(Localization.getLocalized("minicraft.display.quests.no_desc")))
					.setSelectable(false)
					.createMenu()
			};
			selectedEntry = 0;
			updateEntries();
		}
	}

	public static Quest getQuest(String name) {
		return quests.get(name);
	}

	public static ArrayList<Quest> getQuests() {
		return new ArrayList<>(quests.values());
	}
	public static ArrayList<Quest> getUnlockedQuests() {
		return unlockedQuests;
	}
	public static ArrayList<Quest> getDoneQuests() {
		return doneQuests;
	}
	public static HashMap<String, QuestStatus> getStatusQuests() {
		return questStatus;
	}

	public static boolean isQuestDone(String name) {
		return doneQuests.contains(getQuest(name));
	}

	public static Object getQuestData(String name) {
		return questStatus.get(name);
	}
	public static void setQuestData(String name, QuestStatus val) {
		questStatus.put(name, val);
	}
	public static void setQuestDataVal(String name, Object val) {
		questStatus.get(name).val = val;
	}

	public static void unlockQuest(String name) {
		Quest quest = quests.get(name);
		if (quest == null) return;
		if (unlockedQuests.contains(quest)) return;
		quest.unlock();
		unlockedQuests.add(quest);
		Game.notifications.add(Localization.getLocalized("minicraft.notification.quest_unlocked") + " " + Localization.getLocalized(name));
	}
	public static void doneQuest(String name) {
		Quest quest = quests.get(name);
		if (quest == null) return;
		if (doneQuests.contains(quest)) return;
		doneQuests.add(quest);
		Game.notifications.add(Localization.getLocalized("minicraft.notification.quest_done") + " " + Localization.getLocalized(name));
		for (String q : quest.getUnlocks()) unlockQuest(q);
	}

	private static void reinitializeQuests() {
		unlockedQuests.clear();
		doneQuests.clear();
		questStatus.clear();
		for (Quest quest : quests.values()) {
			if (initiallyUnlocked.contains(quest.id)) {
				quest.unlock();
				unlockedQuests.add(quest);
			} else {
				quest.lock();
			}
		}
	}

	public static void loadGameQuests(ArrayList<String> unlocked, ArrayList<String> done) { loadGameQuests(unlocked, done, new HashMap<>()); }
	public static void loadGameQuests(ArrayList<String> unlocked, ArrayList<String> done, Map<String, String> data) {
		reinitializeQuests();
		for (String n : unlocked) {
			Quest q = getQuest(n);
			if (!unlockedQuests.contains(q)) unlockedQuests.add(q);
		}
		for (String n : done) doneQuests.add(getQuest(n));
		for (Entry<String, String> e : data.entrySet()) questStatus.put(e.getKey(), QuestStatus.fromStringType(e.getValue()));
	}
	public static void loadGameQuestsObj(ArrayList<Quest> unlocked, ArrayList<Quest> done) { loadGameQuestsObj(unlocked, done, new HashMap<>()); }
	public static void loadGameQuestsObj(ArrayList<Quest> unlocked, ArrayList<Quest> done, Map<String, String> data) {
		reinitializeQuests();
		for (Quest q : unlocked) if (!unlockedQuests.contains(q)) unlockedQuests.add(q);
		doneQuests.addAll(done);
		for (Entry<String, String> e : data.entrySet()) questStatus.put(e.getKey(), QuestStatus.fromStringType(e.getValue()));
	}

	@Override
	public void tick(InputHandler input) {
		if (entrySelected) {
			if (input.getKey("exit").clicked) {
				menus[0].shouldRender = true;
				entrySelected = false;
			}
			return;
		}
		super.tick(input);
		if (atCorner) {
			ArrayList<String> undoneQuests = new ArrayList<>();
			for (Quest q : unlockedQuests) if (!doneQuests.contains(q)) undoneQuests.add(Localization.getLocalized(q.id)+(questStatus.get(q.id) != null? " | "+questStatus.get(q.id): ""));
			menus[0] = builder.setDisplayLength(undoneQuests.size() > 3? 3: undoneQuests.size())
				.setEntries(StringEntry.useLines(undoneQuests.toArray(new String[0])))
				.createMenu();
		} else {
			if (input.getKey("cursor-left").clicked) if (selectedEntry > 0) {
				selectedEntry--;
				updateEntries();
			};
			if (input.getKey("cursor-right").clicked) if (selectedEntry < 4) {
				selectedEntry++;
				updateEntries();
			};
			if (menus[0].getCurEntry() != null) menus[7].setEntries(StringEntry.useLines(Localization.getLocalized(questWithLocalized.get(((SelectEntry)menus[0].getCurEntry()).getText()).description).split("\n")));
			else menus[7].setEntries(StringEntry.useLines(Localization.getLocalized("minicraft.display.quests.no_desc")));
		}
	}

	@Override
	public void render(Screen screen) {
		if (entrySelected) {
			menus[1].render(screen);
			return;
		}
		super.render(screen);
	}

	private void updateEntries() {
		menus[0].setEntries(questEntries[selectedEntry]);
		String[] entryNames = new String[] {
			"All", "Locked", "Unlocked", "Undone", "Done"
		};
		for (int i = 0; i<5; i++)
			if (i == selectedEntry) menus[i+2].updateEntry(0, new StringEntry(entryNames[i], Color.WHITE));
			else menus[i+2].updateEntry(0, new StringEntry(entryNames[i], Color.GRAY));
		if (menus[0].getSelection() >= menus[0].getEntries().length) menus[0].setSelection(menus[0].getEntries().length-1);;
	}

	private void entrySelected(Quest quest) {
		entrySelected = true;
		ArrayList<ListEntry> e = new ArrayList<>();
		e.add(new StringEntry(Localization.getLocalized(quest.id)));
		boolean isUnlocked = quest.getUnlocked();
		boolean isDone = doneQuests.contains(quest);
		e.add(isUnlocked? isDone? new StringEntry("Done", Color.GREEN): new StringEntry("Unlocked", Color.WHITE): new StringEntry("Locked", Color.GRAY));
		e.add(new StringEntry(""));
		for (String s : Localization.getLocalized(quest.description).split("\n")) e.add(new StringEntry(s));
		menus[0].shouldRender = false;
		menus[1] = builder.setEntries(e).createMenu();
	}

	public static class QuestStatus {
		public static enum Types {
			String (String.class, TypeConverters.STRING),
			Int (Integer.class, TypeConverters.INTEGER),
			Double (Double.class, TypeConverters.DOUBLE);

			public final Class<?> valueType;
			public final TypeConverter<?> typeConverter;
			Types(Class<?> type, TypeConverter<?> convertor) {
				valueType = type;
				typeConverter = convertor;
			}
		}
		public final Types type;
		public final boolean hasMax;
		public final Object maxVal;
		public Object val;

		public QuestStatus(Types type, boolean hasMax, Object iniVal, Object maxVal) {
			this.type = type;
			this.hasMax = hasMax;
			this.maxVal = maxVal;
			val = iniVal;
		}

		public static QuestStatus fromString(Types type, boolean hasMax, String text) {
			if (hasMax) {
				String[] m = text.split("/");
				return new QuestStatus(type, hasMax, type.typeConverter.fromString(m[0]), type.typeConverter.fromString(m[1]));
			} else return new QuestStatus(type, hasMax, type.typeConverter.fromString(text), null);
		}

		/** String format follow to {@link #toQuestString()} */
		public static QuestStatus fromStringType(String text) {
			String[] t = text.split(";", 3);
			return fromString(Types.valueOf(t[0]), Boolean.parseBoolean(t[1]), t[2]);
		}

		@Override
		public String toString() {
			return val.toString()+(hasMax? "/"+maxVal.toString(): "");
		}

		public String toQuestString() {
			return type.toString() + ";" + hasMax + ";" + toString();
		}
	}

	@FunctionalInterface
	public static interface TypeConverter<T> {
		public T fromString(String text);
	}

	// Convertors which convert String to other types
	public static class TypeConverters {
		public static TypeConverter<String> STRING = text -> text;
		public static TypeConverter<Integer> INTEGER = text -> Integer.parseInt(text);
		public static TypeConverter<Double> DOUBLE = text -> Double.parseDouble(text);
	}
}
