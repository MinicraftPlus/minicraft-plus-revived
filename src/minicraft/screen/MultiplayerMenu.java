package minicraft.screen;

import minicraft.Game;
import minicraft.gfx.Screen;
import minicraft.gfx.Font;
import minicraft.gfx.Color;
import minicraft.network.*;

public class MultiplayerMenu extends Menu {
	
	private boolean isHost;
	private Menu parent;
	
	public MultiplayerMenu(boolean isHost, Menu parent) {
		this.isHost = isHost;
		Game.ISHOST = isHost;
		Game.ISONLINE = true;
		this.parent = parent;
		if(isHost)
			Game.server = new MinicraftServer();
		else {
			Game.client = new MinicraftClient();
			System.out.println("attempting client connection; success="+(Game.client.socket!=null));
		}
	}
	
	public void tick() {
		if(input.getKey("exit").clicked) {
			game.setMenu(parent);
			Game.server = null;
			Game.client = null;
			game.ISONLINE = false;
		}
		
		if(input.getKey("select").clicked && isHost && Game.server.threadList.size() > 0) {
			// start actual multiplayer game broadcast
			
		}
	}
	
	public void render(Screen screen) {
		screen.clear(0);
		if(isHost) {
			Font.drawCentered("Awaiting client connections"+getElipses(), screen, 60, Color.get(-1, 555));
			Font.drawCentered("So far:", screen, 70, Color.get(-1, 555));
			int i = 0;
			for(MinicraftServerThread thread: Game.server.threadList) {
				Font.drawCentered(thread.toString(), screen, 80+i*10, Color.get(-1, 345));
				i++;
			}
		}
		else {
			if(!Game.client.done)
				Font.drawCentered("Connecting to game on localhost"+getElipses(), screen, screen.h/2, Color.get(-1, 555));
			if(Game.client.done)
				Font.drawCentered((Game.client.socket == null ? "No Connections available." : "Success!"), screen, screen.h/2, Color.get(-1, 555));
		}
	}
	
	private int ePos = 0;
	private int eposTick = 0;
	
	private String getElipses() {
		String dots = "";
		for(int i = 0; i < 3; i++) {
			if (ePos == i)
				dots += ".";
			else
				dots += " ";
		}
		
		eposTick++;
		if(eposTick >= Game.normSpeed) {
			eposTick = 0;
			ePos++;
		}
		if(ePos >= 3) ePos = 0;
		
		return dots;
	}
}
