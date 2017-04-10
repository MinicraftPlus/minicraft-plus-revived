package com.mojang.ld22.screen;

import com.mojang.ld22.entity.Player;
import com.mojang.ld22.Game;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Font;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.item.resource.Resource;
import com.mojang.ld22.screen.Menu;
import com.mojang.ld22.screen.ModeMenu;
import com.mojang.ld22.screen.TitleMenu;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class WonMenu extends Menu {

	private int inputDelay = 20; // variable to delay the input of the player, so they won't skip the won menu the first second.
	Random random = new Random();
	HashMap<String, Integer> scores;
	//int b1, b2, b3, b4, b5, b6;
	//String s1, s2, s3, s4, s5, s6;
	int w, ml = 0;
	int finalscore = 0;
	String location = Game.gameDir;
	boolean doneunlocked = false;


	public WonMenu(Player player) {
		scores = new HashMap<String, Integer>();
		scores.put("Cloth", player.inventory.count(Resource.cloth) * (random.nextInt(2) + 1) * 10);
		scores.put("Slime", player.inventory.count(Resource.slime) * (random.nextInt(2) + 1) * 10);
		scores.put("Bone", player.inventory.count(Resource.bone) * (random.nextInt(2) + 1) * 10);
		scores.put("Gunpowder", player.inventory.count(Resource.gunp) * (random.nextInt(2) + 1) * 10);
		scores.put("A.Book", player.inventory.count(Resource.bookant) * (random.nextInt(2) + 1) * (random.nextInt(2) + 1) * 15);
		scores.put("Arrow", player.inventory.count(Resource.arrow) * (random.nextInt(2) + 1) * 10);
		/*s1 = "Cloths:+" + b1;
		s2 = "Slimes:+" + b2;
		s3 = "Bones:+" + b3;
		s4 = "Gunpowders:+" + b4;
		s5 = "Books:+" + b5;
		s6 = "Arrows:+" + b6;
		*/
		ml = 0; // max length
		for(String name: scores.keySet().toArray(new String[0])) {
			ml = Math.max(name.length(), ml);
		}
		/*
		if(s2.length() > ml) {
			ml = s2.length();
		}

		if(s3.length() > ml) {
			ml = s3.length();
		}

		if(s4.length() > ml) {
			ml = s4.length();
		}

		if(s5.length() > ml) {
			ml = s5.length();
		}

		if(s6.length() > ml) {
			ml = s6.length();
		}
		*/
		finalscore = Player.score;// + b1 + b2 + b3 + b4 + b5 + b6;
		for(Integer score: scores.values().toArray(new Integer[0])) {
			finalscore += score;
		}
	}

	public void tick() {
		if(inputDelay > 0) inputDelay--;
		if(input.getKey("enter").clicked && inputDelay <= 0) {
			game.setMenu(new TitleMenu());
		}
	}

	public void writeUnlocks(Screen screen, List unlocks) {
		Font.renderFrame(screen, "", w + 2, 3, w + 13, 7 + unlocks.size());
		Font.draw("Unlocked!", screen, w * 8 + 32 - 4, 32, Color.get(-1, 50, 50, 50));
		
		for(int i = 0; i < unlocks.size(); ++i) {
			Font.draw((String)unlocks.get(i), screen, w * 8 + 48 + 2, 48 + i * 12, Color.get(-1, 555, 555, 555));
		}
		
		if(!doneunlocked) {
			for(int i = 0; i < unlocks.size(); ++i) {
				BufferedWriter unlockWriter = null;
				
				try {
					unlockWriter = new BufferedWriter(new FileWriter(location + "/unlocks.miniplussave", true));
					if(((String)unlocks.get(i)).contains("M")) {
						unlockWriter.write("," + ((String)unlocks.get(i)).substring(0, ((String)unlocks.get(i)).indexOf("M")) + "MINUTEMODE");
					} else if(((String)unlocks.get(i)).contains("H")) {
						unlockWriter.write("," + ((String)unlocks.get(i)).substring(0, ((String)unlocks.get(i)).indexOf("H")) + "HOURMODE");
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
					doneunlocked = true;
				}
			}
		}

	}

	public void render(Screen screen) {
		boolean hastime = false;
		
		if(Player.score <= 9999999) {
			w = 21;
		} else {
			w = 14 + 1 + (int)Math.floor(Math.log10((double)Player.score));
		}
		
		w = Math.max(w, ml + 2);
		w = Math.max(w, ("Final Score:" + finalscore).length() + 2);
		w = Math.max(w, ("Press Enter to continue...").length() + 2);

		Font.renderFrame(screen, "", 1, 3, w, 20);
		Font.draw("Game Over! (" + ModeMenu.time + ")", screen, 16, 32, Color.get(-1, 555, 555, 555));
		
		ArrayList unlocks = new ArrayList();
		
		if(ModeMenu.time.contains("20M")) {
			hastime = false;
			
			for(int seconds = 0; seconds < ModeMenu.unlockedtimes.size(); seconds++) {
				if(((String)ModeMenu.unlockedtimes.get(seconds)).contains("10M")) {
					hastime = true;
					break;
				}

				hastime = false;
			}

			if(!hastime && finalscore > 1000) {
				unlocks.add("10M");
			}
		}

		if(ModeMenu.time.contains("1H")) {
			hastime = false;

			for(int seconds = 0; seconds < ModeMenu.unlockedtimes.size(); seconds++) {
				if(((String)ModeMenu.unlockedtimes.get(seconds)).contains("2H")) {
					hastime = true;
					break;
				}

				hastime = false;
			}

			if(!hastime && finalscore > 100000) {
				unlocks.add("2Hr");
			}
		}

		if(unlocks.size() > 0) {
			writeUnlocks(screen, unlocks);
		}
		/*
		seconds = game.gameTime / 60;
		int minutes = seconds / 60;
		int hours = minutes / 60;
		minutes %= 60;
		seconds %= 60;
		String timeString = "";
		if(hours > 0) {
			timeString = hours + "h" + (minutes < 10?"0":"") + minutes + "m";
		} else {
			timeString = minutes + "m " + (seconds < 10?"0":"") + seconds + "s";
		}
		*/
		Font.draw("Player Score: " + Player.score, screen, 16, 48, Color.get(-1, 555, 555, 555));
		//writeCentered("Final Score: " + Player.score, screen, 48, Color.get(-1, 555, 555, 555));
		//writeCentered()
		//StringBuilder var8 = new StringBuilder();
		//Font.draw(, screen, 64, 48, Color.get(-1, 550, 550, 550));
		Font.draw("<Bonuses>", screen, 16, 64, Color.get(-1, Color.rgb(0, 200, 0), Color.rgb(0, 200, 0), Color.rgb(0, 200, 0)));
		int i = 0;
		for(String bonus: scores.keySet().toArray(new String[0])) {
			String label = bonus+"s: ";
			while(label.length() < ml+3) label += " ";
			Font.draw(label+"+"+scores.get(bonus), screen, 16, 80+(i++)*8, Color.get(-1, 550, 550, 550));
		}
		/*Font.draw(s1, screen, 16, 80, Color.get(-1, 550, 550, 550));
		Font.draw(s2, screen, 16, 88, Color.get(-1, 550, 550, 550));
		Font.draw(s3, screen, 16, 96, Color.get(-1, 550, 550, 550));
		Font.draw(s4, screen, 16, 104, Color.get(-1, 550, 550, 550));
		Font.draw(s6, screen, 16, 112, Color.get(-1, 550, 550, 550));
		Font.draw(s5, screen, 16, 120, Color.get(-1, 550, 550, 550));
		*/Font.draw("Final Score: " + finalscore, screen, 16, 136, Color.get(-1, 555, 555, 555));
		//Font.draw("" + , screen, 112, 136, Color.get(-1, 550, 550, 550));
		if(finalscore == 0) {
			Font.draw("Fail!", screen, 136, 136, Color.get(-1, 500, 500, 500));
		}

		Font.draw("Press Enter to continue...", screen, 16, 152, Color.get(-1, 333, 333, 333));
	}
}
