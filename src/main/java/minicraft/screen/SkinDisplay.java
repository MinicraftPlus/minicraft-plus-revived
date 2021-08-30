package minicraft.screen;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.imageio.ImageIO;

import minicraft.core.FileHandler;
import minicraft.core.Game;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Settings;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteSheet;
import minicraft.saveload.Save;

public class SkinDisplay extends Display {

	public static final String DEFAULT_SKIN = "Paul"; // Default Skin :)
    public static final String CAPE_SKIN = "Paul (Cape)"; // Built-in Skin :)
    public static final String FAMILIARBOY_SKIN = "Familiar Boy"; // Built-in Skin :)
    public static final String FAMILIARGIRL_SKIN = "Familiar Girl"; // Built-in Skin :)
    public static final String CUSTOM_SKIN = "Custom"; // User's Custom Imported Skin :)

	private static final String[] ENTRY_NAMES = new String[] { "skins.png" }; // Spritesheets

	public final List<String> skinList;
	public final List<String> skinIDList;
	private final File location;

	private static int offset = 0;
	private int selected;
	public static String SkinOutput = SkinDisplay.DEFAULT_SKIN;

	private Screen lastScreen;

	/* The skins are put in a folder generated by the game called "Skins".
	 * Many skins can be put according to the number of files.
	 */

	public SkinDisplay() {
		this.skinList = new ArrayList<>();
		this.skinIDList = new ArrayList<>();
		this.skinList.add(SkinDisplay.DEFAULT_SKIN);
		this.skinList.add(SkinDisplay.CAPE_SKIN);
		this.skinList.add(SkinDisplay.FAMILIARBOY_SKIN);
		this.skinList.add(SkinDisplay.FAMILIARGIRL_SKIN);
		this.skinIDList.add(SkinDisplay.DEFAULT_SKIN);
		this.skinIDList.add(SkinDisplay.CAPE_SKIN);
		this.skinIDList.add(SkinDisplay.FAMILIARBOY_SKIN);
		this.skinIDList.add(SkinDisplay.FAMILIARGIRL_SKIN);
		this.skinIDList.add(SkinDisplay.CUSTOM_SKIN);

		// Generate skins folder and/or read it.
		this.location = new File(FileHandler.getSystemGameDir() + "/" + FileHandler.getLocalGameDir() + "/Skins");
		this.location.mkdirs();

		// Read and add the .png file to the skins list
		for (String fileName : Objects.requireNonNull(location.list())) {
			if (fileName.endsWith(".png")) { // Only .png skins files ok?
				skinList.add(fileName);
			}
		}

		selected = Settings.getIdx("Skins") + offset;
	}

	public void updateSpriteSheet(Screen screen) throws IOException {
		SpriteSheet[] sheets = new SpriteSheet[1];
		sheets[0] = new SpriteSheet(ImageIO.read(Game.class.getResourceAsStream("/resources/textures/skins.png")));

		if (selected == 0) {
			SkinOutput = skinIDList.get(0);
		}
		else if (selected == 1) {
			SkinOutput = skinIDList.get(1);
		}
		else if (selected == 2) {
			SkinOutput = skinIDList.get(2);
		}
		else if (selected == 3) {
			SkinOutput = skinIDList.get(3);
		}
		else if (selected > 3) {
			SkinOutput = skinIDList.get(4);
			File imageLocation = new File(location, skinList.get(selected));
			sheets[0] = new SpriteSheet(ImageIO.read(imageLocation));
		}
		screen.setSkinSheet(sheets[0]);
	}

