package minicraft.screen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONObject;

import minicraft.saveload.Load;
import minicraft.util.Quest;

public class QuestsDisplay extends Display {
    private static HashMap<String, Quest> quests = new HashMap<>();
    private static ArrayList<Quest> unlockedQuests = new ArrayList<>();
    private static ArrayList<Quest> doneQuests = new ArrayList<>();
    private static HashMap<String, Object> questStatus = new HashMap<>();
    private static ArrayList<String> initiallyUnlocked = new ArrayList<>();

    static {
        try {
            JSONArray json = new JSONArray(String.join("", Load.loadFile("/resources/quests.json")));
            for (int i = 0; i < json.length(); i++) {
                JSONObject obj = json.getJSONObject(i);
                String id = obj.getString("id");
                // Is unlocked initially
                boolean unlocked = obj.optBoolean("unlocked", false);
                JSONArray unlocksJson = obj.getJSONArray("unlocks");
                String[] unlocks = new String[unlocksJson.length()];
                for (int j = 0; j < unlocksJson.length(); j++) unlocks[j] = unlocksJson.getString(j);
                if (unlocked) initiallyUnlocked.add(id);
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

    public static Quest getQuest(String name) {
        return quests.get(name);
    }

    public static ArrayList<Quest> getQuests() {
        return new ArrayList<>(quests.values());
    }
    public static ArrayList<Quest> getUnlockedQuests() {
        return unlockedQuests;
    }
    public static ArrayList<Quest> getDoneQuests() {
        return doneQuests;
    }

    public static boolean isQuestDone(String name) {
        return doneQuests.contains(getQuest(name));
    }

    public static Object getQuestData(String name) {
        return questStatus.get(name);
    }

    public static void unlockQuest(String name) {
        Quest quest = quests.get(name);
        quest.unlock();
        unlockedQuests.add(quest);
    }

    private static void reinitializeQuests() {
        unlockedQuests.clear();
        doneQuests.clear();
        questStatus.clear();
        for (Quest quest : quests.values()) {
            if (initiallyUnlocked.contains(quest.id)) {
                quest.unlock();
                unlockedQuests.add(quest);
            } else {
                quest.lock();
            }
        }
    }

    public static void loadGameQuests(ArrayList<String> unlocked, ArrayList<String> done) { loadGameQuests(unlocked, done, new HashMap<>()); }
    public static void loadGameQuests(ArrayList<String> unlocked, ArrayList<String> done, Map<String, Object> data) {
        reinitializeQuests();
        for (String n : unlocked) unlockedQuests.add(getQuest(n));
        for (String n : done) doneQuests.add(getQuest(n));
        questStatus.putAll(data);
    }
    public static void loadGameQuestsObj(ArrayList<Quest> unlocked, ArrayList<Quest> done) { loadGameQuestsObj(unlocked, done, new HashMap<>()); }
    public static void loadGameQuestsObj(ArrayList<Quest> unlocked, ArrayList<Quest> done, Map<String, Object> data) {
        reinitializeQuests();
        unlockedQuests.addAll(unlocked);
        doneQuests.addAll(done);
        questStatus.putAll(data);
    }
}
