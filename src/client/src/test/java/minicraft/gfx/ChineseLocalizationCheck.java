package minicraft.gfx;

import minicraft.core.Renderer;
import minicraft.core.io.Localization;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.level.tile.Tiles;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Runs without opening a game window or touching a player's preferences. */
public final class ChineseLocalizationCheck {

	private static void check(boolean value, String message) {
		if (!value) throw new AssertionError(message);
	}

	private static String resource(String name) throws Exception {
		try (InputStream in = ChineseLocalizationCheck.class.getResourceAsStream(name)) {
			check(in != null, "Missing resource " + name);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buffer = new byte[8192];
			for (int count; (count = in.read(buffer)) >= 0;) out.write(buffer, 0, count);
			return new String(out.toByteArray(), StandardCharsets.UTF_8);
		}
	}

	private static List<String> placeholders(String text) {
		Matcher matcher = Pattern.compile("%(?:\\d+\\$)?[-#+ 0,(<]*\\d*(?:\\.\\d+)?[a-zA-Z%]").matcher(text);
		List<String> result = new ArrayList<>();
		while (matcher.find()) result.add(matcher.group());
		return result;
	}

	public static void main(String[] args) {
		// Resource loading starts native audio/controller threads; this is a forked test JVM.
		try {
			runChecks(args);
			System.exit(0);
		} catch (Throwable failure) {
			failure.printStackTrace();
			System.exit(1);
		}
	}

