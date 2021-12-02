package minicraft.screen;

import java.io.*;
import java.util.ArrayList;
import java.util.stream.Collectors;

import minicraft.core.Game;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.core.io.Sound;
import minicraft.gfx.*;
import minicraft.saveload.Save;
import minicraft.screen.entry.BlankEntry;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.SelectEntry;
import minicraft.screen.entry.StringEntry;
import minicraft.util.Achievement;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.tinylog.Logger;

public class AchievementsDisplay extends Display {

    private static final ArrayList<Achievement> achievements = new ArrayList<>();

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

            achievements.add(a);

            SelectEntry entry = new SelectEntry(a.name, null, false);

            stringEntries.add(entry);
        }
    }

    public AchievementsDisplay() {
        super(true, true,
                new Menu.Builder(false, 2,RelPos.CENTER, stringEntries).setSize(48, 48).createMenu(),
                new Menu.Builder(false, 2, RelPos.BOTTOM, new StringEntry("")).setSize(200, 32).setPositioning(new Point(Screen.w / 2, Screen.h / 2 + 32), RelPos.BOTTOM).createMenu());
    }

    @Override
    public void init(@Nullable Display parent) {
        super.init(parent);

        selectedAchievement = achievements.get(menus[0].getSelection());
    }

    @Override
    public void tick(InputHandler input) {
        super.tick(input);

        selectedAchievement = achievements.get(menus[0].getSelection());
    }

    @Override
    protected void onSelectionChange(int oldSel, int newSel) {
        super.onSelectionChange(oldSel, newSel);
    }

    /**
     * Use this to lock or unlock an achievement.
     * @param id Achievement ID
     */
    public static void setAchievement(int id, boolean unlocked) {
        Achievement a = achievements.get(id);
        a.setUnlocked(unlocked);

        if (unlocked)
            achievementScore += a.score;
        else
            achievementScore -= a.score;

        new Save();
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
}