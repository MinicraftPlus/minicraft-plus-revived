package minicraft.saveload;

import minicraft.core.Game;
import minicraft.core.Updater;
import minicraft.core.World;
import minicraft.core.io.Settings;
import minicraft.entity.Entity;
import minicraft.entity.furniture.Bed;
import minicraft.entity.furniture.Chest;
import minicraft.entity.furniture.Crafter;
import minicraft.entity.furniture.DeathChest;
import minicraft.entity.furniture.DungeonChest;
import minicraft.entity.furniture.Lantern;
import minicraft.entity.furniture.Spawner;
import minicraft.entity.furniture.Tnt;
import minicraft.entity.mob.AirWizard;
import minicraft.entity.mob.Cow;
import minicraft.entity.mob.Creeper;
import minicraft.entity.mob.Knight;
import minicraft.entity.mob.Mob;
import minicraft.entity.mob.MobAi;
import minicraft.entity.mob.Pig;
import minicraft.entity.mob.Player;
import minicraft.entity.mob.Sheep;
import minicraft.entity.mob.Skeleton;
import minicraft.entity.mob.Slime;
import minicraft.entity.mob.Snake;
import minicraft.entity.mob.Zombie;
import minicraft.gfx.Color;
import minicraft.item.ArmorItem;
import minicraft.item.Inventory;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.PotionItem;
import minicraft.item.PotionType;
import minicraft.item.StackableItem;
import minicraft.item.UnknownItem;
import minicraft.level.Level;
import minicraft.level.tile.Tiles;
import minicraft.util.Logging;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** This is used for loading save files before 1.9.1. */
@SuppressWarnings("SpellCheckingInspection")
public class HistoricLoad {
	/*
	 * Save Structure Change History (UTC)
	 *
	 * Notes:
	 *   - For indices, the indices are the new values when there are changes; old values if they are being removed.
	 *   - Indices are still counted whatever the conditions of the save values are.
	 *   - "<index> (tail)" indicates that the data value is at the tail of the save file.
	 *   - All save data files are saved in .miniplussave (CSV), all comma separated, tailing comma exists.
	 *   - KeyPrefs.miniplussave here is saved in world save.
	 *   - All save files here are saved by world instance, i.e. no global preferences.
	 *   - In Level5 save files, there is an extra tailing comma. (seems like a mistake)
	 *   - Although in some time periods, max armor is saved instead of armor point, it is still loaded as armor point.
	 *   - Skin-On is an abandoned feature. It controls whether to render the player skin suits,
	 *     which has already been removed now.
	 *
	 * Commit History
	 * (1.9.1) 1930f326 Apr 12, 2017 (23:30) # 1.9.1 has been released on the next day (01:53)
	 * - Game.miniplussave Versioning added
	 * b8766a33 Apr 11, 2017 (03:34)
	 * - Player.miniplussave (index: 13,14 (tail)) Armor values inserted (saved only when armor exists)
	 *   - index: 13 - Armor damage buffer value
	 *   - index: 14 - Current armor name
	 * 58c9922f Apr 2, 2017 (02:59)
	 * - Game.miniplussave (index: 3) Arrow count removed
	 * - Player.miniplussave (index: 5) Max armor value changed to armor value
	 * - Player.miniplussave (index: 7) Arrow count inserted
	 * - Player.miniplussave (index: 10) PotionEffects data save conditions changed
	 *   - saved only non-empty -> always saved; saved as empty when empty
	 *     - When empty: "PotionEffects[]"
	 * e0e2549b Mar 29, 2017 (22:12)
	 * - Player.miniplussave (index: 11 (tail)) Skin-On (boolean) inserted
	 *   - Skin-On indicates whether skin should be rendered (preference).
	 *     - It was a temporary in-game option.
	 * f95343bf Mar 24, 2017 (04:48)
	 * - Entities.miniplussave Spawner data (extradata index: 0) mob name changed ...
	 *   - Value changed from class name (from class) to spawner mob name (hardcoded value property)
	 *   - Availabilities of mob property for spawner changes; mob names here are only applicable in spawner class;
	 *     non-mob data is set as zombie; previously, non-mob entities can be set for spawners
	 *     - Mob names that differ from class names: AirWizardII
	 *       The above mobs are not loaaded correctly (thus bug fix), loaded as AirWizard (I)
	 * 01d8e558 Mar 21, 2017 (23:26)
	 * - Entities.miniplussave Spawner data instance added
	 *   - Extradata: entity name (extracted from entity class name), mob level
	 * 13507725 Mar 20, 2017 (10:18)
	 * - Entities.miniplussave DungeonChest data (extradata index: 1) is-locked: always false -> property saved
	 *   - Key and dungeon locking feature are reimplemented
	 *   - No effect to save structure
	 * d97784ce Mar 19, 2017 (11:48)
	 * - Player.miniplussave (index: 9) PotionEffects inserted (saved only data is non-empty)
	 *   - Format: "PotionEffects[<potion data>]"
	 *     - Potion data is separated by ":", no tail separator is added
	 *       - Potion data: "<name>;<time>"
	 * 67d0b872 Mar 11, 2017 (02:28)
	 * - Game.miniplussave (index: 4,5 (tail)) autosave (boolean) and sound toggle (boolean) inserted
	 * 1b392bea Mar 8, 2017 (05:47)
	 * - KeyPrefs.miniplussave added
	 *   - Each data value represents a key-value pair in format: "<name>;<value>"
	 *     - One key bind binds to only one physical key.
	 * 0fbeae59 Feb 26, 2017 (06:22)
	 * - Save/Load files reformatted, and some features disabled:
	 *   - Spawner data instance is disabled.
	 *   - Dungeon locking feature is disabled and forcedly set to "unlocked".
	 * 473bbe5b Feb 26, 2017 (06:21)
	 * - Save/Load system added
	 *
	 * Initial Save Structure (473bbe5b+0fbeae59)
	 *   - Game.miniplussave
	 *     - Data: tick count, autosave time interval, game speed, arrow count
	 *   - Level<index>.miniplussave (for each level)
	 *     - Data: size, size, depth, <tile id> (order: x * h + y)
	 *   - Level<index>data.miniplussave (for each level)
	 *     - Data: <tile data> (order: x * h + y)
	 *   - Player.miniplussave
	 *     - Data: x, y, spawnpoint x, spawnpoint y, health point, max armor value, score, current level index,
	 *       mode, clothing color
	 *       - Mode data value: "<mode>[;<score>]", ";<>" is written only in score mode.
	 *       - Color data value: "[<r>;<g>;<b>]", this seems like to be cloth color
	 */

