package minicraft.screen;

import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.FontStyle;
import minicraft.gfx.SpriteSheet;
import minicraft.gfx.Screen;

public class InstructionsDisplay extends Display {
	private Menu parent; // Creates a parent object to go back to
	
	private static final String[] info = Font.getLines("With the defualt controls...\n\nMove your character with arrow keys or wsad. Press C to attack and X to open the inventory, and to use items. Select an item in the inventory to equip it.\n\nKill the air wizard to win the game!", Screen.w-8, Screen.h - 4 * SpriteSheet.boxWidth, Font.textHeight());
	private static FontStyle style = new FontStyle(Color.get(-1, 333)).setYPos(4 * SpriteSheet.boxWidth);
	
	public InstructionsDisplay(Menu parent) {
		super().setStyle(style);
		text = info;
		
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
		
		super.render(screen);
		//Font.drawParagraph(, screen, 4, true, 4 * 8, false, style, 8);
	}
}
