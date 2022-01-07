package minicraft.screen;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.json.JSONObject;
import org.tinylog.Logger;

public class AchievementsDisplay extends Display {

    private static final HashMap<String, Achievement> achievements = new HashMap<>();

    private static Achievement selectedAchievement;
    private static int achievementScore;

    private static final ArrayList<ListEntry> stringEntries = new ArrayList<>();

    static {

        // Get achievements from a json filed stored in resources. Relative to project root.
        String achievementsJson = "";
        try (InputStream stream = Game.class.getResourceAsStream("/resources/achievements.json")) {
            assert stream != null;
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            achievementsJson = reader.lines().collect(Collectors.joining("\n"));
        } catch (IOException ex) {
            Logger.error("Could not read achievements from json file.");

            ex.printStackTrace();
        }

        // Read the json and put it in the achievements list.
        JSONArray json = new JSONArray(achievementsJson);
        for (Object object : json) {
            JSONObject obj = (JSONObject) object;

            Achievement a = new Achievement(
                    Localization.getLocalized(obj.getString("id")),
                    Localization.getLocalized(obj.getString("desc")),
                    obj.getInt("score")
            );

            achievements.put(obj.getString("id"), a);

            SelectEntry entry = new SelectEntry(obj.getString("id"), null, true);

            stringEntries.add(entry);
        }
    }

    public AchievementsDisplay() {
        super(true, true,
                new Menu.Builder(false, 2, RelPos.CENTER, stringEntries).setSize(48, 48).createMenu(),
                new Menu.Builder(false, 2, RelPos.BOTTOM, new StringEntry("")).setSize(200, 32).setPositioning(new Point(Screen.w / 2, Screen.h / 2 + 32), RelPos.BOTTOM).createMenu());
    }

    @Override
    public void init(@Nullable Display parent) {
        super.init(parent);

        selectedAchievement = achievements.get(((SelectEntry) stringEntries.get(menus[0].getSelection())).getText());
    }

    @Override
    public void tick(InputHandler input) {
        super.tick(input);

        selectedAchievement = achievements.get(((SelectEntry) stringEntries.get(menus[0].getSelection())).getText());
    }

    @Override
    protected void onSelectionChange(int oldSel, int newSel) {
        super.onSelectionChange(oldSel, newSel);
    }

    /**
     * Use this to lock or unlock an achievement.
     * @param id Achievement ID.
     * @param unlocked Whether this achievement should be locked or unlocked.
     * @return True if setting the achievement was successful.
     */
    public static boolean setAchievement(String id, boolean unlocked) {
        return setAchievement(id, unlocked, true);
    }

    private static boolean setAchievement(String id, boolean unlocked, boolean save) {
        Achievement a = achievements.get(id);

        // Return if we didn't find any achievements.
        if (a == null) return false;

        if (a.getUnlocked() && unlocked) return false; // Return if it is already unlocked.
        if (!a.getUnlocked() && !unlocked) return false;  // Return if it is already locked.

        // Make the achievement unlocked in memory.
        a.setUnlocked(unlocked);

        // Add or subtract from score
        if (unlocked)
            achievementScore += a.score;
        else
            achievementScore -= a.score;

        // Save the new list of achievements stored in memory.
        if (save) new Save();

        return true;
    }

    @Override
    public void render(Screen screen) {
        super.render(screen);

        // Title.
        Font.drawCentered(Localization.getLocalized("Achievements"), screen, 8, Color.WHITE);

        // Achievement score.
        Font.drawCentered(Localization.getLocalized("Achievement Score:") + " " + achievementScore, screen, 32, Color.GRAY);

        // Render Achievement Info.
        if (selectedAchievement.getUnlocked()){
            Font.drawCentered(Localization.getLocalized("Achieved!"), screen, 48, Color.GREEN);
        } else {
            Font.drawCentered(Localization.getLocalized("Not Achieved"), screen, 48, Color.RED);
        }

        // Achievement description.
        menus[1].setEntries(StringEntry.useLines(Font.getLines(selectedAchievement.description, menus[1].getBounds().getSize().width, menus[1].getBounds().getSize().height, 2)));

        // Help text.
        Font.drawCentered("Use " + Game.input.getMapping("cursor-down") + " and " + Game.input.getMapping("cursor-up") + " to move.", screen, Screen.h - 8, Color.DARK_GRAY);
    }

    @Override
    public void onExit() {
        // Play confirm sound.
        Sound.confirm.play();
        new Save();
    }

    public static String[] getUnlockedAchievements() {
        ArrayList<String> strings = new ArrayList<>();

        for (String id : achievements.keySet()) {
            if (achievements.get(id).getUnlocked()) {
                strings.add(id);
            }
        }

        return strings.toArray(new String[0]);
    }

    public static void setUnlockedAchievements(JSONArray unlockedAchievements) {
        for (Object id : unlockedAchievements.toList()) {
            if (!setAchievement(id.toString(), true, false)) {
                Logger.error("Could not load unlocked achievement with name {}.", id.toString());
            }
        }
    }
}