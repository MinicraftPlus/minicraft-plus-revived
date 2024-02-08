package minicraft.core.io;

import minicraft.core.CrashHandler;
import minicraft.core.Game;
import minicraft.saveload.Save;
import minicraft.util.Logging;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Stream;

public class FileHandler extends Game {
	private FileHandler() {
	}

	public static final int REPLACE_EXISTING = 0;
	public static final int RENAME_COPY = 1;
	public static final int SKIP = 2;

	public static final String OS;
	private static final String localGameDir;
	static final String systemGameDir;

	static {
		OS = System.getProperty("os.name").toLowerCase();
		String local = "playminicraft/mods/Minicraft_Plus";

		if (OS.contains("windows")) // windows
			systemGameDir = System.getenv("APPDATA");
		else {
			systemGameDir = System.getProperty("user.home");
			if (!OS.contains("mac"))
				local = "." + local; // linux
		}

		localGameDir = "/" + local;
	}


	/**
	 * Determines the path the game will use to store worlds, settings, resource packs, etc.
	 * If saveDir is not null, use it as the game directory. Otherwise use the default path.
	 * <p>
	 * If the default path is used, check if old default path exists and if so move it to the new path.
	 *
	 * @param saveDir Value from --savedir argument. Null if it was not set.
	 */
	public static void determineGameDir(@Nullable String saveDir) {
		if (saveDir != null) {
			gameDir = saveDir;
			Logging.GAMEHANDLER.debug("Determined gameDir: " + gameDir);

			File gameDirFile = new File(gameDir);
			gameDirFile.mkdirs();
		} else {
			saveDir = FileHandler.getSystemGameDir();

			gameDir = saveDir + localGameDir;
			Logging.GAMEHANDLER.debug("Determined gameDir: " + gameDir);

			File testFile = new File(gameDir);
			testFile.mkdirs();

			File oldFolder = new File(saveDir + "/.playminicraft/mods/Minicraft Plus");
			if (oldFolder.exists()) {
				try {
					copyFolderContents(oldFolder.toPath(), testFile.toPath(), RENAME_COPY, true);
				} catch (IOException e) {
					CrashHandler.errorHandle(e);
				}
			}

			if (OS.contains("mac")) {
				oldFolder = new File(saveDir + "/.playminicraft");
				if (oldFolder.exists()) {
					try {
						copyFolderContents(oldFolder.toPath(), testFile.toPath(), RENAME_COPY, true);
					} catch (IOException e) {
						CrashHandler.errorHandle(e);
					}
				}
			}
		}
	}

	public static String getSystemGameDir() {
		return systemGameDir;
	}

	public static String getLocalGameDir() {
		return localGameDir;
	}

	private static void deleteFolder(File top) {
		if (top == null) return;
		if (top.isDirectory()) {
			File[] subfiles = top.listFiles();
			if (subfiles != null)
				for (File subfile : subfiles)
					deleteFolder(subfile);
		}

		//noinspection ResultOfMethodCallIgnored
		top.delete();
	}

	public static void copyFolderContents(Path origFolder, Path newFolder, int ifExisting, boolean deleteOriginal) throws IOException {
		// I can determine the local folder structure with origFolder.relativize(file), then use newFolder.resolve(relative).
		Logging.RESOURCEHANDLER.info("Copying contents of folder " + origFolder + " to new folder " + newFolder);

		Files.walkFileTree(origFolder, new FileVisitor<Path>() {
			public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
				String newFilename = newFolder.resolve(origFolder.relativize(file)).toString();
				if (new File(newFilename).exists()) {
					if (ifExisting == SKIP)
						return FileVisitResult.CONTINUE;
					else if (ifExisting == RENAME_COPY) {
						newFilename = newFilename.substring(0, newFilename.lastIndexOf("."));
						do {
							newFilename += "(Old)";
						} while (new File(newFilename).exists());
						newFilename += Save.extension;
					}
				}

				Path newFile = new File(newFilename).toPath();
				try {
					Files.copy(file, newFile, StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException ex) {
					CrashHandler.errorHandle(ex);
				}
				return FileVisitResult.CONTINUE;
			}

			public FileVisitResult preVisitDirectory(Path p, BasicFileAttributes bfa) {
				return FileVisitResult.CONTINUE;
			}

			public FileVisitResult postVisitDirectory(Path p, IOException ex) {
				return FileVisitResult.CONTINUE;
			}

			public FileVisitResult visitFileFailed(Path p, IOException ex) {
				return FileVisitResult.CONTINUE;
			}
		});

		if (deleteOriginal)
			deleteFolder(origFolder.toFile());
	}

	public static ArrayList<String> listAssets() {
		Path path;
		try {
			path = Paths.get(Objects.requireNonNull(Game.class.getResource("/assets/")).toURI());
		} catch (URISyntaxException e) {
			throw new RuntimeException(e); // CRITICAL ERROR (GAME ASSETS)
		} catch (FileSystemNotFoundException e) {
			try {
				FileSystem fs = FileSystems.newFileSystem(Objects.requireNonNull(Game.class.getResource("/assets/")).toURI(), Collections.emptyMap());
				path = fs.getPath("/assets/");
			} catch (URISyntaxException | IOException ee1) {
				throw new RuntimeException(ee1); // CRITICAL ERROR (GAME ASSETS)
			}
		}

		ArrayList<String> names = new ArrayList<>();
		try (Stream<Path> paths = Files.walk(path)) {
			Path finalPath = path;
			paths.forEach(p -> names.add(finalPath.getParent().relativize(p).toString().replace('\\', '/') +
				(p.toFile().isDirectory() ? "/" : "")));
			return names;
		} catch (IOException e) {
			throw new RuntimeException(e); // CRITICAL ERROR (GAME ASSETS)
		}
	}

	/**
	 * Gets a list of paths to where the localization files are located on your disk, and adds them to the "localizationFiles" HashMap.
	 * The path is relative to the "resources" folder.
	 * Will not work if we are running this from a jar.
	 */
	private static ArrayList<String> listResourcesUsingIDE() {
		ArrayList<String> names = new ArrayList<>();
		try {
			URL fUrl = Game.class.getResource("/");
			if (fUrl == null) {
				Logging.RESOURCEHANDLER_LOCALIZATION.error("Could not find localization folder.");
				return names;
			}

			Path folderPath = Paths.get(fUrl.toURI());
			Files.walk(folderPath)
				.forEach(p -> {
					names.add(folderPath.relativize(p).toString().replace('\\', '/') + (p.toFile().isDirectory() ? "/" : ""));
				});

			if (!names.contains("resources")) { // Providing a simple resolution when the previous location is invalid, the gradle build path is used.
				Path fPath = folderPath.resolve("../../../resources/main");
				Files.walk(fPath)
					.forEach(p -> {
						names.add(fPath.relativize(p).toString().replace('\\', '/') + (p.toFile().isDirectory() ? "/" : ""));
					});
			}
		} catch (IOException | URISyntaxException e) {
			CrashHandler.errorHandle(e);
		}

		return names;
	}
}
