package minicraft.util;

import java.util.ArrayList;
import java.util.Arrays;

import minicraft.core.Action;
import minicraft.item.Item;
import minicraft.item.Recipe;

public class Quest {
    public final String id, description;
	public final QuestReward reward;
    public boolean unlocked;
	public Action callback;

    private final String[] unlocks;
	private QuestSeries series;

	public Quest(String name, String des, QuestReward reward, boolean unlocked, String[] unlocks) {
		this.id = name;
		description = des;
		this.reward = reward;
		this.unlocked = unlocked;
		this.unlocks = unlocks;
	}

	public String[] getUnlocks() { return unlocks.clone(); }
	public QuestSeries getSeries() { return series; }

	public static class QuestSeries {
		public final String id, description;
		public final boolean tutorial;
		public final QuestReward reward;
		public boolean unlocked;
		public Action callback;

		private final String[] unlocks;
		private final ArrayList<Quest> quests;

		public QuestSeries(String id, String desc, ArrayList<Quest> quests, QuestReward reward, boolean unlocked, String[] unlocks) { this(id, desc, quests, reward, unlocked, false, unlocks); }
		public QuestSeries(String id, String desc, ArrayList<Quest> quests, QuestReward reward, boolean unlocked, boolean tutorial, String[] unlocks) {
			this.id = id;
			this.description = desc;
			this.quests = quests;
			quests.forEach(q -> q.series = this);
			this.reward = reward;
			this.unlocked = unlocked;
			this.tutorial = tutorial;
			this.unlocks = unlocks;
		}

		public String[] getUnlocks() { return unlocks.clone(); }
		public ArrayList<Quest> getSeriesQuests() { return new ArrayList<>(quests); }
	}

	public static class QuestReward {
		private final ArrayList<Item> items;
		private final ArrayList<Recipe> recipes;

		public QuestReward(ArrayList<Item> items, ArrayList<Recipe> recipes) {
			this.items = items;
			this.recipes = recipes;
		}

		public ArrayList<Item> getItems() { return new ArrayList<>(items); }
		public ArrayList<Recipe> getRecipe() { return new ArrayList<>(recipes); }
	}
}
