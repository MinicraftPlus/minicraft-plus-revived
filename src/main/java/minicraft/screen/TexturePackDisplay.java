package minicraft.screen;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;

import minicraft.core.FileHandler;
import minicraft.core.Game;
import minicraft.core.Renderer;
import minicraft.core.io.Localization;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteSheet;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.SelectEntry;
import org.tinylog.Logger;

public class TexturePackDisplay extends Display {

	private static final String DEFAULT_TEXTURE_PACK = "Default"; // Default texture
	private static final String[] ENTRY_NAMES = new String[] { "items.png", "tiles.png", "entities.png", "gui.png" }; // Spritesheets

	private static final File location = new File(FileHandler.getSystemGameDir() + "/" + FileHandler.getLocalGameDir() + "/TexturePacks");

	private static List<ListEntry> getAllTexturePacks() {
		List<ListEntry> textureList = new ArrayList<>();
		textureList.add(new SelectEntry(TexturePackDisplay.DEFAULT_TEXTURE_PACK, Game::exitMenu, false));

		// Generate texture packs folder
		if (location.mkdirs()) {
			Logger.info("Created texture packs folder at {}.", location);
		}

		// Read and add the .zip file to the texture pack list.
		for (String fileName : Objects.requireNonNull(location.list())) {
			// Only accept files ending with .zip.
			if (fileName.endsWith(".zip")) {
				textureList.add(new SelectEntry(fileName, Game::exitMenu, false));
			}
		}

		return textureList;
	}
	
	public TexturePackDisplay() {
		super(true, true,
				new Menu.Builder(false, 2, RelPos.CENTER, getAllTexturePacks()).setSize(48, 64).createMenu());
	}

	@Override
	public void onExit() {
		super.onExit();
		getAllTexturePacks();
		updateSheets();
	}

	private void updateSheets() {
		try {
			SpriteSheet[] sheets = new SpriteSheet[TexturePackDisplay.ENTRY_NAMES.length];

			if (menus[0].getSelection() == 0) {
				// Load default sprite sheet.
				sheets = Renderer.loadDefaultSpriteSheets();
			} else {
				try {
					ZipFile zipFile = new ZipFile(new File(location, Objects.requireNonNull(menus[0].getCurEntry()).toString()));

					HashMap<String, HashMap<String, ZipEntry>> resources = generateResourceTree(zipFile); 

					// Load textures
					HashMap<String, ZipEntry> textures = resources.get("textures");
					for (int i = 0; i < TexturePackDisplay.ENTRY_NAMES.length; i++) {
						ZipEntry entry = textures.get(TexturePackDisplay.ENTRY_NAMES[i]);
						if (entry != null) {
							try (InputStream inputEntry = zipFile.getInputStream(entry)) {
								sheets[i] = new SpriteSheet(ImageIO.read(inputEntry));
							} catch (IOException e) {
								e.printStackTrace();
								Logger.error("Loading sheet {} failed. Aborting.", TexturePackDisplay.ENTRY_NAMES[i]);
								return;
							}
						} else {
							Logger.debug("Couldn't load sheet {}, ignoring.", TexturePackDisplay.ENTRY_NAMES[i]);
						}
					}
					// TODO: Extend this to apply to sound (Using resources.get("sound"))
				} catch (IllegalStateException | IOException e) {
					e.printStackTrace();
					Logger.error("Could not load texture pack with name {} at {}.", Objects.requireNonNull(menus[0].getCurEntry()).toString(), location);
					return;
				} catch (NullPointerException e) {
					e.printStackTrace();
					return;
				}
			}

			Renderer.screen.setSheets(sheets[0], sheets[1], sheets[2], sheets[3]);
		} catch(NullPointerException e) {
			e.printStackTrace();
			Logger.error("Changing texture pack failed.");
			return;
		}

		Logger.info("Changed texture pack.");
	}


	/*
		Fill hashmap with folder > list of files structure

		Example Folder Structure:
		textures
			items.png
			gui.png
			entities.png
			tiles.png
		localization
			english_en-us.json
		sound
			bossdeath.wav
		
		Gets converted into a HashMap of HashMaps that look the same
		
		Example getter:
		resources.get("textures").get("tiles.png")
	*/
	public static HashMap<String, HashMap<String, ZipEntry>> generateResourceTree(ZipFile zipFile){
		HashMap<String, HashMap<String, ZipEntry>> resources = new HashMap<>(); 

		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		while(entries.hasMoreElements()){
			ZipEntry entry = entries.nextElement();

			Pattern pattern = Pattern.compile("/(.*?)/");
			Matcher matcher = pattern.matcher(entry.getName());
			if (!matcher.find()) {
				continue;
			}
			if(entry.isDirectory()) {
				resources.put(matcher.group(1), new HashMap<>());
			} else {
				HashMap<String, ZipEntry> directory = resources.get(matcher.group(1));

				if(directory == null) {
					continue;
				}

				String[] validSuffixes = {".json", ".wav", ".png"};
				for(String suffix: validSuffixes) {
					if(entry.getName().endsWith(suffix)) {
						String[] path = entry.getName().split("/");
						String fileName = path[path.length - 1];
						directory.put(fileName, entry);
					}
				}
			}
		}

		return resources;
	}

	@Override
	public void render(Screen screen) {
		super.render(screen);

		// Title
		Font.drawCentered(Localization.getLocalized("Texture Packs"), screen, Screen.h - 180, Color.WHITE);

		// Info text at the bottom.
		Font.drawCentered("Use "+ Game.input.getMapping("cursor-down") + " and " + Game.input.getMapping("cursor-up") + " to move.", screen, Screen.h - 17, Color.DARK_GRAY);
		Font.drawCentered(Game.input.getMapping("SELECT") + " to select.", screen, Screen.h - 9, Color.DARK_GRAY);

		/* Does not work as intended because the sprite sheets aren't updated when changing selection.
		int h = 2;
		int w = 15;
		int xo = (Screen.w - w * 8) / 2;
		int yo = 28;

		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				// Texture pack logo
				screen.render(xo + x * 8, yo + y * 8, x + y * 32, 0, 3);
			}
		}
		 */

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

	public static File getLocation() {
		return location;
	}
}
