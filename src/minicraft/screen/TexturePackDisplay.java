package minicraft.screen;

import minicraft.core.FileHandler;
import minicraft.core.Game;
import minicraft.core.io.InputHandler;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteSheet;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class TexturePackDisplay extends Display {

	private static final String DEFAULT_TEXTURE_PACK = "Default"; // Default texture :)
	private static final String[] ENTRY_NAMES = new String[] { "items.png", "tiles.png", "entities.png", "gui.png" }; // Spritesheets

	private final List<String> textureList;
	private final File location;

	private int selected;
	private boolean shouldUpdate;

	// The texture packs are put in a folder generated by the game called "Textures packs"
	// Many texture packs can be put according to the number of files
	
	public TexturePackDisplay() {
		this.textureList = new ArrayList<>();
		this.textureList.add(TexturePackDisplay.DEFAULT_TEXTURE_PACK);

		this.location = new File(FileHandler.getSystemGameDir() + "/" + FileHandler.getLocalGameDir() + "/TexturePacks");
		this.location.mkdirs();

		for (String fileName : Objects.requireNonNull(location.list())) {
			if (fileName.endsWith(".zip")) { // only .zip files ok?
				textureList.add(fileName);
			}
		}
	}

	private void updateSpriteSheet(Screen screen) throws IOException {
		SpriteSheet[] sheets = new SpriteSheet[TexturePackDisplay.ENTRY_NAMES.length];

		if (selected == 0) {
			sheets[0] = new SpriteSheet(ImageIO.read(Game.class.getResourceAsStream("/resources/textures/items.png")));
			sheets[1] = new SpriteSheet(ImageIO.read(Game.class.getResourceAsStream("/resources/textures/tiles.png")));
			sheets[2] = new SpriteSheet(ImageIO.read(Game.class.getResourceAsStream("/resources/textures/entities.png")));
			sheets[3] = new SpriteSheet(ImageIO.read(Game.class.getResourceAsStream("/resources/textures/gui.png")));
		} else {
			try (ZipFile zipFile = new ZipFile(new File(location, textureList.get(selected)))) {
				for (int i = 0; i < TexturePackDisplay.ENTRY_NAMES.length; i++) {
					ZipEntry entry = zipFile.getEntry(TexturePackDisplay.ENTRY_NAMES[i]);
					if (entry != null) {
						try (InputStream inputEntry = zipFile.getInputStream(entry)) {
							sheets[i] = new SpriteSheet(ImageIO.read(inputEntry));
						}
					}
				}
			}
		}

		screen.setSheet(sheets[0], sheets[1], sheets[2], sheets[3]);
	}

	@Override
	public void tick(InputHandler input) {
		if (input.getKey("menu").clicked || input.getKey("attack").clicked || input.getKey("exit").clicked) {
			Game.exitMenu();
			return;
		}

		if (input.getKey("MOVE-DOWN").clicked && selected > 0) {
			selected--;
			shouldUpdate = true;
		}
		if (input.getKey("MOVE-UP").clicked && selected < textureList.size() - 1) {
			selected++;
			shouldUpdate = true;
		}
	}

	private static String shortNameIfLong(String name) {
		return name.length() > 20 ? name.substring(0, 16) + "..." : name;
	}

	@Override
	public void render(Screen screen) {
		screen.clear(0);

		if (shouldUpdate) {
			shouldUpdate = false;

			try {
				updateSpriteSheet(screen);
			} catch (IOException exception) {
				exception.printStackTrace();
			}
		}

		String selectedUp = selected + 1 > textureList.size() - 1 ? "" : textureList.get(selected + 1);
		String selectedDown = selected - 1 < 0 ? "" : textureList.get(selected - 1);

		Font.drawCentered("Texture Packs", screen, Screen.h - 180, Color.get(0, 555, 555, 555));
		Font.drawCentered(TexturePackDisplay.shortNameIfLong(selectedDown), screen, Screen.h - 72, Color.get(0, 222, 222, 222));
		Font.drawCentered(TexturePackDisplay.shortNameIfLong(textureList.get(selected)), screen, Screen.h - 80, Color.get(0, 555, 555, 555));
		Font.drawCentered(TexturePackDisplay.shortNameIfLong(selectedUp), screen, Screen.h - 90, Color.get(0, 222, 222, 222));
		Font.drawCentered("Arrows keys "+ Game.input.getMapping("MOVE-DOWN") + ", " + Game.input.getMapping("MOVE-UP"), screen, Screen.h - 11, Color.get(0, 222, 222, 222));

		int h = 2;
		int w = 15;
		int xo = (Screen.w - w * 8) / 2;
		int yo = 28;
		
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				screen.render(xo + x * 8, yo + y * 8, x + y * 32, 0, 3); // texture pack logo
			}
		}

		/* Unnecessary for now...
		screen.render(24, 48, 134, Color.get(-1, 110, 330, 550), 0);
		screen.render(48, 48, 136, Color.get(-1, 110, 330, 550), 0);
		screen.render(72, 48, 164, Color.get(-1, 100, 321, 45), 0);
		screen.render(96, 48, 384, Color.get(0, 200, 500, 533), 0);
		screen.render(120, 48, 135, Color.get(-1, 100, 320, 430), 0);
		*/
	}

}