	static void loadSave(String worldName) throws Load.LoadingSessionFailedException {
		String location = Game.gameDir + "/saves/" + worldName + "/";
		String filePath;

		try {
			Game.player.getInventory().clearInv(); // Prepare for loading.
			createBackup(filePath = location + "Game" + Save.extension);
			loadGame(filePath);
			// KeyPrefs are now saved in global preferences, but not world-wide.
			createBackup(filePath = location + "KeyPrefs" + Save.extension);
			if (new File(filePath).delete()) // World-wide KeyPrefs are ignored.
				Logging.SAVELOAD.debug("\"{}\" is deleted.", filePath);

			try {
				for (int i = 0; i < 6; ++i) {
					String dataPath = location + "Level" + i + "data" + Save.extension;
					createBackup(filePath = location + "Level" + i + Save.extension);
					createBackup(dataPath);
					loadLevel(filePath, dataPath, i);
				}
			} catch (Load.MalformedSaveDataException e) {
				throw new Load.MalformedSaveException("World", e);
			}

			createBackup(filePath = location + "Player" + Save.extension);
			loadPlayer(filePath);
			createBackup(filePath = location + "Inventory" + Save.extension);
			loadInventory(filePath);
			// loadEntities is not checked by me, but I assume that it is implemented correctly.
			createBackup(filePath = location + "Entities" + Save.extension);
			loadEntities(filePath); // Most parts of the code are not modified.
		} catch (Load.MalformedSaveException e) {
			throw new Load.LoadingSessionFailedException("Failed to load world \"" + worldName + "\"", e);
		} catch (IndexOutOfBoundsException e) {
			// By design, all checks should be done and this exception should not be thrown anywhere here.
			// MalformedSaveDataFormatException is expected to be used here in case of this.
			/* TODO Implement CodeDesignSecurityError
			 *   Such error would be thrown when there is unexpected behavior happened.
			 *   The cause should be either a design bug or memory integrity failure.
			 *   If it is a design bug, it should be reported and fixed as soon as possible.
			 *   If it is memory integrity failure, it may be because there was a bad runtime injection or
			 *     memory error.
			 */
			throw new AssertionError(e);
		}
	}

	private static List<String> loadFile(String path) throws Load.FileLoadingException {
		try {
			return Arrays.asList(Load.loadFromFile(path, true).split(","));
		} catch (IOException e) {
			throw new Load.FileLoadingException("Failed to load \"" + path + "\"", e);
		}
	}

	private static void createBackup(String path) {
		File file = new File(path);
		File bakFile = new File(path + ".bak");
		if (bakFile.exists()) {
			Logging.SAVELOAD.warn("\"{}\" exists, contents will be overwritten by new backup.",
				bakFile.getPath());
		}

		try {
			if (Files.copy(file.toPath(), bakFile.toPath(), StandardCopyOption.REPLACE_EXISTING).toFile().exists()) {
				Logging.SAVELOAD.trace("Backup \"{}\" is successfully created for \"{}\".",
					bakFile.getPath(), file.getPath());
			} else {
				Logging.SAVELOAD.warn("Backup \"{}\" created for \"{}\" is unexpectedly disappeared",
					bakFile.getPath(), file.getPath());
			}
		} catch (FileAlreadyExistsException e) { // This should not be thrown as REPLACE_EXISTING is set.
			Logging.SAVELOAD.error(new SecurityException("Unexpected exception", e));
		} catch (IOException | SecurityException | UnsupportedOperationException e) {
			Logging.SAVELOAD.error(e, "Failed to create backup of \"{}\"", file.getPath());
			Logging.SAVELOAD.warn("Backup for \"{}\" is ignored.", path);
		}
	}

	private static final Predicate<Integer> POSITIVE_INTEGER_CHECK = v -> v > 0;
	private static final Predicate<Integer> NON_NEGATIVE_INTEGER_CHECK = v -> v >= 0;

