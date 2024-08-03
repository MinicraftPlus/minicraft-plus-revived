package minicraft.screen;

import minicraft.core.Game;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.core.io.Settings;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Point;
import minicraft.gfx.Rectangle;
import minicraft.gfx.Screen;
import minicraft.saveload.Load;
import minicraft.screen.entry.StringEntry;
import minicraft.util.AdvancementElement;
import minicraft.util.Logging;
import minicraft.util.TutorialElement;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.function.Supplier;

public class TutorialDisplayHandler {
	private static final ArrayList<TutorialElement> tutorialElements = new ArrayList<>();
	private static TutorialElement currentOngoingElement = null;

	static {
		try {
			loadTutorialFile("/resources/tutorials.json");
		} catch (IOException e) {
			e.printStackTrace();
			Logging.TUTORIAL.error("Failed to load tutorials.");
		}
	}

	private static void loadTutorialFile(@SuppressWarnings("SameParameterValue") String filename) throws IOException {
		JSONObject json = new JSONObject(String.join("", Load.loadFile(filename)));
		for (int i = 0; i < json.length(); i++)
			tutorialElements.add(null);
		for (String key : json.keySet()) {
			loadTutorialElement(key, json.getJSONObject(key));
		}
	}

	private static void loadTutorialElement(String criterionName, JSONObject json) {
		HashMap<String, AdvancementElement.ElementCriterion> criteria = new HashMap<>();
		JSONObject criteriaJson = json.getJSONObject("criteria");
		if (criteriaJson.isEmpty()) throw new IndexOutOfBoundsException("criteria is empty.");
		for (String key : criteriaJson.keySet()) {
			JSONObject criterion = criteriaJson.getJSONObject(key);
			criteria.put(key, new AdvancementElement.ElementCriterion(criterion.getString("trigger"), criterion.getJSONObject("conditions")));
		}

		AdvancementElement.ElementRewards rewards = AdvancementElement.loadRewards(json.optJSONObject("rewards"));
		// Index is required as the JSONObject is using unordered map.
		tutorialElements.set(json.getInt("index"), new TutorialElement(criterionName, json.getString("description"), criteria, rewards));
	}

	private static final ArrayList<ControlGuide> controlGuides = new ArrayList<>();
	private static ControlGuide currentGuide = null;

	static {
		controlGuides.add(new ControlGuide(300, "move-up|move-down|move-left|move-right",
			() -> Localization.getLocalized("minicraft.control_guide.move",
				String.format("%s|%s|%s|%s", Game.input.getMapping("move-up"),
					Game.input.getMapping("move-left"), Game.input.getMapping("move-down"),
					Game.input.getMapping("move-right")))));
		controlGuides.add(new ControlGuide(1, "attack",
			() -> Localization.getLocalized("minicraft.control_guide.attack", Game.input.getMapping("attack"))));
		controlGuides.add(new ControlGuide(1, "menu",
			() -> Localization.getLocalized("minicraft.control_guide.menu", Game.input.getMapping("menu"))));
		controlGuides.add(new ControlGuide(1, "craft",
			() -> Localization.getLocalized("minicraft.control_guide.craft", Game.input.getMapping("craft"))));
	}

	public static class ControlGuide {
		private static int animation = 60;

		private final int duration; // The duration pressing the key; in ticks.
		private final String key; // From mapping.
		private final Supplier<String> display;
		private int interactedDuration = 0;

		private ControlGuide(int duration, String key, Supplier<String> display) {
			this.duration = duration;
			this.key = key;
			this.display = display;
		}

		private void tick() {
			if (key.contains("|")) {
				for (String k : key.split("\\|")) {
					if (Game.input.inputDown(k)) interactedDuration++;
				}
			} else if (Game.input.inputDown(key))
				interactedDuration++;
		}
	}

