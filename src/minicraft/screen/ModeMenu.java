package minicraft.screen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import minicraft.Game;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.FontStyle;
import minicraft.gfx.Screen;
import minicraft.Sound;

public class ModeMenu extends Menu {
	private Menu parent;

	public static String[] modes = {"Survival", "Creative", "Hardcore", "Score"};
	public static boolean survival;
	public static boolean creative;
	public static boolean hardcore;
	public static boolean score;
	public static int mode = 1;
	
	private static int selectedTime = 0;
	private static List<String> times = new ArrayList<String>();
	public static List<String> unlockedtimes = new ArrayList<String>();
	
	static {
		initTimeList();
	}
	
	public ModeMenu() {
		selectedTime = 0;
	}
	
	public static void initTimeList() {
		times.clear();
		
		times.add("20M");
		times.add("30M");
		times.add("40M");
		times.add("1H");
		
		if(unlockedtimes.size() > 0) {
			times.addAll(unlockedtimes);
			
			HashMap<String, Integer> timeMap = new HashMap<String, Integer>();
			for(String time: times)
				timeMap.put(time, getScoreTime(time));
			
			Collections.sort(times, new Comparator<String>() {
				public int compare(String t1, String t2) {
					if (timeMap.get(t1) > timeMap.get(t2))
						return 1;
					else if (timeMap.get(t1) < timeMap.get(t2))
						return -1;
					
					return 0;
				}
			});
		}
	}
	
	public void tick() {
		if (input.getKey("left").clicked) {
			mode--;
			Sound.craft.play();
		}
		if (input.getKey("right").clicked) {
			mode++;
			Sound.craft.play();
		}
		
		if (mode > 4) mode = 1;
		if (mode < 1) mode = 4;
		
		updateModeBools(mode);
		
		if (score && input.getKey("t").clicked) { //selected is always 0..?
			selectedTime++;
			Sound.test.play();
			if(selectedTime > times.size() - 1)
				selectedTime = 0;
		}
		
		if (input.getKey("z").clicked)
			game.setMenu(new WorldGenMenu());
		else if(input.getKey("select").clicked)
			game.setMenu(new LoadingMenu());
		else if (input.getKey("exit").clicked)
			game.setMenu(new TitleMenu());
	}
	
	public static void updateModeBools(int mode) {
		if(ModeMenu.mode != mode && Game.isValidServer())
			Game.server.updateMode(mode);
		
		ModeMenu.mode = mode;
		
		survival = mode == 1;
		creative = mode == 2;
		hardcore = mode == 3;
		score = mode == 4;
	}
	
	public static String getSelectedTime() {
		if(score)
			return times.get(selectedTime);
		else
			return "Infinity";
	}
	
	public static int getScoreTime() { return getScoreTime(getSelectedTime()); }
	private static int getScoreTime(String timeStr) {
		int time = 0;
		
		Matcher matcher = Pattern.compile("(\\d+)(\\w+)").matcher(timeStr);
		
		while(matcher.find()) {
			String unit = matcher.group(2);
			int amount = Integer.parseInt(matcher.group(1));
			if(unit.contains("H")) time += amount * 60 * 60;
			else if(unit.contains("M")) time += amount * 60;
		}
		
		time *= Game.normSpeed;
		//if (Game.debug) System.out.println("score time: " + time);
		
		return time;
	}
	
	public static void setScoreTime(String timeStr) {
		if(!times.contains(timeStr)) {
			times.add(timeStr);
			selectedTime = times.size() - 1;
		}
		else {
			for(int i = 0; i < times.size(); i++)
				if(times.get(i).equals(timeStr))
					selectedTime = i;
		}
	}

	public void render(Screen screen) {
		int color = Color.get(-1, 300);
		int textCol = Color.get(-1, 555);
		screen.clear(0);
		
		Font.drawCentered("World Name:", screen, screen.h - 180, Color.get(-1, 444));
		Font.drawCentered(WorldSelectMenu.worldname, screen, screen.h - 170, Color.get(-1, 5));
		
		String modeText = "Game Mode:	" + modes[mode - 1];
		new FontStyle(Color.get(-1, 555)).setYPos(8*8).setShadowType(Color.get(-1, 111), false).draw(modeText, screen);
		
		if(mode == 4) Font.drawCentered("<T>ime: " + getSelectedTime(), screen, 95, Color.get(-1, 555));
		
		Font.drawCentered("Press "+input.getMapping("select")+" to Start", screen, screen.h - 75, textCol);
		Font.drawCentered("Press Left and Right", screen, screen.h - 150, textCol);
		Font.drawCentered("Press "+input.getMapping("exit")+" to Return", screen, screen.h - 55, textCol);
		Font.drawCentered("Press Z for world options", screen, screen.h - 35, textCol);
	}
}
