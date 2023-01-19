package minicraft.util;

import minicraft.core.Renderer;
import minicraft.core.io.Localization;
import minicraft.core.io.Sound;
import minicraft.gfx.MinicraftImage;
import minicraft.gfx.SpriteAnimation;
import minicraft.gfx.SpriteLinker;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Objects;

import javax.imageio.ImageIO;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Used for loading resources from resource packs.<br/>
 * Note: Loaders of newer pack format inherit loaders of older pack format.
 */
public abstract class ResourcePackLoader {
	protected ResourcePackLoader() {}

	public static ResourcePackLoader getFormatSuitableResourcePackLoader(int packFormat) {
		// TODO The following parts should be updated when new pack format is introduced.
		if (packFormat == 1)
			return IncompatibleResourcePackLoaders.PathFormat1ResourcePackLoader.INSTANCE;

		// Before 1 and after or the current version.
		return CompatibleResourcePackLoader.INSTANCE;
	}

	private static String getPathFromPackEntry(ResourcePack.PackResourceStream.PackEntry entry) {
		return entry.getFullPath().toString();
	}

	@Nullable
	private static BufferedImage readImageFileFromPack(ResourcePack.PackResourceStream pack, ResourcePack.PackResourceStream.PackEntry entry)
		throws IOException, IllegalStateException, IllegalArgumentException {
		if (entry.isDir()) return null;
		try (InputStream is = pack.getInputStream(getPathFromPackEntry(entry))) {
			if (is == null) return null;
			return ImageIO.read(is);
		}
	}

	private static String readTextFileFromPack(ResourcePack.PackResourceStream pack, String path)
		throws NullPointerException, UncheckedIOException, IllegalStateException {
		InputStream is = pack.getInputStream(path);
		if (is == null) return null;
		return MyUtils.readStringFromInputStream(is);
	}

	public abstract void loadResources(ResourcePack.PackResourceStream pack);

	/**
	 * Loading all textures from the pack.
	 * This ideally should be only used by {@link #loadResources(ResourcePack.PackResourceStream)}.
	 * @param pack The pack to be loaded.
	 */
	@SuppressWarnings("unused")
	public abstract void loadTextures(ResourcePack.PackResourceStream pack);

	/**
	 * Loading the specific category of basic textures from the pack.
	 * This ideally should be only used by {@link #loadResources(ResourcePack.PackResourceStream)}.
	 * @param pack  The pack to be loaded.
	 * @param entry The directory entry of the pack.
	 * @param type  The category of basic textures.
	 */
	@SuppressWarnings("unused")
	public abstract void loadTextures(ResourcePack.PackResourceStream pack, ResourcePack.PackResourceStream.PackEntry entry, SpriteLinker.SpriteType type);

	/**
	 * Loading localization from the pack.
	 * This ideally should be only used by {@link #loadResources(ResourcePack.PackResourceStream)}.
	 * @param pack The pack to be loaded.
	 */
	@SuppressWarnings("unused")
	public abstract void loadLocalization(ResourcePack.PackResourceStream pack);

	/**
	 * Loading the texts from the pack.
	 * This ideally should be only used by {@link #loadResources(ResourcePack.PackResourceStream)}.
	 * @param pack The pack to be loaded.
	 */
	@SuppressWarnings("unused")
	public abstract void loadTexts(ResourcePack.PackResourceStream pack);

	/**
	 * Loading sounds from the pack.
	 * This ideally should be only used by {@link #loadResources(ResourcePack.PackResourceStream)}.
	 * @param pack The pack to be loaded.
	 */
	@SuppressWarnings("unused")
	public abstract void loadSounds(ResourcePack.PackResourceStream pack);

	/** Supports only for current packs with {@link ResourcePack#PACK_FORMAT}. */
	private static final class CompatibleResourcePackLoader extends IncompatibleResourcePackLoaders.PathFormat1ResourcePackLoader {
		public static final CompatibleResourcePackLoader INSTANCE = new CompatibleResourcePackLoader();

