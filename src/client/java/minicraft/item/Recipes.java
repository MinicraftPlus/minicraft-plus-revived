package minicraft.item;

import minicraft.entity.furniture.Bed;
import minicraft.saveload.Save;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Function;

public class Recipes {

	public static final ArrayList<Recipe> anvilRecipes = new ArrayList<>();
	public static final ArrayList<Recipe> ovenRecipes = new ArrayList<>();
	public static final ArrayList<Recipe> furnaceRecipes = new ArrayList<>();
	public static final ArrayList<Recipe> workbenchRecipes = new ArrayList<>();
	public static final ArrayList<Recipe> enchantRecipes = new ArrayList<>();
	public static final ArrayList<Recipe> craftRecipes = new ArrayList<>();
	public static final ArrayList<Recipe> loomRecipes = new ArrayList<>();

	static {
		craftRecipes.add(new Recipe("Workbench_1", "Wood_10"));
		craftRecipes.add(new Recipe("Torch_2", "Wood_1", "coal_1"));
		craftRecipes.add(new Recipe("plank_2", "Wood_1"));
		craftRecipes.add(new Recipe("Plank Wall_1", "plank_3"));
		craftRecipes.add(new Recipe("Wood Door_1", "plank_5"));
		craftRecipes.add(new Recipe("Wood Fence_1", "plank_3"));

		workbenchRecipes.add(new Recipe("Workbench_1", "Wood_10"));
		workbenchRecipes.add(new Recipe("Torch_2", "Wood_1", "coal_1"));
		workbenchRecipes.add(new Recipe("plank_2", "Wood_1"));
		workbenchRecipes.add(new Recipe("Plank Wall_1", "plank_3"));
		workbenchRecipes.add(new Recipe("Wood Door_1", "plank_5"));
		workbenchRecipes.add(new Recipe("Wood Fence_1", "plank_3"));
		workbenchRecipes.add(new Recipe("Lantern_1", "Wood_8", "slime_4", "glass_3"));
		workbenchRecipes.add(new Recipe("Stone Brick_1", "Stone_2"));
		workbenchRecipes.add(new Recipe("Ornate Stone_1", "Stone_2"));
		workbenchRecipes.add(new Recipe("Stone Wall_1", "Stone Brick_3"));
		workbenchRecipes.add(new Recipe("Stone Door_1", "Stone Brick_5"));
		workbenchRecipes.add(new Recipe("Stone Fence_1", "Stone Brick_3"));
		workbenchRecipes.add(new Recipe("Obsidian Brick_1", "Raw Obsidian_2"));
		workbenchRecipes.add(new Recipe("Ornate Obsidian_1", "Raw Obsidian_2"));
		workbenchRecipes.add(new Recipe("Obsidian Wall_1", "Obsidian Brick_3"));
		workbenchRecipes.add(new Recipe("Obsidian Door_1", "Obsidian Brick_5"));
		workbenchRecipes.add(new Recipe("Obsidian Fence_1", "Obsidian Brick_3"));
		workbenchRecipes.add(new Recipe("Oven_1", "Stone_15"));
		workbenchRecipes.add(new Recipe("Furnace_1", "Stone_20"));
		workbenchRecipes.add(new Recipe("Enchanter_1", "Wood_5", "String_2", "Lapis_10"));
		workbenchRecipes.add(new Recipe("Chest_1", "Wood_20"));
		workbenchRecipes.add(new Recipe("Anvil_1", "iron_5"));
		workbenchRecipes.add(new Recipe("Tnt_1", "Gunpowder_10", "Sand_8"));
		workbenchRecipes.add(new Recipe("Loom_1", "Wood_10", "Wool_5"));
		workbenchRecipes.add(new Recipe("Wood Fishing Rod_1", "Wood_10", "String_3"));
		workbenchRecipes.add(new Recipe("Iron Fishing Rod_1", "Iron_10", "String_3"));
		workbenchRecipes.add(new Recipe("Gold Fishing Rod_1", "Gold_10", "String_3"));
		workbenchRecipes.add(new Recipe("Gem Fishing Rod_1", "Gem_10", "String_3"));

		workbenchRecipes.add(new Recipe("Wood Sword_1", "Wood_5"));
		workbenchRecipes.add(new Recipe("Wood Axe_1", "Wood_5"));
		workbenchRecipes.add(new Recipe("Wood Hoe_1", "Wood_5"));
		workbenchRecipes.add(new Recipe("Wood Pickaxe_1", "Wood_5"));
		workbenchRecipes.add(new Recipe("Wood Shovel_1", "Wood_5"));
		workbenchRecipes.add(new Recipe("Wood Bow_1", "Wood_5", "string_2"));
		workbenchRecipes.add(new Recipe("Rock Sword_1", "Wood_5", "Stone_5"));
		workbenchRecipes.add(new Recipe("Rock Axe_1", "Wood_5", "Stone_5"));
		workbenchRecipes.add(new Recipe("Rock Hoe_1", "Wood_5", "Stone_5"));
		workbenchRecipes.add(new Recipe("Rock Pickaxe_1", "Wood_5", "Stone_5"));
		workbenchRecipes.add(new Recipe("Rock Shovel_1", "Wood_5", "Stone_5"));
		workbenchRecipes.add(new Recipe("Rock Bow_1", "Wood_5", "Stone_5", "string_2"));

		workbenchRecipes.add(new Recipe("arrow_3", "Wood_2", "Stone_2"));
		workbenchRecipes.add(new Recipe("Leather Armor_1", "leather_10"));
		workbenchRecipes.add(new Recipe("Snake Armor_1", "scale_15"));

		loomRecipes.add(new Recipe("String_2", "Wool_1"));
		loomRecipes.add(new Recipe("red wool_1", "Wool_1", "rose_1"));
		loomRecipes.add(new Recipe("blue wool_1", "Wool_1", "Lapis_1"));
		loomRecipes.add(new Recipe("green wool_1", "Wool_1", "Cactus_1"));
		loomRecipes.add(new Recipe("yellow wool_1", "Wool_1", "Flower_1"));
		loomRecipes.add(new Recipe("black wool_1", "Wool_1", "coal_1"));
		loomRecipes.add(new Recipe("Bed_1", "Wood_5", "Wool_3"));

		loomRecipes.add(new Recipe("blue clothes_1", "cloth_5", "Lapis_1"));
		loomRecipes.add(new Recipe("green clothes_1", "cloth_5", "Cactus_1"));
		loomRecipes.add(new Recipe("yellow clothes_1", "cloth_5", "Flower_1"));
		loomRecipes.add(new Recipe("black clothes_1", "cloth_5", "coal_1"));
		loomRecipes.add(new Recipe("orange clothes_1", "cloth_5", "rose_1", "Flower_1"));
		loomRecipes.add(new Recipe("purple clothes_1", "cloth_5", "Lapis_1", "rose_1"));
		loomRecipes.add(new Recipe("cyan clothes_1", "cloth_5", "Lapis_1", "Cactus_1"));
		loomRecipes.add(new Recipe("reg clothes_1", "cloth_5"));

		loomRecipes.add(new Recipe("Leather Armor_1", "leather_10"));

		anvilRecipes.add(new Recipe("Iron Armor_1", "iron_10"));
		anvilRecipes.add(new Recipe("Gold Armor_1", "gold_10"));
		anvilRecipes.add(new Recipe("Gem Armor_1", "gem_65"));
		anvilRecipes.add(new Recipe("Empty Bucket_1", "iron_5"));
		anvilRecipes.add(new Recipe("Iron Lantern_1", "iron_8", "slime_5", "glass_4"));
		anvilRecipes.add(new Recipe("Gold Lantern_1", "gold_10", "slime_5", "glass_4"));
		anvilRecipes.add(new Recipe("Iron Sword_1", "Wood_5", "iron_5"));
		anvilRecipes.add(new Recipe("Iron Claymore_1", "Iron Sword_1", "shard_15"));
		anvilRecipes.add(new Recipe("Iron Axe_1", "Wood_5", "iron_5"));
		anvilRecipes.add(new Recipe("Iron Hoe_1", "Wood_5", "iron_5"));
		anvilRecipes.add(new Recipe("Iron Pickaxe_1", "Wood_5", "iron_5"));
		anvilRecipes.add(new Recipe("Iron Shovel_1", "Wood_5", "iron_5"));
		anvilRecipes.add(new Recipe("Iron Bow_1", "Wood_5", "iron_5", "string_2"));
		anvilRecipes.add(new Recipe("Gold Sword_1", "Wood_5", "gold_5"));
		anvilRecipes.add(new Recipe("Gold Claymore_1", "Gold Sword_1", "shard_15"));
		anvilRecipes.add(new Recipe("Gold Axe_1", "Wood_5", "gold_5"));
		anvilRecipes.add(new Recipe("Gold Hoe_1", "Wood_5", "gold_5"));
		anvilRecipes.add(new Recipe("Gold Pickaxe_1", "Wood_5", "gold_5"));
		anvilRecipes.add(new Recipe("Gold Shovel_1", "Wood_5", "gold_5"));
		anvilRecipes.add(new Recipe("Gold Bow_1", "Wood_5", "gold_5", "string_2"));
		anvilRecipes.add(new Recipe("Gem Sword_1", "Wood_5", "gem_50"));
		anvilRecipes.add(new Recipe("Gem Claymore_1", "Gem Sword_1", "shard_15"));
		anvilRecipes.add(new Recipe("Gem Axe_1", "Wood_5", "gem_50"));
		anvilRecipes.add(new Recipe("Gem Hoe_1", "Wood_5", "gem_50"));
		anvilRecipes.add(new Recipe("Gem Pickaxe_1", "Wood_5", "gem_50"));
		anvilRecipes.add(new Recipe("Gem Shovel_1", "Wood_5", "gem_50"));
		anvilRecipes.add(new Recipe("Gem Bow_1", "Wood_5", "gem_50", "string_2"));
		anvilRecipes.add(new Recipe("Shears_1", "Iron_4"));
		anvilRecipes.add(new Recipe("Watering Can_1", "Iron_3"));

		furnaceRecipes.add(new Recipe("iron_1", "iron Ore_3", "coal_1"));
		furnaceRecipes.add(new Recipe("gold_1", "gold Ore_3", "coal_1"));
		furnaceRecipes.add(new Recipe("glass_1", "sand_4", "coal_1"));
		furnaceRecipes.add(new Recipe("glass bottle_1", "glass_3"));

		ovenRecipes.add(new Recipe("cooked pork_1", "raw pork_1", "coal_1"));
		ovenRecipes.add(new Recipe("steak_1", "raw beef_1", "coal_1"));
		ovenRecipes.add(new Recipe("cooked fish_1", "raw fish_1", "coal_1"));
		ovenRecipes.add(new Recipe("bread_1", "wheat_4"));
		ovenRecipes.add(new Recipe("Baked Potato_1", "Potato_1"));

		enchantRecipes.add(new Recipe("Gold Apple_1", "apple_1", "gold_8"));
		enchantRecipes.add(new Recipe("awkward potion_1", "glass bottle_1", "Lapis_3"));
		enchantRecipes.add(new Recipe("speed potion_1", "awkward potion_1", "Cactus_5"));
		enchantRecipes.add(new Recipe("light potion_1", "awkward potion_1", "slime_5"));
		enchantRecipes.add(new Recipe("swim potion_1", "awkward potion_1", "raw fish_5"));
		enchantRecipes.add(new Recipe("haste potion_1", "awkward potion_1", "Wood_5", "Stone_5"));
		enchantRecipes.add(new Recipe("lava potion_1", "awkward potion_1", "Lava Bucket_1"));
		enchantRecipes.add(new Recipe("energy potion_1", "awkward potion_1", "gem_25"));
		enchantRecipes.add(new Recipe("regen potion_1", "awkward potion_1", "Gold Apple_1"));
		enchantRecipes.add(new Recipe("Health Potion_1", "awkward potion_1", "Gunpowder_2", "Leather Armor_1"));
		enchantRecipes.add(new Recipe("Escape Potion_1", "awkward potion_1", "Gunpowder_3", "Lapis_7"));
		enchantRecipes.add(new Recipe("Totem of Air_1", "gold_10", "gem_10", "Lapis_5", "Cloud Ore_5"));
		enchantRecipes.add(new Recipe("Obsidian Poppet_1", "gold_10", "gem_10", "Lapis_5", "Shard_15"));
		enchantRecipes.add(new Recipe("Arcane Fertilizer_3", "Lapis_6", "Bone_2"));
	}

