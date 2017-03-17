package com.mojang.ld22.gfx;

public class Font {
	private static String chars =
			""
					+ //
					"ABCDEFGHIJKLMNOPQRSTUVWXYZ			"
					+ //
					"0123456789.,!?'\"-+=/\\%()<>:;^@bcdefghijklmnopqrstuvwxyz"; //

	public static void draw(String msg, Screen screen, int x, int y, int col) {
		msg = msg.toUpperCase();
		for (int i = 0; i < msg.length(); i++) {
			int ix = chars.indexOf(msg.charAt(i));
			if (ix >= 0) {
				screen.render(x + i * 8, y, ix + 30 * 32, col, 0);
			}
		}
	}

	public static void renderFrame(Screen screen, String title, int x0, int y0, int x1, int y1) {
		for (int y = y0; y <= y1; y++) {
			for (int x = x0; x <= x1; x++) {
				if (x == x0 && y == y0)
					screen.render(x * 8, y * 8, 0 + 13 * 32, Color.get(-1, 1, 5, 555), 0);
				else if (x == x1 && y == y0)
					screen.render(x * 8, y * 8, 0 + 13 * 32, Color.get(-1, 1, 5, 445), 1);
				else if (x == x0 && y == y1)
					screen.render(x * 8, y * 8, 0 + 13 * 32, Color.get(-1, 1, 5, 445), 2);
				else if (x == x1 && y == y1)
					screen.render(x * 8, y * 8, 0 + 13 * 32, Color.get(-1, 1, 5, 445), 3);
				else if (y == y0) screen.render(x * 8, y * 8, 1 + 13 * 32, Color.get(-1, 1, 5, 445), 0);
				else if (y == y1) screen.render(x * 8, y * 8, 1 + 13 * 32, Color.get(-1, 1, 5, 445), 2);
				else if (x == x0) screen.render(x * 8, y * 8, 2 + 13 * 32, Color.get(-1, 1, 5, 445), 0);
				else if (x == x1) screen.render(x * 8, y * 8, 2 + 13 * 32, Color.get(-1, 1, 5, 445), 1);
				else screen.render(x * 8, y * 8, 2 + 13 * 32, Color.get(5, 5, 5, 5), 1);
			}
		}

		draw(title, screen, x0 * 8 + 8, y0 * 8, Color.get(5, 5, 5, 550));
	}

	public static void rendercraftFrame(Screen screen, String title, int x0, int y0, int x1, int y1) {
		for (int y = y0; y <= y1; y++) {
			for (int x = x0; x <= x1; x++) {
				if (x == x0 && y == y0)
					screen.render(x * 8, y * 8, 0 + 13 * 32, Color.get(-1, 1, 300, 400), 0);
				else if (x == x1 && y == y0)
					screen.render(x * 8, y * 8, 0 + 13 * 32, Color.get(-1, 1, 300, 400), 1);
				else if (x == x0 && y == y1)
					screen.render(x * 8, y * 8, 0 + 13 * 32, Color.get(-1, 1, 300, 400), 2);
				else if (x == x1 && y == y1)
					screen.render(x * 8, y * 8, 0 + 13 * 32, Color.get(-1, 1, 300, 400), 3);
				else if (y == y0) screen.render(x * 8, y * 8, 1 + 13 * 32, Color.get(-1, 1, 300, 400), 0);
				else if (y == y1) screen.render(x * 8, y * 8, 1 + 13 * 32, Color.get(-1, 1, 300, 400), 2);
				else if (x == x0) screen.render(x * 8, y * 8, 2 + 13 * 32, Color.get(-1, 1, 300, 400), 0);
				else if (x == x1) screen.render(x * 8, y * 8, 2 + 13 * 32, Color.get(-1, 1, 300, 400), 1);
				else screen.render(x * 8, y * 8, 2 + 13 * 32, Color.get(300, 300, 300, 300), 1);
			}
		}

		draw(title, screen, x0 * 8 + 8, y0 * 8, Color.get(300, 300, 300, 555));
	}

	public static void renderFrameBook(Screen screen, String title, int x0, int y0, int x1, int y1) {
		for (int y = y0; y <= y1; y++) {
			for (int x = x0; x <= x1; x++) {
				if (x == x0 && y == y0)
					screen.render(x * 8, y * 8, 0 + 13 * 32, Color.get(-1, 1, 554, 554), 0);
				else if (x == x1 && y == y0)
					screen.render(x * 8, y * 8, 0 + 13 * 32, Color.get(-1, 1, 554, 554), 1);
				else if (x == x0 && y == y1)
					screen.render(x * 8, y * 8, 0 + 13 * 32, Color.get(-1, 1, 554, 554), 2);
				else if (x == x1 && y == y1)
					screen.render(x * 8, y * 8, 0 + 13 * 32, Color.get(-1, 1, 554, 554), 3);
				else if (y == y0) screen.render(x * 8, y * 8, 1 + 13 * 32, Color.get(-1, 1, 554, 554), 0);
				else if (y == y1) screen.render(x * 8, y * 8, 1 + 13 * 32, Color.get(-1, 1, 554, 554), 2);
				else if (x == x0) screen.render(x * 8, y * 8, 2 + 13 * 32, Color.get(-1, 1, 554, 554), 0);
				else if (x == x1) screen.render(x * 8, y * 8, 2 + 13 * 32, Color.get(-1, 1, 554, 554), 1);
				else screen.render(x * 8, y * 8, 2 + 13 * 32, Color.get(554, 554, 554, 554), 1);
			}

			draw(title, screen, x0 * 8 + 8, y0 * 8, Color.get(-1, 222, 222, 222));
		}
	}
}
