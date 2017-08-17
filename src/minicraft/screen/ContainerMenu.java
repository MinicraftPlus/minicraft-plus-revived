package minicraft.screen;

import minicraft.Game;
import minicraft.Sound;
import minicraft.entity.Chest;
import minicraft.entity.Inventory;
import minicraft.entity.Player;
import minicraft.gfx.Screen;
import minicraft.gfx.Rectangle;
import minicraft.item.Item;
import minicraft.item.StackableItem;

public class ContainerMenu extends InventoryMenu {
	private Player player; // The player that is looking inside the chest
	private Chest chest;
	//private Inventory container; // The inventory of the chest
	//private int selected = 0; // The selected item
	//private String title; // The title of the chest
	//private String[] oText;
	//private int oSelected; // the old selected option (this is used to temporarily save spots moving from chest to inventory & vice-versa)
	private int window = 0; // currently selected window (player's inventory, or chest's inventory)
	
	private InventoryMenu playerMenu;
	
	public ContainerMenu(Player player, Chest chest) {
		super(chest.inventory, chest.name);
		setFrames(new Frame(chest.name, new Rectangle(1, 1, 18, 11, Rectangle.CORNERS)));
		
		playerMenu = new InventoryMenu(player.inventory, "Inventory");
		//.setFrames(new Frame("Inventory", new Rectangle(19, 1, 15+20, 11, Rectangle.CORNERS)));
		
		this.player = player;
		this.chest = chest;
	}

	public void tick() {
		if (input.getKey("menu").clicked) {
			game.setMenu(null); // If the player selects the "menu" key, then it will exit the chest
			return;
		}
		
		int prevWin = window;
		
		if (input.getKey("left").clicked) //if the left key is pressed, the current window will be of the chest
			window = 0;
		if (input.getKey("right").clicked) //if the right key is pressed, the window will be the player's inventory
			window = 1;
		
		/*if(prevWin != window) {
			// window changed
			int tmp = selected; // temp integer will be the currently selected
			selected = oSelected; // selected will become the oSelected
			oSelected = tmp; // oSelected will become the temp integer (save spot for when you switch)
			
			String[] temp = text;
			text = oText;
			oText = temp;
		}*/
		
		if(window == 0) super.tick();
		else playerMenu.tick();
		
		// get player and container inventory references...again?
		Inventory i = window == 1 ? player.inventory : chest.inventory;
		Inventory i2 = window == 0 ? player.inventory : chest.inventory;
		
		//int len = ; // Size of the main inventory
		
		if(i.invSize() == 0) {
			selected = 0;
			return; // nothing else is to be done.
		}
		
		/*//selection fix
		if (selected < 0) selected = 0;
		if (selected >= len) selected = len - 1;
		
		//selection movement
		if (input.getKey("up").clicked) selected--;
		if (input.getKey("down").clicked) selected++;
		//selection sound effects
		if (input.getKey("up").clicked) Sound.pickup.play();
		if (input.getKey("down").clicked) Sound.pickup.play();
		//selection wrap around
		if (selected < 0) selected += len;
		if (selected >= len) selected -= len;
		*/
		// If the "Attack" key is pressed and the inventory's size is bigger than 0...
		if (input.getKey("attack").clicked || input.getKey("drop-one").clicked) {
			Item toSend = i.get(selected);
			
			//if (Game.debug) System.out.println("selected item: " + toSend);
			
			boolean transferAll = input.getKey("attack").clicked || !(toSend instanceof StackableItem) || ((StackableItem)toSend).count == 1;
			
			if(!transferAll) {
				StackableItem item = (StackableItem) toSend;
				if(!ModeMenu.creative)
					item.count--;
				toSend = item.clone();
				((StackableItem)toSend).count = 1;
				updateSelectedItem();
			}
			
			if(Game.isValidClient()) {
				if(i == chest.inventory) // the player is moving an item from chest to inventory.
					Game.client.removeFromChest(chest, selected, input.getKey("attack").clicked);
				else {
					// the player wants to transfer an item to the chest from their inventory.
					Game.client.addToChest(chest, toSend); // if the other menu is the chest, then we are adding to the chest.
					if(transferAll && !ModeMenu.creative)
						removeSelectedItem(); // the request should never be denied, so remove item immedieately as usual. (from the player's inventory, here)
				}
			} else {
				if(transferAll && !(i == player.inventory && ModeMenu.creative)) // don't remove the item from the inv, if in creative mode and the inv is the player
					removeSelectedItem();
				if(!(i2 == player.inventory && ModeMenu.creative)) { // don't add the item to the other inv, if on creative mode and the other inv is the player
					i2.add(playerMenu.selected, toSend.clone());
					playerMenu.text = InventoryMenu.getItemList(i2);
				}
			}
		}
	}
	
	public void render(Screen screen) {
		if (window == 1) screen.setOffset(6 * 8, 0); // Offsets the windows for when the player's inventory is selected
		
		super.render(screen);
		//renderItemList(screen, 1, 1, 18, 11, chest.inventory.getItems(), window == 0 ? selected : -oSelected - 1); // renders all the items from the chest's inventory

		//renderFrame(screen, "inventory", 19, 1, 15 + 20, 11); // renders the player's inventory
		//renderItemList(screen, 19, 1, 15 + 20, 11, player.inventory.getItems(), window == 1 ? selected : -oSelected - 1); // renders all the items from the player's inventory
		
		// now render the other text.
		playerMenu.render(screen);
		
		screen.setOffset(0, 0); // Fixes the offset back to normal
	}
	
	public void onInvUpdate(Inventory inv) {
		super.onInvUpdate(inv);
		if(inv == playerMenu.inv)
			playerMenu.text = getItemList(inv);
	}
}