		@Override
		public void loadResources(ResourcePack.PackResourceStream pack) {
			super.loadResources(pack);
		}

		@Override
		public void loadTextures(ResourcePack.PackResourceStream pack) {
			super.loadTextures(pack);
		}

		@Override
		public void loadTextures(ResourcePack.PackResourceStream pack, ResourcePack.PackResourceStream.PackEntry entry, SpriteLinker.SpriteType type) {
			super.loadTextures(pack, entry, type);
		}

		@Override
		public void loadLocalization(ResourcePack.PackResourceStream pack) {
			super.loadLocalization(pack);
		}

		@Override
		public void loadTexts(ResourcePack.PackResourceStream pack) {
			super.loadTexts(pack);
		}

		@Override
		public void loadSounds(ResourcePack.PackResourceStream pack) {
			for (ResourcePack.PackResourceStream.PackEntry entry : pack.getFiles("assets/sounds", (path, isDir) -> path.toString().endsWith(".wav") && !isDir)) {
				String name = entry.getFilename();
				try (InputStream is = pack.getInputStream(entry.getFullPath().toString())) {
					if (is == null){
						Logging.RESOURCEHANDLER_LOCALIZATION.debug("Unable to load audio: {} in pack : {}", entry.getFullPath(), pack.getPack().getName());
						return;
					}

					Sound.loadSound(name.substring(0, name.length() - 4), new BufferedInputStream(is), pack.getPack().getName());
				} catch (IOException | UnsupportedAudioFileException | LineUnavailableException e) {
					Logging.RESOURCEHANDLER_LOCALIZATION.debug(e, "Unable to load audio: {} in pack : {}", entry.getFullPath(), pack.getPack().getName());
				}
			}
		}
	}

	/** Supports only for resource packs made for an older version of Minicraft+. */
	private static final class IncompatibleResourcePackLoaders {
		private static class PathFormat1ResourcePackLoader extends ResourcePackLoader {
			public static final PathFormat1ResourcePackLoader INSTANCE = new PathFormat1ResourcePackLoader();

			@Override
			public void loadResources(ResourcePack.PackResourceStream pack) {
				loadTextures(pack);
				loadLocalization(pack);
				loadTexts(pack);
				loadSounds(pack);
			}

			@Override
			public void loadTextures(ResourcePack.PackResourceStream pack) {
				for (ResourcePack.PackResourceStream.PackEntry t : pack.getFiles("assets/textures", (p, d) -> d)) {
					switch (t.getFilename()) {
						case "entity": loadTextures(pack, t, SpriteLinker.SpriteType.Entity); break;
						case "gui": loadTextures(pack, t, SpriteLinker.SpriteType.Gui); break;
						case "item": loadTextures(pack, t, SpriteLinker.SpriteType.Item); break;
						case "tile": loadTextures(pack, t, SpriteLinker.SpriteType.Tile); break;
					}
				}
			}

			@Override
			public void loadTextures(ResourcePack.PackResourceStream pack, ResourcePack.PackResourceStream.PackEntry entry, SpriteLinker.SpriteType type) {
				if (entry.getParent() == null) return; // Should be under a directory in assets directory.
				ArrayList<ResourcePack.PackResourceStream.PackEntry> pngFiles = pack.getFiles(entry.getParent().resolve(entry.getFilename()), (p, d) -> !d && p.toString().endsWith(".png"));
				if (type == SpriteLinker.SpriteType.Tile) {
					// Loading sprite sheet metadata.
					for (ResourcePack.PackResourceStream.PackEntry jsonF : pack.getFiles(entry.getFullPath(), (p, isDir) -> p.toString().endsWith(".png.json") && !isDir)) {
						loadTextureMeta(pack, jsonF, pngFiles, type);
					}
				}

				// Loading the png files left.
				for (ResourcePack.PackResourceStream.PackEntry pngF : pngFiles) {
					loadOtherCategorizedTextures(pngF, pack, type);
				}
			}

