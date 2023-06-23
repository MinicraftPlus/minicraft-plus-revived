package minicraft.core;

import minicraft.core.io.Localization;
import minicraft.core.io.Settings;
import minicraft.entity.furniture.Bed;
import minicraft.entity.mob.Player;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;
import minicraft.saveload.Save;
import minicraft.screen.DebugPanelDisplay;
import minicraft.screen.Display;
import minicraft.screen.EndGameDisplay;
import minicraft.screen.LevelTransitionDisplay;
import minicraft.screen.PlayerDeathDisplay;
import minicraft.screen.TutorialDisplayHandler;
import minicraft.screen.WorldSelectDisplay;
import minicraft.util.AdvancementElement;
import minicraft.util.Logging;

import java.awt.GraphicsDevice;

public class Updater extends Game {
	private Updater() {}

	// TIME AND TICKS

	public static final int normSpeed = 60; // Measured in ticks / second.
	public static float gamespeed = 1; // Measured in MULTIPLES OF NORMSPEED.
	public static boolean paused = true; // If the game is paused.

	public static int tickCount = 0; // The number of ticks since the beginning of the game day.
	static int time = 0; // Facilites time of day / sunlight.
	public static final int dayLength = 64800; // This value determines how long one game day is.
	public static final int sleepEndTime = dayLength/8; // This value determines when the player "wakes up" in the morning.
	public static final int sleepStartTime = dayLength/2+dayLength/8; // This value determines when the player allowed to sleep.
	//public static int noon = 32400; // This value determines when the sky switches from getting lighter to getting darker.

	public static int gameTime = 0; // This stores the total time (number of ticks) you've been playing your
	public static boolean pastDay1 = true; // Used to prevent mob spawn on surface on day 1.
	public static int scoreTime; // Time remaining for score mode

	/**
	 * Indicates if FullScreen Mode has been toggled.
	 */
	static boolean FULLSCREEN;

	// AUTOSAVE AND NOTIFICATIONS

	public static boolean updateNoteTick = false;
	public static int notetick = 0; // "note"= notifications.

	private static final int astime = 7200; // tands for Auto-Save Time (interval)
	public static int asTick = 0; // The time interval between autosaves.
	public static boolean saving = false; // If the game is performing a save.
	public static int savecooldown; // Prevents saving many times too fast, I think.
	public static int screenshot = 0; // Counter for screenshot queries.

	public enum Time {
		Morning (0),
		Day (dayLength/4),
		Evening (dayLength/2),
		Night (dayLength/4*3);

		public int tickTime;

		Time(int ticks) {
			tickTime = ticks;
		}
	}

	static void updateFullscreen() {
		// Dispose is needed to set undecorated value
		Initializer.frame.dispose();

		GraphicsDevice device = Initializer.frame.getGraphicsConfiguration().getDevice();
		if (Updater.FULLSCREEN) {
			Initializer.frame.setUndecorated(true);
			device.setFullScreenWindow(Initializer.frame);
		} else {
			Initializer.frame.setUndecorated(false);
			device.setFullScreenWindow(null);
		}

		// Show frame again
		Initializer.frame.setVisible(true);
		// When fullscreen is enabled, focus is lost
		Renderer.canvas.requestFocus();
	}

