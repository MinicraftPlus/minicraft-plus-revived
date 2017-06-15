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
	
	private static String username = "";
	private String typing;
	private String ipAddress = "localhost";
	
	public MultiplayerMenu(boolean isHost, Menu parent) {
		this.isHost = isHost;
		Game.ISONLINE = true;
		Game.ISHOST = isHost;
		this.parent = parent;
		if(username.length() == 0)
			typing = "Player";
	}
	
	public void tick() {
		if(Game.isConnectedClient() && !Game.ISHOST) {
			if (Game.debug) System.out.println("Begin game!");
			game.setMenu(new LoadingMenu());
			//game.initWorld();
			//game.setMenu(null);
		} else if(input.getKey("exit").clicked) {
			game.setMenu(parent);
			if(!Game.ISHOST)
				game.setMenu(new TitleMenu());
		}
		
		if(input.getKey("alt-U").clicked) {
			typing = username;
			username = "";
		}
		
		if(username.length() == 0) {
			checkKeyTyped(java.util.regex.Pattern.compile("[0-9A-Za-z \\-\\.]"));
			
			if(input.getKey("select").clicked) {
				username = typing;
				if(!isHost && !Game.isConnectedClient())
					typing = ipAddress;
				
				if(isHost) {
					Game.server = new MinicraftServer(game);
					Game.server.start();
				}
			}
		}
		else if(!Game.isValidClient()) {
			// this game instance is a client but not the host, but they haven't connected yet.
			checkKeyTyped(null);
			
			if(input.getKey("select").clicked) {
				Game.client = new MinicraftClient(game, ipAddress, username);
			}
		}
	}
	
	private void checkKeyTyped(java.util.regex.Pattern pattern) {
		if(input.lastKeyTyped.length() > 0) {
			String letter = input.lastKeyTyped;
			input.lastKeyTyped = "";
			//java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("[0-9\\.a-zA-Z\\/]");
			if(pattern == null || pattern.matcher(letter).matches())
				typing += letter;
		}
		
		if(input.getKey("backspace").clicked && typing.length() > 0)
			typing = typing.substring(0, typing.length()-1);
	}
	
	public void render(Screen screen) {
		screen.clear(0);
		
		if(username.length() == 0) {
			Font.drawCentered("Enter username to show others:", screen, screen.h/2-6, Color.get(-1, 555));
			Font.drawCentered(typing, screen, screen.h/2+6, Color.get(-1, 444));
		}
		else if(isHost) {
			if(game.isValidServer()) {
				//Font.drawCentered("Server IP Address:", screen, 20, Color.get(-1, 555));
				//Font.drawCentered(game.server.socket..getInetAddress().getHostAddress(), screen, 30, Color.get(-1, 151));
				Font.drawCentered("Awaiting client connections"+getElipses(), screen, 60, Color.get(-1, 444));
				Font.drawCentered("So far:", screen, 70, Color.get(-1, 444));
				int i = 0;
				for(String name: Game.server.getClientNames()) {
					Font.drawCentered(name, screen, 80+i*10, Color.get(-1, 134));
					i++;
				}
			} else {
				Font.drawCentered("Failed to establish server;", screen, screen.h/2-4, Color.get(-1, 522));
				Font.drawCentered("Exit menu and retry.", screen, screen.h/2+4, Color.get(-1, 522));
			}
			
			Font.drawCentered("Alt-U to change username", screen, 2, Color.get(-1, 222));
		}
		else {
			if(game.isValidClient()) {
				//System.out.println("client is valid");
				String msg = "Connecting to game on "+ipAddress+getElipses();
				
				if(Game.client.isConnected())
					msg = "Connection Successful!";
				else
					msg = "No connections available.";
				Font.drawCentered(msg, screen, screen.h/2, Color.get(-1, 555));
			} else {
				Font.drawCentered("Enter ip address to connect to:", screen, screen.h/2-6, Color.get(-1, 555));
				Font.drawCentered(ipAddress, screen, screen.h/2+6, Color.get(-1, 552));
				//Font.drawCentered("Invalid Client. Exit and retry.", screen, screen.h/2, Color.get(-1, 522));
			}
		}
		
		Font.drawCentered("Press "+input.getMapping("exit")+" to return", screen, screen.h-Font.textHeight()*2, Color.get(-1, 333));
		if(game.isValidServer())
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
