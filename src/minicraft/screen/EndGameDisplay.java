package minicraft.screen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import minicraft.Game;
import minicraft.InputHandler;
import minicraft.Settings;
import minicraft.entity.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Screen;
import minicraft.item.Items;
import minicraft.saveload.Save;
import minicraft.screen.entry.ListEntry;
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
	//private HashMap<String, Integer> scores;
	//private int ml;
	private int finalscore;
	//private String location = Game.gameDir;
	//private ArrayList<Integer> unlocks;
	
	public EndGameDisplay(Player player) {
		super(false);
		
		displayTimer = Game.normSpeed; // wait 3 seconds before rendering the menu.
		inputDelay = Game.normSpeed/2; // wait a half-second after rendering before allowing user input.
		
		
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
		
		entries.add(entryFactory("Exit to Menu", new TitleMenu()));
		
		menus = new Menu[] {
			new Menu.Builder(true, 0, RelPos.LEFT, entries)
				//.setPositioning(Screen.center, RelPos.CENTER)
				.createMenu()
		};
		//title = "Game Over!" + (Game.isMode("score") ? " (" + ModeMenu.getSelectedTime() + ")" : "");
		
		/*
		scores = new HashMap<>();
		scores.put("Cloth", player.inventory.count(Items.get("cloth")) * (random.nextInt(2) + 1) * 10);
		scores.put("Slime", player.inventory.count(Items.get("slime")) * (random.nextInt(2) + 1) * 10);
		scores.put("Bone", player.inventory.count(Items.get("bone")) * (random.nextInt(2) + 1) * 10);
		scores.put("Gunpowder", player.inventory.count(Items.get("Gunpowder")) * (random.nextInt(2) + 1) * 10);
		scores.put("A.Book", player.inventory.count(Items.get("Antidious")) * (random.nextInt(2) + 1) * (random.nextInt(2) + 1) * 15);
		scores.put("Arrow", player.inventory.count(Items.get("arrow")) * (random.nextInt(2) + 1) * 10);
		
		ml = 0; // max length
		for(String name: scores.keySet()) {
			ml = Math.max(name.length(), ml);
		}*/
		
		/*finalscore = player.score;
		for(Integer score: scores.values()) {
			finalscore += score;
		}*/
	}
	
	private StringEntry addBonus(String item) {
		int count = Game.player.inventory.count(Items.get(item));
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
		/*else if (input.getKey("exit").clicked) {
			Game.setMenu(new TitleMenu());
		}*/
	}
	
	@Override
	public void render(Screen screen) {
		if(displayTimer <= 0)
			super.render(screen);
	}
	
	private StringEntry[] getAndWriteUnlocks() {
		int scoreTime = (int)Settings.get("scoretime");
		ArrayList<Integer> unlocks = new ArrayList<>();
		
		if(scoreTime == 20 && !Settings.getEntry("scoretime").hasValue(10) && finalscore > 1000) {
			unlocks.add(10);
			// TODO implement hidden options in ArrayEntries, or allow options to be added
			// Settings.getEntry("scoretime").addValue(10)
		}
		
		if(scoreTime == 60 && !Settings.getEntry("scoretime").hasValue(120) && finalscore > 100000) {
			unlocks.add(120);
			// Settings.getEntry("scoretime").addValue(120)
		}
		
		StringEntry[] entries = new StringEntry[unlocks.size()];
		for(int i = 0; i < entries.length; i++)
			entries[i] = new StringEntry("Unlocked! " + unlocks.get(i) + " Score Time");
		
		new Save(); // TODO make this write unlocks
		
		return entries;
	}
	
	
	//private static boolean rendered = false; /// this is a little experiment I'm doing, to see if it will work. The idea is that once it's already drawn everything, nothing's moving, so it should stay drawn. That way I won't have to redraw it every render cycle, which will hopefully save on cpu power... or something...
	
	/*public void render(Screen screen) {
		if(displayTimer > 0 || rendered) return;
		
		//new Frame(title, new Rectangle(1, 3, Screen.w-2, Screen.h-4)).render(screen);
		//super.render(screen); // draws the frame and title
		//Font.drawCentered("Game Over!", screen, 4*8, Color.WHITE);
		
		if(unlocks.size() > 0) {
			FontStyle style = new FontStyle(Color.GREEN).xCenterBounds(Screen.w/2, Screen.w-8);
			style.setYPos(10 * 8).draw("Unlocked!", screen);
			for(int i = 0; i < unlocks.size(); ++i) {
				String unlock = unlocks.get(i).replace("M", "MINUTEMODE").replace("H", "HOURMODE");
				style.setYPos((12+i)*8).draw(unlock, screen);
			}
		}
		
		Font.draw("Player Score: " + Game.player.score, screen, 16, 6*8, Color.WHITE);
		Font.draw("<Bonuses>", screen, 16, 8*8, Color.get(-1, 41));
		int i = 0;
		for(String bonus: scores.keySet().toArray(new String[0])) {
			StringBuilder label = new StringBuilder(bonus + "s: ");
			while(label.length() < ml+3) label.append(" ");
			Font.draw(label+"+"+scores.get(bonus), screen, 16, (10+(i++))*8, Color.YELLOW);
		}
		
		Font.draw("Final Score: " + finalscore, screen, 16, 17*8, Color.WHITE);
		if(finalscore == 0) {
			Font.draw("Fail!", screen, 17*8, 17*8, Color.RED);
		}

		Font.draw("Press "+Game.input.getMapping("exit")+" to exit to menu...", screen, 16, 19*8, Color.GRAY);
		rendered = true;
	}*/
}
