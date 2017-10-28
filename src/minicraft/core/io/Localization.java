package minicraft.core.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import minicraft.core.Game;

import org.jetbrains.annotations.NotNull;

public class Localization {
	
	public static HashMap<String, String> localization = new HashMap<>();
	public static String selectedLanguage = "english";
	public static boolean isLoaded = false;
	
	@NotNull
	public static String getLocalized(String string) {
		if (!isLoaded) loadSelectedLanguageFile();
		
		String localString = localization.get(string);
		
		if (localString == null)
			System.out.println("The string @" + string + "@ is not localized, returning itself instead.");
		
		return (localString == null ? string : localString);
	}
	
	public static void changeLanguage(String newLanguage) {
		isLoaded = false;
		selectedLanguage = newLanguage;
		loadSelectedLanguageFile();
	}
	
	public static void loadSelectedLanguageFile() {
		String fileText = getFileAsString();
		
		String currentKey = "";
		
		for (String line : fileText.split(System.lineSeparator())) {
			// # at the start of a line means the line is a comment.
			if (line.startsWith("#")) continue;
			if (isEmptyOrWhitespace(line)) continue;
			
			if (currentKey.equals("")) {
				currentKey = line;
			} else {
				localization.put(currentKey, line);
				currentKey = "";
			}
		}
		
		isLoaded = true;
	}
	
	private static String getFileAsString() {
		int character;
		StringBuilder builder = new StringBuilder();
		
		// Using getResourceAsStream since we're publishing this as a jar file.
		try (InputStream fileStream = Game.class.getResourceAsStream("/resources/localization/" + selectedLanguage + ".mcpl");) {
			character = fileStream.read();
			do {
				builder.append((char)character);
				character = fileStream.read();
			} while (character != -1);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return builder.toString();
	}
	
	private static boolean isEmptyOrWhitespace(String string) {
		int whitespaceCount = 0;
		
		for (char c : string.toCharArray())
			if (c == ' ')
				whitespaceCount++;
		
		if (string.isEmpty()) return true;
		if (whitespaceCount >= string.length()) return true;
		
		return false;
	}
}
