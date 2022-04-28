package minicraft.screen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import minicraft.saveload.Load;
import minicraft.util.Quest;

public class QuestsDisplay extends Display {
    private static HashMap<String, Quest> quests = new HashMap<>();
    private static ArrayList<Quest> unlockedQuests = new ArrayList<>();
    private static ArrayList<Quest> undoneQuests = new ArrayList<>();
    private static ArrayList<Quest> doneQuests = new ArrayList<>();
    private static HashMap<String, Object> questStatus = new HashMap<>();

    static {
        try {
            JSONArray json = new JSONArray(String.join("", Load.loadFile("/resources/quests.json")));
            for (int i = 0; i < json.length(); i++) {
                JSONObject obj = json.getJSONObject(i);
                String id = obj.getString("id");
                boolean unlocked = obj.optBoolean("unlocked", false);
                JSONArray unlocksJson = obj.getJSONArray("unlocks");
                String[] unlocks = new String[unlocksJson.length()];
                for (int j = 0; j < unlocksJson.length(); j++) unlocks[j] = unlocksJson.getString(j);
                quests.put(id, unlocked? new Quest(id, obj.getString("desc"), unlocked, unlocks): new Quest(id, obj.getString("desc"), unlocks));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public QuestsDisplay() { this(false); }
    public QuestsDisplay(boolean atCornerDisplay) {
        if (atCornerDisplay) {
            
        } else {

        }
    }

    public static 

    public static void loadGameQuests(ArrayList<String> unlocked, ArrayList<String> done, ArrayList<String> undone) { loadGameQuests(unlocked, done, undone, new HashMap<>()); }
    public static void loadGameQuests(ArrayList<String> unlocked, ArrayList<String> done, ArrayList<String> undone, HashMap<String, Object> data) {
        
    }
    public static void loadGameQuestsObj(ArrayList<Quest> unlocked, ArrayList<Quest> done, ArrayList<Quest> undone) { loadGameQuestsObj(unlocked, done, undone, new HashMap<>()); }
    public static void loadGameQuestsObj(ArrayList<Quest> unlocked, ArrayList<Quest> done, ArrayList<Quest> undone, HashMap<String, Object> data) {
        
    }
}
