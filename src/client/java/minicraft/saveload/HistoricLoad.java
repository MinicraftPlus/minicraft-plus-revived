package minicraft.saveload;

import minicraft.core.Game;
import minicraft.core.Updater;
import minicraft.core.World;
import minicraft.core.io.Localization;
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
import minicraft.entity.particle.FireParticle;
import minicraft.entity.particle.SmashParticle;
import minicraft.entity.particle.TextParticle;
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
import minicraft.screen.LoadingDisplay;
import minicraft.util.Logging;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
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
	 *   - Note that conical class name is saved and "com.mojang.ld22.entity." is trimmed when entity is saved,
	 *     unintended "particle." exists as entity name.
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
	 * - Potion system is disabled.
	 * (1.8; May 2013)
	 * - Mostly the same as the initial commit except that potion exists. Therefore, it is the same as Mar 19, 2017
	 *   for Player save file format.
	 * - FireParticle exists.
	 *
	 * Known Bugs with Old Versions (at least existed before 1.9.1)
	 * - Level saves using WorldGenMenu#sized to save level size
	 *   - Existed since Save/Load system was added (also for 1.8), remained unfixed even after 1.9.1
	 *   - Steps to reproduce in old versions:
	 *     - Let the world size setting in World Creation menu be the desired value.
	 *     - Load another world with a world size different from the value set in the menu.
	 *     - Save the loaded world.
	 *     - The resultant world save would use the set value from the menu, instead of Level#h and Level#w.
	 *     - The world save is then corrupted.
	 *   - If the world save is then tried being loaded in the old version, it crashes with IndexOutOfBoundsException.
	 *   - This bug is regarded as a possible case and handled with an on-demand data fixer.
	 * - Entities save using entity class (binary) name to save entity keys
	 *   - Existed since Save/Load system was added (also for 1.8), remained unfixed even after 1.9.1
	 *   - Reasons for exception to occur in old versions:
	 *     - When an entity is saved, its class name is obtained with "com.mojang.ld22.entity." trimmed, but not fully
	 *       applicable to all entities.
	 *     - If the entity class is not barely just belongs to the entity package, but a subpackage underneath it,
	 *       a tailing package subpackage path is append to the entity key.
	 *     - In the old versions, there existed only (2 to 3) particle types that belong to a subpackage under entity.
	 *     - However, since the lifespans of the particles are short, the chance to get them to be saved is low,
	 *       and any entity in the world is saved in old versions.
	 *   - Results in old versions:
	 *     - In 1.8, unknown entity is loaded as an empty Entity instance, and existed in the world forever.
	 *     - Starting from the first commit adding Save/Load system, an error log is printed instead, without it loaded.
	 *   - This exception has been included here with 3 particle cases.
	 * - (Unknown) For 1.8, player's position is sometimes saved in obviously different values.
	 *   - It can be out of the level for a 128-sized world.
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
		LoadingDisplay.setMessage(Localization.getStaticDisplay("minicraft.displays.loading.message.type.historic"));
		try {
			Game.player.getInventory().clearInv(); // Prepare for loading.
			loadGame(location + "Game" + Save.extension);
			LoadingDisplay.progress(10);
			// KeyPrefs is handled later.
			try {
				for (int i = 0; i < 6; ++i) {
					loadLevel(location + "Level" + i + Save.extension, location + "Level" + i + "data" + Save.extension, i);
					LoadingDisplay.progress(10);
				}
			} catch (Load.MalformedSaveDataException e) {
				throw new Load.MalformedSaveException("World", e);
			}

			loadPlayer(location + "Player" + Save.extension);
			LoadingDisplay.progress(10);
			loadInventory(location + "Inventory" + Save.extension);
			LoadingDisplay.progress(10);
			// loadEntities is not checked by me, but I assume that it is implemented correctly.
			loadEntities(location + "Entities" + Save.extension); // Most parts of the code are not modified.
			LoadingDisplay.progress(10);
		} catch (Load.MalformedSaveException e) {
			throw new Load.LoadingSessionFailedException(String.format("Failed to load world \"%s\"", worldName), e);
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

		// Do backups for all files, when the whole world is successfully loaded.
		File keyPrefsFile = new File(location + "KeyPrefs" + Save.extension);
		if (keyPrefsFile.exists()) {
			createBackup(location + "KeyPrefs" + Save.extension);
			// KeyPrefs are now saved in global preferences, but not world-wide.
			if (new File(location + "KeyPrefs" + Save.extension).delete()) // World-wide KeyPrefs are ignored.
				Logging.SAVELOAD.debug("\"{}\" is deleted.", location + "KeyPrefs" + Save.extension);
		}
		createBackup(location + "Game" + Save.extension);
		for (int i = 0; i < 6; ++i) {
			createBackup(location + "Level" + i + Save.extension);
			createBackup(location + "Level" + i + "data" + Save.extension);
		}
		createBackup(location + "Player" + Save.extension);
		createBackup(location + "Inventory" + Save.extension);
		createBackup(location + "Entities" + Save.extension);
	}

	private static List<String> loadFile(String path) throws Load.FileLoadingException {
		try {
			return Arrays.asList(Load.loadFromFile(path, true).split(","));
		} catch (IOException e) {
			throw new Load.FileLoadingException(String.format("Failed to load \"%s\"", path), e);
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
				Logging.SAVELOAD.warn("Backup \"{}\" created for \"{}\" has unexpectedly disappeared",
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
			throw new Load.IllegalSaveDataValueException(String.format("Input: %d", data));
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
					String.format("Corrupted .miniplussave (data count: %d)", data.size()));

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
					String.format("Corrupted .miniplussave (data count: %d)", data.size()));
		} catch (Load.FileLoadingException | Load.MalformedSaveDataFormatException |
		         Load.MalformedSaveDataValueException e) {
			throw new Load.MalformedSaveException("Game", new Load.MalformedSaveDataException("Game", e));
		}
	}

	private static final int[] legalWorldSizes = new int[] { 128, 256, 512 };

	private static void loadLevel(String pathA, String pathB, int index) throws Load.MalformedSaveDataException {
		try {
			List<String> dataA = loadFile(pathA);
			List<String> dataB = loadFile(pathB);
			if (dataA.size() < 3)
				throw new Load.MalformedSaveDataFormatException(
					String.format("Corrupted .miniplussave (Level data count: %d)", dataA.size()));

			int w, h, depth;
			//noinspection DuplicatedCode
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
					new Load.IllegalSaveDataValueException(String.format("width: %d; height: %d", w, h)));
			}

			// Recovery attempt for a known bug about corrupted world size; details above
			if (dataA.size() != 3 + w * h && dataA.size() - 3 == dataB.size()) {
				// If both data sets match the case, an auto fix is available.
				// Otherwise, an exception will still be thrown, if it is still illegal.
				// Additional modifications to the world save causing case mismatch are not allowed.
				for (int a : legalWorldSizes) {
					for (int b : legalWorldSizes) {
						if (a != b && dataA.size() == 3 + a * a && b == w) {
							Load.setDataFixer(new Load.AutoDataFixer() {
								@Override
								protected void performFix(String worldName) {
									// Corruption Validation
									setMessageValidating();
									String location = Game.gameDir + "/saves/" + worldName + "/";
									int[] levelWidths = new int[6];
									int[] levelHeights = new int[6];
									int[] levelSizes = new int[6];
									ArrayList<List<String>> dataAs = new ArrayList<>(6);
									for (int i = 0; i < 6; ++i) {
										try {
											List<String> dataA = loadFile(location + "Level" + i + Save.extension);
											List<String> dataB = loadFile(location + "Level" + i +
												"data" + Save.extension);
											int w, h;
											//noinspection DuplicatedCode
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

											if (w != h || Arrays.stream(legalWorldSizes).noneMatch(v -> v == w) ||
												dataA.size() - 3 != dataB.size() ||
												Arrays.stream(legalWorldSizes).noneMatch(v -> v * v == dataB.size())) {
												Logging.SAVELOAD.error("Unexpected value set detected: w: {}; h: {}; " +
													"Level{} data count: {}; Level{}data data count: {}", w, h,
													i, dataA.size(), i, dataB.size());
												Logging.SAVELOAD.warn("World save recovery for world \"{}\" failed.",
													worldName);
												finish(false);
												return; // Cancelled
											}

											levelWidths[i] = w;
											levelHeights[i] = h;
											levelSizes[i] = dataB.size();
											dataAs.add(dataA);
										} catch (Load.FileLoadingException e) {
											Logging.SAVELOAD.error(e, "Unable to load Level {} save data", i);
											Logging.SAVELOAD.warn("World save recovery for world \"{}\" failed.",
												worldName);
											finish(false);
											return; // Cancelled
										} catch (Load.MalformedSaveDataValueException e) {
											Logging.SAVELOAD.error(e, "Malformed Level {} save data", i);
											Logging.SAVELOAD.warn("World save recovery for world \"{}\" failed.",
												worldName);
											finish(false);
											return; // Cancelled
										}
									}

									for (int i = 1; i < 6; ++i) {
										if (levelWidths[i] != levelWidths[0] || levelHeights[i] != levelHeights[0] ||
											levelSizes[i] != levelSizes[0]) {
											Logging.SAVELOAD.error("Inconsistent level sizes detected (found level {}: " +
												"(w: {}; h: {}; data count: {}); expected (level 0): (w: {}; h: {}; " +
												"data count: {})), but disallowed in this save recovery.", i,
												levelWidths[i], levelHeights[i], levelSizes[i],
												levelWidths[0], levelHeights[0], levelSizes[0]);
											Logging.SAVELOAD.warn("World save recovery for world \"{}\" failed.",
												worldName);
											finish(false);
											return; // Cancelled
										}
									}

									// Validation Completed; Perform Recovery
									setMessagePerforming();
									for (int v : legalWorldSizes) {
										if (v * v == levelSizes[0]) {
											// v is now the desired world size.
											for (int i = 0; i < 6; ++i) {
												List<String> dataA = dataAs.get(i);
												// Replacing the original size values with the desired value.
												dataA.set(0, String.valueOf(v));
												dataA.set(1, String.valueOf(v));
												// Resaving the recovered data.
												try {
													Save.writeToFile(location + "Level" + i + Save.extension,
														dataA.toArray(new String[0]), true);
												} catch (IOException e) {
													Logging.SAVELOAD.error(e, "Unable to save recovered data");
													Logging.SAVELOAD.warn(
														"World save recovery for world \"{}\" failed.", worldName);
													finish(false);
													return; // Cancelled
												}
											}
										}
									}

									// Recovery successfully completed.
									finish(true);
								}
							});
						}
					}
				}
			}

			if (dataA.size() < 3 + w * h)
				throw new Load.MalformedSaveDataFormatException(
					String.format("Corrupted .miniplussave (Level data count: %d)", dataA.size()));
			if (dataB.size() < w * h)
				throw new Load.MalformedSaveDataFormatException(
					String.format("Corrupted .miniplussave (Level data data count: %d)", dataA.size()));

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
							throw new Load.IllegalSaveDataValueException(String.format("Input: %d", id));
						// This should not throw IndexOutOfBoundsException as it is checked above.
						tiles[trg] = Tiles.get(Tiles.oldids.get(id)).id;
					} catch (NumberFormatException | Load.IllegalSaveDataValueException e) {
						throw new Load.MalformedSaveDataValueException(
							String.format("Index %d (Tile id %d/%d;%d)", src + 3, trg, x, y), e);
					}
					try {
						data[trg] = (short) (Byte.parseByte(dataB.get(src)) & 0xFF);
					} catch (NumberFormatException e) {
						throw new Load.MalformedSaveDataValueException(
							String.format("Index %d (Tile data %d/%d;%d)", src, trg, x, y), e);
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
				throw new Load.MalformedSaveDataValueException(String.format("Mode index: %s", split[0]), e);
			}

			int time;
			try {
				time = Integer.parseInt(split[1]);
				validateIntegralData(time, NON_NEGATIVE_INTEGER_CHECK);
			} catch (NumberFormatException | Load.IllegalSaveDataValueException e) {
				throw new Load.MalformedSaveDataValueException(String.format("Score time: %s", split[1]), e);
			}

			if (mode == 4) Updater.scoreTime = time;
			else throw new Load.MalformedSaveDataValueException("Mode",
				new Load.IllegalSaveDataValueException(String.format("Score time is illegal in mode \"%d\"", mode)));
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
			throw new Load.MalformedSaveDataValueException(String.format("Clothing color: %s", data));
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
								throw new Load.IllegalSaveDataValueException(String.format("Invalid potion type: %s", split[0]));
						}
					} catch (Load.IllegalSaveDataValueException | Load.MalformedSaveDataValueException e) {
						throw new Load.MalformedSaveDataValueException(String.format("Potion effect (index: %d)", count), e);
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
					String.format("Corrupted .miniplussave (data count: %d)", data.size()));

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
							String.format("Corrupted .miniplussave (data count: %d)", data.size()));

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
								String.format("Corrupted .miniplussave (data count: %d)", data.size()));

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
							throw new Load.IllegalSaveDataValueException(String.format("Input: %s", val));
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
					} catch (Load.IllegalSaveDataValueException | Load.MalformedDataValueException e) {
						throw new Load.MalformedSaveDataValueException(String.format("Index %d", i), e);
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
		throws Load.IllegalSaveDataValueException, Load.MalformedDataValueException {
		Matcher matcher = ITEM_REGEX.matcher(data);
		if (matcher.matches()) {
			String itemName = matcher.group(1);
			Item item = Items.get(resolveItemName(itemName));
			if (item instanceof UnknownItem)
				throw new Load.MalformedDataValueException("Item name",
					new Load.IllegalDataValueException("Input: " + data));
			int count = 1;
			String countStr = matcher.group(2);
			if (countStr != null) { // When ";<amount>" exists.
				try {
					count = Integer.parseInt(countStr);
					if (count == 0) count = 1; // I am not sure about this.
					validateIntegralData(count, POSITIVE_INTEGER_CHECK);
				} catch (NumberFormatException | Load.IllegalSaveDataValueException e) {
					throw new Load.MalformedDataValueException(
						"Item count", e);
				}
			}

			item = item.copy();
			if (item instanceof StackableItem) {
				((StackableItem) item).count = count;
				if (deathChest != null && inv.add(item) != null) {
					deathChest.getInventory().add(item);
				}
			} else {
				int remained;
				if (deathChest != null && (remained = inv.add(item, count).size()) != 0) {
					deathChest.getInventory().add(item, remained);
				}
			}
		} else {
			throw new Load.IllegalSaveDataValueException(String.format("Input: %s", data));
		}
	}

	private static String resolveItemName(String itemName) {
		/*
		 * Item Name Change History (UTC)
		 *
		 * Notes:
		 * - "St.BrickWall" Stone Wall -> unknown version change to be resolved, so resolved here.
		 * - Note that "Cooked Pork" and "Pork Chop" were 2 separated items, but resolved later?
		 *
		 * Commit History
		 *
		 * c3f2e0ea Apr 02, 2017 (23:01)
		 * - Item "Fish Rod" is removed.
		 * - Tool type "Rod" is added, and "Wood Rod" is added with "Wood" level.
		 * 3e572c1e Mar 31, 2017 (19:38)
		 * - Items added: "Ob.BrickWall", "Obsidian Door"
		 * 9c1abfe2 Mar 29, 2017 (06:01)
		 * - " P." is trimmed out from potions' names except for item "Potion".
		 * - "P." is instead changed to "Potion".
		 * f95343bf Mar 24, 2017 (04:48)
		 * - List of spawners changed.
		 * - New list of spawners: "Cow Spawner", "Pig Spawner", "Sheep Spawner", "Slime Spawner", "Zombie Spawner",
		 *   "Creeper Spawner", "Skeleton Spawner", "Snake Spawner", "Knight Spawner", "AirWizard Spawner",
		 *   "AirWizardII Spawner"
		 * 01d8e558 Mar 21, 2017 (23:26)
		 * - Spawners are implemented.
		 * - Items added: "Zombie Spawner", "Slime Spawner", "Knight Spawner", "Snake Spawner", "Skeleton Spawner",
		 *   "Creeper Spawner", "AirWizard Spawner", "AirWizard Spawner" (duplicated), "Pig Spawner", "Sheep Spawner"
		 * 13507725 Mar 20, 2017 (10:18)
		 * - Item "Key" added
		 * aa565e20 Mar 20, 2017 (01:09)
		 * - Colored wools and clothes implemented.
		 * - 5 "Wool" variations are renamed to the corresponding colors.
		 * - Items added: "Red Clothes", "Blue Clothes", "Green Clothes", "Yellow Clothes", "Black Clothes",
		 *   "Orange Clothes", "Purple Clothes", "Cyan Clothes", "Reg Clothes"
		 * - "Wool" items renamed: "Red Wool", "Blue Wool", "Green Wool", "Yellow Wool", "Black Wool"
		 * - One "Wool" is kept.
		 * d97784ce Mar 19, 2017 (11:48)
		 * - Potions added: "Potion", "Speed P.", "Light P.", "Swim P.", "Energy P.", "Regen P.", "Time P.",
		 *   "Lava P.", "Shield P.", "Haste P."
		 * 3fb901de Mar 11, 2017 (07:34)
		 * - Renamed class "bed" to "Bed", but no effect to the entity name and item name.
		 * 0fbeae59 Feb 26, 2017 (06:22)
		 * - Dungeon Chest "D.Chest" -> "Dungeon Chest"
		 * - "Cooked Pork" is added as a food, but at the same time, "Pork Chop" exists.
		 * 11c3260d Feb 25, 2017 (22:52) First commit
		 * - Save/Load is not added yet, but items exist.
		 * - 6 "Wool" variations existed, but with the same name
		 * - "bed" existed as class name but not as item name even furniture name.
		 *
		 * Several items that have doubt to be saved, and not included in the check and mappings:
		 * - "D.Chest" Dungeon Chest -> cannot be picked up # Since the initial
		 * - "God Lantern" -> not included in ListItems; no longer existed # Since the initial
		 * - "God Workbench" -> not included in ListItems; no longer existed # Since the initial
		 */

		switch (itemName) {
			// Since the initial
				// It was not set that can be picked up, but still checked here.
			case "D.Chest": // Until Feb 26, 2017
				itemName = "Dungeon Chest"; break;
			case "St.BrickWall": // Until 1.9.4-dev4?
				itemName = "Stone Wall"; break;
			case "Fish Rod": // ?
				itemName = "Fishing Rod"; break;
			case "Speed P.": // Until Mar 29, 2017 (06:01)
				itemName = "Speed Potion"; break;
			case "Light P.": // Until Mar 29, 2017 (06:01)
				itemName = "Light Potion"; break;
			case "Swim P.": // Until Mar 29, 2017 (06:01)
				itemName = "Swim Potion"; break;
			case "Energy P.": // Until Mar 29, 2017 (06:01)
				itemName = "Energy Potion"; break;
			case "Regen P.": // Until Mar 29, 2017 (06:01)
				itemName = "Regen Potion"; break;
			case "Time P.": // Until Mar 29, 2017 (06:01)
				itemName = "Time Potion"; break;
			case "Lava P.": // Until Mar 29, 2017 (06:01)
				itemName = "Lava Potion"; break;
			case "Shield P.": // Until Mar 29, 2017 (06:01)
				itemName = "Shield Potion"; break;
			case "Haste P.": // Until Mar 29, 2017 (06:01)
				itemName = "Haste Potion"; break;
		}

		return Load.subOldName(itemName, new Version("0.0.0"));
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

					Entity newEntity;
					try {
						newEntity = getEntity(entityName, Game.player, mobLvl);
					} catch (Load.IllegalDataValueException e) {
						throw new Load.IllegalSaveDataValueException("Entity", e);
					}

					if (newEntity != Game.player) { // the method never returns null, but...
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
								} catch (Load.IllegalSaveDataValueException | Load.MalformedDataValueException e) {
									throw new Load.IllegalSaveDataValueException(String.format("Data Index %d", idx), e);
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
							try {
								MobAi mob = (MobAi) getEntity(info.get(2), Game.player, Integer.parseInt(info.get(3)));
								currentlevel = Integer.parseInt(info.get(info.size() - 1));
								World.levels[currentlevel].add(new Spawner(mob), x, y);
							} catch (Load.IllegalDataValueException e) {
								throw new Load.IllegalSaveDataValueException("Spawner entity", e);
							}
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

	@NotNull
	private static Entity getEntity(String string, Player player, int mobLevel)
			throws Load.IllegalDataValueException {
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
				if (mobLevel > 1) throw new Load.IllegalDataValueException("Mob level: " + mobLevel);
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
			case "IronLantern":
				return new Lantern(Lantern.Type.IRON);
			case "GoldLantern":
				return new Lantern(Lantern.Type.GOLD);
			case "particle.SmashParticle":
				return new SmashParticle(0, 0);
			case "particle.TextParticle":
				return new TextParticle("UNKNOWN", 0, 0, 0);
			case "particle.FireParticle": // Added since Mar 18, 2017 (04:44) c15adeb8
				return new FireParticle(0, 0);
			default:
				Logger.tag("SaveLoad/HistoricLoad").error("Unknown entity requested: {}", string);
				throw new Load.IllegalDataValueException("Input: " + string);
		}
	}
}