	/** Performs quick validation over an integral data. An exception is thrown when necessary. */
	private static void validateIntegralData(int data, @Nullable Predicate<Integer> condition)
		throws Load.IllegalSaveDataValueException {
		if (!(condition == null ? POSITIVE_INTEGER_CHECK : condition).test(data))
			throw new Load.IllegalSaveDataValueException("Input: " + data);
	}

	/** Validates for integral data when it is not loaded but a validation is required. */
	private static void validateIntegralSaveData(String data, String msg) throws Load.MalformedSaveDataValueException {
		try {
			validateIntegralData(Integer.parseInt(data), null);
		} catch (NumberFormatException | Load.IllegalSaveDataValueException e) {
			throw new Load.MalformedSaveDataValueException(msg, e);
		}
	}

	private static final Predicate<Float> POSITIVE_FLOAT_CHECK = v -> v > 0;
	private static final Predicate<Float> NON_NEGATIVE_FLOAT_CHECK = v -> v >= 0;

	private static void validateFloatData(float data, @Nullable Predicate<Float> condition)
		throws Load.IllegalSaveDataValueException {
		if (!(condition == null ? POSITIVE_FLOAT_CHECK : condition).test(data))
			throw new Load.IllegalSaveDataValueException("Input: " + data);
	}

	private static void validateFloatSaveData(String data, String msg) throws Load.MalformedSaveDataValueException {
		try {
			validateFloatData(Float.parseFloat(data), null);
		} catch (NumberFormatException | Load.IllegalSaveDataValueException e) {
			throw new Load.MalformedSaveDataValueException(msg, e);
		}
	}

	/** Validates for boolean data when it is not loaded but a validation is required. */
	private static void validateBooleanSaveData(String data, String msg)
		throws Load.MalformedSaveDataValueException {
		try {
			Load.parseBoolean(data);
		} catch (Load.IllegalDataValueException e) {
			throw new Load.MalformedSaveDataValueException(msg, e);
		}
	}

	private static void loadArrowCount(int ac) {
		Game.player.getInventory().add(Items.get("arrow"), ac);
	}

	private static void loadGame(String path) throws Load.MalformedSaveException {
		try {
			List<String> data = loadFile(path);
			if (data.size() < 3) // Avoids the needs for IndexOutOfBoundsException catch clauses.
				throw new Load.MalformedSaveDataFormatException(
					"Corrupted .miniplussave (data count: " + data.size() + ")");

			try {
				Updater.setTime(Integer.parseInt(data.get(0)));
			} catch (NumberFormatException e) {
				throw new Load.MalformedSaveDataValueException("Index 0 (Game tick count)", e);
			}

			// Index 1 (Auto-save time interval) -> Updater#astime is now fixed; data validation only.
			validateIntegralSaveData(data.get(1), "Index 1 (Auto-save time interval)");

			// Index 2 (Game speed) -> Updater#gamespeed is now no longer saved in saves; data validation only.
			validateFloatSaveData(data.get(2), "Index 2 (Game speed)");

			if (data.size() == 4) { // before Mar 11, 2017
				try {
					int val = Integer.parseInt(data.get(3));
					validateIntegralData(val, NON_NEGATIVE_INTEGER_CHECK);
					loadArrowCount(val);
				} catch (NumberFormatException | Load.IllegalSaveDataValueException e) {
					throw new Load.MalformedSaveDataValueException("Primary version, Index 3 (Arrow count)", e);
				}
			} else if (data.size() == 6) { // Since Mar 11, 2017 until Apr 2, 2017
				try {
					int val = Integer.parseInt(data.get(3));
					validateIntegralData(val, NON_NEGATIVE_INTEGER_CHECK);
					loadArrowCount(val);
				} catch (NumberFormatException | Load.IllegalSaveDataValueException e) {
					throw new Load.MalformedSaveDataValueException("Secondary version, Index 3 (Arrow count)", e);
				}

				try {
					Settings.set("autosave", Load.parseBoolean(data.get(4)));
				} catch (IndexOutOfBoundsException | Load.IllegalDataValueException e) {
					throw new Load.MalformedSaveDataValueException("Secondary version, Index 4 (Auto-save toggle)", e);
				}

				// Index 5 (Sound toggle) -> Settings->sound is now saved in preferences instead of world-wide,
				// data validation only.
				validateBooleanSaveData(data.get(5), "Seconadry version, Index 5 (Sound toggle)");
			} else if (data.size() == 5) { // Since Apr 2, 2017 until Apr 12, 2017 (1.9.1)
				try {
					Settings.set("autosave", Load.parseBoolean(data.get(3)));
				} catch (IndexOutOfBoundsException | Load.IllegalDataValueException e) {
					throw new Load.MalformedSaveDataValueException("Tertiary version, Index 3 (Auto-save toggle)", e);
				}

				// Index 4 (Sound toggle) -> Settings->sound is now saved in preferences instead of world-wide,
				// data validation only.
				validateBooleanSaveData(data.get(4), "Tertiary version, Index 4 (Sound toggle)");
			} else // If data count does not match, it is not able to load as it depends on the count.
				throw new Load.MalformedSaveDataFormatException(
					"Corrupted .miniplussave (data count: " + data.size() + ")");
		} catch (Load.FileLoadingException | Load.MalformedSaveDataFormatException |
		         Load.MalformedSaveDataValueException e) {
			throw new Load.MalformedSaveException("Game", new Load.MalformedSaveDataException("Game", e));
		}
	}

