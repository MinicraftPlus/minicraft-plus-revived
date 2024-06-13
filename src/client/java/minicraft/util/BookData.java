/*
 * SPDX-FileCopyrightText: 2011-2024 Minicraft+ Developers and Contributors
 * SPDX-License-Identifier: GPL-3.0-only
 */

package minicraft.util;

import minicraft.item.BookItem;

public class BookData {

	public static BookItem.BookContent about;
	public static BookItem.BookContent credits;
	public static BookItem.BookContent instructions;
	public static BookItem.BookContent antVenomBook;
	public static BookItem.BookContent storylineGuide;

	public static void resetBooks() {
		about = null;
		credits = null;
		instructions = null;
		antVenomBook = null;
		storylineGuide = null;
	}

	public static String loadBook(String content) {
		return content.replaceAll("\\\\0", "\0");
	}

	public static void saveBook(String bookTitle) {

	}
}