	// VERY IMPORTANT METHOD!! Makes everything keep happening.
	// In the end, calls menu.tick() if there's a menu, or level.tick() if no menu.
	public static void tick() {
		if (input.getKey("FULLSCREEN").clicked) {
			Updater.FULLSCREEN = !Updater.FULLSCREEN;
			Updater.updateFullscreen();
		}

		if (input.getKey("screenshot").clicked) {
			screenshot++;
		}

		if (currentDisplay != displayQuery.peek() && !displayQuery.isEmpty()) {
			Display prevDisplay = currentDisplay; // For both null or not null.
			currentDisplay = displayQuery.peek();
			assert currentDisplay != null;
			currentDisplay.init(prevDisplay);
		}

		if (currentDisplay != null && displayQuery.isEmpty()) {
			currentDisplay.onExit();
			currentDisplay = null;
		}

		assert currentDisplay == displayQuery.peek(); // This should be true.
		while (displayQuery.size() > 1) {
			Display prevDisplay = displayQuery.pop();
			// assert curDisplay == prevDisplay;
			currentDisplay = displayQuery.peek();
			assert currentDisplay != null;
			currentDisplay.init(prevDisplay);
		}

		Level level = levels[currentLevel];
		if (Bed.sleeping()) {
			// IN BED
			if (gamespeed != 20) {
				gamespeed = 20;
			}
			if (tickCount > sleepEndTime) {
				Logging.WORLD.trace("Passing midnight in bed.");
				pastDay1 = true;
				tickCount = 0;
			}
			if (tickCount <= sleepStartTime && tickCount >= sleepEndTime) { // It has reached morning.
				Logging.WORLD.trace("Reached morning, getting out of bed.");
				gamespeed = 1;
				Bed.restorePlayers();
			}
		}

		// Auto-save tick; marks when to do autosave.
		if(!paused)
			asTick++;
		if (asTick > astime) {
			if ((boolean) Settings.get("autosave") && !gameOver && player.health > 0) {

				new Save(WorldSelectDisplay.getWorldName());
			}

			asTick = 0;
		}

		// Increment tickCount if the game is not paused
		if (!paused) setTime(tickCount+1);

		// SCORE MODE ONLY

		if (isMode("minicraft.settings.mode.score") && (!paused && !gameOver)) {
			if (scoreTime <= 0) { // GAME OVER
				gameOver = true;
				setDisplay(new EndGameDisplay());
			}

			scoreTime--;
		}

		if (updateNoteTick) notetick++;

		// This is the general action statement thing! Regulates menus, mostly.
		if (!Renderer.canvas.hasFocus()) {
			input.releaseAll();
		}
		if (Renderer.canvas.hasFocus()) {
			gameTime++;

			input.tick(); // INPUT TICK; no other class should call this, I think...especially the *Menu classes.
			TutorialDisplayHandler.tick(input);
			AdvancementElement.AdvancementTrigger.tick();

			if (currentDisplay != null) {
				// A menu is active.
				if (player != null)
					player.tick(); // It is CRUCIAL that the player is ticked HERE, before the menu is ticked. I'm not quite sure why... the menus break otherwise, though.
				currentDisplay.tick(input);
				paused = true;
			} else {
				// No menu, currently.
				paused = false;

				// If player is alive, but no level change, nothing happens here.
				if (player.isRemoved() && Renderer.readyToRenderGameplay && !Bed.inBed(player)) {
					// Makes delay between death and death menu.
					World.playerDeadTime++;
					if (World.playerDeadTime > 60) {
						setDisplay(new PlayerDeathDisplay());
					}
				} else if (World.pendingLevelChange != 0) {
					setDisplay(new LevelTransitionDisplay(World.pendingLevelChange));
					World.pendingLevelChange = 0;
				}

				player.tick(); // Ticks the player when there's no menu.

				if (level != null) {
					level.tick(true);
					Tile.tickCount++;
				}

				if (currentDisplay == null && input.getKey("F3").clicked) { // Shows debug info in upper-left
					Renderer.showDebugInfo = !Renderer.showDebugInfo;
				}

				// For debugging only
				if (debug && currentDisplay == null && input.getKey("F4").clicked) {
					Game.setDisplay(new DebugPanelDisplay());
				}
			} // End "menu-null" conditional
		} // End hasfocus conditional
	} // End tick()


	// This is the proper way to change the tickCount.
	public static void setTime(int ticks) {
		if (ticks < Time.Morning.tickTime) ticks = 0; // Error correct
		if (ticks < Time.Day.tickTime) time = 0; // Morning
		else if (ticks < Time.Evening.tickTime) time = 1; // Day
		else if (ticks < Time.Night.tickTime) time = 2; // Evening
		else if (ticks < dayLength) time = 3; // Night
		else { // Back to morning
			time = 0;
			ticks = 0;
			pastDay1 = true;
		}
		tickCount = ticks;
	}

	// This is the proper way to change the time of day.
	public static void changeTimeOfDay(Time t) {
		setTime(t.tickTime);
	}
	// This one works too.
	public static void changeTimeOfDay(int t) {
		Time[] times = Time.values();
		if (t > 0 && t < times.length)
			changeTimeOfDay(times[t]); // It just references the other one.
		else
			Logging.WORLD.info("Time " + t + " does not exist.");
	}

	public static Time getTime() {
		Time[] times = Time.values();
		return times[time];
	}

	/** This adds a notification to all player games. */
	public static void notifyAll(String msg) {
		notifyAll(msg, 0);
	}
	public static void notifyAll(String msg, int notetick) {
		msg = Localization.getLocalized(msg);
		notifications.add(msg);
		Updater.notetick = notetick;
	}
}