	private static void loadLevel(String pathA, String pathB, int index) throws Load.MalformedSaveDataException {
		try {
			List<String> dataA = loadFile(pathA);
			List<String> dataB = loadFile(pathB);
			if (dataA.size() < 3)
				throw new Load.MalformedSaveDataFormatException(
					"Corrupted .miniplussave (Level data count: " + dataA.size() + ")");

			int w, h, depth;
			try {
				w = Integer.parseInt(dataA.get(0));
				validateIntegralData(w, null);
			} catch (NumberFormatException | Load.IllegalSaveDataValueException e) {
				throw new Load.MalformedSaveDataValueException("Index 0 (Level width)", e);
			}

			try {
				h = Integer.parseInt(dataA.get(1));
				validateIntegralData(h, null);
			} catch (NumberFormatException | Load.IllegalSaveDataValueException e) {
				throw new Load.MalformedSaveDataValueException("Index 1 (Level height)", e);
			}

			try {
				depth = Integer.parseInt(dataA.get(2));
				validateIntegralData(depth, v -> v >= -4 && v <= 1);
			} catch (NumberFormatException | Load.IllegalSaveDataValueException e) {
				throw new Load.MalformedSaveDataValueException("Index 2 (Level depth)", e);
			}

			if (w != h) {
				// Prevents badly formed level
				Logging.SAVELOAD.error("Non-squared ({}, {}) level size is not supported.", w, h);
				throw new Load.MalformedSaveDataValueException("Level width (Index 0) & height (Index 1)",
					new Load.IllegalSaveDataValueException("width: " + w + "; height: " + h));
			}

			if (dataA.size() < 3 + w * h)
				throw new Load.MalformedSaveDataFormatException(
					"Corrupted .miniplussave (Level data count: " + dataA.size() + ")");
			if (dataB.size() < w * h)
				throw new Load.MalformedSaveDataFormatException(
					"Corrupted .miniplussave (Level data data count: " + dataA.size() + ")");

			Settings.set("size", w);
			short[] tiles = new short[w * h];
			short[] data = new short[w * h];
			for (int x = 0; x < w; ++x) {
				for (int y = 0; y < h; ++y) {
					int src = x * h + y;
					int trg = y * w + x;
					try { // For old saves, these are bytes.
						int id = Byte.parseByte(dataA.get(src + 3)) & 0xFF;
						if (id >= Tiles.oldids.size())
							throw new Load.IllegalSaveDataValueException("Input: " + id);
						// This should not throw IndexOutOfBoundsException as it is checked above.
						tiles[trg] = Tiles.get(Tiles.oldids.get(id)).id;
					} catch (NumberFormatException | Load.IllegalSaveDataValueException e) {
						throw new Load.MalformedSaveDataValueException(
							"Index " + (src + 3) + " (Tile id " + trg + "/" + x + ";" + y + ")", e);
					}
					try {
						data[trg] = (short) (Byte.parseByte(dataB.get(src)) & 0xFF);
					} catch (NumberFormatException e) {
						throw new Load.MalformedSaveDataValueException(
							"Index " + src + " (Tile data " + trg + "/" + x + ";" + y + ")", e);
					}
				}
			}

			World.levels[index] = new Level(w, h, depth, null, false);
			World.levels[index].tiles = tiles;
			World.levels[index].data = data;
		} catch (Load.FileLoadingException | Load.MalformedSaveDataValueException |
		         Load.MalformedSaveDataFormatException e) {
			throw new Load.MalformedSaveDataException("Level " + index, e);
		}
	}

	private static void loadMode(String data) throws Load.MalformedSaveDataValueException {
		/*
		* Survival = 1
		* Creative = 2
		* Hardcore = 3
		* Score = 4
		* Format: <mode>[;<time>]
		* [;<time>] is added when mode = 4
		*/

		int mode;
		if (data.contains(";")) {
			String[] split = data.split(";", 2);
			try {
				mode = Integer.parseInt(split[0]);
				validateIntegralData(mode, v -> v >= 1 && v <= 4);
			} catch (NumberFormatException | Load.IllegalSaveDataValueException e) {
				throw new Load.MalformedSaveDataValueException("Mode index: " + split[0], e);
			}

			int time;
			try {
				time = Integer.parseInt(split[1]);
				validateIntegralData(time, NON_NEGATIVE_INTEGER_CHECK);
			} catch (NumberFormatException | Load.IllegalSaveDataValueException e) {
				throw new Load.MalformedSaveDataValueException("Score time: " + split[1], e);
			}

			if (mode == 4) Updater.scoreTime = time;
			else throw new Load.MalformedSaveDataValueException("Mode",
				new Load.IllegalSaveDataValueException("Score time is illegal in mode \"" + mode + "\""));
		} else {
			try {
				mode = Integer.parseInt(data);
				validateIntegralData(mode, v -> v >= 1 && v <= 4);
			} catch (NumberFormatException | Load.IllegalSaveDataValueException e) {
				throw new Load.MalformedSaveDataValueException("Mode", e);
			}

			if (mode == 4) Updater.scoreTime = 300;
		}

		Settings.setIdx("mode", mode - 1); // Value is shifted in newer versions.
	}

