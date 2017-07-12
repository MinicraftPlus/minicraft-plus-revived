package minicraft.screen;

import java.util.ArrayList;
import java.util.List;
import minicraft.Game;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.FontStyle;
import minicraft.gfx.Screen;
import minicraft.network.MinicraftClient;

public class MultiplayerMenu extends Menu {
	
	private List<String> takenNames = new ArrayList<String>();
	
	private String loadingMessage = "doing nothing";
	private String errorMessage = "";
	
	private String typing = "";
	private boolean inputIsValid = false;
	
	private static enum State {
		WAITING, ENTERIP, ENTERNAME, LOADING, ERROR
	}
	
	private State curState;
	
	public MultiplayerMenu() {
		Game.ISONLINE = true;
		Game.ISHOST = false;
		
		curState = State.ENTERIP;
	}
	
	// this automatically sets the ipAddress, and goes from there. it also assumes the game is a client.
	public MultiplayerMenu(Game game, String ipAddress) {
		this();
		curState = State.WAITING;
		Game.client = new MinicraftClient(game, this, ipAddress);
	}
	
	public void tick() {
		
		switch(curState) {
			case ENTERIP:
				checkKeyTyped(null);
				if(input.getKey("select").clicked) {
					curState = State.WAITING;
					Game.client = new MinicraftClient(game, this, typing); // typing = ipAddress
					typing = "";
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
					curState = State.WAITING;
					Game.client.login(typing); // typing = username
					typing = "";
					return;
				}
			break;
			
			case WAITING:
				/// this is just in case something gets set too early or something and the error state is overridden.
				if(errorMessage.length() > 0)
					curState = State.ERROR;
			break;
		}
		
		if(input.getKey("exit").clicked && !Game.ISHOST) {
			game.setMenu(new TitleMenu());
		}
	}
	
	private void checkKeyTyped(java.util.regex.Pattern pattern) {
		if(pattern == null) pattern = java.util.regex.Pattern.compile("[a-zA-Z0-9 \\-/_\\.:%\\?&,=]");
		if(input.lastKeyTyped.length() > 0) {
			String letter = input.lastKeyTyped;
			input.lastKeyTyped = "";
			//java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("[0-9\\.a-zA-Z\\/]");
			if(pattern == null || pattern.matcher(letter).matches())
				typing += letter;
		}
		
		if(input.getKey("backspace").clicked && typing.length() > 0) {
			// backspace counts as a letter itself, but it's not part of the regex
			typing = typing.substring(0, typing.length()-1);
		}
	}
	
	public void setTakenNames(List<String> names) {
		takenNames = names;
		typing = System.getProperty("user.name"); // a little trick for a nice default username. ;)
		curState = State.ENTERNAME;
	}
	
	public void setLoadingMessage(String msg) {
		curState = State.LOADING;
		loadingMessage = msg;
	}
	
	public void setError(String msg) {
		this.curState = State.ERROR;
		errorMessage = msg;
		if(game.menu != this && Game.isValidClient())
			game.setMenu(this);
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
				break;
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
