package minicraft.core;

import minicraft.item.Recipe;
import minicraft.item.Recipes;

import java.util.ArrayList;
import java.util.List;

public class UnlockableRecipes {
    public final ArrayList<UnlockableRecipe> recipes = new ArrayList<UnlockableRecipe>();

    public UnlockableRecipes() {
        recipes.add(
            new UnlockableRecipe(
                    "Boat",
                    RecipeType.CRAFT,
                    new Recipe(
                            "Boat_1",
                            "Wood_5"
                    )
            )
        );
    }

    public UnlockableRecipe getRecipe(String name) {
        for (UnlockableRecipe r : recipes)
            if (r.name.equalsIgnoreCase(name))
                return r;

        return null;
    }

    private static List<Recipe> getRecipeList(RecipeType type) {
        switch (type) {
            case ANVIL:
                return Recipes.anvilRecipes;
            case OVEN:
                return Recipes.ovenRecipes;
            case FURNACE:
                return Recipes.furnaceRecipes;
            case WORKBENCH:
                return Recipes.workbenchRecipes;
            case ENCHANT:
                return Recipes.enchantRecipes;
            case CRAFT:
                return Recipes.craftRecipes;
            case LOOM:
                return Recipes.loomRecipes;
        }

        return null;
    }

    public enum RecipeType {
        ANVIL,
        OVEN,
        FURNACE,
        WORKBENCH,
        ENCHANT,
        CRAFT,
        LOOM
    }

    public class UnlockableRecipe {
        public String name;
        public boolean unlocked;
        public RecipeType type;
        public Recipe recipe;

        public UnlockableRecipe(String name, RecipeType type, Recipe recipe) {
            this.name = name;
            this.type = type;
            this.recipe = recipe;
        }

        public void unlock() {
            List<Recipe> recipeList = getRecipeList(this.type);
            assert recipeList != null;

            this.unlocked = true;
            recipeList.add(this.recipe);

            // TODO: If networked game, update other players.
        }

        public void lock() {
            List<Recipe> recipeList = getRecipeList(this.type);
            assert recipeList != null;
            this.unlocked = false;
            recipeList.remove(this.recipe);
            // TODO: If networked game, update other players.
        }
    }
}