	private static void runChecks(String[] args) throws Exception {
		String englishText = resource("/assets/localization/en-us.json");
		String chineseText = resource("/assets/localization/zh-cn.json");
		JSONObject english = new JSONObject(englishText), chinese = new JSONObject(chineseText);
		JSONObject info = new JSONObject(resource("/pack.json")).getJSONObject("language").getJSONObject("zh-cn");
		check(info.getString("name").equals("Chinese"), "Language list must say Chinese");
		for (String key : english.keySet()) {
			check(chinese.has(key), "Untranslated key: " + key);
			check(placeholders(english.getString(key)).equals(placeholders(chinese.getString(key))), "Changed format arguments: " + key);
		}
		java.awt.Font bundled;
		try (InputStream in = ChineseLocalizationCheck.class.getResourceAsStream(UnicodeFont.RESOURCE)) {
			bundled = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, in).deriveFont(8f);
		}
		Set<Integer> characters = new TreeSet<>();
		for (String key : chinese.keySet()) chinese.getString(key).codePoints().filter(c -> c > 127).forEach(characters::add);
		for (int cp : characters) {
			check(bundled.canDisplay(cp), String.format("Missing pixel glyph U+%04X (%s)", cp, new String(Character.toChars(cp))));
			MinicraftImage glyph = UnicodeFont.glyph(cp);
			check(Arrays.stream(glyph.pixels).anyMatch(p -> p != 0), "Empty glyph " + cp);
			check(Arrays.stream(glyph.pixels).allMatch(p -> p == 0 || p == 0x1FFFFFF), "Glyph must be crisp monochrome pixels");
			// Compare the complete glyph bounds with the 8x8 sprite to catch clipped strokes.
			BufferedImage full = new BufferedImage(24, 24, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = full.createGraphics();
			g.setFont(bundled);
			g.setColor(java.awt.Color.WHITE);
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
			g.drawString(new String(Character.toChars(cp)), 8, 8 + g.getFontMetrics().getAscent());
			g.dispose();
			long fullPixels = Arrays.stream(full.getRGB(0, 0, 24, 24, null, 0, 24)).filter(p -> p != 0).count();
			check(fullPixels == Arrays.stream(glyph.pixels).filter(p -> p != 0).count(), "Clipped glyph " + cp);
		}
		Localization.resetLocalizations();
		Locale zh = Locale.forLanguageTag("zh-CN");
		Localization.addLocale(Locale.US, new Localization.LocaleInformation(Locale.US, "English", "US"));
		Localization.addLocale(zh, new Localization.LocaleInformation(zh, info.getString("name"), info.getString("region")));
		Localization.addLocalization(Locale.US, englishText);
		Localization.addLocalization(zh, chineseText);
		Localization.changeLanguage("zh-CN");
		check(Localization.getSelectedLanguage().name.equals("Chinese"), "Selected Chinese locale");
		check(Localization.getLocalized("minicraft.displays.title.play").equals("开始游戏"), "Chinese menu text");
		check(Localization.getLocalized("minicraft.displays.quests.quest_info.display.progress", 1, 3).equals("进度：(1/3)"), "Runtime format");
		check(Localization.getLocalizedOrDefault("missing.book", "custom book").equals("custom book"), "Resource-pack fallback");
		check(Localization.getLocalizedOrDefault("minicraft.books.instructions", "English").contains("默认操作"), "Localized book");
		Localization.changeLanguage("en-US");
		check(Localization.getLocalized("minicraft.displays.title.play").equals("Play"), "Switch back to English");
		check(Localization.getLocalizedOrDefault("minicraft.books.instructions", "custom book").equals("custom book"), "English book fallback");
		Localization.changeLanguage("zh-CN");

		check(Font.textWidth("中文ABC") == 40, "Mixed text width");
		check(Font.textWidth(Color.RED_CODE + "中文") == 16, "Color codes must have no width");
		check(Font.textWidth("\uD840\uDC00中") == 16, "Supplementary character width");
		check(Arrays.equals(Font.getLines("中文测试换行", 16, 100, 0), new String[] {"中文", "测试", "换行"}), "Chinese wrapping");
		check(Arrays.equals(Font.getLines("one two three", 56, 100, 0), new String[] {"one two", "three"}), "English word wrapping");
		check(Arrays.equals(Font.getLines("中\n\n文", 16, 100, 0), new String[] {"中", "", "文"}), "Explicit blank lines");
		check(Arrays.equals(Font.getLines("\uD840\uDC00中文", 8, 100, 0), new String[] {"\uD840\uDC00", "中", "文"}), "Do not split surrogate pairs");
		String[] page = Font.getLines("中文测试换行", 16, 8, 0, true);
		check(Arrays.equals(page, new String[] {"中文", "测试换行"}), "Book pagination keeps remainder");
		for (String key : chinese.keySet()) {
			for (String line : Font.getLines(chinese.getString(key).replace('\0', '\n'), 256, 100000, 2))
				check(Font.textWidth(line) <= 256, "Text overflows: " + key);
		}
		Tiles.initTileList();
		List<String> missingItems = new ArrayList<>();
		for (Item item : Items.getAll()) {
			if (item.getDisplayName().matches(".*[A-Za-z].*")) missingItems.add(item.getName() + " -> " + item.getDisplayName());
		}
		check(missingItems.isEmpty(), "Untranslated item names: " + missingItems);

		Renderer.spriteLinker.setSprite(SpriteLinker.SpriteType.Gui, "font", new MinicraftImage(ImageIO.read(ChineseLocalizationCheck.class.getResourceAsStream("/assets/textures/gui/font.png"))));
		BufferedImage image = new BufferedImage(Screen.w, Screen.h, BufferedImage.TYPE_INT_RGB);
		Screen screen = new Screen(image);
		screen.clear(0x181C20);
		Font.draw("Chinese (简体中文)", screen, 16, 16, Color.WHITE);
		Font.draw("开始游戏  设置  语言  保存并退出", screen, 16, 36, Color.WHITE);
		Font.draw("工作台  宝石镐  黑曜石骑士", screen, 16, 56, Color.GREEN);
		Font.draw("默认操作：方向键移动，C 攻击。", screen, 16, 76, Color.YELLOW);
		Font.drawColor(Color.RED_CODE + "中文红色 " + Color.GREEN_CODE + "Green 混排", screen, 16, 96);
		screen.setOffset(8, 8);
		Font.draw("坐标偏移测试", screen, 24, 124, Color.CYAN);
		screen.setOffset(0, 0);
		Font.draw("新字动态渲染：龟 龍 龖", screen, 16, 136, Color.WHITE);
		screen.flush();
		MinicraftImage offsetGlyph = UnicodeFont.glyph('坐');
		for (int y = 0; y < 8; y++) for (int x = 0; x < 8; x++) {
			int expected = offsetGlyph.pixels[x + y * 8] == 0 ? 0x181C20 : Color.CYAN & 0xFFFFFF;
			check((image.getRGB(16 + x, 116 + y) & 0xFFFFFF) == expected, "Pixel tint or camera offset mismatch");
		}
		File directory = new File(args[0]);
		check(directory.isDirectory() || directory.mkdirs(), "Create report folder");
		BufferedImage scaled = new BufferedImage(Screen.w * 3, Screen.h * 3, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = scaled.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		g.drawImage(image, 0, 0, scaled.getWidth(), scaled.getHeight(), null);
		g.dispose();
		ImageIO.write(scaled, "png", new File(directory, "pixel-font.png"));
		minicraft.screen.ChineseMenuCheck.run(directory);
		System.out.println("Chinese checks passed: " + english.length() + " base keys, " + chinese.length() + " translations, " + characters.size() + " glyphs, " + Items.getAll().size() + " items; switching, formatting, wrapping, pagination and rendering.");
	}
}
