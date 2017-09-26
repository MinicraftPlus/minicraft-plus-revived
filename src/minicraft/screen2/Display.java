package minicraft.screen2;

import java.awt.Point;

import minicraft.InputHandler;
import minicraft.screen2.entry.ListEntry;

public abstract class Display implements MenuData {
	
	@Override
	public Menu getMenu() {
		return new Menu(this);
	}
	
	@Override
	public ListEntry[] getEntries() {
		return new ListEntry[0];
	}
	
	@Override
	public void tick(InputHandler input) {}
	
	@Override
	public boolean centerEntries() {
		return false;
	}
	
	@Override
	public int getSpacing() {
		return 0;
	}
	
	@Override
	public Point getAnchor() {
		return new Point();
	}
}
