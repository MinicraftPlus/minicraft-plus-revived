package minicraft.util.resource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.jetbrains.annotations.Nullable;

import minicraft.core.CrashHandler;
import minicraft.core.Game;
import minicraft.core.io.FileHandler;

public class VanillaResourcePack extends ZipResourcePack {
	public VanillaResourcePack() {
		super("vanilla", null);
	}

	@Nullable
	@Override
	protected ZipFile getZipFile() {
		if (this.zipFile == null) {
			if (Game.class.getProtectionDomain().getCodeSource().getLocation().toString().endsWith("/")) {
				try {
					File file = File.createTempFile("resources", ".zip");
					if (file.exists()) file.delete();

					try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(file))) {
						for (String name : FileHandler.listResources()) { // Copy only assets and pack configuration.
							if (name.startsWith("assets/") || name.equals("pack.json") || name.equals("pack.png")) {
								out.putNextEntry(new ZipEntry(name));
								if (!name.endsWith("/")) {
									int b;
									InputStream stream = Game.class.getResourceAsStream("/" + name);
									while ((b = stream.read()) != -1) // Write per byte.
										out.write(b);
								}

								out.closeEntry();
							}
						}
					} catch (IOException e) {
						CrashHandler.crashHandle(e);
					}

					this.zipFile = new ZipFile(file);
				} catch(IOException e) {
					CrashHandler.crashHandle(e);
				}
			}
		}

		return this.zipFile;
	}
}
