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
import java.util.List;
import java.util.Random;

public class WonMenu extends Menu {

	private int inputDelay = 0; // variable to delay the input of the player, so they won't skip the won menu the first second.
	Random random = new Random();
	int b1;
	int b2;
	int b3;
	int b4;
	int b5;
	int b6;
	String s1;
	String s2;
	String s3;
	String s4;
	String s5;
	String s6;
	int w;
	int ml = 0;
	int finalscore = 0;
	String location = Game.gameDir;
	boolean doneunlocked = false;


	public WonMenu(Player player) {
		this.b1 = player.inventory.scored(Resource.cloth) * (this.random.nextInt(2) + 1) * 10;
		this.b2 = player.inventory.scored(Resource.slime) * (this.random.nextInt(2) + 1) * 10;
		this.b3 = player.inventory.scored(Resource.bone) * (this.random.nextInt(2) + 1) * 10;
		this.b4 = player.inventory.scored(Resource.gunp) * (this.random.nextInt(2) + 1) * 10;
		this.b5 = player.inventory.scored(Resource.bookant) * (this.random.nextInt(2) + 1) * (this.random.nextInt(2) + 1) * 15;
		this.b6 = player.inventory.scored(Resource.arrow) * (this.random.nextInt(2) + 1) * 10;
		this.s1 = "Cloths:+" + this.b1;
		this.s2 = "Slimes:+" + this.b2;
		this.s3 = "Bones:+" + this.b3;
		this.s4 = "Gunpowders:+" + this.b4;
		this.s5 = "Books:+" + this.b5;
		this.s6 = "Arrows:+" + this.b6;
		this.ml = this.s1.length();
		if(this.s2.length() > this.ml) {
			this.ml = this.s2.length();
		}

		if(this.s3.length() > this.ml) {
			this.ml = this.s3.length();
		}

		if(this.s4.length() > this.ml) {
			this.ml = this.s4.length();
		}

		if(this.s5.length() > this.ml) {
			this.ml = this.s5.length();
		}

		if(this.s6.length() > this.ml) {
			this.ml = this.s6.length();
		}

		this.finalscore = Player.score + this.b1 + this.b2 + this.b3 + this.b4 + this.b5 + this.b6;
	}

	public void tick() {
		if(this.input.getKey("menu").clicked) {
			this.game.setMenu(new TitleMenu());
		}

	}

	public void unlocked(Screen screen, List unlocked) {
		Font.renderFrame(screen, "", this.w + 2, 3, this.w + 13, 7 + unlocked.size());
		Font.draw("Unlocked!", screen, this.w * 8 + 32 - 4, 32, Color.get(-1, 50, 50, 50));

		int i;
		for(i = 0; i < unlocked.size(); ++i) {
			Font.draw((String)unlocked.get(i), screen, this.w * 8 + 48 + 2, 48 + i * 12, Color.get(-1, 555, 555, 555));
		}

		if(!this.doneunlocked) {
			for(i = 0; i < unlocked.size(); ++i) {
				BufferedWriter bufferedWriter = null;

				try {
					bufferedWriter = new BufferedWriter(new FileWriter(this.location + "/unlocks.miniplussave", true));
					if(((String)unlocked.get(i)).contains("M")) {
						bufferedWriter.write("," + ((String)unlocked.get(i)).substring(0, ((String)unlocked.get(i)).indexOf("M")) + "MINUTEMODE");
					} else if(((String)unlocked.get(i)).contains("H")) {
						bufferedWriter.write("," + ((String)unlocked.get(i)).substring(0, ((String)unlocked.get(i)).indexOf("H")) + "HOURMODE");
					}
				} catch (IOException var14) {
					var14.printStackTrace();
				} finally {
					try {
						if(bufferedWriter != null) {
							bufferedWriter.flush();
							bufferedWriter.close();
						}
					} catch (IOException var13) {
						var13.printStackTrace();
					}

					TitleMenu.loadedunlocks = false;
					this.doneunlocked = true;
				}
			}
		}

	}

	public void render(Screen screen) {
		boolean hastime = false;
		Player var10000 = this.game.player;
		if(Player.score <= 9999999) {
			this.w = 21;
		} else {
			Player var10003 = this.game.player;
			this.w = 14 + 1 + (int)Math.floor(Math.log10((double)Player.score));
		}

		if(this.w <= this.ml) {
			this.w = this.ml + 2;
		}

		if(this.w <= ("Final Score:" + this.finalscore).length() + 2) {
			this.w = ("Final Score:" + this.finalscore).length() + 2;
		}

		Font.renderFrame(screen, "", 1, 3, this.w, 20);
		Font.draw("Game Over! (" + ModeMenu.time + ")", screen, 16, 32, Color.get(-1, 555, 555, 555));
		ArrayList unlocked = new ArrayList();
		int seconds;
		if(ModeMenu.time.contains("20M")) {
			hastime = false;

			for(seconds = 0; seconds < ModeMenu.unlockedtimes.size(); ++seconds) {
				if(((String)ModeMenu.unlockedtimes.get(seconds)).contains("10M")) {
					hastime = true;
					break;
				}

				hastime = false;
			}

			if(!hastime && this.finalscore > '\uafc8') {
				unlocked.add("10M");
			}
		}

		if(ModeMenu.time.contains("1H")) {
			hastime = false;

			for(seconds = 0; seconds < ModeMenu.unlockedtimes.size(); ++seconds) {
				if(((String)ModeMenu.unlockedtimes.get(seconds)).contains("2H")) {
					hastime = true;
					break;
				}

				hastime = false;
			}

			if(!hastime && this.finalscore > 100000) {
				unlocked.add("2Hr");
			}
		}

		if(unlocked.size() > 0) {
			this.unlocked(screen, unlocked);
		}

		seconds = this.game.gameTime / 60;
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

		Font.draw("Score:", screen, 16, 48, Color.get(-1, 555, 555, 555));
		StringBuilder var8 = new StringBuilder();
		Player var10001 = this.game.player;
		Font.draw(var8.append(Player.score).toString(), screen, 64, 48, Color.get(-1, 550, 550, 550));
		Font.draw("<Bonuses>", screen, 16, 64, Color.get(-1, Color.rgb(0, 200, 0), Color.rgb(0, 200, 0), Color.rgb(0, 200, 0)));
		Font.draw(this.s1, screen, 16, 80, Color.get(-1, 550, 550, 550));
		Font.draw(this.s2, screen, 16, 88, Color.get(-1, 550, 550, 550));
		Font.draw(this.s3, screen, 16, 96, Color.get(-1, 550, 550, 550));
		Font.draw(this.s4, screen, 16, 104, Color.get(-1, 550, 550, 550));
		Font.draw(this.s6, screen, 16, 112, Color.get(-1, 550, 550, 550));
		Font.draw(this.s5, screen, 16, 120, Color.get(-1, 550, 550, 550));
		Font.draw("Final Score:", screen, 16, 136, Color.get(-1, 555, 555, 555));
		Font.draw("" + this.finalscore, screen, 112, 136, Color.get(-1, 550, 550, 550));
		if(this.finalscore == 0) {
			Font.draw("Fail!", screen, 128, 136, Color.get(-1, 500, 500, 500));
		}

		Font.draw("Press X to continue", screen, 16, 152, Color.get(-1, 333, 333, 333));
	}
}