			/** This method is only compatible with {@link minicraft.gfx.SpriteLinker.SpriteType#Tile} categorized textures. */
			protected void loadTextureMeta(ResourcePack.PackResourceStream pack, ResourcePack.PackResourceStream.PackEntry jsonF,
										   ArrayList<ResourcePack.PackResourceStream.PackEntry> pngFiles, SpriteLinker.SpriteType type) {
				try {
					JSONObject obj = new JSONObject(readTextFileFromPack(pack, getPathFromPackEntry(jsonF)));
					SpriteLinker.SpriteMeta meta = new SpriteLinker.SpriteMeta();
					String metaFn = jsonF.getFilename();
					String pngFn = metaFn.substring(0, metaFn.length() - 5);
					ResourcePack.PackResourceStream.PackEntry pngF = null;
					for (Iterator<ResourcePack.PackResourceStream.PackEntry> it = pngFiles.iterator(); it.hasNext();) {
						ResourcePack.PackResourceStream.PackEntry e = it.next();
						if (e.getFilename().equals(pngFn)) { // Removing the associated png file from the list.
							it.remove();
							pngF = e; // The png file is handled here instead.
							break;
						}
					}

					if (pngF == null) {
						Path parent = Objects.requireNonNull(jsonF.getParent());
						Logging.RESOURCEHANDLER_RESOURCEPACK.warn("File {} not found but meta file {} found in pack {}; skipping...",
							parent.resolve(pngFn), jsonF.getFullPath(), pack.getPack().getName());
						return;
					}

					BufferedImage image = readImageFileFromPack(pack, pngF);
					if (image == null) { // The file is not dir; the only reason is that the format is unsupported.
						Path parent = Objects.requireNonNull(jsonF.getParent());
						Logging.RESOURCEHANDLER_RESOURCEPACK.warn("Image file {} format unsupported in pack {}; skipping...",
							parent.resolve(pngFn), pack.getPack().getName());
						return;
					}

					if (!isSpriteImageSupported(image, SpriteLinker.SpriteType.Tile)) {
						Path parent = Objects.requireNonNull(jsonF.getParent());
						Logging.RESOURCEHANDLER_RESOURCEPACK.warn("Image file {} dimension {}x{} unsupported in pack {}; skipping...",
							parent.resolve(pngFn), image.getWidth(), image.getHeight(), pack.getPack().getName());
						return;
					}

					// Applying animations.
					MinicraftImage sheet = loadTextureAnimationMeta(image, obj, jsonF, meta, type, pngFn, pack);

					// Applying tile sprite connections.
					String key = metaFn.substring(0, metaFn.length() - 9);
					Renderer.spriteLinker.setSprite(type, key, sheet);
					loadTextureTileSpriteConnectionsMeta(obj, jsonF, meta, pngFiles, pack);

					SpriteAnimation.setMetadata(key, meta);
				} catch (JSONException | IOException | NullPointerException | UncheckedIOException | IllegalArgumentException e) {
					Logging.RESOURCEHANDLER_RESOURCEPACK.warn(e, "Unable to read {} in pack: {}; skipping...", jsonF.getFullPath(), pack.getPack().getName());
				}
			}

			/** This method is only compatible with {@link minicraft.gfx.SpriteLinker.SpriteType#Tile} categorized textures. */
			protected MinicraftImage loadTextureAnimationMeta(BufferedImage image, JSONObject json, ResourcePack.PackResourceStream.PackEntry jsonF,
															  SpriteLinker.SpriteMeta meta, SpriteLinker.SpriteType type, String pngFn, ResourcePack.PackResourceStream pack)
				throws NullPointerException, InvalidPathException {

				JSONObject animation = json.optJSONObject("animation");
				if (animation != null) {
					if (!isSpriteImageFullyCompatible(image, type, true)) {
						Path parent = Objects.requireNonNull(jsonF.getParent());
						Logging.RESOURCEHANDLER_RESOURCEPACK.trace("Image file {} dimension {}x{} is not fully compatible in pack {}.",
							parent.resolve(pngFn), image.getWidth(), image.getHeight(), pack.getPack().getName());
					}

					meta.frametime = animation.getInt("frametime");
					meta.frames = image.getHeight() / 16;
					return new MinicraftImage(image, 16, 16 * meta.frames);
				} else {
					if (!isSpriteImageFullyCompatible(image, type, false)) {
						Path parent = Objects.requireNonNull(jsonF.getParent());
						Logging.RESOURCEHANDLER_RESOURCEPACK.trace("Image file {} dimension {}x{} is not fully compatible in pack {}.",
							parent.resolve(pngFn), image.getWidth(), image.getHeight(), pack.getPack().getName());
					}

					return new MinicraftImage(image, 16, 16);
				}
			}