	/**
	 * This regenerates the recipes.json at once by the recipes above.
	 * Remind that it is recommended to use String Manipulation plugin to resort (JSON Sort) the file if using IntelliJ IDEA.
	 */
	public static void main(String[] args) {
		HashSet<Recipe> recipes = new HashSet<>();
		recipes.addAll(anvilRecipes);
		recipes.addAll(ovenRecipes);
		recipes.addAll(furnaceRecipes);
		recipes.addAll(workbenchRecipes);
		recipes.addAll(enchantRecipes);
		recipes.addAll(craftRecipes);
		recipes.addAll(loomRecipes);
		HashMap<String, Recipe> recipeMap = new HashMap<>();
		HashMap<String, HashSet<Recipe>> duplicatedRecipes = new HashMap<>();
		Function<Item, String> itemNameFixer = item -> {
			String name = item.getName();
			return (name.equalsIgnoreCase("gold apple") ? name.replaceAll("(?i)gold", "golden") :
				item instanceof ToolItem ? name.replaceAll("(?i)wood", "wooden").replaceAll("(?i)rock", "stone") : name)
				.toLowerCase().replace(' ', '_');
		};
		Function<Recipe, String> recipeNameFixer = recipe -> { // This is applied when duplication occurs.
			Item item = recipe.getProduct();
			String name = itemNameFixer.apply(item);
			/*if (item instanceof DyeItem) { TODO
				Map<String, Integer> costs = recipe.getCosts();
				if (costs.size() == 2 && costs.containsKey("WHITE DYE"))
					return name + "_from_white_dye";
				return name;
			} else*/ if (item instanceof FurnitureItem && ((FurnitureItem) item).furniture instanceof Bed) {
				if (recipe.getCosts().containsKey("WHITE BED"))
					return name + "_from_white_bed";
				return name;
			}
			return name;
		};
		for (Recipe recipe : recipes) {
			if (recipes.stream().anyMatch(r -> r != recipe && r.getProduct().equals(recipe.getProduct()))) {
				if (recipes.stream().anyMatch(r -> r != recipe && recipeNameFixer.apply(r).equals(recipeNameFixer.apply(recipe)))) {
					duplicatedRecipes.compute(recipeNameFixer.apply(recipe), (k, v) -> {
						if (v == null) return new HashSet<>(Collections.singletonList(recipe));
						else {
							v.add(recipe);
							return v;
						}
					});
				} else {
					recipeMap.put(recipeNameFixer.apply(recipe), recipe);
				}
			} else {
				recipeMap.put(itemNameFixer.apply(recipe.getProduct()), recipe);
			}
		}
		for (String key : duplicatedRecipes.keySet()) {
			HashSet<Recipe> duplications = duplicatedRecipes.get(key);
			HashMap<Recipe, JTextField> inputs = new HashMap<>();
			JDialog dialog = new JDialog((Frame) null, "Recipe Duplication: " + key);
			dialog.setLayout(new BorderLayout());
			dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
			dialog.addWindowListener(new WindowListener() {
				@Override
				public void windowOpened(WindowEvent e) {}
				@Override
				public void windowClosing(WindowEvent e) {
					dialog.setVisible(false);
					JOptionPane.showMessageDialog(null, "Exit Program");
					dialog.dispose();
					System.exit(0);
				}
				@Override
				public void windowClosed(WindowEvent e) {}
				@Override
				public void windowIconified(WindowEvent e) {}
				@Override
				public void windowDeiconified(WindowEvent e) {}
				@Override
				public void windowActivated(WindowEvent e) {}
				@Override
				public void windowDeactivated(WindowEvent e) {}
			});

			JPanel inputPanel = new JPanel(new SpringLayout());
			inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			for (Recipe recipe : duplications) {
				JLabel l = new JLabel(recipe.toString(), JLabel.TRAILING);
				inputPanel.add(l);
				JTextField textField = new JTextField(key, 20);
				l.setLabelFor(textField);
				inputPanel.add(textField);
				inputs.put(recipe, textField);
			}

			makeCompactGrid(inputPanel,
				duplications.size(), 2, //rows, cols
				6, 6,        //initX, initY
				6, 6);       //xPad, yPad

			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
			buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
			buttonPane.add(Box.createHorizontalGlue());
			JButton doneButton = new JButton("Done");
			doneButton.addActionListener(e -> {
				inputs.forEach((recipe, text) -> recipeMap.put(text.getText(), recipe));
				dialog.setVisible(false);
				dialog.dispose();
			});
			doneButton.setDefaultCapable(true);
			buttonPane.add(doneButton);
			buttonPane.add(Box.createHorizontalGlue());

			class JMultilineLabel extends JTextArea { // Reference: https://stackoverflow.com/a/11034405
				private static final long serialVersionUID = 1L;
				public JMultilineLabel(String text){
					super(text); // According to Mr. Polywhirl, this might improve it -> text + System.lineSeparator()
					setEditable(false);
					setCursor(null);
					setOpaque(false);
					setFocusable(false);
					setFont(UIManager.getFont("Label.font"));
					setWrapStyleWord(true);
					setLineWrap(true);
					//According to Mariana this might improve it
					setBorder(new EmptyBorder(5, 5, 5, 5));
					setAlignmentY(JLabel.CENTER_ALIGNMENT);
				}
			}

			Container dialogPane = dialog.getContentPane();
			dialogPane.add(new JMultilineLabel("Recipes:\n" +
					String.join("\n", duplicatedRecipes.get(key).stream().<CharSequence>map(Recipe::toString)::iterator)),
				BorderLayout.NORTH);
			dialogPane.add(inputPanel, BorderLayout.CENTER);
			dialogPane.add(buttonPane, BorderLayout.SOUTH);
			dialog.pack();
			dialog.setLocationRelativeTo(null);
			dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
			dialog.setVisible(true);
		}

		JSONObject json = new JSONObject();
		for (String key : recipeMap.keySet()) {
			Recipe recipe = recipeMap.get(key);
			JSONObject recipeUnlockingJson = new JSONObject();

			ArrayList<String> costs = new ArrayList<>();
			JSONObject criteriaJson = new JSONObject();
			Map<String, Integer> costMap = recipe.getCosts();
			for (String itemKey : costMap.keySet()) {
				Item item = Items.get(itemKey);
				JSONObject criterionJson = new JSONObject();

				criterionJson.put("trigger", "inventory_changed");

				JSONObject conditionsJson = new JSONObject();
				JSONArray itemConditionsJsonArray = new JSONArray();
				JSONObject itemConditionsJson = new JSONObject();
				JSONArray itemsJson = new JSONArray();
				itemsJson.put(item.getName());
				itemConditionsJson.put("items", itemsJson);
				itemConditionsJsonArray.put(itemConditionsJson);
				conditionsJson.put("items", itemConditionsJsonArray);
				criterionJson.put("conditions", conditionsJson);
				criteriaJson.put("has_" + itemNameFixer.apply(item), criterionJson);

				costs.add(item.getName() + "_" + costMap.get(itemKey));
			}
			recipeUnlockingJson.put("criteria", criteriaJson);

			JSONArray requirementsJson = new JSONArray();
			JSONArray criterionNamesJson = new JSONArray();
			criterionNamesJson.putAll(criteriaJson.keySet());
			requirementsJson.put(criterionNamesJson);
			recipeUnlockingJson.put("requirements", requirementsJson);

			JSONObject rewardsJson = new JSONObject();
			JSONObject recipesJson = new JSONObject();
			JSONArray costsJson = new JSONArray();
			costsJson.putAll(costs);
			recipesJson.put(recipe.getProduct().getName() + "_" + recipe.getAmount(), costsJson);
			rewardsJson.put("recipes", recipesJson);
			recipeUnlockingJson.put("rewards", rewardsJson);

			json.put("minicraft.advancements.recipes." + key, recipeUnlockingJson);
		}

		try {
			Save.writeJSONToFile(new File(System.getProperty("user.dir"), "src/client/resources/resources/recipes.json").toString(),
				json.toString(2));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/* Used by makeCompactGrid. */
	private static SpringLayout.Constraints getConstraintsForCell(
		int row, int col,
		Container parent,
		int cols) {
		SpringLayout layout = (SpringLayout) parent.getLayout();
		Component c = parent.getComponent(row * cols + col);
		return layout.getConstraints(c);
	}

	/*
	 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
	 *
	 * Redistribution and use in source and binary forms, with or without
	 * modification, are permitted provided that the following conditions
	 * are met:
	 *
	 *   - Redistributions of source code must retain the above copyright
	 *     notice, this list of conditions and the following disclaimer.
	 *
	 *   - Redistributions in binary form must reproduce the above copyright
	 *     notice, this list of conditions and the following disclaimer in the
	 *     documentation and/or other materials provided with the distribution.
	 *
	 *   - Neither the name of Oracle or the names of its
	 *     contributors may be used to endorse or promote products derived
	 *     from this software without specific prior written permission.
	 *
	 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
	 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
	 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
	 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
	 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
	 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
	 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
	 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
	 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
	 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
	 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
	 */

	/**
	 * Aligns the first <code>rows</code> * <code>cols</code>
	 * components of <code>parent</code> in
	 * a grid. Each component in a column is as wide as the maximum
	 * preferred width of the components in that column;
	 * height is similarly determined for each row.
	 * The parent is made just big enough to fit them all.
	 *
	 * @param rows number of rows
	 * @param cols number of columns
	 * @param initialX x location to start the grid at
	 * @param initialY y location to start the grid at
	 * @param xPad x padding between cells
	 * @param yPad y padding between cells
	 */
	@SuppressWarnings("SameParameterValue")
	private static void makeCompactGrid(Container parent,
										int rows, int cols,
										int initialX, int initialY,
										int xPad, int yPad) {
		SpringLayout layout;
		try {
			layout = (SpringLayout)parent.getLayout();
		} catch (ClassCastException exc) {
			System.err.println("The first argument to makeCompactGrid must use SpringLayout.");
			return;
		}

		//Align all cells in each column and make them the same width.
		Spring x = Spring.constant(initialX);
		for (int c = 0; c < cols; c++) {
			Spring width = Spring.constant(0);
			for (int r = 0; r < rows; r++) {
				width = Spring.max(width,
					getConstraintsForCell(r, c, parent, cols).
						getWidth());
			}
			for (int r = 0; r < rows; r++) {
				SpringLayout.Constraints constraints =
					getConstraintsForCell(r, c, parent, cols);
				constraints.setX(x);
				constraints.setWidth(width);
			}
			x = Spring.sum(x, Spring.sum(width, Spring.constant(xPad)));
		}

		//Align all cells in each row and make them the same height.
		Spring y = Spring.constant(initialY);
		for (int r = 0; r < rows; r++) {
			Spring height = Spring.constant(0);
			for (int c = 0; c < cols; c++) {
				height = Spring.max(height,
					getConstraintsForCell(r, c, parent, cols).
						getHeight());
			}
			for (int c = 0; c < cols; c++) {
				SpringLayout.Constraints constraints =
					getConstraintsForCell(r, c, parent, cols);
				constraints.setY(y);
				constraints.setHeight(height);
			}
			y = Spring.sum(y, Spring.sum(height, Spring.constant(yPad)));
		}

		//Set the parent's size.
		SpringLayout.Constraints pCons = layout.getConstraints(parent);
		pCons.setConstraint(SpringLayout.SOUTH, y);
		pCons.setConstraint(SpringLayout.EAST, x);
	}
}
