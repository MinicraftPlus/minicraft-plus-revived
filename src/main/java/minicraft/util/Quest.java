package minicraft.util;

import java.util.ArrayList;
import java.util.Arrays;

public class Quest {
    public final String id, description;
    private final ArrayList<String> unlocks = new ArrayList<>();
    private boolean unlocked = false;

    public Quest(String id, String des, String... unlocks) {
        this.id = id;
        description = des;
        for (String u : unlocks) this.unlocks.add(u);
    }
    public Quest(String name, String des, boolean unlocked, String... unlocks) {
        this.id = name;
        description = des;
        this.unlocked = unlocked;
        this.unlocks.addAll(Arrays.asList(unlocks));
    }

    public void unlock() { unlocked = true; }

    public boolean getUnlocked() { return unlocked; }

    public void lock() { unlocked = false; }

	public ArrayList<String> getUnlocks() { return unlocks; }
}
