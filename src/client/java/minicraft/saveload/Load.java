package minicraft.saveload;

import minicraft.core.Game;
import minicraft.core.Updater;
import minicraft.core.World;
import minicraft.core.io.FileHandler;
import minicraft.core.io.Localization;
import minicraft.core.io.Settings;
import minicraft.entity.Arrow;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.FireSpark;
import minicraft.entity.ItemEntity;
import minicraft.entity.Spark;
import minicraft.entity.furniture.Bed;
import minicraft.entity.furniture.Chest;
import minicraft.entity.furniture.Crafter;
import minicraft.entity.furniture.DeathChest;
import minicraft.entity.furniture.DungeonChest;
import minicraft.entity.furniture.KnightStatue;
import minicraft.entity.furniture.Lantern;
import minicraft.entity.furniture.Spawner;
import minicraft.entity.furniture.Tnt;
import minicraft.entity.mob.AirWizard;
import minicraft.entity.mob.Cow;
import minicraft.entity.mob.Creeper;
import minicraft.entity.mob.EnemyMob;
import minicraft.entity.mob.Knight;
import minicraft.entity.mob.Mob;
import minicraft.entity.mob.MobAi;
import minicraft.entity.mob.ObsidianKnight;
import minicraft.entity.mob.Pig;
import minicraft.entity.mob.Player;
import minicraft.entity.mob.Sheep;
import minicraft.entity.mob.Skeleton;
import minicraft.entity.mob.Slime;
import minicraft.entity.mob.Snake;
import minicraft.entity.mob.Zombie;
import minicraft.entity.particle.FireParticle;
import minicraft.entity.particle.SmashParticle;
import minicraft.entity.particle.TextParticle;
import minicraft.entity.vehicle.Boat;
import minicraft.gfx.Color;
import minicraft.gfx.Point;
import minicraft.item.ArmorItem;
import minicraft.item.DyeItem;
import minicraft.item.Inventory;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.PotionItem;
import minicraft.item.PotionType;
import minicraft.item.Recipe;
import minicraft.item.StackableItem;
import minicraft.level.ChunkManager;
import minicraft.level.Level;
import minicraft.level.tile.Tiles;
import minicraft.network.Network;
import minicraft.screen.AchievementsDisplay;
import minicraft.screen.CraftingDisplay;
import minicraft.screen.LoadingDisplay;
import minicraft.screen.MultiplayerDisplay;
import minicraft.screen.PopupDisplay;
import minicraft.screen.QuestsDisplay;
import minicraft.screen.ResourcePackDisplay;
import minicraft.screen.SignDisplay;
import minicraft.screen.SkinDisplay;
import minicraft.screen.TutorialDisplayHandler;
import minicraft.screen.WorldSelectDisplay;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.StringEntry;
import minicraft.util.AdvancementElement;
import minicraft.util.Logging;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Load {

	private String location = Game.gameDir;

	private static final String extension = Save.extension;
	private float percentInc;

	private ArrayList<String> data;
	private ArrayList<String> extradata; // These two are changed when loading a new file. (see loadFromFile())

	private Version worldVer;

	private DeathChest deathChest;

	{
		worldVer = null;

		data = new ArrayList<>();
		extradata = new ArrayList<>();
	}

	public Load(String worldname) {
		this(worldname, true);
	}

	public Load(String worldname, boolean loadGame) {
		loadFromFile(location + "/saves/" + worldname + "/Game" + extension);
		if (data.get(0).contains(".")) worldVer = new Version(data.get(0));
		if (worldVer == null) worldVer = new Version("1.8");

		//if (!hasGlobalPrefs)
		//	hasGlobalPrefs = worldVer.compareTo(new Version("1.9.2")) >= 0;

		if (!loadGame) return;

		if (Game.VERSION.isSpecial() && !worldVer.isSpecial()) {
			Logging.SAVELOAD.info("Old finite world detected, backup prompting...");
			ArrayList<ListEntry> entries = new ArrayList<>();
			entries.addAll(Arrays.asList(StringEntry.useLines(Color.WHITE, false,
				Localization.getLocalized("minicraft.displays.save.popup_display.finite_world_backup_prompt.msg",
					worldVer, Game.VERSION))));
			entries.addAll(Arrays.asList(StringEntry.useLines("minicraft.display.popup.escape_cancel")));

			AtomicBoolean acted = new AtomicBoolean(false);
			AtomicBoolean continues = new AtomicBoolean(false);
			AtomicBoolean doBackup = new AtomicBoolean(false);

			ArrayList<PopupDisplay.PopupActionCallback> callbacks = new ArrayList<>();
			callbacks.add(new PopupDisplay.PopupActionCallback("EXIT", m -> {
				Game.exitDisplay();
				acted.set(true);
				return true;
			}));

			callbacks.add(new PopupDisplay.PopupActionCallback("ENTER|Y", m -> {
				Game.exitDisplay();
				continues.set(true);
				doBackup.set(true);
				acted.set(true);
				return true;
			}));

			callbacks.add(new PopupDisplay.PopupActionCallback("N", m -> {
				Game.exitDisplay();
				continues.set(true);
				acted.set(true);
				return true;
			}));

			Game.setDisplay(new PopupDisplay(new PopupDisplay.PopupConfig(
				"minicraft.displays.save.popup_display.world_backup_prompt", callbacks, 2),
				entries.toArray(new ListEntry[0])));

			while (true) {
				if (acted.get()) {
					if (continues.get()) {
						if (doBackup.get()) {
							Logging.SAVELOAD.info("Performing world backup...");
							int i = 0;
							String filename = worldname;
							File f = new File(location + "/saves/", filename);
							while (f.exists()) { // Increments world name if world exists
								i++;
								filename = worldname + " (" + i + ")";
								f = new File(location + "/saves/", filename);
							}
							f.mkdirs();
							try {
								FileHandler.copyFolderContents(Paths.get(location, "saves", worldname),
									f.toPath(), FileHandler.SKIP, false);
							} catch (IOException e) {
								Logging.SAVELOAD.error(e, "Error occurs while performing world backup, loading aborted");
								throw new RuntimeException(new InterruptedException("World loading interrupted."));
							}

							Logging.SAVELOAD.info("World backup \"{}\" is created.", filename);
							WorldSelectDisplay.updateWorlds();
						} else
							Logging.SAVELOAD.warn("World backup is skipped.");
						Logging.SAVELOAD.debug("World loading continues...");
					} else {
						Logging.SAVELOAD.info("User cancelled world loading, loading aborted.");
						throw new RuntimeException(new InterruptedException("World loading interrupted."));
					}

					break;
				}

				try {
					//noinspection BusyWait
					Thread.sleep(10);
				} catch (InterruptedException ignored) {}
			}
		}

		// Is dev build and if both versions are special or not
		if (Game.VERSION.isDev() && worldVer.compareTo(Game.VERSION) < 0 && (Game.VERSION.isSpecial() == worldVer.isSpecial())) {
			Logging.SAVELOAD.info("Old world detected, backup prompting...");
			ArrayList<ListEntry> entries = new ArrayList<>();
			entries.addAll(Arrays.asList(StringEntry.useLines(Color.WHITE, false,
				Localization.getLocalized("minicraft.displays.save.popup_display.world_backup_prompt.msg",
					worldVer, Game.VERSION))));
			entries.addAll(Arrays.asList(StringEntry.useLines("minicraft.display.popup.escape_cancel")));

			AtomicBoolean acted = new AtomicBoolean(false);
			AtomicBoolean continues = new AtomicBoolean(false);
			AtomicBoolean doBackup = new AtomicBoolean(false);

			ArrayList<PopupDisplay.PopupActionCallback> callbacks = new ArrayList<>();
			callbacks.add(new PopupDisplay.PopupActionCallback("EXIT", m -> {
				Game.exitDisplay();
				acted.set(true);
				return true;
			}));

			callbacks.add(new PopupDisplay.PopupActionCallback("ENTER|Y", m -> {
				Game.exitDisplay();
				continues.set(true);
				doBackup.set(true);
				acted.set(true);
				return true;
			}));

			callbacks.add(new PopupDisplay.PopupActionCallback("N", m -> {
				Game.exitDisplay();
				continues.set(true);
				acted.set(true);
				return true;
			}));

			Game.setDisplay(new PopupDisplay(new PopupDisplay.PopupConfig(
					"minicraft.displays.save.popup_display.world_backup_prompt", callbacks, 2),
					entries.toArray(new ListEntry[0])));

			while (true) {
				if (acted.get()) {
					if (continues.get()) {
						if (doBackup.get()) {
							Logging.SAVELOAD.info("Performing world backup...");
							int i = 0;
							String filename = worldname;
							File f = new File(location + "/saves/", filename);
							while (f.exists()) { // Increments world name if world exists
								i++;
								filename = worldname + " (" + i + ")";
								f = new File(location + "/saves/", filename);
							}
							f.mkdirs();
							try {
								FileHandler.copyFolderContents(Paths.get(location, "saves", worldname),
									f.toPath(), FileHandler.SKIP, false);
							} catch (IOException e) {
								Logging.SAVELOAD.error(e, "Error occurs while performing world backup, loading aborted");
								throw new RuntimeException(new InterruptedException("World loading interrupted."));
							}

							Logging.SAVELOAD.info("World backup \"{}\" is created.", filename);
							WorldSelectDisplay.updateWorlds();
						} else
							Logging.SAVELOAD.warn("World backup is skipped.");
						Logging.SAVELOAD.debug("World loading continues...");
					} else {
						Logging.SAVELOAD.info("User cancelled world loading, loading aborted.");
						throw new RuntimeException(new InterruptedException("World loading interrupted."));
					}

					break;
				}

				try {
					//noinspection BusyWait
					Thread.sleep(10);
				} catch (InterruptedException ignored) {}
			}
		}

		if (worldVer.compareTo(new Version("1.9.2")) < 0)
			new LegacyLoad(worldname);
		else {
			location += "/saves/" + worldname + "/";

			percentInc = 5 + World.levels.length - 1; // For the methods below, and world.

			percentInc = 100f / percentInc;

			LoadingDisplay.setPercentage(0);
			loadGame("Game"); // More of the version will be determined here
			if(worldVer.compareTo(new Version("2.3.0-dev2")) < 0)
				loadWorld("Level");
			else
				loadWorldInf("Level");
			loadEntities("Entities");
			loadInventory("Inventory", Game.player.getInventory());
			loadPlayer("Player", Game.player);

			if (deathChest != null && deathChest.getInventory().invSize() > 0) {
				Game.player.getLevel().add(deathChest, Game.player.x, Game.player.y);
				Logging.SAVELOAD.debug("Added DeathChest which contains exceed items.");
			}

			if (worldVer.compareTo(new Version("2.2.0-dev3")) < 0) {
				Logging.SAVELOAD.trace("Old version dungeon detected.");
				ArrayList<ListEntry> entries = new ArrayList<>();
				entries.addAll(Arrays.asList(StringEntry.useLines(Color.RED,
					Localization.getLocalized("minicraft.displays.loading.regeneration_popup.display.0"),
					Localization.getLocalized("minicraft.displays.loading.regeneration_popup.display.1"),
					Localization.getLocalized("minicraft.displays.loading.regeneration_popup.display.2")
				)));

				entries.addAll(Arrays.asList(StringEntry.useLines(Color.WHITE, "",
					Localization.getLocalized("minicraft.displays.loading.regeneration_popup.display.3", Game.input.getMapping("select")),
					Localization.getLocalized("minicraft.displays.loading.regeneration_popup.display.4", Game.input.getMapping("exit"))
				)));

				AtomicBoolean acted = new AtomicBoolean(false);
				AtomicBoolean continues = new AtomicBoolean(false);

				ArrayList<PopupDisplay.PopupActionCallback> callbacks = new ArrayList<>();
				callbacks.add(new PopupDisplay.PopupActionCallback("select", popup -> {
					Game.exitDisplay();
					acted.set(true);
					continues.set(true);
					return true;
				}));

				callbacks.add(new PopupDisplay.PopupActionCallback("exit", popup -> {
					Game.exitDisplay();
					acted.set(true);
					return true;
				}));

				Game.setDisplay(new PopupDisplay(new PopupDisplay.PopupConfig(null, callbacks, 0), entries.toArray(new ListEntry[0])));

				while (true) {
					if (acted.get()) {
						if (continues.get()) {
							Logging.SAVELOAD.trace("Regenerating dungeon (B4)...");
							LoadingDisplay.setMessage("minicraft.displays.loading.message.dungeon_regeneration");
							int lvlidx = World.lvlIdx(-4);
							boolean reAdd = Game.player.getLevel().depth == -4;
							Level oriLevel = World.levels[lvlidx];
							World.levels[lvlidx] = new Level(oriLevel.w, oriLevel.h, oriLevel.getSeed(), -4, World.levels[World.lvlIdx(-3)], true);
							if (reAdd) World.levels[lvlidx].add(Game.player);
						} else {
							throw new RuntimeException(new InterruptedException("World loading interrupted."));
						}

						break;
					}

					try {
						//noinspection BusyWait
						Thread.sleep(10);
					} catch (InterruptedException ignored) {}
				}
			}
		}
	}

	public Load() {
		this(Game.VERSION);
	}

	public Load(Version worldVersion) {
		this(false, false);
		worldVer = worldVersion;
	}

	public Load(boolean loadConfig, boolean partialLoad) {
		if (!loadConfig) return;
		boolean resave = false;


		location += "/";

		// Check if Preferences.json exists. (new version)
		if (new File(location + "Preferences.json").exists()) {
			loadPrefs("Preferences", partialLoad);

			// Check if Preferences.miniplussave exists. (old version)
		} else if (new File(location + "Preferences" + extension).exists()) {
			loadPrefsOld("Preferences", partialLoad);
			Logging.SAVELOAD.info("Upgrading preferences to JSON.");
			resave = true;

			// No preferences file found.
		} else {
			Logging.SAVELOAD.warn("No preferences found, creating new file.");
			resave = true;
		}

		if (partialLoad) return; // Partial loading only loads partial preferences

		// Load unlocks. (new version)
		File testFileOld = new File(location + "unlocks" + extension);
		File testFile = new File(location + "Unlocks" + extension);
		if (new File(location + "Unlocks.json").exists()) {
			loadUnlocks("Unlocks");
		} else if (testFile.exists() || testFileOld.exists()) { // Load old version
			if (testFileOld.exists() && !testFile.exists()) {
				if (testFileOld.renameTo(testFile)) {
					new LegacyLoad(testFile);
				} else {
					Logging.SAVELOAD.info("Failed to rename unlocks to Unlocks; loading old version.");
					new LegacyLoad(testFileOld);
				}
			}

			loadUnlocksOld("Unlocks");
			resave = true;
			Logging.SAVELOAD.info("Upgrading unlocks to JSON.");
		} else {
			Logging.SAVELOAD.warn("No unlocks found, creating new file.");
			resave = true;
		}

		// We need to load everything before we save, so it doesn't overwrite anything.
		if (resave) {
			new Save();
		}
	}

	public Version getWorldVersion() {
		return worldVer;
	}

	public static ArrayList<String> loadFile(String filename) throws IOException {
		ArrayList<String> lines = new ArrayList<>();

		InputStream fileStream = Load.class.getResourceAsStream(filename);

		try (BufferedReader br = new BufferedReader(new InputStreamReader(fileStream))) {

			String line;
			while ((line = br.readLine()) != null)
				lines.add(line);

		}

		return lines;
	}

	private void loadFromFile(String filename) {
		data.clear();
		extradata.clear();

		String total;
		try {
			total = loadFromFile(filename, true);
			if (total.length() > 0) { // Safe splitting with JSON styled element.
				data.addAll(splitUnwrappedCommas(total));
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		if (filename.contains("Level")) {
			try {
				total = Load.loadFromFile(filename.substring(0, filename.lastIndexOf("/") + 7) + "data" + extension, true);
				extradata.addAll(Arrays.asList(total.split(",")));
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		LoadingDisplay.progress(percentInc);
	}

	private void loadFromFile(String filename, List<String> data) {
		data.clear();

		String total;
		try {
			total = loadFromFile(filename, true);
			if (total.length() > 0) { // Safe splitting with JSON styled element.
				data.addAll(splitUnwrappedCommas(total));
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		LoadingDisplay.progress(percentInc);
	}

	/**
	 * Source: Note: This method is copied from MiniMods.
	 */
	private static ArrayList<String> splitUnwrappedCommas(String input) {
		ArrayList<String> out = new ArrayList<>();
		int lastIdx = 0;
		// 0 {}; 1 []; 2 ()
		Stack<Integer> bracketCounter = new Stack<>();
		char openBracket0 = '{';
		char openBracket1 = '[';
		char openBracket2 = '(';
		char closeBracket0 = '}';
		char closeBracket1 = ']';
		char closeBracket2 = ')';
		char commaChar = ',';
		Predicate<Integer> checkDiff = ch -> {
			if (bracketCounter.isEmpty()) return true;
			return !bracketCounter.peek().equals(ch);
		};

		for (int i = 0; i < input.length(); i++) {
			char ch = input.charAt(i);
			if (ch == commaChar && bracketCounter.isEmpty()) {
				String str = input.substring(lastIdx + (input.charAt(lastIdx) == commaChar ? 1 : 0), i).trim();
				lastIdx = i;
				out.add(str); // Empty strings are expected.
			} else if (ch == openBracket0) {
				bracketCounter.push(0);
			} else if (ch == closeBracket0) {
				if (checkDiff.test(0))
					throw new RuntimeException(String.format("Invalid closing char %s index %s. Input: \"%s\"", ch, i, input));
				bracketCounter.pop();
			} else if (ch == openBracket1) {
				bracketCounter.push(1);
			} else if (ch == closeBracket1) {
				if (checkDiff.test(1))
					throw new RuntimeException(String.format("Invalid closing char %s index %s. Input: \"%s\"", ch, i, input));
				bracketCounter.pop();
			} else if (ch == openBracket2) {
				bracketCounter.push(2);
			} else if (ch == closeBracket2) {
				if (checkDiff.test(2))
					throw new RuntimeException(String.format("Invalid closing char %s index %s. Input: \"%s\"", ch, i, input));
				bracketCounter.pop();
			}
		}

		String str = input.substring(lastIdx).trim();
		if (!str.isEmpty() && !str.chars().allMatch(Character::isWhitespace) &&
			!(str.length() == 1 && str.charAt(0) == commaChar)) {
			out.add(str);
		}

		if (bracketCounter.size() > 0)
			throw new RuntimeException(String.format("Bracket not closed. Input: \"%s\"", input));

		return out;
	}

	public static String loadFromFile(String filename, boolean isWorldSave) throws IOException {
		StringBuilder total = new StringBuilder();

		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
			String curLine;
			while ((curLine = br.readLine()) != null)
				total.append(curLine).append(isWorldSave ? "" : "\n");
		}

		return total.toString();
	}

	private void loadGame(String filename) {
		loadFromFile(location + filename + extension);

		worldVer = new Version(data.remove(0)); // Gets the world version

		if (worldVer.compareTo(new Version("2.2.0-dev1")) >= 0)
			World.setWorldSeed(Long.parseLong(data.remove(0)));
		else
			World.setWorldSeed(0);

		if (worldVer.compareTo(new Version("2.0.4-dev8")) >= 0)
			loadMode(data.remove(0));

		Updater.setTime(Integer.parseInt(data.remove(0)));

		Updater.gameTime = Integer.parseInt(data.remove(0));
		if (worldVer.compareTo(new Version("1.9.3-dev2")) >= 0) {
			Updater.pastDay1 = Updater.gameTime > 65000;
		} else {
			Updater.gameTime = 65000; // Prevents time cheating.
		}

		int diffIdx = Integer.parseInt(data.remove(0));
		if (worldVer.compareTo(new Version("1.9.3-dev3")) < 0)
			diffIdx--; // Account for change in difficulty

		Settings.setIdx("diff", diffIdx);

		AirWizard.beaten = Boolean.parseBoolean(data.remove(0));

		// Check if the AirWizard was beaten in versions prior to 2.1.0
		if (worldVer.compareTo(new Version("2.1.0-dev2")) < 0) {
			if (AirWizard.beaten) {
				Logging.SAVELOAD.debug("AirWizard was beaten in an old version, giving achievement...");
				AchievementsDisplay.setAchievement("minicraft.achievement.airwizard", true);
			}
		}

		if (worldVer.compareTo(new Version("2.2.0-dev1")) >= 0) {
			Settings.set("quests", Boolean.parseBoolean(data.remove(0)));
			Settings.set("tutorials", Boolean.parseBoolean(data.remove(0)));
		}
	}

	private void loadMode(String modedata) {
		int mode;
		if (modedata.contains(";")) {
			String[] modeinfo = modedata.split(";");
			mode = Integer.parseInt(modeinfo[0]);
			if (worldVer.compareTo(new Version("2.0.3")) <= 0)
				mode--; // We changed the min mode idx from 1 to 0.
			if (mode == 3) {
				Updater.scoreTime = Integer.parseInt(modeinfo[1]);
				if (worldVer.compareTo(new Version("1.9.4")) >= 0)
					Settings.set("scoretime", modeinfo[2]);
			}
		} else {
			mode = Integer.parseInt(modedata);
			if (worldVer.compareTo(new Version("2.0.3")) <= 0)
				mode--; // We changed the min mode idx from 1 to 0.

			if (mode == 3) Updater.scoreTime = 300;
		}

		Settings.setIdx("mode", mode);
	}

	private void loadPrefsOld(String filename, boolean partialLoad) {
		loadFromFile(location + filename + extension);
		Version prefVer = new Version("2.0.2"); // the default, b/c this doesn't really matter much being specific past this if it's not set below.

		if (!data.get(2).contains(";")) // signifies that this file was last written to by a version after 2.0.2.
			prefVer = new Version(data.remove(0));

		Settings.set("sound", Boolean.parseBoolean(data.remove(0)));
		Settings.set("autosave", Boolean.parseBoolean(data.remove(0)));

		if (prefVer.compareTo(new Version("2.0.4-dev2")) >= 0)
			Settings.set("fps", Integer.parseInt(data.remove(0)));

		if (partialLoad) return; // Partial loading only loads basic settings.

		if (prefVer.compareTo(new Version("2.0.7-dev5")) >= 0) {
			data.remove(0); // Numeral skin indices are replaced.
		}

		List<String> subdata;
		if (prefVer.compareTo(new Version("2.0.3-dev1")) < 0) {
			subdata = data;
		} else {
			MultiplayerDisplay.savedIP = data.remove(0);
			if (prefVer.compareTo(new Version("2.0.3-dev3")) > 0) {
				MultiplayerDisplay.savedUUID = data.remove(0);
				MultiplayerDisplay.savedUsername = data.remove(0);
			}

			// Get legacy language and convert it into the current format.
			if (prefVer.compareTo(new Version("2.0.4-dev3")) >= 0) {
				// Get language and convert into locale.
				String lang;
				switch (data.remove(0)) {
					case "english":
						lang = "en-us";
						break;
					case "french":
						lang = "fr-fr";
						break;
					case "hungarian":
						lang = "hu-hu";
						break;
					case "indonesia":
						lang = "id-id";
						break;
					case "italiano":
						lang = "it-it";
						break;
					case "norwegian":
						lang = "nb-no";
						break;
					case "portugues":
						lang = "pt-pt";
						break;
					case "spanish":
						lang = "es-es";
						break;
					case "turkish":
						lang = "tr-tr";
						break;
					default:
						lang = null;
						break;
				}

				if (lang != null) {
					Localization.changeLanguage(lang);
				}
			}

			String keyData = data.get(0);
			subdata = Arrays.asList(keyData.split(":"));
		}

		for (String keymap : subdata) {
			String[] map = keymap.split(";");
			Game.input.setKey(map[0], map[1]);
		}
	}

	private void loadPrefs(String filename, boolean partialLoad) {
		JSONObject json;
		try {
			json = new JSONObject(loadFromFile(location + filename + ".json", false));
		} catch (JSONException | IOException ex) {
			ex.printStackTrace();
			return;
		}

		/* Start of the parsing */
		Version prefVer = new Version(json.getString("version"));

		// Settings
		Settings.set("sound", json.getBoolean("sound"));
		Settings.set("autosave", json.getBoolean("autosave"));
		Settings.set("fps", json.getInt("fps"));
		Settings.set("showquests", json.optBoolean("showquests", true));
		if (json.has("hwa")) Settings.set("hwa", json.getBoolean("hwa")); // Default should have been configured

		if (partialLoad) return; // Partial loading only loads basic settings.

		if (json.has("lang")) {
			String lang = json.getString("lang");
			Localization.changeLanguage(lang);
		}

		SkinDisplay.setSelectedSkin(json.optString("skin", "minicraft.skin.paul"));
		SkinDisplay.releaseSkins();

		// Load keymap
		JSONArray keyData = json.getJSONArray("keymap");
		List<Object> subdata = keyData.toList();

		for (Object key : subdata) {
			String str = key.toString();

			// Split key and value
			String[] map = str.split(";");
			Game.input.setKey(map[0], map[1]);
		}

		JSONArray packsJSON = json.optJSONArray("resourcePacks");
		if (packsJSON != null) {
			String[] packs = new String[packsJSON.length()];
			for (int i = 0; i < packs.length; i++) {
				packs[i] = packsJSON.getString(i);
			}

			ResourcePackDisplay.loadResourcePacks(packs);
			ResourcePackDisplay.reloadResources();
		}

		ResourcePackDisplay.releaseUnloadedPacks();
	}

	private void loadUnlocksOld(String filename) {
		loadFromFile(location + filename + extension);

		for (String unlock : data) {
			unlock = unlock.replace("HOURMODE", "H_ScoreTime").replace("MINUTEMODE", "M_ScoreTime").replace("M_ScoreTime", "_ScoreTime").replace("2H_ScoreTime", "120_ScoreTime");

			if (unlock.contains("_ScoreTime"))
				Settings.getEntry("scoretime").setValueVisibility(Integer.parseInt(unlock.substring(0, unlock.indexOf("_"))), true);
		}
	}

	private void loadUnlocks(String filename) {
		JSONObject json;
		try {
			json = new JSONObject(loadFromFile(location + filename + ".json", false));
		} catch (JSONException | IOException ex) {
			ex.printStackTrace();
			return;
		}

		for (Object i : json.getJSONArray("visibleScoreTimes")) {
			Settings.getEntry("scoretime").setValueVisibility(i, true); // Minutes
		}

		// Load unlocked achievements.
		if (json.has("unlockedAchievements"))
			AchievementsDisplay.unlockAchievements(json.getJSONArray("unlockedAchievements"));
	}

	private void loadWorld(String filename) {
		for (int l = World.maxLevelDepth; l >= World.minLevelDepth; l--) {
			LoadingDisplay.setMessage(Level.getDepthString(l), false);
			int lvlidx = World.lvlIdx(l);
			loadFromFile(location + filename + lvlidx + extension);

			int lvlw = Integer.parseInt(data.get(0));
			int lvlh = Integer.parseInt(data.get(1));

			boolean hasSeed = worldVer.compareTo(new Version("2.0.7-dev2")) >= 0;
			long seed = hasSeed ? Long.parseLong(data.get(2)) : 0;
			Settings.set("size", lvlw);

			ChunkManager map = new ChunkManager();

			for (int x = 0; x < lvlw; x++) {
				for (int y = 0; y < lvlh; y++) {
					int tileArrIdx = y + x * lvlw;
					int tileidx = y + x * lvlh; // the tiles are saved with x outer loop, and y inner loop, meaning that the list reads down, then right one, rather than right, then down one.
					String tilename = data.get(tileidx + (hasSeed ? 4 : 3));
					if (worldVer.compareTo(new Version("1.9.4-dev6")) < 0) {
						int tileID = Integer.parseInt(tilename); // they were id numbers, not names, at this point
						if (Tiles.oldids.get(tileID) != null)
							tilename = Tiles.oldids.get(tileID);
						else {
							Logging.SAVELOAD.warn("Tile list doesn't contain tile " + tileID);
							tilename = "grass";
						}
					}

					if (tilename.equalsIgnoreCase("Wool")) {
						if (worldVer.compareTo(new Version("2.0.6-dev4")) < 0) {
							switch (Integer.parseInt(extradata.get(tileidx))) {
								case 1:
									tilename = "Red Wool";
									break;
								case 2:
									tilename = "Yellow Wool";
									break;
								case 3:
									tilename = "Green Wool";
									break;
								case 4:
									tilename = "Blue Wool";
									break;
								case 5:
									tilename = "Black Wool";
									break;
								default:
									tilename = "White Wool";
							}
						} else if (worldVer.compareTo(new Version("2.3.0-dev1")) < 0) {
							tilename = "White Wool";
						}
					} else if (l == World.minLevelDepth + 1 && tilename.equalsIgnoreCase("Lapis") && worldVer.compareTo(new Version("2.0.3-dev6")) < 0) {
						if (Math.random() < 0.8) // don't replace *all* the lapis
							tilename = "Gem Ore";
					} else if (tilename.equalsIgnoreCase("Cloud Cactus")) {

						// Check the eight tiles around the cloud cactus to see if it is empty.
						for (int yy = y - 1; yy <= y + 1; yy++) {
							for (int xx = x - 1; xx <= x + 1; xx++) {
								if (data.get(xx + yy * lvlw + (hasSeed ? 4 : 3)).equalsIgnoreCase("Infinite Fall")) {
									tilename = "Infinite Fall";
									break;
								}
							}
						}
					}

					// Tiles are read in an ord
					loadTile(worldVer, map, x, y, tilename, extradata.get(tileidx));
				}
			}

			Level parent = World.levels[World.lvlIdx(l + 1)];
			World.levels[lvlidx] = new Level(lvlw, lvlh, seed, l, parent, false);

			Level curLevel = World.levels[lvlidx];
			curLevel.chunkManager = map;

			// Tile initialization
			for (int x = 0; x < curLevel.w; ++x) {
				for (int y = 0; y < curLevel.h; ++y) {
					curLevel.getTile(x, y).onTileSet(curLevel, x, y);
				}
			}
			for(int x = 0; x < curLevel.w / ChunkManager.CHUNK_SIZE; x++)
				for(int y = 0; y < curLevel.h / ChunkManager.CHUNK_SIZE; y++)
					curLevel.chunkManager.setChunkStage(x, y, ChunkManager.CHUNK_STAGE_DONE);

			if (Logging.logLevel) curLevel.printTileLocs(Tiles.get("Stairs Down"));

			if (parent == null) continue;
			/// confirm that there are stairs in all the places that should have stairs.
			for (minicraft.gfx.Point p : parent.getMatchingTiles(Tiles.get("Stairs Down"))) {
				if (curLevel.getTile(p.x, p.y) != Tiles.get("Stairs Up")) {
					curLevel.printLevelLoc("INCONSISTENT STAIRS detected; placing stairsUp", p.x, p.y);
					curLevel.setTile(p.x, p.y, Tiles.get("Stairs Up"));
				}
			}
			for (minicraft.gfx.Point p : curLevel.getMatchingTiles(Tiles.get("Stairs Up"))) {
				if (parent.getTile(p.x, p.y) != Tiles.get("Stairs Down")) {
					parent.printLevelLoc("INCONSISTENT STAIRS detected; placing stairsDown", p.x, p.y);
					parent.setTile(p.x, p.y, Tiles.get("Stairs Down"));
				}
			}
		}

		LoadingDisplay.setMessage("minicraft.displays.loading.message.quests");

		if (new File(location + "Quests.json").exists()) {
			Logging.SAVELOAD.warn("Quest.json exists and it has been deprecated; renaming...");
			try {
				Files.move(Paths.get(location, "Quests.json"), Paths.get(location, "Quests.json_old"), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				Logging.SAVELOAD.warn("Quest.json renamed failed.");
			}
		}

		boolean advancementsLoadSucceeded = false;
		if (new File(location + "advancements.json").exists()) {
			try {
				JSONObject questsObj = new JSONObject(loadFromFile(location + "advancements.json", true));
				@SuppressWarnings("unused")
				Version dataVersion = new Version(questsObj.getString("Version"));
				TutorialDisplayHandler.load(questsObj);
				AdvancementElement.loadRecipeUnlockingElements(questsObj);
				QuestsDisplay.load(questsObj);
				advancementsLoadSucceeded = true;
			} catch (IOException e) {
				Logging.SAVELOAD.error(e, "Unable to load advancements.json, loading default quests instead.");
			}
		} else {
			Logging.SAVELOAD.debug("advancements.json not found, loading default quests instead.");
		}

		if (!advancementsLoadSucceeded) {
			TutorialDisplayHandler.reset(false);
			AdvancementElement.resetRecipeUnlockingElements();
			QuestsDisplay.resetGameQuests();
		}

		boolean signsLoadSucceeded = false;
		if (new File(location + "signs.json").exists()) {
			try {
				JSONObject fileObj = new JSONObject(loadFromFile(location + "signs.json", true));
				@SuppressWarnings("unused")
				Version dataVersion = new Version(fileObj.getString("Version"));
				JSONArray dataObj = fileObj.getJSONArray("signs");
				HashMap<Map.Entry<Integer, Point>, List<String>> signTexts = new HashMap<>();
				for (int i = 0; i < dataObj.length(); i++) {
					JSONObject signObj = dataObj.getJSONObject(i);
					signTexts.put(
						new AbstractMap.SimpleImmutableEntry<>(signObj.getInt("level"), new Point(signObj.getInt("x"), signObj.getInt("y"))),
						signObj.getJSONArray("lines").toList().stream().map(e -> (String) e).collect(Collectors.toList())
					);
				}

				SignDisplay.loadSignTexts(signTexts);
				signsLoadSucceeded = true;
			} catch (IOException e) {
				Logging.SAVELOAD.error(e, "Unable to load signs.json, reset sign data instead.");
			}
		} else {
			Logging.SAVELOAD.debug("signs.json not found, reset sign data instead.");
		}

		if (!signsLoadSucceeded) {
			SignDisplay.resetSignTexts();
		}
	}

	private void loadWorldInf(String filename) {
		loadFromFile(location + "/Game" + extension, extradata);
		long seed = Long.parseLong(extradata.get(1));
		for (int l = World.maxLevelDepth; l >= World.minLevelDepth; l--) {
			LoadingDisplay.setMessage(Level.getDepthString(l), false);
			int lvlidx = World.lvlIdx(l);
			loadFromFile(location + filename + lvlidx + "/index" + extension, data);

			Set<Point> chunks = new HashSet<>();
			while(data.size() >= 2)
				chunks.add(new Point(Integer.parseInt(data.remove(0)), Integer.parseInt(data.remove(0))));

			ChunkManager map = new ChunkManager();
			Level parent = World.levels[World.lvlIdx(l + 1)];
			World.levels[lvlidx] = new Level((int)Settings.get("size"), (int)Settings.get("size"), seed, l, parent, false);

			Level curLevel = World.levels[lvlidx];
			curLevel.chunkManager = map;
			for(Point c : chunks) {
				loadFromFile(location + filename + lvlidx + "/t." + c.x + "." + c.y + extension, data);
				loadFromFile(location + filename + lvlidx + "/d." + c.x + "." + c.y + extension, extradata);
				for(int x = 0; x < ChunkManager.CHUNK_SIZE; x++) {
					for(int y = 0; y < ChunkManager.CHUNK_SIZE; y++) {
						int tileidx = y + x * ChunkManager.CHUNK_SIZE; // the tiles are saved with x outer loop, and y inner loop, meaning that the list reads down, then right one, rather than right, then down one.
						int tX = x + c.x * ChunkManager.CHUNK_SIZE, tY = y + c.y * ChunkManager.CHUNK_SIZE;
						loadTile(worldVer, map, tX, tY, data.get(tileidx), extradata.get(tileidx));
						map.getTile(tX, tY).onTileSet(curLevel, tX, tY);
					}
				}
				map.setChunkStage(c.x, c.y, ChunkManager.CHUNK_STAGE_DONE);
			}

			if (Logging.logLevel) curLevel.printTileLocs(Tiles.get("Stairs Down"));

			if (parent == null) continue;
			/// confirm that there are stairs in all the places that should have stairs.
			// if there isn't, mark the chunk as needing stairs
			for (minicraft.gfx.Point p : parent.getMatchingTiles(Tiles.get("Stairs Down"))) {
				if (curLevel.getTile(p.x, p.y) != Tiles.get("Stairs Up")) {
					curLevel.chunkManager.setChunkStage(p.x / ChunkManager.CHUNK_SIZE, p.y / ChunkManager.CHUNK_SIZE, ChunkManager.CHUNK_STAGE_UNFINISHED_STAIRS);
				}
			}
			for (minicraft.gfx.Point p : curLevel.getMatchingTiles(Tiles.get("Stairs Up"))) {
				if (parent.getTile(p.x, p.y) != Tiles.get("Stairs Down")) {
					parent.chunkManager.setChunkStage(p.x / ChunkManager.CHUNK_SIZE, p.y / ChunkManager.CHUNK_SIZE, ChunkManager.CHUNK_STAGE_UNFINISHED_STAIRS);
				}
			}
		}

		LoadingDisplay.setMessage("minicraft.displays.loading.message.quests");

		if (new File(location + "Quests.json").exists()) {
			Logging.SAVELOAD.warn("Quest.json exists and it has been deprecated; renaming...");
			try {
				Files.move(Paths.get(location, "Quests.json"), Paths.get(location, "Quests.json_old"), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				Logging.SAVELOAD.warn("Quest.json renamed failed.");
			}
		}

		boolean advancementsLoadSucceeded = false;
		if (new File(location + "advancements.json").exists()) {
			try {
				JSONObject questsObj = new JSONObject(loadFromFile(location + "advancements.json", true));
				@SuppressWarnings("unused")
				Version dataVersion = new Version(questsObj.getString("Version"));
				TutorialDisplayHandler.load(questsObj);
				AdvancementElement.loadRecipeUnlockingElements(questsObj);
				QuestsDisplay.load(questsObj);
				advancementsLoadSucceeded = true;
			} catch (IOException e) {
				Logging.SAVELOAD.error(e, "Unable to load advancements.json, loading default quests instead.");
			}
		} else {
			Logging.SAVELOAD.debug("advancements.json not found, loading default quests instead.");
		}

		if (!advancementsLoadSucceeded) {
			TutorialDisplayHandler.reset(false);
			AdvancementElement.resetRecipeUnlockingElements();
			QuestsDisplay.resetGameQuests();
		}

		boolean signsLoadSucceeded = false;
		if (new File(location + "signs.json").exists()) {
			try {
				JSONObject fileObj = new JSONObject(loadFromFile(location + "signs.json", true));
				@SuppressWarnings("unused")
				Version dataVersion = new Version(fileObj.getString("Version"));
				JSONArray dataObj = fileObj.getJSONArray("signs");
				HashMap<Map.Entry<Integer, Point>, List<String>> signTexts = new HashMap<>();
				for (int i = 0; i < dataObj.length(); i++) {
					JSONObject signObj = dataObj.getJSONObject(i);
					signTexts.put(
						new AbstractMap.SimpleImmutableEntry<>(signObj.getInt("level"), new Point(signObj.getInt("x"), signObj.getInt("y"))),
						signObj.getJSONArray("lines").toList().stream().map(e -> (String) e).collect(Collectors.toList())
					);
				}

				SignDisplay.loadSignTexts(signTexts);
				signsLoadSucceeded = true;
			} catch (IOException e) {
				Logging.SAVELOAD.error(e, "Unable to load signs.json, reset sign data instead.");
			}
		} else {
			Logging.SAVELOAD.debug("signs.json not found, reset sign data instead.");
		}

		if (!signsLoadSucceeded) {
			SignDisplay.resetSignTexts();
		}
	}

	private static final Pattern OLD_TORCH_TILE_REGEX = Pattern.compile("TORCH ([\\w ]+)");

	public static void loadTile(Version worldVer, ChunkManager map, int x, int y, String tileName, String tileData) {
		Matcher matcher;
		if ((matcher = OLD_TORCH_TILE_REGEX.matcher(tileName.toUpperCase())).matches()) {
			map.setTile(x, y, Tiles.get("Torch"), Tiles.get(matcher.group(1)).id);
		} else {
			short data = 0;
			if (worldVer.compareTo(new Version("2.3.0-dev1")) > 0 || !tileName.equalsIgnoreCase("FLOWER"))
				data = Short.parseShort(tileData);
			map.setTile(x, y, Tiles.get(tileName), data);
		}
	}

	public void loadPlayer(String filename, Player player) {
		LoadingDisplay.setMessage("Player");
		loadFromFile(location + filename + extension);
		loadPlayer(player, data);
	}

	public void loadPlayer(Player player, List<String> origData) {
		List<String> data = new ArrayList<>(origData);
		player.x = Integer.parseInt(data.remove(0));
		player.y = Integer.parseInt(data.remove(0));
		player.spawnx = Integer.parseInt(data.remove(0));
		player.spawny = Integer.parseInt(data.remove(0));
		player.health = Integer.parseInt(data.remove(0));
		if (worldVer.compareTo(new Version("2.2.0-dev3")) >= 0)
			player.extraHealth = Integer.parseInt(data.remove(0));
		if (worldVer.compareTo(new Version("2.0.4-dev7")) >= 0)
			player.hunger = Integer.parseInt(data.remove(0));
		player.armor = Integer.parseInt(data.remove(0));

		if (worldVer.compareTo(new Version("2.0.5-dev5")) >= 0 || player.armor > 0 || worldVer.compareTo(new Version("2.0.5-dev4")) == 0 && data.size() > 5) {
			if (worldVer.compareTo(new Version("2.0.4-dev7")) < 0) {
				// Reverse order b/c we are taking from the end
				player.curArmor = (ArmorItem) Items.get(data.remove(data.size() - 1));
				player.armorDamageBuffer = Integer.parseInt(data.remove(data.size() - 1));
			} else {
				player.armorDamageBuffer = Integer.parseInt(data.remove(0));
				player.curArmor = (ArmorItem) Items.get(data.remove(0), true);
			}
		}
		player.setScore(Integer.parseInt(data.remove(0)));

		if (worldVer.compareTo(new Version("2.0.4-dev7")) < 0) {
			int arrowCount = Integer.parseInt(data.remove(0));
			if (worldVer.compareTo(new Version("2.0.1-dev1")) < 0)
				player.getInventory().add(Items.get("arrow"), arrowCount).forEach(deathChest.getInventory()::add);
		}

		Game.currentLevel = Integer.parseInt(data.remove(0));
		Level level = World.levels[Game.currentLevel];
		if (!player.isRemoved())
			player.remove(); // Removes the user player from the level, in case they would be added twice.
		if (level != null)
			level.add(player);
		else
			Logging.SAVELOAD.trace("Game level to add player {} to is null.", player);

		if (worldVer.compareTo(new Version("2.0.4-dev8")) < 0) {
			String modedata = data.remove(0);
			if (player == Game.player)
				loadMode(modedata); // Only load if you're loading the main player
		}

		String potioneffects = data.remove(0);
		if (!potioneffects.equals("PotionEffects[]")) {
			String[] effects = potioneffects.replace("PotionEffects[", "").replace("]", "").split(":");

			for (String s : effects) {
				String[] effect = s.split(";");
				PotionType pName = Enum.valueOf(PotionType.class, effect[0]);
				PotionItem.applyPotion(player, pName, Integer.parseInt(effect[1]));
			}
		}

		if (worldVer.compareTo(new Version("1.9.4-dev4")) < 0) {
			String colors = data.remove(0).replace("[", "").replace("]", "");
			String[] color = colors.split(";");
			int[] cols = new int[color.length];
			for (int i = 0; i < cols.length; i++)
				cols[i] = Integer.parseInt(color[i]) / 50;

			String col = "" + cols[0] + cols[1] + cols[2];
			Logging.SAVELOAD.debug("Getting color as " + col);
			player.shirtColor = Integer.parseInt(col);
		} else if (worldVer.compareTo(new Version("2.0.6-dev4")) < 0) {
			String color = data.remove(0);
			int[] colors = new int[3];
			for (int i = 0; i < 3; i++)
				colors[i] = Integer.parseInt(String.valueOf(color.charAt(i)));
			player.shirtColor = Color.get(1, colors[0] * 51, colors[1] * 51, colors[2] * 51);
		} else
			player.shirtColor = Integer.parseInt(data.remove(0));

		// Just delete the slot reserved for loading legacy skins.
		if (worldVer.compareTo(new Version("2.1.0")) < 0) {
			data.remove(0);
		}

		// Loading unlocked recipes.
		if (worldVer.compareTo(new Version("2.2.0-dev3")) >= 0) {
			ArrayList<Recipe> recipes = new ArrayList<>();
			JSONObject unlockedRecipes = new JSONObject(data.remove(0));
			for (String key : unlockedRecipes.keySet()) {
				JSONArray costsJson = unlockedRecipes.getJSONArray(key);
				String[] costs = new String[costsJson.length()];
				for (int j = 0; j < costsJson.length(); j++) {
					costs[j] = costsJson.getString(j);
				}

				// Skipping removed vanilla recipes
				if (worldVer.compareTo(new Version("2.2.0-dev6")) <= 0) {
					// Iron Ore * 4 + Coal * 1 => Iron * 1
					if (key.equalsIgnoreCase("iron_1") &&
						costs.length == 2 && costs[0].equalsIgnoreCase("iron Ore_4") &&
						costs[1].equalsIgnoreCase("coal_1"))
						continue;
					// Gold Ore * 4 + Coal * 1 => Gold * 1
					if (key.equalsIgnoreCase("gold_1") &&
						costs.length == 2 && costs[0].equalsIgnoreCase("gold Ore_4") &&
						costs[1].equalsIgnoreCase("coal_1"))
						continue;
				}

				recipes.add(new Recipe(key, costs));
			}

			CraftingDisplay.loadUnlockedRecipes(recipes);
		} else
			CraftingDisplay.resetRecipeUnlocks();
	}

	protected static String subOldName(String name, Version worldVer) {
		if (worldVer.compareTo(new Version("1.9.4-dev4")) < 0) {
			name = name.replace("Hatchet", "Axe").replace("Pick", "Pickaxe").replace("Pickaxeaxe", "Pickaxe").replace("Spade", "Shovel").replace("Pow glove", "Power Glove").replace("II", "").replace("W.Bucket", "Water Bucket").replace("L.Bucket", "Lava Bucket").replace("G.Apple", "Gold Apple").replace("St.", "Stone").replace("Ob.", "Obsidian").replace("I.Lantern", "Iron Lantern").replace("G.Lantern", "Gold Lantern").replace("BrickWall", "Wall").replace("Brick", " Brick").replace("Wall", " Wall").replace("  ", " ");
			if (name.equals("Bucket"))
				name = "Empty Bucket";
		}

		if (worldVer.compareTo(new Version("1.9.4")) < 0) {
			name = name.replace("I.Armor", "Iron Armor").replace("S.Armor", "Snake Armor").replace("L.Armor", "Leather Armor").replace("G.Armor", "Gold Armor").replace("BrickWall", "Wall");
		}

		if (worldVer.compareTo(new Version("2.0.6-dev3")) < 0) {
			name = name.replace("Fishing Rod", "Wood Fishing Rod");
		}

		if (worldVer.compareTo(new Version("2.0.6")) < 0) {
			if (name.startsWith("Pork Chop"))
				name = name.replace("Pork Chop", "Cooked Pork");
		}

		if (worldVer.compareTo(new Version("2.0.7-dev1")) < 0) {
			if (name.startsWith("Seeds"))
				name = name.replace("Seeds", "Wheat Seeds");
		}

		if (worldVer.compareTo(new Version("2.1.0-dev2")) < 0) {
			if (name.startsWith("Shear"))
				name = name.replace("Shear", "Shears");
		}

		if (worldVer.compareTo(new Version("2.2.0-dev4")) < 0) {
			if (name.startsWith("Potion"))
				name = name.replace("Potion", "Awkward Potion");
		}

		if (worldVer.compareTo(new Version("2.3.0-dev1")) < 0) {
			if (name.startsWith("Wool"))
				name = name.replace("Wool", "White Wool");
			if (name.startsWith("Flower"))
				name = name.replace("Flower", "Oxeye Daisy");
		}

		return name;
	}

	public void loadInventory(String filename, Inventory inventory) {
		deathChest = new DeathChest();
		loadFromFile(location + filename + extension);
		loadInventory(inventory, data);
	}

	public void loadInventory(Inventory inventory, List<String> data) {
		inventory.clearInv();

		for (String item : data) {
			if (item.length() == 0) {
				Logging.SAVELOAD.error("loadInventory: Item in data list is \"\", skipping item");
				continue;
			}

			// Apply name changes.
			item = subOldName(item, worldVer);

			if (item.contains("Power Glove")) continue; // Ignore the power glove.
			if (item.contains("Totem of Wind")) continue;

			if (worldVer.compareTo(new Version("2.0.4")) <= 0 && item.contains(";")) {
				String[] curData = item.split(";");
				String itemName = curData[0];

				Item newItem = Items.get(itemName);

				int count = Integer.parseInt(curData[1]);

				if (newItem instanceof StackableItem) {
					((StackableItem) newItem).count = count;
					loadItem(inventory, newItem);
				} else {
					for (int i = 0; i < count; i++) loadItem(inventory, newItem);
				}
			} else {
				loadItem(inventory, Items.get(item));
			}
		}
	}

	private void loadItem(Inventory inventory, Item item) {
		if (inventory.add(item) != null) {
			deathChest.getInventory().add(item.copy());
		}
	}

	private void loadEntities(String filename) {
		LoadingDisplay.setMessage("minicraft.displays.loading.message.entities");
		loadFromFile(location + filename + extension);

		for (int i = 0; i < World.levels.length; i++) {
			World.levels[i].clearEntities();
		}
		for (String name : data) {
			if (name.startsWith("Player")) continue;
			loadEntity(name, worldVer, true);
		}

		for (int i = 0; i < World.levels.length; i++) {
			World.levels[i].checkChestCount();
			World.levels[i].checkAirWizard();
		}
	}

	@Nullable
	public static Entity loadEntity(String entityData, Version worldVer, boolean isLocalSave) {
		entityData = entityData.trim();
		if (entityData.length() == 0) return null;

		String[] stuff = entityData.substring(entityData.indexOf("[") + 1, entityData.indexOf("]")).split(":"); // This gets everything inside the "[...]" after the entity name.
		List<String> info = new ArrayList<>(Arrays.asList(stuff));

		String entityName = entityData.substring(0, entityData.indexOf("[")); // This gets the text before "[", which is the entity name.

		int x = Integer.parseInt(info.get(0));
		int y = Integer.parseInt(info.get(1));

		int eid = -1;
		if (!isLocalSave) {
			eid = Integer.parseInt(info.remove(2));

			// If I find an entity that is loaded locally, but on another level in the entity data provided, then I ditch the current entity and make a new one from the info provided.
			Entity existing = Network.getEntity(eid);

			if (existing != null) {
				// Existing one is out of date; replace it.
				existing.remove();
				Game.levels[Game.currentLevel].add(existing);
				return null;
			}
		}

		Entity newEntity;

		if (entityName.equals("Spark") && !isLocalSave) {
			int awID = Integer.parseInt(info.get(2));
			Entity sparkOwner = Network.getEntity(awID);
			if (sparkOwner instanceof AirWizard)
				newEntity = new Spark((AirWizard) sparkOwner, x, y);
			else {
				Logging.SAVELOAD.error("Failed to load Spark; owner id doesn't point to a correct entity");
				return null;
			}
		} else if (entityName.contains(" Bed")) { // with a space, meaning that the bed has a color name in the front
			String colorName = entityName.substring(0, entityName.length() - 4).toUpperCase().replace(' ', '_');
			try {
				newEntity = new Bed(DyeItem.DyeColor.valueOf(colorName));
			} catch (IllegalArgumentException e) {
				Logging.SAVELOAD.error("Invalid bed variant: `{}`, skipping.", entityName);
				return null;
			}
		} else {
			int mobLvl = 1;
			if (!Crafter.names.contains(entityName)) { // Entity missing debugging
				try {
					Class.forName("minicraft.entity.mob." + entityName);
				} catch (ClassNotFoundException ignored) {
				}
			}

			// Check for level of AirWizard
			if (entityName.equals("AirWizard")) {
				mobLvl = Integer.parseInt(stuff[3]);
			}

			newEntity = getEntity(entityName.substring(entityName.lastIndexOf(".") + 1), mobLvl);
		}

		if (entityName.equals("FireSpark") && !isLocalSave) {
			int obID = Integer.parseInt(info.get(2));
			Entity sparkOwner = Network.getEntity(obID);
			if (sparkOwner instanceof ObsidianKnight)
				newEntity = new FireSpark((ObsidianKnight) sparkOwner, x, y);
			else {
				Logging.SAVELOAD.error("Failed to load FireSpark; owner id doesn't point to a correct entity");
				return null;
			}
		}

		if (newEntity == null)
			return null;

		if (newEntity instanceof Mob) { // This is structured the same way as in Save.java.
			Mob mob = (Mob) newEntity;
			mob.health = Integer.parseInt(info.get(2));

			Class<?> c = null;
			try {
				c = Class.forName("minicraft.entity.mob." + entityName);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

			if (EnemyMob.class.isAssignableFrom(c)) {
				EnemyMob enemyMob = ((EnemyMob) mob);
				enemyMob.lvl = Integer.parseInt(info.get(info.size() - 2));

				if (enemyMob.lvl == 0) {
					Logging.SAVELOAD.debug("Level 0 mob: " + entityName);
					enemyMob.lvl = 1;
				} else if (enemyMob.lvl > enemyMob.getMaxLevel()) {
					enemyMob.lvl = enemyMob.getMaxLevel();
				}

				mob = enemyMob;
			} else if (worldVer.compareTo(new Version("2.0.7-dev1")) >= 0) { // If the version is more or equal to 2.0.7-dev1
				if (newEntity instanceof Sheep) {
					Sheep sheep = ((Sheep) mob);
					if (info.get(3).equalsIgnoreCase("true")) {

						sheep.cut = true;
					}

					mob = sheep;
				}
			}

			newEntity = mob;
		} else if (newEntity instanceof Chest) {
			Chest chest = (Chest) newEntity;
			boolean isDeathChest = chest instanceof DeathChest;
			boolean isDungeonChest = chest instanceof DungeonChest;
			List<String> chestInfo = info.subList(2, info.size() - 1);

			int endIdx = chestInfo.size() - (isDeathChest || isDungeonChest ? 1 : 0);
			for (int idx = 0; idx < endIdx; idx++) {
				String itemData = subOldName(chestInfo.get(idx), worldVer);

				if (itemData.contains("Power Glove")) continue;
				if (itemData.contains("Totem of Wind")) continue;

				Item item = Items.get(itemData);
				chest.getInventory().add(item);
			}

			if (isDeathChest) {
				((DeathChest) chest).time = Integer.parseInt(chestInfo.get(chestInfo.size() - 1));
			} else if (isDungeonChest) {
				((DungeonChest) chest).setLocked(Boolean.parseBoolean(chestInfo.get(chestInfo.size() - 1)));
				if (((DungeonChest) chest).isLocked())
					World.levels[Integer.parseInt(info.get(info.size() - 1))].chestCount++;
			}

			newEntity = chest;
		} else if (newEntity instanceof Spawner) {
			MobAi mob = (MobAi) getEntity(info.get(2).substring(info.get(2).lastIndexOf(".") + 1), Integer.parseInt(info.get(3)));
			if (mob != null)
				newEntity = new Spawner(mob);
		} else if (newEntity instanceof Lantern && worldVer.compareTo(new Version("1.9.4")) >= 0 && info.size() > 3) {
			newEntity = new Lantern(Lantern.Type.values()[Integer.parseInt(info.get(2))]);
		} else if (newEntity instanceof KnightStatue) {
			int health = Integer.parseInt(info.get(2));
			newEntity = new KnightStatue(health);
		} else if (newEntity instanceof Boat) {
			newEntity = new Boat(Direction.getDirection(Integer.parseInt(info.get(2))));
		}

		if (!isLocalSave) {
			if (newEntity instanceof Arrow) {
				int ownerID = Integer.parseInt(info.get(2));
				Mob m = (Mob) Network.getEntity(ownerID);
				if (m != null) {
					Direction dir = Direction.values[Integer.parseInt(info.get(3))];
					int dmg = Integer.parseInt(info.get(5));
					newEntity = new Arrow(m, x, y, dir, dmg);
				}
			}
			if (newEntity instanceof ItemEntity) {
				Item item = Items.get(info.get(2));
				double zz = Double.parseDouble(info.get(3));
				int lifetime = Integer.parseInt(info.get(4));
				int timeleft = Integer.parseInt(info.get(5));
				double xa = Double.parseDouble(info.get(6));
				double ya = Double.parseDouble(info.get(7));
				double za = Double.parseDouble(info.get(8));
				newEntity = new ItemEntity(item, x, y, zz, lifetime, timeleft, xa, ya, za);
			}
			if (newEntity instanceof TextParticle) {
				int textcol = Integer.parseInt(info.get(3));
				newEntity = new TextParticle(info.get(2), x, y, textcol);
				//if (Game.debug) System.out.println("Loaded text particle; color: "+Color.toString(textcol)+", text: " + info.get(2));
			}
		}

		newEntity.eid = eid; // This will be -1 unless set earlier, so a new one will be generated when adding it to the level.
		if (newEntity instanceof ItemEntity && eid == -1)
			Logging.SAVELOAD.warn("Item entity was loaded with no eid");

		int curLevel = Integer.parseInt(info.get(info.size() - 1));
		if (World.levels[curLevel] != null) {
			World.levels[curLevel].add(newEntity, x, y);
		}

		return newEntity;
	}

	@Nullable
	private static Entity getEntity(String string, int mobLevel) {
		switch (string) {
			case "Player":
			case "RemotePlayer":
				return null;
			case "Cow":
				return new Cow();
			case "Sheep":
				return new Sheep();
			case "Pig":
				return new Pig();
			case "Zombie":
				return new Zombie(mobLevel);
			case "Slime":
				return new Slime(mobLevel);
			case "Creeper":
				return new Creeper(mobLevel);
			case "Skeleton":
				return new Skeleton(mobLevel);
			case "Knight":
				return new Knight(mobLevel);
			case "Snake":
				return new Snake(mobLevel);
			case "AirWizard":
				if (mobLevel > 1) return null;
				return new AirWizard();
			case "Spawner":
				return new Spawner(new Zombie(1));
			case "Workbench":
				return new Crafter(Crafter.Type.Workbench);
			case "Chest":
				return new Chest();
			case "DeathChest":
				return new DeathChest();
			case "DungeonChest":
				return new DungeonChest(null);
			case "Anvil":
				return new Crafter(Crafter.Type.Anvil);
			case "Enchanter":
				return new Crafter(Crafter.Type.Enchanter);
			case "Loom":
				return new Crafter(Crafter.Type.Loom);
			case "Furnace":
				return new Crafter(Crafter.Type.Furnace);
			case "Oven":
				return new Crafter(Crafter.Type.Oven);
			case "Bed":
				return new Bed();
			case "Tnt":
				return new Tnt();
			case "Lantern":
				return new Lantern(Lantern.Type.NORM);
			case "Arrow":
				return new Arrow(new Skeleton(0), 0, 0, Direction.NONE, 0);
			case "ItemEntity":
				return new ItemEntity(Items.get("unknown"), 0, 0);
			case "FireParticle":
				return new FireParticle(0, 0);
			case "SmashParticle":
				return new SmashParticle(0, 0);
			case "TextParticle":
				return new TextParticle("", 0, 0, 0);
			case "KnightStatue":
				return new KnightStatue(0);
			case "ObsidianKnight":
				return new ObsidianKnight(0);
			case "DyeVat":
				return new Crafter(Crafter.Type.DyeVat);
			case "Boat":
				return new Boat(Direction.NONE);
			default:
				Logging.SAVELOAD.error("LOAD ERROR: Unknown or outdated entity requested: " + string);
				return null;
		}
	}
}
