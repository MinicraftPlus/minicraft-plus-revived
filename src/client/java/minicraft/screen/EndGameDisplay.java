package minicraft.screen;

import minicraft.core.Game;
import minicraft.core.Updater;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.core.io.Settings;
import minicraft.gfx.Color;
import minicraft.gfx.Screen;
import minicraft.item.Items;
import minicraft.saveload.Save;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.SelectEntry;
import minicraft.screen.entry.StringEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class EndGameDisplay extends Display {
	private static final Random random = new Random();

	private static final String[] scoredItems = {
		"Cloth", "Slime", "Bone", "Arrow", "Gunpowder", "Antidious"
	};

	private int inputDelay; // variable to delay the input of the player, so they won't skip the won menu by accident.
	private int displayTimer;
	private int finalScore;

	static {
		int maxLength = 0;
		for (String s : scoredItems)
			maxLength = Math.max(maxLength, s.length());
	}

	public EndGameDisplay() {
		super(false, false);

		displayTimer = Updater.normSpeed; // wait 3 seconds before rendering the menu.
		inputDelay = Updater.normSpeed / 2; // wait a half-second after rendering before allowing user input.


		ArrayList<ListEntry> entries = new ArrayList<>();

		// calculate the score
		entries.add(new StringEntry(Localization.getLocalized("minicraft.displays.end_game.display.player_score", Game.player.getScore()), Color.WHITE));
		entries.add(new StringEntry("minicraft.displays.end_game.display.bonuses", Color.YELLOW));

		finalScore = Game.player.getScore();
		for (String item : scoredItems)
			addBonus(item);

		entries.add(new StringEntry(Localization.getLocalized("minicraft.displays.end_game.display.final_score", finalScore)));

		// add any unlocks
		entries.addAll(Arrays.asList(getAndWriteUnlocks()));

		entries.add(new SelectEntry("minicraft.displays.end_game.exit", () -> Game.setDisplay(new TitleDisplay())));

		menus = new Menu[]{
			new Menu.Builder(true, 0, RelPos.LEFT, entries).createMenu()
		};
	}

	private void addBonus(String item) {
		int count = Game.player.getInventory().count(Items.get(item));
		int score = count * (random.nextInt(2) + 1) * 10;
		finalScore += score;
	}

	@Override
	public void tick(InputHandler input) {
		super.tick(input);
		if (displayTimer > 0) displayTimer--;
		else if (inputDelay > 0) inputDelay--;
	}

	@Override
	public void render(Screen screen) {
		if (displayTimer <= 0)
			super.render(screen);
	}

	private StringEntry[] getAndWriteUnlocks() {
		int scoreTime = (int) Settings.get("scoretime");
		ArrayList<Integer> unlocks = new ArrayList<>();

		if (scoreTime == 20 && !Settings.getEntry("scoretime").valueIs(10) && finalScore > 1000) {
			unlocks.add(10);
			Settings.getEntry("scoretime").setValueVisibility(10, true);
		}

		if (scoreTime == 60 && !Settings.getEntry("scoretime").valueIs(120) && finalScore > 100000) {
			unlocks.add(120);
			Settings.getEntry("scoretime").setValueVisibility(120, true);
		}

		StringEntry[] entries = new StringEntry[unlocks.size()];
		for (int i = 0; i < entries.length; i++)
			entries[i] = new StringEntry(Localization.getLocalized("minicraft.displays.end_game.display.unlocked", unlocks.get(i)));

		new Save(); // Write preferences and unlocks.

		return entries;
	}
}
