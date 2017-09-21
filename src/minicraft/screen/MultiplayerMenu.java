package minicraft.screen;

import java.io.InputStream;
import java.util.regex.Pattern;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;
import minicraft.Game;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.FontStyle;
import minicraft.gfx.Screen;
import minicraft.network.MinicraftClient;
import minicraft.saveload.Save;

import org.json.JSONObject;

public class MultiplayerMenu extends Display {
	
	private static String domain = "https://playminicraft.com";
	private static String apiDomain = domain+"/api";
	
	public static String savedIP = "";
	public static String savedUUID = "";
	public static String savedUsername = "";
	private String email = "";
	
	private String waitingMessage = "waiting...";
	private String loadingMessage = "nothing";
	private String errorMessage = "";
	
	private String typing = email;
	private boolean inputIsValid = false;
	
	private boolean online = false;
	private boolean typingEmail = true;
	
	private enum State {
		WAITING, LOGIN, ENTERIP, LOADING, ERROR
	}
	
	private State curState;
	
	public MultiplayerMenu() {
		super();
		Game.ISONLINE = true;
		Game.ISHOST = false;
		
		if(savedUUID == null) savedUUID = "";
		if(email == null) email = "";
		if(savedUsername == null) savedUsername = "";
		
		// HTTP REQUEST - determine if there is internet connectivity.
		//String url = "https://www.playminicraft.com"; // test request
		setWaitMessage("testing connection");
		
		Unirest.get(domain).asBinaryAsync(new Callback<InputStream>() {
			@Override
			public void completed(HttpResponse<InputStream> httpResponse) {
				if(httpResponse.getStatus() == 200)
					online = true;
				else
					System.out.println("warning: minicraft site ping returned status code " + httpResponse.getStatus());
				
				//if(Game.main.menu != MultiplayerMenu.this) return; // don't continue if the player moved to a different menu. 
				
				if(savedUUID.length() > 0) {
					// there is a previous login that can be used; check that it's valid
					setWaitMessage("attempting log in");
					//if (Game.debug) System.out.println("fetching username for uuid");
					fetchName(savedUUID);
				}
				
				if(curState == State.ERROR)
					return;
				
				// at this point, the game is online, and either the player could log in automatically, or has to enter their
				// email and password.
				
				if(savedUsername.length() == 0 || savedUUID.length() == 0)
					curState = State.LOGIN; // the player must log in manually.
				else {
					typing = savedIP;
					curState = State.ENTERIP; // the user has sufficient credentials; skip login phase
				}
			}
			
			@Override
			public void failed(UnirestException e) {
				e.printStackTrace();
				cancelled();
			}
			
			@Override
			public void cancelled() {
				if(savedUsername.length() == 0 || savedUUID.length() == 0) {
					// couldn't validate username, and can't enter offline mode b/c there is no username
					setError("no internet connection, but no login data saved; cannot enter offline mode.", false);
					//setError("could not access "+url);
					return;
				}
				
				// there is a saved copy of the uuid and username of the last player; use it for offline mode.
				curState = State.ENTERIP;
			}
		});
		
		//if(Game.debug) System.out.println("end of mm constructor");
	}
	
	// this automatically sets the ipAddress.
	public MultiplayerMenu(Game game, String ipAddress) {
		this();
		//if(Game.debug) System.out.println("ip mm constructor");
		this.game = game;
		if(curState == State.ENTERIP) { // login was automatic
			setWaitMessage("connecting to server");
			Game.client = new MinicraftClient(game, savedUsername,this, ipAddress);
		} else
			savedIP = ipAddress; // must login manually, so the ip address is saved for now.
	}
	
