package com.mojang.ld22.gfx;

public class Color {

	public static int get(int a, int b, int c, int d) {
		return (get(d) << 24) + (get(c) << 16) + (get(b) << 8) + (get(a));
	}

	public static int pixel(int a) {
		return (get(a) << 24) + (get(a) << 16) + (get(a) << 8) + get(a);
	}

	public static int rgb(int red, int green, int blue) {
		boolean rgb = false;
		if (red > 255) {
			red = 255;
		}

		if (green > 255) {
			green = 255;
		}

		if (blue > 255) {
			blue = 255;
		}

		if (red < 50 && red != 0) {
			red = 50;
		}

		if (green < 50 && green != 0) {
			green = 50;
		}

		if (blue < 50 && blue != 0) {
			blue = 50;
		}

		int rgb1 = red / 50 * 100 + green / 50 * 10 + blue / 50;
		return rgb1;
	}

	public static int get(int d) {
		if (d < 0) return 255;
		int r = d / 100 % 10;
		int g = d / 10 % 10;
		int b = d % 10;
		return r * 36 + g * 6 + b;
	}
}
