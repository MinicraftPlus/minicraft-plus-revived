package minicraft.util;

import java.util.ArrayList;
import java.util.Arrays;

import minicraft.item.Recipe;

public class Quest {
    public final String id, description;
    private final ArrayList<String> unlocks = new ArrayList<>();
    private boolean unlocked = false;
	private boolean tutorial = false;
	private final ArrayList<Recipe> recipes;

    public Quest(String name, String des, boolean unlocked, boolean tutorial, String... unlocks) { this(name, des, unlocked, tutorial, new ArrayList<>(), unlocks); }
	public Quest(String name, String des, boolean unlocked, boolean tutorial, ArrayList<Recipe> recipes, String... unlocks) {
		this.id = name;
		description = des;
		this.unlocked = unlocked;
		this.tutorial = tutorial;
		this.unlocks.addAll(Arrays.asList(unlocks));
		this.recipes = recipes;
	}

    public void unlock() { unlocked = true; }
    public void lock() { unlocked = false; }

    public boolean isUnlocked() { return unlocked; }
	public boolean isTutorial() { return tutorial; }

	public ArrayList<String> getUnlocks() { return unlocks; }
	public ArrayList<Recipe> getRecipes() { return recipes; }
}
