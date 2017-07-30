package minicraft.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import minicraft.Game;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.FontStyle;
import minicraft.gfx.Screen;
import minicraft.network.MinicraftClient;
import minicraft.saveload.Save;

public class MultiplayerMenu extends Menu {
	
	//private List<String> takenNames = new ArrayList<String>();
	
	public static String savedIP = "";
	public static String lastUUID = "";
	public static String lastUsername = "";
	
	private String loadingMessage = "doing nothing";
	private String errorMessage = "";
	
	private String typing = savedIP;
	private boolean inputIsValid = false;
	
	private boolean online = false;
	private boolean typingUsername = true;
	
	private static enum State {
		WAITING, LOGIN, ENTERIP, LOADING, ERROR
	}
	
	private State curState;
	
	public MultiplayerMenu() {
		Game.ISONLINE = true;
		Game.ISHOST = false;
		
		if(savedUUID == null) savedUUID = "";
		if(savedUsername == null) savedUsername = "";
		
		curState = State.LOGIN;
		
		// TODO HTTP REQUEST - determine if there is internet connectivity.
		
		// online = ?;
		
		if(savedUUID.length() > 0) {
			// there is a previous login that can be used; check that it's valid
			
			/// TODO HTTP REQUEST - ATTEMPT TO SEND UUID TO SERVER AND UPDATE USERNAME
			
		}
		
		// at this point, the username has been updated, or couldn't be fetched. Or, there was no login.
		if(savedUsername.length() == 0 || savedUUID.length() == 0) {
			if(!online) {
				// couldn't validate username, and can't enter offline mode b/c there is no username
				setError("no login data saved, and no internet connection; cannot enter offline mode.");
				return;
			}
			
			// the user may login
		} else {
			// the user has sufficient credentials; skip login phase
			curState = State.ENTERIP;
		}
		
		if(curState == State.LOGIN)
			typing = savedUsername;
	}
	
	// this automatically sets the ipAddress.
	public MultiplayerMenu(Game game, String ipAddress) {
		this();
		if(curState == State.ENTERIP) {
			Game.client = new MinicraftClient(game, this, ipAddress);
			curState = State.WAITING;
		} else
			savedIP = ipAddress;
	}
	
	public void tick() {
		
		switch(curState) {
			case LOGIN:
				checkKeyTyped(Pattern.compile("[0-9A-Za-z \\-\\.]"));
				
				inputIsValid = true;
				if(typing.length() == 0)
					inputIsValid = false;
				else
				
				if(input.getKey("select").clicked && inputIsValid) {
					if(typingUsername) {
						typingUsername = false;
						savedUsername = typing;
						new Save(game);
						typing = "";
					} else {
						curState = State.WAITING;
						Game.client.login(savedUsername, typing); // typing = password
						typing = "";
					}
					return;
				}
			break;
			
			case ENTERIP:
				checkKeyTyped(Pattern.compile("[a-zA-Z0-9 \\-/_\\.:%\\?&=]"));
				if(input.getKey("select").clicked) {
					curState = State.WAITING;
					savedIP = typing;
					Game.client = new MinicraftClient(game, this, typing); // typing = ipAddress
					new Save(game); // write the saved ip to file
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
	
	private void checkKeyTyped(Pattern pattern) {
		if(input.lastKeyTyped.length() > 0) {
			String letter = input.lastKeyTyped;
			input.lastKeyTyped = "";
			if(pattern == null || pattern.matcher(letter).matches())
				typing += letter;
		}
		
		if(input.getKey("backspace").clicked && typing.length() > 0) {
			// backspace counts as a letter itself, but we don't have to worry about it if it's part of the regex.
			typing = typing.substring(0, typing.length()-1);
		}
	}
	
	/*public void setTakenNames(List<String> names) {
		takenNames = names;
		typing = System.getProperty("user.name"); // a little trick for a nice default username. ;)
		curState = State.ENTERNAME;
	}*/
	
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
			
			case LOGIN:
				String msg = "Enter username:";
				if(!typingUsername)
					msg = "Enter password:";
				Font.drawCentered(msg, screen, screen.h/2-6, Color.get(-1, 555));
				
				msg = typing;
				if(!typingUsername)
					msg = msg.replaceAll(".", "*");
				Font.drawCentered(typing, screen, screen.h/2+6, (inputIsValid?Color.get(-1, 444):Color.get(-1, 500)));
				if(!inputIsValid) {
					//String msg = "Username is taken";
					//if(typing.length() == 0)
						//msg = ;
					
					Font.drawCentered("field is blank", screen, screen.h/2+20, Color.get(-1, 500));
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