	/**
	 * Updating all data by the newly completed element.
	 */
	public static void updateCompletedElement(TutorialElement element) {
		if (!element.isCompleted()) return;
		if (!(boolean) Settings.get("tutorials")) return;
		refreshAll();
	}

	private static void refreshAll() {
		if (currentOngoingElement == null) {
			Settings.set("tutorials", false);
		} else {
			ArrayList<TutorialElement> revertedElements = new ArrayList<>(tutorialElements);
			Collections.reverse(revertedElements);
			TutorialElement completed = revertedElements.stream().filter(AdvancementElement::isCompleted).findFirst().orElse(null);
			if (completed != null && currentOngoingElement != null) {
				if (tutorialElements.indexOf(completed) > tutorialElements.indexOf(currentOngoingElement)) {
					currentOngoingElement = completed;
				}
			}
			if (currentOngoingElement != null && currentOngoingElement.isCompleted()) {
				getNextTutorial();
			}
		}
	}

	private static void getNextTutorial() {
		currentOngoingElement.deregisterCriteria(); // Completely disable the element.
		if (tutorialElements.indexOf(currentOngoingElement) < tutorialElements.size() - 1) {
			currentOngoingElement = tutorialElements.get(tutorialElements.indexOf(currentOngoingElement) + 1);
			currentOngoingElement.update();
		} else {
			turnOffTutorials(); // Completed tutorials.
		}
	}

	public static void skipCurrent() {
		if (currentOngoingElement != null) {
			getNextTutorial();
		}
	}

	public static boolean inTutorial() {
		return currentOngoingElement != null;
	}

	public static boolean inQuests() {
		return (boolean) Settings.get("quests") && currentGuide == null && currentOngoingElement == null;
	}

	public static void turnOffTutorials() {
		currentOngoingElement = null;
		Settings.set("tutorials", false);
		Logging.TUTORIAL.debug("Tutorial completed.");
		Game.notifications.add(Localization.getLocalized("minicraft.notification.tutorials_completed"));
	}

	private static void turnOffGuides() {
		currentGuide = null; // Completed guide.
		if ((boolean) Settings.get("tutorials")) {
			currentOngoingElement = tutorialElements.get(0);
			currentOngoingElement.update();
		}
	}

	public static void tick(InputHandler input) {
		if (currentGuide != null) {
			if (ControlGuide.animation > 0) ControlGuide.animation--;
			if (input.getMappedKey("expandQuestDisplay").isClicked()) {
				Logging.TUTORIAL.debug("Force-completed the guides.");
				turnOffGuides();
				return;
			}

			if (currentGuide.interactedDuration >= currentGuide.duration) {
				if (controlGuides.indexOf(currentGuide) < controlGuides.size() - 1) {
					currentGuide = controlGuides.get(controlGuides.indexOf(currentGuide) + 1);
					ControlGuide.animation = 60;
				} else {
					turnOffGuides(); // Completed guide.
				}

				return;
			}

			if (Game.getDisplay() == null)
				currentGuide.tick();
		}

		if (currentOngoingElement != null) {
			if (input.getMappedKey("expandQuestDisplay").isClicked() && Game.getDisplay() == null) {
				Game.setDisplay(new PopupDisplay(new PopupDisplay.PopupConfig(currentOngoingElement.key, null, 4),
					currentOngoingElement.description));
			}
		}
	}

