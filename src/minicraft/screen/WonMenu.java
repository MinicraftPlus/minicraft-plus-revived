package minicraft.screen;

import minicraft.Game;
import minicraft.entity.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.item.resource.Resource;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class WonMenu extends Menu {

	private int inputDelay = 20; // variable to delay the input of the player, so they won't skip the won menu the first second.
	Random random = new Random();
	HashMap<String, Integer> scores;
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
		
		ml = 0; // max length
		for(String name: scores.keySet().toArray(new String[0])) {
			ml = Math.max(name.length(), ml);
		}
		
		finalscore = Player.score;
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
		renderFrame(screen, "", w + 2, 3, w + 13, 7 + unlocks.size());
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

		renderFrame(screen, "", 1, 3, w, 20);
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
		
		Font.draw("Player Score: " + Player.score, screen, 16, 48, Color.get(-1, 555, 555, 555));
		Font.draw("<Bonuses>", screen, 16, 64, Color.get(-1, Color.rgb(0, 200, 0), Color.rgb(0, 200, 0), Color.rgb(0, 200, 0)));
		int i = 0;
		for(String bonus: scores.keySet().toArray(new String[0])) {
			String label = bonus+"s: ";
			while(label.length() < ml+3) label += " ";
			Font.draw(label+"+"+scores.get(bonus), screen, 16, 80+(i++)*8, Color.get(-1, 550, 550, 550));
		}
		
		Font.draw("Final Score: " + finalscore, screen, 16, 136, Color.get(-1, 555, 555, 555));
		if(finalscore == 0) {
			Font.draw("Fail!", screen, 136, 136, Color.get(-1, 500, 500, 500));
		}

		Font.draw("Press Enter to continue...", screen, 16, 152, Color.get(-1, 333, 333, 333));
	}
}