			protected void loadTextureTileSpriteConnectionsMeta(JSONObject json, ResourcePack.PackResourceStream.PackEntry jsonF, SpriteLinker.SpriteMeta meta,
																ArrayList<ResourcePack.PackResourceStream.PackEntry> pngFiles, ResourcePack.PackResourceStream pack)
				throws IOException, NullPointerException, InvalidPathException {

				JSONObject borderObj = json.optJSONObject("border");
				if (borderObj != null) {
					loadTextureTileSpriteConnectionsBorderMeta(borderObj, jsonF, meta, pngFiles, pack);
					loadTextureTileSpriteConnectionsCornerMeta(borderObj, jsonF, meta, pngFiles, pack);
				}
			}

			protected void loadTextureTileSpriteConnectionsBorderMeta(JSONObject json, ResourcePack.PackResourceStream.PackEntry jsonF, SpriteLinker.SpriteMeta meta,
																	  ArrayList<ResourcePack.PackResourceStream.PackEntry> pngFiles, ResourcePack.PackResourceStream pack)
				throws IOException, NullPointerException, InvalidPathException {

				meta.border = json.optString("key", null);
				if (meta.border != null) {
					String borderFn = meta.border + ".png";
					ResourcePack.PackResourceStream.PackEntry borderF = null;
					try {
						borderF = extractFileFromFileList(pngFiles, borderFn);
					} catch (UnsupportedOperationException e) {
						Path parent = Objects.requireNonNull(jsonF.getParent());
						Logging.RESOURCEHANDLER_RESOURCEPACK.debug(e.getCause(), "Unsupported file list:\n files: {};\n file to search: {}; from meta (tile connection border) {} in pack {}.",
							pngFiles, parent.resolve(borderFn), jsonF.getFullPath(), pack.getPack().getName());
					}

					if (borderF == null) {
						Path parent = Objects.requireNonNull(jsonF.getParent());
						Logging.RESOURCEHANDLER_RESOURCEPACK.warn("File {} not found but meta file {} has defined as border in pack {}; skipping...",
							parent.resolve(borderFn), jsonF.getFullPath(), pack.getPack().getName());
						meta.border = null;
					} else {
						BufferedImage borderImage = readImageFileFromPack(pack, borderF);
						if (borderImage == null) { // The file is not dir; the only reason is that the format is unsupported.
							Path parent = Objects.requireNonNull(jsonF.getParent());
							Logging.RESOURCEHANDLER_RESOURCEPACK.warn("Image file (border) {} format unsupported in pack {}; skipping...",
								parent.resolve(borderFn), pack.getPack().getName());
							meta.border = null;
						} else {
							if (borderImage.getWidth() < 24 || borderImage.getHeight() < 24) {
								Path parent = Objects.requireNonNull(jsonF.getParent());
								Logging.RESOURCEHANDLER_RESOURCEPACK.warn("Image file (border) {} dimension {}x{} unsupported in pack {}; skipping...",
									parent.resolve(borderFn), borderImage.getWidth(), borderImage.getHeight(), pack.getPack().getName());
								meta.border = null;
							} else {
								if (borderImage.getWidth() != 24 || borderImage.getHeight() != 24) {
									Path parent = Objects.requireNonNull(jsonF.getParent());
									Logging.RESOURCEHANDLER_RESOURCEPACK.trace("Image file (border) {} dimension {}x{} is not fully compatible in pack {}.",
										parent.resolve(borderFn), borderImage.getWidth(), borderImage.getHeight(), pack.getPack().getName());
								}

								Renderer.spriteLinker.setSprite(SpriteLinker.SpriteType.Tile, meta.border, new MinicraftImage(borderImage, 24, 24));
							}
						}
					}
				}
			}