	private static final Pattern CLOTHING_COLOR_REGEX = Pattern.compile("\\[(\\d+);(\\d+);(\\d+)]");

	private static void loadClothingColor(String data) throws Load.MalformedSaveDataValueException {
		// Format: [<r>;<g>;<b>]
		Matcher matcher = CLOTHING_COLOR_REGEX.matcher(data);
		if (matcher.matches()) {
			MatchResult matched = matcher.toMatchResult();
			int r, g, b;

			try {
				r = Integer.parseInt(matched.group(1));
				validateIntegralData(r, v -> v >= 0 && v <= 255);
			} catch (NumberFormatException | Load.IllegalSaveDataValueException e) {
				throw new Load.MalformedSaveDataValueException("Clothing color index 0 (Red)", e);
			}

			try {
				g = Integer.parseInt(matched.group(2));
				validateIntegralData(g, v -> v >= 0 && v <= 255);
			} catch (NumberFormatException | Load.IllegalSaveDataValueException e) {
				throw new Load.MalformedSaveDataValueException("Clothing color index 1 (Green)", e);
			}

			try {
				b = Integer.parseInt(matched.group(3));
				validateIntegralData(b, v -> v >= 0 && v <= 255);
			} catch (NumberFormatException | Load.IllegalSaveDataValueException e) {
				throw new Load.MalformedSaveDataValueException("Clothing color index 2 (Blue)", e);
			}

			// It is assumed that normal RGB is used here.
			Game.player.shirtColor = Color.get(1, r, g, b);
		} else
			throw new Load.MalformedSaveDataValueException("Clothing color: " + data);
	}

	private static final Pattern POTION_EFFECTS_REGEX = Pattern.compile("PotionEffects\\[([.\\w;:]+)]");

	private static void loadPotionEffects(String data, boolean allowEmpty) throws Load.MalformedSaveDataValueException {
		if (allowEmpty && data.equals("PotionEffects[]")) return;
		Matcher matcher = POTION_EFFECTS_REGEX.matcher(data);
		if (matcher.matches()) {
			MatchResult result = matcher.toMatchResult();
			int count = 0;
			try {
				for (String pair : result.group(1).split(":")) {
					String[] split = pair.split(";", 2);
					try {
						int time;
						try {
							time = Integer.parseInt(split[1]);
							validateIntegralData(time, NON_NEGATIVE_INTEGER_CHECK);
						} catch (NumberFormatException | Load.IllegalSaveDataValueException e) {
							throw new Load.MalformedSaveDataValueException("Potion effect time", e);
						}

						switch (split[0]) { // In fact, "P." should not be saved as an effect name.
							case "Potion": break;// "Potion" takes no effect but saved in some versions.
							case "Speed": PotionItem.applyPotion(Game.player, PotionType.Speed, time); break;
							case "Light": PotionItem.applyPotion(Game.player, PotionType.Light, time); break;
							case "Swim": PotionItem.applyPotion(Game.player, PotionType.Swim, time); break;
							case "Energy": PotionItem.applyPotion(Game.player, PotionType.Energy, time); break;
							case "Regen": PotionItem.applyPotion(Game.player, PotionType.Regen, time); break;
							case "Time": PotionItem.applyPotion(Game.player, PotionType.Time, time); break;
							case "Lava": PotionItem.applyPotion(Game.player, PotionType.Lava, time); break;
							case "Shield": PotionItem.applyPotion(Game.player, PotionType.Shield, time); break;
							case "Haste": PotionItem.applyPotion(Game.player, PotionType.Haste, time); break;
							default:
								throw new Load.IllegalSaveDataValueException("Invalid potion type: " + split[0]);
						}
					} catch (Load.IllegalSaveDataValueException | Load.MalformedSaveDataValueException e) {
						throw new Load.MalformedSaveDataValueException("Potion effect (index: " + count + ")", e);
					}

					count++;
				}
			} catch (Load.MalformedSaveDataValueException e) {
				throw new Load.MalformedSaveDataValueException("Active potion effects", e);
			}
		} else
			throw new Load.MalformedSaveDataValueException("Active potion effects: " + data);
	}

