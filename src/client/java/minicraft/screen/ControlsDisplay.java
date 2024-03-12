package minicraft.screen;

import minicraft.core.Game;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Point;
import minicraft.gfx.Screen;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.StringEntry;

import java.util.ArrayList;
import java.util.Arrays;

public class ControlsDisplay extends Display {
	private ListEntry[] keyControls;
	private ListEntry[] controllerControls;

	private int displaying; // 0 for keyboard; 1 for controller.

	public ControlsDisplay() {
		super(true, true, new Menu.Builder(false, 0, RelPos.CENTER)
			.setSelectable(true)
			.setPositioning(new Point(Screen.w / 2, 20), RelPos.BOTTOM)
			.setDisplayLength(17)
			.createMenu()
		);

		initKeyControls();
		initControllerControls();

		displaying = Game.input.getLastInputType();
		switchingControls();
	}

	private void initKeyControls() {
		ArrayList<ListEntry> entries = new ArrayList<>(23);
		for (int i = 0; i < 23; i++)
			entries.addAll(Arrays.asList(StringEntry.useLines(String.format("minicraft.displays.controls.display.keyboard.%02d", i))));
		entries.forEach(e -> e.setSelectable(true));
		keyControls = entries.toArray(new ListEntry[0]);
	}

	private void initControllerControls() {
		ArrayList<ListEntry> entries = new ArrayList<>(16);
		for (int i = 0; i < 16; i++)
			entries.addAll(Arrays.asList(StringEntry.useLines(String.format("minicraft.displays.controls.display.controller.%02d", i))));
		entries.forEach(e -> e.setSelectable(true));
		controllerControls = entries.toArray(new ListEntry[0]);
	}

	private void switchingControls() {
		menus[0].setEntries(displaying == 0 ? keyControls : controllerControls);
		menus[0].setSelection(0);
	}

	@Override
	public void render(Screen screen) {
		super.render(screen);

		Font.drawCentered(Localization.getLocalized("minicraft.displays.controls"), screen, 0, Color.WHITE);
		Font.drawCentered(Localization.getLocalized(displaying == 0 ? "minicraft.displays.controls.display.keyboard" : "minicraft.displays.controls.display.controller"), screen, 10, Color.WHITE);

		if (displaying == 0) { // If displaying keyboard mappings.
			Font.drawCentered(Localization.getLocalized("minicraft.displays.controls.display.keyboard.desc"), screen, Screen.h - 16, Color.GRAY);
		} else { // If displaying controller mappings.
			Font.drawCentered(Localization.getLocalized("minicraft.displays.controls.display.controller.desc.0"), screen, Screen.h - 24, Color.GRAY);
			Font.drawCentered(Localization.getLocalized("minicraft.displays.controls.display.controller.desc.1"), screen, Screen.h - 16, Color.GRAY);
		}

		Font.drawCentered(Localization.getLocalized("minicraft.displays.controls.display.help.0", Game.input.getMapping("cursor-left"), Game.input.getMapping("cursor-right")), screen, Screen.h - 8, Color.GRAY);
	}

	@Override
	public void tick(InputHandler input) {
		super.tick(input);

		if (input.inputPressed("cursor-left") || input.inputPressed("cursor-right")) {
			displaying = displaying ^ 1;
			switchingControls();
		}
	}
}