	/**
	 * Rendering directly on the GUI/HUD.
	 */
	public static void render(Screen screen) {
		if (currentGuide != null) { // Is ongoing.
			String[] lines = Font.getLines(currentGuide.display.get(), Screen.w, Screen.h, 0);
			if (ControlGuide.animation > 0) {
				int textWidth = Font.textWidth(lines);
				int xPadding = Screen.w / 2 - (textWidth + 8) / 2;
				int yPadding = Screen.h / 2 - (lines.length * 8 + 8) / 2;
				screen.fillRect(xPadding + 1, yPadding + 1, textWidth + 6, lines.length * 8 + 6, Color.BLUE);
				screen.drawAxisLine(xPadding, yPadding, 0, textWidth + 8, Color.WHITE); // left border
				screen.drawAxisLine(xPadding, yPadding + lines.length * 8 + 7, 0, textWidth + 8, Color.WHITE); // right border
				screen.drawAxisLine(xPadding, yPadding, 1, lines.length * 8 + 8, Color.WHITE); // top border
				screen.drawAxisLine(xPadding + lines.length * 8 + 7, yPadding, 1, lines.length * 8 + 8, Color.WHITE); // bottom border

				int yPad = Screen.h/2 - (lines.length * 8)/2;
				for (int i = 0; i < lines.length; i++) {
					Font.drawCentered(lines[i], screen, yPad + 8 * i, Color.WHITE);
				}
			} else {
				Menu menu = new Menu.Builder(true, 0, RelPos.RIGHT)
					.setPositioning(new Point(Screen.w - 9, 9), RelPos.BOTTOM_LEFT)
					.setSelectable(false)
					.setEntries(StringEntry.useLines(Color.WHITE, false, lines))
					.createMenu();
				menu.render(screen);
				Rectangle bounds = menu.getBounds();
				int length = bounds.getWidth() - 4;
				int bottom = bounds.getBottom() - 2;
				int left = bounds.getLeft() + 2;
				screen.drawAxisLine(left, bottom, 0, length * currentGuide.interactedDuration / currentGuide.duration, Color.WHITE);
				screen.drawAxisLine(left, bottom - 1, 0, length * currentGuide.interactedDuration / currentGuide.duration, Color.WHITE);
			}
		} else if (currentOngoingElement != null) { // Is ongoing.
			Menu menu = new Menu.Builder(true, 0, RelPos.RIGHT)
				.setPositioning(new Point(Screen.w - 9, 9), RelPos.BOTTOM_LEFT)
				.setSelectable(false)
				.setEntries(StringEntry.useLines(Color.WHITE, true, currentOngoingElement.key))
				.createMenu();
			menu.render(screen);
			Rectangle bounds = menu.getBounds();
			String text = Localization.getLocalized("minicraft.displays.tutorial_display_handler.display.element_examine_help",
				Game.input.getMapping("expandQuestDisplay"));
			String[] lines = Font.getLines(text, Screen.w * 2 / 3, Screen.h, 0);
			for (int i = 0; i < lines.length; i++)
				Font.draw(lines[i], screen, bounds.getRight() - Font.textWidth(lines[i]), bounds.getBottom() + 8 * (1 + i), Color.GRAY);
		}
	}

	public static void reset(boolean initial) {
		currentOngoingElement = null;
		tutorialElements.forEach(TutorialElement::reset);
		if (initial) { // The guide is shown only when the world is first created.
			controlGuides.forEach(c -> c.interactedDuration = 0);
			currentGuide = controlGuides.get(0);
			ControlGuide.animation = 60;
		} else {
			currentGuide = null;
		}
	}

	public static void load(JSONObject json) {
		reset(false);
		String tutorialKey = json.optString("CurrentOngoingTutorial", null);
		currentOngoingElement = tutorialKey == null ? null : tutorialElements.stream()
			.filter(element -> element.key.equals(tutorialKey)).findFirst().orElse(null);
		for (String k : json.keySet()) {
			tutorialElements.stream().filter(e -> e.key.equals(k))
				.findFirst().ifPresent(element -> element.load(json.getJSONObject(k)));
		}

		if (currentOngoingElement != null) currentOngoingElement.update();
	}

	/**
	 * Saving and writing all data into the given JSONObject.
	 */
	public static void save(JSONObject json) {
		if (currentOngoingElement != null) json.put("CurrentOngoingTutorial", currentOngoingElement.key);
		tutorialElements.forEach(element -> element.save(json));
	}
}
