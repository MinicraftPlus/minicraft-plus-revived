package minicraft.screen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import minicraft.core.Game;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Sound;
import minicraft.entity.furniture.Crafter;
import minicraft.entity.furniture.Furniture;
import minicraft.entity.mob.Player;
import minicraft.gfx.*;
import minicraft.item.*;
import minicraft.saveload.Load;
import minicraft.saveload.Save;

/*
 *
 */
public class AchievementsDisplay extends Display {

    private static final List<String> achievementList = new ArrayList<>();
    private static final List<String> achievementInfoList = new ArrayList<>();
    public static List<Object> achievedList = new ArrayList<>();
    public static int selectedAchievement = 0;
    private static int achievementScore = 0;
    private static String achievementInfo = "";

    private static List<Object> scored = new ArrayList<>();
    private int step;

    private int achievedColor = 0;

    static {
        reloadAchievements();
    }

    public static void reloadAchievements(){
        /*
         * Checks if Unlocks.json contains achieved data. If so just add Achievement Names and Info, if not add achieved data.
         * Beware all new Achievements must be added after current ones because the list is read in hard-coded order. Starting from 0 (Ex: The first entry would be ID:0)
         *
         */

        achievementList.removeAll(achievementList);
        achievementInfoList.removeAll(achievementInfoList);

        achievementList.add("Woodcutter");
        achievementInfoList.add("Get wood");
        achievementList.add("Benchmarking");
        achievementInfoList.add("Make a workbench");
        achievementList.add("Upgrade!");
        achievementInfoList.add("Get any tool stronger than wooden");
        achievementList.add("Bow down to me!");
        achievementInfoList.add("Kill a Mob with a bow");
        achievementList.add("Go Fish!");
        achievementInfoList.add("Fish up a Fish!");
        achievementList.add("Adooring Protection");
        achievementInfoList.add("Place all door types");
        achievementList.add("Walk the Planks!");
        achievementInfoList.add("Place all plank types");
        achievementList.add("Have a colourful day!");
        achievementInfoList.add("Craft all color types of Clothes");
        achievementList.add("Demolition Demo");
        achievementInfoList.add("Use TNT");
        achievementList.add("An explosive ending");
        achievementInfoList.add("\"Accidentally\" blow yourself up");
        achievementList.add("Afraid of the Dark?");
        achievementInfoList.add("Survive 5 minutes in total darkness");
        achievementList.add("Hot Affair");
        achievementInfoList.add("Use a lava potion to swim in lava");
        achievementList.add("Oooh Shiny!");
        achievementInfoList.add("Find Gem Ore and mine it");
        achievementList.add("Darkness behind light");
        achievementInfoList.add("Reach the lowest caves");
        achievementList.add("Of Knights and Men");
        achievementInfoList.add("Reach the Obsidian Dungeon");
        achievementList.add("Defeat... the air?");
        achievementInfoList.add("Defeat the first Air Wizard!");
        achievementList.add("An Airy Feat!");
        achievementInfoList.add("Defeat the second Air Wizard");
        achievementList.add("On a diet");
        achievementInfoList.add("Eat every food item");
        achievementList.add("Fashion Show");
        achievementInfoList.add("Change your skin!");

        /*
         * Checks if Unlocks.json achievedList contains the right amount of Achievements and if not, replaced with the correct amount
         *
         */
        if (achievedList.size() != achievementList.size()) {
            achievedList.removeAll(achievedList);
            scored.removeAll(scored);
            for (int i = -1; achievementList.size() > i; i++) {
                scored.add(false);
                achievedList.add(false);
            }
        }
        new Save();
    }

    // Updates Achievement Info upon call
    public void updateAchievementInfo(){
        achievementInfo = achievementInfoList.get(selectedAchievement);
    }

    @Override
    public void onExit() {
        // Play confirm sound.
        Sound.confirm.play();
        new Save();
    }

    @Override
    public void tick(InputHandler input) {
        if (input.getKey("exit").clicked) {
            achievementInfo = "";
            Game.exitMenu();
            return;
        }
        if (input.getKey("cursor-down").clicked && selectedAchievement < achievementList.size() - 1) {
            selectedAchievement++;
            achievementInfo = "";
            Sound.select.play();
        }
        if (input.getKey("cursor-up").clicked && selectedAchievement > 0) {
            selectedAchievement--;
            achievementInfo = "";
            Sound.select.play();
        }
        // Updates Achievement Info on Select
        if (input.getKey("SELECT").clicked){
            updateAchievementInfo();
            Sound.confirm.play();
        }
    }