	private static void loadPlayer(String path) throws Load.MalformedSaveException {
		try {
			List<String> data = loadFile(path);
			if (data.size() <  6)
				throw new Load.MalformedSaveDataFormatException(
					"Corrupted .miniplussave (data count: " + data.size() + ")");

			try {
				Game.player.x = Integer.parseInt(data.get(0));
				validateIntegralData(Game.player.x, NON_NEGATIVE_INTEGER_CHECK);
			} catch (NumberFormatException | Load.IllegalSaveDataValueException e) {
				throw new Load.MalformedSaveDataValueException("Index 0 (Player position x)", e);
			}

			try {
				Game.player.y = Integer.parseInt(data.get(1));
				validateIntegralData(Game.player.y, NON_NEGATIVE_INTEGER_CHECK);
			} catch (NumberFormatException | Load.IllegalSaveDataValueException e) {
				throw new Load.MalformedSaveDataValueException("Index 1 (Player position y)", e);
			}

			try {
				Game.player.spawnx = Integer.parseInt(data.get(2));
				validateIntegralData(Game.player.spawnx, NON_NEGATIVE_INTEGER_CHECK);
			} catch (NumberFormatException | Load.IllegalSaveDataValueException e) {
				throw new Load.MalformedSaveDataValueException("Index 2 (Player spawnpoint x)", e);
			}

			try {
				Game.player.spawny = Integer.parseInt(data.get(3));
				validateIntegralData(Game.player.spawny, NON_NEGATIVE_INTEGER_CHECK);
			} catch (NumberFormatException | Load.IllegalSaveDataValueException e) {
				throw new Load.MalformedSaveDataValueException("Index 3 (Player spawnpoint y)", e);
			}

			try {
				Game.player.health = Integer.parseInt(data.get(4));
				// It is rejected if it is out of the range; old max health is 10.
				validateIntegralData(Game.player.health, v -> v >= 0 && v <= 10);
			} catch (NumberFormatException | Load.IllegalSaveDataValueException e) {
				throw new Load.MalformedSaveDataValueException("Index 4 (Player health point)", e);
			}

			try {
				Game.player.armor = Integer.parseInt(data.get(5));
				// It is rejected if it is out of the range.
				validateIntegralData(Game.player.armor, v -> v >= 0 && v <= 100);
			} catch (NumberFormatException | Load.IllegalSaveDataValueException e) {
				throw new Load.MalformedSaveDataValueException("Index 5 (Player armor point)", e);
			}

			try {
				Game.player.setScore(Integer.parseInt(data.get(6)));
			} catch (NumberFormatException e) {
				throw new Load.MalformedSaveDataValueException("Index 6 (Player score)", e);
			}

			/*
			 * Before Mar 19, 2017 # Primary
			 *   - Data count 10
			 *   - PotionEffects not added
			 * Since Mar 19, 2017 until Mar 29, 2017 # Primary revision 1
			 *   - Data count 10 when PotionEffects is empty
			 *   - Data count 11 otherwise
			 * Since Mar 29, 2017 until Apr 2, 2017
			 *   - Data count 11 when PotionEffects is empty
			 *   - Data count 12 otherwise
			 * Since Apr 2, 2017 until Apr 11, 2017
			 *   - Data count 13
			 * Since Apr 11, 2017
			 *   - Data count 13 when no armor
			 *   - Data count 15 otherwise
			 */

			if (data.size() >= 10 && data.size() <= 12) { // Before Apr 2, 2017 # Primary
				/*
				 * Data first 9 is the same (indices: 0-8)
				 * Since Mar 19, 2017 until Mar 29, 2017
				 *   When PotionEffects is empty (data count 10)
				 *     Data index 9 Clothing color # Non-revised
				 *   When PotionEffects is non-empty (data count 11)
				 *     Data index 9 Active potion effects # Revision 1
				 *     Data index 10 Clothing color # Revision 1
				 * Since Mar 29, 2017 until Apr 2, 2017
				 *   When PotionEffects is empty (data count 11)
				 *     Data index 9 Clothing color # Non-revised
				 *     Data index 10 Skin-On # Revision 2 Case 1
				 *   When PotionEffects is non-empty (data count 12)
				 *     Data index 9 Active potion effects # Revision 1
				 *     Data index 10 Clothing color # Revision 1
				 *     Data index 11 Skin-On # Revision 2 Case 2
				 *
				 * Non-revised
				 *   Data index 9 Clothing color
				 * Revision 1
				 *   Data index 9 Active potion effects
				 *   Data index 10 Clothing color
				 * Revision 2
				 *   Data index 10 (Case 1)/11 (Case 2) Skin-On
				 */

				try {
					Game.currentLevel = Integer.parseInt(data.get(7));
					World.levels[Game.currentLevel].add(Game.player);
				} catch (NumberFormatException e) {
					throw new Load.MalformedSaveDataValueException(
						"Primary version, Index 7 (Player active level index)", e);
				}

				try {
					loadMode(data.get(8));
				} catch (Load.MalformedSaveDataValueException e) {
					throw new Load.MalformedSaveDataValueException("Primary version, Index 8 (Game mode)", e);
				}

				if (!data.get(9).startsWith("PotionEffects[")) { // PotionEffects is empty
					// Data count 10-11
					if (data.size() != 10 && data.size() != 11)
						throw new Load.MalformedSaveDataFormatException(
							"Corrupted .miniplussave (data count: " + data.size() + ")");

					try {
						loadClothingColor(data.get(9));
					} catch (Load.MalformedSaveDataValueException e) {
						throw new Load.MalformedSaveDataValueException(
							"Primary version Non-revised, Index 9 (Clothing color)", e);
					}

					if (data.size() == 11) {
						// Index 10 (Skin-On) -> Suits has been removed; this is also removed; data validation only.
						validateBooleanSaveData(data.get(10),
							"Primary version Revision 2 Case 1, Index 10 (Skin-On)");
					}
				} else { // PotionEffects is non-empty
					// Data count 11-12
					if (data.size() != 11 && data.size() != 12)
						throw new Load.MalformedSaveDataFormatException(
							"Corrupted .miniplussave (data count: " + data.size() + ")");

					try {
						loadPotionEffects(data.get(9), false);
					} catch (Load.MalformedSaveDataValueException e) {
						throw new Load.MalformedSaveDataValueException(
							"Primary version Revision 1, Index 9 (Active potion effects)", e);
					}

					try {
						loadClothingColor(data.get(10));
					} catch (Load.MalformedSaveDataValueException e) {
						throw new Load.MalformedSaveDataValueException(
							"Primary version Revision 1, Index 10 (Clothing color)", e);
					}

					if (data.size() == 12) {
						// Index 11 (Skin-On) -> Suits has been removed; this is also removed; data validation only.
						validateBooleanSaveData(data.get(11),
							"Primary version Revision 2 Case 2, Index 11 (Skin-On)");
					}
				}
			} else if (data.size() == 13 || data.size() == 15) { // Since Apr 2, 2017 # Seconary
				try {
					int val = Integer.parseInt(data.get(7));
					validateIntegralData(val, NON_NEGATIVE_INTEGER_CHECK);
					loadArrowCount(val);
				} catch (NumberFormatException | Load.IllegalSaveDataValueException e) {
					throw new Load.MalformedSaveDataValueException("Secondary version, Index 7 (Arrow count)", e);
				}

				try {
					Game.currentLevel = Integer.parseInt(data.get(8));
					World.levels[Game.currentLevel].add(Game.player);
				} catch (NumberFormatException e) {
					throw new Load.MalformedSaveDataValueException(
						"Secondary version, Index 8 (Player active level index)", e);
				}

				try {
					loadMode(data.get(9));
				} catch (Load.MalformedSaveDataValueException e) {
					throw new Load.MalformedSaveDataValueException("Secondary version, Index 9 (Game mode)", e);
				}

				try {
					loadPotionEffects(data.get(10), true);
				} catch (Load.MalformedSaveDataValueException e) {
					throw new Load.MalformedSaveDataValueException(
						"Secondary version, Index 10 (Active potion effects)", e);
				}

				try {
					loadClothingColor(data.get(11));
				} catch (Load.MalformedSaveDataValueException e) {
					throw new Load.MalformedSaveDataValueException(
						"Secondary version, Index 11 (Clothing color)", e);
				}

				// Index 11 (Skin-On) -> Suits has been removed; this is also removed; data validation only.
				validateBooleanSaveData(data.get(12),
					"Secondary version, Index 12 (Skin-On)");

				if (data.size() == 15) {
					try {
						int val = Integer.parseInt(data.get(13));
						validateIntegralData(val, NON_NEGATIVE_INTEGER_CHECK);
						Game.player.armorDamageBuffer = val;
					} catch (NumberFormatException | Load.IllegalSaveDataValueException e) {
						throw new Load.MalformedSaveDataValueException(
							"Secondary version revised, Index 13 (Player armor damage buffer)", e);
					}

					try {
						String val = data.get(14);
						Item item = Items.get(val);
						if (item instanceof ArmorItem)
							Game.player.curArmor = (ArmorItem) item;
						else
							throw new Load.IllegalSaveDataValueException("Input: " + val);
					} catch (NumberFormatException | Load.IllegalSaveDataValueException e) {
						throw new Load.MalformedSaveDataValueException(
							"Secondary version revised, Index 14 (Player active armor)", e);
					}
				} else {
					Game.player.armor = 0;
				}
			}
		} catch (Load.FileLoadingException | Load.MalformedSaveDataValueException |
		         Load.MalformedSaveDataFormatException e) {
			throw new Load.MalformedSaveException("Player", e);
		}
	}

