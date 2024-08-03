package minicraft.screen;

import minicraft.core.Game;
import minicraft.core.World;
import minicraft.core.io.Localization;
import minicraft.gfx.Color;
import minicraft.gfx.Ellipsis;
import minicraft.gfx.Ellipsis.DotUpdater.TimeUpdater;
import minicraft.gfx.Ellipsis.SmoothEllipsis;
import minicraft.gfx.Font;
import minicraft.gfx.FontStyle;
import minicraft.gfx.Screen;
import minicraft.saveload.Load;
import minicraft.saveload.Save;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.StringEntry;

import javax.swing.Timer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

public class LoadingDisplay extends Display {

	private static float percentage = 0;
	private static String progressType = "";
	private static boolean localizeProgressType = true;

	private final Timer t;
	private final Ellipsis ellipsis = new SmoothEllipsis(new TimeUpdater());

	private String msg = "";

	public LoadingDisplay() {
		super(true, false);
		t = new Timer(500, e -> new Thread(() -> { // A new thread is required as this blocks the running thread.
			try {
				Load.setDataFixer(null); // Resets fixer
				World.initWorld();
				Game.setDisplay(null);
			} catch (Load.UserPromptCancelledException ex) {
				World.onWorldExits();
				Game.exitDisplay(); // Exits the loading display and returns to world select display.
				Game.setDisplay(new PopupDisplay(null,
					"minicraft.displays.loading.user_cancellation_popup.display"));
			} catch (Load.BackupCreationFailedException ex) {
				World.onWorldExits();
				Game.exitDisplay(); // Exits the loading display and returns to world select display.
				Game.setDisplay(new PopupDisplay(null,
					StringEntry.useLines(Color.WHITE, false, Localization.getLocalized(
						"minicraft.displays.loading.backup_creation_failed_popup.display", getErrorMessage(ex.getCause())))));
			} catch (Load.WorldLoadingFailedException ex) {
				World.onWorldExits();
				Game.exitDisplay(); // Exits the loading display and returns to world select display.
				Load.AutoDataFixer dataFixer;
				if ((dataFixer = Load.getDataFixer()) != null) {
					AtomicBoolean acted = new AtomicBoolean(false);
					AtomicBoolean perform = new AtomicBoolean(false);
					ArrayList<PopupDisplay.PopupActionCallback> callbacks = new ArrayList<>();
					callbacks.add(new PopupDisplay.PopupActionCallback("EXIT", m -> {
						acted.set(true);
						return true;
					}));
					callbacks.add(new PopupDisplay.PopupActionCallback("SELECT", m -> {
						perform.set(true);
						acted.set(true);
						return true;
					}));

					ArrayList<ListEntry> entries = new ArrayList<>();
					Collections.addAll(entries, StringEntry.useLines(Color.WHITE, false,
						Localization.getLocalized("minicraft.displays.loading.corrupted_world_fixer_available.display",
							WorldSelectDisplay.getWorldName())));
					Collections.addAll(entries, StringEntry.useLines(Color.WHITE, false,
						Localization.getLocalized("minicraft.displays.loading.corrupted_world_fixer_available.select",
							Game.input.getMapping("SELECT"))));
					Game.setDisplay(new PopupDisplay(new PopupDisplay.PopupConfig(
						"minicraft.displays.loading.corrupted_world.title", callbacks, 2),
						entries.toArray(new ListEntry[0])));

					while (true) {
						if (acted.get()) {
							if (perform.get()) {
								dataFixer.startFixer(WorldSelectDisplay.getWorldName());
							} else {
								Game.setDisplay(new PopupDisplay(new PopupDisplay.PopupConfig(
									"minicraft.displays.loading.corrupted_world_fixing_cancelled.title",
									null, 2),
									StringEntry.useLines(Color.WHITE, false, Localization.getLocalized(
										"minicraft.displays.loading.corrupted_world_fixing_cancelled.display",
										getErrorMessage(ex.getCause())))));
							}

							break;
						}

						try {
							//noinspection BusyWait
							Thread.sleep(10);
						} catch (InterruptedException ignored) { }
					}
				} else
					Game.setDisplay(new PopupDisplay(null,
						StringEntry.useLines(Color.WHITE, false, Localization.getLocalized(
							"minicraft.displays.loading.loading_failed_popup.display", getErrorMessage(ex.getCause())))));
			}
		}, "World Initialization Thread").start());
		t.setRepeats(false);
	}

	private String getErrorMessage(Throwable e) {
		StringBuilder msg = new StringBuilder(e.getMessage());
		while ((e = e.getCause()) != null) {
			msg.append(": ").append(e.getMessage());
		}

		return msg.toString();
	}

	@Override
	public void init(Display parent) {
		if (parent != null && parent.getParent() == this) return; // Undefined behaviour
		super.init(parent);
		percentage = 0;
		progressType = "minicraft.displays.loading.message.world";
		localizeProgressType = true;
		if (WorldSelectDisplay.hasLoadedWorld())
			msg = "minicraft.displays.loading.message.loading";
		else
			msg = "minicraft.displays.loading.message.generating";
		t.start();
	}

	@Override
	public void onExit() {
		percentage = 0;
		if (!WorldSelectDisplay.hasLoadedWorld()) {
			msg = "minicraft.displays.loading.message.saving";
			progressType = "minicraft.displays.loading.message.world";
			localizeProgressType = true;
			new Save(WorldSelectDisplay.getWorldName());
			Game.notifications.clear();
		}
	}

	public static void setPercentage(float percent) {
		percentage = percent;
	}

	public static float getPercentage() {
		return percentage;
	}

	public static void setMessage(String progressType) {
		setMessage(progressType, true);
	}

	public static void setMessage(String progressType, boolean localize) {
		LoadingDisplay.progressType = progressType;
		localizeProgressType = localize;
	}

	public static void progress(float amt) {
		percentage = Math.min(100, percentage + amt);
	}

	@Override
	public void render(Screen screen) {
		super.render(screen);
		int percent = Math.round(percentage);
		Font.drawParagraph(screen, new FontStyle(Color.RED), 6,
			Localization.getLocalized(msg) + (progressType.length() > 0 ? " " + (localizeProgressType ? Localization.getLocalized(progressType) : progressType) : "")
				+ ellipsis.updateAndGet(),
			percent + "%"
		);
	}
}
