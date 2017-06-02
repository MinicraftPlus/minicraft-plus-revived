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
	private String ipAddress = "localhost";
	
	public MultiplayerMenu(boolean isHost, Menu parent) {
		this.isHost = isHost;
		Game.ISONLINE = true;
		Game.ISHOST = isHost;
		this.parent = parent;
	}
	
	public void init(Game game, InputHandler input) {
		super.init(game, input);
		if(Game.client == null && Game.server == null) {
			if(isHost) Game.server = new MinicraftServer();
		}
		else if(Game.server != null) Game.server.checkSockets();
	}
	
	public void tick() {
		boolean isConnectedClient = game.isValidClient() && Game.client.isConnected();
		if(!isHost && Game.client != null && Game.debug)
			System.out.println("valid client and connected: " + isConnectedClient);
		
		if(isConnectedClient) {
			if (Game.debug) System.out.println("Begin game!");
			game.resetstartGame();
			game.setMenu(null);
		} else if(input.getKey("exit").clicked) {
			game.setMenu(parent);
			if(!Game.ISHOST) {
				if (Game.debug) System.out.println("quitting multiplayer mode on client side; exiting multiplayer menu");
				// this should be reached only on client runtimes when no connection has yet been established.
				if(Game.client != null) {
					Game.client.endConnection();
					Game.client = null;
				}
				Game.ISONLINE = false;
			}
		}
		
		if(!isHost && !Game.isValidClient()) {
			
			if(input.lastKeyTyped.length() > 0) {
				String letter = input.lastKeyTyped;
				input.lastKeyTyped = "";
				java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("[0-9\\.]");
				if(pattern.matcher(letter).matches())
					ipAddress += letter;
			}
			
			if(input.getKey("select").clicked) {
				Game.client = new MinicraftClient(ipAddress);
				Game.client.start();
			}
			
			if(input.getKey("backspace").clicked && ipAddress.length() > 0)
				ipAddress = ipAddress.substring(0, ipAddress.length()-1);
		}
	}
	
	public void render(Screen screen) {
		screen.clear(0);
		if(isHost) {
			if(game.isValidHost()) {
				//Font.drawCentered("Server IP Address:", screen, 20, Color.get(-1, 555));
				//Font.drawCentered(game.server.getAddress(), screen, 30, Color.get(-1, 151));
				Font.drawCentered("Awaiting client connections"+getElipses(), screen, 60, Color.get(-1, 444));
				Font.drawCentered("So far:", screen, 70, Color.get(-1, 444));
				int i = 0;
				for(MinicraftServerThread thread: Game.server.threadList) {
					Font.drawCentered(thread.getClientName(), screen, 80+i*10, Color.get(-1, 134));
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
			} else {
				Font.drawCentered("Enter ip address to connect to:", screen, screen.h/2-6, Color.get(-1, 555));
				Font.drawCentered(ipAddress, screen, screen.h/2+6, Color.get(-1, 552));
				//Font.drawCentered("Invalid Client. Exit and retry.", screen, screen.h/2, Color.get(-1, 522));
			}
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
