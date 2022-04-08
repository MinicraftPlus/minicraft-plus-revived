package minicraft.item;

import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.tinylog.Logger;

import javafx.util.Pair;
import minicraft.core.Game;
import minicraft.saveload.Load;

public class Recipes {
	
	public static final ArrayList<Recipe> anvilRecipes = new ArrayList<>();
	public static final ArrayList<Recipe> ovenRecipes = new ArrayList<>();
	public static final ArrayList<Recipe> furnaceRecipes = new ArrayList<>();
	public static final ArrayList<Recipe> workbenchRecipes = new ArrayList<>();
	public static final ArrayList<Recipe> enchantRecipes = new ArrayList<>();
	public static final ArrayList<Recipe> craftRecipes = new ArrayList<>();
	public static final ArrayList<Recipe> loomRecipes = new ArrayList<>();

	private static List<JSONObject> getRecipesFiles() {
		List<JSONObject> files = new ArrayList<>();

		try {
			URL fUrl = Game.class.getResource("/resources/recipes/");
			if (fUrl == null) {
				Logger.error("Could not find recipes folder.");
				return files;
			}

			Path folderPath = Paths.get(fUrl.toURI());
			DirectoryStream<Path> dir = Files.newDirectoryStream(folderPath);
			for (Path p : dir) {
				String filename = p.getFileName().toString();
				try {
					files.add(Load.loadJsonFile("/resources/recipes/" + filename));
				} catch (StringIndexOutOfBoundsException e) {
					Logger.error("Could not load recipe file with path: {}", p);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return files;
		}

		return files;
	}

	public static void load() {
		List<JSONObject> files = getRecipesFiles();
		craftRecipes.clear();
		workbenchRecipes.clear();
		loomRecipes.clear();
		enchantRecipes.clear();
		ovenRecipes.clear();
		furnaceRecipes.clear();
		anvilRecipes.clear();

		for (JSONObject file : files) {
			try {
				JSONArray forR = file.getJSONArray("for");
				JSONArray requires = file.getJSONArray("requires");
				JSONObject product = file.getJSONObject("product");

				String createdItem = product.getString("item");
				int amount = product.optInt("amount", 1);

				List<Pair<String, Integer>> reqItems = new ArrayList<>();
				for (int i = 0; i < requires.length(); i++) {
					JSONObject r = requires.getJSONObject(i);
					String item = r.getString("item");
					int itemA = r.optInt("amount", 1);

					reqItems.add(new Pair<String, Integer>(item, itemA));
				}

				Recipe recipe = new Recipe(createdItem, amount, reqItems);

				for (int i = 0; i < forR.length(); i++) {
					String name = forR.getString(i);
					if (name.equals("craft")) {
						craftRecipes.add(recipe);
					} else if (name.equals("workbench")) {
						workbenchRecipes.add(recipe);
					} else if (name.equals("loom")) {
						loomRecipes.add(recipe);
					} else if (name.equals("enchant")) {
						enchantRecipes.add(recipe);
					} else if (name.equals("oven")) {
						ovenRecipes.add(recipe);
					} else if (name.equals("furnace")) {
						furnaceRecipes.add(recipe);
					} else if (name.equals("anvil")) {
						anvilRecipes.add(recipe);
					}
				}
			} catch (Exception e) {
				throw e;
			}
		}

		Logger.info("Craft Recipes: {}", craftRecipes.size());
		Logger.info("Workbench Recipes: {}", workbenchRecipes.size());
		Logger.info("Loom Recipes: {}", loomRecipes.size());
		Logger.info("Anvil Recipes: {}", anvilRecipes.size());
		Logger.info("Oven Recipes: {}", ovenRecipes.size());
		Logger.info("Furnace Recipes: {}", furnaceRecipes.size());
		Logger.info("Enchant Recipes: {}", enchantRecipes.size());
	}
}
