package minicraft.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

class FileHandler extends Game {
	private FileHandler() {}
	
	static final String OS;
	static final String localGameDir;
	static final String systemGameDir;
	
	static {
		OS = System.getProperty("os.name").toLowerCase();
		//System.out.println("os name: \"" +os + "\"");
		if(OS.contains("windows")) // windows
			systemGameDir = System.getenv("APPDATA");
		else
			systemGameDir = System.getProperty("user.home");
		
		if(OS.contains("mac") || OS.contains("nix") || OS.contains("nux") || OS.contains("aix")) // mac or linux
			localGameDir = "/.playminicraft/mods/Minicraft_Plus";
		else
			localGameDir = "/playminicraft/mods/Minicraft_Plus"; // windows, probably.
		
		//System.out.println("system game dir: " + systemGameDir);
	}
	
	
	static void determineGameDir(String saveDir) {
		gameDir = saveDir + localGameDir;
		if(debug) System.out.println("determined gameDir: " + gameDir);
		
		String prevLocalGameDir = "/.playminicraft/mods/Minicraft Plus";
		File testFile = new File(systemGameDir + localGameDir);
		File testFileOld = new File(systemGameDir + prevLocalGameDir);
		if(!testFile.exists() && testFileOld.exists()) {
			// rename the old folders to the new scheme
			testFile.mkdirs();
			if(OS.contains("windows")) {
				try {
					java.nio.file.Files.setAttribute(testFile.toPath(), "dos:hidden", true);
				} catch (java.io.IOException ex) {
					System.err.println("couldn't make game folder hidden on windows:");
					ex.printStackTrace();
				}
			}
			
			File[] files = getAllFiles(testFileOld).toArray(new File[0]);
			for(File file: files) {
				File newFile = new File(file.getPath().replace(testFileOld.getPath(), testFile.getPath()));
				if(file.isDirectory()) newFile.mkdirs(); // these should be unnecessary.
				else file.renameTo(newFile);
			}
			
			deleteAllFiles(testFileOld);
			
			testFile = new File(systemGameDir + ".playminicraft");
			if(OS.contains("windows") && testFile.exists())
				deleteAllFiles(testFile);
		}
	}
	
	private static List<File> getAllFiles(File top) {
		List<File> files = new ArrayList<File>();
		if(top == null) {
			System.err.println("GAME: cannot search files of null folder.");
			return files;
		}
		if(!top.isDirectory()) {
			files.add(top);
			return files;
		} else
			files.add(top);
		
		File[] subfiles = top.listFiles();
		if(subfiles != null)
			for(File subfile: subfiles)
				files.addAll(getAllFiles(subfile));
		
		return files;
	}
	
	private static void deleteAllFiles(File top) {
		if(top == null) return;
		if(top.isDirectory()) {
			File[] subfiles = top.listFiles();
			if(subfiles != null)
				for (File subfile : subfiles)
					deleteAllFiles(subfile);
		}
		//noinspection ResultOfMethodCallIgnored
		top.delete();
	}
	
}