	public void tick() {
		
		switch(curState) {
			case LOGIN:
				checkKeyTyped(/*Pattern.compile("[0-9A-Za-z \\-\\.]")*/null);
				
				inputIsValid = typing.length() != 0;
				
				if(input.getKey("select").clicked && inputIsValid) {
					if(typingEmail) {
						typingEmail = false;
						email = typing;
						//new Save(game);
						typing = "";
					} else {
						login(email, typing); // typing = password
						typing = "";
					}
					return;
				}
			break;
			
			case ENTERIP:
				checkKeyTyped(/*Pattern.compile("[a-zA-Z0-9 \\-/_\\.:%\\?&=]")*/null);
				if(input.getKey("select").clicked) {
					setWaitMessage("connecting to server");
					savedIP = typing;
					Game.client = new MinicraftClient(game, savedUsername,this, typing); // typing = ipAddress
					new Save(game); // write the saved ip to file
					typing = "";
					return;
				} else if(input.getKey("shift-escape").clicked) {
					// logout
					MultiplayerMenu.savedUUID = "";
					MultiplayerMenu.savedUsername = "";
					new Save(game); // so the next time they start up the game to log in, it won't try to log in automatically.
					typing = "";
					curState = State.LOGIN;
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
	
	private void login(String email, String password) {
		setWaitMessage("logging in");
		
		/// HTTP REQUEST - send username and password to server via HTTPS, expecting a UUID in return.
		Unirest.post(apiDomain+"/login")
				.field("email", email)
				.field("password", password)
				.asJsonAsync(new Callback<JsonNode>() {
					@Override
					public void completed(HttpResponse<JsonNode> response) {
						JSONObject json = response.getBody().getObject();
						if(Game.debug) System.out.println("received json from login attempt: " + json.toString());
						switch(json.getString("status")) {
							case "error":
								setError(json.getString("message"), false); // in case the user abandoned the menu, don't drag them back.
								break;
							
							case "success":
								MultiplayerMenu.savedUUID = json.getString("uuid");
								MultiplayerMenu.savedUsername = json.getString("name");
								setWaitMessage("saving credentials");
								new Save(game);
								typing = MultiplayerMenu.savedIP;
								curState = State.ENTERIP;
								break;
						}
					}
					
					@Override
					public void failed(UnirestException e) {
						e.printStackTrace();
						cancelled();
					}
					
					@Override
					public void cancelled() {
						setError("login failed.", false);
					}
				});
	}
	
	private void fetchName(String uuid) {
		/// HTTP REQUEST - ATTEMPT TO SEND UUID TO SERVER AND UPDATE USERNAME
		HttpResponse<JsonNode> response = null;
		
		try {
			response = Unirest.post(apiDomain+"/fetch-name")
					.field("uuid", savedUUID)
					.asJson();
		} catch (UnirestException e) {
			e.printStackTrace();
		}
		
		if(response != null) {
			JSONObject json = response.getBody().getObject();
			//if(Game.debug) System.out.println("received json from username request: " + json.toString());
			switch(json.getString("status")) {
				case "error":
					setError("problem with saved login data; please exit and login again.", false);
					savedUUID = "";
					break;
				
				case "success":
					if(Game.debug) System.out.println("successfully received username from playminicraft server");
					savedUsername = json.getString("name");
					break;
			}
		} else// if(Game.debug)
			setError("Internal server error: Couldn't fetch username from uuid");
		//System.out.println("response to username fetch was null");
	}
	
	private static final Pattern control = Pattern.compile("\\p{Print}"); // should match only printable characters.
	private void checkKeyTyped(Pattern pattern) {
		if(input.lastKeyTyped.length() > 0) {
			String letter = input.lastKeyTyped;
			input.lastKeyTyped = "";
			if(control.matcher(letter).matches() && (pattern == null || pattern.matcher(letter).matches()))
				typing += letter;
		}
		
		if(input.getKey("backspace").clicked && typing.length() > 0) {
			// backspace counts as a letter itself, but we don't have to worry about it if it's part of the regex.
			typing = typing.substring(0, typing.length()-1);
		}
	}
	
	public void setWaitMessage(String message) {
		waitingMessage = message;
		curState = State.WAITING;
	}
	
	public void setLoadingMessage(String msg) {
		curState = State.LOADING;
		loadingMessage = msg;
	}
	
	public void setError(String msg) { setError(msg, true); }
	public void setError(String msg, boolean overrideMenu) {
		this.curState = State.ERROR;
		errorMessage = msg;
		if(overrideMenu && Game.main.menu != this && Game.isValidClient())
			Game.main.setMenu(this);
	}
	
	public void render(Screen screen) {
		screen.clear(0);
		
		switch(curState) {
			case ENTERIP:
				Font.drawCentered("logged in as: " + MultiplayerMenu.savedUsername, screen, 6, Color.get(-1, 252));
				
				if(!online)
					Font.drawCentered("offline mode: local servers only", screen, Screen.h/2 - Font.textHeight()*6,
							Color.get(-1, 335));
				
				Font.drawCentered("Enter ip address to connect to:", screen, Screen.h/2-Font.textHeight()-2, Color
						.get
						(-1, 555));
				Font.drawCentered(typing, screen, Screen.h/2, Color.get(-1, 552));
				
				Font.drawCentered("Press Shift-Escape to logout", screen, Screen.h-Font.textHeight()*7, Color.get
						(-1, 444));
				break;
			
			case LOGIN:
				String msg = "Enter email:";
				if(!typingEmail)
					msg = "Enter password:";
				Font.drawCentered(msg, screen, Screen.h/2-6, Color.get(-1, 555));
				
				msg = typing;
				if(!typingEmail)
					//noinspection ReplaceAllDot
					msg = msg.replaceAll(".", ".");
				Font.drawCentered(msg, screen, Screen.h/2+6, (inputIsValid?Color.get(-1, 444):Color.get(-1, 500)));
				if(!inputIsValid) {
					Font.drawCentered("field is blank", screen, Screen.h/2+20, Color.get(-1, 500));
				}
				
				Font.drawCentered("get an account at:", screen, Font.textHeight()/2-1, Color.get(-1, 345));
				Font.drawCentered(domain.substring(domain.indexOf("://")+3)+"/register", screen, Font.textHeight()*3/2, Color.get
						(-1, 345));
				
				break;
			
			case WAITING:
				Font.drawCentered(waitingMessage+getElipses(), screen, Screen.h/2, Color.get(-1, 555));
				break;
			
			case LOADING:
				Font.drawCentered("Loading "+loadingMessage+" from server"+getElipses(), screen, Screen.h/2, Color.get(-1, 555));
				//Font.drawCentered(transferPercent+"%", screen, Screen.h/2+6, Color.get(-1, 555));
				break;
			
			case ERROR:
				//if(Game.tickCount % 10 == 0) System.out.println("error message: " + errorMessage);
				Font.drawCentered("Could not connect to server:", screen, Screen.h/2-6, Color.get(-1, 500));
				FontStyle style = new FontStyle(Color.get(-1, 511));
				Font.drawParagraph(errorMessage, screen, 0, true, Screen.h/2+6, false, style, 1);
				//Font.drawCentered(errorMessage, screen, Screen.h/2+6, Color.get(-1, 511));
				break;
		}
		
		if(curState == State.ENTERIP || curState == State.ERROR) {
			Font.drawCentered("Press "+input.getMapping("exit")+" to return", screen, Screen.h-Font.textHeight()*2, Color.get(-1, 333));
		}
	}
	
	private int ePos = 0;
	private int eposTick = 0;
	
	private String getElipses() {
		StringBuilder dots = new StringBuilder();
		for(int i = 0; i < 3; i++) {
			if (ePos == i)
				dots.append(".");
			else
				dots.append(" ");
		}
		
		eposTick++;
		if(eposTick >= Game.normSpeed) {
			eposTick = 0;
			ePos++;
		}
		if(ePos >= 3) ePos = 0;
		
		return dots.toString();
	}
}
