package minicraft.screen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import minicraft.core.Game;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Settings;
import minicraft.core.Updater;
import minicraft.entity.mob.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Screen;
import minicraft.item.Items;
import minicraft.saveload.Save;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.SelectEntry;
import minicraft.screen.entry.StringEntry;

public class EndGameDisplay extends Display {
	private static final Random random = new Random();
	
	private static final String[] scoredItems = {
		"Cloth", "Slime", "Bone", "Arrow", "Gunpowder", "Antidious"
	};
	private static final int maxLen;
	static {
		int maxLength = 0;
		for(String s: scoredItems)
			maxLength = Math.max(maxLength, s.length());
		maxLen = maxLength;
	}
	
	private int inputDelay; // variable to delay the input of the player, so they won't skip the won menu by accident.
	private int displayTimer;
	private int finalscore;
	
	public EndGameDisplay(Player player) {
		super(false, false);
		
		displayTimer = Updater.normSpeed; // wait 3 seconds before rendering the menu.
		inputDelay = Updater.normSpeed/2; // wait a half-second after rendering before allowing user input.
		
		
		ArrayList<ListEntry> entries = new ArrayList<>();
		
		// calculate the score
		entries.add(new StringEntry("Player Score: " + Game.player.score, Color.WHITE));
		entries.add(new StringEntry("<Bonuses>", Color.YELLOW));
		
		finalscore = Game.player.score;
		for(String item: scoredItems)
			addBonus(item);
		
		entries.add(new StringEntry("Final Score: " + finalscore));
		
		// add any unlocks
		entries.addAll(Arrays.asList(getAndWriteUnlocks()));
		
		entries.add(new SelectEntry("Exit to Menu", () -> Game.setMenu(new TitleDisplay())));
		
		//title = "Game Over!" + (Game.isMode("score") ? " (" + ModeMenu.getSelectedTime() + ")" : "");
		menus = new Menu[] {
			new Menu.Builder(true, 0, RelPos.LEFT, entries)
				//.setPositioning(Screen.center, RelPos.CENTER)
				//.setTitle()
				.createMenu()
		};
	}
	
	private StringEntry addBonus(String item) {
		int count = Game.player.getInventory().count(Items.get(item));
		int score = count * (random.nextInt(2) + 1) * 10;
		finalscore += score;
		StringBuilder buffer = new StringBuilder();
		while(item.length()+buffer.length() < maxLen) buffer.append(" ");
		return new StringEntry(count+" "+item+"s: "+buffer+"+"+score, Color.YELLOW);
	}
	
	@Override
	public void tick(InputHandler input) {
		if (displayTimer > 0) displayTimer--;
		else if (inputDelay > 0) inputDelay--;
		else super.tick(input);
	}
	
	@Override
	public void render(Screen screen) {
		if(displayTimer <= 0)
			super.render(screen);
	}
	
	private StringEntry[] getAndWriteUnlocks() {
		int scoreTime = (int)Settings.get("scoretime");
		ArrayList<Integer> unlocks = new ArrayList<>();
		
		if(scoreTime == 20 && !Settings.getEntry("scoretime").valueIs(10) && finalscore > 1000) {
			unlocks.add(10);
			// TODO implement hidden options in ArrayEntries, or allow options to be added
			// Settings.getEntry("scoretime").addValue(10)
		}
		
		if(scoreTime == 60 && !Settings.getEntry("scoretime").valueIs(120) && finalscore > 100000) {
			unlocks.add(120);
			// Settings.getEntry("scoretime").addValue(120)
		}
		
		StringEntry[] entries = new StringEntry[unlocks.size()];
		for(int i = 0; i < entries.length; i++)
			entries[i] = new StringEntry("Unlocked! " + unlocks.get(i) + " Score Time");
		
		new Save(); // TODO make this write unlocks
		
		return entries;
	}
}
