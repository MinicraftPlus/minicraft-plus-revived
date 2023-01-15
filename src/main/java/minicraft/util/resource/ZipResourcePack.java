package minicraft.util.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.jetbrains.annotations.Nullable;

public class ZipResourcePack extends ResourcePack {
	@Nullable
	protected ZipFile zip;
	protected boolean failedToOpen;

	private File zipFile;

	public ZipResourcePack(String name, File zipFile) {
		super(name);

		this.zip = this.getZipFile();
	}

	@Nullable
	protected ZipFile getZipFile() {
		if (this.zip == null && !this.failedToOpen) {
			try {
				return new ZipFile(this.zipFile);
			} catch(IOException e) {
				this.failedToOpen = true;
			}
		}

		return null;
	}

	@Override
	public InputStream getFile(String path) throws IOException {
		if (this.zip != null) {
			ZipEntry entry = this.zip.getEntry(path);

			if (entry != null) {
				return this.zip.getInputStream(entry);
			}
		}

		return null;
	}

	// TODO
	@Override
	public void getAllFiles(String beginPath, Predicate<String> filePathPredicate, FindResultConsumer consumer) {
	}

	@Override
	public void close() {
		if (this.zip != null) {
			try {
				this.zip.close();
			} catch(IOException e) {}
		}
	}
}