	private static final Pattern ITEM_REGEX = Pattern.compile("([\\w .]+)(?:;(\\d+))?");

	private static void loadInventory(String path) throws Load.MalformedSaveException {
		DeathChest deathChest = new DeathChest();
		try {
			List<String> data = loadFile(path);
			Inventory inv = Game.player.getInventory();

			try {
				for (int i = 0; i < data.size(); i++) {
					try {
						loadToInventory(data.get(i), inv, deathChest);
					} catch (Load.MalformedSaveDataValueException | Load.IllegalSaveDataValueException e) {
						throw new Load.MalformedSaveDataValueException("Index " + i, e);
					}
				}
			} catch (Load.MalformedSaveDataValueException e) {
				throw new Load.MalformedSaveDataException("Invalid item entry", e);
			}
		} catch (Load.FileLoadingException | Load.MalformedSaveDataException e) {
			throw new Load.MalformedSaveException("Inventory", e);
		}

		if (deathChest.getInventory().invSize() > 0) {
			Game.player.getLevel().add(deathChest, Game.player.x, Game.player.y);
			Logging.SAVELOAD.info("Added DeathChest which contains exceed items.");
		}
	}

	private static void loadToInventory(String data, Inventory inv, @Nullable DeathChest deathChest)
		throws Load.IllegalSaveDataValueException, Load.MalformedSaveDataValueException {
		Matcher matcher = ITEM_REGEX.matcher(data);
		if (matcher.matches()) {
			String itemName = matcher.group(1);
			Item item = Items.get(Load.subOldName(itemName
				.replace("P.", "Potion")
				.replace("Fish Rod", "Fishing Rod")
				.replace("bed", "Bed"), new Version("0.0.0"))
				.replace("St.", "Stone "));
			if (item instanceof UnknownItem)
				throw new Load.MalformedSaveDataValueException("Item name",
					new Load.IllegalSaveDataValueException("Input: " + data));
			int count = 1;
			String countStr = matcher.group(2);
			if (countStr != null) { // When ";<amount>" exists.
				try {
					count = Integer.parseInt(countStr);
					if (count == 0) count = 1; // I am not sure about this.
					validateIntegralData(count, POSITIVE_INTEGER_CHECK);
				} catch (NumberFormatException | Load.IllegalSaveDataValueException e) {
					throw new Load.MalformedSaveDataValueException(
						"Item count", e);
				}
			}

			item = item.copy();
			if (item instanceof StackableItem) {
				((StackableItem) item).count = count;
				int added = inv.add(item);
				if (deathChest != null && added < count) {
					deathChest.getInventory().add(item);
				}
			} else {
				int added = inv.add(item, count);
				if (deathChest != null && added < count) {
					deathChest.getInventory().add(item, count - added);
				}
			}
		} else {
			throw new Load.IllegalSaveDataValueException("Input: " + data);
		}
	}

