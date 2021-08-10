package minicraft.screen;

import minicraft.core.FileHandler;
import minicraft.core.Game;
import minicraft.core.Renderer;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Settings;
import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.*;
import minicraft.gfx.Color;
import minicraft.gfx.Font;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class SkinDisplay extends Display {

	private static final String DEFAULT_SKIN = "Paul"; // Default Skin :)
	private static final String CAPE_SKIN = "Paul (Cape)"; // Default texture :)
	private static final String FAMILIARBOY_SKIN = "Familiar Boy"; // Default texture :)

	private static final String[] ENTRY_NAMES = new String[] { "skins.png" }; // Spritesheets

	private final List<String> textureList;
	private int selected;
	private boolean shouldUpdate;

	public static MobSprite[][] Defaultsprites = MobSprite.compileMobSpriteAnimations(0, 16);
	public static MobSprite[][] Capesprites = MobSprite.compilePlayerSpriteAnimations(0, 0);
	public static MobSprite[][] FamiliarBoysprites = MobSprite.compilePlayerSpriteAnimations(0, 4);

	/* The texture packs are put in a folder generated by the game called "Textures packs".
	 * Many texture packs can be put according to the number of files.
	 */

	public SkinDisplay() {
		this.textureList = new ArrayList<>();
		this.textureList.add(SkinDisplay.DEFAULT_SKIN);
		this.textureList.add(SkinDisplay.CAPE_SKIN);
		this.textureList.add(SkinDisplay.FAMILIARBOY_SKIN);

		selected = Settings.getIdx("Skins");
	}

	public void updateSpriteSheet(Screen screen) throws IOException {
		MobSprite[][][] MobDisplayed = new MobSprite[1][][];

		if (selected == 0) {
			MobDisplayed[0] = Defaultsprites;
		}
		if (selected == 1) {
			MobDisplayed[0] = Capesprites;
		}
		if (selected == 2) {
			MobDisplayed[0] = FamiliarBoysprites;
		}
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
		if (input.getKey("cursor-up").clicked && selected < textureList.size() - 1) {
			selected++;

		}
		
        if (input.getKey("SELECT").clicked) {
            shouldUpdate = true;
        }
	}

	// In case the name is too big ...
	private static String shortNameIfLong(String name) {
		return name.length() > 22 ? name.substring(0, 16) + "..." : name;
	}

	@Override
	public void render(Screen screen) {
		screen.clear(0);

		if (shouldUpdate) {
			shouldUpdate = false;
			Settings.getEntry("Skins").setSelection(selected);
			Game.exitMenu();
		}

		String selectedUp = selected + 1 > textureList.size() - 1 ? "" : textureList.get(selected + 1);
		String selectedDown = selected - 1 < 0 ? "" : textureList.get(selected - 1);

		// Render the menu
		Font.drawCentered("Skins", screen, Screen.h - 180, Color.YELLOW); // Title
		Font.drawCentered(SkinDisplay.shortNameIfLong(selectedDown), screen, Screen.h - 70, Color.GRAY); // Unselected space
		Font.drawCentered(SkinDisplay.shortNameIfLong(textureList.get(selected)), screen, Screen.h - 80, Color.GREEN); // Selection
		Font.drawCentered(SkinDisplay.shortNameIfLong(selectedUp), screen, Screen.h - 90, Color.GRAY); // Other unselected space
		Font.drawCentered("Use "+ Game.input.getMapping("cursor-down") + ", " + Game.input.getMapping("cursor-up") + ", " + Game.input.getMapping("SELECT"), screen, Screen.h - 11, Color.get(0, 222, 222, 222)); // Controls

		int h = 2;
		int w = 15;
		int xo = (Screen.w - w * 8) / 2;
		int yo = 28;


		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				screen.render(xo + x * 8, yo + y * 8, x + y * 32, 0, 3); // Texture pack logo
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