package minicraft.screen.entry;

import minicraft.InputHandler;
import minicraft.gfx.FontStyle;
import minicraft.gfx.Screen;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public abstract class ListEntry implements ActionListener {
	
	private javax.swing.Timer t = new javax.swing.Timer(100, this);
	protected ListEntry() {
		t.setRepeats(false);
		t.start();
	}
	
	public void tick(InputHandler input) {
		if(input.getKey("select").clicked)
			onSelect();
	}
	
	/**
	 * Called when the enter key is pressed when this entry is selected.
	 */
	public void onSelect() {
	}
	
	/**
	 * Called in the constructor, so that it can be extended, and additional behavior may be added without the need of a full new class declation.
	 */
	protected void setup() {
	}
	
	public void actionPerformed(ActionEvent e) {
		t.stop();
		setup();
	}
	
	//public abstract String getText(); // to fetch the text string to be displayed. 
	public abstract void render(Screen screen, FontStyle style);
}