	private static void loadEntities(String path) throws Load.MalformedSaveException {
		try {
			List<String> data = loadFile(path);
			// Simply just copy
			for (int i = 0; i < data.size(); i++) {
				try {
					List<String> info = Arrays.asList(data.get(i).substring(data.get(i).indexOf("[") + 1, data.get(i).indexOf("]")).split(":")); // This gets everything inside the "[...]" after the entity name.

					String entityName = data.get(i).substring(0, data.get(i).indexOf("[")).replace("bed", "Bed").replace("II", ""); // This gets the text before "[", which is the entity name.
					int x = Integer.parseInt(info.get(0));
					int y = Integer.parseInt(info.get(1));

					int mobLvl = 0;
					try {
						if (Class.forName("EnemyMob").isAssignableFrom(Class.forName(entityName)))
							mobLvl = Integer.parseInt(info.get(info.size() - 2));
					} catch (ClassNotFoundException ignored) { }

					Entity newEntity = getEntity(entityName, Game.player, mobLvl);

					if (newEntity != null && newEntity != Game.player) { // the method never returns null, but...
						int currentlevel;
						if (newEntity instanceof Mob) {
							Mob mob = (Mob) newEntity;
							mob.health = Integer.parseInt(info.get(2));
							currentlevel = Integer.parseInt(info.get(info.size() - 1));
							World.levels[currentlevel].add(mob, x, y);
						} else if (newEntity instanceof Chest) {
							Chest chest = (Chest) newEntity;
							boolean isDeathChest = chest instanceof DeathChest;
							boolean isDungeonChest = chest instanceof DungeonChest;
							List<String> chestInfo = info.subList(2, info.size() - 1);

							int endIdx = chestInfo.size() - (isDeathChest || isDungeonChest ? 1 : 0);
							for (int idx = 0; idx < endIdx; idx++) {
								String itemData = chestInfo.get(idx);
								if (itemData.isEmpty()) continue; // this skips any null items
								try {
									loadToInventory(itemData, chest.getInventory(), null);
								} catch (Load.IllegalSaveDataValueException | Load.MalformedSaveDataValueException e) {
									throw new Load.IllegalSaveDataValueException("Data Index " + idx, e);
								}
							}

							if (isDeathChest) {
								((DeathChest) chest).time = Integer.parseInt(chestInfo.get(chestInfo.size() - 1).replace("tl;", "")); // "tl;" is only for old save support
							} else if (isDungeonChest) {
								((DungeonChest) chest).setLocked(Boolean.parseBoolean(chestInfo.get(chestInfo.size() - 1)));
							}

							currentlevel = Integer.parseInt(info.get(info.size() - 1));
							World.levels[currentlevel].add(chest instanceof DeathChest ? chest : chest instanceof DungeonChest ? (DungeonChest) chest : chest, x, y);
						} else if (newEntity instanceof Spawner) {
							MobAi mob = (MobAi) getEntity(info.get(2), Game.player, Integer.parseInt(info.get(3)));
							currentlevel = Integer.parseInt(info.get(info.size() - 1));
							if (mob != null)
								World.levels[currentlevel].add(new Spawner(mob), x, y);
						} else {
							currentlevel = Integer.parseInt(info.get(2));
							World.levels[currentlevel].add(newEntity, x, y);
						}
					} // End of entity not null conditional
				} catch (Load.IllegalSaveDataValueException e) {
					throw new Load.MalformedSaveDataException("Invalid entity entry",
						new Load.MalformedSaveDataValueException("Index " + i, e));
				}
			}
		} catch (Load.FileLoadingException | Load.MalformedSaveDataException e) {
			throw new Load.MalformedSaveException("Entities", e);
		}
	}

	private static Entity getEntity(String string, Player player, int mobLevel) {
		switch (string) {
			case "Player":
				return player;
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
				return new DungeonChest(false);
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
			case "IronLantern":
				return new Lantern(Lantern.Type.IRON);
			case "GoldLantern":
				return new Lantern(Lantern.Type.GOLD);
			default:
				Logger.tag("SaveLoad/LegacyLoad").warn("Unknown or outdated entity requested: " + string);
				return null;
		}
	}
}
