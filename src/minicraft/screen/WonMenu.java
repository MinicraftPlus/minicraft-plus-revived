package minicraft.screen;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import minicraft.Game;
import minicraft.entity.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.item.Items;

public class WonMenu extends Menu {
	private Random random = new Random();
	
	private int inputDelay; // variable to delay the input of the player, so they won't skip the won menu by accident.
	private int displayTimer;
	private HashMap<String, Integer> scores;
	private int ml;
	private int finalscore;
	private String location = Game.gameDir;
	//private boolean doneunlocked = false;
	private ArrayList<String> unlocks;
	
	public WonMenu(Player player) {
		displayTimer = Game.normSpeed; // wait 3 seconds before rendering the menu.
		inputDelay = Game.normSpeed/2; // wait a half-second before allow user input.
		
		scores = new HashMap<String, Integer>();
		scores.put("Cloth", player.inventory.count(Items.get("cloth")) * (random.nextInt(2) + 1) * 10);
		scores.put("Slime", player.inventory.count(Items.get("slime")) * (random.nextInt(2) + 1) * 10);
		scores.put("Bone", player.inventory.count(Items.get("bone")) * (random.nextInt(2) + 1) * 10);
		scores.put("Gunpowder", player.inventory.count(Items.get("Gunpowder")) * (random.nextInt(2) + 1) * 10);
		scores.put("A.Book", player.inventory.count(Items.get("Antidious")) * (random.nextInt(2) + 1) * (random.nextInt(2) + 1) * 15);
		scores.put("Arrow", player.inventory.count(Items.get("arrow")) * (random.nextInt(2) + 1) * 10);
		
		ml = 0; // max length
		for(String name: scores.keySet().toArray(new String[0])) {
			ml = Math.max(name.length(), ml);
		}
		
		finalscore = Player.score;
		for(Integer score: scores.values().toArray(new Integer[0])) {
			finalscore += score;
		}
		
		unlocks = new ArrayList<String>();
		writeUnlocks();
	}

	public void tick() {
		if (displayTimer > 0) displayTimer--;
		else if (inputDelay > 0) inputDelay--;
		else if (input.getKey("exit").clicked) {
			game.setMenu(new TitleMenu());
		}
	}

	public void writeUnlocks() {
		//boolean hastime = false;
		String scoreTime = ModeMenu.getSelectedTime();
		List<String> unlockedtimes = ModeMenu.unlockedtimes;
		
		if(scoreTime.equals("20M") && !unlockedtimes.contains("10M") && finalscore > 1000)
			unlocks.add("10M");
		
		if(scoreTime.equals("1H") && !unlockedtimes.contains("2H") && finalscore > 100000)
			unlocks.add("2H");
		
		/*
		 { // if playing a 20 minute game...
			hastime = false;
			
			for(int seconds = 0; seconds < ModeMenu.unlockedtimes.size(); seconds++) {
				/// searches through the unlocked times for the 10 minute option
				if(ModeMenu.unlockedtimes.get(seconds).contains("10M")) {
					break;
					hastime = true; // if found, set to true.
				}
			}
			
			if(!ModeMenu.unlockedtimes.contains("10M") && finalscore > 1000) { // if 10 min option wasn't found, and this game's score is high enough to unlock that option...
				unlocks.add("10M"); // add the 10 min option to the list of times to be recorded as "unlocked".
			}
		}
		
		if(scoreTime.contains("1H")) {
			hastime = false;
			
			for(int seconds = 0; seconds < ModeMenu.unlockedtimes.size(); seconds++) {
				if(ModeMenu.unlockedtimes.get(seconds).contains("2H")) {
					hastime = true;
					break;
				}
				
				hastime = false;
			}
			
			if(!hastime && finalscore > 100000) {
				unlocks.add("2H");
			}
		}*/
		
		if(unlocks.size() == 0)
			return;
		
		for(int i = 0; i < unlocks.size(); i++) {
			BufferedWriter unlockWriter = null;
			
			try {
				unlockWriter = new BufferedWriter(new FileWriter(location + "/unlocks.miniplussave", true));
				if(unlocks.get(i).contains("M")) {
					unlockWriter.write("," + unlocks.get(i).substring(0, unlocks.get(i).indexOf("M")) + "MINUTEMODE"); // this effectively writes 10MINUTEMODE.
				} else if(unlocks.get(i).contains("H")) {
					unlockWriter.write("," + unlocks.get(i).substring(0, unlocks.get(i).indexOf("H")) + "HOURMODE"); // this effectively writes 2HOURMODE.
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			} finally {
				try {
					if(unlockWriter != null) {
						unlockWriter.flush();
						unlockWriter.close();
					}
				} catch (IOException ex) {
					ex.printStackTrace();
				}
				
				TitleMenu.loadedunlocks = false;
			}
		}
	}
	
	public void render(Screen screen) {
		if(displayTimer > 0) return;
		/*
		if(Player.score <= 9999999) {
			w = 21;
		} else {
			w = 14 + 1 + (int)Math.floor(Math.log10((double)Player.score));
		}
		
		w = Math.max(w, ml + 2);
		w = Math.max(w, ("Final Score:" + finalscore).length() + 2);
		w = Math.max(w, ("Press "+input.getMapping("exit")+" to exit to menu...").length() + 2);
		*/
		renderFrame(screen, "", 1, 3, screen.w/8-2, screen.h/8-5);
		Font.drawCentered("Game Over! (" + ModeMenu.getSelectedTime() + ")", screen, 4*8, Color.get(-1, 555));
		
		if(unlocks.size() > 0) {
			//renderFrame(screen, "Unlocked!", w + 2, 3, w + 13, 7 + unlocks.size());
			//Font.drawCentered("Unlocked!", screen, w * 8/2 + 32 - 4, 32, Color.get(-1, 50));
			Font.drawCentered("Unlocked!", screen, screen.w/2, screen.w-8, 6*8, Color.get(-1, 50));
			
			for(int i = 0; i < unlocks.size(); ++i) {
				//Font.draw(unlocks.get(i), screen, w * 8 + 48 + 2, 48 + i * 12, Color.get(-1, 555));
				Font.drawCentered(unlocks.get(i), screen, screen.w/2, screen.w-8, (8+i)*8, Color.get(-1, 50));
			}
		}
		
		Font.draw("Player Score: " + Player.score, screen, 16, 6*8, Color.get(-1, 555));
		Font.draw("<Bonuses>", screen, 16, 8*8, Color.get(-1, 040));
		int i = 0;
		for(String bonus: scores.keySet().toArray(new String[0])) {
			String label = bonus+"s: ";
			while(label.length() < ml+3) label += " ";
			Font.draw(label+"+"+scores.get(bonus), screen, 16, (10+(i++))*8, Color.get(-1, 550));
		}
		
		Font.draw("Final Score: " + finalscore, screen, 16, 17*8, Color.get(-1, 555));
		if(finalscore == 0) {
			Font.draw("Fail!", screen, 17*8, 17*8, Color.get(-1, 500));
		}

		Font.draw("Press "+input.getMapping("exit")+" to exit to menu...", screen, 16, 19*8, Color.get(-1, 333));
	}
}
