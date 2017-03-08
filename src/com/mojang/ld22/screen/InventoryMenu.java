package com.mojang.ld22.screen;

import com.mojang.ld22.Game;
import com.mojang.ld22.entity.Player;
import com.mojang.ld22.gfx.Font;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.item.Item;
import com.mojang.ld22.sound.Sound;

public class InventoryMenu extends Menu {
	private Player player;
	private int selected = 0;
	
	public InventoryMenu(Player player) {
		this.player = player;
		
		if (player.activeItem != null) {
			player.inventory.items.add(0, player.activeItem);
			player.activeItem = null;
		}
	}
	
	public void tick() {
		if (input.getKey("menu").clicked) game.setMenu(null);
		
		if (input.getKey("up").clicked) selected--;
		if (input.getKey("down").clicked) selected++;
		if (input.getKey("up").clicked) Sound.pickup.play(); 
		if (input.getKey("down").clicked) Sound.pickup.play(); 
		
		int len = player.inventory.items.size();
		if (len == 0) selected = 0;
		if (selected < 0) selected += len;
		if (selected >= len) selected -= len;
		
		if (input.getKey("attack").clicked && len > 0) {
			Item item = player.inventory.items.remove(selected);
			if (item.getName() == "Fish Rod"){
				Game.truerod = true;
			} else {
				Game.truerod = false;
			}
			player.activeItem = item;
			game.setMenu(null);
		}
	}
	
	public void render(Screen screen) {
		Font.renderFrame(screen, "inventory", 1, 1, 20, 11);
		renderItemList(screen, 1, 1, 20, 11, player.inventory.items, selected);
	}
}