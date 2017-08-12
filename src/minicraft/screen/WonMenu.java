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
import minicraft.gfx.FontStyle;
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
		
		finalscore = player.score;
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
		String scoreTime = ModeMenu.getSelectedTime();
		List<String> unlockedtimes = ModeMenu.unlockedtimes;
		
		if(scoreTime.equals("20M") && !unlockedtimes.contains("10M") && finalscore > 1000)
			unlocks.add("10M");
		
		if(scoreTime.equals("1H") && !unlockedtimes.contains("2H") && finalscore > 100000)
			unlocks.add("2H");
		
		if(unlocks.size() == 0)
			return;
		
		for(int i = 0; i < unlocks.size(); i++) {
			BufferedWriter unlockWriter = null;
			
			try {
				unlockWriter = new BufferedWriter(new FileWriter(location + "/Unlocks.miniplussave", true));
				unlockWriter.write("," + unlocks.get(i) + "_ScoreTime");
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
			}
		}
		
		ModeMenu.unlockedtimes.addAll(unlocks);
		ModeMenu.initTimeList();
	}
	
	public void render(Screen screen) {
		if(displayTimer > 0) return;
		
		renderFrame(screen, "", 1, 3, Screen.w/8-2, Screen.h/8-4);
		Font.drawCentered("Game Over! (" + ModeMenu.getSelectedTime() + ")", screen, 4*8, Color.get(-1, 555));
		
		if(unlocks.size() > 0) {
			FontStyle style = new FontStyle(Color.get(-1, 50)).xCenterBounds(Screen.w/2, Screen.w-8);
			style.setYPos(10 * 8).draw("Unlocked!", screen);
			for(int i = 0; i < unlocks.size(); ++i) {
				String unlock = unlocks.get(i).replace("M", "MINUTEMODE").replace("H", "HOURMODE");
				style.setYPos((12+i)*8).draw(unlock, screen);
			}
		}
		
		Font.draw("Player Score: " + game.player.score, screen, 16, 6*8, Color.get(-1, 555));
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