			protected void loadTextureTileSpriteConnectionsCornerMeta(JSONObject json, ResourcePack.PackResourceStream.PackEntry jsonF, SpriteLinker.SpriteMeta meta,
																	  ArrayList<ResourcePack.PackResourceStream.PackEntry> pngFiles, ResourcePack.PackResourceStream pack)
				throws IOException, NullPointerException, InvalidPathException {

				meta.corner = json.optString("corner", null);
				if (meta.corner != null) {
					String cornerFn = meta.corner + ".png";
					ResourcePack.PackResourceStream.PackEntry cornerF = null;
					try {
						cornerF = extractFileFromFileList(pngFiles, cornerFn);
					} catch (UnsupportedOperationException e) {
						Path parent = Objects.requireNonNull(jsonF.getParent());
						Logging.RESOURCEHANDLER_RESOURCEPACK.debug(e.getCause(), "Unsupported file list:\n files: {};\n file to search: {}; from meta (tile connection corner) {} in pack {}.",
							pngFiles, parent.resolve(cornerFn), jsonF.getFullPath(), pack.getPack().getName());
					}

					if (cornerF == null) {
						Path parent = Objects.requireNonNull(jsonF.getParent());
						Logging.RESOURCEHANDLER_RESOURCEPACK.warn("File {} not found but meta file {} has defined as border in pack {}; skipping...",
							parent.resolve(cornerFn), jsonF.getFullPath(), pack.getPack().getName());
						meta.corner = null;
					} else {
						BufferedImage cornerImage = readImageFileFromPack(pack, cornerF);
						if (cornerImage == null) { // The file is not dir; the only reason is that the format is unsupported.
							Path parent = Objects.requireNonNull(jsonF.getParent());
							Logging.RESOURCEHANDLER_RESOURCEPACK.warn("Image file (corner) {} format unsupported in pack {}; skipping...",
								parent.resolve(cornerFn), pack.getPack().getName());
							meta.corner = null;
						} else {
							if (cornerImage.getWidth() < 16 || cornerImage.getHeight() < 16) {
								Path parent = Objects.requireNonNull(jsonF.getParent());
								Logging.RESOURCEHANDLER_RESOURCEPACK.warn("Image file (corner) {} dimension {}x{} unsupported in pack {}; skipping...",
									parent.resolve(cornerFn), cornerImage.getWidth(), cornerImage.getHeight(), pack.getPack().getName());
								meta.corner = null;
							} else {
								if (cornerImage.getWidth() != 16 || cornerImage.getHeight() != 16) {
									Path parent = Objects.requireNonNull(jsonF.getParent());
									Logging.RESOURCEHANDLER_RESOURCEPACK.trace("Image file (corner) {} dimension {}x{} is not fully compatible in pack {}.",
										parent.resolve(cornerFn), cornerImage.getWidth(), cornerImage.getHeight(), pack.getPack().getName());
								}

								Renderer.spriteLinker.setSprite(SpriteLinker.SpriteType.Tile, meta.corner, new MinicraftImage(cornerImage, 16, 16));
							}
						}
					}
				}
			}

			protected ResourcePack.PackResourceStream.PackEntry extractFileFromFileList(ArrayList<ResourcePack.PackResourceStream.PackEntry> files, String fn)
				throws UnsupportedOperationException {

				for (Iterator<ResourcePack.PackResourceStream.PackEntry> it = files.iterator(); it.hasNext();) {
					try {
						ResourcePack.PackResourceStream.PackEntry e = it.next();
						if (e.getFilename().equals(fn)) { // Removing the associated file from the list.
							it.remove();
							return e; // The file is handled in the separated handler instead.
						}
					} catch (NoSuchElementException e) {
						return null;
					} catch (UnsupportedOperationException | IllegalStateException e) {
						throw new UnsupportedOperationException(e); // Invalid file list.
					}
				}

				return null;
			}

