package minicraft.screen;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import minicraft.core.FileHandler;
import minicraft.core.Game;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.core.io.Sound;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.screen.Menu.Builder;
import minicraft.screen.entry.InputEntry;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.SelectEntry;
import minicraft.screen.entry.StringEntry;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

/**
 * Used to edit worlds. These actions include renaming, deleting, and copying worlds.
 */
public class WorldEditDisplay extends Display {
	private static final String worldsDir = Game.gameDir + "/saves/";
	private final Action action;

	private static String worldName;
	private boolean confirm;

	enum Action {
		Copy("C", Color.BLUE),
		Rename("R", Color.GREEN),
		Delete("D", Color.RED);

		public final String key;
		public final int color;

		Action(String key, int col) {
			this.key = key;
			this.color = col;
		}
	}

	public WorldEditDisplay(Action action) {
		super(true);
		this.action = action;
		this.confirm = false;
	}

	@Override
	public void init(@Nullable Display parent) {
		super.init(parent);

		if (getParent() instanceof WorldSelectDisplay) {
			ArrayList<ListEntry> entries = new ArrayList<>();

			ArrayList<String> names = WorldSelectDisplay.getWorldNames();
			for (String name : names) {
				entries.add(new SelectEntry(name, () -> {
					worldName = name;
					menus[1] = new Builder(true, 0, RelPos.CENTER, getConfirmMenuEntries(action)).setSelectable(true).createMenu();
					confirm = true;
					selection = 1;
				}, false) {
					@Override
					public int getColor(boolean isSelected) {
						if (isSelected)
							return action.color;
						return super.getColor(false);
					}
				});
			}

			menus = new Menu[] {
					new Menu.Builder(false, 0, RelPos.CENTER, entries)
							.setDisplayLength(5)
							.setScrollPolicies(1, true)
							.createMenu(),
					new Menu.Builder(true, 0, RelPos.CENTER).setShouldRender(false).createMenu()
			};
		}
	}

	@Override
	public void tick(InputHandler input) {
		if (confirm) {
			if (input.getKey("select").clicked) {
				InputEntry entry;

				// The location of the world folder on the disk.
				File world = new File(worldsDir + worldName);

				// Do the action.
				switch (action) {
					case Delete:
						Logger.debug("Deleting world: " + world);
						File[] list = world.listFiles();
						for (File file : list) {
							file.delete();
						}
						world.delete();
						break;

					case Copy:
						entry = (InputEntry) menus[1].getCurEntry();
						if (!entry.isValid())
							break;
						//user hits enter with a valid new name; copy is created here.
						String newname = entry.getUserInput();
						File newworld = new File(worldsDir + newname);
						newworld.mkdirs();
						Logger.debug("Copying world {} to world {}.", world, newworld);
						// walk file tree
						try {
							FileHandler.copyFolderContents(new File(worldsDir + worldName).toPath(), newworld.toPath(), FileHandler.REPLACE_EXISTING, false);
						} catch (IOException e) {
							e.printStackTrace();
						}
						break;

					case Rename:
						entry = (InputEntry) menus[1].getCurEntry();
						if (!entry.isValid())
							break;

						// User hits enter with a vaild new name; name is set here:
						String name = entry.getUserInput();

						// Try to rename the file, if it works, return
						if (world.renameTo(new File(worldsDir + name))) {
							Logger.debug("Renaming world {} to new name: {}", world, name);
						} else {
							Logger.error("Rename failed in WorldEditDisplay.");
						}
						break;
				}

				confirm = false;
				Sound.confirm.play();
				if (WorldSelectDisplay.getWorldNames().size() > 0)
					Game.setDisplay(new WorldSelectDisplay());
				else
					Game.setDisplay(new WorldGenDisplay());
				return;
			}
		}

		super.tick(input);
	}

	@Override
	public void render(Screen screen) {
		super.render(screen);

		Font.drawCentered(Game.input.getMapping("select") + Localization.getLocalized(" to confirm"), screen, Screen.h - 60, Color.GRAY);
		Font.drawCentered(Game.input.getMapping("exit") + Localization.getLocalized(" to return"), screen, Screen.h - 40, Color.GRAY);

		Font.drawCentered(Localization.getLocalized("Select a World to " + action), screen, 0, action.color);
	}

	private static List<ListEntry> getConfirmMenuEntries(Action action) {
		ArrayList<ListEntry> entries = new ArrayList<>();
		if (action == Action.Delete) {
			entries.addAll(Arrays.asList(
					new StringEntry("Are you sure you want to delete", action.color),
					new StringEntry("\"" + worldName + "\"?", Color.tint(action.color, 1, true)),
					new StringEntry("This can not be undone!", action.color)
			));
		} else {
			List<String> names = WorldSelectDisplay.getWorldNames();
			if(action == Action.Rename)
				names.remove(worldName);
			entries.add(new StringEntry("New World Name:", action.color));
			entries.add(WorldGenDisplay.makeWorldNameInput("", names, worldName));
		}

		entries.addAll(Arrays.asList(StringEntry.useLines(Color.WHITE, "",
				Game.input.getMapping("select") + " to confirm",
				Game.input.getMapping("exit") + " to cancel"
		)));

		return entries;
	}
}
