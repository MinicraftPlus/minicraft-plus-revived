package minicraft.gfx;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

/** Bundled pixel glyphs for characters outside the resource-pack sprite alphabet. */
final class UnicodeFont {

	static final String RESOURCE = "/assets/fonts/fusion-pixel-8px-monospaced-zh_hans.ttf";
	private static final int CACHE_LIMIT = 4096;
	private static final java.awt.Font FONT = loadFont();
	// A bounded cache lets resource packs and user text introduce new characters on demand.
	private static final Map<Integer, MinicraftImage> GLYPHS = new LinkedHashMap<Integer, MinicraftImage>(128, 0.75f, true) {
		@Override
		protected boolean removeEldestEntry(Map.Entry<Integer, MinicraftImage> entry) {
			return size() > CACHE_LIMIT;
		}
	};

	private UnicodeFont() {}

	private static java.awt.Font loadFont() {
		try (InputStream stream = UnicodeFont.class.getResourceAsStream(RESOURCE)) {
			if (stream == null) throw new IllegalStateException("Missing bundled pixel font: " + RESOURCE);
			return java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, stream).deriveFont(8f);
		} catch (Exception exception) {
			throw new IllegalStateException("Cannot load bundled pixel font", exception);
		}
	}

	static synchronized MinicraftImage glyph(int codePoint) {
		int supported = FONT.canDisplay(codePoint) ? codePoint : 0xFFFD;
		MinicraftImage cached = GLYPHS.get(supported);
		if (cached != null) return cached;
		BufferedImage image = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		try {
			graphics.setFont(FONT);
			graphics.setColor(java.awt.Color.WHITE);
			graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
			graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
			graphics.drawString(new String(Character.toChars(supported)), 0, graphics.getFontMetrics().getAscent());
		} finally {
			graphics.dispose();
		}
		MinicraftImage glyph = new MinicraftImage(image);
		GLYPHS.put(supported, glyph);
		return glyph;
	}
}
