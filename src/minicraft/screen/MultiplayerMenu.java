package minicraft.screen;

import minicraft.Game;
import minicraft.InputHandler;
import minicraft.entity.RemotePlayer;
import minicraft.gfx.Screen;
import minicraft.gfx.Font;
import minicraft.gfx.Color;
import minicraft.network.*;

public class MultiplayerMenu extends Menu {
	
	private boolean isHost;
	private Menu parent;
	
	public MultiplayerMenu(boolean isHost, Menu parent) {
		this.isHost = isHost;
		Game.ISONLINE = true;
		Game.ISHOST = isHost;
		this.parent = parent;
	}
	
	public void init(Game game, InputHandler input) {
		super.init(game, input);
		if(Game.client == null && Game.server == null) {
			if(isHost) Game.server = new MinicraftServer(game);
			else {
				Game.client = new MinicraftClient(game);
				Game.client.start();
			}
		}
		else if(Game.server != null) Game.server.checkSockets();
	}
	
	public void tick() {
		if(!isHost)
			System.out.println("valid client and connected: " + (game.isValidClient() && Game.client.isConnected()));
		if(game.isValidClient() && Game.client.isConnected()) {
			System.out.println("Begin game!");
			game.resetstartGame();
			game.setMenu(null);
		} else if(input.getKey("exit").clicked) {
			game.setMenu(parent);
			if(!Game.ISHOST) {
				System.out.println("quitting multiplayer mode on client side; exiting multiplayer menu");
				// this should be reached only on client runtimes when no connection has yet been established.
				Game.client = null;
				Game.ISONLINE = false;
			}
		}
	}
	
	public void render(Screen screen) {
		screen.clear(0);
		if(isHost) {
			if(game.isValidHost()) {
				Font.drawCentered("Awaiting client connections"+getElipses(), screen, 60, Color.get(-1, 555));
				Font.drawCentered("So far:", screen, 70, Color.get(-1, 555));
				int i = 0;
				for(MinicraftServerThread thread: Game.server.threadList) {
					Font.drawCentered(thread.getClientName(), screen, 80+i*10, Color.get(-1, 345));
					i++;
				}
			} else {
				Font.drawCentered("Failed to establish server;", screen, screen.h/2-4, Color.get(-1, 522));
				Font.drawCentered("Exit menu and retry.", screen, screen.h/2+4, Color.get(-1, 522));
			}
		}
		else {
			if(game.isValidClient()) {
				//System.out.println("client is valid");
				String msg = "_";
				if(!Game.client.done)
					msg = "Connecting to game on localhost"+getElipses();
				else if(Game.client.isConnected())
					msg = "Connection Successful!";
				else
					msg = "No connections available.";
				//System.out.println("client is connected: " + Game.client.isConnected());
				Font.drawCentered(msg, screen, screen.h/2, Color.get(-1, 555));
			} else
				Font.drawCentered("Invalid Client.", screen, screen.h/2, Color.get(-1, 522));
		}
		
		Font.drawCentered("Press "+input.getMapping("exit")+" to return", screen, screen.h-Font.textHeight()*2, Color.get(-1, 333));
		if(game.isValidHost())
			Font.drawCentered("(game will still be multiplayer)", screen, screen.h-Font.textHeight(), Color.get(-1, 333));
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
