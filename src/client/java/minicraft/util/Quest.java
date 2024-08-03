package minicraft.util;

import minicraft.screen.QuestsDisplay;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Quest extends AdvancementElement {
	private QuestSeries series = null;
	private final @Nullable String parent;

	public Quest(String key, String description, Map<String, ElementCriterion> criteria, @Nullable AdvancementElement.ElementRewards rewards,
	             @NotNull Set<HashSet<String>> requirements, @Nullable String parent,
	             @NotNull HashMap<String, ElementCriterion> unlockingCriteria, @NotNull Set<HashSet<String>> unlockingRequirements) {
		super(key, description, criteria, rewards, requirements, unlockingCriteria, unlockingRequirements);
		this.parent = parent;
	}

	public QuestSeries getSeries() {
		return series;
	}

	public @Nullable Quest getParent() {
		if (parent != null && series != null) {
			return series.quests.get(parent);
		}

		return null;
	}

	@Override
	protected boolean isUnlockable() {
		if (unlocked) return true;
		if (!series.unlocked) return false;
		Quest parent = getParent();
		return super.isUnlockable() && (parent == null || parent.isCompleted());
	}

	@Override
	public void update() {
		super.update();
		series.update();
		QuestsDisplay.refreshDisplayableQuests();
	}

	private void update0() {
		super.update();
		QuestsDisplay.refreshDisplayableQuests();
	}

	public static class QuestSeries extends AdvancementElement {
		private final HashMap<String, Quest> quests = new HashMap<>();

		public QuestSeries(String key, String description, Map<String, AdvancementElement.ElementCriterion> criteria,
		                   @Nullable AdvancementElement.ElementRewards rewards, @NotNull Set<HashSet<String>> requirements,
		                   @NotNull Map<String, Quest> quests, @NotNull HashMap<String, ElementCriterion> unlockingCriteria,
		                   @NotNull Set<HashSet<String>> unlockingRequirements) {
			super(key, description, criteria, rewards, requirements, unlockingCriteria, unlockingRequirements);
			this.quests.putAll(quests);
			quests.values().forEach(q -> q.series = this);
		}

		public HashMap<String, Quest> getSeriesQuests() {
			return new HashMap<>(quests);
		}

		@Override
		protected boolean checkIsCompleted() {
			return quests.values().stream().allMatch(quest -> quest.completed) && super.checkIsCompleted();
		}

		@Override
		public void update() {
			super.update();
			quests.values().forEach(Quest::update0);
			QuestsDisplay.refreshDisplayableQuests();
		}
	}
}
