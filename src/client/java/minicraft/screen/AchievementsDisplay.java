package minicraft.screen;

import minicraft.core.Game;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.core.io.Sound;
import minicraft.gfx.Color;
import minicraft.gfx.Dimension;
import minicraft.gfx.Font;
import minicraft.gfx.Insets;
import minicraft.gfx.MinicraftImage;
import minicraft.gfx.Point;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteLinker;
import minicraft.saveload.Save;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.SelectEntry;
import minicraft.screen.entry.StringEntry;
import minicraft.util.Achievement;
import minicraft.util.Logging;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AchievementsDisplay extends Display {

	private static final HashMap<String, Achievement> achievements = new HashMap<>();

	private static Achievement selectedAchievement;
	private static int achievementScore;

	static {
		// Get achievements from a json file stored in resources. Relative to project root.
		try (InputStream stream = Game.class.getResourceAsStream("/resources/achievements.json")) {
			if (stream != null) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

				// Read lines and combine into a string.
				String achievementsJson = reader.lines().collect(Collectors.joining("\n"));

				// Load json.
				JSONArray json = new JSONArray(achievementsJson);
				for (Object object : json) {
					JSONObject obj = (JSONObject) object;

					// Create an achievement with the data.
					Achievement a = new Achievement(
						Localization.getLocalized(obj.getString("id")),
						obj.getString("desc"),
						obj.getInt("score")
					);

					achievements.put(obj.getString("id"), a);
				}
			} else {
				Logging.ACHIEVEMENT.error("Could not find achievements json.");
			}
		} catch (IOException ex) {
			Logging.ACHIEVEMENT.error("Could not read achievements from json file.");
			ex.printStackTrace();
		} catch (JSONException e) {
			Logging.ACHIEVEMENT.error("Achievements json contains invalid json.");
		}
	}

	public AchievementsDisplay() {
		super(true, true,
			new Menu.Builder(false, 2, RelPos.CENTER, getAchievemensAsEntries()).setSize(48, 48).createMenu(),
			new Menu.Builder(false, 2, RelPos.BOTTOM, new StringEntry("")).setSize(200, 32).setPositioning(new Point(Screen.w / 2, Screen.h / 2 + 32), RelPos.BOTTOM).createMenu());
	}

	@Override
	public void init(@Nullable Display parent) {
		super.init(parent);
		if (achievements.isEmpty()) {
			Game.setDisplay(new TitleDisplay());
			Logging.ACHIEVEMENT.error("Could not open achievements menu because no achievements could be found.");
			return;
		}

		ListEntry curEntry = menus[0].getCurEntry();
		if (curEntry instanceof SelectEntry) {
			selectedAchievement = achievements.get(((SelectEntry) curEntry).getText());
		}
	}

	@Override
	public void onExit() {
		// Play confirm sound.
		Sound.play("confirm");
		new Save();
	}

	@Override
	public void tick(InputHandler input) {
		super.tick(input);

		ListEntry curEntry = menus[0].getCurEntry();
		if (curEntry instanceof SelectEntry) {
			selectedAchievement = achievements.get(((SelectEntry) curEntry).getText());
		}
	}

	@Override
	public void render(Screen screen) {
		super.render(screen);

		// Title.
		Font.drawCentered(Localization.getLocalized("minicraft.displays.achievements"), screen, 8, Color.WHITE);

		// Achievement score.
		Font.drawCentered(Localization.getLocalized("minicraft.displays.achievements.display.score", achievementScore), screen, 32, Color.GRAY);

		if (selectedAchievement != null) {

			// Render Achievement Info.
			if (selectedAchievement.getUnlocked()) {
				Font.drawCentered(Localization.getLocalized("minicraft.displays.achievements.display.achieved"), screen, 48, Color.GREEN);
			} else {
				Font.drawCentered(Localization.getLocalized("minicraft.displays.achievements.display.not_achieved"), screen, 48, Color.RED);
			}

			// Achievement description.
			menus[1].setEntries(StringEntry.useLines(Font.getLines(Localization.getLocalized(selectedAchievement.description), menus[1].getBounds().getSize().width, menus[1].getBounds().getSize().height, 2)));
		}

		// Help text.
		Font.drawCentered(Localization.getLocalized("minicraft.displays.achievements.display.help", Game.input.getMapping("cursor-down"), Game.input.getMapping("cursor-up")), screen, Screen.h - 8, Color.DARK_GRAY);
	}

	/**
	 * Use this to lock or unlock an achievement.
	 *
	 * @param id       Achievement ID.
	 * @param unlocked Whether this achievement should be locked or unlocked.
	 * @return True if setting the achievement was successful.
	 */
	public static boolean setAchievement(String id, boolean unlocked) {
		return setAchievement(id, unlocked, true, false);
	}

	public static boolean setAchievement(boolean allowCreative, String id, boolean unlocked) {
		return setAchievement(id, unlocked, true, allowCreative);
	}

	private static boolean setAchievement(String id, boolean unlocked, boolean save, boolean allowCreative) {
		Achievement a = achievements.get(id);

		// Return if it is in creative mode
		if (!allowCreative && Game.isMode("minicraft.settings.mode.creative")) return false;
		// Return if we didn't find any achievements.
		if (a == null) return false;

		if (a.getUnlocked() && unlocked) return false; // Return if it is already unlocked.
		if (!a.getUnlocked() && !unlocked) return false;  // Return if it is already locked.

		// Make the achievement unlocked in memory.
		a.setUnlocked(unlocked);
		Logging.ACHIEVEMENT.debug("Updating data of achievement with id: {}.", id);

		// Add or subtract from score
		if (unlocked) {
			achievementScore += a.score;

			// Tells the player that they got an achievement.
			Game.inGameToasts.add(new AchievementUnlockToast(id));
		} else
			achievementScore -= a.score; // Logical?

		// Save the new list of achievements stored in memory.
		if (save) new Save();

		return true;
	}

	private static class AchievementUnlockToast extends Toast {
		private static final int ICON_PADDING = 16; // 16 pixels wide space is reserved.

		private static final AchievementUnlockToastFrame FRAME = new AchievementUnlockToastFrame();

		private static class AchievementUnlockToastFrame extends ToastFrame {
			private static final SpriteLinker.LinkedSprite sprite =
				new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Gui, "toasts")
					.setSpriteDim(0, 6, 3, 3);

			protected AchievementUnlockToastFrame() {
				super(new Insets(3));
			}

			@Override
			public void render(Screen screen, int x, int y, int w, int h) {
				render(screen, x, y, w, h, sprite.getSprite());
			}
		}

		private final String title = Localization.getLocalized(
			"minicraft.toast.achievements.achievement_unlocked.title");
		private final String name;
		private final Dimension size;

		public AchievementUnlockToast(String id) {
			super(240); // 4 seconds
			name = Localization.getLocalized(id);
			size = new Dimension((Math.max(Font.textWidth(name), Font.textWidth(title)) +
				FRAME.paddings.left + FRAME.paddings.right + ICON_PADDING + 7) / 8 * 8,
				(Math.max(0, 2 * Font.textHeight() + SPACING +
					FRAME.paddings.top + FRAME.paddings.bottom) + 7) / 8 * 8);
		}

		@Override
		public void render(Screen screen) {
			// From the left
			int x = -size.width * (ANIMATION_TIME - animationTick) / ANIMATION_TIME; // Shifting with animation (sliding)
			int y = 0;
			FRAME.render(screen, x, y, size.width / 8, size.height / 8);
			screen.render(x + FRAME.paddings.left + (ICON_PADDING - MinicraftImage.boxWidth) / 2,
				y + FRAME.paddings.top + MinicraftImage.boxWidth / 2,
					Objects.requireNonNull(SpriteLinker.missingTexture(SpriteLinker.SpriteType.Item)));
			Font.draw(title, screen, x + FRAME.paddings.left + ICON_PADDING, y + FRAME.paddings.top, Color.GOLD);
			Font.draw(name, screen, x + FRAME.paddings.left + ICON_PADDING,
				y + FRAME.paddings.top + SPACING + Font.textHeight(), Color.WHITE);
		}
	}

	/**
	 * Gets an array of all the unlocked achievements.
	 *
	 * @return A string array with each unlocked achievement's id in it.
	 */
	public static String[] getUnlockedAchievements() {
		ArrayList<String> strings = new ArrayList<>();

		for (String id : achievements.keySet()) {
			if (achievements.get(id).getUnlocked()) {
				strings.add(id);
			}
		}

		return strings.toArray(new String[0]);
	}

	public static List<ListEntry> getAchievemensAsEntries() {
		List<ListEntry> l = new ArrayList<>();
		for (String id : achievements.keySet()) {
			// Add entry to list.
			l.add(new SelectEntry(id, null, true) {
				/**
				 * Change the color of the selection.
				 */
				@Override
				public int getColor(boolean isSelected) {
					if (achievements.get(id).getUnlocked()) {
						return Color.GREEN;
					} else {
						return Color.WHITE;
					}
				}
			});
		}

		return l;
	}

	/**
	 * Unlocks a list of achievements.
	 *
	 * @param unlockedAchievements An array of all the achievements we want to load, ids.
	 */
	public static void unlockAchievements(JSONArray unlockedAchievements) {
		for (Object id : unlockedAchievements.toList()) {
			if (!setAchievement(id.toString(), true, false, false)) {
				Logging.ACHIEVEMENT.warn("Could not load unlocked achievement with name {}.", id.toString());
			}
		}
	}
}
