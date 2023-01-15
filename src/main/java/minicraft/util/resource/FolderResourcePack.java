package minicraft.util.resource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

public class FolderResourcePack extends ResourcePack {
	private final Path root;

	public FolderResourcePack(String name, Path folderPath) {
		super(name);
		this.root = folderPath;
	}

	@Override
	@Nullable
	public InputStream getFile(String filePath) throws IOException {
		Path path = this.root.resolve(filePath);

		if (Files.exists(path, new LinkOption[0])) {
			return Files.newInputStream(path, new OpenOption[0]);
		}

		return null;
	}

	// TODO
	@Override
	public void getAllFiles(String beginPath, Predicate<String> filePathPredicate, FindResultConsumer consumer) {
	}
}