    private void achievedScore(int ID, int scoreAmount, int stepNum){
        if (achievedList.get(ID).equals(true) && scored.get(ID).equals(false) && step == stepNum) {
            step++;
            achievementScore = achievementScore + scoreAmount;
            scored.set(ID,true);
        }
        else {
            step++;
        }
    }

    /**
     * Use this to achieve an Achievement.
     * @param ID Achievement ID
     */
    public static void achieve(int ID){
        achievedList.set(ID, true);
        new Save();
    }

    /**
     * Use this to unachieve an Achievement.
     * @param ID Achievement ID
     */
    public static void unachieve(int ID){
        achievedList.set(ID, false);
    }

    @Override
    public void render(Screen screen) {
        screen.clear(0);

        // Get the selections
        String selectedUpUp = selectedAchievement + 2 > achievementList.size() - 2 ? "" : achievementList.get(selectedAchievement + 2);
        String selectedUp = selectedAchievement + 1 > achievementList.size() - 1 ? "" : achievementList.get(selectedAchievement + 1);
        String selectedDown = selectedAchievement - 1 < 0 ? "" : achievementList.get(selectedAchievement - 1);
        String selectedDownDown = selectedAchievement - 2 < 0 ? "" : achievementList.get(selectedAchievement - 2);

        // Title.
        Font.drawCentered("Achievements", screen, Screen.h - 180, Color.YELLOW);

        // Green if Achievement is achieved & Red if not
        if (achievedList.get(selectedAchievement).equals(true)){
            achievedColor = Color.GREEN;
        }
        else {
            achievedColor = Color.RED;
        }



        /*if (player.activeItem instanceof FurnitureItem) {
            if (player.activeItem.equals(Crafter.Type.Workbench)) {
                AchievementsDisplay.achieve(1);
                System.out.println("Crafted Workbench");
            }
        }*/
        // Checks all values of achievedList and adds score if achieved
        achievedScore(0,5,0);
        achievedScore(1,10,1);
        achievedScore(2,10,2);
        achievedScore(3,10,3);
        achievedScore(4,5,4);
        achievedScore(5,5,5);
        achievedScore(6,5,6);
        achievedScore(7,5,7);
        achievedScore(8,10,8);
        achievedScore(9,5,9);
        achievedScore(10,15,10);
        achievedScore(11,15,11);
        achievedScore(12,20,12);
        achievedScore(13,25,13);
        achievedScore(14,25,14);
        achievedScore(15,25,15);
        achievedScore(16,25,16);
        achievedScore(17,25,17);
        achievedScore(18,10,18);
        achievedScore(19,5,19);


        // Render the menu.
        Font.drawCentered(AchievementsDisplay.shortNameIfLong(selectedUpUp), screen, Screen.h - 60, Color.GRAY); // First unselected space
        Font.drawCentered(AchievementsDisplay.shortNameIfLong(selectedUp), screen, Screen.h - 70, Color.GRAY); // Second unselected space
        Font.drawCentered(AchievementsDisplay.shortNameIfLong(achievementList.get(selectedAchievement)), screen, Screen.h - 80, achievedColor); // Selection
        Font.drawCentered(AchievementsDisplay.shortNameIfLong(selectedDown), screen, Screen.h - 90, Color.GRAY); // Third space
        Font.drawCentered(AchievementsDisplay.shortNameIfLong(selectedDownDown), screen, Screen.h - 100, Color.GRAY); // Fourth space

        // Help text & Achievement Score.
        Font.drawCentered("Use "+ Game.input.getMapping("cursor-down") + " and " + Game.input.getMapping("cursor-up") + " to move.", screen, Screen.h - 25, Color.WHITE);
        Font.drawCentered(Game.input.getMapping("SELECT") + " to select.", screen, Screen.h - 17, Color.WHITE);
        Font.drawCentered("Achievement Score:" + achievementScore, screen, Screen.h - 9, Color.WHITE);

        // Render Achievement Info.
        if (achievedList.get(selectedAchievement).equals(true)){
            Font.drawCentered("Achieved!",screen, Screen.h - 140, Color.GREEN);
        }
        else{
            Font.drawCentered("Not Achieved",screen, Screen.h - 140, Color.RED);
        }
        Font.drawCentered(achievementInfo, screen, Screen.h - 132, Color.WHITE);
    }


    // In case the name is too big ...
    private static String shortNameIfLong(String name) {
        return name.length() > 22 ? name.substring(0, 16) + "..." : name;
    }
}