package minicraft.screen;

import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.FontStyle;
import minicraft.gfx.Screen;

public class InstructionsMenu extends Menu {
	private Menu parent; // Creates a parent object to go back to

	/** The about menu is a read menu about what you have to do in the game. Only contains text and a black background */
	public InstructionsMenu(Menu parent) {
		this.parent = parent; // The parent Menu that it will go back to.
	}

	public void tick() {
		if (input.getKey("exit").clicked || input.getKey("select").clicked) {
			game.setMenu(parent);  // If the user presses escape, it will go back to the parent menu.
		}
	}

	/** Renders the text on the screen */
	public void render(Screen screen) {
		screen.clear(0); // clears the screen to be a black color.
		
		Font.drawCentered("HOW TO PLAY", screen, 1 * 8, Color.get(-1, 555)); //draws Title text
		FontStyle style = new FontStyle(Color.get(-1, 333));
		Font.drawParagraph(
		  "With the defualt controls...\n\nMove your character with arrow keys or wsad. Press C to attack and X to open the inventory, and to use items. Select an item in the inventory to equip it.\n\nKill the air wizard to win the game!",
		  screen, 4, true, 4 * 8, false, style, 8);
	}
}
