package minicraft.util.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.jetbrains.annotations.Nullable;

public class ZipResourcePack extends ResourcePack {
	@Nullable
	protected ZipFile zipFile;
	protected boolean failedToOpen;

	private File file;

	public ZipResourcePack(String name, File zipFile) {
		super(name);
		this.file = zipFile;
	}

	@Nullable
	protected ZipFile getZipFile() {
		if (this.zipFile == null && !this.failedToOpen) {
			try {
				this.zipFile = new ZipFile(this.file);
			} catch(IOException e) {
				this.failedToOpen = true;
			}
		}

		return this.zipFile;
	}

	@Override
	public InputStream getFile(String path) throws IOException {
		ZipFile zip = this.getZipFile();
		if (zip != null) {
			ZipEntry entry = zip.getEntry(path);

			if (entry != null) {
				return zip.getInputStream(entry);
			}
		}

		return null;
	}

	@Override
	public void getAllFiles(String beginPath, Predicate<String> filePathPredicate, FindResultConsumer consumer) {
		ZipFile zip = this.getZipFile();
		if (zip == null) return;

		Enumeration<? extends ZipEntry> enumeration = zip.entries();

		while (enumeration.hasMoreElements()) {
			ZipEntry entry = enumeration.nextElement();

			if (entry.isDirectory() || !entry.getName().startsWith(beginPath)) continue;

			if (filePathPredicate.test(entry.getName())) {
				try {
					consumer.accept(entry.getName(), zip.getInputStream(entry));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void close() {
		if (this.zipFile != null) {
			try {
				this.zipFile.close();
			} catch(IOException e) {}
		}
	}
}
