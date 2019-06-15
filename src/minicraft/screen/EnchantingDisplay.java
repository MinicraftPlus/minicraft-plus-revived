package minicraft.screen;

import minicraft.core.Game;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Sound;
import minicraft.entity.mob.Player;
import minicraft.gfx.Point;
import minicraft.gfx.Screen;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.screen.entry.*;

import java.util.ArrayList;

public class EnchantingDisplay extends Display {

    private Player player;
    private Item[] tools;

    private ItemListMenu toolList;
    private Menu.Builder choiceBox;

    private Menu.Builder selectedItemBox;
    private Item selectedItem;

    public EnchantingDisplay(String name, Player player) {
        this.player = player;

        tools = new Item[this.player.getInventory().invSize()];

        int numTools = 0;
        for (int i = 0; i < this.player.getInventory().invSize(); i++) {
            if (this.player.getInventory().get(i) instanceof ToolItem) {
                tools[numTools] = this.player.getInventory().get(i);
                numTools++;
            }
        }

        ItemEntry[] entries = new ItemEntry[numTools];
        for (int i = 0; i < numTools; i++) {
            entries[i] = new ItemEntry(tools[i]);
        }

        toolList = new ItemListMenu(entries, name);

        selectedItemBox = new Menu.Builder(true, 3, RelPos.TOP)
                .setEntries(new StringEntry("    "))
                .setPositioning(new Point(196, 21), RelPos.CENTER)
                .setSelectable(false);

        selectedItem = null;


        ArrayList<ListEntry> choices = new ArrayList<>();
        choices.add(new SelectEntry("Enchant",() -> enchantItem()));
        choices.add(new SelectEntry("Cancel", () -> selectedItem = null));
        choiceBox = new Menu.Builder(true, 3, RelPos.CENTER)
                .setEntries(choices)
                .setPositioning(new Point(172, 79), RelPos.RIGHT);

        menus = new Menu[]{ toolList, selectedItemBox.createMenu(), choiceBox.createMenu() };
    }

    public void enchantItem() {
        ToolItem tool = ((ToolItem) selectedItem);

        if (tool.ench == 0 && player.getInventory().count(Items.get("cloud dust")) >= 5) {
            player.getInventory().removeItems(Items.get("cloud dust"), 5);
            player.getInventory().removeItem(selectedItem);
            tool.ench = 1;
            player.getInventory().add(tool);
            Game.exitMenu();
        } else {
            Sound.monsterHurt.play();
        }
    }

    @Override
    public void tick(InputHandler input) {
        super.tick(input);

        if (input.getKey("menu").clicked) {
            Game.exitMenu();
            return;
        }

        if (input.getKey("select").clicked && selection == 0) {
            selectedItem = tools[toolList.getSelection()];
            Sound.select.play();
        }
    }

    @Override
    public void render(Screen screen) {
        super.render(screen);

        if (selectedItem != null) {
            selectedItem.sprite.render(screen, 192, 17);
        }
    }
}
