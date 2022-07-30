package minicraft.util;

public class Achievement {
	public final String name, description;
	public final int score;
	private boolean unlocked = false;

	public Achievement(String name, String description, int score) {
		this.name = name;
		this.description = description;
		this.score = score;
	}

	public void setUnlocked(boolean value) {
		unlocked = value;
	}

	public boolean getUnlocked() {
		return unlocked;
	}

	public String getName() {
		return name;
	}
}
