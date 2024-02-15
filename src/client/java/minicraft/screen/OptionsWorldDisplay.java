package minicraft.screen;

import minicraft.core.Game;
import minicraft.core.io.Settings;
import minicraft.gfx.Color;
import minicraft.saveload.Save;
import minicraft.screen.entry.BlankEntry;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.SelectEntry;
import minicraft.screen.entry.StringEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

public class OptionsWorldDisplay extends Display {
	public OptionsWorldDisplay() {
		super(true);

		List<ListEntry> entries = getEntries();

		if (TutorialDisplayHandler.inTutorial()) {
			entries.add(new SelectEntry("minicraft.displays.options_world.skip_current_tutorial", () -> {
				TutorialDisplayHandler.skipCurrent();
				Game.exitDisplay();
			}));
			entries.add(new BlankEntry());
			entries.add(new SelectEntry("minicraft.displays.options_world.turn_off_tutorials", () -> {
				ArrayList<PopupDisplay.PopupActionCallback> callbacks = new ArrayList<>();
				callbacks.add(new PopupDisplay.PopupActionCallback("select", popup -> {
					TutorialDisplayHandler.turnOffTutorials();
					Executors.newCachedThreadPool().submit(() -> {
						Game.exitDisplay();
						try {
							Thread.sleep(50);
						} catch (InterruptedException ignored) {
						}
						Game.exitDisplay();
					});
					return true;
				}));

				Game.setDisplay(new PopupDisplay(new PopupDisplay.PopupConfig("minicraft.display.popup.title_confirm", callbacks, 4), StringEntry.useLines(Color.RED,
					"minicraft.displays.options_world.off_tutorials_confirm_popup", "minicraft.display.popup.enter_confirm", "minicraft.display.popup.escape_cancel")));
			}));
		}

		if (TutorialDisplayHandler.inQuests()) {
			entries.add(4, Settings.getEntry("showquests"));
		}

		menus = new Menu[]{
			new Menu.Builder(false, 6, RelPos.LEFT, entries)
				.setTitle("minicraft.displays.options_world")
				.createMenu()
		};
	}

	private List<ListEntry> getEntries() {
		return new ArrayList<>(Arrays.asList(Settings.getEntry("diff"),
			Settings.getEntry("fps"),
			Settings.getEntry("sound"),
			Settings.getEntry("autosave"),
			new SelectEntry("minicraft.display.options_display.change_key_bindings", () -> Game.setDisplay(new KeyInputDisplay())),
			new SelectEntry("minicraft.displays.controls", () -> Game.setDisplay(new ControlsDisplay())),
			new SelectEntry("minicraft.display.options_display.language", () -> Game.setDisplay(new LanguageSettingsDisplay())),
			Settings.getEntry("screenshot"),
			new SelectEntry("minicraft.display.options_display.resource_packs", () -> Game.setDisplay(new ResourcePackDisplay()))
		));
	}

	@Override
	public void onExit() {
		new Save();
		Game.MAX_FPS = (int) Settings.get("fps");
	}
}