			/** Used after filtered out png meta connected pngs.
			 * Applicable only for textures categorized in {@link SpriteLinker.SpriteType}. */
			protected void loadOtherCategorizedTextures(ResourcePack.PackResourceStream.PackEntry pngF, ResourcePack.PackResourceStream pack, SpriteLinker.SpriteType type)
				throws NullPointerException, InvalidPathException {

				try {
					String pngFn = pngF.getFilename();
					BufferedImage image = readImageFileFromPack(pack, pngF);
					if (image == null) { // The file is not dir; the only reason is that the format is unsupported.
						Path parent = Objects.requireNonNull(pngF.getParent());
						Logging.RESOURCEHANDLER_RESOURCEPACK.warn("Image file {} format unsupported in pack {}; skipping...",
							parent.resolve(pngFn), pack.getPack().getName());
						return; // Skipping.
					}

					if (!isSpriteImageSupported(image, type)) {
						Path parent = Objects.requireNonNull(pngF.getParent());
						Logging.RESOURCEHANDLER_RESOURCEPACK.warn("Image file {} dimension {}x{} unsupported in pack {}; skipping...",
							parent.resolve(pngFn), image.getWidth(), image.getHeight(), pack.getPack().getName());
						return; // Skipping.
					}

					if (!isSpriteImageFullyCompatible(image, type, false)) {
						Path parent = Objects.requireNonNull(pngF.getParent());
						Logging.RESOURCEHANDLER_RESOURCEPACK.trace("Image file {} dimension {}x{} is not fully compatible in pack {}.",
							parent.resolve(pngFn), image.getWidth(), image.getHeight(), pack.getPack().getName());
					}

					MinicraftImage sheet;
					if (type == SpriteLinker.SpriteType.Item) {
						sheet = new MinicraftImage(image, 8, 8); // Set the minimum tile sprite size.
					} else if (type == SpriteLinker.SpriteType.Tile) {
						sheet = new MinicraftImage(image, 16, 16); // Set the minimum item sprite size.
					} else {
						int width = image.getWidth();
						int height = image.getHeight();
						sheet = new MinicraftImage(image, width - width % 8, height - height % 8);
					}

					Renderer.spriteLinker.setSprite(type, pngFn.substring(0, pngFn.length() - 4), sheet);
				} catch (IOException | IllegalArgumentException | NullPointerException e) {
					Logging.RESOURCEHANDLER_RESOURCEPACK.warn(e, "Unable to load {} in pack: {}; skipping...", pngF, pack.getPack().getName());
				}
			}

			protected boolean isSpriteImageSupported(BufferedImage image, SpriteLinker.SpriteType type) {
				// Extra pixels are cropped out; regarded as supported.
				if (type == SpriteLinker.SpriteType.Tile) {
					return image.getWidth() >= 16 && image.getHeight() >= 16;
				} else {
					return image.getWidth() >= 8 && image.getHeight() >= 8;
				}
			}

			protected boolean isSpriteImageFullyCompatible(BufferedImage image, SpriteLinker.SpriteType type, boolean isAnimated) {
				// Extra pixels are counted in.
				if (type == SpriteLinker.SpriteType.Tile) {
					if (image.getWidth() != 16) return false;
					else if (isAnimated)
						return image.getHeight() % 16 == 0 && image.getHeight() > 0;
					else
						return image.getHeight() == 16; // Perfect square.
				} else {
					return image.getWidth() > 0 && image.getWidth() % 8 == 0 &&
						image.getHeight() > 0 && image.getHeight() % 8 == 0;
				}
			}

