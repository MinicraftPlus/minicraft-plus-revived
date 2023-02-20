package minicraft.screen;

import javax.swing.Timer;

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
import minicraft.saveload.Save;

public class LoadingDisplay extends Display {

	private static float percentage = 0;
	private static String progressType = "";

	private final Timer t;
	private final Ellipsis ellipsis = new SmoothEllipsis(new TimeUpdater());

	private String msg = "";

	public LoadingDisplay() {
		super(true, false);
		t = new Timer(500, e -> {
			try {
				World.initWorld();
				Game.setDisplay(null);
			} catch (RuntimeException ex) {
				Throwable t = ex.getCause();
				if (t instanceof InterruptedException) {
					Game.exitDisplay();
					World.onWorldExits();
					Game.exitDisplay();
					Game.setDisplay(new PopupDisplay(null, "minicraft.displays.loading.regeneration_cancellation_popup.display"));
				} else
					throw ex;
			}
		});
		t.setRepeats(false);
	}

	@Override
	public void init(Display parent) {
		super.init(parent);
		percentage = 0;
		progressType = "minicraft.displays.loading.message.world";
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
			new Save(WorldSelectDisplay.getWorldName());
			Game.notifications.clear();
		}
	}

	public static void setPercentage(float percent) {
		percentage = percent;
	}
	public static float getPercentage() { return percentage; }
	public static void setMessage(String progressType) { LoadingDisplay.progressType = progressType; }

	public static void progress(float amt) {
		percentage = Math.min(100, percentage + amt);
	}

	@Override
	public void render(Screen screen) {
		super.render(screen);
		int percent = Math.round(percentage);
		Font.drawParagraph(screen, new FontStyle(Color.RED), 6,
			Localization.getLocalized(msg) + (progressType.length() > 0 ? " " + Localization.getLocalized(progressType) : "") + ellipsis.updateAndGet(),
			percent + "%"
		);
	}
}