    @Override
    public void onExit() {
        Settings.getEntry("Skins").setSelection(selected);
        try {
            updateSpriteSheet(lastScreen);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
		new Save();
        if (selected > 3) {
        	offset = (selected - 4);
			Settings.setIdx("Skins", 4);
		} else {
			Settings.setIdx("Skins", selected);
		}
        Game.exitMenu();
    }

	@Override
	public void tick(InputHandler input) {
		if (input.getKey("menu").clicked || input.getKey("attack").clicked || input.getKey("exit").clicked) {
			Game.exitMenu();
			return;
		}
		if (input.getKey("cursor-down").clicked && selected > 0) {
			selected--;

		}
		if (input.getKey("cursor-up").clicked && selected < skinList.size() - 1) {
			selected++;
		}
	}

	// In case the name is too big ...
	private static String shortNameIfLong(String name) {
		return name.length() > 22 ? name.substring(0, 16) + "..." : name;
	}

	@Override
	public void render(Screen screen) {
		screen.clear(0);

		lastScreen = screen;

		String selectedUp = selected + 1 > skinList.size() - 1 ? "" : skinList.get(selected + 1);
		String selectedDown = selected - 1 < 0 ? "" : skinList.get(selected - 1);

		// Render the menu
		Font.drawCentered("Skins", screen, Screen.h - 180, Color.YELLOW); // Title
		Font.drawCentered(SkinDisplay.shortNameIfLong(selectedDown), screen, Screen.h - 70, Color.GRAY); // Unselected space
		Font.drawCentered(SkinDisplay.shortNameIfLong(skinList.get(selected)), screen, Screen.h - 80, Color.GREEN); // Selection
		Font.drawCentered(SkinDisplay.shortNameIfLong(selectedUp), screen, Screen.h - 90, Color.GRAY); // Other unselected space
		Font.drawCentered("Use "+ Game.input.getMapping("cursor-down") + ", " + Game.input.getMapping("cursor-up") + ", " + Game.input.getMapping("SELECT"), screen, Screen.h - 11, Color.get(0, 222, 222, 222)); // Controls

		int h = 2;
		int w = 16;
		int xo = (Screen.w - w * 8) / 2;
		int yo = 28;

		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				if (selected == 0) {
					try {
						updateSpriteSheet(lastScreen);
					} catch (IOException exception) {
						exception.printStackTrace();
					}
					screen.render(xo + x * 8, yo + y * 8, x + (y + 16) * 32, 0, 2); // Paul
				}
				else if (selected == 1) {
					try {
						updateSpriteSheet(lastScreen);
					} catch (IOException exception) {
						exception.printStackTrace();
					}
					screen.render(xo + x * 8, yo + y * 8, x + y * 32, 0, 4); // Paul (Cape)
				}
				else if (selected == 2) {
					try {
						updateSpriteSheet(lastScreen);
					} catch (IOException exception) {
						exception.printStackTrace();
					}
					screen.render(xo + x * 8, yo + y * 8, x + (y + 4) * 32, 0, 4); // Familiar Boy
				}
				else if (selected == 3) {
					try {
						updateSpriteSheet(lastScreen);
					} catch (IOException exception) {
						exception.printStackTrace();
					}
					screen.render(xo + x * 8, yo + y * 8, x + (y + 8) * 32, 0, 4); // Familiar Girl
				}
				else if (selected > 3) {
					try {
						updateSpriteSheet(lastScreen);
					} catch (IOException exception) {
						exception.printStackTrace();
					}
					screen.render(xo + x * 8, yo + y * 8, x + y * 32, 0, 4); // Custom Skin
				}

			}
		}

		/* Unnecessary for now...
	     * This part is used to test the change of textures
	     * in some future it may be useful
	     *
	     *screen.render(24, 48, 134, Color.get(-1, 110, 330, 550), 0);
	     *screen.render(48, 48, 136, Color.get(-1, 110, 330, 550), 0);
	     *screen.render(72, 48, 164, Color.get(-1, 100, 321, 45), 0);
	     *screen.render(96, 48, 384, Color.get(0, 200, 500, 533), 0);
	     *screen.render(120, 48, 135, Color.get(-1, 100, 320, 430), 0);
	     */
	}

}