			@Override
			public void loadLocalization(ResourcePack.PackResourceStream pack) {
				try { // pack.json should be valid here; validated previously.
					JSONObject json = new JSONObject(readTextFileFromPack(pack, "pack.json"));
					JSONObject langJSON = json.optJSONObject("language");

					if (langJSON != null) {
						for (String loc : langJSON.keySet()) {
							try {
								JSONObject info = langJSON.getJSONObject(loc);
								if (Localization.addLocale(loc, new Localization.LocaleInformation(loc, info.getString("name"), info.optString("region", null)))) {
									Logging.RESOURCEHANDLER_RESOURCEPACK.warn("Language {} has already been existed before pack {}; skipping...", loc, pack.getPack().getName());
								}
							} catch (JSONException e) {
								Logging.RESOURCEHANDLER_RESOURCEPACK.warn(e, "Invalid localization configuration in pack: {}", pack.getPack().getName());
							}
						}
					}
				} catch (JSONException | NullPointerException | UncheckedIOException e) {
					Logging.RESOURCEHANDLER_RESOURCEPACK.warn(e, "Unable to load pack.json in pack: {}", pack.getPack().getName());
				}

				for (ResourcePack.PackResourceStream.PackEntry entry : pack.getFiles("assets/localization", (path, isDir) -> path.toString().endsWith(".json") && !isDir)) {
					try { // JSON verification.
						String fn = entry.getFilename();
						String json = readTextFileFromPack(pack, entry.getFullPath().toString());
						JSONObject obj = new JSONObject(json);
						for (String k : obj.keySet()) {
							obj.getString(k);
						}

						// Add verified localization.
						if (!Localization.addLocalization(fn.substring(0, fn.length() - 5), json)) {
							Logging.RESOURCEHANDLER_LOCALIZATION.warn("Unable to add localization because the language is not existed: {} in pack: {}", entry.getFullPath(), pack.getPack().getName());
						}
					} catch (NullPointerException | UncheckedIOException e) {
						Logging.RESOURCEHANDLER_LOCALIZATION.warn(e, "Unable to load localization: {} in pack: {}", entry.getFullPath(), pack.getPack().getName());
					} catch (JSONException e) {
						Logging.RESOURCEHANDLER_LOCALIZATION.warn(e, "Invalid JSON format detected in localization: {} in pack: {}", entry.getFullPath(), pack.getPack().getName());
					}
				}
			}

			@Override
			public void loadTexts(ResourcePack.PackResourceStream pack) {
				for (ResourcePack.PackResourceStream.PackEntry entry : pack.getFiles("assets/books", (path, isDir) -> path.toString().endsWith(".txt") && !isDir))  {
					try {
						String book = BookData.loadBook(readTextFileFromPack(pack, entry.getFullPath().toString()));
						switch (entry.getFilename()) {
							case "about.txt": BookData.about = () -> book; break;
							case "credits.txt": BookData.credits = () -> book; break;
							case "instructions.txt": BookData.instructions = () -> book; break;
							case "antidous.txt": BookData.antVenomBook = () -> book; break;
							case "story_guide.txt": BookData.storylineGuide = () -> book; break;
						}
					} catch (NullPointerException | UncheckedIOException e) {
						Logging.RESOURCEHANDLER_LOCALIZATION.warn(e, "Unable to load book: {} in pack: {}", entry.getFullPath(), pack.getPack().getName());
					}
				}
			}

			@Override
			public void loadSounds(ResourcePack.PackResourceStream pack) {
				for (ResourcePack.PackResourceStream.PackEntry entry : pack.getFiles("assets/sound", (path, isDir) -> path.toString().endsWith(".wav") && !isDir)) {
					String name = entry.getFilename();
					try (InputStream is = pack.getInputStream(entry.getFullPath().toString())) {
						if (is == null){
							Logging.RESOURCEHANDLER_LOCALIZATION.debug("Unable to load audio: {} in pack: {}", entry.getFullPath(), pack.getPack().getName());
							return;
						}

						Sound.loadSound(name.substring(0, name.length() - 4), new BufferedInputStream(pack.getInputStream(entry.getFullPath().toString())), pack.getPack().getName());
					} catch (IOException | UnsupportedAudioFileException | LineUnavailableException e) {
						Logging.RESOURCEHANDLER_LOCALIZATION.debug(e, "Unable to load audio: {} in pack : {}", entry.getFullPath(), pack.getPack().getName());
					}
				}
			}
		}
	}
}
