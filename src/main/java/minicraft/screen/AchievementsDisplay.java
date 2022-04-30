package minicraft.screen;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import minicraft.core.Game;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.core.io.Sound;
import minicraft.gfx.*;
import minicraft.saveload.Save;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.SelectEntry;
import minicraft.screen.entry.StringEntry;
import minicraft.util.Achievement;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.tinylog.Logger;

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
                Logger.error("Could not find achievements json.");
            }
        } catch (IOException ex) {
            Logger.error("Could not read achievements from json file.");
            ex.printStackTrace();
        } catch (JSONException e) {
            Logger.error("Achievements json contains invalid json.");
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
            Logger.error("Could not open achievements menu because no achievements could be found.");
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
        Sound.confirm.play();
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
        Font.drawCentered(Localization.getLocalized("minicraft.display.achievement"), screen, 8, Color.WHITE);

        // Achievement score.
        Font.drawCentered(Localization.getLocalized("minicraft.display.achievement.score") + " " + achievementScore, screen, 32, Color.GRAY);

        if (selectedAchievement != null) {

            // Render Achievement Info.
            if (selectedAchievement.getUnlocked()) {
                Font.drawCentered(Localization.getLocalized("minicraft.display.achievement.achieved"), screen, 48, Color.GREEN);
            } else {
                Font.drawCentered(Localization.getLocalized("minicraft.display.achievement.not_achieved"), screen, 48, Color.RED);
            }

            // Achievement description.
            menus[1].setEntries(StringEntry.useLines(Font.getLines(Localization.getLocalized(selectedAchievement.description), menus[1].getBounds().getSize().width, menus[1].getBounds().getSize().height, 2)));
        }

        // Help text.
        Font.drawCentered("Use " + Game.input.getMapping("cursor-down") + " and " + Game.input.getMapping("cursor-up") + " to move.", screen, Screen.h - 8, Color.DARK_GRAY);
    }

    /**
     * Use this to lock or unlock an achievement.
     * @param id Achievement ID.
     * @param unlocked Whether this achievement should be locked or unlocked.
     * @return True if setting the achievement was successful.
     */
    public static boolean setAchievement(String id, boolean unlocked) {
        return setAchievement(id, unlocked, true, false);
    }

    private static boolean setAchievement(String id, boolean unlocked, boolean save, boolean isMenu) {
        Achievement a = achievements.get(id);

        // Return if we didn't find any achievements.
        if (a == null) return false;

        if (a.getUnlocked() && unlocked) return false; // Return if it is already unlocked.
        if (!a.getUnlocked() && !unlocked) return false;  // Return if it is already locked.

        // Make the achievement unlocked in memory.
		if (!Game.isMode("creative") || isMenu) {
        	a.setUnlocked(unlocked);
        	Logger.debug("Updating data of achievement with id: {}.", id);
		}

        // Add or subtract from score
        if (unlocked) {
            achievementScore += a.score;
            // Tells the player that they achieved an achievement.
            Game.notifications.add(Localization.getLocalized("minicraft.notification.achievement_unlocked") + " " + Localization.getLocalized(id));
        }
        else
            achievementScore -= a.score;

        // Save the new list of achievements stored in memory.
        if (save) new Save();

        return true;
    }

    /**
     * Gets an array of all the unlocked achievements.
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
            l.add(new SelectEntry(id, null, true)
            {
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
     * @param unlockedAchievements An array of all the achievements we want to load, ids.
     */
    public static void unlockAchievements(JSONArray unlockedAchievements) {
        for (Object id : unlockedAchievements.toList()) {
            if (!setAchievement(id.toString(), true, false)) {
                Logger.warn("Could not load unlocked achievement with name {}.", id.toString());
            }
        }
    }
}
