package minicraft.screen;

public class InventoryMenu extends ScrollingMenu {
	
	protected InventoryMenu(MenuData data, Frame... frames) {
		super(data, 9, 1, frames);
	}
	
	/* this should take control of:
	 * 
	 * - frames, but only defaults
	 * - allow extra rendering (done through render)
	 * 
	 */
}
