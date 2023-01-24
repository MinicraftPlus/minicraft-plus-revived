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
import minicraft.item.Recipe;
import minicraft.saveload.Load;
import minicraft.screen.entry.SelectEntry;
import minicraft.screen.entry.StringEntry;
import minicraft.util.AdvancementElement;
import minicraft.util.Logging;
import minicraft.util.TutorialElement;
import org.json.JSONArray;
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
		for (String key : json.keySet()) {
			loadTutorialElement(key, json.getJSONObject(key));
		}
	}

	private static void loadTutorialElement(String criterionName, JSONObject json) {
		HashMap<String, AdvancementElement.ElementCriterion> criteria = new HashMap<>();
		JSONObject criteriaJson = json.getJSONObject("criteria");
		for (String key : criteriaJson.keySet()) {
			JSONObject criterion = criteriaJson.getJSONObject(key);
			criteria.put(key, new AdvancementElement.ElementCriterion(criterion.getString("trigger"), criterion.getJSONObject("conditions")));
		}

		AdvancementElement.ElementRewards rewards = AdvancementElement.loadRewards(json.optJSONObject("rewards"));
		tutorialElements.add(new TutorialElement(criterionName, json.getString("description"), criteria, rewards));
	}

	private static final ArrayList<ControlGuide> controlGuides = new ArrayList<>();
	private static ControlGuide currentGuide = null;

	static {
		controlGuides.add(new ControlGuide(180, "move-up",
			() -> Localization.getLocalized("minicraft.control_guide.move_up", Game.input.getMapping("move-up"))));
		controlGuides.add(new ControlGuide(180, "move-down",
			() -> Localization.getLocalized("minicraft.control_guide.move_down", Game.input.getMapping("move-down"))));
		controlGuides.add(new ControlGuide(180, "move-left",
			() -> Localization.getLocalized("minicraft.control_guide.move_left", Game.input.getMapping("move-left"))));
		controlGuides.add(new ControlGuide(180, "move-right",
			() -> Localization.getLocalized("minicraft.control_guide.move_right", Game.input.getMapping("move-right"))));
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
		private final String key;
		private final Supplier<String> display;
		private int interactedDuration = 0;

		private ControlGuide(int duration, String key, Supplier<String> display) {
			this.duration = duration;
			this.key = key;
			this.display = display;
		}

		private void tick() {
			if (Game.input.getKey(key).down)
				interactedDuration++;
		}
	}

	/** Updating all data by the newly completed element. */
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
		}
	}

	public static void skipCurrent() {
		if (currentOngoingElement != null) {
			if (tutorialElements.indexOf(currentOngoingElement) < tutorialElements.size() - 1) {
				currentOngoingElement = tutorialElements.get(tutorialElements.indexOf(currentOngoingElement) + 1);
			} else {
				turnOffTutorials(); // Completed tutorials.
			}
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
		Game.notifications.add(Localization.getLocalized("minicraft.notification.tutorial_completed"));
	}

	private static void turnOffGuides() {
		currentGuide = null; // Completed guide.
		if ((boolean) Settings.get("tutorials"))
			currentOngoingElement = tutorialElements.get(0);
	}

	public static void tick(InputHandler input) {
		if (currentGuide != null) {
			if (ControlGuide.animation > 0) ControlGuide.animation--;
			if (input.getKey("expandQuestDisplay").clicked) {
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

			currentGuide.tick();
		}

		if (currentOngoingElement != null) {
			if (input.getKey("expandQuestDisplay").clicked) {
				Game.setDisplay(new PopupDisplay(new PopupDisplay.PopupConfig(currentOngoingElement.key, null, 4),
					currentOngoingElement.description));
			}
		}
	}

	/** Rendering directly on the GUI/HUD. */
	public static void render(Screen screen) {
		if (currentGuide != null) { // Is ongoing.
			String[] lines = Font.getLines(currentGuide.display.get(), Screen.w, Screen.h, 0);
			if (ControlGuide.animation > 0) {
				int textWidth = Font.textWidth(lines);
				int xPadding = Screen.w/2 - (textWidth + 8)/2;
				int yPadding = Screen.h/2 - (lines.length * 8 + 8)/2;
				int yPad = Screen.h/2 - (lines.length * 8)/2;
				for (int i = 0; i < lines.length * 8 + 8; i++) { // Background.
					for (int j = 0; j < textWidth + 8; j++) {
						screen.pixels[xPadding + j + (yPadding + i) * Screen.w] =
							i == 0 || i == lines.length * 8 + 7 ||
								j == 0 || j == textWidth + 7 ? Color.WHITE : Color.BLUE;
					}
				}

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
				for (int i = 0; i < length * currentGuide.interactedDuration / currentGuide.duration; i++) {
					screen.pixels[left + i + bottom * Screen.w] = Color.WHITE;
					screen.pixels[left + i + (bottom - 1) * Screen.w] = Color.WHITE;
				}
			}
		} else if (currentOngoingElement != null) { // Is ongoing.
			new Menu.Builder(true, 0, RelPos.RIGHT)
				.setPositioning(new Point(Screen.w - 9, 9), RelPos.BOTTOM_LEFT)
				.setSelectable(false)
				.setEntries(StringEntry.useLines(Color.WHITE, false, currentOngoingElement.key))
				.createMenu()
				.render(screen);
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
	}

	/** Saving and writing all data into the given JSONObject. */
	public static void save(JSONObject json) {
		if (currentOngoingElement != null) json.put("CurrentOngoingTutorial", currentOngoingElement.key);
		tutorialElements.forEach(element -> element.save(json));
	}
}
