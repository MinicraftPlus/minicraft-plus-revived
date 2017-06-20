package minicraft.screen;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.ArrayList;
import minicraft.Game;
import minicraft.InputHandler;
import minicraft.entity.RemotePlayer;
import minicraft.gfx.Screen;
import minicraft.gfx.Font;
import minicraft.gfx.FontStyle;
import minicraft.gfx.Color;
import minicraft.network.*;

public class MultiplayerMenu extends Menu {
	
	//private boolean isHost;
	public List<String> takenNames = new ArrayList<String>();
	
	public String loadingMessage = "nothing";
	public String errorMessage = "";
	
	public String typing = "";
	private boolean inputIsValid = false;
	
	public static enum State {
		WAITING, ENTERIP, ENTERNAME, LOADING, CONNECTED, ERROR
	}
	
	public State curState;
	
	public MultiplayerMenu() {
		//this.isHost = isHost;
		Game.ISONLINE = true;
		Game.ISHOST = false;
		
		curState = State.ENTERIP;
	}
	
	// this automatically sets the ipAddress, and goes from there. it also assumes the game is a client.
	public MultiplayerMenu(Game game, String ipAddress) {
		this();
		curState = State.WAITING;
		Game.client = new MinicraftClient(game, ipAddress);
	}
	
	public void tick() {
		
		switch(curState) {
			case CONNECTED:
				if (Game.debug) System.out.println("Begin game!");
				Game.levels[game.currentLevel].add(game.player);
				Game.readyToRenderGameplay = true;
				game.setMenu(null);
			return;
			
			case ENTERIP:
				checkKeyTyped(null);
				if(input.getKey("select").clicked) {
					Game.client = new MinicraftClient(game, typing); // typing = ipAddress
					typing = "";
					curState = State.WAITING;
					return;
				}
			break;
				
			case ENTERNAME:
				checkKeyTyped(java.util.regex.Pattern.compile("[0-9A-Za-z \\-\\.]"));
				
				inputIsValid = true;
				if(typing.length() == 0)
					inputIsValid = false;
				else
					for(String name: takenNames)
						if(name.equalsIgnoreCase(typing))
							inputIsValid = false;
				
				if(input.getKey("select").clicked && inputIsValid) {
					Game.client.login(typing); // typing = username
					typing = "";
					curState = State.WAITING;
					return;
				}
			break;
		}
		
		if(input.getKey("exit").clicked && !Game.ISHOST) {
			game.setMenu(new TitleMenu());
		}
		/*
		if(input.getKey("alt-U").clicked) {
			typing = username;
			username = "";
		}
		
		if(curState == State.ENTERNAME) {
			
		}
		else if(!Game.isValidClient()) {
			// this game instance is a client but not the host, but they haven't connected yet.
			checkKeyTyped(null);
			
			if(input.getKey("select").clicked) {
				Game.client = new MinicraftClient(game, ipAddress);
			}
		}*/
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
		
		switch(curState) {
			case ENTERIP:
				Font.drawCentered("Enter ip address to connect to:", screen, screen.h/2-6, Color.get(-1, 555));
				Font.drawCentered(typing, screen, screen.h/2+6, Color.get(-1, 552));
				break;
			
			case ENTERNAME:
				Font.drawCentered("Enter username to show others:", screen, screen.h/2-6, Color.get(-1, 555));
				Font.drawCentered(typing, screen, screen.h/2+6, (inputIsValid?Color.get(-1, 444):Color.get(-1, 500)));
				if(!inputIsValid) {
					String msg = "Username is taken";
					if(typing.length() == 0)
						msg = "Username cannot be blank";
					
					Font.drawCentered(msg, screen, screen.h/2+20, Color.get(-1, 500));
				}
				break;
			
			case WAITING:
				Font.drawCentered("Communicating with server"+getElipses(), screen, screen.h/2, Color.get(-1, 555));
				break;
			
			case CONNECTED:
				Font.drawCentered("Connection Successful!", screen, screen.h/2, Color.get(-1, 555));
				break;
			
			case LOADING:
				Font.drawCentered("Loading "+loadingMessage+" from server"+getElipses(), screen, screen.h/2, Color.get(-1, 555));
				//Font.drawCentered(transferPercent+"%", screen, screen.h/2+6, Color.get(-1, 555));
				break;
			
			case ERROR:
				//if(Game.tickCount % 10 == 0) System.out.println("error message: " + errorMessage);
				Font.drawCentered("Could not connect to server:", screen, screen.h/2-6, Color.get(-1, 500));
				FontStyle style = new FontStyle(Color.get(-1, 511));
				Font.drawParagraph(errorMessage, screen, 0, true, screen.h/2+6, false, style, 1);
				//Font.drawCentered(errorMessage, screen, screen.h/2+6, Color.get(-1, 511));
				return;
		}
		
		/*
		else if(isHost) {
			if(game.isValidServer()) {
				//Font.drawCentered("Server IP Address:", screen, 20, Color.get(-1, 555));
				//Font.drawCentered(game.server.socket..getInetAddress().getHostAddress(), screen, 30, Color.get(-1, 151));
				
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
				
				//Font.drawCentered("Invalid Client. Exit and retry.", screen, screen.h/2, Color.get(-1, 522));
			}
		}
		*/
		if(curState == State.ENTERIP || curState == State.ENTERNAME || curState == State.ERROR) {
			Font.drawCentered("Press "+input.getMapping("exit")+" to return", screen, screen.h-Font.textHeight()*2, Color.get(-1, 333));
		}
		//if(game.isValidServer())
			//Font.drawCentered("(game will still be multiplayer)", screen, screen.h-Font.textHeight(), Color.get(-1, 333));
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
